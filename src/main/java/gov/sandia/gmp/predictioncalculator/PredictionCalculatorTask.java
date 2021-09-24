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
package gov.sandia.gmp.predictioncalculator;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.predictorfactory.PredictorFactory;

/**
 * A parallel task that can run in sequential or distributed fashion using
 * the JPPF system by calling the run() function on a distributed node. This
 * task, as currently written, will not work when run in concurrent mode within
 * the same JVM because @Predictor is not thread-safe.
 *
 * <p> The job of this object is to execute a list of @PredictionRequestInterface
 * objects using the specified @PredictorInterface. The PredictorInterface is 
 * stored as a static object and is recreated if the "predictor_type" property
 * changes in the provided @PropertiesPlusGMP properties file.
 *
 * @author avencar
 *
 */
@SuppressWarnings("serial")
public class PredictionCalculatorTask extends ParallelTask
{

	/**
	 * Simple static task id counter that is auto-incremented with each
	 * constructor call.
	 */
	private static int aId = -1;

	/**
	 * The list of @PredictorRequestInterface requests to be processed by the
	 * run() method.
	 */
	private ArrayList<PredictionRequestInterface> aPredRequests = null;

	/**
	 * Simple static screen output flag.
	 */
	private static boolean aOutput = true;

	/**
	 * The @PropertiesPlusGMP properties file to be used by the Predictor
	 */
	private PropertiesPlusGMP properties;


	/**
	 * Standard constructor that sets the @PropertiesPlusGMP properties file,
	 * and the list of @PredictionRequestInterface objects to be executed by
	 * the run() method.
	 * 
	 * @param properties - properties file
	 * @param requests - list of prediction requests
	 */
	public PredictionCalculatorTask(
			PropertiesPlusGMP properties,
			ArrayList<PredictionRequestInterface> requests)
	{
		this.aPredRequests = new ArrayList<PredictionRequestInterface>(requests);
		this.properties = properties;

		// set unique task id
		++aId;
	    setId(Integer.toString(aId));
	}

	/**
	 * The function that processes all @PredictionInterfaceRequest objects stored in
	 * aPredRequests when called. Each @PredictionInterfaceRequest is used as input
	 * into the predictor to compute a @PredictionInterface result. The results are
	 * stored in a single @PredictionCalculatorTaskResult object that matches one-for-one
	 * to this @PredictionCalculatorTask. 

	 * The run() method is called by the JPPF distributed system to be
	 * executed on a network node.
	 */
	@Override
	public void run()
	{
		if (aPredRequests == null || aPredRequests.size() <= 0) System.exit(0);
		
		// Set predictor identifier and create a new result bundle
		PredictionCalculatorTaskResult result;
		result = new PredictionCalculatorTaskResult(aPredRequests.size());
		result.setTaskSubmitTime(getSubmitTime());
		result.setIndex(getIndex());
		setResult(result);
		
		try
		{
			String hostname = (InetAddress.getLocalHost()).getHostName();
			result.setHostName(hostname);
			outputTaskInfo(result.getHostName(), "Entry", "");
			
			// Use PredictoryFactory to compute predictions
			PredictorFactory pf = new PredictorFactory(properties,"predictor_type");
			pf.addPredictionRequests(aPredRequests);
			result.setPredictions(pf.computePredictions(false));
		
		    outputTaskInfo(result.getHostName(), "Exit", "");  
		    
		}
		catch (Exception ex)
		{
			result.setException(ex);
			ex.printStackTrace();
			if (aOutput) {
				stdout("PredictionCalculatorTask::catch (Exception ex) ");
				result.appendMessage("Exception Occurred ...");
			}
			outputTaskInfo(result.getHostName(), "Error", "");
		}
	}

	/**
	 * Outputs task information as an event that can be picked up by task
	 * listeners. The task id, host name and time are always output. The
	 * optional tag and message are added to the string to identify the
	 * pertinent information. The information is only sent if aOutput is
	 * true.
	 * 
	 * @param hostname - Host name of the machine executing the task.
	 * @param tag - Information tag (e.g. "Entry" or "Exit").
	 * @param msg - Pertinent information to be conveyed by the message.
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
			stdout(s);
		}
	}


	/**
	 * Returns the list of PredictorObservation objects.
	 *
	 * @return The list of PredictorObservation objects.
	 */
	public ArrayList<PredictionRequestInterface> getPredictionRequests()
	{
		return aPredRequests;
	}

	/**
	 * Set the screen output flag
	 */
	public void setOutput(boolean out)
	{
		aOutput = out;
	}


	/**
	 * Construct a list of @PredictionCalculatorTask tasks with a max number of predictions
	 * per task.
	 * @param properties - used as input to construct of @PredictionCalculatorTask
	 * @param requests - @PredictionRequestInterface requests
	 * @param numPredPerTask - maximum # of predictions per task
	 * @return
	 */
	public static ArrayList<PredictionCalculatorTask> buildPredictionCalculatorTasks(PropertiesPlusGMP properties,
			ArrayList<PredictionRequestInterface> requests,int numPredPerTask)
	{
		ArrayList<PredictionCalculatorTask> tasks = new ArrayList<PredictionCalculatorTask>();
		ArrayList<PredictionRequestInterface> requestsForTask = new ArrayList<PredictionRequestInterface>();

		for (PredictionRequestInterface request : requests) {
			requestsForTask.add(request);
			if (requestsForTask.size() >= numPredPerTask) {
				PredictionCalculatorTask task = new PredictionCalculatorTask(properties,requestsForTask);
				tasks.add(task);
				requestsForTask = new ArrayList<PredictionRequestInterface>();
			}
		}

		// Last task
		if (requestsForTask.size() > 0) {
			PredictionCalculatorTask task = new PredictionCalculatorTask(properties,requestsForTask);
			tasks.add(task);
		}

		return tasks;
	}	
}
