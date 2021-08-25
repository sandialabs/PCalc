package gov.sandia.gmp.iomi;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import gov.sandia.gmp.iomihistory.IOMIHistoryList;
import gov.sandia.gmp.iomihistory.IOMIHistoryState;
import gov.sandia.gmp.util.containers.linkedlist.EmbeddedLinkedList;
import gov.sandia.gmp.util.globals.Globals;

/**
 * This is the heart of the IOMI system that can control allocation and file IO
 * for any distributed parallel objects allocated by client tasks on Fabric
 * distributed servers (hosts). This object maintains a map of all such objects
 * that inherit the IOMI Interface (IOMIInterface) which provides the necessary
 * behavior to control memory allocation and file IO operations. The status of
 * the manager is kept in the IOMIHostState object which defines all of the
 * pertinent statistics for analysis on the client side. Each host instantiates
 * a single instance (singleton) of this object to provide the aforementioned
 * control.
 * <p>
 * The primary methods supported by this object include the following:
 * <p>
 * IOMIInterface getIOReference     (IOMIFactoryInterface iomifctry, int i);
 * void          removeIOReference  (IOMIFactoryInterface iomifctry,
 * IOMIObjectInterface psm);
 * void          processIO          (IOMIInterface ior, boolean request);
 * void          checkCache         ();
 * <p>
 * where getIOReference returns a new or previously referenced object to a
 * calling application, removeIORefence releases the reference, and processIO
 * requests permission to perform a file IO operation that includes read, write,
 * rename, and delete. The method checkCache is used to determine if preset
 * memory storage limits are being exceeded in which case Least-Recently-Used
 * (LRU) cache objects are destroyed to free resource. These three public
 * functions (and the private checkCache which is called by the public method
 * notifyIOAllocate when its load argument is true) constitute the extent of
 * all control exercised by the user. Generally, the application extends an
 * object that is both Memory and file IO Intensive (IOMI) where the child
 * implements an IOMIInterface. In addition a factory method is created that
 * implements an IOMIFactory interface which supplies the create function to
 * make new IOMI objects. The interface forces the definition of a reference
 * count for memory management and an IO "allow" flag to give ordered
 * permissions to the child IOMI object to perform file IO operations.
 * <p>
 * In addition to managing IOMI object creation, this manager also controls
 * when the object is released back into the memory pool for collection by the
 * GC. Once an object is completely dereferenced it is added to an internal
 * memory cache in case it should be requested again by another client task.
 * This allows reusable objects to be re-referenced quickly in a loaded state
 * instead of having to be recreated and re-populated, which can be expensive.
 * If, however, memory resource exceeds a user defined preset fraction of the
 * total available memory defined on the host, all of the available non-
 * referenced objects are considered for unloading and subsequent re-absorption
 * back into the memory pool. The LRU objects are released first in a time-wise
 * sequential order up to the most recently used. If an applications referenced
 * objects require all of the available memory resource then all of the
 * non-referenced cached objects will be released before an out-of-memory
 * exception is thrown.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class IOMIManager implements Serializable {
    /**
     * The singleton IOMIManager on a host.
     */
    private static IOMIManager aIOMIManager = null;

    /**
     * Outputs debug information if true.
     */
    private static boolean aDebug = false;

    public static synchronized void setDebug(boolean dbg) {
        aDebug = dbg;
    }

    /**
     * Static singleton constructor.
     *
     * @return Singleton IOMIManager for a single host.
     * @throws IOException
     */
    public static synchronized IOMIManager getManager() throws IOException {
        if (aIOMIManager == null) {
            aIOMIManager = new IOMIManager();
        }
        return aIOMIManager;
    }

    public static synchronized void resetManager() {
        aIOMIManager = null;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * The IO wait queue. Contains IOMIInterface objects waiting to process their
     * IO request.
     */
    private Queue<IOMIInterface> aIOWaitQueue = null;

    /**
     * The IOMIInterface object set currently executing their IO process request.
     */
    private HashSet<IOMIInterface> aIOProcSet = null;

    /**
     * The map of all currently instantiated IOMIInterface objects.
     */
    private HashMap<String, HashMap<Integer, IOMIInterface>> aIORefMap = null;

    /**
     * The host state information object including task times, process times,
     * GC calls and times, and IOMIObjectState containers.
     */
    private IOMIHostState aIOMIHostState = null;

    /**
     * Map of allocated IOMI objects that are no longer referenced but are
     * cached in this map (associated with the time they were cached) for later
     * use. If re-accessed the object reference count is incremented and it is
     * removed from this cache map. If too much memory is used (during a create
     * call in processIOReference(...)) non-referenced objects in this cache may
     * be permanently unloaded (requiring a recreate and reload if accessed in
     * the future) to regain memory. If so they are unloaded in a Least Recently
     * Used (LRU) fashion.
     */
    private EmbeddedLinkedList<IOMIInterface> aIOMIObjectCacheList = null;

    /**
     * List of objects that have been unloaded. Used to test for reloads to gauge
     * cache efficiency.
     */
    private HashSet<Integer> aIOMIObjectUnloadList = null;

    /**
     * Limits the amount of cache objects that can be stored simultaneously in
     * the IOMIManager. Note: The total number of objects referenced could
     * exceed this number without any objects being set into the cache store.
     * The more active threads the higher the number of referenced, but non-
     * cached, objects. This is strictly a secondary limit on how many objects
     * can be stored in cache awaiting reuse. The primary limit should always be
     * aMaxMemUseFraction which removes cached objects based on memory use
     * criteria.
     */
    private int aMaxCacheObjectCount = 500;

    /**
     * The maximum memory fraction (of the available memory) allowed after which
     * cached IOMI objects in the cache list (aIOMIObjectCacheList) are destroyed.
     */
    private double aMaxMemUseFraction = .75;

    /**
     * The lower memory fraction (of the available memory) limit used to stop
     * the unloading of cached IOMI objects when less than this fraction of
     * memory is used.
     */
    private double aMinMemUseFraction = .67;

    /**
     * The number of simultaneously allowed IO operations by this host. These
     * can be file reads, writes, renames, or deletes.
     */
    private int aSimultaneousIOOpsLmt = 8;

    /**
     * The total number of IOMIInterface objects created by this IOMIManager.
     */
    private int aInstanceCount = 0;

    /**
     * The total number of IOMIInterface objects unloaded by this IOMIManager.
     */
    private int aUnloadCount = 0;

    /**
     * If aDebug is true this represents the IOMIInterface object new instance
     * frequency that causes a screen dump of the current load status of
     * aIORefMap.
     */
    private int aRefDumpFreq = 20;

    /*
     * Manages the construction and output of object history if requested.
     */
    private IOMIHistoryList aObjHistory = null;

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     *
     * @throws IOException
     */
    protected IOMIManager() throws IOException {
        String hostname = (InetAddress.getLocalHost()).getHostName();
        if (hostname.indexOf(".") > -1)
            hostname = hostname.substring(0, hostname.indexOf("."));

        aIOMIHostState = new IOMIHostState(hostname);

        aIORefMap = new HashMap<String, HashMap<Integer, IOMIInterface>>();
        aIOWaitQueue = new LinkedList<IOMIInterface>();
        aIOProcSet = new HashSet<IOMIInterface>();
        aIOMIObjectCacheList = new EmbeddedLinkedList<IOMIInterface>();
        aIOMIObjectUnloadList = new HashSet<Integer>();
    }

    /**
     * Turns on object history output.
     *
     * @param historyPath The path into which history files are written.
     * @throws IOException
     */
    public synchronized void turnOnObjectHistory(String historyPath)
            throws IOException {
        aObjHistory = new IOMIHistoryList(historyPath,
                aIOMIHostState.getHostName());
    }

    /**
     * Sets the simultaneous file IO operations limit.
     *
     * @param sioolmt The new simultaneous file IO operations limit.
     */
    public synchronized void setSimultaneousIOOpsLimit(int sioolmt) {
        aSimultaneousIOOpsLmt = sioolmt;
    }

    /**
     * Sets the min and max cache memory limits. Instantiated objects that are no
     * longer referenced are saved in a cache store until they are reused or the
     * cache memory execeeds the maximum limit set here. If the limit is exceeded
     * instantiated objects are unloaded (dereferenced) until the total cache
     * memory drops below the minimum limit set here. The limits are fractions
     * (0.0 to 1.0) of the total memory available. The Least-Recently-Used (LRU)
     * objects are unloaded first.
     *
     * @param mx            The maximum available memory used fraction before objects are
     *                      unloaded (starts the unload).
     * @param mn            The cache memory limit below which objects are no longer unloaded
     *                      if a cache memory unload was in progress (stops the unload).
     * @param mmuschkcache  The max memory check cache limit.
     * @param mxCacheObjCnt The max number of cache objects stored simultaneously. Once this
     *                      limit is reached new ones are added to the beginning of cache
     *                      and old ones are removed from the end.
     */
    public synchronized void setMaxAndMinCacheMemoryLimits(double mx, double mn,
                                                           int mxCacheObjCnt) {
        aMaxMemUseFraction = mx;
        aMinMemUseFraction = mn;
        aMaxCacheObjectCount = mxCacheObjCnt;
    }

    /**
     * Returns the host state to the caller.
     *
     * @return The host state to the caller.
     */
    public IOMIHostState getState() {
        aIOMIHostState.getGCTiming().update();
        return aIOMIHostState;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the IOReference associated with the index i. If this object doesn't
     * exist it is created with a call to create(...). If it does exist its
     * reference count is incremented before returning it to the caller.
     */
    public IOMIInterface getIOReference(IOMIFactoryInterface iomifctry, int i)
            throws IOException {
        return processIOReference(iomifctry, i, null);
    }

    /**
     * Dereferences the input IOReferenceObject. If the reference removal results
     * in a reference count of zero the object is removed from the internal map
     * (aIORefMap), its unLoadIO() method is called ... the object is now free
     * to be garbage collected.
     */
    public void removeIOReference(IOMIFactoryInterface iomifctry,
                                  IOMIObjectInterface psm)
            throws IOException {
        processIOReference(iomifctry, 0, psm);
    }

    /**
     * Synchronized call to process getIOReference(i) and removeIOReference(...)
     * methods.
     *
     * @param i       IOReference object to be returned or created.
     * @param iomiobj IOReferenceObject whose reference counted will be decremented.
     * @return The IOReference associated with i.
     * @throws IllegalStateException
     */
    private synchronized IOMIInterface
    processIOReference(IOMIFactoryInterface iomifctry, int i,
                       IOMIObjectInterface iomiobj)
            throws IOException {
        // see if this is a get or remmove

        if (iomiobj == null) {
            return addObjectReference(iomifctry, i);
        } else {
            removeObjectReference(iomifctry, iomiobj);
            return null;
        }
    }

    /**
     * Responsible for retrieving/creating a new IOMIInterface object and
     * incrementing its reference count. The function first checks the primary
     * storage map aIORefMap to see if the object exists. The objects class name
     * and unique IOMI index (input in this function) define its key.
     * <p>
     * If the object does not exist in aIORefMap then it has not yet been
     * instantiated. This function instantiates a new IOMIInterface object by
     * calling the create function of the input IOMIFactoryInteface object. The
     * new object is added to the map and its reference count is incremented.
     * <p>
     * If the object exists in aIORefMap its reference count is checked to see if
     * it is referenced (non-zero reference count). If the reference count is
     * zero then this is a non-referenced cached object. In that case the object
     * is removed from the cache map and its reference count is incremented.
     * <p>
     * Finally, if the object exists and its reference count is already non-zero
     * it is simply incremented by one more. In all cases the new or existing
     * object is returned to the caller ready for use.
     *
     * @param iomifctry The factory that can create a new IOMIInterface of the
     *                  type for which the factory was made to create.
     * @param i         The unique IOMI index.
     * @return The requested object with an incremented reference count.
     */
    private IOMIInterface addObjectReference(IOMIFactoryInterface iomifctry,
                                             int i) throws IOException {
        HashMap<Integer, IOMIInterface> factoryMap;

        // get ... see if an object resides in the map associated with the
        // input factory and index

        factoryMap = aIORefMap.get(iomifctry.getClassName());
        if (factoryMap == null) {
            // none defined for the factory ... create a new one and add it to
            // the map

            factoryMap = new HashMap<Integer, IOMIInterface>();
            aIORefMap.put(iomifctry.getClassName(), factoryMap);
        }

        // see if the index is defined in the factory map

        IOMIInterface iomii = factoryMap.get(i);
        if (iomii == null) {
            // see if this object has been loaded before

            if (aIOMIObjectUnloadList.contains(i)) {
                aIOMIObjectUnloadList.remove(i);
                aIOMIHostState.incrementIOMIObjectReloadCount();
                addHistory(IOMIHistoryState.RELOAD, i);
            }

            // create a new instance from the factory and add it to the map

            if (aDebug) System.out.println("      Instantiating IO " + i);
            iomii = iomifctry.create(i, this, aDebug);
            factoryMap.put(i, iomii);
            ++aInstanceCount;
            addHistory(IOMIHistoryState.CREATE, i);
        } else if (!iomii.isIOReferenced()) {
            // is defined but not referenced ... must be in cache ... remove it

            if (aDebug) System.out.println("      Re-referencing IO " + i);

            if ((iomii.getNextElement() == null) &&
                    (iomii.getPrevElement() == null) &&
                    (aIOMIObjectCacheList.size() > 1))
                outputError("IOMI Object NULL Cache Pointers:", iomii);

            aIOMIObjectCacheList.remove(iomii);
            addHistory(IOMIHistoryState.REMOVECACHEREREF, i);
            iomii.setNoRefTag(-1);
            aIOMIHostState.setIOMIObjectCacheEntries(aIOMIObjectCacheList.size());
            aIOMIHostState.incrementIOMIObjectCacheReuseCount();
        }

        // increment the IOMIInterface reference count

        iomii.addIOReference();
        addHistory(IOMIHistoryState.ADDREFERENCE, i);
        aIOMIHostState.incrementReferenceCount(iomii.getIOMIClassName());

        // dump the reference map if debug is true and dump frequency count has
        // been met

        if (aDebug && (aInstanceCount % aRefDumpFreq == 0))
            System.out.println(dumpReferenceMap("   "));

        // return object to the caller

        return iomii;
    }

    /**
     * Responsible for removing a reference on the input IOMIObjectInterface
     * object. If after decrementing the reference and the objects reference
     * count is still larger than zero then the function simply returns. If,
     * however, the reference count has gone to zero then the object is added to
     * the memory cache map for later reuse should such a request arise.
     *
     * @param iomifctry The IOMIFactoryInterface object that created the input
     *                  object and contains its class name information used to
     *                  find the object in the containing map (aIORefMap).
     * @param iomiobj   The IOMIObjectInterface object whose reference count will
     *                  be decremented. Only used to ensure that the contained
     *                  object and the input object are one in the same.
     * @throws IOException
     * @throws IllegalStateException
     */
    private void removeObjectReference(IOMIFactoryInterface iomifctry,
                                       IOMIObjectInterface iomiobj) throws IOException
    //throws IllegalStateException

    {
        HashMap<Integer, IOMIInterface> factoryMap;

        // remove reference ... throw an error if the input factory is not
        // contained in the map

        factoryMap = aIORefMap.get(iomifctry.getClassName());
        if (factoryMap == null) {
            String s = "Error: IOMIFactoryInterface (" + iomifctry.getClassName() +
                    ") ..." + NL +
                    "       Is not contained in the shared IOMIInterface " +
                    "container ..." + NL;
            throw new IllegalStateException(s);
        }

        // throw an error if the input IOReferenceObject is not contained
        // in the map

        IOMIInterface ior = factoryMap.get(iomiobj.getIOIndex());
        if (ior == null) {
            String s = "Error: IOObjectInterface index (" + iomiobj.getIOIndex() +
                    ") ..." + NL +
                    "       Is not contained in the shared IOMIInterface " +
                    "container ..." + NL;
            throw new IllegalStateException(s);
        }

        // Throw an error if the object in the map and the input object are
        // different

        if (ior != iomiobj) {
            String s = "Error: Input IOReferenceObject: " + NL +
                    iomiobj.getIOIndex() + NL +
                    "       Is not the same as the mapped IOReference object: " +
                    NL + ior.getIOIndex() + NL;
            throw new IllegalStateException(s);
        }

        // Throw an error if the IOReference object is not referenced

        if (!ior.isIOReferenced()) {
            String s = "Error: Input IOReference: " + NL +
                    iomiobj.getIOIndex() + NL +
                    "       Has a zero reference count ... " + NL;
            throw new IllegalStateException(s);
        }

        // all good ... remove a reference and see if the IOReference object is
        // no longer referenced.

        ior.removeIOReference();
        addHistory(IOMIHistoryState.REMOVEREFERENCE, ior.getIOIndex());
        aIOMIHostState.decrementReferenceCount(ior.getIOMIClassName());
        if (!ior.isIOReferenced()) {
            // no longer referenced ... determine outcome

            if (aIOMIObjectCacheList.size() >= aMaxCacheObjectCount) {
                // cache is full add this one to the beginning and remove and unload
                // the last one

                addIOMIObjectToCache(ior);
                removeLastFromCacheAndDestroy(0);
            } else if (aIOMIHostState.isIOMIManagerStressed()) {
                // host is memory stressed ... unload and remove from reference map

                removeRefMapObject(ior);
                destroyIOMIObject(ior);
            } else {
                // ok to add to cache

                addIOMIObjectToCache(ior);
                aIOMIHostState.setIOMIObjectCacheEntries(aIOMIObjectCacheList.size());
            }
        }
    }

    /**
     * Adds a new operation to the object history file.
     *
     * @param op    The operation to be added.
     * @param index The IOMI object index to be added.
     * @throws IOException
     */
    private void addHistory(IOMIHistoryState op, int index)
            throws IOException {
        if (aObjHistory != null) aObjHistory.add(op, index);
    }

    /**
     * Adds a new operation to the object history file.
     *
     * @param op    The operation to be added.
     * @param index The IOMI object index to be added.
     * @param amt   An optional long amount (time/memory) associated with the
     *              operation.
     * @throws IOException
     */
    private void addHistory(IOMIHistoryState op, int index, long amt)
            throws IOException {
        if (aObjHistory != null) aObjHistory.add(op, index, amt);
    }

    /**
     * Writes the final object history file.
     *
     * @throws IOException
     */
    public synchronized void writeFinalObjectHistoryFile() throws IOException {
        if (aObjHistory != null) aObjHistory.writeFinalFile();
    }

    /**
     * Removes the last entry from the cache, from the primary map, and destroys
     * the object. The amount of recovered memory (bytes) is returned to the
     * caller.
     *
     * @return The amount of recovered memory (bytes).
     * @throws IOException
     */
    private long removeLastFromCacheAndDestroy(int ityp) throws IOException {
        // remove object and see if it is not null

        long mem = 0;
        IOMIInterface ii = aIOMIObjectCacheList.removeLast();
        if (ii != null) {
            // set reference tag, get memory, remove from primary map, and destroy

            if (ityp == 0)
                addHistory(IOMIHistoryState.REMOVECACHECNTLMT, ii.getIOIndex());
            else if (ityp == 1)
                addHistory(IOMIHistoryState.REMOVECACHEMEMLMT, ii.getIOIndex());

            ii.setNoRefTag(-1);
            mem = ii.getAllocatedMemory();
            removeRefMapObject(ii);
            destroyIOMIObject(ii);
        }

        // return memory

        return mem;
    }

    /**
     * Adds the input object to the cache list (not referenced).
     *
     * @param ior The input object to be added to the cache list.
     * @throws IOException
     */
    private void addIOMIObjectToCache(IOMIInterface ior) throws IOException {
        long tag = (new Date()).getTime();
        ior.setNoRefTag(tag);
        // cache add index time
        aIOMIObjectCacheList.addFirst(ior);
        addHistory(IOMIHistoryState.ADDCACHE, ior.getIOIndex());
        if (aDebug) System.out.println("      Cacheing IO " + ior.getIOIndex());
    }

    /**
     * Unloads and destroys the input object. Adds the object IOIndex to the
     * unload list.
     *
     * @param ior The object to be unloaded and destroyed.
     * @throws IOException
     */
    private void destroyIOMIObject(IOMIInterface ior) throws IOException {
        aIOMIObjectUnloadList.add(ior.getIOIndex());
        ior.unLoadIO();
        ++aUnloadCount;
        addHistory(IOMIHistoryState.DESTROY, ior.getIOIndex());

        if (aDebug) System.out.println("      Unloaded IO " + ior.getIOIndex());
    }

    /**
     * Checks the cache of non-referenced IOMI objects to see if the total
     * allocated memory exceeds the maximum memory use fraction (aMaxMemUseFraction)
     * If it does the Least-Recently-Used (LRU) non-referenced objects are
     * unloaded until the total memory use is less than the minimum fraction
     * (aMinMemUseFraction).
     *
     * @throws IOException
     */
    private synchronized void checkCache() throws IOException {
        // reset the check cache flag and stress state if they were true

        aIOMIHostState.setStressState(false);

        // get maximum  available memory and used memory and calculate use fraction

        double frcUsed = aIOMIHostState.updateMemoryUse();
        addHistory(IOMIHistoryState.VMMEMORYCHECK, -1,
                aIOMIHostState.getVMUsedMemory());

        // if use fraction exceeds maximum memory fraction setting and objects are
        // contained in the cache unload some (least recently used first) to help
        // reduce the memory load

        if (frcUsed > aMaxMemUseFraction) {
            if (aIOMIObjectCacheList.size() > 0) {
                // Unload down to minimum memory use fraction if any are available

                long mem = 0;
                if (aDebug) System.out.println("      Used Memory Fraction = " +
                        frcUsed);

                // unload cache until minimum memory limit is satisfied

                long usedMem = aIOMIHostState.getVMUsedMemory();
                long maxMem = aIOMIHostState.getVMMaximumMemory();
                while ((aIOMIObjectCacheList.size() > 0) &&
                        (double) (usedMem - mem) / maxMem > aMinMemUseFraction) {
                    // increment the memory to be freed and add to unload set

                    mem += removeLastFromCacheAndDestroy(1);
                }

                // call GC if objects were destroyed

                if (mem > 0) {
                    aIOMIHostState.setIOMIObjectCacheEntries(aIOMIObjectCacheList.size());
                    addHistory(IOMIHistoryState.GCSTART, -1);
                    long recvrdMemory = aIOMIHostState.gc();
                    addHistory(IOMIHistoryState.GCEND, -1, recvrdMemory);
                    frcUsed = aIOMIHostState.getVMUsedMemoryFraction();
                }
            }

            // If the memory fraction still exceeds the maximum use limit after
            // cache removal then there is nothing else that the manager can do to
            // alleviate the resource stress ... set the memory stress flag to
            // notify the client

            if (frcUsed > aMaxMemUseFraction) aIOMIHostState.setStressState(true);
        } // end if (frcUsed > aMaxMemUseFraction)
    }

    /**
     * Called to remove an object from the IO reference map (aIORefMap).
     *
     * @param ii The object to remove.
     */
    private void removeRefMapObject(IOMIInterface ii) {
        HashMap<Integer, IOMIInterface> ifaci;

        // get the factory map of objects and remove the object from the factory
        // map ... if empty remove the factory map from aIORefMap

        ifaci = aIORefMap.get(ii.getIOMIClassName());
        ifaci.remove(ii.getIOIndex());
        if (ifaci.isEmpty()) {
            aIORefMap.remove(ii.getIOMIClassName());
        }
    }

    /**
     * Processes scheduled IOReference object for IO requests and completions.
     * For completions, if less than aSimultaneousIOOpsLmt objects are currently
     * processing IO operations the requesting object is also allowed and added
     * to the process set (aIOProcSet). Otherwise, the object is added to the wait
     * queue until it can be processed with fewer than aSimultaneousIOOpsLmt other
     * objects simultaneously processing IO operations. If this is a removal the
     * object is removed from the process set (aIOProcSet) and the wait queue is
     * checked to see if any other requesting objects are waiting. If they are the
     * next entry in the queue is retrieved and its "allow" flag is set to true so
     * that it can begin IO processing.
     *
     * @param ior     The IOReference object requesting IO processing
     *                (request = true) or notifying this IOReferenceManager of
     *                the completion of the request (request = false).
     * @param request The flag indicating a request (true) or completion (false)
     *                of IO processing.
     * @throws IOException
     */
    public synchronized void processIO(IOMIInterface ior, boolean request) throws IOException {
        // see if this is a request or completion

        if (request) {
            // request ... see if it can be processed right away

            if (aIOProcSet.size() < aSimultaneousIOOpsLmt) {
                // process now ... add to process set and notify IOReference

                aIOProcSet.add(ior);
                ior.setAllowIO(true);
                addHistory(IOMIHistoryState.ALLOWIO, ior.getIOIndex());
                if (aDebug) System.out.println("        Allow IO " + ior.getIOIndex());
            } else {
                // must wait ... add to wait queue

                aIOWaitQueue.add(ior);
                addHistory(IOMIHistoryState.QUEUEIO, ior.getIOIndex());

                if (aDebug) System.out.println("        Queue IO " + ior.getIOIndex());
            }
        } else // completed
        {
            // completion ... remove the IOReference from the processing set and
            // reset its IO process flag to false

            aIOProcSet.remove(ior);
            ior.setAllowIO(false);
            if (aDebug) System.out.println("        Completed IO " + ior.getIOIndex());

            // get the next IOReference in the queue if any

            IOMIInterface iorNxt = aIOWaitQueue.poll();
            if (iorNxt != null) {
                // found one ... add to process set and notify IOReference that it
                // can process its request

                addHistory(IOMIHistoryState.REMOVEQUEUEIO, iorNxt.getIOIndex());
                aIOProcSet.add(iorNxt);
                iorNxt.setAllowIO(true);
                addHistory(IOMIHistoryState.ALLOWIO, iorNxt.getIOIndex());
                if (aDebug) System.out.println("        Allow IO (UnQueue) " +
                        iorNxt.getIOIndex());
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that a process has completed.
     *
     * @param processType The name of the process that completed.
     * @param time        The time it took to complete the process (msec).
     * @param cummulative If true the time is added to the cummulative ("Total")
     *                    process time.
     */
    public void notifyProcessTime(String processType, long time,
                                  boolean cummulative) {
        aIOMIHostState.updateProcessTime(processType, time, cummulative);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that a task has completed.
     *
     * @param time The time it took to complete the task (msec).
     */
    public void notifyTaskTime(long time) {
        aIOMIHostState.updateTaskTime(time);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it was created.
     *
     * @param type   The type (class name) of the object.
     * @param memory The instanced memory (bytes) of the object.
     */
    public void notifyCreate(String type, long memory) {
        aIOMIHostState.incrementInstanceMemory(type, memory);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it allocated significant memory (caused by a File "Read" operation
     * or IOMIObject "fill" function). If load is true then memory is checked to
     * see if it exceeds a limit in which case cache objects will need to be
     * removed.
     *
     * @param type   The type (class name) of the object.
     * @param memory The amount of memory (bytes) contained within the object.
     * @param load   True if this is a primary load allocation.
     * @throws IOException
     */
    public synchronized void notifyAllocate(String type, long memory, int index,
                                            boolean load) throws IOException {
        aIOMIHostState.incrementMemory(type, memory);
        if (load) {
            addHistory(IOMIHistoryState.LOADDATA, index);
            aIOMIHostState.incrementLoadCount(type);
            checkCache();
        }
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it was unloaded (destroyed any contained memory and was released
     * from all references to be re-absorbed by the GC).
     *
     * @param type      The type (class name) of the object.
     * @param memory    The amount of memory to be returned to the VM.
     * @param extraLoad A flag indicating the object being destroyed was composed
     *                  of two separate loads (e.g. MatrixBlock held its data and
     *                  the transpose of its data).
     */
    public synchronized void notifyDestroy(String type, long memory,
                                           boolean extraLoad) {
        aIOMIHostState.decrementInstanceMemory(type, memory, extraLoad);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it has completed a file "Read" operation.
     *
     * @param type     The type (class name) of the object.
     * @param srvrtag  The file server tag string that performed the file io.
     * @param memory   The amount of memory (bytes) read.
     * @param time     The time (msec) to perform the file IO operation.
     * @param failures The number of IO failures while performing the operation.
     * @throws IOException
     */
    public synchronized void notifyFileRead(String type, String srvrtag, int index,
                                            long memory, long time, int failures)
            throws IOException {
        addHistory(IOMIHistoryState.COMPLETEIOREAD, index);
        aIOMIHostState.updateFileRead(type, srvrtag, memory, time, failures);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it has completed a file "Write" operation.
     *
     * @param type     The type (class name) of the object.
     * @param srvrtag  The file server tag string that performed the file io.
     * @param memory   The amount of memory (bytes) written.
     * @param time     The time (msec) to perform the file IO operation.
     * @param failures The number of IO failures while performing the operation.
     */
    public synchronized void notifyFileWrite(String type, String srvrtag, int index,
                                             long memory, long time, int failures)
            throws IOException {
        addHistory(IOMIHistoryState.COMPLETEIOWRITE, index);
        aIOMIHostState.updateFileWrite(type, srvrtag, memory, time, failures);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it has completed a file "Rename" operation.
     *
     * @param type     The type (class name) of the object.
     * @param srvrtag  The file server tag string that performed the file io.
     * @param time     The time (msec) to perform the file IO operation.
     * @param failures The number of IO failures while performing the operation.
     */
    public synchronized void notifyFileRename(String type, String srvrtag, int index,
                                              long time, int failures)
            throws IOException {
        addHistory(IOMIHistoryState.COMPLETEIORENAME, index);
        aIOMIHostState.updateFileRename(type, srvrtag, time, failures);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it has completed a file "Delete" operation.
     *
     * @param type     The type (class name) of the object.
     * @param srvrtag  The file server tag string that performed the file io.
     * @param time     The time (msec) to perform the file IO operation.
     * @param failures The number of IO failures while performing the operation.
     */
    public synchronized void notifyFileDelete(String type, String srvrtag, int index,
                                              long time, int failures)
            throws IOException {
        addHistory(IOMIHistoryState.COMPLETEIODELETE, index);
        aIOMIHostState.updateFileDelete(type, srvrtag, time, failures);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it waited to perform a requested file IO operation.
     *
     * @param type The type (class name) of the object.
     * @param time The time (msec) waiting before performing the file IO
     *             operation.
     */
    public synchronized void notifyIOWait(String type, long time) {
        aIOMIHostState.updateIOWait(type, time);
    }

    /**
     * Called by an IOMIInterface object to notify the host state (aIOMIHostState)
     * that it was blocked while waiting for another thread to complete a file IO
     * operation.
     *
     * @param type  The type (class name) of the object.
     * @param time  The time (msec) spent blocking until access was allowed to
     *              access the memory content loaded by another thread.
     * @param index The IOMIObject index.
     * @throws IOException
     */
    public synchronized void notifyIOBlocking(String type, long time, int index)
            throws IOException {
        addHistory(IOMIHistoryState.THREADDELAY, index, time);
        aIOMIHostState.updateIOBlocking(type, time);
    }

    /**
     * Notifies the host of a possible object reuse (if refCount > 1).
     *
     * @param refCount The input reference count which is a reuse if > 1.
     */
    public void notifyObjectReferenceReuse(int refCount) {
        aIOMIHostState.incrementIOMIObjectReferenceReuse(refCount);
    }

    /**
     * Notifies the host of an IOMI object unload.
     */
    public void notifyObjectUnload() {
        aIOMIHostState.incrementIOMIObjectUnloadCount();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Comparator of IO object names.
     */
    public class compareNames implements Comparator<IOMIInterface> {
        @Override
        public int compare(IOMIInterface i1, IOMIInterface i2) {
            return i1.getIOMIClassName().compareToIgnoreCase(i2.getIOMIClassName());
        }
    }

    /**
     * Comparator of IO indexes.
     */
    public class compareIndex implements Comparator<IOMIInterface> {
        @Override
        public int compare(IOMIInterface i1, IOMIInterface i2) {
            if (i1.getIOIndex() < i2.getIOIndex())
                return 1;
            else if (i1.getIOIndex() > i2.getIOIndex())
                return -1;
            else
                return 0;
        }
    }

    /**
     * Returns a string containing a table with information for all IOMIInterface
     * objects contained in the primary map (aIORefMap). The table is sorted on
     * the primary object class name and then on it's IOMI index. Additional
     * information for each object includes it's reference count, cache tag, a
     * boolean which is true if it's currently non-referenced and contained in
     * the cache store, the amount of allocated memory, a boolean indicating if
     * it's currently waiting to process an IO request, and a boolean that is
     * true if it is processing an IO request.
     * <p>
     * Note: This function is currently only called in debug mode within the
     * function checkCache(). Doing so has frozen the map contents and the
     * many of the output columns of the returned table. However, the last
     * 3 columns (i.e. memory, in IO request wait queue, processing IO
     * request) may change based on other threads performing IO input or
     * output. IO input can change the amount of allocated memory.
     *
     * @param hdr The amount of space added the beginning of each line in the
     *            returned string.
     * @return A string containing a table with information for all IOMIInterface
     * objects contained in the primary map (aIORefMap).
     */
    private String dumpReferenceMap(String hdr) {
        // add all objects in the aIORefMap into an array list

        ArrayList<IOMIInterface> iomiList = new ArrayList<IOMIInterface>();
        synchronized (aIORefMap) {
            for (Map.Entry<String, HashMap<Integer, IOMIInterface>> e :
                    aIORefMap.entrySet()) {
                HashMap<Integer, IOMIInterface> iomiMap = e.getValue();
                for (Map.Entry<Integer, IOMIInterface> eiomi : iomiMap.entrySet())
                    iomiList.add(eiomi.getValue());
            }
        }

        // sort on names (primary), and IO index (secondary)

        Collections.sort(iomiList, new compareIndex());
        Collections.sort(iomiList, new compareNames());

        // define tabel title, column headers, and row and column alignment

        String title = "Instantitated IOMIInterface Objects";

        //            object   reference   Cache           Queued  Processing
        //    index    name      count      Tag    Memory  for IO      IO

        int ncols = 7;
        String[][] colHdr = {{"", "Object", "Reference", "Cache",
                "", "Queued", "Processing"},
                {"Index", "Name", "Count", "Tag",
                        "Memory", "for IO", "IO"}};

        Globals.TableAlignment ta = Globals.TableAlignment.LEFT;
        Globals.TableAlignment rowAlign = ta;
        Globals.TableAlignment[] colAlign = {ta, ta, ta, ta, ta, ta, ta};

        // Build the table data from the ordered array list

        int colspc = 2;
        String[][] data = new String[iomiList.size()][ncols];

        // initialize counts and loop over each entry in the sorted list

        int refCount = 0;
        int cacheCount = 0;
        int ioQueueCount = 0;
        int ioProcCount = 0;
        long totlMem = 0;
        for (int i = 0; i < iomiList.size(); ++i) {
            // get the next IOMI object and its string array of data entries

            IOMIInterface iomi = iomiList.get(i);
            String[] dataRow = data[i];

            dataRow[0] = String.format("%5d", iomi.getIOIndex());
            dataRow[1] = iomi.getIOMIClassName();
            int count = iomi.getIOReferenceCount();
            refCount += count;
            dataRow[2] = String.format("%3d", count);
            boolean inCache = iomi.getNoRefTag() != -1;
            if (inCache) {
                ++cacheCount;
                dataRow[3] = Globals.getTimeStamp(iomi.getNoRefTag());
            } else
                dataRow[3] = " -1 ";
            long mem = iomi.getAllocatedMemory();
            totlMem += mem;
            dataRow[4] = Globals.memoryUnit(mem);
            boolean inIOWaitQueue = aIOWaitQueue.contains(iomi);
            if (inIOWaitQueue) ++ioQueueCount;
            dataRow[5] = (inIOWaitQueue) ? "Yes" : "No";
            boolean inIOProcSet = aIOProcSet.contains(iomi);
            if (inIOProcSet) ++ioProcCount;
            dataRow[6] = (inIOProcSet) ? "Yes" : "No";
        }

        // output the table to the string s

        String s = "";
        s += Globals.makeTable(hdr, title, "", colHdr, colAlign, null,
                rowAlign, data, colspc);

        // add summary rows

        s += hdr + "Entry Count                  = " + iomiList.size() + NL +
                hdr + "Total Reference Count        = " + refCount + NL +
                hdr + "Total Cache Count            = " + cacheCount + NL +
                hdr + "Total IO Queue Count         = " + ioQueueCount + NL +
                hdr + "Total IO Process Count       = " + ioProcCount + NL +
                hdr + "Total IOMI Object Memory     = " +
                Globals.memoryUnit(totlMem) + NL +
                hdr + "Total IOMI Intances          = " + aInstanceCount + NL +
                hdr + "Total IOMI Unloads           = " + aUnloadCount + NL;

        // get total Virtual Machine memory and fraction thereof

        long maxmem = Runtime.getRuntime().maxMemory();
        long totlmem = Runtime.getRuntime().totalMemory();
        long freemem = Runtime.getRuntime().freeMemory();
        long usedmem = totlmem - freemem;
        long availmem = maxmem - usedmem;

        double totlfrc = 100.0 * totlmem / maxmem;
        double usedfrc = 100.0 * usedmem / maxmem;
        double availfrc = 100.0 * availmem / maxmem;

        // add VM memory information

        s += hdr + "Maximum VM Memory            = " +
                String.format("%10s", Globals.memoryUnit(maxmem)) + NL +
                hdr + "Total Allocated VM Memory    = " +
                String.format("%10s (%6.2f", Globals.memoryUnit(totlmem), totlfrc) +
                " %)" + NL +
                hdr + "Application Used Memory      = " +
                String.format("%10s (%6.2f", Globals.memoryUnit(usedmem), usedfrc) +
                " %)" + NL +
                hdr + "Application Available Memory = " +
                String.format("%10s (%6.2f", Globals.memoryUnit(availmem), availfrc) +
                " %)" + NL;

        // return the table.

        return s;
    }

//
///**
// * Called to check the memory state of the JVM. If the amount of used
// * memory exceeds the maximum memory use fraction (aMaxMemUseFraction) by
// * more than an additional fraction of aMaxMemUseCheckCacheFrctn then
// * the check cache flag is called
// */
//public synchronized void memoryCheck()
//{
//  // get maximum  available memory and used memory and calculate use fraction
//
//  long maxAvailMemory = Runtime.getRuntime().maxMemory();
//  long usedMemory     = Runtime.getRuntime().totalMemory() -
//                        Runtime.getRuntime().freeMemory();
//  double frcUsed      = (double) usedMemory / maxAvailMemory;
//
//  // if use fraction exceeds maximum memory fraction (+ a delta) then
//  // perform a garbage collection to see if the used memory can be reduced
//
//  if (frcUsed > aMaxMemUseFraction)
//  {
//    // garbage collect and get used memory fraction
//
//    usedMemory = aIOMIHostState.gc(false);
//    frcUsed    = (double) usedMemory / maxAvailMemory;
//    if (frcUsed > aMaxMemUseFraction)
//    {
//      // still have excessive memory usage. Set the stressed flag as nothing
//      // else can be done (will have to run with fewer threads if an OOM
//      //error occurs)
//
//      aIOMIHostState.setStressState(true);
//    }
//  }
//}

    private void outputError(String err, IOMIInterface iomii) throws IOException {
        System.out.println(err);
        String tst = Globals.getTimeStamp();
        System.out.println(tst);
        System.out.println("");
        String iis = iomii.toString();
        System.out.println(iis);
        System.out.println("");
        String refmap = dumpReferenceMap("    ");
        System.out.println(refmap);
        System.out.println("");

        //String fp = "\\\\thummper\\GNEM\\devlpool\\jrhipp\\IOMIManagerErrFile.txt";
        String fp = "\\\\thinmint\\data\\GMP\\LSINV\\IOMIManagerErrFile.txt";

        fp = convertWinFilePathToLinux(fp);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        writer.write(tst);
        writer.write(NL);
        writer.write(iis);
        writer.write(NL);
        writer.write(refmap);
        writer.write(NL);
        writer.close();
    }

    public static String convertWinFilePathToLinux(String inputFilePath) {
        if (inputFilePath == null) return null;
        if (inputFilePath.isEmpty()) return "";

        if (!System.getProperty("os.name").toLowerCase().contains("win")) {

            // only convert file path is OS is NOT Windows
            inputFilePath = inputFilePath.trim();

            // find index of third "\" by finding first instance of "\" after
            // skipping the first 2 characters ("\\")
            int thirdSlashIndex = inputFilePath.trim().indexOf('\\', 2);

            // only convert if the first part of the string also contains
            // a "." character.  This is necessary as this function may
            // get called more than once for the same string, and we only
            // want to modify it if the first portion contains
            // "server.sandia.gov" or "134.253.123.yyy".
            if (inputFilePath.startsWith("\\\\") && thirdSlashIndex >= 2
                    && inputFilePath.substring(0, thirdSlashIndex).contains(".")) {
                // create substring starting from the third slash
                // (this drops "\\server.sandia.gov" from the string)
                inputFilePath = inputFilePath.substring(thirdSlashIndex);
            }

            // replace all "\" with "/"
            inputFilePath = inputFilePath.replace('\\', '/');
        }

        return inputFilePath;
    }
}
