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
package gov.sandia.gmp.lookupdz;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.EllipticityCorrections;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.StaType;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.LookupTableInterface;
import gov.sandia.gmp.baseobjects.interfaces.PathCorrectionsInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.libcorr3dgmp.LibCorr3DGMP;
import gov.sandia.gmp.libcorr3dgmp.UncertaintyHierarchical;
import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Utils;

/**
 * LookupTablesGMP, like all implementations of Predictor, is NOT thread-safe.
 * @author sballar
 *
 */
public class LookupTablesGMP extends Predictor implements UncertaintyInterface
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2512941352701245224L;

	/**
	 * Name of the supported model .
	 */
	private String modelName;

	/**
	 * Path to directory that contains all the lookup tables for the supported
	 * model
	 */
	private File tableDirectory;

	/**
	 * Path to directory that contains ellipticity corrections
	 */
	private File ellipticityDirectory;

	private boolean useEllipticityCorrections;
	private boolean useElevationCorrections;
	private double sedimentaryVelocity;
	
	private long algorithmId = -1, modelId = -1;

	private boolean fileNamesIncludeModelName;

	private HashMap<SeismicPhase, LookupTable> tableMap = new HashMap<SeismicPhase, LookupTable>(20);

	private final EllipticityCorrections ellip;
	
	/**
	 * Extrapolation flag. Uses extrapolation if required and this flag is true.
	 */
	private boolean useExtrapolation = false;

	/**
	 * This is the set of GeoAttributes that LookupTablesGMP is capable of
	 * computing. The set of GeoAttributes that is actually computed during any
	 * call to LookupTablesGMP.getPrediction() or getPredictions() will depend
	 * on the set of requestetdAttributes that are submitted as part of the
	 * PredictionRequestInterface object.
	 */
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet
			.of(GeoAttributes.TRAVEL_TIME, 
					GeoAttributes.TT_BASEMODEL,
					GeoAttributes.TT_MODEL_UNCERTAINTY,
					GeoAttributes.TT_PATH_CORRECTION,
					GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.TT_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.TT_ELLIPTICITY_CORRECTION,
					GeoAttributes.TT_ELEVATION_CORRECTION,
					GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE,
					GeoAttributes.DTT_DLAT, 
					GeoAttributes.DTT_DLON,
					GeoAttributes.DTT_DR, 
					GeoAttributes.DTT_DTIME,
					GeoAttributes.AZIMUTH, 
					GeoAttributes.AZIMUTH_DEGREES,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DAZ_DLAT, 
					GeoAttributes.DAZ_DLON,
					GeoAttributes.DAZ_DR, 
					GeoAttributes.DAZ_DTIME,
					GeoAttributes.SLOWNESS, 
					GeoAttributes.SLOWNESS_DEGREES,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DSH_DLAT, 
					GeoAttributes.DSH_DLON,
					GeoAttributes.DSH_DR, 
					GeoAttributes.DSH_DTIME,
					GeoAttributes.BACKAZIMUTH, 
					GeoAttributes.OUT_OF_PLANE,
					GeoAttributes.CALCULATION_TIME, 
					GeoAttributes.DISTANCE,
					GeoAttributes.DISTANCE_DEGREES);
	
	protected EnumSet<SeismicPhase> supportedPhases;

	public LookupTablesGMP(PropertiesPlusGMP properties)
			throws GMPException, IOException
	{
		this(properties, null);
	}

	public LookupTablesGMP(PropertiesPlusGMP properties,
			LookupTableInterface libcorr3DModelsGMP)
			throws GMPException, IOException
	{
		super(properties, properties.getInt("lookup2dPredictionsPerTask", 10000));

		String prefix = "lookup2d";

		modelName = properties.getProperty(prefix + "Model", "ak135");

		tableDirectory = properties.getFile(prefix + "TableDirectory");

		ellipticityDirectory = properties.getFile(prefix+"EllipticityCorrectionsDirectory");

		if (tableDirectory == null || ellipticityDirectory == null)
		{
			File seismicBaseData = properties.getFile("seismicBaseData", new File("seismic-base-data.jar"));

			this.tableDirectory = new File(new File(seismicBaseData, "tt"), modelName);

			this.ellipticityDirectory = new File(new File(seismicBaseData, "el"), "ak135");
		}

		fileNamesIncludeModelName = new File(tableDirectory, modelName + ".P").exists();

		discoverSupportedPhases();
		
		useElevationCorrections = properties.getBoolean(prefix
				+ "UseElevationCorrections", true);

		sedimentaryVelocity = properties.getDouble(prefix
				+ "SedimentaryVelocity", 5.8);

		useEllipticityCorrections = properties.getBoolean(prefix
				+ "UseEllipticityCorrections", true);
		
		ttModelUncertaintyScale = properties.getDoubleArray(prefix+"TTModelUncertaintyScale", null);

		if (useEllipticityCorrections)
			ellip = new EllipticityCorrections(ellipticityDirectory);
		else
			ellip = null;

		setupPathCorrectionsInterface(properties, libcorr3DModelsGMP);
		setupUncertaintyInterface(properties);

	}

	/**
	 * This constructor is used to construct an instance of LookupTablesGMP that
	 * is fully configured and ready to compute a Prediction by simply calling
	 * getPrediction(). Used when setting up for concurrent prediction
	 * calculations. All the arguments must be thread-safe.
	 * 
	 * @param predictionRequest
	 * @param directory
	 *            full path to the directory that contains all the travel time
	 *            lookup tables. This is a directory full of files whose names
	 *            correspond to phase names.
	 * @param modelName
	 *            the name of the model. This is the name of the subdirectory
	 *            that contains all the phase files.
	 * @param sedimentaryVelocity
	 * @param taupToolkitModel
	 * @throws GMPException
	 * @throws IOException
	 */
	protected LookupTablesGMP(String modelName, File tableDirectory,
			File ellipticityDirectory, PathCorrectionsInterface pathCorr,
			boolean usePathCorrectionsInDerivativesTT,
			boolean usePathCorrectionsInDerivativesAZ,
			boolean usePathCorrectionsInDerivativesSH,
			boolean useEllipticityCorrections, 
			boolean useElevationCorrections,
			double sedimentaryVelocity, 
			double[] ttModelUncertaintyScale,
			int maxProcessors, int predictionsPerTask, int predictorVerbosity)
			throws GMPException, IOException
	{
		super(null, maxProcessors, predictionsPerTask, predictorVerbosity);

		this.modelName = modelName;

		this.tableDirectory = tableDirectory;

		this.ellipticityDirectory = ellipticityDirectory;

		try
		{
			this.ellip = new EllipticityCorrections(ellipticityDirectory);
		}
		catch (FileNotFoundException e)
		{
			throw new GMPException(e);
		}

		this.useEllipticityCorrections = useEllipticityCorrections;
		this.useElevationCorrections = useElevationCorrections;
		this.sedimentaryVelocity = sedimentaryVelocity;
		
		this.ttModelUncertaintyScale = ttModelUncertaintyScale;

		this.pathCorrections = pathCorr;
		this.usePathCorrectionsInDerivativesTT = usePathCorrectionsInDerivativesTT;
		this.usePathCorrectionsInDerivativesAZ = usePathCorrectionsInDerivativesAZ;
		this.usePathCorrectionsInDerivativesSH = usePathCorrectionsInDerivativesSH;

		if (this.pathCorrections != null)
			uncertaintyInterface = new UncertaintyHierarchical(
					(UncertaintyInterface) pathCorrections, this);
		else
			uncertaintyInterface = this;

		discoverSupportedPhases();
	}

	/**
	 * Retrieve a fully configured instance of LookupTablesGMP that is ready to
	 * compute a Prediction of the supplied PredictionRequest item. The returned
	 * Predictor is really a copy of this Predictor. Used when setting up for
	 * concurrent prediction calculations.
	 * 
	 * @throws IOException
	 */
	@Override
	protected LookupTablesGMP getCopy() throws GMPException, IOException
	{

		PathCorrectionsInterface pathCorr = this.pathCorrections == null ? null
				: this.pathCorrections.copyPathCorr();

		return new LookupTablesGMP(modelName, tableDirectory,
				ellipticityDirectory, pathCorr,
				usePathCorrectionsInDerivativesTT, 
				usePathCorrectionsInDerivativesAZ, 
				usePathCorrectionsInDerivativesSH, 
				useEllipticityCorrections,
				useElevationCorrections, sedimentaryVelocity,
				this.ttModelUncertaintyScale, 
				this.getMaxProcessors(), this.getPredictionsPerTask(),
				this.getPredictorVerbosity());
	}

	/**
	 * Setup path corrections. The following properties are relevant
	 * <p>
	 * lookup2dPathCorrectionsType (geomodel | libcorr) <br>
	 * lookup2dGeoModelPathCorrectionsRoot <br>
	 * lookup2dLibCorrPathCorrectionsRoot <br>
	 * lookup2dLibCorrPathCorrectionsRelativeGridPath
	 * lookup2dLibCorrLibCorrOptimizationType
	 * lookup2dUsePathCorrectionsInDerivatives lookup2dLibCorrInterpolatorType
	 * 
	 * @param properties
	 * @throws GMPException
	 * @throws IOException
	 */
	protected void setupPathCorrectionsInterface(PropertiesPlusGMP properties,
			LookupTableInterface libcorr3DModelsGMP) throws GMPException,
		IOException
	{
		String prefix = "lookup2d";
		String type = properties.getProperty(prefix + "PathCorrectionsType");
		if (type == null)
			pathCorrections = null;
		else if (type.toLowerCase().startsWith("libcorr"))
		{
			File rootPath = properties.getFile(prefix
					+ "LibCorrPathCorrectionsRoot");
			String relGridPath = properties.getProperty(prefix
					+ "LibCorrPathCorrectionsRelativeGridPath", ".");

			boolean preloadModels = properties.getBoolean(prefix
					+ "LibCorrPreloadModels", false);

			boolean usePathCorrectionsInDerivatives = properties.getBoolean(prefix
					+ "UsePathCorrectionsInDerivatives", false);
			
			usePathCorrectionsInDerivativesTT = properties.getBoolean(prefix
					+ "UsePathCorrectionsInDerivativesTT", usePathCorrectionsInDerivatives);
			
			usePathCorrectionsInDerivativesAZ = properties.getBoolean(prefix
					+ "UsePathCorrectionsInDerivativesAZ", usePathCorrectionsInDerivatives);
			
			usePathCorrectionsInDerivativesSH = properties.getBoolean(prefix
					+ "UsePathCorrectionsInDerivativesSH", usePathCorrectionsInDerivatives);
			

			InterpolatorType interpTypeHorz = InterpolatorType
					.valueOf(properties.getProperty(
							prefix + "LibCorrInterpolatorTypeHorizontal",
							"linear").toUpperCase());

			InterpolatorType interpTypeRadial = InterpolatorType
					.valueOf(properties.getProperty(
							prefix + "LibCorrInterpolatorTypeRadial", "linear")
							.toUpperCase());

			if (libcorr3DModelsGMP == null)
				pathCorrections = new LibCorr3DGMP(rootPath, relGridPath,
						preloadModels, interpTypeHorz,
						interpTypeRadial);
			else
				pathCorrections = new LibCorr3DGMP(libcorr3DModelsGMP,
						interpTypeHorz, interpTypeRadial);
		}
	}

	/**
	 * Setup uncertainties. If pathCorrections are active then the uncertainty
	 * type will be hierarchical otherwise uncertainty type will be
	 * DistanceDependent
	 * 
	 * @throws GMPException
	 * @throws IOException
	 */
	@Override
	protected void setupUncertaintyInterface(PropertiesPlusGMP properties)
		throws GMPException, IOException
	{
		String prefix = "lookup2d";
		String type = pathCorrections != null ? "hierarchical"
				: "DistanceDependent";

		type = properties.getProperty(prefix + "UncertaintyType", type);

		if (type.equalsIgnoreCase("hierarchical"))
			uncertaintyInterface = new UncertaintyHierarchical(
					(UncertaintyInterface) pathCorrections, this);
		else if (type.equalsIgnoreCase("DistanceDependent"))
			uncertaintyInterface = this;
		else
			throw new GMPException("\nUnable to set up uncertaintyInterface\n");
	}

	/**
	 * Return a Map from ReceiverInterface -> SeismicPhase -> GeoAttributes that
	 * includes specific observations that this Predictor wishes to support.
	 * Even if overall responsibility for some phase is assigned by
	 * PredictorFactory to another Predictor, this Predictor will try to claim
	 * the specific observations included in this map for itself. < * p> This
	 * Predictor may also support other observations based on SeismicPhase as
	 * determined by PredictorFactory
	 * 
	 * <p>
	 * GeoAttributes will include a subset of TRAVEL_TIME, AZIMUTH and SLOWNESS.
	 * 
	 * @return
	 * @throws GMPException
	 */
	@Override
	public AttributeIndexerSmart claimObservations() throws GMPException
	{
		if (pathCorrections == null)
			return new AttributeIndexerSmart();

		return pathCorrections.claimObservations();
	}

	private File getFile(SeismicPhase phase) throws FileNotFoundException
	{
		if (fileNamesIncludeModelName)
			return new File(tableDirectory, modelName + "." + phase.getFileName());
		
		return new File(tableDirectory, phase.getFileName());
	}

	public LookupTable getTable(SeismicPhase phase) throws Exception
	{
		LookupTable table = tableMap.get(phase);
		if (table == null)
			tableMap.put(phase, table = new LookupTable(getFile(phase)));
		return table;
	}

	@Override
	public String getPredictorName()
	{
		return this.getClass().getSimpleName();
	}
	
	@Override
	public Object getEarthModel()
	{
		return tableMap;
	}

	@Override
	public String getModelName()
	{
		return modelName;
	}

	@Override
	public String getModelName(int maxLength)
	{
		return modelName.length() <= maxLength ? modelName : modelName
				.substring(0, maxLength);
	}

	@Override
	public String getModelDescription() throws GMPException
	{
		return modelName;
	}

	@Override
	public boolean isSupported(ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute, double epochTime)
	{
		return supportedAttributes.contains(attribute)
				&& getSupportedPhases().contains(phase);
	}

	@Override
	public void setAlgorithmId(long algorithmId)
	{
		this.algorithmId = algorithmId;
	}

	@Override
	public long getAlgorithmId()
	{
		return algorithmId;
	}

	@Override
	public void setModelId(long modelId)
	{
		this.modelId = modelId;
	}

	@Override
	public long getModelId()
	{
		return modelId;
	}

	@Override
	public File getModelFile()
	{
		return tableDirectory;
	}
	
	public File getModelFile(SeismicPhase phase) throws Exception
	{
		LookupTable tbl = getTable(phase);
		return tbl == null ? null : tbl.getFile();
	}

	@Override
	public double getSurfaceRadius(GeoVector position) throws GMPException
	{
		return 6371.;
	}

	@Override
	public double getSurfaceDepth(GeoVector position) throws GMPException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public PredictionInterface getPrediction(PredictionRequestInterface request)
		throws GMPException
	{
		travelTime = slowness = dttdr = dshdx = dshdr = Globals.NA_VALUE;

		if (!request.isDefining())
			return new Prediction(request, this,
					"PredictionRequest was non-defining");

		long timer = System.currentTimeMillis();

		Prediction result = new Prediction(request, this);
		
		LookupTable table = null;

		try
		{

			double xDeg = request.getDistanceDegrees();
			double depth = Math.max(request.getSource().getPosition().getDepth(), 0.);
			
			// deal with roundoff errors that prevent valid depths from being processed.
			if (depth > 700. && depth < 700.01)
				depth = 700.;

			double[] predictions = new double[6];

			table = getTable(request.getPhase());

			if (table == null)
				return new Prediction(request, this, String.format(
						"Phase %s is not supported.", request.getPhase().toString()));

			int code = table.interpolate(
					xDeg,
					depth,
					request.getRequestedAttributes().contains(
							GeoAttributes.DTT_DR),
					request.getRequestedAttributes().contains(
							GeoAttributes.DSH_DR), useExtrapolation, predictions);

			//if (code != 0)
			if (code < 0 || (code > 0 && !useExtrapolation))
				return new Prediction(
						request,
						this,
						String.format(
								"LookupTable.interpolate() returned code %d: %s%n"
										+ "Source location %s%n"
										+ "Receiver %s%n"
										+ "Phase %s%n"
										+ "Receiver-source distance, azimuth %1.3f deg, %1.2f%n",
								code,
								LookupTable.getErrorMessage(code),
								request.getSource().getPosition().toString(),
								request.getReceiver().toString(),
								request.getPhase().toString(),
								request.getReceiver()
										.getPosition()
										.distanceDegrees(
												request.getSource()
														.getPosition()),
								request.getReceiver()
										.getPosition()
										.azimuthDegrees(
												request.getSource()
														.getPosition(), -999.)));
			
			// elements of predictions array:
			// 0: tt (sec)
			// 1: dtdx (sec/degree)
			// 2: d2tdx2 (sec/degree^2)
			// 3: dtdz (sec/km)
			// 4: d2tdz2 (sec/km^2)
			// 5: d2tdxdz (sec/(km.degree))
			
			travelTime = predictions[0];
			slowness = toDegrees(predictions[1]);
			dshdx = toDegrees(toDegrees(predictions[2]));
			dttdr = -predictions[3];
			dshdr = -toDegrees(predictions[5]);

			setLookupTableIndexes(request);
			
			if (ellip != null)
			{
				double ellipCorr = ellip.getEllipCorr(request.getPhase(),
						request.getReceiver().getPosition(), request
								.getSource().getPosition());
				result.setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION,
						ellipCorr);

				travelTime += ellipCorr;
			}

			if (useElevationCorrections)
			{
				// find the elevation correction for the receiver
				double elevCorr = getElevationCorrection(-request.getReceiver()
						.getPosition().getDepth(), slowness);
				result.setAttribute(GeoAttributes.TT_ELEVATION_CORRECTION,
						elevCorr);

				// if the source is above the surface of the earth, then
				// find an elevation correction for the source.
				double srcElev = -request.getSource().getPosition().getDepth();
				double srcElevCorr = srcElev <= 0. ? 0
						: getElevationCorrection(srcElev, slowness);
				result.setAttribute(
						GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE,
						srcElevCorr);

				travelTime += elevCorr + srcElevCorr;
			}
			
			setGeoAttributes(request, result, uncertaintyInterface);
			
			// setGeoAttributes will set tt_basemodel equal to travel time 
			// including ellipticity and elevation corrections.  Return it 
			// to uncorrected value.
			result.setAttribute(GeoAttributes.TT_BASEMODEL, predictions[0]);
			
			result.setRayType(RayType.REFRACTION);

		}
		catch (Exception e)
		{
			//e.printStackTrace();
			result = new Prediction(request, this, e);
		}
		
		if (request.getRequestedAttributes().contains(GeoAttributes.CALCULATION_TIME))
			result.setAttribute(GeoAttributes.CALCULATION_TIME, (System.currentTimeMillis() - timer) * 1e-3);

		return result;
	}

	/**
	 * Retrieve the elevation correction for a given elevation above sea level
	 * (in km) and horizontal slowness (in sec/radian). The correction is
	 * elevation/sedimentaryVelocity * cos(incidence_angle).
	 * 
	 * @param elevation
	 *            above sea level, in km
	 * @param slowness
	 *            horizontal slowness in sec/radian
	 * @return elevation correction in sec.
	 */
	public double getElevationCorrection(double elevation, double slowness)
	{
		double el = slowness * sedimentaryVelocity / 6371.;
		if (el > 1.0)
			el = 1.0 / el;
		return elevation / sedimentaryVelocity
				* sqrt(1. - min(1.0, pow(el, 2)));
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to
	 * the supplied string.
	 */
	@Override
	protected PredictionInterface getNewPrediction(
			PredictionRequestInterface predictionRequest, String msg)
	{
		return new Prediction(predictionRequest, this, msg);
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to
	 * the error message and stack trace of the supplied Exception.
	 */
	@Override
	protected PredictionInterface getNewPrediction(
			PredictionRequestInterface predictionRequest, Exception e)
	{
		return new Prediction(predictionRequest, this, e);
	}

	// ///////////////////////////////////////////////////////////////////
	//
	// UncertaintyInterface
	//
	// ///////////////////////////////////////////////////////////////////

	@Override
	public String getUncertaintyRootDirectory() throws IOException
	{
		return tableDirectory.getCanonicalPath();
	}

	@Override
	public String getUncertaintyType()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException,
		IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve the code version and timestamp
	 * 
	 * @return code version
	 */
	public static String getVersion() 
	{
		return Utils.getVersion("lookup-tables-dz");
	}

	@Override
	public String getPredictorVersion() {
		return getVersion();
	}
	
	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}
	
	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute) throws IOException
	{
		return getFile(phase).getPath();
	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		try
		{
			LookupTable table = getTable(phase);
			return table != null && table.getUncertainties() != null
					&& table.getUncertainties().length > 0;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Override
	public int getLookupTableIndex(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return -1;
	}

	@Override
	public int getLookupTableIndex(
			PredictionRequestInterface predictionRequest,
			GeoAttributes ttPathCorrection)
	{
		return -1;
	}

	@Override
	public double getUncertainty(SourceInterface source,
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException
	{
		switch (attribute)
		{
		case TT_MODEL_UNCERTAINTY:
			try {
				LookupTable table = getTable(phase);
				return table == null ? Globals.NA_VALUE : table
						.interpolateUncertainty(source.getPosition()
								.distanceDegrees(receiver.getPosition()), Math.max(
								source.getPosition().getDepth(), 0));
			} catch (Exception e) {
				throw new GMPException(e);
			}
		case AZIMUTH_MODEL_UNCERTAINTY:
		{
			double distance = source.getPosition().distanceDegrees(
					receiver.getPosition());
			if (receiver.getStaType() == StaType.ARRAY)
			{
				if (distance < 30)
					return toRadians(5);
				if (distance < 100)
					return toRadians(2);
				return toRadians(1);
			}
			if (distance < 30)
				return toRadians(20);
			if (distance < 100)
				return toRadians(10);
			return toRadians(5);
		}
		case AZIMUTH_MODEL_UNCERTAINTY_DEGREES:
		{
			double distance = source.getPosition().distanceDegrees(
					receiver.getPosition());
			if (receiver.getStaType() == StaType.ARRAY)
			{
				if (distance < 30)
					return 5;
				if (distance < 100)
					return 2;
				return 1;
			}
			if (distance < 30)
				return 20;
			if (distance < 100)
				return 10;
			return 5;
		}
		case SLOWNESS_MODEL_UNCERTAINTY:
			return receiver.getStaType() == StaType.ARRAY ? toDegrees(1.5)
					: toDegrees(2.5);
		case SLOWNESS_MODEL_UNCERTAINTY_DEGREES:
			return receiver.getStaType() == StaType.ARRAY ? 1.5 : 2.5;
		default:
			throw new GMPException(
					"attribute is "
							+ attribute.toString()
							+ " but must be one of "
							+ "[ TT_MODEL_UNCERTAINTY | AZIMUTH_MODEL_UNCERTAINTY | SLOWNESS_MODEL_UNCERTAINTY "
							+ "| AZIMUTH_MODEL_UNCERTAINTY_DEGREES | SLOWNESS_MODEL_UNCERTAINTY_DEGREES ]");

		}
	}

	@Override
	public double getUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		return getUncertainty(prediction.getSource(), prediction.getReceiver(),
				prediction.getPhase(), attribute);
	}

	@Override
	public boolean setUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		prediction.setAttribute(attribute,
				getUncertainty(prediction, attribute));
		return prediction.getAttribute(attribute) != Globals.NA_VALUE;
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex)
		throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getUncertainty(int lookupIndex, SourceInterface source)
		throws GMPException
	{
		throw new UnsupportedOperationException();
	}

	public PathCorrectionsInterface getPathCorrModel()
	{
		return pathCorrections;
	}

	@Override
	public PredictorType getPredictorType()
	{
		return PredictorType.LOOKUP2D;
	}

	@Override public EnumSet<GeoAttributes> getSupportedAttributes() { return supportedAttributes; }
	
	@Override public EnumSet<SeismicPhase> getSupportedPhases() { return supportedPhases; }

	/**
	 * Examine all the files in the tableDirectory and determine which phases are supported.
	 * This private method is only called by LookupTablesGMP constructors.
	 * @throws FileNotFoundException 
	 */
	private void discoverSupportedPhases() throws FileNotFoundException
	{
		supportedPhases = EnumSet.noneOf(SeismicPhase.class);
		for (SeismicPhase phase : EnumSet.allOf(SeismicPhase.class))
			if (new SeismicBaseData(getFile(phase)).exists())
				supportedPhases.add(phase);
	}
	
	/**
	 * Some Predictors are able to look up attribute values such as
	 * TT_PATH_CORRECTION or TT_MODEL_UNCERTAINTY in lookup tables. It is
	 * advantageous to store the indexes of the attributes in the
	 * PredictionRequest object so that repeated calls for the same attribute
	 * values can be done as efficiently as possible.
	 * <p>
	 * This Predictor method is called by a PredictionRequest object so that the
	 * Predictor can set the lookup table indexes in the PredictionRequest.
	 * 
	 * @param predictionRequest
	 *            reference to the PredictionRequest object that is requesting
	 *            to have its lookup table indexes set.
	 */
	@Override
	public void setLookupTableIndexes(
			PredictionRequestInterface predictionRequest)
	{
		if (pathCorrections != null
				&& !predictionRequest.haveLookupIndexesBeenSet())
		{
			predictionRequest.setLookupIndexPathCorrTT(pathCorrections
					.getLookupTableIndex(predictionRequest,
							GeoAttributes.TT_PATH_CORRECTION));

			predictionRequest.setLookupIndexPathCorrAZ(pathCorrections
					.getLookupTableIndex(predictionRequest,
							GeoAttributes.AZIMUTH_PATH_CORRECTION));

			predictionRequest.setLookupIndexPathCorrSH(pathCorrections
					.getLookupTableIndex(predictionRequest,
							GeoAttributes.SLOWNESS_PATH_CORRECTION));
		}

		if (uncertaintyInterface instanceof UncertaintyHierarchical)
		{
			int index = uncertaintyInterface.getLookupTableIndex(
					predictionRequest, GeoAttributes.TT_MODEL_UNCERTAINTY);
			if (index >= 0)
				predictionRequest.setLookupIndexUncertaintyTT(index);
			else
				predictionRequest.setLookupIndexUncertaintyTT(-1);

			index = uncertaintyInterface.getLookupTableIndex(predictionRequest,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY);
			if (index >= 0)
				predictionRequest.setLookupIndexUncertaintyAZ(index);
			else
				predictionRequest.setLookupIndexUncertaintyAZ(-1);

			index = uncertaintyInterface.getLookupTableIndex(predictionRequest,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);
			if (index >= 0)
				predictionRequest.setLookupIndexUncertaintySH(index);
			else
				predictionRequest.setLookupIndexUncertaintySH(-1);
		}
	}

	private double travelTime;
	private double slowness;
	private double dttdr;
	private double dshdx;
	private double dshdr;
	
	@Override
	protected double getTravelTime() { return travelTime; }

	@Override
	protected double getSlowness() { return slowness; }

	@Override
	protected double getDttDr() { return dttdr; }
	
	
	@Override
	protected double getDshDx() { return dshdx; }
	
	@Override
	protected double getDshDr() { return dshdr; }
	
	
}
