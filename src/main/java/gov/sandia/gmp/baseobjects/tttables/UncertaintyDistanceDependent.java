/**
 * 
 */
package gov.sandia.gmp.baseobjects.tttables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * @author sballar
 *
 */
public class UncertaintyDistanceDependent implements UncertaintyInterface {

	protected AttributeTables uncertaintyTables;

	/**
	 * 
	 * @param properties
	 * @throws GMPException if 'prefixUncertaintyDirectory' and
	 *                      'prefixUncertaintyModel' properties are not specified.
	 */
	public UncertaintyDistanceDependent(PropertiesPlusGMP properties, String prefix) throws GMPException {
		// uncertaityDirectory should be something like "SNL_Tool_Root/seismicBaseData"
		File uncertaityDirectory = properties.getFile(prefix + "UncertaintyDirectory",
				new File("seismic-base-data.jar"));
		
		String modelName;
		
		if (uncertaityDirectory.exists() && uncertaityDirectory.isDirectory() &&
				new File(uncertaityDirectory, "distance_dependent_uncertainty").exists() &&
				new File(uncertaityDirectory, "prediction_model.geotess").exists())
		{
			uncertaityDirectory = new File(uncertaityDirectory, "distance_dependent_uncertainty");
			File ttDir = new File(uncertaityDirectory, "tt");
			
			String[] list = ttDir.list();
			// must ignore all file names that start with '.'
			ArrayList<String> modelFiles = new ArrayList<>(list.length);
			for (String s : list)
				if (new File(ttDir, s).isDirectory())
					modelFiles.add(s);
			
			if (modelFiles.size() != 1)
				throw new GMPException(String.format("Expected to find 1 uncertainty model file in directory%n"
						+ "%s%n"
						+ "but found %d:%n"
						+ "%s%n",
						new File(uncertaityDirectory, "tt").getAbsolutePath(),
						modelFiles.size(), Arrays.toString(modelFiles.toArray())));
			modelName = modelFiles.get(0);
		}
		else
		{
			modelName = properties.getProperty(prefix + "UncertaintyModel", "ak135");

			if (modelName.length() == 0)
				throw new GMPException(
						prefix + "UncertaintyModel is not specified in the property file. " + "Suggested value is ak135");
		}
		
		try {
			uncertaintyTables = new AttributeTables(uncertaityDirectory, modelName);
		} catch (FileNotFoundException e) {
			uncertaintyTables = null;
			throw new GMPException(e);
		}
	}

	private UncertaintyDistanceDependent(AttributeTables uncertaintyTables) {
		this.uncertaintyTables = uncertaintyTables;
	}

	/**
	 * Ensures that all necessary files are specified in par file and exist in file
	 * system.
	 * 
	 * @param properties
	 * @param propertyPrefix
	 * @return error messages or empty string if no errors.
	 * @throws GMPException
	 */
	static public String checkFiles(PropertiesPlusGMP properties, String propertyPrefix) throws GMPException {
		File seismicBaseData = properties.getFile(propertyPrefix + "UncertaintyDirectory");

		String errMessage = "";
		if (seismicBaseData == null)
			errMessage += propertyPrefix + "UncertaintyDirectory is not specified in the property file" + Globals.NL;

		String modelName = properties.getProperty(propertyPrefix + "UncertaintyModel", "");

		if (modelName.length() == 0)
			errMessage += propertyPrefix + "UncertaintyModel is not specified in the property file." + Globals.NL;

		File f = new File(new File(seismicBaseData, "tt"), modelName);
		if (!f.exists())
			try {
				errMessage += String.format(propertyPrefix + "UncertaintyModel %s not found in file sytem%n",
						f.getCanonicalPath());
			} catch (IOException e) {
				errMessage += e.getMessage();
			}
		return errMessage;
	}

	@Override
	public String getUncertaintyType() {
		return this.getClass().getSimpleName();
	}

	static public String getVersion() {
		return "1.0.0";
	}

	/**
	 * Returns true if the files containing the uncertainty information were
	 * successfully located.
	 * 
	 * @return
	 */
	public boolean isValid() {
		return uncertaintyTables != null;
	}

	public Table getTable(GeoAttributes attribute, SeismicPhase phase) throws IOException, GMPException {
		return uncertaintyTables.getTable(attribute, phase);
	}

	/*
	 * Set the value of attribute to the appropriate uncertainty value.
	 */
	@Override
	public boolean setUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws IOException, GMPException {
		prediction.setAttribute(attribute, getUncertainty(prediction, attribute));
		return prediction.getAttribute(attribute) != Globals.NA_VALUE;
	}

	/*
	 * Return uncertainty estimate. For travel time, uncertainties come from bottom
	 * of travel time tables. For azimuth, returns a constant 15 degrees (converted
	 * to radians). For slowness returns a constant 1.5 sec/degree (converted to
	 * sec/radian).
	 */
	@Override
	public double getUncertainty(SourceInterface source, ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException {
		if (attribute == GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY)
			return Math.toRadians(15.);

		if (attribute == GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES)
			return 15.;

		if (attribute == GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY)
			return Math.toDegrees(1.5); // convert 1.5 sec/degree to sec/radian

		if (attribute == GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES)
			return 1.5; // convert 1.5 sec/degree to sec/radian

		if (isValid()) {
			if (attribute == GeoAttributes.TT_MODEL_UNCERTAINTY)
				try {
					return uncertaintyTables.getValue(attribute, phase,
							source.getPosition().distanceDegrees(receiver.getPosition()),
							source.getPosition().getDepth());
				} catch (OutOfRangeException e) {
					// Uncertainty should be designed such that it returns
					// a value at all distances and depths.
					e.printStackTrace();
					System.exit(1);
				}
		}
		return Globals.NA_VALUE;

	}

	/*
	 * Return uncertainty estimate. For travel time, uncertainties come from bottom
	 * of travel time tables. For azimuth, returns a constant 15 degrees (converted
	 * to radians). For slowness returns a constant 1.5 sec/degree (converted to
	 * sec/radian).
	 */
	@Override
	public double getUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws IOException, GMPException {
		if (attribute == GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY
				&& prediction.getSupportedAttributes().keySet().contains(GeoAttributes.AZIMUTH))
			return Math.toRadians(15.);

		if (attribute == GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES
				&& prediction.getSupportedAttributes().keySet().contains(GeoAttributes.AZIMUTH))
			return 15.;

		if (attribute == GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY
				&& prediction.getSupportedAttributes().keySet().contains(GeoAttributes.SLOWNESS))
			return Math.toDegrees(1.5); // convert 1.5 sec/degree to sec/radian

		if (attribute == GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES
				&& prediction.getSupportedAttributes().keySet().contains(GeoAttributes.SLOWNESS))
			return 1.5; // convert 1.5 sec/degree to sec/radian

		if (isValid()) {
			if (attribute == GeoAttributes.TT_MODEL_UNCERTAINTY
					&& prediction.getSupportedAttributes().keySet().contains(GeoAttributes.TRAVEL_TIME))
				try {
					return uncertaintyTables.getValue(attribute, prediction);
				} catch (OutOfRangeException e) {
					// Uncertainty should be designed such that it returns
					// a value at all distances and depths.
					e.printStackTrace();
					System.exit(1);
				}
		}
		return Globals.NA_VALUE;
	}

	public EnumSet<SeismicPhase> getSupportedPhases() {
		return uncertaintyTables.getSupportedPhases();
	}

	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		File f = uncertaintyTables.getFile(attribute, phase);
		if (f == null)
			return ("null");

//		try 
//		{
//			return f.getCanonicalPath();
//		} 
//		catch (IOException e) 
//		{
//			return e.getMessage();
//		}

		return f.getPath();

	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return true;
	}

	@Override
	public String getUncertaintyRootDirectory() throws IOException {
		return uncertaintyTables.getSeismicBaseData().getCanonicalPath();
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException {
		// assumption here is that AttributeTables is thread-safe.
		return new UncertaintyDistanceDependent(uncertaintyTables);
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
	public int getLookupTableIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return -1;
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex) throws IOException {
		return "";
	}

	@Override
	public double getUncertainty(int lookupTableIndex, SourceInterface source) throws GMPException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}

}
