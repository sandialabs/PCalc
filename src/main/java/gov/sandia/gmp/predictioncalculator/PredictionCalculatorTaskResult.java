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

import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.parallelutils.ParallelResult;

/**
 * Used as a container to hold the results from a specific
 * @PredictionCalculatorTask.
 *
 * <p> The primary content of this object is a list of @PredictorInterface result
 * objects containing the prediction results of a set of @PredictionRequestInterface 
 * requests. The computing host name, and the process calculation time are also returned.
 *
 * @author avencar
 *
 */
@SuppressWarnings("serial")
public class PredictionCalculatorTaskResult extends ParallelResult
{
	/**
	 * Static line separator for output.
	 */
	private static final String NL = System.getProperty("line.separator");

	/**
	 * The list of @PredictionInterface objects corresponding to a set of
	 * @PredictionRequestInterface inputs.
	 */
	private ArrayList<PredictionInterface> aPredictions = null;

	/**
	 * Used largely for debugging purposes
	 */
	private String aMessage         = "";

	/**
	 * Standard constructor that sets the size of the @PredictionInterface list.
	 *
	 * @param sze The number of @PredictionInterface objects contained by this result.
	 */
	public PredictionCalculatorTaskResult(int size)
	{
		aPredictions = new ArrayList<PredictionInterface>(size);
	}

	/**
	 * Returns the entire list of @PredictionInterface objects.
	 *
	 * @return The list
	 */
	public ArrayList<PredictionInterface> getPredictions()
	{
		return aPredictions;
	}

	/**
	 * Set the entire list of @PredictionInterface objects.
	 * @param predictions - the list
	 */
	public void setPredictions(ArrayList<PredictionInterface> predictions) 
	{
		aPredictions = predictions;
	}

	/**
	 * Add another line to the debug message.
	 *
	 * @param s New line added to the debug message.
	 */
	public void appendMessage(String s)
	{
		aMessage += s + NL;
	}

	/**
	 * Clears the debug message.
	 */
	public void clearMessage()
	{
		aMessage = "";
	}

	/**
	 * Returns debug message string.
	 *
	 * @return Debug message string.
	 */
	public String getMessage()
	{
		return aMessage;
	}
}
