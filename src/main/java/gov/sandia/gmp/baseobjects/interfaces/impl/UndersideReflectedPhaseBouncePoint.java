package gov.sandia.gmp.baseobjects.interfaces.impl;

import java.io.IOException;
import java.io.Serializable;

import gov.sandia.gmp.util.globals.Globals;

/**
 * Used by predictors that understand under-side reflection bounce points. The
 * class sets the state of a "bounce point" position to be either "Fixed" or
 * "Variable". The default is variable, in which case the predictor determines
 * the bounce point position using optimization to minimize the total ray travel
 * time. If the state is set to "Fixed" then predictor simply calculates the ray
 * using the fixed position and returns the result. The position used can be
 * determined by the predictor or passed in by the PredictionRequest. If the
 * PredictionRequest is set up containing a fixed bounce point position then
 * that position is used to calculate the ray. Otherwise, the predictor will
 * attempt to evaluate a static position using it's AK135 TauP bounce point
 * files. If a TauP bounce point file does not exist for the phase then the
 * predictor will throw an erro.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class UndersideReflectedPhaseBouncePoint implements Serializable {
	/**
	 * Used by predictors that understand under-side reflection bounce points. Used
	 * to fix the bounce point position to a fixed result.
	 */
	private boolean bouncePointPositionFixed = false;

	/**
	 * Used by predictors that understand under-side reflection bounce points. Used
	 * to fix the bounce point latitude (degrees).
	 */
	private double bpLat = Globals.TWO_PI;

	/**
	 * Used by predictors that understand under-side reflection bounce points. Used
	 * to fix the bounce point longitude (degrees).
	 */
	private double bpLon = 2.0 * Globals.TWO_PI;

	/**
	 * Default constructor. Sets the bounce point position state as "Variable".
	 */
	public UndersideReflectedPhaseBouncePoint() {
		setBouncePointPositionAsVariable();
	}

	/**
	 * Standard constructor. Sets the bounce point position state as "Fixed" or
	 * "Variable" depending on the input boolean fixed.
	 */
	public UndersideReflectedPhaseBouncePoint(boolean fixed) {
		if (fixed)
			setBouncePointPositionAsFixed();
		else
			setBouncePointPositionAsVariable();
	}

	/**
	 * Standard constructor. Sets the bounce point position state as "Fixed" to the
	 * input latdeg, londeg setting (in degrees). If latdeg or londeg are out of
	 * range then an exception is thrown.
	 */
	public UndersideReflectedPhaseBouncePoint(double latdeg, double londeg) throws IOException {
		setFixedBouncePointPositionDeg(latdeg, londeg);
	}

	/**
	 * Sets the predictors behavior to treat phases with bounce points as "fixed"
	 * (i.e. it is not evaluated through optimization).
	 */
	public void setBouncePointPositionAsFixed() {
		bouncePointPositionFixed = true;
	}

	/**
	 * Sets the predictors behavior to treat phases with bounce points as "variable"
	 * (i.e. it is evaluated through optimization).
	 */
	public void setBouncePointPositionAsVariable() {
		bouncePointPositionFixed = false;
		bpLat = Globals.TWO_PI;
		bpLon = 2.0 * Globals.TWO_PI;
	}

	/**
	 * Sets the bounce point position to the input latitude (lat) and longitude
	 * (lon) in degrees. If the inputs are valid (in range) then the bounce point
	 * fixed position is set and the boolean bouncePointPositionFixed is set to
	 * true. If either input is invalid no action is taken.
	 * 
	 * @param lat Latitude (deg) of bounce point.
	 * @param lon Longitude (deg) of bounce point.
	 */
	public void setFixedBouncePointPositionDeg(double lat, double lon) throws IOException {
		if (validLatitudeDegrees(lat) & validLongitudeDegrees(lon)) {
			bouncePointPositionFixed = true;
			bpLat = lat;
			bpLon = lon;
		} else
			throw new IOException("Invalid Bounce Point Position: " + lat + ", " + lon);
	}

	/**
	 * Returns true if the fixed bounce point has been set. This also implies that
	 * the bounce point state is "Fixed".
	 * 
	 * @return True if the fixed bounce point has been set.
	 */
	public boolean isFixedBouncePointPositionSet() {
		return !(bpLat == Globals.TWO_PI);
	}

	/**
	 * Returns true if the bounce point state is "Fixed". This does not imply that
	 * the bounce point position has been set. Call method
	 * isFixedBouncePointPositionSet() to determine if the bounce point fixed
	 * position has been set.
	 * 
	 * @return Returns true if the bounce point state is "Fixed".
	 */
	public boolean isBouncePointFixed() {
		return bouncePointPositionFixed;
	}

	/**
	 * Returns the fixed bounce point latitude.
	 * 
	 * @return The fixed bounce point latitude (deg).
	 */
	public double getFixedBouncePointLatitudeDeg() {
		return bpLat;
	}

	/**
	 * Returns the fixed bounce point longitude.
	 * 
	 * @return The fixed bounce point longitude (deg).
	 */
	public double getFixedBouncePointLongitudeDeg() {
		return bpLon;
	}

	public static boolean validLatitudeDegrees(double latdeg) {
		return (latdeg <= Globals.PI_OVR_TWO && latdeg >= -Globals.PI_OVR_TWO);
	}

	public static boolean validLongitudeDegrees(double londeg) {
		return (londeg <= Math.PI && londeg >= -Math.PI);
	}

}
