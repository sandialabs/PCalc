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
package gov.sandia.gmp.baseobjects.interfaces.impl;

import static gov.sandia.gmp.util.globals.Globals.NA_VALUE;
import static gov.sandia.gmp.util.globals.Globals.TWO_PI;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.PathCorrectionsInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.tttables.UncertaintyDistanceDependent;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

/**
 * If method getPrediction(PredictionRequestInterface) is called then a
 * Prediction for a single source-receiver pair is computed in sequential mode.
 * Methods getPredictions() compute multiple predictions in concurrent mode.
 * 
 * @author sballar
 * 
 */
abstract public class Predictor implements Callable<Predictor>, PredictorInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6190497049496325081L;

	private static int nextIndex = 0;
	private int index;

	protected int taskIndex;
	long taskTimer;
	protected ArrayList<PredictionRequestInterface> predictionRequest;
	protected ArrayList<PredictionInterface> predictions;
	private int predictorVerbosity;

	protected final int predictionsPerTask;

	private final ConcurrentLinkedQueue<Predictor> predictorPool = new ConcurrentLinkedQueue<Predictor>();

	protected final int maxProcessors;

	/**
	 * Here to facilitate interactions with database. Getter and setter provided but
	 * value is never modified by this class.
	 */
	private long modelId = -1;

	/**
	 * Here to facilitate interactions with database. Getter and setter provided but
	 * value is never modified by this class.
	 */
	private long algorithmId = -1;

	/**
	 * This is the set of GeoAttributes that the Predictor is capable of computing.
	 * The set of GeoAttributes that is actually computed during any call to
	 * getPrediction() or getPredictions() will depend on the set of
	 * requestetdAttributes that are submitted as part of the
	 * PredictionRequestInterface object. That set can be retrieved by referencing
	 * PredictionInterface.getSupportedAttributes().
	 */
	// protected EnumSet<GeoAttributes> supportedAttributes;

	/**
	 * travel time model uncertainty scale factor. tt will be set to: tt =
	 * ttModelUncertaintyScale[0]*tt + ttModelUncertaintyScale[1] Ignored if null.
	 */
	protected double[] ttModelUncertaintyScale;
	protected double[] azModelUncertaintyScale;
	protected double[] shModelUncertaintyScale;

	protected PathCorrectionsInterface pathCorrections;

	protected boolean usePathCorrectionsInDerivativesTT;
	protected boolean usePathCorrectionsInDerivativesAZ;
	protected boolean usePathCorrectionsInDerivativesSH;

	protected UncertaintyInterface uncertaintyInterface = null;

	public Predictor() {
		index = nextIndex++;

		predictionsPerTask = 1000;

		predictorVerbosity = 0;

		maxProcessors = Runtime.getRuntime().availableProcessors();
	}

	/**
	 * 
	 * @param properties
	 * @throws GMPException
	 */
	public Predictor(PropertiesPlusGMP properties, int predictionsPerTask) throws GMPException {
		index = nextIndex++;

		// set maxProcessors, the maximum number of processors to use
		// concurrently.
		if (properties.containsKey("maxProcessors"))
			this.maxProcessors = properties.getInt("maxProcessors");
		else
			this.maxProcessors = properties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors());

		this.predictionsPerTask = predictionsPerTask;

		this.predictorVerbosity = properties.getInt("predictorVerbosity", 0);
	}

	protected Predictor(UncertaintyInterface uncertaintyInterface, int maxProcessors, int predictionsPerTask,
			int verbosity) {
		index = nextIndex++;

		this.uncertaintyInterface = uncertaintyInterface;
		this.maxProcessors = maxProcessors;
		this.predictionsPerTask = predictionsPerTask;

		this.predictorVerbosity = verbosity;
	}

	/**
	 * Derived classes must supply a method that computes a single
	 * PredictionInterface object given a single PredictionRequestInterface object.
	 */
	@Override
	abstract public PredictionInterface getPrediction(PredictionRequestInterface request) throws GMPException;

	/**
	 * Uses java.util.concurrent package to compute multiple PredictionInterface
	 * objects in parallel using multi-threading.
	 */
	@Override
	public ArrayList<PredictionInterface> getPredictions(
			Collection<? extends PredictionRequestInterface> predictionRequests) throws GMPException {
		ArrayList<PredictionInterface> predictions = new ArrayList<PredictionInterface>(predictionRequests.size());

		int nTasks = (int) ceil(predictionRequests.size() / (double) predictionsPerTask);

		if (maxProcessors < 2 || nTasks < 2) {
			if (predictorVerbosity > 1) {
				// wrap the loop over prediction calculations in a timer.
				taskTimer = System.currentTimeMillis();

				for (PredictionRequestInterface request : predictionRequests)
					predictions.add(getPrediction(request));

				taskTimer = System.currentTimeMillis() - taskTimer;
				System.out.printf("Predictor %s processed %d requests using 1 processor in %1.3f sec%n",
						getPredictorName(), predictionRequests.size(), taskTimer * 1e-3);
			} else // no timer information
				for (PredictionRequestInterface request : predictionRequests)
					predictions.add(getPrediction(request));
		} else {
			// use multi-threading
			long timer = predictorVerbosity > 1 ? System.currentTimeMillis() : 0;
			try {
				if (nTasks < maxProcessors)
					nTasks = maxProcessors;

				// actual number of requests per task
				int npt = (int) ceil(predictionRequests.size() / (double) nTasks);

				if (predictorVerbosity > 0)
					System.out.printf(
							"Predictor %s processing %d requests using %d processors. %d tasks with %d requests per task%n",
							getPredictorName(), predictionRequests.size(), maxProcessors, nTasks, npt);

				taskTimer = 0;

				// break the collection of prediction requests up into a list or
				// arrays of requests. Each array of requests contains the requests that
				// will be submitted to a predictor in a single task.
				LinkedList<ArrayList<PredictionRequestInterface>> taskRequests = new LinkedList<ArrayList<PredictionRequestInterface>>();
				Iterator<? extends PredictionRequestInterface> it = predictionRequests.iterator();
				while (it.hasNext()) {
					ArrayList<PredictionRequestInterface> requestSet = new ArrayList<PredictionRequestInterface>(npt);
					for (int i = 0; i < npt && it.hasNext(); ++i)
						requestSet.add(it.next());
					taskRequests.add(requestSet);
				}

				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcessors);

				CompletionService<Predictor> queue = new ExecutorCompletionService<Predictor>(threadPool);

				// queueSize is the number of tasks that have been submitted to the queue at any
				// given time.
				int queueSize = 0;
				boolean done = false;

				while (!done) {
					// while there at still tasks to be computed, and the current queueSize is less
					// than
					// twice the number of processors, submit another task for processing.
					while (!taskRequests.isEmpty() && queueSize < maxProcessors) {
						// get a predictor from the pool. An existing predictor will be retrieved if one
						// is available. Otherwise a copy of this predictor will be instantiated and
						// returned.
						Predictor task = getPredictorFromPool();
						// get the next array of prediction requests that are to be computed.
						task.predictionRequest = taskRequests.removeFirst();
						// submit the task and increment queueSize
						queue.submit(task);
						++queueSize;
					}

					// there is an assumption here that calculation of a task takes longer than
					// removing
					// a completed task from the queue and adding all the predictions to the result
					// set.

					if (queueSize > 0) {
						// wait for a task to finish
						Predictor predictor = queue.take().get();
						--queueSize;

						predictions.addAll(predictor.predictions);

						taskTimer += taskTimer;

						// return the predictor to the predictorPool so it can be
						// reused for future processing.
						returnPredictorToPool(predictor);
					}

					// we are done when there are no more tasks in the queue.
					done = queueSize == 0;
				}

				threadPool.shutdown();
			} catch (Exception e) {
				throw new GMPException(e);
			}

			timer = System.currentTimeMillis() - timer;

			if (predictorVerbosity > 1) {
				System.out.printf("Outer timer                    : %7.3f sec%n", timer * 1e-3);
				System.out.printf("Sum inner timers / nProcessors : %7.3f sec%n", taskTimer * 1e-3 / maxProcessors);
				System.out.printf("Overhead                       : %7.3f sec%n",
						timer * 1e-3 - taskTimer * 1e-3 / maxProcessors);
				System.out.printf("Average time to compute one task (outer)      : %7.3f sec%n",
						timer * 1e-3 * maxProcessors / nTasks);
				System.out.printf("Average time to compute one task (inner)      : %7.3f sec%n",
						taskTimer * 1e-3 / nTasks);
				System.out.printf("Average time to compute one prediction (outer): %10.6f msec%n",
						((double) timer * maxProcessors) / predictionRequests.size());
				System.out.printf("Average time to compute one prediction (inner): %10.6f msec%n",
						((double) taskTimer) / predictionRequests.size());
				System.out.println();
			}
		}
		return predictions;
	}

	/**
	 * Uses java.util.concurrent package to compute multiple PredictionInterface
	 * objects in parallel using multi-threading.
	 * <p>
	 * Bridge between array based and collection based method calls. Converts the
	 * supplied array of prediction requests into a List, calls the collection-based
	 * getPredictions() method, receives a Collection of PredictionInterface objects
	 * which it converts into an array of same and returns that.
	 */
	@Override
	public PredictionInterface[] getPredictions(PredictionRequestInterface[] predictionRequests) throws GMPException {
		return getPredictions(Arrays.asList(predictionRequests))
				.toArray(new PredictionInterface[predictionRequests.length]);
	}

	@Override
	public Predictor call() {
		if (predictorVerbosity > 1)
			taskTimer = System.currentTimeMillis();
		predictions = new ArrayList<PredictionInterface>(predictionRequest.size());
		for (PredictionRequestInterface request : predictionRequest) {
			PredictionInterface prediction = null;
			try {
				prediction = getPrediction(request);
			} catch (GMPException e) {
				prediction = getNewPrediction(request, e);
			}
			predictions.add(prediction);
		}
		if (predictorVerbosity > 1)
			taskTimer = System.currentTimeMillis() - taskTimer;
		return this;
	}

	/**
	 * Derived classes must supply a new Predictor object that is fully configured
	 * and ready to compute a prediction for the supplied PredictionRequest object.
	 * The concurrency call() method in this class will be called to actually
	 * compute the prediction.
	 * 
	 * @param predictionRequest
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	abstract protected Predictor getCopy() throws GMPException, IOException;

	/**
	 * Derived classed must supply a method that returns a new PredictionInterface
	 * object based on a PredictionRequest and an error message.
	 * 
	 * @param predictionRequest
	 * @param msg
	 * @return
	 */
	abstract protected PredictionInterface getNewPrediction(PredictionRequestInterface predictionRequest, String msg);

	/**
	 * Derived classed must supply a method that returns a new PredictionInterface
	 * object based on a PredictionRequest and an Exception.
	 * 
	 * @param predictionRequest
	 * @param ex
	 * @return
	 */
	abstract protected PredictionInterface getNewPrediction(PredictionRequestInterface predictionRequest, Exception ex);

	/**
	 * Retrieve an existing Predictor object from the pool. If the pool is null or
	 * empty, instantiate a new one and return a reference to it.
	 * 
	 * @return
	 * @throws IOException
	 * @throws @throws     TauModelException
	 */
	private synchronized Predictor getPredictorFromPool() throws GMPException, IOException {
		// retrieve a Predictor from the pool.
		Predictor predictor = predictorPool.poll();
		if (predictor == null) {
			predictor = getCopy();

			if (predictorVerbosity >= 3)
				System.out.println("getPredictorFromPool() got new predictor with index " + getIndex());
		} else if (predictorVerbosity >= 3)
			System.out.println("getPredictorFromPool() got old predictor with index " + getIndex());
		return predictor != null ? predictor : getCopy();

	}

	/**
	 * Return a Predictor to the predictor pool so that it can be used for future
	 * processing.
	 * 
	 * @param predictor
	 */
	private void returnPredictorToPool(Predictor predictor) {
		if (predictorVerbosity >= 3)
			System.out.println("returnPredictorToPool() returning predictor with index " + getIndex());
		predictionRequest = null;
		predictions = null;
		predictorPool.offer(predictor);
	}

	/*
	 * Returns "AK135".
	 * 
	 * @see gov.sandia.gmp.util.interfaces.PredictorInterface#getModelDescription()
	 */
	@Override
	abstract public String getModelDescription() throws GMPException;

	/*
	 * Returns "AK135".
	 * 
	 * @see gov.sandia.gmp.util.interfaces.PredictorInterface#getModelName()
	 */
	@Override
	abstract public String getModelName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.sandia.gmp.util.interfaces.PredictorInterface#getPredictorName()
	 */
	@Override
	abstract public String getPredictorName();

	@Override
	abstract public PredictorType getPredictorType();

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.sandia.gmp.util.interfaces.PredictorInterface#getPredictorVersion()
	 */
	@Override
	abstract public String getPredictorVersion();

	@Override
	public long getAlgorithmId() {
		return algorithmId;
	}

	@Override
	public long getModelId() {
		return modelId;
	}

	@Override
	public synchronized void setAlgorithmId(long algorithmId) {
		this.algorithmId = algorithmId;
	}

	@Override
	public synchronized void setModelId(long modelId) {
		this.modelId = modelId;
	}

	@Override
	abstract public File getModelFile();

	/**
	 * @return the maxProcessors
	 */
	public int getMaxProcessors() {
		return maxProcessors;
	}

	/**
	 * @return the maxProcessors
	 */
	public int getPredictionsPerTask() {
		return predictionsPerTask;
	}

	@Override
	public UncertaintyInterface getUncertaintyInterface() {
		return uncertaintyInterface;
	}

	@Override
	public void setUncertaintyInterface(UncertaintyInterface uncertaintyInterface) {
		this.uncertaintyInterface = uncertaintyInterface;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	protected void setupUncertaintyInterface(PropertiesPlusGMP properties) throws GMPException, IOException {
		String prefix = getPredictorName().toLowerCase();
		String type = properties.getProperty(prefix + "UncertaintyType");
		if (type != null && type.equals("DistanceDependent"))
			uncertaintyInterface = new UncertaintyDistanceDependent(properties, prefix);
		else
			uncertaintyInterface = new UncertainyNAValue(properties, prefix);
	}

	/**
	 * Retrieve a brief name for the model loaded into Bender.
	 */
	@Override
	public String getModelName(int maxLength) {
		String name = getModelName();
		if (name.length() <= maxLength)
			return name;
		return name.substring(0, maxLength);
	}

	/**
	 * Some Predictors are able to look up attribute values such as
	 * SLOWNESS_PATH_CORRECTION or SLOWNESS_MODEL_UNCERTAINTY in lookup tables. It
	 * is advantageous to store the indexes of the attributes in the
	 * PredictionRequest object so that repeated calls for the same attribute values
	 * can be done as efficiently as possible.
	 * <p>
	 * This Predictor method is called by a PredictionRequest object so that the
	 * Predictor can set the lookup table indexes in the PredictionRequest.
	 * 
	 * @param predictionRequest reference to the PredictionRequest object that is
	 *                          requesting to have its lookup table indexes set.
	 */
	@Override
	public void setLookupTableIndexes(PredictionRequestInterface predictionRequest) {
		// do nothing. Predictors that support lookup tables will override this method.
	}

	public int getPredictorVerbosity() {
		return predictorVerbosity;
	}

	public void setPredictorVerbosity(int predictorVerbosity) {
		this.predictorVerbosity = predictorVerbosity;
	}

	/**
	 * Retrieve predicted base model travel time in seconds. 'base model' means
	 * 'predicted value without path corrections'. Should include ellipticity and
	 * elevation corrections, if appropriate.
	 * 
	 * @return predicted base model travel time in seconds.
	 * @throws Exception
	 */
	abstract protected double getTravelTime() throws Exception;

	/**
	 * Retrieve predicted base model slowness in seconds per radian, or NA_VALUE if
	 * invalid.
	 * 
	 * @return predicted base model slowness in seconds per radian.
	 * @throws Exception
	 */
	abstract protected double getSlowness() throws Exception;

	/**
	 * Retrieve the derivative of travel time wrt radius, in seconds/km, or NA_VALUE
	 * if invalid.
	 * 
	 * @return the derivative of travel time wrt radius, in seconds/km.
	 * @throws Exception
	 */
	abstract protected double getDttDr() throws Exception;

	/**
	 * Retrieve the derivative of slowness wrt distance, in seconds/(radian*radian),
	 * or NA_VALUE if invalid.
	 * 
	 * @return the derivative of slowness wrt distance, in seconds/(radian*radian)
	 * @throws Exception
	 */
	abstract protected double getDshDx() throws Exception;

	/**
	 * Retrieve the derivative of slowness wrt radius in seconds/(radian*km), or
	 * NA_VALUE if invalid.
	 * 
	 * @return the derivative of slowness wrt radius in seconds/(radian*km)
	 * @throws Exception
	 */
	abstract protected double getDshDr() throws Exception;

	/**
	 * Retrieve the derivative of travel time wrt distance, in seconds/radian.
	 * Synonym for getSlowness.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDttDx() throws Exception {
		return getSlowness();
	}

	/**
	 * Retrieve the derivative of travel time wrt latitude in seconds/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDttDlat() throws Exception {
		if (getSlowness() == NA_VALUE || getBackAzimuth() == NA_VALUE)
			return NA_VALUE;
		return -getSlowness() * cos(getBackAzimuth());
	}

	/**
	 * Retrieve the derivative of travel time wrt longitude in seconds/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDttDlon() throws Exception {
		if (getSlowness() == NA_VALUE || getBackAzimuth() == NA_VALUE)
			return NA_VALUE;
		return -getSlowness() * sin(getBackAzimuth());
	}

	/**
	 * Retrieve the derivative of slowness wrt latitude in seconds/radian/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDshDlat() throws Exception {
		if (getBackAzimuth() == NA_VALUE || getDshDx() == NA_VALUE)
			return NA_VALUE;
		return -getDshDx() * cos(getBackAzimuth());
	}

	/**
	 * Retrieve the derivative of slowness wrt longitude in seconds/radian/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDshDlon() throws Exception {
		if (getBackAzimuth() == NA_VALUE || getDshDx() == NA_VALUE)
			return NA_VALUE;
		return -getDshDx() * sin(getBackAzimuth());
	}

	/**
	 * Retrieve the derivative of receiver-source azimuth wrt source-receiver
	 * distance, in radians/radians. This is generally zero.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDazDx() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve the derivative of receiver-source azimuth wrt latitude, in
	 * radians/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDazDlat() throws Exception {
		if (getBackAzimuth() == NA_VALUE || getDistance() < 1e-7)
			return NA_VALUE;
		return sin(getBackAzimuth()) / sin(getDistance());
	}

	/**
	 * Retrieve the derivative of receiver-source azimuth wrt longitude, in
	 * radians/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDazDlon() throws Exception {
		if (getBackAzimuth() == NA_VALUE || getDistance() < 1e-7)
			return NA_VALUE;
		return -cos(getBackAzimuth()) / sin(getDistance());
	}

	/**
	 * Retrieve the derivative of azimuth wrt to source radius, in radians/km.
	 * Usually zero.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDazDr() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve the derivative of travel time wrt to origin time, in seconds/second
	 * Hard to imagine a scenario where this is not 1..
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDttDt() throws Exception {
		return 1.;
	}

	/**
	 * Retrieve the derivative of azimuth wrt to origin time, in radians/second
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDazDt() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve the derivative of slowness wrt to origin time, in
	 * seconds/radians/second
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getDshDt() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve the maximum departure from the plane of the great circle, in km.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected double getOutOfPlane() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve receiver-source azimuth in radians in range 0 to 2*PI, or NA_VALUE
	 * if anything goes wrong.
	 * 
	 * @return
	 */
	protected double getAzimuth() {
		if (azimuth == NA_VALUE) {
			azimuth = VectorGeo.azimuth(receiver, source, NA_VALUE);
			if (azimuth != NA_VALUE && azimuth < 0.)
				azimuth += TWO_PI;
		}
		return azimuth;
	}

	/**
	 * Retrieve the source-receiver azimuth in radians in range 0 to 2*PI, or
	 * NA_VALUE if anything goes wrong.
	 * 
	 * @return
	 */
	protected double getBackAzimuth() {
		if (backAzimuth == NA_VALUE) {
			backAzimuth = VectorGeo.azimuth(source, receiver, NA_VALUE);
			if (backAzimuth != NA_VALUE && backAzimuth < 0.)
				backAzimuth += TWO_PI;
		}
		return backAzimuth;
	}

	/**
	 * Retrieve the source-receiver distance in radians.
	 * 
	 * @return
	 */
	protected double getDistance() {
		if (distance == NA_VALUE)
			distance = VectorGeo.angle(source, receiver);
		return distance;
	}

	// Define a couple of private variables that are instantiated in
	// setAttributes(...)
	// and used by private methods called therein.
	private double[] source = new double[3];
	private double[] receiver = new double[3];
	private double azimuth = NA_VALUE;
	private double backAzimuth = NA_VALUE;
	private double distance = NA_VALUE;

	/**
	 * This method sets a whole bunch of GeoAttributes in the supplied Prediction
	 * object based on values returned by a bunch of methods like getTravleTime()
	 * getSlowness() etc. Five of the methods:
	 * <ul>
	 * <li>getTravelTime()
	 * <li>getSlowness()
	 * <li>getDttDr()
	 * <li>getDshDx()
	 * <li>getDshDr()
	 * </ul>
	 * <br>
	 * are abstract and must be implemented by derived classes. Others, like
	 * getOutOFPlane() and many more have default implementatons in this Predictor
	 * class which can be overriden by derived classes if they can compute values
	 * that differ from the default implementations.
	 * 
	 * <p>
	 * Also applies path corrections and sets relevant GeoAttributes, as
	 * appropriate.
	 * 
	 * <p>
	 * Also sets uncertainty values, if requested and available.
	 * 
	 * @param request
	 * @param prediction
	 * @param uncertaintyInterface
	 * @throws Exception
	 */
	protected void setGeoAttributes(PredictionRequestInterface request, Prediction prediction,
			UncertaintyInterface uncertaintyInterface) throws Exception {
		source = request.getSource().getPosition().getUnitVector();
		receiver = request.getReceiver().getPosition().getUnitVector();
		azimuth = backAzimuth = distance = NA_VALUE;

		double value, correction;
		int pcIndex;
		if (request.getRequestedAttributes().contains(GeoAttributes.TRAVEL_TIME)) {
			// unpath-corrected travel time in seconds
			value = getTravelTime();
			correction = 0;
			prediction.setAttribute(GeoAttributes.TT_BASEMODEL, value);
			prediction.setAttribute(GeoAttributes.TRAVEL_TIME, value);

			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT))
				prediction.setAttribute(GeoAttributes.DTT_DLAT, getDttDlat());

			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON))
				prediction.setAttribute(GeoAttributes.DTT_DLON, getDttDlon());

			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR))
				prediction.setAttribute(GeoAttributes.DTT_DR, getDttDr());

			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DTIME))
				prediction.setAttribute(GeoAttributes.DTT_DTIME, getDttDt());

			pcIndex = request.getLookupIndexPathCorrTT();
			if (pathCorrections != null && pcIndex >= 0) {
				correction = pathCorrections.getPathCorrection(pcIndex, request.getSource());

				if (value != NA_VALUE && correction != NA_VALUE)
					value += correction;

				prediction.setAttribute(GeoAttributes.TT_PATH_CORRECTION, correction);
				prediction.setAttribute(GeoAttributes.TRAVEL_TIME, value);

				if (usePathCorrectionsInDerivativesTT) {
					if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT)) {
						value = prediction.getAttribute(GeoAttributes.DTT_DLAT);
						correction = pathCorrections.getPathCorrDerivLat(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;
						prediction.setAttribute(GeoAttributes.DTT_DLAT, value);
						prediction.setAttribute(GeoAttributes.TT_PATH_CORR_DERIV_LAT, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON)) {
						value = prediction.getAttribute(GeoAttributes.DTT_DLON);
						correction = pathCorrections.getPathCorrDerivLon(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;
						prediction.setAttribute(GeoAttributes.DTT_DLON, value);
						prediction.setAttribute(GeoAttributes.TT_PATH_CORR_DERIV_LON, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR)) {
						value = prediction.getAttribute(GeoAttributes.DTT_DR);
						correction = pathCorrections.getPathCorrDerivRadial(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;
						prediction.setAttribute(GeoAttributes.DTT_DR, value);
						prediction.setAttribute(GeoAttributes.TT_PATH_CORR_DERIV_RADIAL, correction);
					}
				}
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.TT_MODEL_UNCERTAINTY)) {
				uncertaintyInterface.setUncertainty(prediction, GeoAttributes.TT_MODEL_UNCERTAINTY);
				if (ttModelUncertaintyScale != null && ttModelUncertaintyScale.length > 0) {
					double ttuncertainty = prediction.getAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY);
					if (ttuncertainty != NA_VALUE) {
						ttuncertainty *= ttModelUncertaintyScale[0];
						ttuncertainty += ttModelUncertaintyScale[1];
						prediction.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, ttuncertainty);
					}
				}
			}
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH)
				|| request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES)) {
			// unpath-corrected azimuth in radians
			value = getAzimuth();
			correction = 0;
			prediction.setAttribute(GeoAttributes.AZIMUTH_BASEMODEL, value);

			if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH))
				prediction.setAttribute(GeoAttributes.AZIMUTH, value);

			if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES))
				prediction.setAttribute(GeoAttributes.AZIMUTH_DEGREES, Math.toDegrees(value));

			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLAT))
				prediction.setAttribute(GeoAttributes.DAZ_DLAT, getDazDlat());

			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLON))
				prediction.setAttribute(GeoAttributes.DAZ_DLON, getDazDlon());

			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DR))
				prediction.setAttribute(GeoAttributes.DAZ_DR, getDazDr());

			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DTIME))
				prediction.setAttribute(GeoAttributes.DAZ_DTIME, getDazDt());

			pcIndex = request.getLookupIndexPathCorrAZ();
			if (pathCorrections != null && pcIndex >= 0) {
				correction = pathCorrections.getPathCorrection(pcIndex, request.getSource());

				if (value != NA_VALUE && correction != NA_VALUE)
					value = (value + correction + TWO_PI) % TWO_PI;

				prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORRECTION, correction);

				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH))
					prediction.setAttribute(GeoAttributes.AZIMUTH, value);

				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES))
					prediction.setAttribute(GeoAttributes.AZIMUTH_DEGREES, Math.toDegrees(value));

				if (usePathCorrectionsInDerivativesAZ) {
					if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLAT)) {
						value = prediction.getAttribute(GeoAttributes.DAZ_DLAT);
						correction = pathCorrections.getPathCorrDerivLat(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;
						prediction.setAttribute(GeoAttributes.DAZ_DLAT, value);
						prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LAT, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLON)) {
						value = prediction.getAttribute(GeoAttributes.DAZ_DLON);
						correction = pathCorrections.getPathCorrDerivLon(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;
						prediction.setAttribute(GeoAttributes.DAZ_DLON, value);
						prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LON, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DR)) {
						value = prediction.getAttribute(GeoAttributes.DAZ_DR);
						correction = pathCorrections.getPathCorrDerivRadial(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;
						prediction.setAttribute(GeoAttributes.DAZ_DR, value);
						prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL, correction);
					}
				}
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY)
					|| request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES)) {
				value = uncertaintyInterface.getUncertainty(prediction, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY);
				if (value != NA_VALUE && azModelUncertaintyScale != null && azModelUncertaintyScale.length > 0)
					value = value * azModelUncertaintyScale[0] + azModelUncertaintyScale[1];
				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY))
					prediction.setAttribute(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, value);
				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES))
					prediction.setAttribute(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
							value == NA_VALUE ? NA_VALUE : toDegrees(value));
			}
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS)
				|| request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES)) {
			// unpath-corrected slowness in sec/radian
			value = getSlowness();
			correction = 0;
			prediction.setAttribute(GeoAttributes.SLOWNESS_BASEMODEL, value);

			if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS))
				prediction.setAttribute(GeoAttributes.SLOWNESS, value);

			if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
				prediction.setAttribute(GeoAttributes.SLOWNESS_DEGREES, Math.toRadians(value));

			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DX))
				prediction.setAttribute(GeoAttributes.DSH_DX, getDshDx());

			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DX_DEGREES)) {
				double dshdx = getDshDx();
				prediction.setAttribute(GeoAttributes.DSH_DX_DEGREES,
						dshdx == NA_VALUE ? NA_VALUE : toRadians(toRadians(dshdx)));
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLAT))
				prediction.setAttribute(GeoAttributes.DSH_DLAT, getDshDlat());

			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLON))
				prediction.setAttribute(GeoAttributes.DSH_DLON, getDshDlon());

			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DR))
				prediction.setAttribute(GeoAttributes.DSH_DR, getDshDr());

			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DTIME))
				prediction.setAttribute(GeoAttributes.DSH_DTIME, getDshDt());

			pcIndex = request.getLookupIndexPathCorrSH();
			if (pathCorrections != null && pcIndex >= 0) {
				correction = pathCorrections.getPathCorrection(pcIndex, request.getSource());

				if (value != NA_VALUE && correction != NA_VALUE)
					value = value + correction;

				prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORRECTION, correction);

				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS))
					prediction.setAttribute(GeoAttributes.SLOWNESS, value);

				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
					prediction.setAttribute(GeoAttributes.SLOWNESS_DEGREES, Math.toRadians(value));

				if (usePathCorrectionsInDerivativesAZ) {
					if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DX)) {
						value = prediction.getAttribute(GeoAttributes.DSH_DX);
						correction = pathCorrections.getPathCorrDerivLat(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;

						prediction.setAttribute(GeoAttributes.DSH_DX, value);
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DX_DEGREES)) {
						value = prediction.getAttribute(GeoAttributes.DSH_DX_DEGREES);
						correction = pathCorrections.getPathCorrDerivLat(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += toRadians(correction);

						prediction.setAttribute(GeoAttributes.DSH_DX_DEGREES, value);
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLAT)) {
						value = prediction.getAttribute(GeoAttributes.DSH_DLAT);
						correction = pathCorrections.getPathCorrDerivLat(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;

						prediction.setAttribute(GeoAttributes.DSH_DLAT, value);
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LAT, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLON)) {
						value = prediction.getAttribute(GeoAttributes.DSH_DLON);
						correction = pathCorrections.getPathCorrDerivLon(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;

						prediction.setAttribute(GeoAttributes.DSH_DLON, value);
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LON, correction);
					}

					if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DR)) {
						value = prediction.getAttribute(GeoAttributes.DSH_DR);
						correction = pathCorrections.getPathCorrDerivRadial(pcIndex, request.getSource());
						if (value != NA_VALUE && correction != NA_VALUE)
							value += correction;

						prediction.setAttribute(GeoAttributes.DSH_DR, value);
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL, correction);
					}
				}
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY))
				uncertaintyInterface.setUncertainty(prediction, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);

			if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES))
				uncertaintyInterface.setUncertainty(prediction, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES);

			if (shModelUncertaintyScale != null && shModelUncertaintyScale.length == 2) {
				double shuncertainty = prediction.getAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);
				if (shuncertainty != NA_VALUE) {
					shuncertainty *= shModelUncertaintyScale[0];
					shuncertainty += shModelUncertaintyScale[1];
					prediction.setAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, shuncertainty);
				}
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY)
					|| request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES)) {
				value = uncertaintyInterface.getUncertainty(prediction, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);
				if (value != NA_VALUE && shModelUncertaintyScale != null && shModelUncertaintyScale.length > 0)
					value = value * shModelUncertaintyScale[0] + shModelUncertaintyScale[1];
				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY))
					prediction.setAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, value);
				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES))
					prediction.setAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
							value == NA_VALUE ? NA_VALUE : toRadians(value));
			}
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH_DEGREES))
			prediction.setAttribute(GeoAttributes.BACKAZIMUTH_DEGREES,
					getBackAzimuth() == NA_VALUE ? NA_VALUE : toDegrees(getBackAzimuth()));

		if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH))
			prediction.setAttribute(GeoAttributes.BACKAZIMUTH, getBackAzimuth());

		if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE))
			prediction.setAttribute(GeoAttributes.DISTANCE, getDistance());

		if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE_DEGREES))
			prediction.setAttribute(GeoAttributes.DISTANCE_DEGREES, toDegrees(getDistance()));

		if (request.getRequestedAttributes().contains(GeoAttributes.OUT_OF_PLANE))
			prediction.setAttribute(GeoAttributes.OUT_OF_PLANE, getOutOfPlane());

	}

}
