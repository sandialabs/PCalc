package gov.sandia.gmp.observationprediction;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.bender.BenderConstants.SearchMethod;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.polygon.Polygon3D;
import gov.sandia.gmp.util.profiler.Profiler;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * A parallel distributed task that runs in a distributed fashion using
 * the JPPF system by calling the run() function on a distributed node.
 *
 * <p> The job of this object is to execute a list of PredictorObservation
 * (aPredObs) using the Bender predictor. The bender GeoModel is stored as
 * a static object and is recreated if the current model file name is
 * different than the instantiated file name which is saved in the static
 * string aModelFilePath.
 *
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class PredictorParallelTask extends ParallelTask
{

  /**
   * The current static instantiated tomography GeoModel. The object is
   * initialized to null to force construction (if requested) on the first entry.
   */
  protected static GeoTessModel   aTomoModel         = null;

  /**
   * The current static instantiated tomography GeoTessModel file name.
   */
  private static String    aTomoModelFilePath         = "";

  /**
   * Simple static id counter that is auto-incremented with each
   * constructor call and set into the JPPFTask as its task id.
   */
  private static int       aId                    = -1;

  /**
   * Current version string.
   */
  //private static String   aVersion                = "4.1.0";
  
  /**
   * The list of predictor observation objects to be processed by the
   * run() method.
   */
  private ArrayList<PredictorObservation> aPredObs = null;

  /**
   * The GeoModel file path from which the GeoModel used by the Bender
   * is created.
   */
  private String          predModelFilePath           = null;

  /**
   * The GeoModel file path from which the GeoModel used by the Bender
   * is created.
   */
  private String          tomoModelFilePath           = null;

  /**
   * The Polygon file path from which the active node polygon used by
   * the GeoModel is created.
   */
  private String          polygonFilePath         = null;
  
  /**
   * Simple static debug flag.
   */
  private static boolean  aDebug                  = false;

  /**
   * Simple static screen output flag.
   */
  private static boolean  aOutput                 = true;

  /**
   * The initial submission time of this task. This time is carried through
   * the process until returned to the client where it is used to determine
   * the total process and parallel transfer time of the task.
   */
  private long            aTaskSubmitTime         = 0;

  /**
   * The profiler sample period (milliseconds). If less than 1 it is not
   * used.
   */
  private long            aProfilerSamplePeriod   = -1;

  /**
   * The profiler verbose flag. Outputs the profile here at the node if true.
   */
  private boolean         aProfilerNodeVerbose    = false;
  
  /**
   * Read GeoModel fail limit.
   */
  private transient static int      aReadModelFailLimit = 10;
 
  private PropertiesPlusGMP        aProperties = null;
  
  /**
   * Standard constructor that sets the GeoModel file name, GeoModel
   * active region polygon, and the list of BenderObservation objects
   * to be executed by the run() method.
   *
   * @param modelFilePath GeoModel file name.
   * @param modelFileFormat GeoModel file format specifier. (ignored)
   * @param polygonFilePath GeoModel active region polygon.
   * @param actvNodeAttributes Array of active node GeoAttributes (typically)
   *                           PSLOWNESS, SSLOWNESS, or both) to be used to
   *                           define the active node setting.
   * @param benderObs List of BenderObservation objects to be executed
   *                  by the run() method.
   */
  public PredictorParallelTask(String predModelFilePath, String tomoModelFilePath,
                               String polygonFilePath,
                               ArrayList<PredictorObservation> benderObs)
  {
    this.aPredObs = new ArrayList<PredictorObservation>(benderObs);
    this.predModelFilePath = predModelFilePath;
    this.tomoModelFilePath = tomoModelFilePath;
    this.polygonFilePath = polygonFilePath;

    // set JPPFTask id
    ++aId;
    setId(Integer.toString(aId));
  }

  
  /**
   * Returns the current code version.
   * 
   * @return The current code version.
   */
  public static String getVersion()
  {
  	return ObservationPrediction.getVersion();
  }


  /**
   * Sets the profilers sample period (milliseconds).
   * 
   * @param psp The profilers sample period (milliseconds).
   */
  public void setProfiler(long psp, boolean psv)
  {
    aProfilerSamplePeriod = psp;
    aProfilerNodeVerbose  = psv;
  }

  /**
   * Sets a default predictor properties object.
   * 
   * @param properties The default predictor properties object.
   */
  public void setDefaultPredictorProperties(PropertiesPlusGMP predProperties)
  {
  	aProperties = predProperties;
  }

  /**
   * The function that processes all PredictorObservation objects stored in
   * aPredObs when called. Each PredictorObservation is used as an input
   * into the Bender ray calculation method to compute a RayInfo result
   * object for output. The results are stored as a set of PredictorResult
   * objects in a single PredictorParallelTaskResult that matches one-for-one
   * this PredictorParallelTask. Each PredictorResult contains the computed
   * RayInfo object and the index of the PredictorObservation object.
   *
   * <p> The run() method is called by the JPPF distributed system to be
   * executed on a network node.
   */
  @Override
  public void run()
  {
    // convert file paths from Windows to Linux if necessary
	  
//    aPredModelFilePath  = PropertiesPlus.convertWinFilePathToLinux(aPredModelFilePath);
    aTomoModelFilePath  = PropertiesPlus.convertWinFilePathToLinux(aTomoModelFilePath);
    predModelFilePath   = PropertiesPlus.convertWinFilePathToLinux(predModelFilePath);
    tomoModelFilePath   = PropertiesPlus.convertWinFilePathToLinux(tomoModelFilePath);
    polygonFilePath     = PropertiesPlus.convertWinFilePathToLinux(polygonFilePath);

    // create and initialize the task result

    PredictorParallelTaskResult results;
    results = new PredictorParallelTaskResult(aPredObs.size(),
                                              predModelFilePath,
                                              polygonFilePath);
    results.setTaskSubmitTime(getSubmitTime());
    results.setIndex(getIndex());
    setResult(results);

    // create temporary definitions and objects

    PredictorFactory predictorFactory = null; 
  	ArrayList<double[]> rayPathUnitVec  = new ArrayList<double[]>();
  	ArrayListDouble     rayPathRadii    = new ArrayListDouble();
    HashMapIntegerDouble weights = new HashMapIntegerDouble();
    Profiler profiler = null;

    // enter error catch code

    try
    {
    	// get and save host name

      String hostname = (InetAddress.getLocalHost()).getHostName();
      results.setHostName(hostname);
      outputTaskInfo(results.getHostName(), "Entry", "");

      // create profiler if requested

      if (aProfilerSamplePeriod > 0)
      {
        profiler = new Profiler(Thread.currentThread(), aProfilerSamplePeriod,
                                "PredictorParallelTask:" + hostname);
        profiler.setTopClass("gov.sandia.gmp.observationprediction.PredictorParallelTask");
        profiler.setTopMethod("run");
        profiler.accumulateOn();
      }

      // Make a default properties file if it does not exist or doesn not
      // define a predictor.

      long time0  = (new Date()).getTime();
      if (aProperties == null) aProperties = new PropertiesPlusGMP();
    	if (aProperties.getProperty("predictors") == null)
        aProperties.put("predictors", "Bender");
    	if (aProperties.get("predictors").equals("Bender"))
    	{
        aProperties.put("benderModel", predModelFilePath);
        if ((polygonFilePath != null) && !polygonFilePath.equals("")) 
          aProperties.put("benderModelActiveNodePolygon", polygonFilePath);      		
    	}

      // create the predictor factory ... add the prediction requests ... get
      // the predictions

      predictorFactory = new PredictorFactory(aProperties, "predictors");
      predictorFactory.addPredictionRequests(aPredObs);
      ArrayList<PredictionInterface> predictions = predictorFactory.getPredictions(false);
      
       // done with predictions ... now get the prediction model and create the
      // tomography model for producing node weights if requested.

      GeoTessModel predModel = ((GeoTessModel) predictorFactory.getPredictor(PredictorType.BENDER).getEarthModel());
      predModelFilePath = predModel.getCurrentModelFileName();
      createTomographyGeoTessModel(predModel);

      // loop over all predictions and populate the prediction result list and
      // build the ray weights for each prediction

      for (int i = 0; i < predictions.size(); i++)
      {
    	// add ray to list and continue
      	PredictionInterface  pi = predictions.get(i);  
      	PredictorObservation po = (PredictorObservation)pi.getPredictionRequest();
    
      	if (po != null) {
      		PredictorResult pr = new PredictorResult(pi, po.getObservationIndex(),
                                                 true);
      		buildTomographyModelWeights(pi, rayPathUnitVec, rayPathRadii, weights,
        		                        pr, po);
      		results.addRay(pr);
      	}
      }

      // done ... set list of rays into results and the calculation time
      // and host processor name ... exit

      long boid = aPredObs.get(0).getObservationId();
      if (aOutput) System.out.println("Finished Bender Compute Ray " +
                                      "Calculation (Group Index = " + boid +
                                      ") ...");
      results.setCalculationTime(time0);

      if (aDebug) results.appendMessage("Execution complete .... Done");
      
      // turn off profiler if on and set into results

      if (profiler != null)
      {
        profiler.stop();
        if (aProfilerNodeVerbose) profiler.printAccumulationString();
        results.setProfilerContent(profiler.getProfilerContent());
        profiler = null;
      }      
      outputTaskInfo(results.getHostName(), "Exit", "");
    }
    catch (UnsupportedOperationException ex)
    {
      // likely GeoModel or Bender creation error

      if (profiler != null)
      {
        profiler.stop();
        if (aProfilerNodeVerbose) profiler.printAccumulationString();
        profiler = null;
      }      

      if (aOutput)
        System.out.println("BenderTaskBundle::catch " +
                           "(UnsupportedOperationException ex) ");
      ex.printStackTrace();
      results.setException(ex);
      if (aDebug) results.appendMessage("Exception Occurred ...");
      outputTaskInfo(results.getHostName(), "Error", "");
    }
    catch (Exception ex)
    {
      // likely GeoModel or Bender creation error

      if (profiler != null)
      {
        profiler.stop();
        if (aProfilerNodeVerbose) profiler.printAccumulationString();
        profiler = null;
      }      

      if (aOutput)
        System.out.println("BenderTaskBundle::catch (Exception ex) ");
      ex.printStackTrace();
      results.setException(ex);
      if (aDebug) results.appendMessage("Exception Occurred ...");
      outputTaskInfo(results.getHostName(), "Error", "");
    }
  }
//
//    // set predictor/model identifier and create a new result bundle
//
//    PredictorParallelTaskResult results;
//    results = new PredictorParallelTaskResult(aPredObs.size(),
//                                              predModelFilePath, polygonFilePath);
//    results.setTaskSubmitTime(getSubmitTime());
//    results.setIndex(getIndex());
//    setResult(results);
//
////    HashMapIntegerDouble weights = new HashMapIntegerDouble();
//
//    try
//    {
//      String hostname = (InetAddress.getLocalHost()).getHostName();
//      results.setHostName(hostname);
//      outputTaskInfo(results.getHostName(), "Entry", "");
//
//      // see if a new bender needs to be instantiated
//      synchronized(PredictorParallelTaskResult.class)
//      {
//        if ((aModel == null) || !aPredModelFilePath.equals(predModelFilePath))
//        {
//          // create a new bender and save in static variable. Only create if
//          // bender does not yet exist or if the iteration count has changed
//          // first set directory path
//
//          // create GeoModel, set active Polygon, and create Bender object
//          if (aDebug)
//          {
//            results.appendMessage("Creating a new bender with id:");
//            results.appendMessage("   " + id);
//          }
//          
//          // Read in the GeoModel
//          System.out.println("before reading geomodel");
//          aModel     = readGeoModel(predModelFilePath);
//          if (!tomoModelFilePath.equals(""))
//            aTomoModel = readGeoModel(tomoModelFilePath);
//
//          Polygon3D polygon = null;
//          if ((polygonFilePath != null) && (polygonFilePath.length() > 0))
//          {
//          	File f = new File(polygonFilePath);
//            polygon = new Polygon3D(f);
//            aModel.setActiveRegion(polygon);
//            if (aTomoModel != null)
//              aTomoModel.setActiveRegion(polygon);
//          }
//          else
//          {
//            aModel.setActiveRegion();
//            if (aTomoModel != null)
//              aTomoModel.setActiveRegion();
//          }
//
//          if (aOutput)
//          {
//            System.out.println("");
//            System.out.println("Created Prediction GeoModel ...");
//            System.out.println(aModel.getMetaData().getInputModelFile().getCanonicalPath());
//            System.out.println("");
//            if (aTomoModel != null) {
//            	System.out.println("Created Tomography GeoModel ...");
//            	System.out.println(aTomoModel.getMetaData().getInputModelFile().getCanonicalPath());
//            }
//          }
//          if (aDebug) results.appendMessage("Created new bender ...");
//
//          // save configuration string and set verbosity
//
//          aPredModelFilePath = predModelFilePath;
//        }
//      }
//
//      // create profiler if requested
//
//      if (aProfilerSamplePeriod > 0)
//      {
//        profiler = new Profiler(Thread.currentThread(), aProfilerSamplePeriod,
//                                "PredictorParallelTask:" + hostname);
//        profiler.setTopClass("gov.sandia.gmp.observationprediction.PredictorParallelTask");
//        profiler.setTopMethod("run");
//        profiler.accumulateOn();
//      }
//
//      // create bender and initialize output
//
//      bender = new Bender(aModel);
//      bender.setVerbosity(0);
//      bender.setMaxCalcTime(aMaxAllowedRayCalcTime);
//      bender.setSearchMethod(aBenderSearchMethod);
//      bender.setGradientCalculator(GradientCalculationMode.PRECOMPUTED, tetSize);
//      bender.setUseTTSiteCorrections(useSiteTermCorrections);
//      bender.setAllowCMBDiffraction(allowCMBDiffraction);
//      long boid = aPredObs.get(0).getObservationId();
//      if (aOutput) System.out.println("Beginning Bender Compute Ray " +
//                                      "Calculation (Group Index = " + boid +
//                                      ") ...");
////    	ArrayList<double[]> rayPathUnitVec  = new ArrayList<double[]>();
////    	ArrayListDouble     rayPathRadii    = new ArrayListDouble();
//
//      // set start time and loop over all executable bender objects
//
//      long time0  = (new Date()).getTime();
//      long timeChk0 = System.nanoTime();
//      long timeChk1 = 0;
//      long timeChkLimit = 500000000L * aPredObs.size();
//      if (aDebug) results.appendMessage("Calculating " + aPredObs.size() +
//                                        " ray paths ...");
//      for (int i = 0; i < aPredObs.size(); i++)
//      {
//        // get the ith execution object and set the source, receiver, and phase
//
//        PredictorObservation bo = aPredObs.get(i);
//
//        // calculate the ray if defining
//
//        RayInfo ray = null;
//        boolean computedRay = true;
//        if (!bo.isDefining())
//        {
//          // non-defining ray ... set dummy rayinfo object
//
//          computedRay = false;
//          ray = new RayInfo(bo, bender, "observation is non-defining");
//          if (aDebug) results.appendMessage("Ray is non-defininig ...");
//        }
//        else
//        {
//          try
//          {
//            // compute ray
//
//            ray = bender.computeFastRays(bo)[0];
// 
//
//            // check run time for this bundle ... if too long then output
//            // a message indicating progress thus far
//
//            timeChk1 = System.nanoTime();
//            if (aOutput &&
//                (timeChk1 - timeChk0 > timeChkLimit))
//            {
//              timeChk0 = timeChk1;
//              System.out.println("  Processing index <group, obs> = <" +
//                                 boid + ", " + i + "> of " +
//                                 aPredObs.size() + " observations ...");
//            }
//          }
//          catch (Exception ex)
//          {
//            // execution error ... set dummy rayinfo object and save error
//            // message into it
//
//            if (aDebug) results.appendMessage("Exception Occurred ...");
//            computedRay = false;
//            ray = new RayInfo(bo, bender, getStackTraceString(ex));
//            System.out.println("  Bender.ComputeFastRays(b0) error ..." + NL +
//                               "    Ray Index: " + bo.getObservationIndex() + NL +
//                               "    " + getStackTraceString(ex));
//          }
//        }
//
//        // add ray to list and continue
//
//        PredictorResult br = new PredictorResult(ray, bo.getObservationIndex(),
//                                                 computedRay);
//        
//        if (aTomoModel != null)
//        {
//          // convert prediction ray path to tomography grid weights if it is
//          // a valid ray
//
//          if (ray.getRayPath() != null)
//          {
//            // valid ray ... get tomography grid weights
//          	ray.getRayPath(rayPathUnitVec, rayPathRadii);
//          	aTomoModel.getWeights(rayPathUnitVec, rayPathRadii, null,
//          			                  InterpolatorType.LINEAR,
//          			                  InterpolatorType.LINEAR, weights);
////            aTomoModel.getWeights(ray.getWaveType(), ray.getRayPath(),
////                                  weights);
//            // loop over all entries and put them in an array ... set the index and
//            // weight arrays into the predictor result
//
//            HashMapIntegerDouble.Iterator it = weights.iterator();
//            int[]    indxs = new int [weights.size()];
//            double[] wghts = new double [weights.size()];
//            int j = 0;
//            while (it.hasNext())
//            {
//              HashMapIntegerDouble.Entry e = it.nextEntry();
//              indxs[j] = e.getKey();
//              wghts[j] = e.getValue();
//              ++j;
//            }
//            br.setWeights(indxs, wghts);
//
////            // ******** test code
////            double[] rweights = ray.getActiveNodeWeights();
////            int[]    rindxs   = ray.getActiveNodeIndexes();
////            HashMapIntegerDouble.Entry e;
////            if (Math.abs(rweights.length - weights.size()) > 1)
////            {
////              System.out.println("Input weights size (" + rweights.length +
////                                 ") is not equal to output size (" +
////                                 weights.size() + ") ...");
////            }
////            for (int k = 0; k < rweights.length; ++k)
////            {
////              e = weights.getEntry(rindxs[k]);
////              if (e == null)
////              {
////                System.out.println("Can't find index = " + rindxs[k] +
////                                   " in weights ...");
////                synchronized (this)
////                {
////                  ++aMissingWeights;
////                  if (rweights[k] > 1.0e-2)
////                  {
////                    System.out.println("Missing large Weight found (" +
////                                       rweights[k]);
////                    ++aLargeMissingWeights;
////                  }
////                }
////              }
////            }
////            // ******** test code
//
//            // if RAY_PATH was not requested nullify it and set result into the
//            // result array
//
//            if (!bo.returnRayPath()) ray.nullifyRayPath();
//            ray.nullifyActiveNodeWeights();
//          }
//        }
//        results.addRay(br);
//      }
//
//      // done ... set list of rays into results and the calculation time
//      // and host processor name ... exit
//
//      if (aOutput) System.out.println("Finished Bender Compute Ray " +
//                                      "Calculation (Group Index = " + boid +
//                                      ") ...");
//      results.setCalculationTime(time0);
//
//      if (aDebug) results.appendMessage("Execution complete .... Done");
//      
//      // turn off profiler if on and set into results
//
//      if (profiler != null)
//      {
//        profiler.stop();
//        if (aProfilerNodeVerbose) profiler.printAccumulationString();
//        results.setProfilerContent(profiler.getProfilerContent());
//        profiler = null;
//      }      
//      outputTaskInfo(results.getHostName(), "Exit", "");
//    }
//    catch (UnsupportedOperationException ex)
//    {
//      // likely GeoModel or Bender creation error
//
//      if (profiler != null)
//      {
//        profiler.stop();
//        if (aProfilerNodeVerbose) profiler.printAccumulationString();
//        profiler = null;
//      }      
//
//      if (aOutput)
//        System.out.println("BenderTaskBundle::catch " +
//                           "(UnsupportedOperationException ex) ");
//      ex.printStackTrace();
//      results.setException(ex);
//      if (aDebug) results.appendMessage("Exception Occurred ...");
//      outputTaskInfo(results.getHostName(), "Error", "");
//    }
//    catch (Exception ex)
//    {
//      // likely GeoModel or Bender creation error
//
//      if (profiler != null)
//      {
//        profiler.stop();
//        if (aProfilerNodeVerbose) profiler.printAccumulationString();
//        profiler = null;
//      }      
//
//      if (aOutput)
//        System.out.println("BenderTaskBundle::catch (Exception ex) ");
//      ex.printStackTrace();
//      results.setException(ex);
//      if (aDebug) results.appendMessage("Exception Occurred ...");
//      outputTaskInfo(results.getHostName(), "Error", "");
//    }
//  }

  /**
   * Reads the GeoTessModel at the input file path. The method continues to
   * attempt to read the model should a timeout occur (up to aReadModelFailLimit
   * attempts before throwing an error).
   * 
   * @param modelPath The model to be read.
   * @return The loaded GeoTessModel.
   */
  private GeoTessModel readGeoModel(String modelPath)
  {
    GeoTessModel mdl;
    
    int ecnt = 0;
    while (true)
    {
      try
      {
        mdl = new GeoTessModel(modelPath);
        return mdl;
      }
      catch (Exception ex)
      {
        // unsuccessful ... increment count and try again
        ++ecnt;
        if (ecnt == aReadModelFailLimit) {
          System.exit(-1);
        }
          
        // Wait for 5 seconds
        try 
        {
          Thread.sleep(5000);
        }
        catch (InterruptedException e) { }
      }
    }
  }

  /**
   * Outputs task information as an event that can be picked up by task
   * listeners. The task id, host name and time are always output. The
   * optional tag and message are added to the string to identify the
   * pertinent information. The information is only sent if aOutput is
   * true.
   * 
   * @param hostname Hostname of the machine executing the task.
   * @param tag Information tag (e.g. "Entry" or "Exit").
   * @param msg Pertinent information to be conveyed by the message.
   */
  private void outputTaskInfo(String hostname, String tag, String msg)
  {
    if (aOutput)
    {
      // output a finalization string and send it across to the parent
      // application to notify of task completion

      String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
      String s = "Task (id = " + getId() + ") " + tag + 
                 ", Host Name: " + hostname +
                 ", Time: " + formatter.format(cal.getTime()) + NL;
      if (!msg.equals("")) s += msg + NL;
      System.out.println(s);
      //fireNotification(new JPPFTaskEvent(s));
    }
  }

  /**
   * Sets the Model file path.
   *
   * @param ic The Bender / Model id tag.
   */
  public void setBenderModelFilePath(String predModelFilePath)
  {
    this.predModelFilePath = predModelFilePath;
  }

  /**
   * Returns the list of PredictorObservation objects.
   *
   * @return The list of PredictorObservation objects.
   */
  public ArrayList<PredictorObservation>  getPredictorObservations()
  {
    return aPredObs;
  }

  /**
   * Set the debug output flag to dbg.
   */
  public void setDebug(boolean dbg)
  {
    aDebug = dbg;
  }

  /**
   * Set the screen output flag to out.
   */
  public void setOutput(boolean out)
  {
    aOutput = out;
  }

  /**
   * Sets/Resets a flag to perform a calculation for the change in
   * travel time wrt. slowness for each grid point that influences
   * the ray path. The flag is set if the input flag is true and
   * reset otherwise.
   *  
   * @param aEvalTomoDerivatives
   */
  public void setDerivativeEvaluation(boolean aEvalTomoDerivatives)
  {
    if (aEvalTomoDerivatives)
    {
      for (PredictorObservation obs: aPredObs)
        obs.getRequestedAttributes().add(GeoAttributes.DTT_DSLOW);
    }
    else
    {
      for (PredictorObservation obs: aPredObs)
        obs.getRequestedAttributes().remove(GeoAttributes.DTT_DSLOW);
    }
  }
  
  /**
  * Simple utilities to return the stack trace of an
  * exception as a String.
  */
  public static String getStackTraceString(Throwable aThrowable)
  {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
  

  /**
   * Constructs a list of parallel tasks from the input list of observations.
   *
   * @param obs List of all observations from which parallel tasks will be
   *            constructed.
   * @param numPredPerTask Approximate number of predictions per task.
   * @return List of all parallel prediction tasks.
   */
  public static ArrayList<PredictorParallelTask>
         buildPredictorParallelTasks(ObservationList obs,
                                     String tomoModelPath,
                                     int numPredPerTask,
                                     SearchMethod bsm,
                                     PropertiesPlusGMP predictorProps,
                                     boolean saveRayPaths)
  {
    PredictorParallelTask bob;
    
    // set up predictor observation and predictor parallel task lists

    ArrayList<PredictorObservation> predObs =
                                 new ArrayList<PredictorObservation> ();
    ArrayList<PredictorParallelTask> predPTask =
                                 new ArrayList<PredictorParallelTask> ();

    // build path information for each bundle

    String modelFilePath = "", polyFilePath = "";
    try
    {
      GeoTessModel geoModel = obs.getGeoModel();
      modelFilePath = geoModel.getCurrentModelFileName();
      File polygonFile = geoModel.getActiveRegionPolygonFile();
      if (polygonFile == null)
        polyFilePath = "";
      else
        polyFilePath = polygonFile.getCanonicalPath();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    // set the last np tasks (approximately) to decrease several observation per
    // task such that the last task has at most nrmin observations (Note: this is
    // a performance tweak so that the processors are not lying around while the
    // last tasks finish).

    int np = 200;
    int nrmin = 10;
    int nr = 0;
    for (int i = 0; i < np; ++i)
      nr += numPredPerTask - (numPredPerTask - nrmin) * i / (np - 1);
    int Nt = (obs.size() - nr) / numPredPerTask;
    int n = numPredPerTask;

    // loop over all observations and fill the task bundles

    int tskcnt = 0;
    int obscnt = 0;
    for (ObservationTomo ob : obs)
    {
      // add BenderObservations to current list of PredictorObservations until
    	// full then create a new PredictorParallelTask. Note: Only add observations
    	// that are valid for tomography and not marked for skipping. If the
    	// save ray path flag (saveRayPaths) is set then add them even if they are
    	// marked for skipping since ray path information will be required for
    	// output at the end of the current iteration.

    	if (ob.getStatus().isValidForTomography() &&
    			(saveRayPaths || !ob.isSkipRayTrace()))
//      if ((!ob.getStatus().ignorePrediction() ||
//          (ob.getStatus() == ObservationStatus.NO_STATUS) ||
//          ob.usePoorlyRepresentedInTomography()) &&
//          !ob.isSkipRayTrace())
      {
      	// add a new request (request has bounce point fixed if
      	// ob.isSkipBouncePointOptimization() is true

    		try
    		{
          predObs.add(ob.getPredictorObservation(saveRayPaths));
    		}
        catch (Exception ex)
        {
          ex.printStackTrace();
        }

        // if hit max or end, create a new bundle and add to list

        if ((predObs.size() == n) || (obscnt == obs.size() - 1))
        {
          // create a new bundle and add it to the list
          bob = new PredictorParallelTask(modelFilePath, tomoModelPath,
                                          polyFilePath, predObs);
          bob.setDefaultPredictorProperties(predictorProps);

//          if (bsm != null) bob.setBenderSearchMethod(bsm);

          predPTask.add(bob);

          // clear list of bender observations for the next bundle

          predObs.clear();

          // increment the task count and see if we are at the last np (or so)
          // tasks where the observation count will decrease linearly down to
          // nrmin by the last task

          ++tskcnt;
          if (tskcnt > Nt)
          {
            n = numPredPerTask -
                (numPredPerTask - nrmin) * (tskcnt - Nt - 1) / (np - 1);
            if (n < nrmin) n = nrmin;
          }
        }

        // increment the observation count and continue

        ++obscnt;

      } // end if (!ob.getStatus().ignorePrediction())
    } // end for (ObservationTomo ob : obs)

    return predPTask;
  }

  /**
   * Sets the task submission time (called only by the client).
   * 
   * @param tsksbmttime The task submission time.
   */
  public void setTaskSubmitTime(long tsksbmttime)
  {
    aTaskSubmitTime = tsksbmttime;
  }

  /**
   * Sets the task submission time (called only by the client).
   * 
   * @param tsksbmttime The task submission time.
   */
  public long getTaskSubmitTime()
  {
    return aTaskSubmitTime;
  }

  /**
   * Formulates a tomography GeoTessModel give one of 3 outcomes.
   *   1) tomoModelFilePath is not defined (i.e. = ""), in which case a
   *      tomography model is not produced. This corresponds to standard
   *      prediction scenarios where tomography weight calculations are not
   *      produced.
   *   2) tomoModelFilePath is defined and is equal to the prediction model
   *      (i.e. tomoModelFilePath = predModelFilePath), in which case the
   *      input prediction model is used as the tomography model to evaluate
   *      tomography weights which are returned to the caller.
   *   3) tomoModelFilePath is defined but does not reference the model file
   *      specified by the prediction model. In this case a new tomography
   *      model and polygon are constructed and the model file active region is
   *      set to the polygon specification. This tomography model is used to
   *      calculate tomography weights using ray paths that were calculated from
   *      the prediction model.
   *      
   * @param predModel The prediction model used by the predictor factory.
   * @throws IOException
   */
  private void createTomographyGeoTessModel(GeoTessModel predModel) throws IOException
  {
    synchronized(PredictorParallelTaskResult.class)
    {
      if (!tomoModelFilePath.equals(""))
      {
        if ((aTomoModel == null) || !aTomoModelFilePath.equals(tomoModelFilePath))
        {
        	// check to see if the tomography model file path and the prediction
        	// model file path are the same ... if they are then get the
        	// tomography model from the predictor factory

        	if (predModelFilePath.equals(tomoModelFilePath))
        		aTomoModel = predModel;
        	else
        	{
	          aTomoModel = readGeoModel(tomoModelFilePath);

	          Polygon3D polygon = null;
	          if ((polygonFilePath != null) && (polygonFilePath.length() > 0))
	          {
	          	File f = new File(polygonFilePath);
	            polygon = new Polygon3D(f);
	            aTomoModel.setActiveRegion(polygon);
	          }
	          else
	            aTomoModel.setActiveRegion();

	          if (aOutput)
	          {
	            System.out.println("");
	            System.out.println("Created Tomography GeoModel ...");
	            System.out.println(aTomoModel.getMetaData().getInputModelFile().getCanonicalPath());
	            System.out.println("");
	          }
        	}

          // save configuration string and set verbosity

          aTomoModelFilePath = tomoModelFilePath;
        }
      }
    }
  }

  private void buildTomographyModelWeights(PredictionInterface pi,
  		                                     ArrayList<double[]> rayPathUnitVec,
  		                                     ArrayListDouble rayPathRadii,
  		                                     HashMapIntegerDouble weights,
  		                                     PredictorResult pr,
  		                                     PredictorObservation po) throws GeoTessException
  {
    if (aTomoModel != null)
    {
      // convert prediction ray path to tomography grid weights if it is
      // a valid ray

      if (pi.getRayPath() != null)
      {
        // valid ray ... get tomography grid weights
      	pi.getRayPath(rayPathUnitVec, rayPathRadii);
      	aTomoModel.getWeights(rayPathUnitVec, rayPathRadii, null,
      			                  InterpolatorType.LINEAR,
      			                  InterpolatorType.LINEAR, weights);
//        aTomoModel.getWeights(ray.getWaveType(), ray.getRayPath(),
//                              weights);
        // loop over all entries and put them in an array ... set the index and
        // weight arrays into the predictor result

        HashMapIntegerDouble.Iterator it = weights.iterator();
        int[]    indxs = new int [weights.size()];
        double[] wghts = new double [weights.size()];
        int j = 0;
        while (it.hasNext())
        {
          HashMapIntegerDouble.Entry e = it.nextEntry();
          indxs[j] = e.getKey();
          wghts[j] = e.getValue();
          ++j;
        }
        pr.setWeights(indxs, wghts);

        // if RAY_PATH was not requested nullify it and set result into the
        // result array

        if (!po.returnRayPath()) pi.nullifyRayPath();
        //pi.nullifyActiveNodeWeights();
      }
    }
    else
    	pr.setWeights(pi.getRayWeightIndexes(), pi.getRayWeights());   	
  }
}
