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

import java.io.IOException;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.exceptions.GMPException;

public interface UncertaintyInterface {

	/**
	 * Return a map from ReceiverInterface -> SeismicPhase -> GeoAttributes that
	 * includes a key set for every supported combination.
	 * 
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	AttributeIndexerSmart claimObservations() throws GMPException, IOException;

	/**
	 * Name of directory from which uncertainty information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	public String getUncertaintyRootDirectory() throws IOException;

	/**
	 * Returns the type of the UncertaintyInterface object: UncertaintyNAValue,
	 * UncertaintyDistanceDependent, etc.
	 */
	public String getUncertaintyType();

	/**
	 * Get a copy of this UncertaintyInterface object that reuses all thread-safe
	 * components.
	 * 
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	UncertaintyInterface copyUncertainty() throws GMPException, IOException;

	/**
	 * Returns a version such as '1.2.3'
	 * 
	 * @return
	 */
	public String getUncertaintyVersion();

	/**
	 * Name of specific file from which uncertainty information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	public String getUncertaintyModelFile(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute)
			throws IOException;

	/**
	 * Return true if this Uncertainty object supports the specified
	 * station-phase-attribute at the indicated epochTime. Note that many
	 * Uncertainty objects don't care about some of these parameters. For example,
	 * SLBM can return uncertainty for a phase and attribute regardless of station
	 * or epoch time. So it will simply ignore what ever is supplied in those
	 * parameters.
	 * 
	 * @param receiver
	 * @param phase
	 * @param attribute one of TT_MODEL_UNCERTAINTY, AZIMUTH_MODEL_UNCERTAINTY or
	 *                  SLOWNESS_MODEL_UNCERTAINTY.
	 * @param epochTime
	 * @return
	 */
	public boolean isUncertaintySupported(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * If this Uncertainty object is supported by lookup tables, then retrieve the
	 * index of the lookup table that supports the given source, receiver, phase and
	 * attribute. Returns -1 if the Uncertainty object is not supported by lookup
	 * tables, or if the supplied source/receiver/phase/attribute is not supported
	 * by any of the lookup tables.
	 * <ul>
	 * Supported attributes:
	 * <li>TT_MODEL_UNCERTAINTY
	 * <li>AZIMUTH_MODEL_UNCERTAINTY
	 * <li>SLOWNESS_MODEL_UNCERTAINTY
	 * </ul>
	 * 
	 * @param source
	 * @param receiver
	 * @param phase
	 * @param attribute TT_MODEL_UNCERTAINTY, AZIMUTH_MODEL_UNCERTAINTY,
	 *                  SLOWNESS_MODEL_UNCERTAINTY.
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	int getLookupTableIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * If this Uncertainty object is supported by lookup tables, then retrieve the
	 * index of the lookup table that supports the given PredictionRequest and
	 * attribute. Returns -1 if the Uncertainty object is not supported by lookup
	 * tables, or if the supplied PredictionRequest is not supported by any of the
	 * lookup tables.
	 * <ul>
	 * Supported attributes:
	 * <li>TT_MODEL_UNCERTAINTY
	 * <li>AZIMUTH_MODEL_UNCERTAINTY
	 * <li>SLOWNESS_MODEL_UNCERTAINTY
	 * </ul>
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 */
	int getLookupTableIndex(PredictionRequestInterface predictionRequest, GeoAttributes ttPathCorrection);

	/**
	 * Retrieve the value of the uncertainty for the specified source, receiver,
	 * phase, attribute.
	 * 
	 * @param source
	 * @param receiver
	 * @param phase
	 * @param attribute
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	public double getUncertainty(SourceInterface source, ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException;

	/**
	 * Retrieve the uncertainty value for the given GeoAttribute which should be one
	 * of TT_MODEL_UNCERTAINTY, AZIMUTH_MODEL_UNCERTAINTY or
	 * SLOWNESS_MODEL_UNCERTAINTY
	 * 
	 * @param prediction
	 * @param attribute
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	public double getUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws GMPException, IOException;

	/**
	 * If the specified attribute is one of the uncertainty attributes, set the
	 * value of the attribute in the prediction.
	 * 
	 * @param prediction
	 * @param attribute  one of TT_MODEL_UNCERTAINTY, AZIMUTH_MODEL_UNCERTAINTY or
	 *                   SLOWNESS_MODEL_UNCERTAINTY
	 * @return true if the uncertainty was set successfully. If false, the
	 *         uncertainty will have been set to Globals.NA_VALUE.
	 * @throws GMPException
	 * @throws IOException
	 */
	public boolean setUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws GMPException, IOException;

	/**
	 * Name of specific file from which uncertainty information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	public String getUncertaintyModelFile(int lookupTableIndex) throws IOException;

	double getUncertainty(int lookupIndex, SourceInterface source) throws GMPException;

}
