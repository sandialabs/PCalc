package gov.sandia.gmp.parallelutils;

import java.io.Serializable;

/**
 * @author bjlawry
 * <p>This interface provides ParallelUtils a way to get around dependencies
 * that Distributed ParallelTasks have that are not required by applications
 * that don't require JPPF/NRM functionality.
 */
public interface SharedObjectProvider extends Serializable{
	/**
	 * This method may only be called from distributed versions of
	 * ParallelTasks and returns what a JPPFTask instance would return when
	 * calling getDataProvider().getValue(key).  This method always returns
	 * null if used in a non-distributed sense.
	 * 
	 * @param key the key that is mapped to the desired shared object. 
	 * @return the shared object associated with the given key, provided that
	 * A) the key is valid and B) the calling task implementation is being used
	 * in the distributed sense.
	 */
	public Object getSharedObject(String key) throws Exception;
	
	/** 
	 * Sets this SharedObjectProvider's result instance.
	 * @param result
	 */
	public void setResult(Object result);
	
	/** @return this SharedObjectProvider's result instance. */
	public Object getResult();
	
	/**
	 * Allows the developer to store a unique ID for this task.
	 * @param id
	 */
	public void setId(String id);
	
	/** @return the unique ID set by calling setID(String id). */
	public String getId();
}
