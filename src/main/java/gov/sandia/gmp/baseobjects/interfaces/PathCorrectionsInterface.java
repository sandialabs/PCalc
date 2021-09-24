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

public interface PathCorrectionsInterface {
	/**
	 * Name of directory from which path correction information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	String getPathCorrRootDirectory() throws IOException;

	/**
	 * Get a copy of this PathCorrectionsInterface object that reuses all
	 * thread-safe components.
	 * 
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	PathCorrectionsInterface copyPathCorr() throws GMPException, IOException;

	/**
	 * Return a map from ReceiverInterface -> SeismicPhase -> GeoAttributes that
	 * includes a key set for every supported combination.
	 * 
	 * @return
	 * @throws GMPException
	 */
	AttributeIndexerSmart claimObservations() throws GMPException;

	/**
	 * <ul>
	 * Supported attributes:
	 * <li>TT_PATH_CORRECTION
	 * <li>AZIMUTH_PATH_CORRECTION
	 * <li>SLOWNESS_PATH_CORRECTION
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 */
	int getLookupTableIndex(PredictionRequestInterface predictionRequest, GeoAttributes attribute);

	/**
	 * 
	 * @param source
	 * @param receiver
	 * @param phase
	 * @param attribute TT_PATH_CORRECTION, AZIMUTH_PATH_CORRECTION,
	 *                  SLOWNESS_PATH_CORRECTION.
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	int getLookupTableIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * 
	 * @param lookupTableIndex
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrection(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivHorizontal(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @param source
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivLat(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @param source
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivLon(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivRadial(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * Name of specific file from which path correction information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	String getPathCorrModelFile(int lookupTableIndex) throws IOException;

	// InterpolatorType getPathCorrInterpolatorType();

	// void setPathCorrInterpolatorType(InterpolatorType interpType) throws
	// GMPException;

}
