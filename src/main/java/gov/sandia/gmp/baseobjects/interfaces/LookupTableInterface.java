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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;

/**
 * This interface supports the idea of an n-dimensional lookup table that
 * contains quantities of interest. The dimensions of the lookup table are:
 * <ul>
 * <li>Receiver
 * <li>SeismicPhase
 * <li>GeoAttribute
 * <li>Source (3D space and time)
 * </ul>
 * Instances of classes that implement this interface must be thread-safe.
 * <p>
 * This interface only supports the idea of the existence of lookup tables. It
 * does not specify methods to extract information from lookup tables, either
 * actual table values or interpolated information. Other interfaces will
 * specify those types of methods.
 * 
 * @author sballar
 *
 */
public interface LookupTableInterface {

	/**
	 * Get vertion number.
	 * 
	 * @return
	 */
	String getVersion();

	/**
	 * Returns path to directory where models are stored
	 */
	File getRootPath();

	/**
	 * Retrieve the name of the file that contains the model that supports the
	 * specified receiver, phase, attribute. Returns null if the
	 * receiver/phase/attribute is unsupported.
	 * 
	 * @param pathCorrIndex
	 * @return a reference to the model, or null
	 */
	File getModelFile(int modelIndex);

	/**
	 * Retrieve the name of the file that contains the model that supports the
	 * specified receiver, phase, attribute. Returns null if the
	 * receiver/phase/attribute is unsupported.
	 * 
	 * @param sta       name of receiver
	 * @param phase     seismic phase
	 * @param attribute Libcorr3D supports TT_DELTA_AK135 and TT_MODEL_UNCERTAINTY
	 * @return a reference to the model, or null
	 */
	File getModelFile(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * Map from ReceiverInterface -> phase -> attribute.
	 * 
	 * @return
	 */
	AttributeIndexerSmart getSupportMap();

	/**
	 * Retrieve the index of the model that supports the specified receiver, phase,
	 * attribute, or -1.
	 * 
	 * @param receiver
	 * @param phase
	 * @param attribute
	 * @return the index of the model that supports the specified receiver, phase,
	 *         attribute, or -1.
	 */
	int getModelIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * Retrieve the number of unique ReceiverInterface -> phase -> attribute
	 * combinations that are supported by this LibCorr3DModels object.
	 * 
	 * @return
	 */
	int size();

	/**
	 * Returns true if a model exists to support the specified receiver, phase,
	 * attribute.
	 * 
	 * @param receiver  receiver
	 * @param phase     seismic phase; LibCorr3D supports P and Pn
	 * @param attribute Libcorr3D supports TT_DELTA_AK135 and TT_MODEL_UNCERTAINTY
	 * @return
	 */
	boolean isSupported(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * Retrieve total number of stations, not unique receiver names. For a site with
	 * entries for multiple on-off times, each entry contributes to nReceivers.
	 * 
	 * @return total number of ReceiverInterface objects.
	 */
	int getNReceivers();

	/**
	 * Retrieve the number of unique receiver names. For a site with entries for
	 * multiple on-off times, only 1 entry contributes to nReceiverNames.
	 * 
	 * @return
	 */
	int getNReceiverNames();

	/**
	 * Map from receiver name -> array list of ReceiverInterfaces that have that
	 * name but have different onTimes. In the ArrayList, stations are in order of
	 * decreasing onTime.
	 * 
	 * @return map from receiver name -> array list of ReceiverInterfaces
	 */
	Map<String, ArrayList<ReceiverInterface>> getSupportedReceivers();

	/**
	 * Retrieve the Receiver object with specified name and that was active at
	 * specified epochTime. To test for a receiver that is currently active, specify
	 * epochTime = 1e10. Returns null if no receiver meets the criteria.
	 * 
	 * @param sta       receiver name
	 * @param epochTime seconds since 1970
	 * @return Receiver or null
	 */
	ReceiverInterface getReceiver(String sta, double epochTime);

	/**
	 * Retrieve the ReceiverInterface object with specified name and that has the
	 * latest onTime. Returns null if sta is not supported.
	 * 
	 * @param sta receiver name
	 * @return Receiver or null
	 */
	ReceiverInterface getReceiver(String sta);

	/**
	 * Set of phases supported by any one model in this set of models. Just because
	 * a phase is in this list does not guarantee that it is supported by any
	 * specific model. Must check isSupported(Receiver, phase, attribute) to
	 * determine that.
	 * 
	 * @return Set of phases supported by any one model in this set of models.
	 */
	Set<SeismicPhase> getSupportedSeismicPhases();

	/**
	 * Set of attributes supported by any one model in this set of models. Just
	 * because an attributes is in this list does not guarantee that it is supported
	 * by any specific model. Must check isSupported(Receiver, phase, attribute) to
	 * determine that.
	 * 
	 * @return Set of attributes supported by any one model in this set of models.
	 */
	Set<GeoAttributes> getSupportedGeoAttributesAttributes();

	/**
	 * Return true if model is available for receiver, phase, attribute, at the
	 * specified epochTime. To test for a receiver that is currently active, specify
	 * epochTime = 1e10.
	 * 
	 * @param sta
	 * @param phase
	 * @param attribute
	 * @param epochTime seconds since 1970
	 * @return true if model is available for receiver, phase, attribute, at the
	 *         specified epochTime.
	 */
	boolean isSupported(String sta, SeismicPhase phase, GeoAttributes attribute, double epochTime);

	/**
	 * Return true if there is a model available for receiver, phase, attribute,
	 * independent of on/off time.
	 * 
	 * @param sta
	 * @param phase
	 * @param attribute
	 * @return
	 */
	boolean isSupported(String sta, SeismicPhase phase, GeoAttributes attribute);

}
