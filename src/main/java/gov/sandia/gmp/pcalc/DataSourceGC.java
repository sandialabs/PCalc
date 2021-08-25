package gov.sandia.gmp.pcalc;

import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

public class DataSourceGC extends DataSource
{
	public DataSourceGC(PCalc pcalc) throws Exception
	{
		super(pcalc);
		
		bucket.inputType = IOType.GREATCIRCLE;

		pcalc.extractDepthInfo(bucket);

		if (pcalc.application == Application.PREDICTIONS)
			pcalc.extractStaPhaseInfo(bucket, true);

		GeoVector gcStart = properties.getGeoVector("gcStart");

		bucket.greatCircle = null;
		
		if (properties.containsKey("gcEnd"))
			bucket.greatCircle = new GreatCircle(gcStart.getUnitVector(), properties.getGeoVector("gcEnd").getUnitVector());
		else if (properties.containsKey("gcAzimuth") && properties.containsKey("gcDistance"))
		{
			double azimuth = properties.getDouble("gcAzimuth", Globals.NA_VALUE);
			double distance = properties.getDouble("gcDistance", Globals.NA_VALUE);
			if (azimuth != Globals.NA_VALUE && distance != Globals.NA_VALUE)
				bucket.greatCircle = new GreatCircle(gcStart.getUnitVector(), 
						VectorUnit.move(gcStart.getUnitVector(), Math.toRadians(distance), Math.toRadians(azimuth)));
		}

		if (bucket.greatCircle == null)
			throw new GMPException("\nNot enough information in property file to create great circle. \n"
					+"Must specify gcStart and either gcEnd, or gcDistance and gcAzimuth \n");

		double gcFirstDistance = properties.getDouble("gcFirstDistance", 0.);
		double gcLastDistance = properties.getDouble("gcLastDistance", bucket.greatCircle.getDistanceDegrees());
		int nPoints = -1;
		boolean gcOnCenters = properties.getBoolean("gcOnCenters", false);
		
		if (properties.containsKey("gcNpoints"))
			nPoints = properties.getInt("gcNpoints");
		else if (properties.containsKey("gcSpacing"))
			nPoints = (int)Math.ceil((gcLastDistance-gcFirstDistance)/properties.getDouble("gcSpacing"))
			+ (gcOnCenters ? 0 : 1);
		else
			throw new GMPException(String.format("%nMust specify either gcSpacing or gcNpoints%n"));
		
		double spacing = (gcLastDistance-gcFirstDistance)/(nPoints-1);
		
		bucket.points = new ArrayList<GeoVector>(nPoints);
		for (int i=0; i<nPoints; ++i)
		{
			double d = gcFirstDistance + (i + (gcOnCenters ? 0.5 : 0.)) *spacing;
			bucket.points.add(new GeoVector(bucket.greatCircle.getPoint(Math.toRadians(d)), Double.NaN));
		}
		
		String[] parameters = properties.getProperty("gcPositionParameters", "")
				.replaceAll(",", " ").split("\\s+");
		
		bucket.positionParameters = new ArrayList<GeoAttributes>();
		
		for (String attribute : parameters)
		{
			attribute = attribute.trim();
			if (attribute.length() > 0)
			try
			{
				bucket.inputAttributes.add(attribute.trim().toLowerCase());
				bucket.positionParameters.add(GeoAttributes.valueOf(attribute.toUpperCase()));
			} 
			catch (java.lang.IllegalArgumentException ex1)
			{
				throw new GMPException(String.format("%nProperty gcPositionParameters contains invalid parameter %s%n", attribute));
			}
		}
		
		// build the input header from the list of column names
		setInputHeader();

	}

}
