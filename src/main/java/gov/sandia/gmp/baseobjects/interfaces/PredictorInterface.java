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
package gov.sandia.gmp.baseobjects.interfaces;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
//X import gov.sandia.gmp.baseobjects.geovector.GeoVector.EarthShape;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.exceptions.GMPException;

public interface PredictorInterface extends Serializable {

	// Predictors should have a constructor that takes a reference to a
	// PropertyPlusGMP
	// object and configures itself such that it is ready to produce valid
	// predictions of GeoAttributes, ie., seismic observables.

	/**
	 * @return name of the predictor
	 */
	public String getPredictorName();

	/**
	 * @return version of the predictor
	 */
	public String getPredictorVersion();

	/**
	 * @return a reference to the Earth Model that this Predictor is using to make
	 *         predictions.
	 */
	public Object getEarthModel();

	/**
	 * @return name of the model upon which predictions will be based.
	 */
	public String getModelName();

	/**
	 * @return The name of the model upon which predictions will be based. Limited
	 *         to length of maxLength characters.
	 */
	public String getModelName(int maxLength);

	/**
	 * @return a String containing a brief, but potentially multiline, description
	 *         of the model that will be used by the Predictor.
	 * @throws GMPException
	 */
	public String getModelDescription() throws GMPException;

	/**
	 * Return a Map from ReceiverInterface -> SeismicPhase -> GeoAttributes that
	 * includes specific observations that this Predictor wishes to support. Even if
	 * overall responsibility for some phase is assigned by PredictorFactory to
	 * another Predictor, this Predicto will try to claim the specific observations
	 * included in this map for itself. < p>This Predictor may also support other
	 * observations based on SeismicPhase as determined by PredictorFactory
	 * 
	 * <p>
	 * GeoAttributes will include a subset of TRAVEL_TIME, AZIMUTH and SLOWNESS.
	 * 
	 * @return
	 * @throws GMPException
	 */
	public AttributeIndexerSmart claimObservations() throws GMPException;

	public boolean isSupported(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute,
			double epochTime);

	/**
	 * Retrieve a PredictionInferface object for the specified
	 * source/receiver/phase.
	 * 
	 * @return PredictionInterface
	 */
	public PredictionInterface getPrediction(PredictionRequestInterface request) throws GMPException;

	/**
	 * Retrieve PredictionInterface objects for numerous source/receiver/phase
	 * triples.
	 * 
	 * @return PredictionInterface[]
	 */
	public PredictionInterface[] getPredictions(PredictionRequestInterface[] requests) throws GMPException;

	/**
	 * Retrieve PredictionInterface objects for numerous PredictionRequestInterface
	 * objects
	 */
	public ArrayList<PredictionInterface> getPredictions(Collection<? extends PredictionRequestInterface> requests)
			throws GMPException;

	/**
	 * Set the algorithmId to the specified value. Default value is -1.
	 * 
	 * @param algorithmId
	 */
	public void setAlgorithmId(long algorithmId);

	/**
	 * @returne the algorithmId. Default value is -1.
	 */
	public long getAlgorithmId();

	/**
	 * Set the modelId to the specified value. Default value is -1.
	 * 
	 * @param modelId
	 */
	public void setModelId(long modelId);

	/**
	 * @return the modelId. Default value is -1.
	 */
	public long getModelId();

	/**
	 * 
	 * @return a reference to the File from which the Earth model was loaded.
	 */
	public File getModelFile();

	public UncertaintyInterface getUncertaintyInterface();

	public void setUncertaintyInterface(UncertaintyInterface uncertaintyInterface);

	/**
	 * Some Predictors are able to look up attribute values such as
	 * TT_PATH_CORRECTION or TT_MODEL_UNCERTAINTY in lookup tables. It is
	 * advantageous to store the indexes of the attributes in the PredictionRequest
	 * object so that repeated calls for the same attribute values can be done as
	 * efficiently as possible.
	 * <p>
	 * This Predictor method is called by a PredictionRequest object so that the
	 * Predictor can set the lookup table indexes in the PredictionRequest.
	 * 
	 * @param predictionRequest reference to the PredictionRequest object that is
	 *                          requesting to have its lookup table indexes set.
	 */
	void setLookupTableIndexes(PredictionRequestInterface predictionRequest);

	/**
	 * @param position
	 * @return the radius of the surface of the solid Earth at the specified
	 *         position.
	 * @throws GMPException
	 */
	public double getSurfaceRadius(GeoVector position) throws GMPException;

	/**
	 * @param position
	 * @return the depth below sea level of the surface of the solid Earth at the
	 *         specified position.
	 * @throws GMPException
	 */
	public double getSurfaceDepth(GeoVector position) throws GMPException;

	PredictorType getPredictorType();

	EnumSet<GeoAttributes> getSupportedAttributes();

	EnumSet<SeismicPhase> getSupportedPhases();

}
