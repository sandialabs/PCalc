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
package gov.sandia.gmp.pcalc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.rayuncertainty.RayUncertainty;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class RayUncertaintyPCalc {
	

	public void run(PCalc pcalc, Bucket dataBucket) throws Exception
	{
		if (pcalc.log.getVerbosity() >= 1)
			pcalc.log.writeln("Computing RayUncertainty.");
		
		long timer = System.currentTimeMillis();
		
		// extract ray uncertainty properties from the pcalc properties.
		PropertiesPlus properties = buildPropertyFile(pcalc, dataBucket);
		
		runRayUncertainty(properties);
		
		extractUncertaintyValues(properties, pcalc, dataBucket);
		
		try
		{
			// write a file named DONE in the ioDirectory indicating that
			// RayUncertainty has completed and that the directory can be deleted.
			File f = properties.getFile("ioDirectory");
			if (f!= null)
			{
				f = new File(f, "DONE");
				FileWriter fw = new FileWriter(f);
				fw.write(new Date().toString());
				fw.close();
			}
		}
		catch (Exception ex) {
			pcalc.log.writeln(ex);	
		}
		
		// Rename the properties file from rayuncertainty-sta-yyyy-MM-dd-HH-mm-ss-SSS.properties
		// to deleteme-sta-yyyy-MM-dd-HH-mm-ss-SSS.properties
		File propertiesFile = properties.getFile("propertiesFileName");
		try {
			if (propertiesFile != null)
			{
				File newFileName = new File(propertiesFile.getParentFile(), 
						propertiesFile.getName().replace("rayuncertainty", "deleteme"));
				if (propertiesFile.renameTo(newFileName))
					propertiesFile = newFileName;
			}
		}
		catch (Exception ex) {
			pcalc.log.writeln(ex);	
		}
		
		// delete the ray uncertainty properties file (created by buildPropertyFile method).
		try {
			propertiesFile.delete();
		} catch (Exception ex) {
			pcalc.log.writeln(ex);
		}
		

		
		// Rename the iodirectory from rayuncertainty-yyyy-MM-dd-HH-mm-ss-SSS
		// to deleteme-yyyy-MM-dd-HH-mm-ss-SSS
		File ioDirectory = properties.getFile("ioDirectory");
		try {
			if (ioDirectory != null)
			{
				File newFileName = new File(ioDirectory.getParentFile(), 
						ioDirectory.getName().replace("rayuncertainty", "deleteme"));
				if (ioDirectory.renameTo(newFileName))
					ioDirectory = newFileName;
			}
		}
		catch (Exception ex) {
			pcalc.log.writeln(ex);	
		}
		
		try {
			// delete the temporary ray uncertainty work directory. Try 10 times.
			for (int i=0; i<10; ++i)
			{
				Thread.sleep(1000);
				deleteFile(ioDirectory);
				Thread.sleep(1000);
				if (!ioDirectory.exists())
					break;
			}
		} catch (Exception e) {
			pcalc.log.writeln(e);
		}
		
		if (pcalc.log.getVerbosity() >= 1)
			pcalc.log.writef("Computing RayUncertainties completed in %s%n%n", Globals.elapsedTime(timer));
	}
	
	private void extractUncertaintyValues(PropertiesPlus properties, PCalc pcalc, Bucket dataBucket) throws Exception
	{
		File ioDirectory = properties.getFile("ioDirectory");		
		
		Scanner input = new Scanner(new File(ioDirectory, "out.txt"));
		while (input.hasNextLine())
			pcalc.log.writeln(input.nextLine());
		input.close();
		
		// copy uncertainty results from the uncertainty.txt file to the dataBucket.modelVallues array
		int idx = pcalc.outputAttributes.indexOf(GeoAttributes.TT_MODEL_UNCERTAINTY)+1;
		input = new Scanner(new File(ioDirectory, "variance.txt"));
		String line = input.nextLine();
		while (!line.trim().startsWith("(sec^2)"))
			line = input.nextLine();
		int count = 0;
		while (input.hasNext())
		{
			line = input.nextLine();
			++count;
			Scanner in = new Scanner(line);
			in.next(); // ignore sta 
			dataBucket.modelValues[in.nextInt()][idx] = Math.sqrt(in.nextDouble());
			in.close();
		}
		input.close();
		pcalc.log.writef("%d uncertainty values computed. %d valid, %d invalid (set to NaN)%n",
				dataBucket.modelValues.length, count, 
				(dataBucket.modelValues.length-count));
	}
	
	private void runRayUncertainty(PropertiesPlus properties) throws Exception
	{
		RayUncertainty ru = new RayUncertainty();
		ru.initializeSolution(properties.getFile("propertiesFileName").getAbsolutePath());
		ru.solve();
		if (ru.getGUI() != null) ru.getGUI().dispose();
		ru = null;
		// call the garbage collector
		System.gc();
	}

	private PropertiesPlus buildPropertyFile(PCalc pcalc, Bucket dataBucket) throws Exception
	{

		File workDir = pcalc.properties.getFile("benderUncertaintyWorkDir");
		if (workDir == null)
			throw new Exception("Must specify property benderUncertaintyWorkDir in the properties file.");
		workDir.mkdirs();
		
		File benderModel = pcalc.properties.getFile("benderModel");
		if (benderModel == null)
			throw new Exception("Property benderModel not specified in the properties file.");
		
		if (!benderModel.exists())
			throw new Exception(String.format("benderModel %s does not exist.", benderModel.getAbsoluteFile()));
		
		if (!benderModel.isDirectory())
			throw new Exception(String.format("benderModel %s is not a directory.", benderModel.getAbsoluteFile()));
		
		File tomoModel = new File(benderModel, "tomo_model.geotess");

		if (!tomoModel.exists())
			throw new Exception(String.format("Properties file specifies benderModel=%s%nbut that directory does not contain tomo_model.geotess.",
					benderModel.getAbsolutePath()));
		
		File ioDirectory = getWorkDir(workDir, pcalc.bucket.site.getSta());
		
		if (ioDirectory.exists())
			throw new Exception("Unable to create a new, empty, bender uncertainty work directory "
					+ioDirectory.getAbsolutePath());
		
		File stdevFile = new File(benderModel, "layer_standard_deviations.properties");
		if (!stdevFile.exists())
			throw new Exception("File "+stdevFile.getAbsolutePath()+" does not exist.");
		PropertiesPlus stddev = new PropertiesPlus(stdevFile);
		
		pcalc.properties.setProperty("rayUncertaintyIODirectory", ioDirectory.getCanonicalPath());
		
		PropertiesPlus p = new PropertiesPlus();
		p.setProperty("ioDirectory", ioDirectory.getCanonicalPath());

		p.setProperty("startPhase", pcalc.properties.getProperty("startPhase", "PREDICTION"));
		p.setProperty("endPhase", pcalc.properties.getProperty("endPhase", "RAY_UNCERTAINTY"));
		p.setProperty("outputMode", "both");
		p.setProperty("phaseDefinition", pcalc.properties.getProperty("phase"));
		p.setProperty("storagePrecision", pcalc.properties.getProperty("geotessDataType", "FLOAT").toUpperCase());

		if (pcalc.properties.containsKey("slownessLayerStandardDeviation_P"))
			p.setProperty("slownessLayerStandardDeviation_P", pcalc.properties.getProperty("slownessLayerStandardDeviation_P"));
		else
		{
			p.setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", ""));
			pcalc.properties.setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", "unspecified"));
		}			
		
		if (pcalc.properties.containsKey("slownessLayerStandardDeviation_S"))
			p.setProperty("slownessLayerStandardDeviation_S", pcalc.properties.getProperty("slownessLayerStandardDeviation_S"));
		else		
		{
			p.setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", ""));
			pcalc.properties.setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", "unspecified"));
		}			
		
		p.setProperty("geoModelTomographyPath", tomoModel.getParent());
		p.setProperty("geoModelTomographyFileName", tomoModel.getName());

		p.setProperty("covarianceMatrixActiveNodeMapPath", benderModel.getAbsolutePath());
		p.setProperty("covarianceFileServerPaths", benderModel.getAbsolutePath());

		p.setProperty("covarianceMatrixActiveNodeMapFileName", "activenodeIndexMap");
		p.setProperty("covarianceFileServerBlockMap", pcalc.properties.getProperty("covarianceFileServerBlockMap", ""));
		p.setProperty("covarianceFileServerStorageUseFraction", pcalc.properties.getProperty("covarianceFileServerStorageUseFraction", ""));
		p.setProperty("covarianceFileSecondaryPaths", pcalc.properties.getProperty("covarianceFileSecondaryPaths", "cov ginv"));

		
		p.setProperty("displayGUI", pcalc.properties.getBoolean("displayGUI", false));
		
		if (pcalc.properties.containsKey("parallelMode")) 
			p.setProperty("parallelMode", pcalc.properties.getProperty("parallelMode"));
		if (pcalc.properties.containsKey("maxProcessors")) 
			p.setProperty("maxProcessors", pcalc.properties.getProperty("maxProcessors"));
		if (pcalc.properties.containsKey("fabricApplicationName")) 
			p.setProperty("fabricApplicationName", pcalc.properties.getProperty("fabricApplicationName"));
		if (pcalc.properties.containsKey("fabricMaxThreadsPerNode")) 
			p.setProperty("fabricMaxThreadsPerNode", pcalc.properties.getProperty("fabricMaxThreadsPerNode"));
		if (pcalc.properties.containsKey("fabricBaselineNodeMemory")) 
			p.setProperty("fabricBaselineNodeMemory", pcalc.properties.getProperty("fabricBaselineNodeMemory"));
		if (pcalc.properties.containsKey("nodeMaxMemory")) 
			p.setProperty("nodeMaxMemory", pcalc.properties.getProperty("nodeMaxMemory"));
		if (pcalc.properties.containsKey("driverMaxMemory")) 
			p.setProperty("driverMaxMemory", pcalc.properties.getProperty("driverMaxMemory"));
		if (pcalc.properties.containsKey("taskTimeout")) 
			p.setProperty("taskTimeout", pcalc.properties.getProperty("taskTimeout"));

		p.setProperty("receiverDefinition", "");
		Site site = pcalc.bucket.site;
		if (site == null)
			throw new Exception("Site is undefined.");
		p.setProperty("receiverDefinitionList", String.format("%s %d %d %1.6f %1.6f %6.3f %s %s %1.3f %1.3f",
				site.getSta(), site.getOndate(), site.getOffdate(), site.getLat(), site.getLon(), site.getElev(),
				site.getStatype(), site.getRefsta(), site.getDnorth(), site.getDeast()));

		p.setProperty("sourceDefinition", "PROPERTIESFILE");
		StringBuffer sourceDefinitionList = new StringBuffer();
		for (GeoVector point : dataBucket.points)
			sourceDefinitionList.append(point.toString("%1.6f,%1.6f,%1.3f;"));
		p.setProperty("sourceDefinitionList", sourceDefinitionList.toString());

        File rayPropertiesFileName = new File(ioDirectory.getAbsoluteFile()+".properties");
        p.setProperty("propertiesFileName", rayPropertiesFileName.getAbsolutePath());
        
        String comment = String.format("Temporary properties file created automatically by %s\n"
                + "which will be used by RayUncertainty to compute path dependent travel time uncertainty values",
        getClass().getName());
        
        FileOutputStream fos = new FileOutputStream(rayPropertiesFileName);
        
        p.store(fos, comment);
        
        fos.close();

		return p;
	}
	
	/**
	 * Delete the specified file from the file system.  If the specified
	 * file is a directory, this method will recursively delete all the 
	 * files and directories in the specified directory before deleting the
	 * directory itself.
	 * @param file
	 */
	private boolean deleteFile(File file)
	{
		if (file == null)
			return false;
		
		if (file.isDirectory())
			for (File f : file.listFiles())
				deleteFile(f);
		boolean deleted = false;
		try {
			deleted = java.nio.file.Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print("directory "+file.getAbsolutePath()+" is not empty.\nContents:\n");
			for (String f : file.list())
				System.out.println(f);
		}
		return deleted;
	}
	
	/**
	 * Get a File consisting of root/rayuncertainty-yyyy-MM-dd-HH-mm-ss-SSS
	 * @param root
	 * @return
	 * @throws Exception
	 */
	private synchronized File getWorkDir(File root, String sta) throws Exception
	{
		Thread.sleep(50);
		DateFormat tformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		return new File(root, String.format("rayuncertainty-%s-%s",sta, tformat.format(new Date())));
	}

}
