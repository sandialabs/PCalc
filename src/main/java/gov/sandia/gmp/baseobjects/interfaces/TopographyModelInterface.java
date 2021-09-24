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

import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Interface for models that can return the topography/bathymerty at a specified
 * location on the Earth.
 * 
 * @author sballar
 *
 */
public interface TopographyModelInterface {

	/**
	 * Load the topography model from a file
	 * 
	 * @param topoModelFile
	 * @param the           index of the attribute in the topography model that
	 *                      contains the topography information.
	 * @throws Exception
	 */
	void loadTopoModel(File topoModelFile) throws Exception;

	/**
	 * Load and configure a topography model using properties in the specified
	 * PropertiesPlus object.
	 * 
	 * @param properties a PropertiesPlus objet.
	 * @throws Exception
	 */
	void loadTopoModel(PropertiesPlus properties) throws Exception;

	/**
	 * Return true if a model has been loaded and is ready for use.
	 * 
	 * @return true if a model has been loaded and is ready for use.
	 */
	boolean isValid();

	/**
	 * Retrieve the value of the topography/bathymetry, in km, at the specified
	 * location.
	 * 
	 * @param unitVector the value of the topography/bathymetry, in km, at the
	 *                   specified location.
	 * @return the value of the topography/bathymetry, in km, at the specified
	 *         location.
	 */
	double getTopoKm(double[] unitVector) throws Exception;

	/**
	 * Retrieve the name of the filei from which the topography model was loaded.
	 * 
	 * @return the name of the filei from which the topography model was loaded.
	 */
	File getModelFile();

}
