package gov.sandia.gmp.pcalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.PointMap;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.seismicitydepth.SeismicityDepthModel;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.Polygon;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class DataSourceGeoTess extends DataSource
{
	public DataSourceGeoTess(PCalc pcalc) throws Exception
	{
		super(pcalc);

		bucket.inputType = IOType.GEOTESS;

		pcalc.extractStaPhaseInfo(bucket, true);
		ReceiverInterface receiver = bucket.receivers.get(0);

		int modelDimensions = properties.containsKey("geotessDepthSpacing")  
				|| properties.containsKey("geotessDepths") ? 3 : 2;

		GeoTessPosition seismicity_depth = null;
		int seismicityDepthMinIndex = -1;
		int seismicityDepthMaxIndex = -1;

		double depthSpacing = Double.NaN;
		double[] depths = null;

		if (modelDimensions == 3)
		{
			if (properties.containsKey("geotessDepthSpacing")  
					&& properties.containsKey("geotessDepths"))
				throw new Exception("Cannot specify both geotessDepthSpacing and geotessDepths");

			if (properties.containsKey("geotessDepths"))
			{
				depths = properties.getDoubleArray("geotessDepths");
				if (depths.length == 0)
					throw new Exception(String.format("property geotessDepths = %s but must specify at least one depth",
							properties.getProperty("geotessDepths")));
			}

			if (properties.containsKey("geotessDepthSpacing"))
			{
				depthSpacing = properties.getDouble("geotessDepthSpacing");
				if (depthSpacing <= 0.)
					throw new Exception(String.format("property geotessDepthSpacing = %1.3f but must be > 0. when geotessModelDimensions = 3",
							depthSpacing));
				// if property seismicityDepthModel is specified, then the seismicity depth model will be loaded from the
				// the specified file.  If seismicityDepthModel is 'default', or is not specified, then the default model
				// will be loaded from the internal resources directory.
				seismicity_depth = SeismicityDepthModel.getGeoTessPosition(properties.getProperty("seismicityDepthModel", "default"));

				seismicityDepthMinIndex = seismicity_depth.getModel().getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MIN");
				seismicityDepthMaxIndex = seismicity_depth.getModel().getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MAX");

				if (log.isOutputOn())
				{
					log.writeln("Seismicity Depth Model: \n");
					//log.writeln(seismicity_depth.getModel());
					log.writeln(GeoTessModelUtils.statistics(seismicity_depth.getModel()));
					log.writeln();
				}
			}
		}

		// Create a MetaData object in which we can specify information
		// needed for model construction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// will repopulate the description in DataSinkGeoTess before writing to output file.
		metaData.setDescription("");

		// Specify a list of layer names delimited by semi-colons
		if (modelDimensions == 2)
			metaData.setLayerNames("surface");
		else
			metaData.setLayerNames("sesimicity_depth");

		String[] attributes = new String[pcalc.outputAttributes.size()];
		String[] units = new String[pcalc.outputAttributes.size()];
		for (int i=0; i< pcalc.outputAttributes.size(); ++i)
		{
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
		metaData.setModelSoftwareVersion("PCalc "+PCalc.getVersion());

		// specify the date when the model was generated.  This gets
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		metaData.setEarthShape(VectorGeo.earthShape);

		GeoTessGrid grid = null;

		if (pcalc.properties.containsKey("geotessInputGridFile"))
			grid = new GeoTessGrid(pcalc.properties.getFile("geotessInputGridFile"));
		else
		{
			PropertiesPlus gridProperties = new PropertiesPlus();
			gridProperties.setProperty("verbosity = 0");
			gridProperties.setProperty("gridConstructionMode = scratch");
			gridProperties.setProperty("nTessellations = 1");
			for (Entry<Object, Object> p : properties.entrySet())
            {
                String key = (String) p.getKey();
                if (key.startsWith("geotess") && !key.equals("geotessGridFile"))
                {
                    key = key.substring(7,8).toLowerCase()+key.substring(8);
                    gridProperties.setProperty(key,  (String) p.getValue());
                }
            }

            if (gridProperties.containsKey("polygons"))
            {
                String property = properties.getProperty("geotessPolygons");
                property = property.replaceAll("<site.lat>", String.format("%1.6f", receiver.getPosition().getLatDegrees()))
                        .replaceAll("<site.lon>", String.format("%1.6f", receiver.getPosition().getLonDegrees()));
                gridProperties.setProperty("polygons", property);
            }

            // apply rotation that will place grid vertex 0 at the station location.
            if (pcalc.properties.getBoolean("geotessRotateGridToStation", true))
                gridProperties.setProperty("rotateGrid",
                        String.format("%1.6f %1.6f",
                                receiver.getPosition().getLatDegrees(),
                                receiver.getPosition().getLonDegrees()));

            grid = (GeoTessGrid) GeoTessBuilderMain.run(gridProperties);
        }
        
        String outputType = properties.getProperty("outputType", "geotess");
        
        if (outputType.equalsIgnoreCase("libcorr3d"))
        {
        	boolean ok = pcalc.outputAttributes.size() == 2 
            		&& pcalc.outputAttributes.get(0) == GeoAttributes.TT_DELTA_AK135 
            		&& pcalc.outputAttributes.get(1) == GeoAttributes.TT_MODEL_UNCERTAINTY;
            		
        	if (!ok)
        	{
        		String outputAttributes = "";
        		for (GeoAttributes a : pcalc.outputAttributes)
        			outputAttributes = outputAttributes + ", "+a.toString().toLowerCase();
        		if (outputAttributes.length() > 2)
        			outputAttributes = outputAttributes.substring(2);
        		
        		throw new Exception(String.format("When outputType is %s outputAttributes must \n"
        				+ "equal tt_delta_ak135, tt_model_uncertainty but that is not the case.\n"
        				+ "outputAttributes = %s",
        				outputType, outputAttributes));
        	}

        	LibCorr3DModel model = new LibCorr3DModel(grid, metaData);
            ReceiverInterface r = bucket.receivers.get(0);
            model.setSite(r.getSta(), r.getOndate(), r.getOffdate(), r.getPosition().getLatDegrees(),
                    r.getPosition().getLonDegrees(), -r.getPosition().getDepth(), r.getStaName(),
                    r.getStaTypeString(), r.getRefsta(), r.getDnorth(), r.getDeast());

            model.setPhase(bucket.phases.get(0).toString());
            model.setSupportedPhases(bucket.supportedPhases);

            model.setBaseModel(properties.getProperty("lookup2dModel", "AK135"));
            model.setParameters(metaData.getAttributeNamesString());

            bucket.geotessModel = model;
        }
        else
        	bucket.geotessModel = new GeoTessModel(grid, metaData);
        
        double geotessActiveNodeRadius = properties.getDouble("geotessActiveNodeRadius", -1.);
        Polygon polygon;
        if (geotessActiveNodeRadius > 0.)
        	polygon = new Polygon(receiver.getPosition().getUnitVector(), 
        			Math.toRadians(geotessActiveNodeRadius), 100);
        else
        	polygon = new Polygon(true);

        double[] dataD = new double[bucket.geotessModel.getNAttributes()];
        Arrays.fill(dataD, Double.NaN);

        float[] dataF = new float[bucket.geotessModel.getNAttributes()];
        Arrays.fill(dataF, Float.NaN);

        // Populate the model with profiles.
        if (modelDimensions == 2)
        {
        	// this is a 2D model with data only at the surface of the earth
        	for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx)
        		if (polygon.contains(bucket.geotessModel.getGrid().getVertex(vtx)))
        			bucket.geotessModel.setProfile(vtx,
        					metaData.getDataType() == DataType.DOUBLE ?
        							Data.getDataDouble(dataD.clone()) :
        								Data.getDataFloat(dataF.clone()));
        		else
        			bucket.geotessModel.setProfile(vtx);
        }
        else if (depths != null)
        {
        	for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx)
        	{
        		// retrieve the unit vector corresponding to the current vertex
        		double[] vertex = bucket.geotessModel.getGrid().getVertex(vtx);

        		double earthRadius = VectorGeo.getEarthRadius(vertex);

        		float[] radii = new float[depths.length];
        		for (int i=0; i<depths.length; ++i)
        			radii[i] = (float) (earthRadius-depths[depths.length-i-1]);

        		if (polygon.contains(vertex))
        		{
        			if (metaData.getDataType() == DataType.DOUBLE)
        			{
        				double[][] rawData = new double[radii.length][];
        				for (int i = 0; i < radii.length; ++i)
        					rawData[i] = dataD.clone();
        				bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
        			}
        			else if (metaData.getDataType() == DataType.FLOAT)
        			{
        				float[][] rawData = new float[radii.length][];
        				for (int i = 0; i < radii.length; ++i)
        					rawData[i] = dataF.clone();
        				bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
        			}
        		}
        		else
        			bucket.geotessModel.setProfile(vtx, 0, radii); 
        	}
        }
        else
        {
        	for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx)
        	{
        		// retrieve the unit vector corresponding to the current vertex
        		double[] vertex = bucket.geotessModel.getGrid().getVertex(vtx);

        		seismicity_depth.set(vertex, 1e4);
        		double minDepth = seismicity_depth.getValue(seismicityDepthMinIndex);

        		// maxDepth can be no less than minDepth and no greater than 700 km.
        		double maxDepth = Math.min(700., Math.max(minDepth, seismicity_depth.getValue(seismicityDepthMaxIndex)));

        		double earthRadius = VectorGeo.getEarthRadius(vertex);

        		float[] radii = Globals.getArrayFloat(earthRadius-maxDepth, earthRadius-minDepth, depthSpacing);

        		if (polygon.contains(vertex))
        		{
        			if (metaData.getDataType() == DataType.DOUBLE)
        			{
        				double[][] rawData = new double[radii.length][];
        				for (int i = 0; i < radii.length; ++i)
        					rawData[i] = dataD.clone();
        				bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
        			}
        			else if (metaData.getDataType() == DataType.FLOAT)
        			{
        				float[][] rawData = new float[radii.length][];
        				for (int i = 0; i < radii.length; ++i)
        					rawData[i] = dataF.clone();
        				bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
        			}
        		}
        		else
        			bucket.geotessModel.setProfile(vtx, 0, radii);
        	}
        }

        PointMap pm = bucket.geotessModel.getPointMap();
        bucket.points = new ArrayList<GeoVector>(pm.size());

        if (bucket.geotessModel.is3D())
            for (int i=0; i<pm.size(); ++i)
                bucket.points.add(new GeoVector(pm.getPointUnitVector(i), pm.getPointRadius(i)));
        else
            for (int i=0; i<pm.size(); ++i)
                bucket.points.add(new GeoVector(pm.getPointUnitVector(i),
                        VectorGeo.getEarthRadius(pm.getPointUnitVector(i))));


    }

}