package gov.sandia.gmp.parallelutils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The concurrent ParallelBroker uses the Java completion service and a
 * ThreadPoolExecutor to submit tasks as threads on the current executing
 * platform. The thread pool uses 2 times the number of available
 * processors defined on the current machine.
 * 
 * @author jrhipp
 *
 */
public class ParallelBrokerConcurrent extends ParallelBroker
{
  /**
   * Number of processor threads to use (number of available processors, by
   * default).
   */
  private int aNProcessors = Runtime.getRuntime().availableProcessors();

  /**
   * The local thread pool on which the processes will execute.
   */
  private ThreadPoolExecutor aThreadPool =
         (ThreadPoolExecutor) Executors.newFixedThreadPool(aNProcessors);

  /**
   * The completion service results queue used to save the results as they
   * return from the thread pool.
   */
  private CompletionService<ParallelResult> aQueue =
          new ExecutorCompletionService<ParallelResult>(aThreadPool);

  /**
   * The current number of tasks for which results are or will be available.
   * This number is simply the total number of submitted tasks minus those
   * that have been retrieved with a call to getResults().
   */
  private int aTaskCount = 0;

  /**
   * A simple inner class required to implement the concurrent Callable
   * interface. This object wraps the BenderResultBundle call with a
   * try/catch exception block. 
   * 
   * @author jrhipp
   *
   */
  final class ConcurrentTask implements Callable<ParallelResult>,
  CommunicationsManager
  {
    /**
     * The task to be executed concurrently
     */
    ParallelTask pt = null;

    /**
     * Standard constructor that saves the input task.
     * 
     * @param pt - The input task to be executed concurrently.
     */
    public ConcurrentTask(ParallelTask pt)
    {
      this.pt = pt;
    }

    /**
     * Execute the task saved in bob. This function will execute the task
     * in a concurrent fashion by calling the tasks run() function. 
     */
    @Override
	public ParallelResult call() throws Exception
    {
      // call the task run function, retrieve and return the result

      try
      {
    	if(ParallelTask.getCommunicationsManager() == null){
    		synchronized(ParallelTask.class){
    			if(ParallelTask.getCommunicationsManager() == null)
    				ParallelTask.setCommunicationsManager(this);
    		}
    	}
    	  
    	pt.setLocalThreadCount(aNProcessors);
        pt.run();
        pt.setLocalThreadCount(null);
        return pt.getResultObject();
      }
      catch (Exception ex)
      {
        // return null if an error occurs

        ex.printStackTrace();
        return (ParallelResult) null;
      }
    }

	@Override
	public void sendToClient(Object message) throws IOException{
		ParallelBrokerConcurrent.this.setMessageReceived(message);
	}
  }

  /**
   * Default constructor.
   */
  public ParallelBrokerConcurrent()
  {
    super();
  }

  /**
   * Sets the processor count to the input value instead of the one
   * returned by Runtime.getRuntime().availableProcessors().
   * 
   * @param procCount The concurrent processor count to use.
   * 
   */
  @Override
  public void setProcessorCount(int procCount)
  {
    if (procCount > 0)
    {
      aNProcessors = procCount;
      aThreadPool  = (ThreadPoolExecutor) Executors.newFixedThreadPool(aNProcessors);
      aQueue       = new ExecutorCompletionService<ParallelResult>(aThreadPool);
    }
    //System.out.println("Number of processors to be used: " + aNProcessors);
  }
  
  @Override
  public void close(){ aThreadPool.shutdown(); }

  /**
   * Returns the type name of the ParallelBroker.
   * 
   * @return The type name of the ParallelBroker.
   */
  @Override
  public String getName()
  {
    return "Concurrent";
  }

  /**
   * Returns the available processor count.
   * 
   * @return The available processor count.
   */
  @Override
  public int getProcessorCount()
  {
    return aNProcessors;
  }
  
  /**
   * Returns the available processor count, by host.
   * 
   * @return The available processor count, by host.
   */
  @Override
  public Map<String, Integer> getProcessorCountByHost() {
	  Map<String, Integer> map = new HashMap<String,Integer>();
	  try {
		  map.put((InetAddress.getLocalHost()).getHostName(), aNProcessors);
	  } catch (UnknownHostException e) {}
	  return map;
  }

  @Override
  public int getProcessorCountEstimate() { return aNProcessors; }

  /**
   * Return the next available result or null if none are available
   * or ever will be.
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResult()
  {
    // return null if no tasks are pending

    if (aTaskCount == 0) return null;

    // get the most recent Future if any. If none are found return null

    Future<ParallelResult> f = aQueue.poll();
    if (f != null)
    {
      // found a future ... decrement the task count and return the result.
      // if an error occurs return null.

      try
      {
        --aTaskCount;
        //System.out.println("Remaining Tasks " + aTaskCount);
        return f.get();
      }
      catch(Exception ex)
      {
        ex.printStackTrace();
        return  null;
      }
    }
    else
      // none are available ... return null

      return  null;
  }

  /**
   * Return the next available result or null if none are available
   * or ever will be. This function waits (blocks) until a task is available
   * before returning.
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResultWait()
  {
    // return null if no tasks are pending

    if (aTaskCount == 0) return null;

    // get another task

    try
    {
      ParallelResult pr = aQueue.take().get();
      --aTaskCount;
      //System.out.println("Remaining Tasks " + aTaskCount);
      return pr;
    }
    catch(Exception ex)
    {
        ex.printStackTrace();
        return null;
    }
  }

  /**
   * Return all available results or null if none are available
   * or ever will be.
   * 
   * @return List of all available results.
   */
  @Override
  public List<ParallelResult> getResults()
  {
    // return null if no tasks are pending

    if (aTaskCount == 0) return null;

    // create a results vector to add any entries into

    ArrayList<ParallelResult> results =
      new ArrayList<ParallelResult>(aTaskCount + 1);

    // loop over all returned results

    Future<ParallelResult> f;
    while ((f = aQueue.poll()) != null)
    {
      // found a future ... decrement the task count and add the result
      // into the list. if an error occurs return null.

      try
      {
        --aTaskCount;
        results.add(f.get());
      }
      catch(Exception ex)
      {
        ex.printStackTrace();
        return  null;
      }
    }
    
    // if no results are available return null ... otherwise return the
    // results

    if (results.size() == 0)
      return null;
    else
      return results;
  }

  /**
   * Return all available results or null if none are available
   * or ever will be. This function waits (blocks) until tasks are available
   * before returning.
   * 
   * @return List of all available results.
   */
  @Override
  public List<ParallelResult> getResultsWait()
  {
    // return null if no tasks are pending

    if (aTaskCount == 0) return null;

    // wait for results and return

    List<ParallelResult> result;
    while ((result = getResults()) == null);
    return result;
  }

  /**
   * Returns true if no more task results are available.
   * 
   * @return True if no more task results are available.
   */
  @Override
  public boolean isEmpty()
  {
    return (aTaskCount == 0);
  }

  /**
   * Returns the number of task results that are currently available.
   * 
   * @return The number of task results that are currently available.
   */
  @Override
  public int size()
  {
    return aTaskCount;
  }

  /**
   * Submit a list of tasks for concurrent parallel processing. This
   * function returns immediately.
   * 
   * @param tsks The list of all ParallelTasks to be submitted for
   *             processing.
   */
  @Override
  public void submit(List<? extends ParallelTask> tsks)
  {
    // loop over each task and submit

    for (int i = 0; i < tsks.size(); ++i) submit(tsks.get(i));
  }

  /**
   * Submit a single task for concurrent parallel processing. This
   * function returns immediately.
   * 
   * @param tsk The task to be processed.
   */
  @Override
  public void submit(ParallelTask tsk)
  {
    // increment task count, wrap the task in a Callable, and submit it to
    // the completion service for processing.

    ++aTaskCount;
    ConcurrentTask ct = new ConcurrentTask(tsk);
    aQueue.submit(ct);
  }

  @Override
  public void submitStaticCleanupTask(StaticCleanupTask tsk, boolean waitFor) {
	Future<?> f = aThreadPool.submit(tsk);
	if(waitFor){
	  try {
	    f.get();
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	}
  }
}
