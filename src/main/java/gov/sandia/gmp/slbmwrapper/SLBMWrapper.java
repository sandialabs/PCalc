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
package gov.sandia.gmp.slbmwrapper;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
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
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.libcorr3dgmp.LibCorr3DGMP;
import gov.sandia.gmp.libcorr3dgmp.UncertaintyHierarchical;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gnem.slbmjni.SLBMException;
import gov.sandia.gnem.slbmjni.SlbmInterface;

/**
 * Implements a wrapper around the SLBM_JNI (RSTT).
 * 
 * Like all implementations of PredictorInterface, SLBMWrapper is NOT thread-safe.
 * 
 * @author sballar
 * 
 */
public class SLBMWrapper extends Predictor implements UncertaintyInterface
{
    // annoyingly long method which will run at startup and try to load slbmjni,
    // first from the system paths, then by searching environment variables
    // ($RSTT_ROOT, $RSTT_HOME, $SLBM_ROOT, $SLBM_HOME). this also has to take
    // into account multiple possible file extensions on MacOS (.dylib, .jnilib)
    static
    {
        // first we'll just try and load the library
        try
        {
            System.loadLibrary("slbmjni");
        }

        // if that didn't work, we'll start checking environmental variables
        catch (java.lang.UnsatisfiedLinkError e)
        {
            // get the filename of the library we're looking for
            String libName = System.mapLibraryName("slbmjni");  // e.g., "libslbmjni.so"
            String libBase = libName.split("\\.")[0];  // file basename
            String libExt  = libName.split("\\.")[1];  // file extension

            // make our list of env vars to search, in preferred order
            String envVars[] = {"RSTT_ROOT", "RSTT_HOME", "SLBM_ROOT", "SLBM_HOME"};

            // initialize a boolean for when the library has loaded. if we have
            // successfully loaded it, we'll end the whole method
            boolean jniLoaded = false;

            // loop through each environment variable and look for slbmjni
            for (String env : envVars)
            {
                // try and get the environment variable
                String rootDir = System.getenv(env);

                // move on if it wasn't set
                if (rootDir == null)
                    continue;

                // first check if libName exists
                if (new File(rootDir + "/lib/" + libBase + "." + libExt).exists())
                    System.load(rootDir + "/lib/" + libBase + "." + libExt);  // load it

                // if that file doesn't exist, look for libslbmjni.jnilib
                else if (new File(rootDir + "/lib/" + libBase + ".jnilib").exists())
                    System.load(rootDir + "/lib/" + libBase + ".jnilib");  // load it

                // if that doesn't exist, I we'll move onto the next variable
                else
                    continue;

                // we made it this far, so we must have loaded the library!
                jniLoaded = true;  // set our boolean to true
                break;             // break out of the loop

            }

            // if, we still haven't loaded slbmjni, throw a helpful error message
            if (!jniLoaded)
            {
                // append some helpful info to the error message
                String errMsg = e.getMessage() + " or [$RSTT_ROOT/lib, $RSTT_HOME/lib, $SLBM_ROOT/lib, $SLBM_HOME/lib]";

                // make a new UnsatisfiedLinkError with our updated message
                UnsatisfiedLinkError ex = new UnsatisfiedLinkError(errMsg);

                // print out the stacktrace, some helpful info, and exit
                ex.printStackTrace();
                System.out.println("Did you try adding '-Djava.library.path=\"/path/to/rstt/lib\"' to your 'java' command?");
                System.out.println("Alternatively, set $RSTT_ROOT, $RSTT_HOME, $SLBM_ROOT, or $SLBM_HOME environment variables.");
                System.exit(1);
            }
        }
    }

	private static final long serialVersionUID = -2496823124037980507L;

	private final SlbmInterface slbm;

	private final File slbmModel;

	private final double slbmMaxDistance;
	private final double slbmMaxDepth;
	private final double slbmCHMax;

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
					GeoAttributes.TT_PATH_CORR_DERIV_LAT,
					GeoAttributes.TT_PATH_CORR_DERIV_LON,
					GeoAttributes.TT_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DTT_DLAT,
					GeoAttributes.DTT_DLON,
					GeoAttributes.DTT_DR,
					GeoAttributes.DTT_DTIME,
					GeoAttributes.AZIMUTH,
					GeoAttributes.AZIMUTH_DEGREES,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LAT,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LON,
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
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LAT,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LON,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL,
					// GeoAttributes.DSH_DLAT,
					// GeoAttributes.DSH_DLON,
					// GeoAttributes.DSH_DR,
					// GeoAttributes.DSH_DTIME,
					GeoAttributes.BACKAZIMUTH,
					GeoAttributes.BACKAZIMUTH_DEGREES,
					GeoAttributes.OUT_OF_PLANE, GeoAttributes.CALCULATION_TIME,
					GeoAttributes.DISTANCE, GeoAttributes.DISTANCE_DEGREES);

	protected static final EnumSet<SeismicPhase> supportedPhases = EnumSet.of(
			SeismicPhase.Pn, SeismicPhase.Sn, SeismicPhase.Pg, SeismicPhase.Lg);

	/**
	 * Implements a wrapper around the SLBM/RSTT
	 * 
	 * @param properties
	 * @throws GMPException
	 * @throws IOException
	 */
	public SLBMWrapper(PropertiesPlusGMP properties) throws Exception
	{
		this(properties, null);
	}

	/**
	 * Implements a wrapper around the SLBM/RSTT
	 * 
	 * @param properties
	 * @throws GMPException
	 * @throws IOException
	 */
	public SLBMWrapper(PropertiesPlusGMP properties,
			LookupTableInterface libcorr3DModelsGMP)
			throws Exception
	{
		super(properties, properties.getInt("slbmPredictionsPerTask", 100));

		if (properties.getProperty("slbmModel") == null)
			throw new GMPException(
					"Property slbmModel is not specified in the property file.");

		slbmModel = properties.getFile("slbmModel");

		if (!slbmModel.exists())
			throw new GMPException(String.format(
					"slbmModel = %s does not exist.",
					slbmModel.getCanonicalPath()));

		this.slbm = new SlbmInterface();
		try { slbm.loadVelocityModel(slbmModel.getCanonicalPath()); } 
		catch (SLBMException e) { throw new GMPException(e); }

		double val;
		val = properties.getDouble("slbm_max_distance", Globals.NA_VALUE);
		slbmMaxDistance = (val == Globals.NA_VALUE ? val : Math.toRadians(val));
		slbmMaxDepth = properties.getDouble("slbm_max_depth", Globals.NA_VALUE);
		slbmCHMax = properties.getDouble("slbm_ch_max", Globals.NA_VALUE);

		if (slbmMaxDistance != Globals.NA_VALUE)
			slbm.setMaxDistance(slbmMaxDistance);
		if (slbmMaxDepth != Globals.NA_VALUE)
			slbm.setMaxDepth(slbmMaxDepth);
		if (slbmCHMax != Globals.NA_VALUE)
			slbm.setCHMax(slbmCHMax);

		setupPathCorrectionsInterface(properties, libcorr3DModelsGMP);
		setupUncertaintyInterface(properties);
		
		ttModelUncertaintyScale = properties.getDoubleArray("slbmTTModelUncertaintyScale", null);

	}

	static public List<String> getRecognizedProperties()
	{
		return Arrays.asList(new String[] { "maxProcessors", "slbmModel",
				"slbm_max_distance", "slbm_max_depth", "slbm_ch_max" });
	}

	/**
	 * This constructor is used to construct an instance of SLBM that is fully
	 * configured and ready to compute a Prediction by simply calling
	 * getPrediction(). Used when setting up for concurrent prediction
	 * calculations. All the arguments must be thread-safe.
	 * 
	 * @param predictionRequest
	 * @param slbm
	 * @param slbmModel
	 * @param uncertaintyInterface
	 * @param queueSizeMax
	 * @throws GMPException
	 */
	protected SLBMWrapper(
			SlbmInterface slbm, File slbmModel, double slbmMaxDistance,
			double slbmMaxDepth, double slbmCHMax, int queueSizeMax,
			int predictionsPerTask, int predictorVerbosity,
			double[] ttModelUncertaintyScale,
			PathCorrectionsInterface pathCorr,
			boolean usePathCorrectionsInDerivativesTT,
			boolean usePathCorrectionsInDerivativesAZ,
			boolean usePathCorrectionsInDerivativesSH
			) throws GMPException
	{
		super(null, queueSizeMax, predictionsPerTask, predictorVerbosity);
		this.slbm = slbm;
		this.slbmModel = slbmModel;
		this.slbmMaxDistance = slbmMaxDistance;
		this.slbmMaxDepth = slbmMaxDepth;
		this.slbmCHMax = slbmCHMax;

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
	}

	/**
	 * Retrieve a fully configured instance of SLBMWrapper that is ready to
	 * compute a Prediction of the supplied PredictionRequest item. The returned
	 * Predictor is really a copy of this Predictor. Used when setting up for
	 * concurrent prediction calculations.
	 * 
	 * @throws IOException
	 */
	@Override
	protected Predictor getCopy()
		throws GMPException, IOException
	{
		SlbmInterface slbm = new SlbmInterface();
		try
		{
			slbm.loadVelocityModel(slbmModel.getCanonicalPath());
		}
		catch (IOException | SLBMException e)
		{
			throw new GMPException(e);
		}
		if (slbmMaxDistance != Globals.NA_VALUE)
			slbm.setMaxDistance(slbmMaxDistance);
		if (slbmMaxDepth != Globals.NA_VALUE)
			slbm.setMaxDepth(slbmMaxDepth);
		if (slbmCHMax != Globals.NA_VALUE)
			slbm.setCHMax(slbmCHMax);

		PathCorrectionsInterface pathCorr = this.pathCorrections == null ? null
				: this.pathCorrections.copyPathCorr();

		return new SLBMWrapper(slbm, slbmModel,
				slbmMaxDistance, slbmMaxDepth, slbmCHMax,
				this.getMaxProcessors(), this.getPredictionsPerTask(),
				this.getPredictorVerbosity(), this.ttModelUncertaintyScale,
				pathCorr, 
				usePathCorrectionsInDerivativesTT,
				usePathCorrectionsInDerivativesAZ,
				usePathCorrectionsInDerivativesSH);
	}

	/*
	 * Retrieve a Prediction for the supplied source, receiver, phase
	 * combination specified in the PredictionRequestInterface object. Computed
	 * travel times include ellipticity and elevation corrections, if they are
	 * required. If the request in non-defining then an invalid Prediction is
	 * returned with errormessage "PredictionRequest was non-defining".
	 * Derivatives of travel with respect to radius and slowness with respect to
	 * lat, lon and radius are computed only if the appropriate GeoAttributes
	 * are specified in the Set of RequestedAttributes supplied in the
	 * PredictionRequest.
	 * 
	 * @see
	 * gov.sandia.gmp.util.interfaces.PredictorInterface#getPrediction(gov.sandia
	 * .gmp.util.interfaces.PredictionRequestInterface)
	 */
	@Override
	public PredictionInterface getPrediction(PredictionRequestInterface request)
		throws GMPException
	{
		travelTime = slowness = dttdlat = dttdlon = dttdr = Globals.NA_VALUE;

		if (!request.isDefining())
			return new SLBMResult(request, this,
					"PredictionRequest was non-defining");

		if (slbmMaxDepth != Globals.NA_VALUE
				&& request.getSource().getPosition().getDepth() >= slbmMaxDepth)
			return new SLBMResult(request, this,
					String.format(
							"Source depth of %1.3f exceeds slbmMaxDepth %1.3f",
							request.getSource().getPosition().getDepth(),
							slbmMaxDepth));

		if (slbmMaxDistance != Globals.NA_VALUE && request.getDistance() >= slbmMaxDistance)
			return new SLBMResult(
					request,
					this,
					String.format(
							"Source-receiver distance of %1.3f exceeds slbmMaxDistance %1.3f",
							Math.toDegrees(request.getDistance()),
							Math.toDegrees(slbmMaxDistance)));

		String phase = request.getPhase() == SeismicPhase.P ? "Pn" : request.getPhase().toString();

		SLBMResult result = null;
		long timer = System.nanoTime();
		try
		{
			slbm.createGreatCircle(phase, 
					request.getSource().getPosition().getLat(), 
					request.getSource().getPosition().getLon(),
					request.getSource().getPosition().getDepth(), 
					request.getReceiver().getPosition().getLat(), 
					request.getReceiver().getPosition().getLon(),
					request.getReceiver().getPosition().getDepth());

			result = new SLBMResult(request, this);

			setLookupTableIndexes(request);
			
			setGeoAttributes(request, result, uncertaintyInterface);
			
			result.setRayType(RayType.REFRACTION);
		}
		catch (Exception e)
		{
			result = new SLBMResult(request, this, e);
		}

		if (request.getRequestedAttributes().contains(
				GeoAttributes.CALCULATION_TIME))
			result.setAttribute(GeoAttributes.CALCULATION_TIME,
					(System.nanoTime() - timer) * 1e-9);
		return result;
	}

	/*
	 * Retrieve the name of this Predictor: "SLBM"
	 */
	@Override
	public String getPredictorName()
	{
		return "SLBM";
	}

	@Override
	protected PredictionInterface getNewPrediction(
			PredictionRequestInterface predictionRequest, String msg)
	{
		return new SLBMResult(predictionRequest, this, msg);
	}

	@Override
	protected PredictionInterface getNewPrediction(
			PredictionRequestInterface predictionRequest, Exception ex)
	{
		return new SLBMResult(predictionRequest, this, ex);
	}

	/*
	 * Returns the File of the directory from which the slbm model was read.
	 */
	@Override
	public File getModelFile()
	{
		return slbmModel;
	}

	/*
	 * Returns the name of the directory from which the slbm model was read.
	 */
	@Override
	public String getModelDescription() throws GMPException
	{
		return getModelName();
	}

	/*
	 * Returns the name of the directory from which the slbm model was read.
	 */
	@Override
	public String getModelName()
	{
		return slbmModel.getName();
	}

	@Override
	public Object getEarthModel()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getSurfaceDepth(GeoVector position) throws GMPException
	{
		try
		{
			return slbm.getInterpolatedPoint(position.getLat(),
					position.getLon()).depth[1];
		}
		catch (SLBMException e)
		{
			throw new GMPException(e);
		}
	}

	@Override
	public double getSurfaceRadius(GeoVector position) throws GMPException
	{
		try
		{
			return position.getEarthRadius()
					- slbm.getInterpolatedPoint(position.getLat(),
							position.getLon()).depth[1];
		}
		catch (SLBMException e)
		{
			throw new GMPException(e);
		}
	}

	public SlbmInterface getSlbmInterface()
	{
		return slbm;
	}

	@Override
	public boolean isSupported(ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute, double epochTime)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	/**
	 * Returns the type of the UncertaintyInterface object: UncertaintyNAValue,
	 * UncertaintyDistanceDependent, etc.
	 */
	@Override
	public String getUncertaintyType()
	{
		return "Internal";
	}

	/*
	 * Retrieve the value returned by calling SlbmInterface.getVersion().
	 */
	public String getSlbmVersion()
	{
		return slbm == null ? "null" : slbm.getVersion();
	}

	static public String getVersion() 	{ 
		return Utils.getVersion("slbm-wrapper");
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
	public String getUncertaintyRootDirectory() throws IOException
	{
		return slbmModel.getCanonicalPath();
	}

	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute) throws IOException
	{
		return slbmModel.getCanonicalPath();
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getUncertainty(SourceInterface source,
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException
	{
		switch (attribute)
		{
		case TT_MODEL_UNCERTAINTY:
			try
			{
				return slbm.getTravelTimeUncertainty(phase.toString(), 
						source.getPosition().distance(receiver.getPosition()));
			}
			catch (SLBMException e)
			{
				throw new GMPException(e);
			}
		case AZIMUTH_MODEL_UNCERTAINTY:
		{
			double distance = source.getPosition().distanceDegrees(receiver.getPosition());
			if (receiver.getStaType() == StaType.ARRAY)
			{
				if (distance <30) return toRadians(5);
				if (distance < 100) return toRadians(2);
				return toRadians(1);
			}
			if (distance < 30) return toRadians(20);
			if (distance < 100) return toRadians(10);
			return toRadians(5);
		}
		case AZIMUTH_MODEL_UNCERTAINTY_DEGREES:
		{
			double distance = source.getPosition().distanceDegrees(receiver.getPosition());
			if (receiver.getStaType() == StaType.ARRAY)
			{
				if (distance < 30) return 5;
				if (distance < 100) return 2;
				return 1;
			}
			if (distance < 30) return 20;
			if (distance < 100) return 10;
			return 5;
		}
		case SLOWNESS_MODEL_UNCERTAINTY:
			return receiver.getStaType() == StaType.ARRAY ? toDegrees(1.5) : toDegrees(2.5);
		case SLOWNESS_MODEL_UNCERTAINTY_DEGREES:
			return receiver.getStaType() == StaType.ARRAY ? 1.5 : 2.5;
		default:
			throw new GMPException( "attribute is " + attribute.toString() + " but must be one of "
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
		prediction.setAttribute(attribute, getUncertainty(prediction, attribute));
		return prediction.getAttribute(attribute) != Globals.NA_VALUE;
	}

	/**
	 * If this Uncertainty object is supported by lookup tables, then retrieve
	 * the index of the lookup table that supports the given PredictionRequest
	 * and attribute. Returns -1 if the Uncertainty object is not supported by
	 * lookup tables, or if the supplied PredictionRequest is not supported by
	 * any of the lookup tables.
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
	public int getLookupTableIndex(
			PredictionRequestInterface predictionRequest,
			GeoAttributes ttPathCorrection)
	{
		return -1;
	}

	@Override
	public int getLookupTableIndex(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return -1;
	}

	@Override
	public double getUncertainty(int lookupTableIndex, SourceInterface source)
		throws GMPException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex)
		throws IOException
	{
		return "";
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
			LookupTableInterface libcorr3DModelsGMP) throws Exception
	{
		String prefix = "slbm";
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
						preloadModels, interpTypeHorz, interpTypeRadial);
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
		String prefix = "slbm";
		String type = pathCorrections != null ? "hierarchical"
				: "slbm";

		type = properties.getProperty(prefix + "UncertaintyType", type);

		if (type.equalsIgnoreCase("hierarchical"))
			uncertaintyInterface = new UncertaintyHierarchical(
					(UncertaintyInterface) pathCorrections, this);
		else if (type.equalsIgnoreCase("slbm"))
			uncertaintyInterface = this;
		else
			throw new GMPException("\nUnable to set up uncertaintyInterface\n");
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

	@Override
	public PredictorType getPredictorType()
	{
		return PredictorType.SLBM;
	}

	@Override public EnumSet<GeoAttributes> getSupportedAttributes() { return supportedAttributes; }

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases()
	{
		return supportedPhases;
	}

	private double travelTime = Globals.NA_VALUE;
	private double slowness = Globals.NA_VALUE;
	private double dttdlat = Globals.NA_VALUE;
	private double dttdlon = Globals.NA_VALUE;
	private double dttdr = Globals.NA_VALUE;

	@Override
	protected double getTravelTime() throws Exception 
	{ 
		if (travelTime == Globals.NA_VALUE)
			travelTime = slbm.getTravelTime();
		return travelTime;
	}

	@Override
	protected double getSlowness()  throws Exception 
	{ 
		if (slowness == Globals.NA_VALUE)
			slowness = slbm.getSlowness();
		return slowness;
	}

	@Override
	protected double getDttDlat()  throws Exception 
	{ 
		if (dttdlat == Globals.NA_VALUE)
			dttdlat = slbm.get_dtt_dlat();
		return dttdlat;
	}

	@Override
	protected double getDttDlon()  throws Exception 
	{ 
		if (dttdlon == Globals.NA_VALUE)
			dttdlon = slbm.get_dtt_dlon();
		return dttdlon;
	}
	
	@Override
	protected double getDttDr()  throws Exception 
	{ 
		if (dttdr == Globals.NA_VALUE)
			dttdr = -slbm.get_dtt_ddepth();
		return dttdr;
	}

	@Override
	protected double getDshDx()  throws Exception { return Globals.NA_VALUE; }
	
	@Override
	protected double getDshDr()  throws Exception { return Globals.NA_VALUE; }

}
