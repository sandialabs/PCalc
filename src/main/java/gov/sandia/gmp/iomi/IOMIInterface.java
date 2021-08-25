package gov.sandia.gmp.iomi;

import java.io.IOException;

import gov.sandia.gmp.util.containers.linkedlist.EmbeddedLinkedListInterface;

/**
 * The IOMI object interface. Usually assigned to children of a base class
 * that only defined methods in IOMIObjectInterface. Objects implementing this
 * interface are manipulated by IOMIFactoryInterface objects and the
 * IOMIManager.
 *
 * @author jrhipp
 */
public interface IOMIInterface extends IOMIObjectInterface,
        EmbeddedLinkedListInterface<IOMIInterface> {
    /**
     * The function called by the owning MatrixBlockManger to set the IO
     * process flag to true or false.
     *
     * @param saio The new value of the IO process flag.
     */
    public void setAllowIO(boolean saio);

    /**
     * The IO request function. This function calls the IOMIManager
     * to add itself to the internal IO process queue. When the manager approves
     * the request for actual processing it will call setAllowIO(true)
     * so that the function exits allowing the IO process to commence.
     *
     * @throws InterruptedException
     */
    public void requestIO() throws IOException;

    /**
     * Notifies the IOMIManager of the completed IO process so
     * that it can allow the next queued IOMIInterface object (if any) to perform
     * its requested IO.
     *
     * @param iorfo The FileIO operation completed.
     * @throws IOException
     */
    public void completedIO(IOMIFileOp iorfo, String srvrTag) throws IOException;

    /**
     * Called when new memory has been allocated in this IOMIInterface object.
     *
     * @param mem  The amount of memory allocated in this IOMIInterface object.
     * @param load True if this is the primary load allocation.
     */
    public void notifyIOAllocate(long mem, boolean load) throws IOException;

    /**
     * Called when a thread has been blocking waiting for IO to complete.
     *
     * @param tim The amount of time this thread has spent blocking waiting for
     *            an IO operation to complete.
     */
    public void notifyIOBlocking(long tim) throws IOException;

    /**
     * Called by IOManager to add a reference to this IOMIInteface object
     */
    public void addIOReference();

    /**
     * Called by IOManager to remove a reference to this IOMIInteface object.
     */
    public void removeIOReference();

    /**
     * Returns the current IOMIManager reference count assigned to this object.
     */
    public boolean isIOReferenced();

    /**
     * Returns the current IO reference count.
     *
     * @return The current IO reference count.
     */
    public int getIOReferenceCount();

    /**
     * Called by the IOMIManager to unload this objects memory resource thus
     * allowing GC to occur.
     */
    public void unLoadIO();

    /**
     * Returns the IOMIClassName for this IOMIInteface object.
     *
     * @return The IOMIClassName for this IOMIInteface object.
     */
    public String getIOMIClassName();

    /**
     * Returns the allocated memory for this object.
     *
     * @return The allocated memory for this object.
     */
    public long getAllocatedMemory();

    /**
     * Sets the no reference tag (a time stamp). Used by the IOMIManager cache
     * to store non-referenced objects in a Least Recently Used (LRU) cache.
     *
     * @param tag The tag (time stamp).
     */
    public void setNoRefTag(long tag);

    /**
     * Returns the Least Recently Used (LRU) time stamp for this object.
     *
     * @return The Least Recently Used (LRU) time stamp for this object.
     */
    public long getNoRefTag();

    /**
     * Output toString().
     *
     * @return toString().
     */
    @Override
    public String toString();
}
