package gov.sandia.gmp.pcalc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.rayuncertainty.RayUncertainty;
import gov.sandia.gmp.util.globals.Globals;
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
		
		// delete the ray uncertainty properties file (created by buildPropertyFile method).
		deleteFile(properties.getFile("propertiesFileName"));
		
		// delete the temporary ray uncertainty work directory
		deleteFile(properties.getFile("ioDirectory"));
		
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
		input = new Scanner(new File(ioDirectory, "uncertainty.txt"));
		String line = input.nextLine();
		while (!line.trim().startsWith("StaA"))
			line = input.nextLine();
		int count = 0;
		while (input.hasNext())
		{
			line = input.nextLine();
			++count;
			Scanner in = new Scanner(line);
			in.next(); // ignore sta 
			dataBucket.modelValues[in.nextInt()][idx] = in.nextDouble();
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
		
		File ioDirectory = getWorkDir(workDir);
		
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
			p.setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", ""));
		
		if (pcalc.properties.containsKey("slownessLayerStandardDeviation_S"))
			p.setProperty("slownessLayerStandardDeviation_S", pcalc.properties.getProperty("slownessLayerStandardDeviation_S"));
		else		
			p.setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", ""));
		
		p.setProperty("geoModelTomographyPath", tomoModel.getParent());
		p.setProperty("geoModelTomographyFileName", tomoModel.getName());

		p.setProperty("covarianceMatrixActiveNodeMapPath", benderModel.getAbsolutePath());
		p.setProperty("covarianceFileServerPaths", benderModel.getAbsolutePath());

		p.setProperty("covarianceMatrixActiveNodeMapFileName", "activenodeIndexMap");
		p.setProperty("covarianceFileServerBlockMap", pcalc.properties.getProperty("covarianceFileServerBlockMap", ""));
		p.setProperty("covarianceFileServerStorageUseFraction", pcalc.properties.getProperty("covarianceFileServerStorageUseFraction", ""));
		p.setProperty("covarianceFileSecondaryPaths", pcalc.properties.getProperty("covarianceFileSecondaryPaths", "cov ginv"));

		
		p.setProperty("displayGUI", pcalc.properties.getBoolean("displayGUI", true));
		
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
        
        p.store(new FileOutputStream(rayPropertiesFileName), comment);

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
	 * Get a File consisting of root/deleteme_<currentTimeMillis>
	 * @param root
	 * @return
	 * @throws Exception
	 */
	private synchronized File getWorkDir(File root) throws Exception
	{
		Thread.sleep(50);
		return new File(root, String.format("deleteme_%d",System.currentTimeMillis()));
	}

}
