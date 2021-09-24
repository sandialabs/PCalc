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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Returns Globals.NA_VALUE no matter what is requested.
 * 
 * @author sballar
 *
 */
public class UncertainyNAValue implements UncertaintyInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8419904169472984003L;

	/**
	 * 
	 */
	public UncertainyNAValue() {
	}

	/**
	 * 
	 */
	public UncertainyNAValue(PropertiesPlusGMP properties, String propertyPrefix) {
	}

	@Override
	public String getUncertaintyType() {
		return this.getClass().getSimpleName();
	}

	static public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}

	@Override
	public double getUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws FileNotFoundException, GMPException {
		return Globals.NA_VALUE;
	}

	@Override
	public double getUncertainty(SourceInterface source, ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException {
		return Globals.NA_VALUE;
	}

	@Override
	public boolean setUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws FileNotFoundException, GMPException {
		prediction.setAttribute(attribute, getUncertainty(prediction, attribute));
		return false;
	}

	public EnumSet<SeismicPhase> getSupportedPhases() {
		return EnumSet.allOf(SeismicPhase.class);
	}

	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return "null";
	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return true;
	}

	@Override
	public String getUncertaintyRootDirectory() throws IOException {
		return "null";
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException {
		return this;
	}

	@Override
	public AttributeIndexerSmart claimObservations() throws GMPException {
		return new AttributeIndexerSmart();
	}

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
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 */
	@Override
	public int getLookupTableIndex(PredictionRequestInterface predictionRequest, GeoAttributes ttPathCorrection) {
		return -1;
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLookupTableIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return -1;
	}

	@Override
	public double getUncertainty(int lookupIndex, SourceInterface source) throws GMPException {
		throw new UnsupportedOperationException();
	}

}
