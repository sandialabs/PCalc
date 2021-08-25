package gov.sandia.gmp.baseobjects.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

public interface PredictionInterface {

	/**
	 * The ID of the Observation that is to have a predicted value calculated.
	 * 
	 * @return long
	 */
	long getObservationId();

	/**
	 * For internal use by Predictors. Applications must supply a requestId
	 * variable, but should assume that Predictor objects will modify it.
	 * 
	 * @return int
	 */
	int getRequestId();

	/**
	 *
	 * @return SeismicPhase
	 */
	SeismicPhase getPhase();

	/**
	 * getReceiver returns the RayInfo node at the receiver
	 *
	 * @return RayInfoNode
	 */
	ReceiverInterface getReceiver();

	/**
	 * getSource returns the RayInfo node at the source
	 *
	 * @return RayInfoNode
	 */
	SourceInterface getSource();

	/**
	 * Retrieve source-receiver epicentral distance, in radians.
	 * 
	 * @return
	 */
	double getDistance();

	/**
	 * RayType is one of REFRACTED, REFLECTION, DIFFRACTION, INVALID, ERROR
	 * 
	 * @return one of REFRACTED, REFLECTION, DIFFRACTION, INVALID, ERROR
	 */
	RayType getRayType();

	/**
	 * Returns true if no errors occurred during calculation of the prediction. If
	 * false, then getErrorMessage() should return a message indicating what the
	 * problem was.
	 *
	 * @return true if no errors occurred during calculation of the prediction.
	 */
	boolean isValid();

	/**
	 * Returns an error message, if one was generated.
	 * 
	 * @return String
	 */
	String getErrorMessage();

	/**
	 * An EnumMap containing GeoAttributes and computed values. The keySet will be a
	 * subset of all the GeoAttributes that are supported by a Predictor (see
	 * PredictorInterface.getSupportedGeoAttributes(). The keySet will contain all
	 * the GeoAttributes that were requested of the Predictor object in a
	 * PredictionRequest object. Some of the results may be invalid. It will not
	 * contain any GeoAttributes that were not requested.
	 * 
	 * @return EnumMap<GeoAttributes, Double>
	 */
	EnumMap<GeoAttributes, Double> getSupportedAttributes();

	/**
	 * Retrieve the value of the specified GeoAttribute. Returns BaseConst.NA_VALUE
	 * if an unsupported GeoAttribute is requested.
	 *
	 * @param attribute GeoAttributes
	 * @return double
	 */
	double getAttribute(GeoAttributes attribute);

	/**
	 * Retrieve an array containing the values of some subset of GeoAttributes.
	 * Unsupported or invalid attributes are returned with value BaseConst.NA_VALUE;
	 * 
	 * @return double[]
	 */
	double[] getAttributes(GeoAttributes[] attributes);

	/**
	 * Retrieve a map from GeoAttribute to Double value for a set of attributes.
	 * 
	 * @param attributes
	 * @return GeoAttributeValues
	 */
	EnumMap<GeoAttributes, Double> getAttributes(EnumSet<GeoAttributes> attributes);

	/**
	 * Retrieve a map from GeoAttribute to Double value for a set of attributes.
	 * 
	 * @param attributes
	 */
	void getAttributes(EnumSet<GeoAttributes> attributes, EnumMap<GeoAttributes, Double> values);

	/**
	 * Set the value of the specified GeoAttributes.
	 * 
	 * @param attribute
	 * @param value
	 */
	void setAttribute(GeoAttributes attribute, double value);

	/**
	 * toString returns the information content of this RayInfo object formatted in
	 * a String.
	 *
	 * @return String
	 */
	String toString();

	/**
	 * @return the name of the Earth model used to generate this Prediction
	 */
	String getModelName();

	/**
	 * @return the name of the Predictor used to generate this Prediction
	 */
	String getPredictorName();

	/**
	 * If the Uncertainty object that supports this Prediciton object gets
	 * uncertainty values from lookup tables, then retrieve the lookup table index
	 * that supports the suppled attribute, which must be one of
	 * TT_MODEL_UNCERTAINTY, SLOWNESS_MODEL_UNCERTAINTY or
	 * AZIMUTH_MODEL_UNCERTAINTY. Otherwise, returns -1.
	 * 
	 * @param attribute
	 * @return retrieve the lookup table index that supports the suppled attribute
	 */
	int getLookupIndexUncertainty(GeoAttributes attribute);

	/**
	 * @return the lookupIndexUncertaintyTT
	 */
	int getLookupIndexUncertaintyTT();

	/**
	 * @param lookupIndexUncertaintyTT the lookupIndexUncertaintyTT to set
	 */
	// void setLookupIndexUncertaintyTT(int lookupIndexUncertaintyTT);

	/**
	 * @return the lookupIndexUncertaintyAZ
	 */
	int getLookupIndexUncertaintyAZ();

	/**
	 * @param lookupIndexUncertaintyAZ the lookupIndexUncertaintyAZ to set
	 */
	// void setLookupIndexUncertaintyAZ(int lookupIndexUncertaintyAZ);

	/**
	 * @return the lookupIndexUncertaintySH
	 */
	int getLookupIndexUncertaintySH();

	PredictionRequestInterface getPredictionRequest();

	int getPredictorIndex();

	/**
	 * Returns the predictions active node indexes.
	 * 
	 * @return The predictions active node indexes.
	 */
	public int[] getRayWeightIndexes();

	/**
	 * Returns the predictions active node weights.
	 * 
	 * @return The predictions active node weights.
	 */
	public double[] getRayWeights();

	/**
	 * Returns the predictions ray path as a list of GeoVector objects.
	 * 
	 * @return The predictions ray path as a list of GeoVector objects.
	 */
	public ArrayList<GeoVector> getRayPath();

	/**
	 * Returns the predictions list of ray path positions as a list of unit vectors
	 * and a list of radii. The input lists are cleared before filling with the ray
	 * path data.
	 * 
	 * @param unitVectors The predictions list of ray path unit vectors on return.
	 * @param radii       The predictions list of ray path radii on return.
	 */
	public void getRayPath(ArrayList<double[]> unitVectors, ArrayListDouble radii);

	/**
	 * Nullifies the internal list of ray path unit vectors and radii.
	 */
	public void nullifyRayPath();

	/**
	 * @param lookupIndexUncertaintySH the lookupIndexUncertaintySH to set
	 */
	// void setLookupIndexUncertaintySH(int lookupIndexUncertaintySH);

	/**
	 * Writes the data for this prediction to the input FileOutputBuffer. The buffer
	 * is assumed to be open on entry and is not closed on exit.
	 * 
	 * @param fob The FileOutputBuffer into which this predictions data is written.
	 * @throws IOException
	 */
	public void writePrediction(FileOutputBuffer fob) throws IOException;
}
