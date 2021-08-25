// Module:        $RCSfile: EarthConst.java,v $
// Revision:      $Revision: 1.32 $
// Last Modified: $Date: 2013/09/06 00:02:42 $
// Last Check-in: $Author: sballar $

package gov.sandia.gmp.baseobjects.globals;

/**
 * This class defines earth constants for use by EMPS code.
 * 
 * Note: On a GRS80 ellipsoid, earth radius = 6371. km at latitudes where unit
 * vector is [0.8169307371701541, 0.0, +/-0.5767357893061854]. At such points,
 * the geographic latitude is +/- 35.402807205794716 degrees and the geocentric
 * latitude is +/- 35.221281425820400 degrees
 */
public class EarthConst {

	/**
	 * Average Earth radius in km.
	 */
	public static final double EARTH_RAD = 6371.0;

	/**
	 * Earth equatorial radius in km (GRS80 ellipsoid).
	 */
	public static final double EARTH_A = 6378.137;

	/**
	 * Ellipsoidal Earth flattening parameter (GRS80 ellipsoid).
	 */
	public static final double EARTH_F = 1.0 / 298.257222101;

	/**
	 * 1 - Earth axis ratio squared. EARTH_E is the eccentricity squared. EARTH_E =
	 * 0.006694380022900787
	 */
	public static final double EARTH_E = EARTH_F * (2.0 - EARTH_F);

	/**
	 * Ellipsoidal Earth axis ratio (GRS80 semi-minor over semi-major axes).
	 */
	public static final double EARTH_AXIS_RATIO = 1.0 - EARTH_F;

	/**
	 * Ellipsoidal Earth axis ratio squared (also = 1.0 - EARTH_E).
	 * EARTH_AXIS_RATIO_SQUARED = 0.9933056199770992
	 */
	public static final double EARTH_AXIS_RATIO_SQUARED = 1.0 - EARTH_E;

	/**
	 * Converts the input geodetic latitude to a geocentric latitude.
	 * 
	 * @param geodeticLat Geodetic latitude.
	 * @return Geocentric latitude.
	 */
	public static double geodeticToGeocentricLatitude(double geodeticLat) {
		return Math.atan(EARTH_AXIS_RATIO_SQUARED * Math.tan(geodeticLat));
	}

	/**
	 * Converts the input geocentric latitude to a geodetic latitude.
	 * 
	 * @param geocentricLat Geocentric latitude.
	 * @return Geodetic latitude.
	 */
	public static double geocentricToGeodeticLatitude(double geocentricLat) {
		return Math.atan(Math.tan(geocentricLat) / EARTH_AXIS_RATIO_SQUARED);
	}

}
