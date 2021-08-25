package gov.sandia.gmp.iomi;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.gmp.iomi.IOMIObjectState.TableUnit;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.gctiming.GCTiming;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.statistics.Statistic;

/**
 * Used to hold the performance state of a single host running under the Fabric
 * distributed parallel resource manager. More than one type of IOMIInterface
 * object is supported at one time. This allows for the management of several
 * object types in a simultaneous fashion.
 * <p>
 * The host state contains a host name, start time, last update time, and a
 * host set (If it is an cumulative state ... see below). It also
 * maintains a total task time statistic and a string based categorization of
 * process times. These are called in an application to update total task time
 * and specific process times of interest. It is the application clients
 * responsibility to ensure the times that are input represent the specified
 * processes.
 * <p>
 * In addition to task and process times, garbage collection process times are
 * stored in a GCTiming object. This object maintains a list of all GC types
 * (their JVM assigned names) associated with their call frequency and the
 * amount of time (msec) spent in each. A "total" category type is also
 * supported that represents the sum of all GC types called.
 * <p>
 * Finally, a map of all IOMIObjectState supported for this IOMIHostState is
 * maintained. An application can add and manipulate as many as necessary.
 * <p>
 * The host state may be a cumulative host where several hosts are agglomerated
 * together to form a superset of hosts that all possess a common attribute
 * (e.g. all hosts that have the same Xeon type specification for their
 * processor set, or all hosts are agglomerated into the set "ALL" which
 * represents the total summary of all hosts used in an application). Functions
 * are provided to add consecutive host states to this one to form agglomerates.
 * <p>
 * Functions to write and read this host state to and from disk are provided
 * so that the host state can be saved for later retrieval. Also, toString()
 * methods are given so that the host state can be output for examination.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class IOMIHostState implements Serializable {
    /**
     * The name of this host state.
     */
    private String aHostName = "";

    /**
     * The set of all hosts used to make this host state a cumulative type
     * host state. If this host state is not a cummulative host state it will
     * contain a single entry (the host name).
     */
    private HashSet<String> aHostSet = null;

    /**
     * Set by the IOMIManager when it it's memory limits are being exceeded and
     * it has no items in cache to recover extra memory resource.
     */
    private boolean aManagerStress = false;

    /**
     * The time at which this host was created (msec).
     */
    private long aStartTime = 0;

    /**
     * The last time this host state was modified (msec).
     */
    private long aLastUpdateTime = 0;

    /**
     * The total task time statistic (values input in msec).
     */
    private Statistic aTaskTime = new Statistic();

    /**
     * The map of all process types associated with their timing statistic
     * (times in msec).
     */
    private ConcurrentHashMap<String, Statistic> aProcessTime = null;

    /**
     * A sorted map of of the requested output order of each process type input
     * into aProcessTime.
     */
    private TreeMap<Integer, String> aProcessOrder = null;

    /**
     * Identifies the maximum number of simultaneous threads using this
     * IOMIHostState. At any arbitrary instance the thread use count could be
     * less than this number but has never been larger.
     */
    private int aMaxThreadUseCount = 0;

    /**
     * The current number of threads using this IOMIHostState.
     */
    private int aCurrentThreadUseCount = 0;

    /**
     * The maximum number of entries in the IOMIManager cache list.
     */
    private int aMaxCacheEntries = 0;

    /**
     * The current number of entries in the IOMIManager cache list.
     */
    private int aCurrentCacheEntries = 0;

    /**
     * Counts the number of times a specific IOMIObject is reused. Once for each
     * cache removal and once for each reference above 1.
     */
    private int aIOMIObjectRefReuseCount = 0;

    /**
     * Counts the number of successful cache reuses (Object is re-referenced from
     * cache.
     */
    private int aIOMIObjectCacheReuseCount = 0;

    /**
     * Counts the number of times an IOMIObject is unloaded.
     */
    private int aIOMIObjectDestroyCount = 0;

    /**
     * Counts the number of times an IOMIObject is reloaded. (loaded -> used ->
     * cached -> destroyed -> reloaded)
     */
    private int aIOMIObjectReloadCount = 0;

    /**
     * The number of times the GC was called manually (function gc(...) below)
     * by the owning host.
     */
    private int aGCCountManual = 0;

    /**
     * The amount of time spent in the GC following a manual call (function
     * gc(...) below) by the owning host.
     */
    private long aGCTimeManual = 0;

    /**
     * The total recovered memory from calls to gc().
     */
    private long aGCRecvrdMemManual = 0;

    /**
     * The maximum memory defined for use by the virtual machine.
     */
    private long aVMMaxMemory = 0;

    /**
     * The amount of memory currently made available by the virtual machine for
     * the applications use. This is always less than or equal to aVMMaxMemory.
     */
    private long aVMTotalMemory = 0;

    /**
     * The amount of memory currently in use by the application.
     */
    private long aVMUsedMemory = 0;

    /**
     * The maximum amount of memory ever obseved in use by the application.
     */
    private long aVMMaxUsedMemory = 0;

    /**
     * The total amount of memory available for use by the application.
     */
    private long aVMAvailableMemory = 0;

    /**
     * The JVM reported GC statistics for each type of GC used, the number of
     * times it was called, and the amount of time spent in each. A total
     * category is also provided that combines all GC types into a single type.
     */
    private GCTiming aGCTiming = new GCTiming();

    /**
     * A map of all aIOMIObjectState objects supported by this host state.
     */
    private HashMap<String, IOMIObjectState> aIOMIObjectStateMap = null;

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Standard constructor called to form cumulative type host states.
     *
     * @param hostname The host state name.
     * @param strtTime The start time for the host state.
     */
    public IOMIHostState(String hostname, long strtTime) {
        aHostName = hostname;
        aStartTime = strtTime;
        aLastUpdateTime = aStartTime;
        aIOMIObjectStateMap = new HashMap<String, IOMIObjectState>();
        aProcessTime = new ConcurrentHashMap<String, Statistic>();
        aProcessOrder = new TreeMap<Integer, String>();
        aHostSet = new HashSet<String>();
        aHostSet.add(aHostName);
    }

    /**
     * Standard constructor.
     *
     * @param hostname The host state name.
     */
    public IOMIHostState(String hostname) {
        aHostName = hostname;
        aStartTime = (new Date()).getTime();
        aLastUpdateTime = aStartTime;
        aIOMIObjectStateMap = new HashMap<String, IOMIObjectState>();
        aProcessTime = new ConcurrentHashMap<String, Statistic>();
        aProcessOrder = new TreeMap<Integer, String>();
        aHostSet = new HashSet<String>();
        aHostSet.add(aHostName);
    }

    /**
     * Used to create a synchronized safe copy of the input IOMIHostState for
     * serialization and transport across the network.
     *
     * @param iomihs The input (and actively updated) IOMIHostState object that
     *               will be copied into this one.
     */
    public IOMIHostState(IOMIHostState iomihs) {
        // copy intrinsics

        synchronized (iomihs) {
            aHostName = iomihs.aHostName;
            aStartTime = iomihs.aStartTime;
            aLastUpdateTime = iomihs.aLastUpdateTime;
            aMaxThreadUseCount = iomihs.aMaxThreadUseCount;
            aCurrentThreadUseCount = iomihs.aCurrentThreadUseCount;
            aCurrentCacheEntries = iomihs.aCurrentCacheEntries;
            aMaxCacheEntries = iomihs.aMaxCacheEntries;
            aGCCountManual = iomihs.aGCCountManual;
            aGCTimeManual = iomihs.aGCTimeManual;
            aGCRecvrdMemManual = iomihs.aGCRecvrdMemManual;
            aVMMaxMemory = iomihs.aVMMaxMemory;
            aVMTotalMemory = iomihs.aVMTotalMemory;
            aVMAvailableMemory = iomihs.aVMAvailableMemory;
            aVMUsedMemory = iomihs.aVMUsedMemory;
            aVMMaxUsedMemory = iomihs.aVMMaxUsedMemory;
            aIOMIObjectDestroyCount = iomihs.aIOMIObjectDestroyCount;
            aIOMIObjectReloadCount = iomihs.aIOMIObjectReloadCount;
            aIOMIObjectRefReuseCount = iomihs.aIOMIObjectRefReuseCount;
            aIOMIObjectCacheReuseCount = iomihs.aIOMIObjectCacheReuseCount;
        }

        // copy host set

        if (iomihs.aHostSet != null)
            synchronized (iomihs.aHostSet) {
                aHostSet = new HashSet<String>();
                for (String h : iomihs.aHostSet) aHostSet.add(h);
            }

        // copy task time statistic

        synchronized (iomihs.aTaskTime) {
            aTaskTime.add(iomihs.aTaskTime);
        }

        // copy process time map

        if (iomihs.aProcessTime != null)
            synchronized (iomihs.aProcessTime) {
                aProcessTime = new ConcurrentHashMap<String, Statistic>();
                for (Map.Entry<String, Statistic> e : iomihs.aProcessTime.entrySet()) {
                    Statistic s = new Statistic();
                    synchronized (e.getValue()) {
                        s.add(e.getValue());
                    }
                    aProcessTime.put(e.getKey(), s);
                }
            }

        // copy process order map

        if (iomihs.aProcessOrder != null)
            synchronized (iomihs.aProcessOrder) {
                aProcessOrder = new TreeMap<Integer, String>();
                for (Map.Entry<Integer, String> e : iomihs.aProcessOrder.entrySet())
                    aProcessOrder.put(e.getKey(), e.getValue());
            }

        // update input GCTiming and copy into this GCTiming

        iomihs.aGCTiming.update();
        aGCTiming = new GCTiming(iomihs.aGCTiming);

        // copy all IOMIObjectState objects

        if (iomihs.aIOMIObjectStateMap != null)
            synchronized (iomihs.aIOMIObjectStateMap) {
                aIOMIObjectStateMap = new HashMap<String, IOMIObjectState>();
                for (Map.Entry<String, IOMIObjectState> e :
                        iomihs.aIOMIObjectStateMap.entrySet()) {
                    IOMIObjectState iomios = new IOMIObjectState(e.getValue());
                    aIOMIObjectStateMap.put(e.getKey(), iomios);
                }
            }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the set of all IOMIObjectState objects associated with this host
     * state defined by the input objectName. If empty all IOMIObjectState objects
     * are returned. Otherwise, the set contains only the requested object. If
     * the input name is not recognized and empty set is returned.
     *
     * @param objectName The name of the IOMIObjectState object to return in the
     *                   set. If "" all IOMIObjectState objects are returned.
     * @return The set of all IOMIObjectState objects associated with this host
     * state defined by the input objectName.
     */
    public HashSet<IOMIObjectState> getObjectSet(String objectName) {
        HashSet<IOMIObjectState> objectSet;
        objectSet = new HashSet<IOMIObjectState>();

        // add all but "ALL" object states to set if objectName = ""
        // otherwise only add objectName to set

        if (objectName.equals("")) {
            for (String objName : aIOMIObjectStateMap.keySet())
                if (!objName.equals("ALL"))
                    objectSet.add(aIOMIObjectStateMap.get(objName));
        } else
            objectSet.add(aIOMIObjectStateMap.get(objectName));

        // return set

        return objectSet;
    }

    public synchronized void setStressState(boolean stress) {
        aManagerStress = stress;
    }

    public boolean isIOMIManagerStressed() {
        return aManagerStress;
    }

    /**
     * Returns the parameter value associated with the input parameter name. If
     * the parameter name is not found -1 is returned. Supported names include
     * <p>
     * "GC Manual Count",
     * "GC Manual Time",
     * "GC Total Count",
     * "GC Total Time", and
     * "Process Overhead".
     *
     * @param paramName The name of the parameter to be returned.
     * @return The parameter value associated with the input parameter name.
     */
    public double getHostParameter(String paramName) {
        if (paramName.equals("GC Manual Count"))
            return aGCCountManual;
        else if (paramName.equals("GC Manual Time"))
            return aGCTimeManual;
        else if (paramName.equals("GC Total Recovered Memory"))
            return aGCRecvrdMemManual;
        else if (paramName.equals("Maximum VM Memory"))
            return aVMMaxMemory;
        else if (paramName.equals("Total Allocated VM Memory"))
            return aVMTotalMemory;
        else if (paramName.equals("Total Available VM Memory"))
            return aVMAvailableMemory;
        else if (paramName.equals("Total Used Memory"))
            return aVMUsedMemory;
        else if (paramName.equals("Maximum Observed Used Memory"))
            return aVMMaxUsedMemory;
        else if (paramName.equals("GC Total Count"))
            return aGCTiming.getTotalCollectionCount();
        else if (paramName.equals("GC Total Time"))
            return aGCTiming.getTotalCollectionTime();
        else if (paramName.equals("Process Overhead"))
            return getCummulativeOverheadTime();

        return -1;
    }

    /**
     * Returns the host state task or process time statistic. If paramName =
     * "Task" the task statistic is returned. Otherwise, the process time
     * statistic associated with paramName is returned. If no process time matches
     * the input string a null reference is returned.
     *
     * @param paramName "Task" for a task time statistic return or a process time
     *                  name.
     * @return The host state task or process time statistic.
     */
    public Statistic getHostStatistic(String paramName) {
        if (paramName.equals("Task"))
            return aTaskTime;
        else
            return aProcessTime.get(paramName);
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Reads this host state from the file at the input path.
     *
     * @param pathFileName The path/file from which this host state will be read.
     * @throws IOException
     */
    public void read(String pathFileName) throws IOException {
        FileInputBuffer fib = new FileInputBuffer(pathFileName);
        read(fib);
    }

    /**
     * Reads this host state from the input file input buffer.
     *
     * @param fib The file input buffer from which this host state will be read.
     * @throws IOException
     */
    public void read(FileInputBuffer fib) throws IOException {
        // read in host name, host set, start time, and last update time

        aHostName = fib.readString();
        int n = fib.readInt();
        aHostSet.clear();
        for (int i = 0; i < n; ++i)
            aHostSet.add(fib.readString());
        aStartTime = fib.readLong();
        aLastUpdateTime = fib.readLong();
        aCurrentThreadUseCount = fib.readInt();
        aMaxThreadUseCount = fib.readInt();
        aCurrentCacheEntries = fib.readInt();
        aMaxCacheEntries = fib.readInt();
        aIOMIObjectRefReuseCount = fib.readInt();
        aIOMIObjectDestroyCount = fib.readInt();
        aIOMIObjectReloadCount = fib.readInt();
        aIOMIObjectCacheReuseCount = fib.readInt();

        // read task time statistic

        aTaskTime.read(fib);

        // read process time statistic

        n = fib.readInt();
        aProcessTime.clear();
        for (int i = 0; i < n; ++i) {
            String procName = fib.readString();
            Statistic stat = new Statistic();
            stat.read(fib);
            aProcessTime.put(procName, stat);
        }

        // read process name order

        n = fib.readInt();
        aProcessOrder.clear();
        for (int i = 0; i < n; ++i) {
            int order = fib.readInt();
            String procName = fib.readString();
            aProcessOrder.put(order, procName);
        }

        // read GC timing information

        aGCCountManual = fib.readInt();
        aGCTimeManual = fib.readLong();
        aGCRecvrdMemManual = fib.readLong();
        aVMMaxMemory = fib.readLong();
        aVMTotalMemory = fib.readLong();
        aVMAvailableMemory = fib.readLong();
        aVMUsedMemory = fib.readLong();
        aVMMaxUsedMemory = fib.readLong();
        aGCTiming.clear();
        aGCTiming.read(fib);

        // read all associated object states

        n = fib.readInt();
        aIOMIObjectStateMap.clear();
        for (int i = 0; i < n; ++i) {
            String objectName = fib.readString();
            IOMIObjectState objectState = new IOMIObjectState(objectName);
            objectState.read(fib);
            aIOMIObjectStateMap.put(objectName, objectState);
        }
    }

    /**
     * Writes this host state to the file at the output path.
     *
     * @param pathFileName The path/file into which this host state will be
     *                     written.
     * @throws IOException
     */
    public void write(String pathFileName) throws IOException {
        FileOutputBuffer fob = new FileOutputBuffer(pathFileName);
        write(fob);
    }

    /**
     * Reads this host state from the input file input buffer.
     *
     * @param fob The file output buffer into which this host state will be
     *            written.
     * @throws IOException
     */
    public void write(FileOutputBuffer fob) throws IOException {
        // write the host name, host set, start time, and last update time

        fob.writeString(aHostName);
        fob.writeInt(aHostSet.size());
        for (String hostName : aHostSet)
            fob.writeString(hostName);
        fob.writeLong(aStartTime);
        fob.writeLong(aLastUpdateTime);
        fob.writeInt(aCurrentThreadUseCount);
        fob.writeInt(aMaxThreadUseCount);
        fob.writeInt(aCurrentCacheEntries);
        fob.writeInt(aMaxCacheEntries);
        fob.writeInt(aIOMIObjectRefReuseCount);
        fob.writeInt(aIOMIObjectDestroyCount);
        fob.writeInt(aIOMIObjectReloadCount);
        fob.writeInt(aIOMIObjectCacheReuseCount);

        // write the task time statistic

        aTaskTime.write(fob);

        // write all process time statistics

        fob.writeInt(aProcessTime.size());
        if (aProcessTime.size() > 0) {
            for (Map.Entry<String, Statistic> e : aProcessTime.entrySet()) {
                fob.writeString(e.getKey());
                e.getValue().write(fob);
            }
        }

        // write the process name output order

        fob.writeInt(aProcessOrder.size());
        if (aProcessOrder.size() > 0) {
            for (Map.Entry<Integer, String> e : aProcessOrder.entrySet()) {
                fob.writeInt(e.getKey());
                fob.writeString(e.getValue());
            }
        }

        // write the GC timing information

        fob.writeInt(aGCCountManual);
        fob.writeLong(aGCTimeManual);
        fob.writeLong(aGCRecvrdMemManual);
        fob.writeLong(aVMMaxMemory);
        fob.writeLong(aVMTotalMemory);
        fob.writeLong(aVMAvailableMemory);
        fob.writeLong(aVMUsedMemory);
        fob.writeLong(aVMMaxUsedMemory);
        aGCTiming.write(fob);

        // write each object state

        fob.writeInt(aIOMIObjectStateMap.size());
        for (Map.Entry<String, IOMIObjectState> e :
                aIOMIObjectStateMap.entrySet()) {
            fob.writeString(e.getKey());
            e.getValue().write(fob);
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a map of all object states supported by this host state associated
     * with a fractional percent result representing the value of the object
     * state for some File operation parameter of some server for some statistical
     * parameter scaled by the same file operation parameter for all servers
     * for the same statistical parameter. The parameter type can be "Total",
     * "Read", "Write", "Rename", or "Delete". The parameter unit can be "Size",
     * "Time", or "Rate". The statistic parameter type can be any of the entries
     * in Statistic.StatType including MINIMUM, MAXIMUM, ABS_MAXIMUM, MEAN,
     * STANDARD_DEVIATION, RMS, or SUM.
     *
     * @param srvrName  Any valid server name including "ALL".
     * @param paramType Any valid File operation including "Total", "Read",
     *                  "Write", "Rename", or "Delete".
     * @param unitType  Any valid File operation statistics type including
     *                  "Size", "Time", or "Rate".
     * @param st        Any valid Statistic.StatType including MINIMUM,
     *                  MAXIMUM, ABS_MAXIMUM, MEAN, STANDARD_DEVIATION, RMS,
     *                  or SUM.
     * @return A map of all object states supported by this host state associated
     * with a fractional percent result representing the value of the
     * object state for some File operation parameter of some server for
     * some statistical parameter scaled by the same file operation
     * parameter for all servers for the same statistical parameter.
     */
    public HashMap<String, Double> rankServers(String srvrName, String paramType,
                                               String unitType,
                                               Statistic.StatType st) {
        return rankServers(srvrName, paramType, unitType, paramType, st);
    }

    /**
     * Returns a map of all object states supported by this host state associated
     * with a fractional percent result representing the value of the object
     * state for some File operation parameter of some server for some statistical
     * parameter scaled by the "Total" file operation parameter for all servers
     * for the same statistical parameter. The parameter type can be "Total",
     * "Read", "Write", "Rename", or "Delete". The parameter unit can be "Size",
     * "Time", or "Rate". The statistic parameter type can be any of the entries
     * in Statistic.StatType including MINIMUM, MAXIMUM, ABS_MAXIMUM, MEAN,
     * STANDARD_DEVIATION, RMS, or SUM.
     *
     * @param srvrName  Any valid server name including "ALL".
     * @param paramType Any valid File operation including "Total", "Read",
     *                  "Write", "Rename", or "Delete".
     * @param unitType  Any valid File operation statistics type including
     *                  "Size", "Time", or "Rate".
     * @param st        Any valid Statistic.StatType including MINIMUM,
     *                  MAXIMUM, ABS_MAXIMUM, MEAN, STANDARD_DEVIATION, RMS,
     *                  or SUM.
     * @return A map of all object states supported by this host state associated
     * with a fractional percent result representing the value of the
     * object state for some File operation parameter of some server for
     * some statistical parameter scaled by the "Total" file operation
     * parameter for all servers for the same statistical parameter.
     */
    public HashMap<String, Double> rankServersWithTotal(String srvrName,
                                                        String paramType,
                                                        String unitType,
                                                        Statistic.StatType st) {
        return rankServers(srvrName, paramType, unitType, "Total", st);
    }

    /**
     * Returns the unique list of all IOMIObjectStates in the internal map. If
     * the map contains two entries then the "ALL" state is simply a duplicate
     * of the only entry. In that case only the unique single entry is returned.
     * If more than two entries are contained in the map then they are all
     * returned but the "ALL" entry is always placed as the first entry in the
     * list.
     *
     * @return The unique list of all IOMIObjectStates in the internal map.
     */
    public ArrayList<IOMIObjectState> getUniqueIOMIObjectStateList() {
        // create list ... add the cumulative "ALL" state if more than two entries
        // are contained in the map

        ArrayList<IOMIObjectState> iomiObjList = new ArrayList<IOMIObjectState>();
        if (aIOMIObjectStateMap.size() > 2)
            iomiObjList.add(aIOMIObjectStateMap.get("ALL"));

        // now add all of the non - "ALL" states

        for (Map.Entry<String, IOMIObjectState> e :
                aIOMIObjectStateMap.entrySet()) {
            if (!e.getKey().equals("ALL")) {
                iomiObjList.add(e.getValue());
            }
        }

        // done ... return list

        return iomiObjList;
    }

    /**
     * Returns a map of all object states supported by this host state associated
     * with a fractional percent result representing the value of the object
     * state for some File operation parameter of some server for some statistical
     * parameter scaled by the file operation parameter for all servers for the
     * same statistical parameter. The parameter type or total parameter type can
     * be "Total", "Read", "Write", "Rename", or "Delete". The parameter unit can
     * be "Size", "Time", or "Rate". The statistic parameter type can be any of
     * the entries in Statistic.StatType including MINIMUM, MAXIMUM, ABS_MAXIMUM,
     * MEAN, STANDARD_DEVIATION, RMS, or SUM.
     *
     * @param srvrName       Any valid server name including "ALL".
     * @param paramType      Any valid File operation including "Total", "Read",
     *                       "Write", "Rename", or "Delete".
     * @param unitType       Any valid File operation statistics type including
     *                       "Size", "Time", or "Rate".
     * @param totalParamType Any valid File operation including "Total", "Read",
     *                       "Write", "Rename", or "Delete".
     * @param st             Any valid Statistic.StatType including MINIMUM,
     *                       MAXIMUM, ABS_MAXIMUM, MEAN, STANDARD_DEVIATION, RMS,
     *                       or SUM.
     * @return A map of all object states supported by this host state associated
     * with a fractional percent result representing the value of the
     * object state for some File operation parameter of some server for
     * some statistical parameter scaled by the file operation parameter
     * for all servers for the same statistical parameter.
     */
    public HashMap<String, Double> rankServers(String srvrName,
                                               String paramType, String unitType,
                                               String totalParamType,
                                               Statistic.StatType st) {
        ConcurrentHashMap<String, Statistic> totlStats, valuStats;

        // create map to be returned

        HashMap<String, Double> rank = new HashMap<String, Double>();

        // get "ALL" cumulative object state and loop over each object state

        IOMIObjectState totalObjectState = aIOMIObjectStateMap.get("ALL");
        for (Map.Entry<String, IOMIObjectState> e :
                aIOMIObjectStateMap.entrySet()) {
            // only process a state if it is not the cumulative state

            if (!e.getKey().equals("ALL")) {
                // get object state map of statistics for each server and see if it
                // exists (is defined)

                String objectName = e.getKey();
                valuStats = e.getValue().getServerStatistic(paramType, unitType);
                if (valuStats != null) {
                    // defined ... get requested server statistic and see if it is defined

                    Statistic stat = valuStats.get(srvrName);
                    if (stat != null) {
                        // statistic is defined ... get requested statistic parameter and
                        // cumulative object parameter map for the total parameter type and
                        // then the result for the same server and statistics parameter

                        double valRslt = stat.getResult(st);
                        totlStats = totalObjectState.getServerStatistic(totalParamType,
                                unitType);
                        double totlRslt = totlStats.get(srvrName).getResult(st);

                        // add percent ratio of object type result with the cumulative
                        // total result to the map

                        rank.put(objectName, 100.0 * valRslt / totlRslt);
                    }
                }
            }
        }

        // done ... return ranking map

        return rank;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true if the input host name is contained in this host states host
     * set.
     *
     * @param hostname The host name to be checed for containment within this
     *                 host states host set.
     * @return True if the input host name is contained in this host states host
     * set.
     */
    public boolean containsHostInSet(String hostname) {
        return aHostSet.contains(hostname);
    }

    /**
     * Adds the input host name to the host set.
     *
     * @param hostname The host name to be added to the host set.
     */
    public void addHostToSet(String hostname) {
        aHostSet.add(hostname);
    }

    /**
     * Clears the host set.
     */
    public void clearHostSet() {
        aHostSet.clear();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the host name for which this IOMIHostState was instantiated.
     *
     * @return The host name for which this IOMIHostState was instantiated.
     */
    public String getHostName() {
        return aHostName;
    }

    /**
     * Returns the start time when this host state was created.
     *
     * @return The start time when this host state was created.
     */
    public long getStartTime() {
        return aStartTime;
    }

    /**
     * Returns the last time this host state was updated.
     *
     * @return The last time this host state was updated.
     */
    public long getLastUpdateTime() {
        return aLastUpdateTime;
    }

    /**
     * Returns the task time statistic.
     *
     * @return The task time statistic.
     */
    public Statistic getTaskTimeStatistic() {
        return aTaskTime;
    }

    /**
     * Returns the process time statistic associated with the input process name.
     * Null is returned if no association exists.
     *
     * @param processName The process name for which the associated process time
     *                    statistic will be returned.
     * @return The process time statistic associated with the input process name.
     */
    public Statistic getProcessTimeStatistic(String processName) {
        return aProcessTime.get(processName);
    }

    /**
     * Returns the cumulative process time statistic (associated with process name
     * "ALL"). Null is returned if no association exists.
     *
     * @return The cumulative process time statistic (associated with process name
     * "ALL").
     */
    public Statistic getCummulativeProcessTimeStatistic() {
        return aProcessTime.get("ALL");
    }

    /**
     * Returns the manual GC count. Number of times it was called initiated by
     * the application.
     *
     * @return The manual GC count.
     */
    public int getGCManualCallCount() {
        return aGCCountManual;
    }

    /**
     * Returns the manual GC process time. Time accumulated from a GC call
     * initiated by the application.
     *
     * @return The manual GC process time.
     */
    public long getGCManualCallTime() {
        return aGCTimeManual;
    }

    /**
     * Returns the manual GC process recovered memory (bytes). Total memory
     * accumulated from all calls to gc() initiated by the owning
     * IOMIManager.
     *
     * @return The manual GC process recovered memory (bytes).
     */
    public long getGCManualTotalRecoveredMemory() {
        return aGCRecvrdMemManual;
    }

    /**
     * Returns the maximum amount of memory contained by this virtual machine that
     * is available for use by applications.
     *
     * @return The maximum amount of VM memory (bytes).
     */
    public long getVMMaximumMemory() {
        return aVMMaxMemory;
    }

    /**
     * Returns the total allocated memory made available for use by this virtual
     * machine by requesting applications.
     *
     * @return The total allocated VM memory (bytes).
     */
    public long getVMTotalMemory() {
        return aVMTotalMemory;
    }

    /**
     * Returns the total amount of memory currently available (max - used) for
     * use by this virtual machine for requesting applications.
     *
     * @return The total amount of avaialble VM memory (bytes).
     */
    public long getVMAvailableMemory() {
        return aVMAvailableMemory;
    }

    /**
     * Returns the amount of VM memory used requesting applications.
     *
     * @return The total amount of VM memory used (bytes).
     */
    public long getVMUsedMemory() {
        return aVMUsedMemory;
    }

    /**
     * Returns the maximum observed used memory (bytes). Set after calling the
     * method updateMemoryUse().
     *
     * @return The maximum observed used memory (bytes).
     */
    public long getVMMaximumUsedMemory() {
        return aVMMaxUsedMemory;
    }

    /**
     * Returns the fraction of VM used memory.
     *
     * @return The fraction of VM used memory.
     */
    public synchronized double getVMUsedMemoryFraction() {
        return aVMUsedMemory / aVMMaxMemory;
    }

    /**
     * Returns the GCTiming object containing all GC call statistics.
     *
     * @return The GCTiming object containing all GC call statistics.
     */
    public GCTiming getGCTiming() {
        return aGCTiming;
    }

    /**
     * Returns a unique name used to distinguish this host state from another
     * instantiated at another time.
     *
     * @return A unique name used to distinguish this host state from another
     * instantiated at another time.
     */
    public String getUniqueName() {
        return aHostName + "(" + aStartTime + ")";
    }

    /**
     * Returns the maximum defined thread use count seen up to the point of
     * the call.
     *
     * @return The maximum defined thread use count.
     */
    public int getMaxThreadUseCount() {
        return aMaxThreadUseCount;
    }

    /**
     * Returns the current thread use count.
     *
     * @return The current thread use count.
     */
    public int getCurrentThreadUseCount() {
        return aCurrentThreadUseCount;
    }

    /**
     * Returns the maximum number of entries stored in the IOMIManager
     * IOMIObject cache seen up to the point of the call.
     *
     * @return The maximum number of entries stored in the IOMIManager
     * IOMIObject cache.
     */
    public int getMaximumCacheEntries() {
        return aMaxCacheEntries;
    }

    /**
     * Returns the current number of entries stored in the IOMIManager
     * IOMIObject cache.
     *
     * @return The current number of entries stored in the IOMIManager
     * IOMIObject cache.
     */
    public int getCurrentCacheEntries() {
        return aCurrentCacheEntries;
    }

    /**
     * Returns the number of IOMIObject reuses.
     *
     * @return The number of IOMIObject reuses.
     */
    public int getIOMIObjectReuseCount() {
        return aIOMIObjectRefReuseCount;
    }

    /**
     * Returns the number of IOMIObject unloads.
     *
     * @return The number of IOMIObject unloads.
     */
    public int getIOMIObjectUnloadCount() {
        return aIOMIObjectDestroyCount;
    }

    /**
     * Returns the number of IOMIObject reloads.
     *
     * @return The number of IOMIObject reloads.
     */
    public int getIOMIObjectReloadCount() {
        return aIOMIObjectReloadCount;
    }

    /**
     * Returns the number of IOMIObject cache reuse count.
     *
     * @return The number of IOMIObject cache reuse count.
     */
    public int getIOMIObjectCacheReuseCount() {
        return aIOMIObjectCacheReuseCount;
    }

    /**
     * Returns the maximum in-core instance memory allocated by this host state
     * at some point in its lifetime for the requested object name. Zero is
     * returned if the object name is not found.
     *
     * @param objectName The object name for which the maximum in-core instance
     *                   memory will be returned.
     * @return The maximum in-core instance memory allocated by this host state
     * at some point in its lifetime for the requested object name.
     */
    public long getMaxInstanceMemory(String objectName) {
        IOMIObjectState iormts = aIOMIObjectStateMap.get(objectName);
        if (iormts == null) return 0;

        return iormts.getMaximumInstanceMemory();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Adds a process type to the process time type order array. This will be a
     * header in the process time table that will be at position order in the
     * output rows.
     *
     * @param order The row position of the process type.
     * @param name  The name of this process type.
     */
    public void addProcessOrder(int order, String name) {
        aProcessOrder.put(order, name);
    }

    /**
     * Returns the ProcessOrder map which orders the ProcessTime Statistics.
     *
     * @return The ProcessOrder map which orders the ProcessTime Statistics.
     */
    public TreeMap<Integer, String> getProcessOrder() {
        return aProcessOrder;
    }

    /**
     * Adds the content of another host state to this one. This is called by the
     * IOMICumulativeHostState object to concatenate many hosts together into a
     * single grouping.
     *
     * @param iorms The input host state to be added to this one.
     */
    public void add(IOMIHostState iorms) {
        aIOMIObjectRefReuseCount += iorms.aIOMIObjectRefReuseCount;
        aIOMIObjectDestroyCount += iorms.aIOMIObjectDestroyCount;
        aIOMIObjectReloadCount += iorms.aIOMIObjectReloadCount;
        aIOMIObjectCacheReuseCount += iorms.aIOMIObjectCacheReuseCount;

        aCurrentCacheEntries += iorms.aCurrentCacheEntries;
        aCurrentThreadUseCount += iorms.aCurrentThreadUseCount;

        if (aMaxCacheEntries < iorms.aMaxCacheEntries)
            aMaxCacheEntries = iorms.aMaxCacheEntries;
        if (aMaxThreadUseCount < iorms.aMaxThreadUseCount)
            aMaxThreadUseCount = iorms.aMaxThreadUseCount;

        // Loop over all process time statistics and add the input process time
        // statistics to this one.

        for (Map.Entry<String, Statistic> e :
                iorms.aProcessTime.entrySet()) {
            // get next statistic and see if it is defined ... add it if not and
            // then add the process type

            Statistic stat = aProcessTime.get(e.getKey());
            if (stat == null) {
                stat = new Statistic();
                aProcessTime.put(e.getKey(), stat);
            }
            stat.add(e.getValue());
        }

        // add the process order

        for (Map.Entry<Integer, String> e :
                iorms.aProcessOrder.entrySet()) {
            aProcessOrder.put(e.getKey(), e.getValue());
        }

        // add GC timing information

        aTaskTime.add(iorms.aTaskTime);
        aGCCountManual += iorms.aGCCountManual;
        aGCTimeManual += iorms.aGCTimeManual;
        aGCRecvrdMemManual += iorms.aGCRecvrdMemManual;
        aVMMaxMemory += iorms.aVMMaxMemory;
        aVMTotalMemory += iorms.aVMTotalMemory;
        aVMAvailableMemory += iorms.aVMAvailableMemory;
        aVMUsedMemory += iorms.aVMUsedMemory;
        if (aVMMaxUsedMemory < iorms.aVMMaxUsedMemory)
            aVMMaxUsedMemory = iorms.aVMMaxUsedMemory;
        aGCTiming.add(iorms.aGCTiming);

        // loop over all IOMIObjectState entries in the input and add them to this
        // one

        for (Map.Entry<String, IOMIObjectState> e :
                iorms.aIOMIObjectStateMap.entrySet()) {
            // get object state from this map ... if not defined add it

            IOMIObjectState mngrTypeState = aIOMIObjectStateMap.get(e.getKey());
            if (mngrTypeState == null) {
                mngrTypeState = new IOMIObjectState(e.getKey());
                aIOMIObjectStateMap.put(e.getKey(), mngrTypeState);
            }

            // add input object state to this one

            mngrTypeState.add(e.getValue());
        }
    }

    /**
     * Clears all entries from this host state.
     */
    public void clear() {
        aProcessTime.clear();
        aProcessOrder.clear();
        aTaskTime.reset();
        aGCCountManual = 0;
        aGCTimeManual = 0;
        aGCRecvrdMemManual = 0;
        aVMMaxMemory = 0;
        aVMTotalMemory = 0;
        aVMAvailableMemory = 0;
        aVMUsedMemory = 0;
        aVMMaxUsedMemory = 0;
        aGCTiming.clear();
        aIOMIObjectStateMap.clear();
        aIOMIObjectRefReuseCount = 0;
        aIOMIObjectDestroyCount = 0;
        aIOMIObjectReloadCount = 0;
        aIOMIObjectCacheReuseCount = 0;
        aCurrentCacheEntries = 0;
        aMaxCacheEntries = 0;
        aCurrentThreadUseCount = 0;
        aMaxThreadUseCount = 0;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Zeros this host state current instance memory. Called by the cumulative
     * host state when more than one host state entry exists for the same host.
     * This can happen when a host drops and is restarted by Fabric. Only the
     * most recent host state (of the same name) is allowed to have current
     * instance memory so that cumulative groups, when added together, do not
     * show an erroneous excessive amount of memory allocation.
     */
    public void zeroCurrentInstanceMemory() {
        for (Map.Entry<String, IOMIObjectState> e :
                aIOMIObjectStateMap.entrySet()) {
            e.getValue().zeroCurrentInstanceMemory();
        }
    }

    /**
     * Sets this host state reference count for the input object name with the
     * input new reference count setting. If the object state for the input
     * object name does not exist it is created. A cumulative object state
     * is also created (if necessary) and updated.
     *
     * @param objectName The object state for which the reference count will be
     *                   set.
     * @param refCount   The current object reference count.
     */
    public void incrementReferenceCount(String objectName) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.incrementReferenceCount();
        iorms = getCreateObjectState("ALL");
        iorms.incrementReferenceCount();
        setUpdateTime();
    }

    /**
     * Sets this host state reference count for the input object name with the
     * input new reference count setting. If the object state for the input
     * object name does not exist it is created. A cumulative object state
     * is also created (if necessary) and updated.
     *
     * @param objectName The object state for which the reference count will be
     *                   set.
     * @param refCount   The current object reference count.
     */
    public void decrementReferenceCount(String objectName) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.decrementReferenceCount();
        iorms = getCreateObjectState("ALL");
        iorms.decrementReferenceCount();
        setUpdateTime();
    }

    /**
     * Increments this host state instance memory (count and memory) for the input
     * object name with the input amount of memory. If the object state for the
     * input object name does not exist it is created. A cumulative object state
     * is also created (if necessary) and updated.
     *
     * @param objectName The object state for which the memory will be
     *                   incremented.
     * @param memory     The amount of memory to increment.
     */
    public void incrementInstanceMemory(String objectName, long memory) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.incrementInstanceMemory(memory);
        iorms = getCreateObjectState("ALL");
        iorms.incrementInstanceMemory(memory);
        setUpdateTime();
    }

    /**
     * Increments this host state memory for the input object name with the input
     * amount of memory. If the object state for the input object name does not
     * exist it is created. A cumulative object state is also created (if
     * necessary) and updated.
     *
     * @param objectName The object state for which the memory will be
     *                   incremented.
     * @param memory     The amount of memory to increment.
     */
    public void incrementMemory(String objectName, long memory) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.incrementMemory(memory);
        iorms = getCreateObjectState("ALL");
        iorms.incrementMemory(memory);
        setUpdateTime();
    }

    /**
     * Increments this host state load count for the input object name. If the
     * object state for the input object name does not exist it is created. A
     * cumulative object state is also created (if necessary) and updated.
     *
     * @param objectName The object state for which the memory will be
     *                   incremented.
     */
    public void incrementLoadCount(String objectName) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.incrementLoadCount();
        iorms = getCreateObjectState("ALL");
        iorms.incrementLoadCount();
        setUpdateTime();
    }

    /**
     * Decrements this host state instance memory (count and memory) for the input
     * object name with the input amount of memory. If the object state for the
     * input object name does not exist it is created. A cumulative object state
     * is also created (if necessary) and updated.
     *
     * @param objectName The object state for which the memory will be
     *                   decremented.
     * @param memory     The amount of memory to decrement.
     * @param extraLoad  A flag indicating the object being destroyed was composed
     *                   of two separate loads (e.g. MatrixBlock held its data and
     *                   the transpose of its data).
     */
    public void decrementInstanceMemory(String objectName, long memory,
                                        boolean extraLoad) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.decrementInstanceMemory(memory, extraLoad);
        iorms = getCreateObjectState("ALL");
        iorms.decrementInstanceMemory(memory, extraLoad);
        setUpdateTime();
    }

    /**
     * Synchronized function to increment the current thread use count.
     */
    public synchronized void incrementThreadUseCount() {
        ++aCurrentThreadUseCount;
        setMaximumThreadUseCount();
    }

    /**
     * Synchronized function to decrement the current thread use count.
     */
    public synchronized void decrementThreadUseCount() {
        --aCurrentThreadUseCount;
    }

    /**
     * Synchronized function to set the maximum thread use count.
     */
    private synchronized void setMaximumThreadUseCount() {
        if (aMaxThreadUseCount < aCurrentThreadUseCount)
            aMaxThreadUseCount = aCurrentThreadUseCount;
    }

    /**
     * Sets the current and maximum IOMIManager IOMIObject cache entry count.
     *
     * @param sze The new IOMIManager IOMIObject cache entry count.
     */
    public synchronized void setIOMIObjectCacheEntries(int sze) {
        aCurrentCacheEntries = sze;
        if (aMaxCacheEntries < aCurrentCacheEntries)
            aMaxCacheEntries = aCurrentCacheEntries;
    }

    public synchronized void incrementIOMIObjectReferenceReuse(int refCount) {
        if (refCount > 1) ++aIOMIObjectRefReuseCount;
    }

    public synchronized void incrementIOMIObjectUnloadCount() {
        ++aIOMIObjectDestroyCount;
    }

    public synchronized void incrementIOMIObjectReloadCount() {
        ++aIOMIObjectReloadCount;
    }

    public synchronized void incrementIOMIObjectCacheReuseCount() {
        ++aIOMIObjectCacheReuseCount;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Updates this host state task time with a new task time entry.
     *
     * @param time The new task time entry.
     */
    public void updateTaskTime(long time) {
        synchronized (aTaskTime) {
            aTaskTime.add(time);
        }
        setUpdateTime();
    }

    /**
     * Updates the process given by the input name.
     *
     * @param processName The process whose time will be updated.
     * @param time        The additional process time (msec).
     * @param cumulative  If true the additional time is added to the cumulative
     *                    result ("ALL").
     */
    public void updateProcessTime(String processName, long time,
                                  boolean cummulative) {
        Statistic stat;

        // update process stat

        synchronized (aProcessTime) {
            stat = aProcessTime.get(processName);
            if (stat == null) {
                stat = new Statistic();
                aProcessTime.put(processName, stat);
            }
        }
        synchronized (stat) {
            stat.add(time);
        }

        // update cumulative statistic

        if (cummulative) {
            synchronized (aProcessTime) {
                stat = aProcessTime.get("ALL");
                if (stat == null) {
                    stat = new Statistic();
                    aProcessTime.put("ALL", stat);
                }
            }
            synchronized (stat) {
                stat.add(time);
            }
        }

        // set update time and return

        setUpdateTime();
    }

    /**
     * Updates the read statistics for the input object name reading data from
     * the input server tag. The amount of memory read, the time to read, and any
     * read failures are also input.
     *
     * @param objectName The object name for which the read was processed.
     * @param srvrTag    The server tag from which the data was read.
     * @param memory     The amount of data (bytes) read.
     * @param time       The amount of time to read (msec).
     * @param failures   The failure count while reading.
     */
    public void updateFileRead(String objectName, String srvrTag,
                               long memory, long time, int failures) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.updateFileRead(srvrTag, memory, time, failures);
        iorms = getCreateObjectState("ALL");
        iorms.updateFileRead(srvrTag, memory, time, failures);
        setUpdateTime();
    }

    /**
     * Updates the write statistics for the input object name writing data to
     * the input server tag. The amount of memory written, the time to write, and
     * any write failures are also input.
     *
     * @param objectName The object name for which the write was processed.
     * @param srvrTag    The server tag into which the data was written.
     * @param memory     The amount of data (bytes) written.
     * @param time       The amount of time to write (msec).
     * @param failures   The failure count while writing.
     */
    public void updateFileWrite(String objectName, String srvrTag,
                                long memory, long time, int failures) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.updateFileWrite(srvrTag, memory, time, failures);
        iorms = getCreateObjectState("ALL");
        iorms.updateFileWrite(srvrTag, memory, time, failures);
        setUpdateTime();
    }

    /**
     * Updates the rename statistics for the input object name renaming files on
     * the input server tag. The amount of time to rename and any rename failures
     * are also input.
     *
     * @param objectName The object name for which file renaming was processed.
     * @param srvrTag    The server tag on which the file was renamed.
     * @param time       The amount of time to rename (msec).
     * @param failures   The failure count while renaming.
     */
    public void updateFileRename(String objectName, String srvrTag,
                                 long time, int failures) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.updateFileRename(srvrTag, time, failures);
        iorms = getCreateObjectState("ALL");
        iorms.updateFileRename(srvrTag, time, failures);
        setUpdateTime();
    }

    /**
     * Updates the delete statistics for the input object name deleting files on
     * the input server tag. The amount of time to delete and any delete failures
     * are also input.
     *
     * @param objectName The object name for which file deleting was processed.
     * @param srvrTag    The server tag on which the file was deleted.
     * @param time       The amount of time to delete (msec).
     * @param failures   The failure count while deleting.
     */
    public void updateFileDelete(String objectName, String srvrTag,
                                 long time, int failures) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.updateFileDelete(srvrTag, time, failures);
        iorms = getCreateObjectState("ALL");
        iorms.updateFileDelete(srvrTag, time, failures);
        setUpdateTime();
    }

    /**
     * Updates the IO wait statistics for the input object name. The amount of
     * time waiting to process an IO request is also input.
     *
     * @param objectName The object name which was required to wait before
     *                   processing its IO request.
     * @param time       The amount of time waiting (msec).
     */
    public void updateIOWait(String objectName, long time) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.updateIOWait(time);
        iorms = getCreateObjectState("ALL");
        iorms.updateIOWait(time);
        setUpdateTime();
    }

    /**
     * Updates the IO blocking statistics for the input object name. The amount of
     * time blocking while another thread was processing an IO request is also
     * input.
     *
     * @param objectName The object name which was blocking while another thread
     *                   was processing an IO request.
     * @param time       The amount of time blocking (msec).
     */
    public void updateIOBlocking(String objectName, long time) {
        IOMIObjectState iorms = getCreateObjectState(objectName);
        iorms.updateIOBlocking(time);
        iorms = getCreateObjectState("ALL");
        iorms.updateIOBlocking(time);
        setUpdateTime();
    }

    /**
     * Sets the update time to now.
     */
    public synchronized void setUpdateTime() {
        aLastUpdateTime = (new Date()).getTime();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves the memory status from the run time object and updates the
     * maximum memory, total memory, used memory, maximum used memory, and he
     * available memory. Then it calculates the fraction of all memory used and
     * returns the result.
     *
     * @return The fraction of memory used.
     */
    public double updateMemoryUse() {
        synchronized (this) {
            aVMMaxMemory = Runtime.getRuntime().maxMemory();
            aVMTotalMemory = Runtime.getRuntime().totalMemory();
            long freemem = Runtime.getRuntime().freeMemory();
            aVMUsedMemory = aVMTotalMemory - freemem;
            if (aVMMaxUsedMemory < aVMUsedMemory) aVMMaxUsedMemory = aVMUsedMemory;
            aVMAvailableMemory = aVMMaxMemory - aVMUsedMemory;
        }

        return (double) aVMUsedMemory / aVMMaxMemory;
    }

    /**
     * Calls the Garbage Collector, updates the manual GC parameters, and returns
     * the recovered memory amount.
     *
     * @return The new amount of recovered memory.
     */
    public long gc() {
        // call garbage collector and record time

        long oldUsedMemory = aVMUsedMemory;
        long tim = (new Date()).getTime();
        Runtime.getRuntime().gc();
        tim = (new Date()).getTime() - tim;

        // set manual values

        long recvrdMemory = 0;
        synchronized (this) {
            // get new used memory amount

            aVMUsedMemory = Runtime.getRuntime().totalMemory() -
                    Runtime.getRuntime().freeMemory();

            ++aGCCountManual;
            aGCTimeManual += tim;
            recvrdMemory = oldUsedMemory - aVMUsedMemory;
            aGCRecvrdMemManual += recvrdMemory;
        }

        // return recovered memory

        return recvrdMemory;
    }

    /**
     * Calculates and returns the cumulative overhead time as the difference
     * between the total task time and the sum of all process times, file IO
     * process times, and IO delay times. If negative zero is returned.
     *
     * @return The cumulative overhead time as the difference between the total
     * task time and the sum of all process times, file IO process times,
     * and IO delay times.
     */
    public double getCummulativeOverheadTime() {
        IOMIObjectState mngrObjectState;
        Statistic stat;

        // get difference between task time and all process times

        double ovrhd = aTaskTime.getSum() -
                aProcessTime.get("ALL").getSum();

        // subtract off file IO and delay times for all supported object types

        mngrObjectState = aIOMIObjectStateMap.get("ALL");
        if (mngrObjectState != null) {
            stat = mngrObjectState.getCummulativeFileTotalTimeStatistic();
            if (stat != null) ovrhd -= stat.getSum();
            stat = mngrObjectState.getTotalIODelayStatistic();
            if (stat != null) ovrhd -= stat.getSum();
        }

        // if overhead is negative set to zero ... return result

        if (ovrhd < 0.0) ovrhd = 0.0;
        return ovrhd;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the requested object state. Null is returned if the input object
     * name is not defined.
     *
     * @param objectName The name of the object whose state will be returned.
     * @return The requested object state.
     */
    public IOMIObjectState getObjectState(String objectName) {
        return aIOMIObjectStateMap.get(objectName);
    }

    /**
     * Returns the map of all object types associated with their names.
     *
     * @return The map of all object types associated with their names.
     */
    public HashMap<String, IOMIObjectState> getObjectStateMap() {
        return aIOMIObjectStateMap;
    }

    /**
     * Private function that returns the requested object state or creates and
     * returns a new one if it does not exist.
     *
     * @param objectName The name of the object state to return.
     * @return The requested object state.
     */
    private IOMIObjectState getCreateObjectState(String objectName) {
        IOMIObjectState iorms;

        // get the state if it exists or create a new one if not

        synchronized (aIOMIObjectStateMap) {
            iorms = aIOMIObjectStateMap.get(objectName);
            if (iorms == null) {
                iorms = new IOMIObjectState(objectName);
                aIOMIObjectStateMap.put(objectName, iorms);
            }
        }

        return iorms;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns this object as a string description.
     *
     * @return This object as a string description.
     */
    @Override
    public String toString() {
        return toString("");
    }

    /**
     * Returns this object as a formatted string description prepending hdr to
     * the beginning of every line.
     *
     * @param hdr The header prepended to every line.
     * @return This object as a formatted string description
     */
    public String toString(String hdr) {
        //   IOMI Host Name = aHostName
        //
        //       Current Time            = Globals.getTimeStamp()
        //       Start Time              = aStartTime
        //       Last Update Time        = aLastUpdateTime
        //       Elapsed Time            = aLastUpdateTime - aStartTime
        //
        //  Summary:
        //
        //      Total task time             (% of total)                    = xxx hrs  (100.00)
        //      Total process time          (% of total)                    = xxx mins (  3.40)
        //      Total process overhead time (% of total)                    = xxx secs (  0.97)
        //      Total thread delay time     (% of total)                    = xxx hrs  ( 62.12)
        //      Total File IO time          (% of total)                    = xxx hrs  ( 37.21)
        //
        //      Additional GC Time          (% of total)                    = xxx mins (  3.28)
        //
        //      Total   File IO Failures                                    =   x
        //      Total   IOMI Object File IO Read/Write Data Size            = yyy.yy GB
        //
        //      ********************************************************************
        //
        //      Total                     ==> All [Allocated, Loaded]
        //                                    [Objects, Memory]. Includes "Current" +
        //                                    "Destroyed" over execution lifetime.
        //      Maximum                   ==> Maximum [Allocated, Loaded, Referenced]
        //                                    [Objects, Memory]. The largest value
        //                                    of "Current" over the execution
        //                                    lifetime.
        //      Current                   ==> Current [Allocated, Loaded, Referenced]
        //                                    [Objects, Memory]. The "Current" value
        //                                    when this text was written.
        //
        //      Loaded     Instance Count ==> Instanced Objects that have loaded
        //                                    their data.
        //      Allocated  Instance Count ==> All Instanced Objects ... some loaded,
        //                                    some not.
        //      Referenced Instance Count ==> Number of times Instanced Objects are
        //                                    referenced by Process Tasks.
        //      Destroyed  Instance Count ==> Number of Instanced Objects destroyed.
        //                                    (Loaded -> Used -> Cached ->
        //                                     Destroyed)
        //      Reload     Instance Count ==> Loaded -> Used -> Cached ->
        //                                    Destroyed -> Reloaded.
        //      Reference  Reuse    Count ==> Reference Count > 1
        //      Cache      Entry    Count ==> Number of unreferenced objects
        //                                    stored in the cache
        //      Cache      Reuse    Count ==> Loaded -> Used -> Cached ->
        //                                    Re-referenced.
        //      Cache      Miss     Ratio  =  Reload / (Reload + CacheReuse)
        //
        //      ********************************************************************
        //
        //      Total   IOMI Object  Loaded    Instance  Count              =   25
        //      Maximum IOMI Object  Loaded    Instance  Count              =   10
        //      Current IOMI Object  Loaded    Instance  Count              =   8
        //
        //      Total   IOMI Object  Allocated Instance  Count              =   25
        //      Maximum IOMI Object  Allocated Instance  Count              =   10
        //      Current IOMI Object  Allocated Instance  Count              =   8
        //
        //      Total   IOMI Object  Allocated Memory    Size               =  35.17 GB
        //      Maximum IOMI Object  Allocated Memory    Size               =   6.55 GB
        //      Current IOMI Object  Allocated Memory    Size               =   4.55 GB
        //
        //      Total   IOMI Object  Destroyed Instance  Count              = 1005
        //      Total   IOMI Object  Reload    Instance  Count              = 1043
        //
        //      Maximum IOMI Object  Task      Reference Count              =   10
        //      Current IOMI Object  Task      Reference Count              =   8
        //      Total   IOMI Object  Reference Reuse     Count              = 1043
        //
        //      Maximum IOMI Object  Cache     Entry     Count              = 434
        //      Current IOMI Object  Cache     Entry     Count              = 404
        //      Total   IOMI Object  Cache     Reuse     Count              = 1043
        //      Cache   Miss                             Ratio              = ****
        //
        //      Maximum Simultaneous Thread    Use       Count              = 16
        //      Current Simultaneous Thread    Use       Count              = 14
        //
        //  Results:
        //
        //                    Count     Minimum     Maximum     Mean     Std. Dev.      RMS        Sum      % of Total
        //       ------------------------------------------------------------------------------------------------------
        //       Task Time     2       33.35 mins   2.78 hrs   1.67 hrs   1.57 hrs    2.00 hrs   3.33 hrs   100.00
        //       Process Time  2       34.02 mins   3.33 hrs   1.95 hrs   1.96 hrs    2.39 hrs   3.90 hrs    42.62
        //
        //       Unaccounted Process Overhead (% of Task Time) = overhead (%)
        //
        //       GC Statistics:
        //
        //            Manual Call Count                   = aGCCountManual
        //            Manual Call Time                    = aGCTimeManual
        //            Manual Total Recovered Memory       = aGCRecvrdMemManual
        //            Manual CacheRecovery Call Count     = aGCCacheCollctnCount
        //            Manual Total Recovered Cache Memory = aGCRecvrdCacheMem
        //
        //            Collector        Count     Time
        //            ------------------------------------
        //            Cummulative       xxx      xxxx mins
        //            Mark And Sweep    xxx      xxxx mins
        //            G1                xxx      xxxx mins
        //
        //       GC Additional Process Time (% of Task Time)   = gctime (%)
        //
        //       IOMIObjectState Object Name = aObjectName
        //

        String s = "";

        // print host name and host set if defined beyond 1

        s += hdr + "IOMI Host State:" + NL + NL;
        s += hdr + "    " + Globals.repeat("~", 144) + NL;
        s += hdr + "    Host Name = " + aHostName + NL + NL;
        if (aHostSet.size() > 1) {
            String s1 = hdr + "    Host Set: ";
            int ln = s1.length();
            boolean newLine = true;
            for (String hn : aHostSet) {
                if (!hn.equals(aHostName)) {
                    if (ln + hn.length() + 1 >= 80) {
                        s += s1 + NL;
                        s1 = NL + hdr + "              ";
                        ln = s1.length();
                        newLine = true;
                    }
                    ln += hn.length();
                    if (newLine) {
                        s1 += hn;
                        newLine = false;
                    } else {
                        s1 += ", " + hn;
                        ln += 2;
                        ;
                    }
                }
            }
            if (!newLine) s += s1 + NL;
            s += NL;
        }

        // output life time information

        s += hdr + "    Current Time                 = " +
                Globals.getTimeStamp() + NL;
        s += hdr + "    Start Time                   = " +
                Globals.getTimeStamp(aStartTime) + NL;
        s += hdr + "    Last Modification Time       = " +
                Globals.getTimeStamp(aLastUpdateTime) + NL;
        s += hdr + "    Elapsed Time                 = " +
                Globals.elapsedTimeString(aStartTime, aLastUpdateTime) + NL + NL;

        // get summary information

        IOMIObjectState totlObjState = aIOMIObjectStateMap.get("ALL");
        double ttt = aTaskTime.getSum();
        double tpt = aProcessTime.get("ALL").getSum();
        double tpot = getCummulativeOverheadTime();
        double ttdt = 0.0;
        if (totlObjState.getTotalIODelayStatistic() != null)
            ttdt = totlObjState.getTotalIODelayStatistic().getSum();
        double tfiot = totlObjState.getFileTotalTimeStatistic("ALL").getSum();
        double agct = aGCTiming.getTotalCollectionTime();
        int tiof = totlObjState.getCummulativeIOFailureCount();
        long tfiods = (long) totlObjState.getCummulativeFileTotalMemoryStatistic().getSum();
        int toli = totlObjState.getTotalLoadedCount();
        int moli = totlObjState.getMaximumLoadedCount();
        int coli = totlObjState.getCurrentLoadedCount();
        int toi = totlObjState.getTotalInstanceCount();
        int moi = totlObjState.getMaximumInstanceCount();
        int coi = totlObjState.getCurrentInstanceCount();
        int mor = totlObjState.getMaximumReferenceCount();
        int cor = totlObjState.getCurrentReferenceCount();
        long tima = totlObjState.getTotalInstanceMemory();
        long mima = totlObjState.getMaximumInstanceMemory();
        long cima = totlObjState.getCurrentInstanceMemory();
        double cmr = 100.0 * aIOMIObjectReloadCount /
                (aIOMIObjectReloadCount + aIOMIObjectCacheReuseCount);

        // output summary information

        s += hdr + "    Summary:" + NL + NL;
        s += hdr + "        " +
                "Total Task Time             (% of total)                    = " +
                Globals.timeStringAbbrvUnits(ttt) +
                " (100.00 %)" + NL;
        s += hdr + "        " +
                "Total Process Time          (% of total)                    = " +
                Globals.timeStringAbbrvUnits(tpt) +
                " (" + String.format("%6.2f", 100.0 * tpt / ttt) + " %)" + NL;
        s += hdr + "        " +
                "Total Process Overhead Time (% of total)                    = " +
                Globals.timeStringAbbrvUnits(tpot) +
                " (" + String.format("%6.2f", 100.0 * tpot / ttt) + " %)" + NL;
        s += hdr + "        " +
                "Total Thread Delay Time     (% of total)                    = " +
                Globals.timeStringAbbrvUnits(ttdt) +
                " (" + String.format("%6.2f", 100.0 * ttdt / ttt) + " %)" + NL;
        s += hdr + "        " +
                "Total File IO Time          (% of total)                    = " +
                Globals.timeStringAbbrvUnits(tfiot) +
                " (" + String.format("%6.2f", 100.0 * tfiot / ttt) + " %)" + NL + NL;
        s += hdr + "        " +
                "Additional GC Time          (% of total)                    = " +
                Globals.timeStringAbbrvUnits(agct) +
                " (" + String.format("%6.2f", 100.0 * agct / ttt) + " %)" + NL + NL;
        s += hdr + "        " +
                "Total   File IO Failures                                    = " +
                String.format("%5d", tiof) + NL;
        s += hdr + "        " +
                "Total   IOMI Object File IO Read/Write Data Size            = " +
                Globals.memoryUnit(tfiods) + NL + NL;
        s += hdr + "        " +
                "*********************************************************************" +
                NL + NL +
                hdr + "        " +
                "Total                     ==> All [Allocated, Loaded]" + NL +
                hdr + "        " +
                "                              [Objects, Memory]. Includes \"Current\"" + NL +
                hdr + "        " +
                "                              and \"Destroyed\" over execution" + NL +
                hdr + "        " +
                "                              lifetime." + NL +
                hdr + "        " +
                "Maximum                   ==> Maximum [Allocated, Loaded, Referenced]" + NL +
                hdr + "        " +
                "                              [Objects, Memory]. The largest value" + NL +
                hdr + "        " +
                "                              of \"Current\" over the execution" + NL +
                hdr + "        " +
                "                              lifetime." + NL +
                hdr + "        " +
                "Current                   ==> Current [Allocated, Loaded, Referenced]" + NL +
                hdr + "        " +
                "                              [Objects, Memory]. The \"Current\" value" + NL +
                hdr + "        " +
                "                              when this text was written." + NL + NL +
                hdr + "        " +
                "Loaded     Instance Count ==> Instanced Objects that have loaded" + NL +
                hdr + "        " +
                "                              their data." + NL +
                hdr + "        " +
                "Allocated  Instance Count ==> All Instanced Objects ... some loaded," + NL +
                hdr + "        " +
                "                              some not." + NL +
                hdr + "        " +
                "Referenced Instance Count ==> Number of times Instanced Objects are" + NL +
                hdr + "        " +
                "                              referenced by Process Tasks." + NL +
                hdr + "        " +
                "Destroyed  Instance Count ==> Number of Instanced Objects destroyed." + NL +
                hdr + "        " +
                "                              (Loaded -> Used -> Cached ->" + NL +
                hdr + "        " +
                "                               Destroyed)" + NL +
                hdr + "        " +
                "Reload     Instance Count ==> Loaded -> Used -> Cached ->" + NL +
                hdr + "        " +
                "                              Destroyed -> Reloaded." + NL +
                hdr + "        " +
                "Reference  Reuse    Count ==> Reference Count > 1" + NL +
                hdr + "        " +
                "Cache      Entry    Count ==> Number of unreferenced objects" + NL +
                hdr + "        " +
                "                              stored in the cache." + NL +
                hdr + "        " +
                "Cache      Reuse    Count ==> Loaded -> Used -> Cached ->" + NL +
                hdr + "        " +
                "                              Re-referenced." + NL +
                hdr + "        " +
                "Cache      Miss     Ratio  =  Reload / (Reload + CacheReuse)" + NL + NL +
                hdr + "        " +
                "*********************************************************************" +
                NL + NL;

        s += hdr + "        " +
                "Total   IOMI Object  Loaded    Instance  Count              = " +
                String.format("%5d", toli) + NL;
        s += hdr + "        " +
                "Maximum IOMI Object  Loaded    Instance  Count              = " +
                String.format("%5d", moli) + NL;
        s += hdr + "        " +
                "Current IOMI Object  Loaded    Instance  Count              = " +
                String.format("%5d", coli) + NL + NL;
        s += hdr + "        " +
                "Total   IOMI Object  Allocated Instance  Count              = " +
                String.format("%5d", toi) + NL;
        s += hdr + "        " +
                "Maximum IOMI Object  Allocated Instance  Count              = " +
                String.format("%5d", moi) + NL;
        s += hdr + "        " +
                "Current IOMI Object  Allocated Instance  Count              = " +
                String.format("%5d", coi) + NL + NL;
        s += hdr + "        " +
                "Total   IOMI Object  Allocated Memory    Size               = " +
                Globals.memoryUnit(tima) + NL;
        s += hdr + "        " +
                "Maximum IOMI Object  Allocated Memory    Size               = " +
                Globals.memoryUnit(mima) + NL;
        s += hdr + "        " +
                "Current IOMI Object  Allocated Memory    Size               = " +
                Globals.memoryUnit(cima) + NL + NL;
        s += hdr + "        " +
                "Total   IOMI Object  Destroyed Instance  Count              = " +
                String.format("%5d", aIOMIObjectDestroyCount) + NL;
        s += hdr + "        " +
                "Total   IOMI Object  Reload    Instance  Count              = " +
                String.format("%5d", aIOMIObjectReloadCount) + NL + NL;
        s += hdr + "        " +
                "Maximum IOMI Object  Task      Reference Count              = " +
                String.format("%5d", mor) + NL;
        s += hdr + "        " +
                "Current IOMI Object  Task      Reference Count              = " +
                String.format("%5d", cor) + NL;
        s += hdr + "        " +
                "Total   IOMI Object  Reference Reuse     Count              = " +
                String.format("%5d", aIOMIObjectRefReuseCount) + NL + NL;
        s += hdr + "        " +
                "Maximum IOMI Object  Cache     Entry     Count              = " +
                String.format("%5d", aMaxCacheEntries) + NL;
        s += hdr + "        " +
                "Current IOMI Object  Cache     Entry     Count              = " +
                String.format("%5d", aCurrentCacheEntries) + NL;
        s += hdr + "        " +
                "Total   IOMI Object  Cache     Reuse     Count              = " +
                String.format("%5d", aIOMIObjectCacheReuseCount) + NL;
        s += hdr + "        " +
                "                     Cache     Miss      Ratio (%)          = " +
                String.format("%7.2f", cmr) + NL + NL;
        s += hdr + "        " +
                "Maximum Simultaneous Thread    Use       Count              = " +
                String.format("%5d", aMaxThreadUseCount) + NL;
        s += hdr + "        " +
                "Current Simultaneous Thread    Use       Count              = " +
                String.format("%5d", aCurrentThreadUseCount) + NL + NL;
        s += hdr + "        " +
                "IOMI Manager Memory Resource Stress Flag                    = " +
                aManagerStress + NL + NL;

        // output results ... first the task/process table

        s += hdr + "    Task/Process Results:" + NL + NL;

        ArrayList<String> rowName = new ArrayList<String>();
        ArrayList<Statistic> rowStat = new ArrayList<Statistic>();
        ArrayList<TableUnit> rowUnit = new ArrayList<TableUnit>();

        // build output data

        int[] totalRows = {-1, -1, -1};
        boolean useTotalColumn = false;
        if (aTaskTime != null) {
            rowName.add("Task Time");
            rowStat.add(aTaskTime);
            rowUnit.add(TableUnit.TIME_UNIT);
        }
        if (aProcessTime != null) {
            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            if (aProcessTime.size() > 2) {
                totalRows[1] = 2;
                useTotalColumn = true;
                rowName.add("Total Process Time");
                rowStat.add(aProcessTime.get("ALL"));
                rowUnit.add(TableUnit.TIME_UNIT);
                if (aProcessOrder.size() > 0) {
                    for (Map.Entry<Integer, String> e : aProcessOrder.entrySet()) {
                        Statistic stat = aProcessTime.get(e.getValue());
                        if (stat != null) {
                            rowName.add(e.getValue() + " Time");
                            rowStat.add(stat);
                            rowUnit.add(TableUnit.TIME_UNIT);
                        }
                    }
                } else {
                    for (Map.Entry<String, Statistic> e : aProcessTime.entrySet()) {
                        if (!e.getKey().equals("ALL")) {
                            rowName.add(e.getKey() + " Time");
                            rowStat.add(e.getValue());
                            rowUnit.add(TableUnit.TIME_UNIT);
                        }
                    }
                }
            } else {
                for (Map.Entry<String, Statistic> e : aProcessTime.entrySet()) {
                    if (!e.getKey().equals("ALL")) {
                        rowName.add(e.getKey() + " Time");
                        rowStat.add(e.getValue());
                        rowUnit.add(TableUnit.TIME_UNIT);
                    }
                }
            }
        }

        // build column and row headers

        if (rowName.size() > 0) {
            int[] column0 = new int[1];
            s += NL + IOMIObjectState.statTable(hdr + "        ",
                    useTotalColumn, "Task/Process Statistic",
                    rowName, rowStat, rowUnit, totalRows, column0);
            s += NL;
        }
        s += hdr + "        " +
                "Total Process Overhead Time (% of total task time) = " +
                Globals.timeStringAbbrvUnits(tpot) +
                " (" + String.format("%7.2f", 100.0 * tpot / ttt) + " %)" + NL + NL;

        // output virtual machine memory information

        double totlfrc = 100.0 * aVMTotalMemory / aVMMaxMemory;
        double availfrc = 100.0 * aVMAvailableMemory / aVMMaxMemory;
        double usedfrc = 100.0 * aVMUsedMemory / aVMMaxMemory;
        double maxusedfrc = 100.0 * aVMMaxUsedMemory / aVMMaxMemory;

        s += hdr + "    Virtual Machine (VM) Memory:" + NL + NL;
        s += hdr + "        Max. VM Memory           =   " +
                String.format("%10s", Globals.memoryUnit(aVMMaxMemory)) + NL;
        s += hdr + "        Allocated VM Memory      =   " +
                String.format("%10s (%7.2f", Globals.memoryUnit(aVMTotalMemory), totlfrc) +
                " %)" + NL;
        s += hdr + "        Available VM Memory      =   " +
                String.format("%10s (%7.2f", Globals.memoryUnit(aVMAvailableMemory), availfrc) +
                " %)" + NL;
        s += hdr + "        Used VM Memory           =   " +
                String.format("%10s (%7.2f", Globals.memoryUnit(aVMUsedMemory), usedfrc) +
                " %)" + NL;
        s += hdr + "        Max. Obs. Used VM Memory =   " +
                String.format("%10s (%7.2f", Globals.memoryUnit(aVMMaxUsedMemory), maxusedfrc) +
                " %)" + NL + NL;

        // output GC information

        s += hdr + "    GC Statistics:" + NL + NL;
        s += hdr + "        Manual Call Count                   = " +
                String.format("%5d", aGCCountManual) + NL;
        s += hdr + "        Manual Call Time                    = " +
                Globals.timeStringAbbrvUnits(aGCTimeManual) + NL;
        s += hdr + "        Manual Total Recovered Memory       = " +
                Globals.memoryUnit(aGCRecvrdMemManual) + NL + NL;

        if ((aGCTiming.getCollectorCount() > 0) &&
                (aGCTiming.getTotalCollectionCount() > 0)) {
            String[][] entries;
            if (aGCTiming.getCollectorCount() > 1)
                entries = new String[aGCTiming.getCollectorCount() + 2][4];
            else
                entries = new String[2][3];
            String[] row0 = entries[0];
            row0[0] = "Collector";
            row0[1] = "Count";
            row0[2] = "Time";
            if (aGCTiming.getCollectorCount() > 1) {
                row0[3] = "% of Total";
                String[] row1 = entries[1];
                row1[0] = "Total Time";
                row1[1] = " " + aGCTiming.getTotalCollectionCount();
                row1[2] = Globals.timeStringAbbrvUnits(aGCTiming.getTotalCollectionTime());
                row1[3] = "100.00 %";
                int k = 2;
                for (String name : aGCTiming.getNames()) {
                    if (!name.equals("Total")) {
                        String[] rowk = entries[k];
                        rowk[0] = name;
                        rowk[1] = " " + aGCTiming.getCollectionCount(name);
                        rowk[2] = Globals.timeStringAbbrvUnits(aGCTiming.getCollectionTime(name));
                        rowk[3] = String.format("%7.2f", 100.0 *
                                aGCTiming.getCollectionTime(name) /
                                aGCTiming.getTotalCollectionTime());
                        ++k;
                    }
                }
            } else {
                String[] row1 = entries[1];
                String name = aGCTiming.getNames().iterator().next();
                row1[0] = name;
                row1[1] = " " + aGCTiming.getCollectionCount(name);
                row1[2] = Globals.timeStringAbbrvUnits(aGCTiming.getCollectionTime(name));
            }

            // have entries ... determin max column widths

            int[] maxColumnWidth = new int[entries[0].length];
            for (int j = 0; j < entries[0].length; ++j)
                maxColumnWidth[j] = entries[0][j].length();
            for (int i = 1; i < entries.length; ++i)
                for (int j = 0; j < entries[0].length; ++j)
                    if (maxColumnWidth[j] < entries[i][j].length())
                        maxColumnWidth[j] = entries[i][j].length();
            for (int i = 1; i < entries.length; ++i)
                for (int j = 0; j < entries[0].length; ++j)
                    maxColumnWidth[j] += 3;

            // now have column widths draw table

            int colsum = maxColumnWidth[0];
            s += hdr + "        " +
                    Globals.leftJustifyString(entries[0][0], maxColumnWidth[0]);
            for (int j = 1; j < entries[0].length; ++j) {
                s += Globals.centerString(entries[0][j], maxColumnWidth[j], true);
                colsum += maxColumnWidth[j];
            }
            s += NL;
            s += hdr + "        " + Globals.repeat("-", colsum) + NL;
            for (int i = 1; i < entries.length; ++i) {
                s += hdr + "        " +
                        Globals.leftJustifyString(entries[i][0], maxColumnWidth[0]);
                for (int j = 1; j < entries[0].length; ++j)
                    s += Globals.leftJustifyString(entries[i][j], maxColumnWidth[j]);
                s += NL;
            }
            s += NL;
        }
        s += hdr + "        " +
                "Additional GC Time in Excess of Task Time (% of total task time) = " +
                Globals.timeStringAbbrvUnits(agct) +
                " (" + String.format("%7.2f", 100.0 * agct / ttt) + " %)" + NL + NL;

        // done ... output all object states

        s += hdr + "    IOMI Object States:" + NL + NL;
        if (aIOMIObjectStateMap.size() > 2) {
            IOMIObjectState iormts = aIOMIObjectStateMap.get("ALL");
            s += iormts.toString(hdr + "        ") + NL;
        }
        for (Map.Entry<String, IOMIObjectState> e :
                aIOMIObjectStateMap.entrySet()) {
            if (!e.getKey().equals("ALL"))
                s += e.getValue().toString(hdr + "        ") + NL;
        }

        // done ... return string

        return s;
    }
}
