package gov.sandia.gmp.parallelutils;


import java.util.Date;
import java.util.LinkedList;

/**
 * A runnable designed to retrieve all returned tasks immediately upon
 * return form the ParallelBroker object so that their time stamp can
 * be set. This ensures their is no bias in the parallel transfer
 * overhead time estimate. This class simply starts running in a new
 * thread and continually updates a list of returned task results
 * (LSINVTaskResult). The list is updated by polling the ParallelBroker
 * and returning any task results that it may have, time stamping the
 * result, and storing it in the list. This thread continues until
 * shutdown by the client after all tasks have been processed and
 * received.
 * 
 * @author jrhipp
 *
 */
public class GetTaskResults<T extends ParallelResult> implements Runnable
{
	/**
	 * The parallel broker from which task results are returned.
	 */
  private ParallelBroker aParallelBrkr   = null;

	/**
	 * Number of task results returned
	 */
  private int            taskCount       = 0;

  /**
   * The list of all returned results from the ParallelBroker.
   */
  private LinkedList<ParallelResult>  returnedResults = new LinkedList<ParallelResult>();

  /**
   * The run condition. Defaults to true until stopped by the client using the function
   * stop().
   */
  private boolean        runCondition    = true;

  /**
   * The runnable thread for this class.
   */
  private Thread         runThread       = null;

  /**
   * Default constructor. Creates a new thread and starts it. 
   */
  public GetTaskResults(ParallelBroker pb)
  {
  	aParallelBrkr = pb;
    runThread = new Thread(this, "GetResults");
    runThread.start();
  }

  /**
   * The run method called by the new thread. It simply polls the
   * ParallelBroker to see if any new task results have been
   * returned. If any are found they are time stamped and added to
   * the internal list (returnedResults).
   */
  @Override
  public void run()
  {
    // clear the list and enter the perpetual while loop ... this loop
    // exits when the boolean runCondition is set to false by calling
    // function stop() below.

    returnedResults.clear();
    while (runCondition)
    {
      // see if a new task result is available

			ParallelResult tskrslt = aParallelBrkr.getResult();

      if (tskrslt != null)
      {
        // found task result ... add it to the list

        tskrslt.setTaskReturnTime((new Date()).getTime());

        synchronized(this)
        {
        	++taskCount;
          returnedResults.add(tskrslt);
        }
      }
    }
  }

  /**
   * Stop this thread.
   */
	public synchronized void stop()
	{
	  runCondition = false;
	}
	
	/**
	 * Get the next available returned task result. Returns null if not tasks
	 * are present.
	 * 
	 * @return The next available returned task result.
	 */
	@SuppressWarnings("unchecked")
	public synchronized T getNextResult()
	{
    if (returnedResults.size() > 0)
    	return (T) returnedResults.pollFirst();
    else
    	return null;
	}

	/**
	 * Returns the current stored task count.
	 * 
	 * @return The current stored task count.
	 */
	public synchronized int getCurrentCount()
	{
		return returnedResults.size();
	}

	/**
	 * Returns the total task count added to this container over its lifetime.
	 * 
	 * @return The total task count added to this container over its lifetime.
	 */
	public synchronized int getTotalTaskCount()
	{
		return taskCount;
	}
}