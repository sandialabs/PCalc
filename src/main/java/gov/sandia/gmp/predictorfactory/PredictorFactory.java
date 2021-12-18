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
package gov.sandia.gmp.predictorfactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.LookupTableInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.infrasoundpredictor.InfrasoundPredictor;
import gov.sandia.gmp.libcorr3dgmp.LibCorr3DModelsGMP;
import gov.sandia.gmp.lookupdz.LookupTablesGMP;
import gov.sandia.gmp.slbmwrapper.SLBMWrapper;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

/**
 * Utility to help manage PredictorInterface objects such as Bender, SLBM, TaupToolkit, 
 * PGL, etc.  An application can construct a PredictorFactory object, passing it a PropertiesPlus
 * object and String indicating the name of the property that specifies the list of 
 * PredictorInterfaces that are to be used.  For example, if property 
 * <BR>myFavoritePredictors = lookup2d, SLBM(Pn, Pg), bender(Pn, Sn)<BR>
 * then lookup2d will be be used for all phases not specified later in the list,
 * SLBM will be used for phase Pg and Bender will be used for phase Pn and Sn.  
 * 
 * <p>PredictorFactory is not thread-safe because it supports collections of PredictorInterface
 * objects which are not thread-safe.
 * 
 * <p>A queue is implemented such that applications can add PredictionRequest objects
 * to the queue using addPredictionRequest(), which stores them by Predictor -> Set<PredictionRequest>
 * where the correct Predictor is chosen based on the SeismicPhase of the PredictionRequest.  Then
 * the application can call computePredictions which will compute the PredictionRequests in 
 * parallel, returning all the Prediction objects in an ArrayList.  Applications 
 * should remember to call clearPredictionRequestQueue() in order to clear the queue
 * after predictions have been retrieved.
 * 
 * @author sballar
 *
 */
public class PredictorFactory 
{

	static public String getVersion() 	{ 
		return Utils.getVersion("predictor-factory");
	}
	
	private EnumSet<PredictorType> supportedPredictors = EnumSet.of(
			PredictorType.BENDER, PredictorType.LOOKUP2D, 
			PredictorType.SLBM, PredictorType.INFRASOUND);

	/**
	 * Map from a SeismicPhase to the appropriate PredictorType object.
	 */
	private LinkedHashMap<SeismicPhase, PredictorType> phaseToPredictorType = 
			new LinkedHashMap<SeismicPhase, PredictorType>();
	
	private LinkedHashMap<PredictorType, PredictorInterface> predictorTypeToPredictorInterface = 
			new LinkedHashMap<>(); 

	/**
	 * A map of File name to LibCorr3DModelsGMP. 
	 */
	private static HashMap<String, LibCorr3DModelsGMP> correctionSurfaces = new HashMap<>();

	/**
	 * A map of File name to GeoTessModel. 
	 */
	private static HashMap<String, GeoTessModel> geotessModels = new HashMap<>();

	private PropertiesPlusGMP properties;
	
	private ScreenWriterOutput logger;

	/**
	 * Map PredictorInterface -> Set of PredictionRequests.  Used to send a whole set of PredictionRequests to 
	 * the appropriate predictor for processing in parallel. 
	 */
	private Map<PredictorInterface, Set<PredictionRequestInterface>> predictionRequestQueue;

	/**
	 * Number of PredictionRequests currently in the queue waiting for prediction calculation.
	 */
	private int predictionRequestQueueSize;
	
	/**
	 * Default PredictorFactory implements the lookup2d predictor
	 * using the default seismicBaseData stored in the project/jar file.
	 * @throws Exception
	 */
	public PredictorFactory() throws Exception
	{
		this.properties = new PropertiesPlusGMP();
		properties.setProperty("predictors = lookup2d");
		this.logger = null;
		parsePredictorMap("predictors");
	}

	/**
	 * Constructor.  Instantiates and configures a set of PredictorInterface objects
	 * for use by the calling application.
	 * @param properties
	 * @param propertyName  The name of the property in supplied properties object that
	 * identifies the list of Predictors that are to be instantiated.  For example, if 
	 * properties contains property with key myFavoritePredictors: 
	 * <BR>myFavoritePredictors = lookup2d, slbm(Pn, Pg), bender(Pn, Sn)<BR>
	 * then lookup2d will be be used for all phases not specified later in the list,
	 * SLBM will be used for phase Pg and Bender will be used for phase Pn and Sn.  
	 * @throws GMPException
	 * @throws IOException 
	 * @throws GeoTessException 
	 */
	public PredictorFactory(PropertiesPlusGMP properties, String propertyName) 
			throws Exception
	{ this(properties, propertyName, null);	}

	/**
	 * Constructor.  Instantiates and configures a set of PredictorInterface objects
	 * for use by the calling application.
	 * @param properties
	 * @param propertyName  The name of the property in supplied properties object that
	 * identifies the list of Predictors that are to be instantiated.  For example, if 
	 * properties contains property with key myFavoritePredictors: 
	 * <BR>myFavoritePredictors = lookup2d, slbm(Pn, Pg), bender(Pn, Sn)<BR>
	 * then lookup2d will be be used for all phases not specified later in the list,
	 * SLBM will be used for phase Pg and Bender will be used for phase Pn and Sn.  
	 * @throws GMPException
	 * @throws IOException 
	 * @throws GeoTessException 
	 */
	public PredictorFactory(PropertiesPlusGMP properties, String propertyName, ScreenWriterOutput logger) 
			throws Exception
	{
		this.properties = properties;
		this.logger = logger;
		parsePredictorMap(propertyName);
	}
	
	/**
	 * Retrieve the PredictorType that is assigned to the specified 
	 * phase.  Can return null is no PredictorType was specified for the 
	 * specified phase.
	 * @param phase
	 * @return
	 */
	public PredictorType getPredictorType(SeismicPhase phase) {
		PredictorType t = phaseToPredictorType.get(phase);
		if (t == null)
			t = phaseToPredictorType.get(SeismicPhase.NULL);
		return t;
	}

	/**
	 * If the phase is one of the supported phases, then check to see 
	 * if the predictor for that phase has already been instantiated.
	 * If it has, then return a reference to it.  If it has not, instantiate
	 * a new instance, store a reference to it internally, and return 
	 * a reference. 
	 * 
	 *  <p>Caution: Many predictors are not thread-safe so only call this
	 *  method on PredictorFactory objects instantiated in a single task
	 *  of a multi-threaded application.
	 * 
	 * <p>If a new instance of a Predictor object is instantiated, and
	 * the Predictor uses a model of some sort, then that model will be
	 * loaded from file if necessary and a copy stored in a static map.  
	 * If the model has been loaded previously,
	 * then the new Predictor that is constructed will include a reference
	 * to the existing model (assumption: models are thread safe!).
	 * @param predictorType
	 * @return
	 * @throws Exception
	 */
	public PredictorInterface getPredictor(SeismicPhase phase) throws Exception {
		return getPredictor(getPredictorType(phase));
	}

	/**
	 * If the specified predictorType is one of the supported types, then check to see 
	 * if the predictor for that type has already been instantiated.
	 * If it has, then return a reference to it.  If it has not, instantiate
	 * a new instance, store a reference to it internally, and return 
	 * a reference. 
	 * 
	 *  <p>Caution: Many predictors are not thread-safe so only call this
	 *  method on PredictorFactory objects instantiated in a single task
	 *  of a multi-threaded application.
	 * 
	 * <p>If a new instance of a Predictor object is instantiated, and
	 * the Predictor uses a model of some sort, then that model will be
	 * loaded from file if necessary and a copy stored in a static map.  
	 * If the model has been loaded previously,
	 * then the new Predictor that is constructed will include a reference
	 * to the existing model (assumption: models are thread safe!).
	 * @param predictorType
	 * @return
	 * @throws Exception
	 */
	public PredictorInterface getPredictor(PredictorType pType) throws Exception {
		if (pType == null) return null;
		PredictorInterface predictor = predictorTypeToPredictorInterface.get(pType);
		if (predictor == null)
			predictorTypeToPredictorInterface.put(pType, predictor = getNewPredictor(pType));
		return predictor;
	}
	
	public boolean isSupported(PredictorType pType)
	{
		return predictorTypeToPredictorInterface.containsKey(pType);
	}
	
	/**
	 * If the predictorType is one of the supported predictorTypes, then 
	 * a new instance of a Predictor object is instantiated.  If
	 * the Predictor uses a model of some sort, then that model will be
	 * loaded from file if necessary and a copy stored in a static map.  
	 * If the model has been loaded previously,
	 * then the new Predictor that is constructed will include a reference
	 * to the existing model (assumption: models are thread safe!).
	 * @param predictorType
	 * @return
	 * @throws GeoTessException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private PredictorInterface getNewPredictor(PredictorType predictorType) throws Exception
	{
		if (predictorType == null) return null;
		
		switch (predictorType)
		{
		case BENDER:
			return new Bender(properties, getGeoTessModel("bender"));
//		case TAUPTOOLKIT:
//			return new TaupToolkitWrapper(properties, getLibCorr("tauptoolkit"));
		case LOOKUP2D:
			return new LookupTablesGMP(properties, getLibCorr("lookup2d"));
		case INFRASOUND:
			return new InfrasoundPredictor(properties);
		case SLBM:
			return new SLBMWrapper(properties, getLibCorr("slbm"));
		case RSTT:
			return new SLBMWrapper(properties, getLibCorr("slbm"));
		default:
			throw new GMPException(predictorType.toString()+" is not a supported PredictorType.");
		}
	}

	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		s.append(String.format("%s%n", this.getClass().getSimpleName()));

		try {
			for (Entry<SeismicPhase, PredictorType> entry : phaseToPredictorType.entrySet())
			{
				String phase = entry.getKey().toString();
				PredictorType predictorType = entry.getValue();
				switch (predictorType)
				{
				case BENDER:
				{
					GeoTessModel model = getGeoTessModel("bender"); 
					s.append(String.format("%-12s bender(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase,
									model == null ? "" : ((GeoTessModel)model).getMetaData().getInputModelFile().getCanonicalPath()));
					break;
				}
//				case TAUPTOOLKIT:
//				{
//					LibCorr3DModelsGMP model = getLibCorr("tauptoolkit");
//					s.append(String.format("%-12s tauptoolkit(%s)%n", 
//							phase.equals("NULL") ? "all phases" : phase,
//									model == null ? "" : ((LookupTableInterface)model).getModelFile(0).getCanonicalPath()));
//					break;
//				}
				case LOOKUP2D:
				{
					LibCorr3DModelsGMP model = getLibCorr("lookup2d");
					s.append(String.format("%-12s lookup2d (%s)%n", 
							phase.equals("NULL") ? "all phases" : phase,
									model == null ? "" : ((LookupTableInterface)model).getModelFile(0).getCanonicalPath()));
					break;
				}
				case INFRASOUND:
					s.append(String.format("%-12s infrasound(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase, ""));
					break;

				case SLBM:
				{
					LibCorr3DModelsGMP model = getLibCorr("slbm");
					s.append(String.format("%-12s slbm(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase,
									model == null ? "" : ((LookupTableInterface)model).getModelFile(0).getCanonicalPath()));
					break;
				}
				default:
					throw new GMPException(predictorType.toString()+" is not a supported PredictorType.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public String getDependencyList()
	{
		StringBuffer s = new StringBuffer();
		s.append(String.format("%s %s%n", this.getClass().getSimpleName(), getVersion()));

		try {
			for (PredictorType predictorType : supportedPredictors)
			{
				switch (predictorType)
				{
				case BENDER:
				{
					s.append(String.format("Bender %s%n", Bender.getVersion()));
					break;
				}
				case LOOKUP2D:
				{
					s.append(String.format("Lookup2D %s%n", LookupTablesGMP.getVersion()));
					break;
				}
				case INFRASOUND:
					s.append(String.format("InfrasoundPredictor %s%n", InfrasoundPredictor.getVersion()));
					break;

				case SLBM:
				{
					s.append(String.format("SLBMWrapper %s%n", SLBMWrapper.getVersion()));
					break;
				}
				default:
					throw new GMPException(predictorType.toString()+" is not a supported PredictorType.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	/**
	 * Retrieve the predictor specification from the properties file and parse it 
	 * into a map of SeismicPhase -> PredictorType.  Some predictors (i.e., Bender)
	 * are able to accept references to thread-safe model objects (GeoTessModel).  
	 * Some predictors cannot.  The supplied ModelInterface object may be null.
	 * @param properties
	 * @param propertyName
	 * @param model 
	 * @throws GMPException
	 * @throws IOException 
	 * @throws GeoTessException 
	 */
	private void parsePredictorMap(String propertyName)
			throws Exception
	{

		String predictorList = properties.getProperty(propertyName);
		if (predictorList == null)
			throw new GMPException(propertyName+" is not a  specified property in the properties file.");

		if (predictorList.indexOf("#") > 0)
			predictorList = predictorList.substring(0, predictorList.indexOf("#"));

		boolean readingPredictor = true;
		boolean readingPhase = false;
		StringBuffer predictorString = new StringBuffer();
		StringBuffer phaseString = new StringBuffer();

		// separate the predictor names from the phase lists.
		for (char ch : predictorList.trim().toCharArray())
		{
			if (readingPredictor)
			{
				if (ch == '(')
				{
					readingPredictor = false;
					readingPhase = true;
				} 
				else if (ch == ',')
				{
					predictorString.append(';');
					phaseString.append(" ; ");
				} 
				else
					predictorString.append(ch);

			} 
			else if (readingPhase)
			{
				if (ch == ')')
				{
					readingPredictor = true;
					readingPhase = false;
				} 
				else
					phaseString.append(ch);
			}
		}

		// parse the list of predictorNames
		String[] predictorNames = predictorString.toString().split(";");
		for (int i = 0; i < predictorNames.length; ++i)
			predictorNames[i] = predictorNames[i].trim();

		//System.out.println("predictorNames = " + Arrays.toString(predictorNames));

		PredictorType[] predictorTypes = new PredictorType[predictorNames.length];
		for (int i=0; i<predictorNames.length; ++i)
		{
			try
			{
				predictorTypes[i] = PredictorType.valueOf(predictorNames[i].trim().toUpperCase());
			}
			catch (java.lang.IllegalArgumentException ex)
			{
				StringBuffer buf = new StringBuffer(predictorNames[i]
						+" is not a recognized PredictorType.  Must be one of");
				for (PredictorType p : PredictorType.values())
					buf.append(' ').append(p.toString().toLowerCase());
				throw new GMPException(buf.toString());
			}
		}

		// parse the list of phaseLists
		String[] phaseLists = phaseString.toString().split(";");
		for (int i = 0; i < phaseLists.length; ++i)
			phaseLists[i] = phaseLists[i].trim();

		//System.out.println("phaseLists    = " + Arrays.toString(phaseLists));

		// populate a map from predictor type -> set of phases
		LinkedHashMap<PredictorType, HashSet<SeismicPhase>> predictorTypeToPhase = 
				new LinkedHashMap<PredictorType, HashSet<SeismicPhase>>();

		for (int i = 0; i < phaseLists.length; ++i)
		{
			HashSet<SeismicPhase> p = new HashSet<SeismicPhase>();
			predictorTypeToPhase.put(predictorTypes[i], p);

			for (String s : phaseLists[i].split(","))
				if (s.trim().length() > 0)
					p.add(SeismicPhase.valueOf(s.trim()));
		}

		for (Map.Entry<PredictorType, HashSet<SeismicPhase>> entry : predictorTypeToPhase.entrySet())
		{
			PredictorType predictorType = entry.getKey();
			HashSet<SeismicPhase> phaseList = entry.getValue();

			// if this predictor had no phase list, set the phaselist to SeismicPhase.NULL
			// and this predictorType can support all phases
			if (phaseList.isEmpty())
				phaseList.add(SeismicPhase.NULL);

			// add entry for SeismicPhase -> PredictorInterface
			// to the main map. Note that a subsequent predictor
			// might replace this entry. Predictor order in the
			// properties file matters.
			for (SeismicPhase phase : phaseList)
				phaseToPredictorType.put(phase, predictorType);

		}
	}
	
	private LibCorr3DModelsGMP getLibCorr(String prefix) throws GMPException, IOException
	{
		LibCorr3DModelsGMP libcorr = null;

		if (properties.getProperty(prefix+"PathCorrectionsType", "-").toLowerCase().startsWith("libcorr"))
		{
			File pathCorrRoot = properties.getFile(prefix+"LibCorrPathCorrectionsRoot");

			if (pathCorrRoot != null)
			{
				libcorr = correctionSurfaces.get(pathCorrRoot.getCanonicalPath());

				if (libcorr == null)
				{
					String relativeGridPath = properties.getProperty(prefix+"LibCorrPathCorrectionsRelativeGridPath", ".");

					boolean preloadModels = properties.getBoolean(prefix+"LibCorrPreloadModels", false);

					int ecnt = 0;
					while (true) {
						try {
							InterpolatorType interpTypeHorz = InterpolatorType
									.valueOf(properties.getProperty(
											prefix + "LibCorrInterpolatorTypeHorizontal",
											"linear").toUpperCase());

							InterpolatorType interpTypeRadial = InterpolatorType
									.valueOf(properties.getProperty(
											prefix + "LibCorrInterpolatorTypeRadial", "linear")
											.toUpperCase());

							libcorr = new LibCorr3DModelsGMP(pathCorrRoot, relativeGridPath, preloadModels, logger, interpTypeHorz, interpTypeRadial);

							correctionSurfaces.put(pathCorrRoot.getCanonicalPath(), libcorr);

							if (logger != null && logger.getVerbosity() > 0)
								logger.writeln("PredictorFactory loaded LibCorr3DModelsGMP " + libcorr.toString());

							break;
						}
						catch (Exception ex) {
							// unsuccessful ... increment count and try again
							++ecnt;
							ex.printStackTrace();
							libcorr = null;
							if (ecnt == 10) {
								throw new GMPException("Failed to load LibCorr3DModelsGMP from " + pathCorrRoot.getCanonicalPath());
							}
							try { Thread.sleep(5000); } // Wait for 5 seconds, before re-try
							catch (InterruptedException e) { }
						}
					}
				}
			}
		}
		return libcorr;
	}

	/**
	 * Retrieve the GeoTessModel that is specified in the properties file with 
	 * property <prefix>Model.  For example, if the properties file specified when
	 * this PredictorFactory was constructed contains a property benderModel = xxx,
	 * then the GeoTessModel that was specified will be returned.  
	 * @param prefix
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	public GeoTessModel getGeoTessModel(String prefix) throws Exception
	{
		GeoTessModel model = null;

		File modelFile = properties.getFile(prefix+"Model");

		if (modelFile != null)
		{
			String modelFileName = modelFile.getCanonicalPath();
			model = geotessModels.get(modelFileName);
			if (model == null)
			{
				// Read in the GeoModel
				int ecnt = 0;
				while (true) {
					try {
						File f = modelFile;

						if (f.isDirectory() && new File(f, "prediction_model.geotess").exists())
							f = new File(f, "prediction_model.geotess");

						// attempt to load model file
						if (f.exists())
							model = GeoTessModel.getGeoTessModel(f);
						else
							throw new IOException(modelFileName + " does not exist or is unreachable");

						File polygonFile = properties.getFile(prefix+"ModelActiveNodePolygon");
						if (polygonFile != null)
							model.setActiveRegion(polygonFile);

						geotessModels.put(modelFileName, model);

						if (logger != null && logger.getVerbosity() > 0)
							logger.writef("PredictorFactory loaded GeoTessModelSiteData %s%s%n", modelFileName,
									(polygonFile == null ? "" : "\n  with polygon "+polygonFile.getCanonicalPath()));

						break;
					}
					catch (GMPException ex) {
						// unsuccessful ... increment count and try again
						ex.printStackTrace();
						++ecnt;
						if (ecnt == 10) {
							throw new GMPException("Failed to read in benderModel at " + modelFileName
							    +"\n"+ex.getMessage());
						}
						try { Thread.sleep(5000); } // Wait for 5 seconds, before re-try
						catch (InterruptedException e) { }
					}
				}				
			}
		}
		return model;
	}

	/**
	 * Retrieve a Prediction for the specified PredictionRequest.
	 * @param request
	 * @return Prediction for the specied request.
	 * @throws Exception only in the event that a Predictor constructor throws an exception. 
	 * If a Predictor throws an exception while trying to compute a prediction, or if this
	 * PredictorFactory does not support the specified request, then a new, invalid Prediction
	 * will be returned (see Prediction.getErrorMessage() to determine what happened).
	 */
	public PredictionInterface getPrediction(PredictionRequestInterface request) throws Exception {
		PredictorInterface predictor = getPredictor(request.getPhase());
		if (predictor != null)
		{
			try 
			{
				return predictor.getPrediction(request);
			} 
			catch (Exception e) 
			{
				return new Prediction(request, predictor, e);
			}
		}
		return new Prediction(request, null, 
				new Exception("PredictorFactory does not support predictions for phase "+request.getPhase().toString()));
	}

	/**
	 * Clear the queue of PredictionRequests that are awaiting calculation.
	 */
	public void clearPredictionRequestQueue()
	{
		if (predictionRequestQueue != null)
			predictionRequestQueue.clear();
		predictionRequestQueueSize = 0;
	}

	/**
	 * Add a Collection of PredictionRequests to the queue of such requests that are awaiting processing.  The 
	 * requests are stored in a map of PredictorInterface -> Set of PredictionRequests where the
	 * PredictorInterface is selected based on the SeismicPhase of the supplied PredictionRequest.
	 * Unsupported predictionRequests are ignored.
	 * @param predictionRequest
	 * @return number of predictionRequests that are supported by a PredictorInterface.
	 * @throws Exception 
	 */
	public int addPredictionRequests(Collection< ? extends PredictionRequestInterface> predictionRequests) throws Exception
	{
		int nSupported = 0;
		for (PredictionRequestInterface request : predictionRequests)
			if (addPredictionRequest(request))
				++nSupported;
		return nSupported;
	}

	/**
	 * Add a PredictionRequest to the queue of such requests that are awaiting processing.  The 
	 * requests are stored in a map of PredictorInterface -> Set of PredictionRequests where the
	 * PredictorInterface is selected based on the SeismicPhase of the supplied PredictionRequest.
	 * 
	 * @param predictionRequest
	 * @return true if the PredictionRequest is supported by a PredictorInterface, false otherwise.
	 * Unsupported PredictionRequests are ignored.
	 * @throws Exception 
	 */
	public boolean addPredictionRequest(PredictionRequestInterface predictionRequest) throws Exception
	{
		if (predictionRequestQueue == null)
			predictionRequestQueue = new HashMap<PredictorInterface, Set<PredictionRequestInterface>>();

		PredictorInterface predictor = getPredictor(predictionRequest.getPhase());
		if (predictor != null)
		{
			Set<PredictionRequestInterface> requestSet = predictionRequestQueue.get(predictor);
			if (requestSet == null)
				predictionRequestQueue.put(predictor, 
						requestSet = new LinkedHashSet<PredictionRequestInterface>());

			requestSet.add(predictionRequest);
			++predictionRequestQueueSize;
			return true;
		}
		return false;
	}
	
	/**
	 * Compute Predictions for all the PredictionRequests that are currently waiting for calculation
	 * and clear the PredictionRequestQueue.
	 * <p>
	 * This is a synonym for computePredictions(parallelMode).
	 * @param parallelMode 
	 * @return ArrayList<PredictionInterface>
	 * @throws GMPException
	 * @throws GeoTessException 
	 * @throws IOException 
	 */
	public ArrayList<PredictionInterface> getPredictions(boolean parallelMode) throws Exception
	{
		return computePredictions(parallelMode);
	}

	/**
	 * Compute Predictions for all the PredictionRequests that are currently waiting for calculation
	 * and clear the PredictionRequestQueue
	 * <p>
	 * This is a synonym for getPredictions(parallelMode).
	 * @param parallelMode 
	 * @return ArrayList<PredictionInterface>
	 * @throws GMPException
	 * @throws GeoTessException 
	 * @throws IOException 
	 */
	public ArrayList<PredictionInterface> computePredictions(boolean parallelMode) throws Exception
	{
		ArrayList<PredictionInterface> predictions = new ArrayList<PredictionInterface>(predictionRequestQueueSize);

		if (predictionRequestQueue != null)
		{
			// iterate through the map Predictor -> Set of Arrivals
			for (Entry<PredictorInterface, Set<PredictionRequestInterface>> entry : predictionRequestQueue.entrySet())
			{
				// identify the predictor
				PredictorInterface predictor = entry.getKey();

				// find the subSet of Arrivals mapped to that predictor
				Set<PredictionRequestInterface> arrivalSubset = entry.getValue();

				if (predictor != null && arrivalSubset != null)
				{
					// send all the Arrivals to the Predictor and get back a Collection of
					// results.  The predictor may be able to compute predictions in parallel.
					if (parallelMode)
						for (PredictionInterface prediction : predictor.getPredictions(arrivalSubset))
							predictions.add(prediction);
					else
						for (PredictionRequestInterface request : arrivalSubset)
							predictions.add(predictor.getPrediction(request));
				}
			}
			clearPredictionRequestQueue();
		}
		return predictions;
	}

	/**
	 * @return the number of PredictionRequestInterface objects currently in the queue.
	 */
	public int getPredictionRequestQueueSize()
	{
		return predictionRequestQueueSize;
	}

	/**
	 * Retrieve a Map from directory name to LibCorr3DModelsGMP.
	 * @return
	 */
	public static HashMap<String, LibCorr3DModelsGMP> getCorrectionSurfaces() {
		return correctionSurfaces;
	}

}
