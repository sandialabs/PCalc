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
package gov.sandia.gmp.libcorr3dgmp;

import java.io.File;
import java.io.IOException;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.gmp.util.exceptions.GMPException;

public class Translation
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		try
		{
			new Translation().ttlookupTables();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	protected void ttlookupTables() throws GeoTessException, IOException, GMPException
	{
		File inputDir = new File("");
				
				//"C:\\Users\\sballar\\work\\LibCorr3DModels\\IMS_2012_07_19");

		File outputDir = inputDir;
		outputDir.mkdirs();

		System.out.println("Input  directory = " + inputDir.getCanonicalPath());
		System.out.println("Output directory = " + outputDir.getCanonicalPath());

		int nModels = 0, nGrids = 0;
		
		for (File original : inputDir.listFiles())
			if (original.isFile() && GeoTessModel.isGeoTessModel(original))
			{
				System.out.println(original.getName());

				LibCorr3DModel model = new LibCorr3DModel(original);
				
				model.getMetaData().setAttributes("TT_DELTA_AK135; TT_MODEL_UNCERTAINTY", 
						"seconds; seconds");
				
				model.writeModel(new File(outputDir, original.getName()), model
						.getMetaData().getGridInputFileName());

				++nModels;
			}

		System.out.printf("%nTranslated %d models and %d grids%n", nModels,
				nGrids);

	}

}
