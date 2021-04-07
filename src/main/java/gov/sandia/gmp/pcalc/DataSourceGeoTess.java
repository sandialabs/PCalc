/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *	BSD Open Source License.
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *
 *	1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *	2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *	3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *	4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc;

import gov.sandia.geotess.*;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

public class DataSourceGeoTess extends DataSource {
    public DataSourceGeoTess(PCalc pcalc) throws Exception {
        super(pcalc);

        bucket.inputType = InputType.GEOTESS;

        pcalc.extractStaPhaseInfo(bucket, true);
        ReceiverInterface receiver = bucket.receivers.get(0);

        SeismicPhase phase = bucket.phases.get(0);

        int modelDimensions = properties.getInt("geotessModelDimensions", -1);

        if (modelDimensions == -1)
            throw new Exception("property geotessModelDimensions is not specified in the properties file.  Must equal either 2 or 3.");

        if (modelDimensions != 2 && modelDimensions != 3)
            throw new Exception("property geotessModelDimensions = " + modelDimensions + " is invalid.  Valid values are 2 or 3");

        GeoTessPosition seismicity_depth = null;
        double depthSpacing = Double.NaN;

        if (modelDimensions == 3) {
            depthSpacing = properties.getDouble("geotessDepthSpacing", Double.NaN);
            if (Double.isNaN(depthSpacing))
                throw new Exception("property geotessDepthSpacing must be specified in the properties file when geotessModelDimensions = 3");
            if (depthSpacing <= 0.)
                throw new Exception(String.format("property geotessDepthSpacing = %1.3f but must be > 0. when geotessModelDimensions = 3",
                        depthSpacing));

            InputStream s = getClass().getResourceAsStream("/seismicity_depth.geotess");
            if (s == null)
                throw new Exception("Resource seismicity_depth.geotess not found.");
            GeoTessModel seismicityDepthModel = new GeoTessModel(new DataInputStream(s));
            seismicity_depth = seismicityDepthModel.getGeoTessPosition();
        }

        // Create a MetaData object in which we can specify information
        // needed for model construction.
        GeoTessMetaData metaData = new GeoTessMetaData();

        // Specify a description of the model.
        StringBuffer description = new StringBuffer(properties.getProperty("geotessDescription",
                "GeoTessModel constructed by PCalc containing predicted values"));
        description.append("\n\nPhase = ").append(phase.toString()).append("\n");
        description.append("Receiver = ").append(receiver.getSiteRow().toString()).append("\n");
        metaData.setDescription(description.toString());

        // Specify a list of layer names delimited by semi-colons
        if (modelDimensions == 2)
            metaData.setLayerNames("surface");
        else
            metaData.setLayerNames("sesimicity_depth");

        String[] attributes = new String[pcalc.outputAttributes.size()];
        String[] units = new String[pcalc.outputAttributes.size()];
        for (int i = 0; i < pcalc.outputAttributes.size(); ++i) {
            attributes[i] = pcalc.outputAttributes.get(i).toString();
            units[i] = pcalc.outputAttributes.get(i).getUnits();
        }
        metaData.setAttributes(attributes, units);

        // specify the DataType for the data. All attributes, in all
        // profiles, will have the same data type.  Note that this
        // applies only to the data; radii are always stored as floats.
        metaData.setDataType(DataType.valueOf(
                properties.getProperty("geotessDataType", "FLOAT").toUpperCase()));

        // specify the name of the software that is going to generate
        // the model.  This gets stored in the model for future reference.
        metaData.setModelSoftwareVersion(getClass().getCanonicalName());

        // specify the date when the model was generated.  This gets
        // stored in the model for future reference.
        metaData.setModelGenerationDate(new Date().toString());

        metaData.setEarthShape(VectorGeo.earthShape);

        GeoTessGrid grid = null;

        if (pcalc.properties.containsKey("geotessInputGridFile"))
            grid = new GeoTessGrid(pcalc.properties.getFile("geotessInputGridFile"));
        else {
            PropertiesPlus gridProperties = new PropertiesPlus();
            gridProperties.setProperty("verbosity = 0");
            gridProperties.setProperty("gridConstructionMode = scratch");
            gridProperties.setProperty("nTessellations = 1");
            for (Entry<Object, Object> p : properties.entrySet()) {
                String key = (String) p.getKey();
                if (key.startsWith("geotess") && !key.equals("geotessGridFile")) {
                    key = key.substring(7, 8).toLowerCase() + key.substring(8);
                    gridProperties.setProperty(key, (String) p.getValue());
                }
            }

            // apply euler rotation that will place grid vertex 0 at the station location.
            if (pcalc.properties.getBoolean("geotessRotateGridToStation", true))
                gridProperties.setProperty("eulerRotationAngles",
                        String.format("%1.6f, %1.6f, 90.",
                                90. + receiver.getPosition().getLonDegrees(),
                                90. - receiver.getPosition().getGeocentricLatDegrees()));


            grid = (GeoTessGrid) GeoTessBuilderMain.run(gridProperties);
        }

        // call a GeoTessModel constructor to build the model.
        bucket.geotessModel = new GeoTessModel(grid, metaData);

        double[] dataD = new double[bucket.geotessModel.getNAttributes()];
        Arrays.fill(dataD, Double.NaN);

        float[] dataF = new float[bucket.geotessModel.getNAttributes()];
        Arrays.fill(dataF, Float.NaN);

        // Populate the model with profiles.
        for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx) {
            if (modelDimensions == 2)
                // this is a 2D model with data only at the surface of the earth
                bucket.geotessModel.setProfile(vtx,
                        metaData.getDataType() == DataType.DOUBLE ?
                                Data.getDataDouble(dataD.clone()) :
                                Data.getDataFloat(dataF.clone()));
            else {
                // retrieve the unit vector corresponding to the current vertex
                double[] vertex = bucket.geotessModel.getGrid().getVertex(vtx);

                float rsurface = (float) VectorGeo.getEarthRadius(vertex);
                float rdepth = rsurface - Math.min(699.999F, (float) seismicity_depth.set(vertex, rsurface).getValue(2));

                float[] radii = Globals.getArrayFloat(rdepth, rsurface, depthSpacing);

                if (metaData.getDataType() == DataType.DOUBLE) {
                    double[][] rawData = new double[radii.length][];
                    for (int i = 0; i < radii.length; ++i)
                        rawData[i] = dataD.clone();
                    bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
                } else if (metaData.getDataType() == DataType.FLOAT) {
                    float[][] rawData = new float[radii.length][];
                    for (int i = 0; i < radii.length; ++i)
                        rawData[i] = dataF.clone();
                    bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
                }
            }
        }

        if (log.isOutputOn())
            log.writeln(bucket.geotessModel);

        PointMap pm = bucket.geotessModel.getPointMap();
        bucket.points = new ArrayList<GeoVector>(pm.size());

        if (bucket.geotessModel.is3D())
            for (int i = 0; i < pm.size(); ++i)
                bucket.points.add(new GeoVector(pm.getPointUnitVector(i), pm.getPointRadius(i)));
        else
            for (int i = 0; i < pm.size(); ++i)
                bucket.points.add(new GeoVector(pm.getPointUnitVector(i),
                        VectorGeo.getEarthRadius(pm.getPointUnitVector(i))));


    }

}
