package gov.sandia.gmp.bender.level;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;

/**
 * <p>A LevelFraction object represents a Level within a GeoTessModel that
 * computes radii that are all at the same fractional radius within a major
 * layer, regardless of the position of the input GeoTessPosition argument. 
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * <p>Converted for use with GeoTessModel/GeoTessPosition Nov, 2014.</p>
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 2.0
 */
@SuppressWarnings("serial")
public class LevelFraction extends Level
{
	/**
	 * The fraction of the radius within a major layer of this Level.
	 */
	private double fraction;

	/**
	 * Constructor specifying major layer index and fraction. 
	 *
	 * @param majorLayerIndex The Major layer index of this Level.
	 * @param name            The name of this Level.
	 * @param fraction        The fractional radius within the major layer.
	 */
	public LevelFraction(int majorLayerIndex, String name, double fraction)
	{
		super(majorLayerIndex, name);
		this.fraction = fraction;
	}

	/**
	 * Constructor specifying major layer index and fraction. 
	 *
	 * @param layer
	 * @param fraction
	 */
	public LevelFraction(int majorLayerIndex, double fraction)
	{
		this(majorLayerIndex, 
				String.format("F_%d_%1.2f", majorLayerIndex, fraction), fraction);
	}

	/**
	 * Constructor specifying major layer index, name and fraction. 
	 *
	 * @param layer
	 * @param name
	 * @param fraction
	 */
	public LevelFraction(int majorLayerIndex, String name, double numerator, double denominator)
	{
		this(majorLayerIndex, name, numerator/denominator);
	}

	/**
	 * Constructor specifying major layer index and fraction. 
	 *
	 * @param layer
	 * @param fraction
	 */
	public LevelFraction(int majorLayerIndex, double numerator, double denominator)
	{
		this(majorLayerIndex, numerator/denominator);
	}

	/**
	 * Override of an abstract method that retrieves the radius of this Level
	 * object, as determined at the position of the specified input profile.

	 * @param profile The input profile used to retrieve the radius at this Level.
	 * @return The radius at this Level.
	 * @throws GeoTessException
	 */
	@Override
	public double getRadius(GeoTessPosition profile) throws GeoTessException
	{
		return constrainRadius(profile, 
				profile.getRadiusTop(majorLayerIndex-1) +
				profile.getLayerThickness(majorLayerIndex)*fraction);
	}

	/**
	 * Override of abstract method that retrieves the content of this level object
	 * and writes it to the supplied StringBuffer.
	 * 
	 * @param buf       The buffer within which the toString() is written.
	 * @param profile   The profile defining where Level radii are extracted.
	 * @param attribute The attributes to be output at the radius location.
	 */
	@Override
	public void toString(StringBuffer buf, GeoTessPosition profile,
			                 GeoAttributes attribute) 
	       throws GeoTessException
	{
		//GeoTessPosition gtp = GeoTessPosition.getGeoTessPosition(profile);
		buf.append(String.format(" %4d, %12s", majorLayerIndex, name));

		double r = getRadius(profile);
		profile.setRadius(majorLayerIndex, r);
		int attrIndx = profile.getModel().getMetaData().getAttributeIndex(attribute.name());
		buf.append(String.format(" z=%10.4f  r=%10.4f  d=%10.4f  v=%10.4f",
				fraction, r, getDepth(profile), 1. / profile.getValue(attrIndx)));
	}

	/**
	 * Returns the content of this Level object.
	 */
	@Override
	public String toString()
	{
		return String.format(
				"Fraction:   majorLayerIndex = %3d, fraction = %10.4f%n",
				majorLayerIndex, fraction);
	}
}
