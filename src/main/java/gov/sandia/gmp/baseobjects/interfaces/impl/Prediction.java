/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.baseobjects.interfaces.impl;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;

/*
 * A container class to store predicted values of seismic observables in an
 * EnumMap of GeoAttributes -> Double
 * It doesn't process the information in anyway but simply stores the values
 * and supplies getters and setters.  This class, and other classes 
 * derived off of it, are appropriate for serialization and transmission
 * across networks. 
 */
public class Prediction implements PredictionInterface, Serializable {

	private static final long serialVersionUID = -6015387032594709889L;

	private static int nextIndex = 0;

	private final int predictorIndex;

	private PredictionRequestInterface predictionRequest;

	/**
	 * An EnumMap containing GeoAttributes and computed values. The keySet will be a
	 * subset of all the GeoAttributes that are supported by a Predictor (see
	 * PredictorInterface.getSupportedGeoAttributes(). The keySet will contain all
	 * the GeoAttributes that were requested of the Predictor object in a
	 * PredictionRequest object. Some of the results may be invalid. It will not
	 * contain any GeoAttributes that were not requested.
	 */
	private EnumMap<GeoAttributes, Double> values = new EnumMap<GeoAttributes, Double>(GeoAttributes.class);

	private String modelName = "-";

	private String predictorName = "-";

	/**
	 * Error message that can be set by PredictorInterface objects
	 */
	protected String errorMessage;

	/**
	 * Maximum error message length. If an application calls setErrorMessage() with
	 * message longer than this length, then middle of message is replaced with "
	 * ... " such that the length of the message is reduced to
	 * maxErrorMessageLength.
	 */
	public final static int maxErrorMessageLength = 10000;

	/**
	 * What type of ray this is: INVALID, ERROR, REFRACTION, DIFFRACTION,
	 * REFLECTION.
	 */
	private RayType rayType = RayType.INVALID;

	public Prediction() {
		predictorIndex = nextIndex++;
	}

	public Prediction(PredictionRequestInterface request, PredictorInterface predictor) {
		predictorIndex = nextIndex++;
		this.predictionRequest = request;
		for (GeoAttributes attribute : request.getRequestedAttributes())
			values.put(attribute, Globals.NA_VALUE);
		if (predictor != null) {
			modelName = predictor.getModelName();
			predictorName = predictor.getPredictorName();
		}

	}

	/**
	 * Constructor to be used in the case where a Prediction calculation failed for
	 * some reason. getRayType() will return RayType.INVALID;
	 * 
	 * @param predictionRequest
	 * @param message
	 */
	public Prediction(PredictionRequestInterface predictionRequest, PredictorInterface predictor, String message) {
		this(predictionRequest, predictor);
		setErrorMessage(message);
		rayType = RayType.INVALID;
	}

	/**
	 * Constructor to be used when Prediction calculation failed.
	 * 
	 * @param predictionRequest
	 * @param ex
	 */
	public Prediction(PredictionRequestInterface predictionRequest, PredictorInterface predictor, Exception ex) {
		this(predictionRequest, predictor);
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("    %s%n", ex.getClass().getName()));
		if (ex.getMessage() != null)
			buf.append(String.format("    %s%n", ex.getMessage()));

		buf.append(String.format("Rcvr; Src; Phase = %s; %s; %s  Delta = %1.4f%n",
				((GeoVector) predictionRequest.getReceiver()).geovectorToString(),
				((GeoVector) predictionRequest.getSource()).geovectorToString(),
				predictionRequest.getPhase().toString(), predictionRequest.getReceiver().getPosition()
						.distanceDegrees(predictionRequest.getSource().getPosition())));
		for (StackTraceElement trace : ex.getStackTrace())
			buf.append(String.format("        at %s%n", trace));
		setErrorMessage(buf.toString());
		rayType = RayType.ERROR;
	}

	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getPredictorName() {
		return predictorName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public void setPredictorName(String predictorName) {
		this.predictorName = predictorName;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage == null ? "" : errorMessage;
	}

	@Override
	public long getObservationId() {
		return predictionRequest.getObservationId();
	}

	@Override
	public SeismicPhase getPhase() {
		return predictionRequest.getPhase();
	}

	/**
	 * getWaveType will return one of P, S
	 * 
	 * @return WaveType
	 */
	public GeoAttributes getWaveType() {
		return predictionRequest.getPhase().getWaveType();
	}

	public void setRayType(RayType rayType) {
		this.rayType = rayType;
	}

	@Override
	public RayType getRayType() {
		return rayType;
	}

	@Override
	public ReceiverInterface getReceiver() {
		return predictionRequest.getReceiver();
	}

	@Override
	public int getRequestId() {
		return predictionRequest.getRequestId();
	}

	@Override
	public SourceInterface getSource() {
		return predictionRequest.getSource();
	}

	@Override
	public PredictionRequestInterface getPredictionRequest() {
		return predictionRequest;
	}

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
	@Override
	public EnumMap<GeoAttributes, Double> getSupportedAttributes() {
		return values;
	}

	/**
	 * A String representation of all the GeoAttributes that were requested when
	 * this Prediction was computed.
	 */
	public String getSupportedAttributesString() {
		StringBuffer s = new StringBuffer();
		for (GeoAttributes a : values.keySet())
			s.append(s.length() == 0 ? "" : ", ").append(a.toString());
		return s.toString();
	}

	@Override
	public boolean isValid() {
		return getRayType() != RayType.ERROR && getRayType() != RayType.INVALID;
	}

	/**
	 * Set the error message string for this ray.
	 * 
	 * @param errorMessage String
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage.length() < maxErrorMessageLength)
			this.errorMessage = errorMessage;
		else
			this.errorMessage = String.format("%s%n%n..<original message size = %d>..%n%n%s",
					errorMessage.substring(0, maxErrorMessageLength / 2), errorMessage.length(),
					errorMessage.substring(errorMessage.length() - maxErrorMessageLength / 2));
	}

	/**
	 * Set the error message for this ray.
	 * 
	 * @param errorMessage String
	 */
	public void setErrorMessage(Exception ex) {
		this.errorMessage = Globals.getExceptionAsString(ex);
	}

	/**
	 * Store a GeoAttribute-value pair.
	 * 
	 * @param attribute
	 * @param value
	 */
	@Override
	public void setAttribute(GeoAttributes attribute, double value) {
		values.put(attribute, new Double(value));
	}

	/**
	 * Retrieve a new double[] containing the values of the requested attributes.
	 */
	@Override
	public double[] getAttributes(GeoAttributes[] attributes) {
		double[] val = new double[attributes.length];
		for (int i = 0; i < attributes.length; ++i)
			val[i] = getAttribute(attributes[i]);
		return val;
	}

	@Override
	public EnumMap<GeoAttributes, Double> getAttributes(EnumSet<GeoAttributes> attributes) {
		EnumMap<GeoAttributes, Double> val = new EnumMap<GeoAttributes, Double>(GeoAttributes.class);
		getAttributes(attributes, val);
		return val;
	}

	@Override
	public void getAttributes(EnumSet<GeoAttributes> attributes, EnumMap<GeoAttributes, Double> values) {
		for (GeoAttributes attribute : attributes)
			values.put(attribute, getAttribute(attribute));
	}

	/**
	 * Retrieve the value of the specified GeoAttribute. Returns BaseConst.NA_VALUE
	 * if an unsupported GeoAttribute is requested.
	 * 
	 * @param attribute GeoAttributes
	 * @return double
	 */
	public double getAttribute(String attribute) {
		return getAttribute(GeoAttributes.valueOf(attribute.toUpperCase()));
	}

	/**
	 * Retrieve the value of the specified GeoAttribute. Returns BaseConst.NA_VALUE
	 * if an unsupported GeoAttribute is requested.
	 * 
	 * @param attribute GeoAttributes
	 * @return double
	 */
	public double getAttribute(GeoAttributes attribute) {
		Double value = values.get(attribute);
		return value == null ? Globals.NA_VALUE : value.doubleValue();
	}

	public EnumMap<GeoAttributes, Double> getAttributes() {
		return values;
	}

	@Override
	public String toString() {
		EnumSet<RayType> validTypes = EnumSet.of(RayType.REFLECTION, RayType.REFRACTION, RayType.TOP_SIDE_DIFFRACTION,
				RayType.BOTTOM_SIDE_DIFFRACTION);

		StringBuffer buf = new StringBuffer();
		buf.append(String.format("Receiver: %s%n", getReceiver().toString()));
		buf.append(String.format("Source: %s%n", getSource().toString()));
		buf.append(String.format("Phase: %s%n", getPhase().toString()));
		buf.append(String.format("RayType: %s%n", getRayType()));
		if (validTypes.contains(getRayType()))
			for (GeoAttributes attribute : values.keySet())
				buf.append(String.format("%-20s : %1.6f%n", attribute.toString(), values.get(attribute)));
		else
			buf.append(errorMessage).append(NL);

		return buf.toString();
	}

	public Arrival getArrivalRow() {
		Arrival arrivalRow = new Arrival();

		arrivalRow.setArid(getObservationId());
		arrivalRow.setSta(getReceiver().getSta());
		arrivalRow.setTime(getSource().getOriginTime() + getAttribute(GeoAttributes.TRAVEL_TIME));
		arrivalRow.setJdate(GMTFormat.getJDate(arrivalRow.getTime()));
		arrivalRow.setDeltim(1.);
		arrivalRow.setAzimuth(getAttribute(GeoAttributes.AZIMUTH_DEGREES));
		arrivalRow.setDelaz(15.);
		arrivalRow.setSlow(getAttribute(GeoAttributes.SLOWNESS_DEGREES));
		arrivalRow.setDelaz(1.5);
		arrivalRow.setAuth(System.getenv("user.name"));
		arrivalRow.setIphase(getPhase().toString());

		return arrivalRow;
	}

	public Assoc getAssocRow() {
		Assoc assocRow = new Assoc();

		assocRow.setArid(getObservationId());
		assocRow.setSta(getReceiver().getSta());
		assocRow.setOrid(getSource().getSourceId());
		assocRow.setPhase(getPhase().toString());
		assocRow.setDelta(getReceiver().getPosition().distanceDegrees(getSource().getPosition()));
		assocRow.setSeaz(getReceiver().getPosition().azimuthDegrees(getSource().getPosition()));
		assocRow.setEsaz(getSource().getPosition().azimuthDegrees(getReceiver().getPosition()));

		assocRow.setTimeres(0.);
		assocRow.setAzres(0.);
		assocRow.setSlores(0.);

		assocRow.setTimedef("d");
		assocRow.setAzdef("d");
		assocRow.setSlodef("d");

		assocRow.setVmodel(modelName.length() > 15 ? modelName.substring(0, 15) : modelName);

		return assocRow;
	}

	public Origin getOriginRow() {
		return ((Source) getSource()).getOriginRow();
	}

	public Site getSiteRow() {
		return ((Receiver) getReceiver()).getSiteRow();
	}

	@Override
	public double getDistance() {
		if (predictionRequest.getSource() == null || predictionRequest.getReceiver() == null)
			return Globals.NA_VALUE;

		return predictionRequest.getSource().getPosition().distance(predictionRequest.getReceiver().getPosition());
	}

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
	public int getLookupIndexUncertainty(GeoAttributes attribute) {
		switch (attribute) {
		case TT_MODEL_UNCERTAINTY:
			return predictionRequest.getLookupIndexUncertaintyTT();
		case SLOWNESS_MODEL_UNCERTAINTY:
			return predictionRequest.getLookupIndexUncertaintySH();
		case AZIMUTH_MODEL_UNCERTAINTY:
			return predictionRequest.getLookupIndexUncertaintyAZ();
		default:
			return -1;
		}
	}

	/**
	 * @return the lookupIndexUncertaintyTT
	 */
	public int getLookupIndexUncertaintyTT() {
		return predictionRequest.getLookupIndexUncertaintyTT();
	}

	/**
	 * @return the lookupIndexUncertaintyAZ
	 */
	public int getLookupIndexUncertaintyAZ() {
		return predictionRequest.getLookupIndexUncertaintyAZ();
	}

	/**
	 * @return the lookupIndexUncertaintySH
	 */
	public int getLookupIndexUncertaintySH() {
		return predictionRequest.getLookupIndexUncertaintySH();
	}

	public int getPredictorIndex() {
		return predictorIndex;
	}

	/**
	 * Returns the predictions ray path as a list of GeoVector objects.
	 * 
	 * @return The predictions ray path as a list of GeoVector objects.
	 */
	public ArrayList<GeoVector> getRayPath() {
		throw new UnsupportedOperationException("Method is not implemented ...");
	}

	/**
	 * Returns the predictions active node indexes.
	 * 
	 * @return The predictions active node indexes.
	 */
	public int[] getRayWeightIndexes() {
		throw new UnsupportedOperationException("Method is not implemented ...");
	}

	/**
	 * Returns the predictions active node weights.
	 * 
	 * @return The predictions active node weights.
	 */
	public double[] getRayWeights() {
		throw new UnsupportedOperationException("Method is not implemented ...");
	}

	/**
	 * Returns the predictions list of ray path positions as a list of unit vectors
	 * and a list of radii. The input lists are cleared before filling with the ray
	 * path data.
	 * 
	 * @param unitVectors The predictions list of ray path unit vectors on return.
	 * @param radii       The predictions list of ray path radii on return.
	 */
	public void getRayPath(ArrayList<double[]> unitVectors, ArrayListDouble radii) {
		throw new UnsupportedOperationException("Method is not implemented ...");
	}

	/**
	 * Nullifies the internal list of ray path unit vectors and radii.
	 */
	public void nullifyRayPath() {
		throw new UnsupportedOperationException("Method is not implemented ...");
	}

	@Override
	public void writePrediction(FileOutputBuffer fob) throws IOException {
		// not used by Prediction
	}

}
