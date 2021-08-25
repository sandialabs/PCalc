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
