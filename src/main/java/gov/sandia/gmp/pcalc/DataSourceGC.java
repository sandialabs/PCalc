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

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

import java.util.ArrayList;

public class DataSourceGC extends DataSource {
    public DataSourceGC(PCalc pcalc) throws Exception {
        super(pcalc);

        bucket.inputType = InputType.GREATCIRCLE;

        pcalc.extractDepthInfo(bucket);

        if (pcalc.application == Application.PREDICTIONS)
            pcalc.extractStaPhaseInfo(bucket, true);

        GeoVector gcStart = properties.getGeoVector("gcStart");

        bucket.greatCircle = null;

        if (properties.getProperty("gcEnd") != null)
            bucket.greatCircle = new GreatCircle(gcStart.getUnitVector(), properties.getGeoVector("gcEnd").getUnitVector());
        else {
            double azimuth = Math.toRadians(properties.getDouble("gcAzimuth", Globals.NA_VALUE));
            double distance = Math.toRadians(properties.getDouble("gcDistance", Globals.NA_VALUE));
            if (azimuth != Globals.NA_VALUE && distance != Globals.NA_VALUE)
                bucket.greatCircle = new GreatCircle(gcStart.getUnitVector(),
                        VectorUnit.move(gcStart.getUnitVector(), distance, azimuth));
        }

        if (bucket.greatCircle == null)
            throw new GMPException("\nNot enough information in property file to create great circle. \n"
                    + "Must specify gcStart and either gcEnd, or gcDistance and gcAzimuth \n");

        ArrayList<double[]> points = null;

        if (properties.getProperty("gcSpacing") != null) {
            double gcSpacing = Math.toRadians(properties.getDouble("gcSpacing"));
            boolean gcOnCenters = properties.getBoolean("gcOnCenters", false);
            int nPoints = (int) Math.ceil(bucket.greatCircle.getDistance() / gcSpacing)
                    + (gcOnCenters ? 0 : 1);
            points = bucket.greatCircle.getPoints(nPoints, gcOnCenters);
        } else if (properties.getProperty("gcNpoints") != null)
            points = bucket.greatCircle.getPoints(properties.getInt("gcNpoints"),
                    properties.getBoolean("gcOnCenters", false));
        else
            throw new GMPException(String.format("%nMust specify either gcSpacing or gcNpoints%n"));

        bucket.points = new ArrayList<GeoVector>(points.size());
        for (double[] point : points)
            bucket.points.add(new GeoVector(point, Double.NaN));

        String[] parameters = properties.getProperty("gcPositionParameters", "")
                .replaceAll(",", " ").replaceAll("  ", " ").split(" ");

        bucket.positionParameters = new ArrayList<GeoAttributes>();

        for (String attribute : parameters) {
            attribute = attribute.trim();
            if (attribute.length() > 0)
                try {
                    bucket.inputAttributes.add(attribute.trim().toLowerCase());
                    bucket.positionParameters.add(GeoAttributes.valueOf(attribute.toUpperCase()));
                } catch (java.lang.IllegalArgumentException ex1) {
                    throw new GMPException(String.format("%nProperty gcPositionParameters contains invalid parameter %s%n", attribute));
                }
        }

        // build the input header from the list of column names
        setInputHeader();

    }

}
