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

import java.io.Serializable;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.UndersideReflectedPhaseBouncePoint;

/**
 * A PredictionRequest object is basically a container class to group together
 * all the information needed to compute a Prediction including:
 * <ul>
 * <li>a Source,
 * <li>a Receiver,
 * <li>a SeismicPhase
 * <li>a set of requested attributes
 * </ul>
 * It also has reference to a PredictionInterface object that will contain the
 * Prediction if/when it is computed.
 * <p>
 * Note that there are no data stored here. Therefore residuals cannot be
 * computed. Class Observation extends PredictionRequest and stores typical
 * seismic data and residuals.
 * 
 * @author sballar
 * 
 */
public interface PredictionRequestInterface extends Serializable {
	/**
	 * The ID of the Observation that is to have a predicted value calculated. This
	 * is optional. Could be set to -1 and ignored.
	 * 
	 * @return long
	 */
	public long getObservationId();

	public void setObservationId(long observationId);

	/**
	 * For use by PredictorInterface objects. They may modify these values. Calling
	 * applications should provide a requestId variable but should not count on this
	 * value remaining unmodified.
	 * 
	 * @return int
	 */
	public void setRequestId(int requestId);

	/**
	 * For use by PredictorInterface objects. They may modify these values. Calling
	 * applications should provide a requestId variable but should not count on this
	 * value remaining unmodified.
	 */
	public int getRequestId();

	/**
	 * Source position.
	 * 
	 * @return GeoVector
	 */
	public SourceInterface getSource();

	/**
	 * Receiver position.
	 * 
	 * @return GeoVector
	 */
	public ReceiverInterface getReceiver();

	/**
	 * Seismic phase.
	 * 
	 * @return SeismicPhase
	 */
	public SeismicPhase getPhase();

	/**
	 * Change the seismic phase of the prediction request.
	 * 
	 * @param phase
	 */
	public void setPhase(SeismicPhase phase);

	/**
	 * Retrieve the index of the specified attribute in the lookup table.
	 * <p>
	 * Some Predictors are able to look up attribute values such as
	 * TT_PATH_CORRECTION or TT_MODEL_UNCERTAINTY in lookup tables. It is
	 * advantageous to store the indexes of the attributes in the PredictionRequest
	 * object so that repeated calls for the same attribute values can be done as
	 * efficiently as possible.
	 * 
	 * @param attribute
	 * @return the index of the specified attribute in the lookup table.
	 */
	boolean haveLookupIndexesBeenSet();

	int getLookupIndexTT();

	int getLookupIndexAZ();

	int getLookupIndexSH();

	int getLookupIndexPathCorrTT();

	int getLookupIndexPathCorrAZ();

	int getLookupIndexPathCorrSH();

	int getLookupIndexUncertaintyTT();

	int getLookupIndexUncertaintyAZ();

	int getLookupIndexUncertaintySH();

	void setLookupIndexTT(int index);

	void setLookupIndexAZ(int index);

	void setLookupIndexSH(int index);

	void setLookupIndexPathCorrTT(int index);

	void setLookupIndexPathCorrAZ(int index);

	void setLookupIndexPathCorrSH(int index);

	void setLookupIndexUncertaintyTT(int index);

	void setLookupIndexUncertaintyAZ(int index);

	void setLookupIndexUncertaintySH(int index);

	/**
	 * If a PredictionRequest.isDefining() returns false then the Predictor should
	 * refrain from computing a prediction and should instead return a
	 * PredictionInterface object with isValid() false, all data set to
	 * BaseConst.NA_VALUE, and errorMessage = ""PredictionRequest was non-defining".
	 * 
	 * @return
	 */
	public boolean isDefining();

	/**
	 * The set of GeoAttributes that the calling application intends to request from
	 * the PredictionInterface objects that will ultimately be returned by calls to
	 * Predictor.getPrediction().
	 */
	public EnumSet<GeoAttributes> getRequestedAttributes();

	/**
	 * Extract the bare minimum amount of information from a
	 * PredictionRequestInterface object. For example, baseobjects.Observation
	 * implements PredictionRequestInterface but is inefficient for serialization
	 * across networks. Calling getPredictionRequest() would extract a
	 * PredictionRequest object (a concrete, minimal implementation
	 * PredictionRequestInterface).
	 * 
	 * @return
	 */
	public PredictionRequestInterface getPredictionRequest();

	/**
	 * @return the source-receiver separation in radians
	 */
	public double getDistance();

	/**
	 * @return the source-receiver separation in degrees
	 */
	public double getDistanceDegrees();

	/**
	 * Used by predictors that understand the behavior of reflective phases that are
	 * defined with under side reflective bounce points. If the under side reflected
	 * phase bounce point object is not defined it is first created and then
	 * returned.
	 * 
	 * @return an UndersideReflectedPhaseBouncePoint object.
	 */
	public UndersideReflectedPhaseBouncePoint getUndersideReflectedPhaseBouncePoint();
}
