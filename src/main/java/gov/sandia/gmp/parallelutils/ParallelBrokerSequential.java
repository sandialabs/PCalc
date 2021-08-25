package gov.sandia.gmp.parallelutils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Sequential ParallelBroker turns off all concurrent or distributed
 * parallelism and submits and processes tasks in the order called.
 * This mode is generally useful for debugging or in cases where
 * concurrency or distributed options are not available.
 *
 * @author jrhipp
 *
 */
public class ParallelBrokerSequential extends ParallelBroker
implements CommunicationsManager
{
  private LinkedList<ParallelTask> aTasks =
    new LinkedList<ParallelTask>();

  /**
   * Default constructor.
   */
  public ParallelBrokerSequential()
  {
    super();
  }

  /**
   * Returns the type name of the ParallelBroker.
   * 
   * @return The type name of the ParallelBroker.
   */
  @Override
  public String getName()
  {
    return "Sequential";
  }

  /**
   * Returns the available processor count (this is always 1 for a
   * SequentialBroker).
   * 
   * @return The available processor count.
   */
  @Override
  public int getProcessorCount()
  {
    return 1;
  }
  
  /**
   * Returns the available processor count, by host (always 1 entry, with a value of 1)
   * 
   * @return The available processor count, by host.
   */
  @Override
  public Map<String, Integer> getProcessorCountByHost() {
	  Map<String, Integer> map = new HashMap<String,Integer>();
	  try {
		  map.put((InetAddress.getLocalHost()).getHostName(),1);
	  } catch (UnknownHostException e) {}
	  return map;
  }
  
  @Override
  public int getProcessorCountEstimate() { return 1; }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResult()
  {
    // return null if no results are available or pending

    if (isEmpty()) return null;

    // remove the next task and calculate it ... return the result

    ParallelTask tsk = aTasks.remove();
    tsk.setLocalThreadCount(1);
    tsk.run();
    tsk.setLocalThreadCount(null);
    return tsk.getResultObject();
  }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResultWait()
  {
    return getResult();
  }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public List<ParallelResult> getResults()
  {
    // return null if no results are available or pending
    
    if (isEmpty()) return null;

    // create a list to put the task in

    ArrayList<ParallelResult> results =
              new ArrayList<ParallelResult>(1);

    // remove the next task and calculate it ... add it to the result and
    // return the result

    ParallelTask tsk = aTasks.remove();
    tsk.setLocalThreadCount(1);
    tsk.run();
    tsk.setLocalThreadCount(null);
    results.add(tsk.getResultObject());
    return results;
  }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public List<ParallelResult> getResultsWait()
  {
    return getResults();
  }

  /**
   * Returns true if no more task results are available.
   * 
   * @return True if no more task results are available.
   */
  @Override
  public boolean isEmpty()
  {
    return (size() == 0);
  }

  /**
   * Returns the number of task results that are currently available.
   * 
   * @return The number of task results that are currently available.
   */
  @Override
  public int size()
  {
    return aTasks.size();
  }

  /**
   * Submit a list of tasks for processing. This function returns immediately.
   * 
   * @param tsks The list of all ParallelTasks to be submitted for
   *             processing.
   */
  @Override
  public void submit(List<? extends ParallelTask> tsks)
  {
    for(ParallelTask t : tsks) submit(t);
  }

  /**
   * Submit a single task for processing. This function returns immediately.
   * 
   * @param tsk The ParallelTask to be submitted for processing.
   */
  @Override
  public void submit(ParallelTask tsk)
  {
	if(ParallelTask.getCommunicationsManager() == null){
		synchronized(ParallelTask.class){
			if(ParallelTask.getCommunicationsManager() == null)
				ParallelTask.setCommunicationsManager(this);
		}
	}
    aTasks.add(tsk);
  }

  @Override
  public void sendToClient(Object message){
	  super.setMessageReceived(message);
  }
  
  @Override
  public void submitStaticCleanupTask(final StaticCleanupTask tsk,
		  boolean waitFor){
    if(waitFor) tsk.run();
    else new Thread(tsk).start();
  }
}
