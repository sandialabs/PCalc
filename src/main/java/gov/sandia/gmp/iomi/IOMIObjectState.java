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
package gov.sandia.gmp.iomi;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.statistics.Statistic;

/**
 * Used to hold the performance state of some IOMIInterface object including
 * File IO (read, write, rename, and/or delete) size, time, rate, and failure
 * counts, IO Delay time including IO Wait time and blocking time (Wait time is
 * the amount of time a thread is told to wait by the IOMIManager before it is
 * allowed to process its IO request while blocking time is the amount of time
 * a thread must wait while some other thread processes an IO request).
 * <p>
 * Additionally, this object records the number of instances (current in-core,
 * maximum, and total) of the IOMIInterface object represented by this state
 * and the amount of memory allocated (current in-core, maximum in-core, and
 * total) from this IOMIInterface object.
 * <p>
 * One or more of these states, each representing a different class of
 * IOMIInterface objects, are maintained by a single IOMIHostState object.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class IOMIObjectState implements Serializable {
    /**
     * The name of the IOMIInterface object. This is usually just the simple
     */
    private String aObjectName = "";

    /**
     * The current in-core instance count of this object created by the
     * owning IOMIManager.
     */
    private int aCurrInstanceCount = 0;

    /**
     * The maximum in-core instance count of this object seen by the
     * owning IOMIManager over the managers lifetime.
     */
    private int aMaxInstanceCount = 0;

    /**
     * The total number of this object created by the
     * owning IOMIManager over the managers lifetime.
     */
    private int aTotalInstanceCount = 0;

    /**
     * The current in-core instance count of this object created by the owning
     * IOMIManager that has also loaded its data. When an IOMI object is
     * created it generally has not loaded its data. Loaded counts <= Instanced
     * counts.
     */
    private int aCurrLoadedCount = 0;

    /**
     * The maximum in-core instance count of this object seen by the owning
     * IOMIManager over the managers lifetime that has also loaded its data.
     * When an IOMI object is created it generally has not loaded its data.
     * Loaded counts <= Instanced counts.
     */
    private int aMaxLoadedCount = 0;

    /**
     * The total number of this object created by the owning IOMIManager over
     * the managers lifetime that has also loaded its data. When an IOMI object
     * is created it generally has not loaded its data. Loaded counts <=
     * Instanced counts.
     */
    private int aTotalLoadedCount = 0;

    /**
     * Current reference count by the using application.
     */
    private int aCurrReferenceCount = 0;

    /**
     * Maximum reference count attained by the using application.
     */
    private int aMaxReferenceCount = 0;

    /**
     * The current in-core memory allocation of this object created by the
     * owning IOMIManager.
     */
    private long aCurrInstanceMemory = 0;

    /**
     * The maximum in-core memory allocation of this object seen by the
     * owning IOMIManager over the managers lifetime.
     */
    private long aMaxInstanceMemory = 0;

    /**
     * The total amount of memory allocated for this object created by the
     * owning IOMIManager over the managers lifetime.
     */
    private long aTotalInstanceMemory = 0;

    /**
     * The total number of bytes read or written for objects of this type over
     * the lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aTotalSizeStat = null;

    /**
     * The total number of bytes read for objects of this type over the
     * lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aReadSizeStat = null;

    /**
     * The total number of bytes written for objects of this type over the
     * lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aWriteSizeStat = null;

    /**
     * The total time (msec) spent reading or writing data for objects of this
     * type over the lifetime of the owning IOMIManager. The statistic is stored
     * for each unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aTotalTimeStat = null;

    /**
     * The total time (msec) spent reading data for objects of this type
     * over the lifetime of the owning IOMIManager. The statistic is stored for
     * each unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aReadTimeStat = null; // r

    /**
     * The total time (msec) spent writing data for objects of this type
     * over the lifetime of the owning IOMIManager. The statistic is stored for
     * each unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aWriteTimeStat = null; // w

    /**
     * The total time (msec) spent renaming objects of this type over
     * the lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aRenameTimeStat = null;

    /**
     * The total time (msec) spent deleting objects of this type over
     * the lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aDeleteTimeStat = null;

    /**
     * The total rate reading or writing data for objects of this type over
     * the lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aTotalRateStat = null;

    /**
     * The total rate reading data for objects of this type over
     * the lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aReadRateStat = null; // r

    /**
     * The total rate writing data for objects of this type over
     * the lifetime of the owning IOMIManager. The statistic is stored for each
     * unique file server name (key).
     */
    private ConcurrentHashMap<String, Statistic> aWriteRateStat = null; // w

    /**
     * The total number of failures from all file IO operations for objects of
     * this type over the lifetime of the owning IOMIManager. The statistic is
     * stored for each unique file server name (key).
     */
    private ConcurrentHashMap<String, Integer> aIOFailureCount = null;

    /**
     * The total number of file IO operations that had now wait time.
     */
    private int aIOWait0Count = 0;

    /**
     * The total amount of time (msec) spent blocking for some IO operation to
     * complete, or waiting to begin some IO operation (imposed by the owning
     * IOMIManager) by some thread.
     */
    private Statistic aTotalIODelayStat = null;

    /**
     * The total amount of time (msec) spent waiting by some thread
     * to begin an IO operation (imposed by the owning IOMIManager).
     */
    private Statistic aIOWaitTimeStat = null; // wa

    /**
     * The total amount of time (msec) spent blocking by some thread
     * for an IO operation to complete.
     */
    private Statistic aIOBlockingTimeStat = null; // b

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Standard constructor.
     *
     * @param objName The object name (simple class name) of the object for which
     *                this state is instantiated.
     */
    public IOMIObjectState(String objName) {
        aObjectName = objName;
    }

    /**
     * Used to create a synchronized safe copy of the input IOMIObjectState for
     * serialization and transport across the network.
     *
     * @param iomios The input (and actively updated) IOMIObjectState object that
     *               will be copied into this one.
     */
    public IOMIObjectState(IOMIObjectState iomios) {
        // copy intrinsics

        synchronized (iomios) {
            aObjectName = iomios.aObjectName;
            aCurrInstanceCount = iomios.aCurrInstanceCount;
            aMaxInstanceCount = iomios.aMaxInstanceCount;
            aTotalInstanceCount = iomios.aTotalInstanceCount;
            aCurrLoadedCount = iomios.aCurrLoadedCount;
            aMaxLoadedCount = iomios.aMaxLoadedCount;
            aTotalLoadedCount = iomios.aTotalLoadedCount;
            aCurrReferenceCount = iomios.aCurrReferenceCount;
            aMaxReferenceCount = iomios.aMaxReferenceCount;
            aCurrInstanceMemory = iomios.aCurrInstanceMemory;
            aMaxInstanceMemory = iomios.aMaxInstanceMemory;
            aTotalInstanceMemory = iomios.aTotalInstanceMemory;
            aIOWait0Count = iomios.aIOWait0Count;
        }

        // copy server statistic hash maps

        if (iomios.aTotalSizeStat != null) {
            aTotalSizeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aTotalSizeStat, iomios.aTotalSizeStat);
        }
        if (iomios.aReadSizeStat != null) {
            aReadSizeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aReadSizeStat, iomios.aReadSizeStat);
        }
        if (iomios.aWriteSizeStat != null) {
            aWriteSizeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aWriteSizeStat, iomios.aWriteSizeStat);
        }
        if (iomios.aTotalTimeStat != null) {
            aTotalTimeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aTotalTimeStat, iomios.aTotalTimeStat);
        }
        if (iomios.aReadTimeStat != null) {
            aReadTimeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aReadTimeStat, iomios.aReadTimeStat);
        }
        if (iomios.aWriteTimeStat != null) {
            aWriteTimeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aWriteTimeStat, iomios.aWriteTimeStat);
        }
        if (iomios.aRenameTimeStat != null) {
            aRenameTimeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aRenameTimeStat, iomios.aRenameTimeStat);
        }
        if (iomios.aDeleteTimeStat != null) {
            aDeleteTimeStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aDeleteTimeStat, iomios.aDeleteTimeStat);
        }
        if (iomios.aTotalRateStat != null) {
            aTotalRateStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aTotalRateStat, iomios.aTotalRateStat);
        }
        if (iomios.aReadRateStat != null) {
            aReadRateStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aReadRateStat, iomios.aReadRateStat);
        }
        if (iomios.aWriteRateStat != null) {
            aWriteRateStat = new ConcurrentHashMap<String, Statistic>();
            copyStatMap(aWriteRateStat, iomios.aWriteRateStat);
        }

        // copy failure count hash map

        if (iomios.aIOFailureCount != null)
            synchronized (iomios.aIOFailureCount) {
                aIOFailureCount = new ConcurrentHashMap<String, Integer>();
                for (Map.Entry<String, Integer> e : iomios.aIOFailureCount.entrySet())
                    aIOFailureCount.put(e.getKey(), new Integer(e.getValue()));
            }

        // copy delay statistics

        if (iomios.aTotalIODelayStat != null) {
            aTotalIODelayStat = new Statistic();
            synchronized (iomios.aTotalIODelayStat) {
                aTotalIODelayStat.add(iomios.aTotalIODelayStat);
            }
        }
        if (iomios.aIOWaitTimeStat != null) {
            aIOWaitTimeStat = new Statistic();
            synchronized (iomios.aIOWaitTimeStat) {
                aIOWaitTimeStat.add(iomios.aIOWaitTimeStat);
            }
        }
        if (iomios.aIOBlockingTimeStat != null) {
            aIOBlockingTimeStat = new Statistic();
            synchronized (iomios.aIOBlockingTimeStat) {
                aIOBlockingTimeStat.add(iomios.aIOBlockingTimeStat);
            }
        }
    }

    /**
     * Copies the fromMap to the toMap.
     *
     * @param toMap   The map to contain the copy.
     * @param fromMap The map to be copied.
     */
    private void copyStatMap(ConcurrentHashMap<String, Statistic> toMap,
                             ConcurrentHashMap<String, Statistic> fromMap) {
        synchronized (fromMap) {
            for (Map.Entry<String, Statistic> e : fromMap.entrySet()) {
                Statistic s = new Statistic();
                synchronized (e.getValue()) {
                    s.add(e.getValue());
                }
                toMap.put(e.getKey(), s);
            }
        }
    }

    /**
     * Returns the parameter specified by the input parameter name.
     *
     * @param paramName The parameter name for which the parameter will be
     *                  returned. The following names are supported.
     *                  "IO Zero Wait 0 Count",
     *                  "Current Instance Count",
     *                  "Maximum Instance Count",
     *                  "Total Instance Count",
     *                  "Current Instance Memory",
     *                  "Maximum Instance Memory", and
     *                  "Total Instance Memory",
     * @return The parameter associated with the input parameter name. If
     * paramName is not defined -1 is returned.
     */
    public double getObjectParameter(String paramName) {
        if (paramName.equals("IO Zero Wait Count"))
            return aIOWait0Count;
        else if (paramName.equals("Current Instance Count"))
            return aCurrInstanceCount;
        else if (paramName.equals("Maximum Instance Count"))
            return aMaxInstanceCount;
        else if (paramName.equals("Total Instance Count"))
            return aTotalInstanceCount;
        else if (paramName.equals("Current Loaded Count"))
            return aCurrLoadedCount;
        else if (paramName.equals("Maximum Loaded Count"))
            return aMaxLoadedCount;
        else if (paramName.equals("Total Loaded Count"))
            return aTotalLoadedCount;
        else if (paramName.equals("Current Reference Count"))
            return aCurrReferenceCount;
        else if (paramName.equals("Maximum Reference Count"))
            return aMaxReferenceCount;
        else if (paramName.equals("Current Instance Memory"))
            return aCurrInstanceMemory;
        else if (paramName.equals("Maximum Instance Memory"))
            return aMaxInstanceMemory;
        else if (paramName.equals("Total Instance Memory"))
            return aTotalInstanceMemory;

        return -1;
    }

    /**
     * Returns the delay parameter statistic specified by the
     * input parameter name.
     *
     * @param paramName The parameter name for which the delay statistic will be
     *                  returned. The following names are supported.
     *                  "Total IO Delay",
     *                  "IO Wait Time", and
     *                  "IO Blocking Time"
     * @return The delay statistic associated with the input parameter name. If
     * paramName is not defined null is returned.
     */
    public Statistic getObjectDelayStatistic(String paramName) {
        if (paramName.equals("Total IO Delay"))
            return aTotalIODelayStat;
        else if (paramName.equals("IO Wait Time"))
            return aIOWaitTimeStat;
        else if (paramName.equals("IO Blocking Time"))
            return aIOBlockingTimeStat;

        return null;
    }

    /**
     * Returns the map of servers associated with IO statistics given the input
     * parameter type and unit.
     *
     * @param paramType The parameter type to be returned ... Valid values include
     *                  "Total", "Read", "Write", "Rename", and "Delete".
     * @param paramUnit The parameter unit to be returned ... valid values include
     *                  "Size", "Time", and "Rate".
     * @return The map of servers associated with IO statistics given the input
     * parameter type and unit. If an input type or unit is not
     * recognized null is returned.
     */
    public ConcurrentHashMap<String, Statistic>
    getServerStatistic(String paramType, String paramUnit) {
        if (paramType.equals("Total")) {
            if (paramUnit.equals("Size"))
                return aTotalSizeStat;
            else if (paramUnit.equals("Time"))
                return aTotalTimeStat;
            else if (paramUnit.equals("Rate"))
                return aTotalRateStat;
        } else if (paramType.equals("Read")) {
            if (paramUnit.equals("Size"))
                return aReadSizeStat;
            else if (paramUnit.equals("Time"))
                return aReadTimeStat;
            else if (paramUnit.equals("Rate"))
                return aReadRateStat;
        } else if (paramType.equals("Write")) {
            if (paramUnit.equals("Size"))
                return aWriteSizeStat;
            else if (paramUnit.equals("Time"))
                return aWriteTimeStat;
            else if (paramUnit.equals("Rate"))
                return aWriteRateStat;
        } else if (paramType.equals("Rename")) {
            if (paramUnit.equals("Time"))
                return aRenameTimeStat;
        } else if (paramType.equals("Delete")) {
            if (paramUnit.equals("Time"))
                return aDeleteTimeStat;
        }

        return null;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Reads this state object from the path file name.
     *
     * @param pathFileName The path file name containing this state object.
     * @throws IOException
     */
    public void read(String pathFileName) throws IOException {
        FileInputBuffer fib = new FileInputBuffer(pathFileName);
        read(fib);
    }

    /**
     * Reads this state object from the provided file input buffer.
     *
     * @param fib The file input buffer from which this state object will be read.
     * @throws IOException
     */
    public void read(FileInputBuffer fib) throws IOException {
        aObjectName = fib.readString();
        aCurrInstanceCount = fib.readInt();
        aMaxInstanceCount = fib.readInt();
        aTotalInstanceCount = fib.readInt();
        aCurrLoadedCount = fib.readInt();
        aMaxLoadedCount = fib.readInt();
        aTotalLoadedCount = fib.readInt();
        aCurrReferenceCount = fib.readInt();
        aMaxReferenceCount = fib.readInt();
        aCurrInstanceMemory = fib.readLong();
        aMaxInstanceMemory = fib.readLong();
        aTotalInstanceMemory = fib.readLong();

        aTotalSizeStat = readServerStat(fib);
        aReadSizeStat = readServerStat(fib);
        aWriteSizeStat = readServerStat(fib);

        aTotalTimeStat = readServerStat(fib);
        aReadTimeStat = readServerStat(fib);
        aWriteTimeStat = readServerStat(fib);
        aRenameTimeStat = readServerStat(fib);
        aDeleteTimeStat = readServerStat(fib);

        aTotalRateStat = readServerStat(fib);
        aReadRateStat = readServerStat(fib);
        aWriteRateStat = readServerStat(fib);
        aIOFailureCount = null;
        int n = fib.readInt();
        if (n > 0) {
            aIOFailureCount = new ConcurrentHashMap<String, Integer>();
            for (int i = 0; i < n; ++i) {
                String srvrName = fib.readString();
                int failCnt = fib.readInt();
                aIOFailureCount.put(srvrName, failCnt);
            }
        }
        aIOWait0Count = fib.readInt();
        aTotalIODelayStat = readStat(fib);
        aIOWaitTimeStat = readStat(fib);
        aIOBlockingTimeStat = readStat(fib);
    }

    /**
     * Writes this state object to the input file ouput buffer.
     *
     * @param fob The file output buffer into which this state object will be
     *            written.
     * @throws IOException
     */
    public void write(FileOutputBuffer fob) throws IOException {
        fob.writeString(aObjectName);
        fob.writeInt(aCurrInstanceCount);
        fob.writeInt(aMaxInstanceCount);
        fob.writeInt(aTotalInstanceCount);
        fob.writeInt(aCurrLoadedCount);
        fob.writeInt(aMaxLoadedCount);
        fob.writeInt(aTotalLoadedCount);
        fob.writeInt(aCurrReferenceCount);
        fob.writeInt(aMaxReferenceCount);
        fob.writeLong(aCurrInstanceMemory);
        fob.writeLong(aMaxInstanceMemory);
        fob.writeLong(aTotalInstanceMemory);

        writeServerStat(fob, aTotalSizeStat);
        writeServerStat(fob, aReadSizeStat);
        writeServerStat(fob, aWriteSizeStat);

        writeServerStat(fob, aTotalTimeStat);
        writeServerStat(fob, aReadTimeStat);
        writeServerStat(fob, aWriteTimeStat);
        writeServerStat(fob, aRenameTimeStat);
        writeServerStat(fob, aDeleteTimeStat);

        writeServerStat(fob, aTotalRateStat);
        writeServerStat(fob, aReadRateStat);
        writeServerStat(fob, aWriteRateStat);
        if (aIOFailureCount == null)
            fob.writeInt(0);
        else {
            fob.writeInt(aIOFailureCount.size());
            for (Map.Entry<String, Integer> e : aIOFailureCount.entrySet()) {
                fob.writeString(e.getKey());
                fob.writeInt(e.getValue());
            }
        }

        fob.writeInt(aIOWait0Count);
        writeStat(fob, aTotalIODelayStat);
        writeStat(fob, aIOWaitTimeStat);
        writeStat(fob, aIOBlockingTimeStat);
    }

    /**
     * Reads a statistic from the input file input buffer. If the statistic was
     * not provided a null reference is returned.
     *
     * @param fib The input file input buffer from which the statistic is read.
     * @return The input statistic or null if it was not provided.
     * @throws IOException
     */
    private Statistic readStat(FileInputBuffer fib) throws IOException {
        Statistic stat = null;

        int b = fib.readInt();
        if (b == 1) {
            stat = new Statistic();
            stat.read(fib);
        }

        return stat;
    }

    /**
     * Writes the input statistic into the inpt file output buffer.
     *
     * @param fob  The file output buffer into which stat willl be written.
     * @param stat The statistic to be written.
     * @throws IOException
     */
    private void writeStat(FileOutputBuffer fob, Statistic stat) throws IOException {
        if (stat == null)
            fob.writeInt(0);
        else {
            fob.writeInt(1);
            stat.write(fob);
        }
    }

    /**
     * Reads a server statistic from the input file input buffer.
     *
     * @param fib The file input buffer from which the server statistic will be
     *            read.
     * @return The input map of servers associated with their statistic.
     * @throws IOException
     */
    private ConcurrentHashMap<String, Statistic>
    readServerStat(FileInputBuffer fib) throws IOException {
        ConcurrentHashMap<String, Statistic> srvrStat = null;

        int n = fib.readInt();
        if (n > 0) {
            srvrStat = new ConcurrentHashMap<String, Statistic>();
            for (int i = 0; i < n; ++i) {
                String srvrName = fib.readString();
                Statistic stat = new Statistic();
                stat.read(fib);
                srvrStat.put(srvrName, stat);
            }
        }

        return srvrStat;
    }

    /**
     * Writes the input map of server statistic into the file output buffer.
     *
     * @param fob      The file output buffer into which the map of server statistics
     *                 will be written.
     * @param srvrStat The map of server statistics to be written.
     * @throws IOException
     */
    private void writeServerStat(FileOutputBuffer fob,
                                 ConcurrentHashMap<String, Statistic> srvrStat)
            throws IOException {
        if (srvrStat == null)
            fob.writeInt(0);
        else {
            fob.writeInt(srvrStat.size());
            for (Map.Entry<String, Statistic> e : srvrStat.entrySet()) {
                fob.writeString(e.getKey());
                e.getValue().write(fob);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    //   IOMIObjectState Object Name = aObjectName
    //
    //       Current Instance Count       = aCurrInstanceCount
    //       Maximum Instance Count       = aMaxInstanceCount
    //       Total   Instance Count       = aTotalInstanceCount
    //
    //       Current Instance Memory      = aCurrInstanceMemory
    //       Maximum Instance Memory      = aMaxInstanceMemory
    //       Total   Instance Memory      = aTotalInstanceMemory
    //
    //       IO Wait 0 Time Count         = aIOWait0Count
    //
    //       File IO Statistic     Count   Min    Max   Mean   Std.Dev.  RMS    Sum
    //       -----------------------------------------------------------------------
    //       IO Wait Time          #####  #####  #####  #####   #####   #####  #####
    //       IO Blocking Time      #####  #####  #####  #####   #####   #####  #####
    //
    //       Server ALL:  //           File IO Statistic     Count   Min    Max   Mean   Std.Dev.  RMS    Sum
    //           -----------------------------------------------------------------------
    //           Read Memory           #####  #####  #####  #####   #####   #####  #####
    //           Write Memory          #####  #####  #####  #####   #####   #####  #####
    //
    //           Read Time             #####  #####  #####  #####   #####   #####  #####
    //           Write Time            #####  #####  #####  #####   #####   #####  #####
    //           Rename Time           #####  #####  #####  #####   #####   #####  #####
    //           Delete Time           #####  #####  #####  #####   #####   #####  #####
    //
    //           IO Failure Count    = #####
    //
    //       Server A:
    //           File IO Statistic     Count   Min    Max   Mean   Std.Dev.  RMS    Sum
    //           -----------------------------------------------------------------------
    //           Read Memory           #####  #####  #####  #####   #####   #####  #####
    //           Write Memory          #####  #####  #####  #####   #####   #####  #####
    //
    //           Read Time             #####  #####  #####  #####   #####   #####  #####
    //           Write Time            #####  #####  #####  #####   #####   #####  #####
    //           Rename Time           #####  #####  #####  #####   #####   #####  #####
    //           Delete Time           #####  #####  #####  #####   #####   #####  #####
    //
    //           IO Failure Count    = #####
    //       ...
    //
    //       Server Z:
    //           File IO Statistic     Count   Min    Max   Mean   Std.Dev.  RMS    Sum
    //           -----------------------------------------------------------------------
    //           Read Memory           #####  #####  #####  #####   #####   #####  #####
    //           Write Memory          #####  #####  #####  #####   #####   #####  #####
    //
    //           Read Time             #####  #####  #####  #####   #####   #####  #####
    //           Write Time            #####  #####  #####  #####   #####   #####  #####
    //           Rename Time           #####  #####  #####  #####   #####   #####  #####
    //           Delete Time           #####  #####  #####  #####   #####   #####  #####
    //
    //           IO Failure Count    = #####
    //

    /**
     * Standard to string output.
     */
    @Override
    public String toString() {
        return toString("");
    }

    /**
     * Returns a string that defined this objects content.
     *
     * @param hdr A string header appended to the beginning of every output line.
     * @return A string that defined this objects content.
     */
    public String toString(String hdr) {
        String s = "";

        // output header line and object name
        s += hdr + Globals.repeat("\"", 140) + NL;
        s += hdr + "Object  Name = " + aObjectName + NL + NL;

        // output instance and memory loads for this object state

        s += hdr + "Object Host Instance and Memory Loads:" + NL + NL;

        s += hdr + "    Total   Instance Count       = " +
                String.format("%5d", aTotalInstanceCount) + NL;
        s += hdr + "    Maximum Instance Count       = " +
                String.format("%5d", aMaxInstanceCount) + NL;
        s += hdr + "    Current Instance Count       = " +
                String.format("%5d", aCurrInstanceCount) + NL + NL;

        s += hdr + "    Total   Loaded Count         = " +
                String.format("%5d", aTotalLoadedCount) + NL;
        s += hdr + "    Maximum Loaded Count         = " +
                String.format("%5d", aMaxLoadedCount) + NL;
        s += hdr + "    Current Loaded Count         = " +
                String.format("%5d", aCurrLoadedCount) + NL + NL;

        s += hdr + "    Maximum Reference Count      = " +
                String.format("%5d", aMaxReferenceCount) + NL;
        s += hdr + "    Current Reference Count      = " +
                String.format("%5d", aCurrReferenceCount) + NL + NL;

        s += hdr + "    Total   Instance Memory      = " +
                Globals.memoryUnit(aTotalInstanceMemory) + NL;
        s += hdr + "    Maximum Instance Memory      = " +
                Globals.memoryUnit(aMaxInstanceMemory) + NL;
        s += hdr + "    Current Instance Memory      = " +
                Globals.memoryUnit(aCurrInstanceMemory) + NL + NL;

        // output thread delay table header

        s += hdr + "Object Thread Delay Time Information:" + NL + NL;

        s += hdr + "    IO Zero Wait Time Count      = " +
                String.format("%5d", aIOWait0Count) + NL + NL;

        // assemble row name, statistic, and unit entries for each row

        ArrayList<String> rowName = new ArrayList<String>();
        ArrayList<Statistic> rowStat = new ArrayList<Statistic>();
        ArrayList<TableUnit> rowUnit = new ArrayList<TableUnit>();

        int[] totalRows = {-1, -1, -1};
        boolean useTotalColumn = false;
        if ((aIOWaitTimeStat != null) &&
                (aIOBlockingTimeStat != null)) {
            // both wait and blocking times exist ... add a total line also

            totalRows[1] = 0;
            useTotalColumn = true;
            rowName.add("Total IO Delay Time");
            rowStat.add(aTotalIODelayStat);
            rowUnit.add(TableUnit.TIME_UNIT);
            rowName.add("IO Wait Time");
            rowStat.add(aIOWaitTimeStat);
            rowUnit.add(TableUnit.TIME_UNIT);
            rowName.add("IO Blocking Time");
            rowStat.add(aIOBlockingTimeStat);
            rowUnit.add(TableUnit.TIME_UNIT);
        } else if (aIOWaitTimeStat != null) {
            // only wait time exists ... add line

            rowName.add("IO Wait Time");
            rowStat.add(aIOWaitTimeStat);
            rowUnit.add(TableUnit.TIME_UNIT);
        } else if (aIOBlockingTimeStat != null) {
            // only blocking time exists ... add line

            rowName.add("IO Blocking Time");
            rowStat.add(aIOBlockingTimeStat);
            rowUnit.add(TableUnit.TIME_UNIT);
        }

        // output delay time table if any rows were added

        if (rowName.size() > 0) {
            int[] column0 = new int[1];
            s += NL + statTable(hdr + "    ", useTotalColumn, "IO Delay Statistic",
                    rowName, rowStat, rowUnit, totalRows, column0);
        }

        // Output each unique server

        s += NL + hdr + "File IO Server Statistics:" + NL;

        HashSet<String> uniqueServerSet = getUniqueServerSet();
        if (uniqueServerSet.size() > 0) {
            // if only one unique server exists and output that server ... otherwise
            // output "ALL" and each server

            if (uniqueServerSet.size() == 2) {
                uniqueServerSet.remove("ALL");
                s += NL + fileIOStatTable(hdr + "    ",
                        uniqueServerSet.iterator().next());
            } else {
                s += NL + fileIOStatTable(hdr + "    ", "ALL");
                uniqueServerSet.remove("ALL");
                for (String srvrTag : uniqueServerSet)
                    s += NL + fileIOStatTable(hdr + "    ", srvrTag);
            }
        }

        return s;
    }

    /**
     * Outputs a statistics table for IO statistics defined for the input server
     * tag. If no valid entries exist an empty string is returned. Only the valid
     * rows that are defined in the table below are returned.
     * <p>
     * Server NAME:
     * File IO Statistic     Count   Min    Max   Mean   Std.Dev.  RMS    Sum
     * -----------------------------------------------------------------------
     * Read Memory           #####  #####  #####  #####   #####   #####  #####
     * Write Memory          #####  #####  #####  #####   #####   #####  #####
     * <p>
     * Read Time             #####  #####  #####  #####   #####   #####  #####
     * Write Time            #####  #####  #####  #####   #####   #####  #####
     * Rename Time           #####  #####  #####  #####   #####   #####  #####
     * Delete Time           #####  #####  #####  #####   #####   #####  #####
     * <p>
     * IO Failure Count    = #####
     *
     * @param hdr     Row space header.
     * @param srvrTag The server tag whose IO table is to be output.
     * @return The string containing the table formatted as above.
     */
    private String fileIOStatTable(String hdr, String srvrTag) {
        String s = "";

        int[] totalRows = {-1, -1, -1};
        ArrayList<String> rowName = new ArrayList<String>();
        ArrayList<Statistic> rowStat = new ArrayList<Statistic>();
        ArrayList<TableUnit> rowUnit = new ArrayList<TableUnit>();
        boolean useTotalColumn = getValidStats(srvrTag, rowName, rowStat,
                rowUnit, totalRows);
        if (rowName.size() == 0) return "";

        int[] column0 = new int[1];
        s += hdr + "Server: " + srvrTag + NL + NL;
        s += statTable(hdr + "    ", useTotalColumn, "File IO Statistic",
                rowName, rowStat, rowUnit, totalRows, column0);
        s += NL;
        s += hdr + "    IO Failure Count" + Globals.repeat(" ", column0[0] - 18) +
                "= " + String.format("%5d", getIOFailureCount(srvrTag)) + NL;

        return s;
    }

    /**
     * Similar to fileIOStatTable except an ArrayList<String[]> DataRow table is
     * populated for the HostStateMonitor GUI.
     *
     * @param srvrTag The server whose table data is to be returned.
     * @return The ArrayList<String[]> DataRow table for the input server.
     */
    public ArrayList<String[]> getTableRowData(String srvrTag) {
        // create returned data lists and call getValidStats

        int[] totalRows = {-1, -1, -1};
        ArrayList<String> rowName = new ArrayList<String>();
        ArrayList<Statistic> rowStat = new ArrayList<Statistic>();
        ArrayList<TableUnit> rowUnit = new ArrayList<TableUnit>();
        boolean useTotalColumn = getValidStats(srvrTag, rowName, rowStat,
                rowUnit, totalRows);

        // create row data array list to be returned to caller and build column
        // header

        ArrayList<String[]> rowData = new ArrayList<String[]>();
        int ncols = 8;
        if (useTotalColumn) ++ncols;
        String[] colHdr = new String[ncols];
        colHdr[0] = "File IO Statistic";
        colHdr[1] = "Count";
        colHdr[2] = "Minimum";
        colHdr[3] = "Maximum";
        colHdr[4] = "Mean";
        colHdr[5] = "Std. Dev.";
        colHdr[6] = "RMS";
        colHdr[7] = "Sum";
        if (useTotalColumn) colHdr[8] = "% of Total";

        // loop over all statistic rows and fill row data

        for (int i = 0; i < rowStat.size(); ++i) {
            // get next row of statistic and entries. If statistic is null add a row
            // of empty strings.

            Statistic stat = rowStat.get(i);
            String[] rowEntries = new String[ncols];
            rowData.add(rowEntries);
            if (stat == null)
                for (int j = 0; j < ncols; ++j) rowEntries[j] = "";
            else {
                // non-null statistic ... determine if row is size, time, or rate.

                rowEntries[0] = rowName.get(i);
                rowEntries[1] = Integer.toString(stat.getCount());
                if (rowUnit.get(i) == TableUnit.TIME_UNIT) {
                    // time row ... create column entries

                    rowEntries[2] = Globals.timeStringAbbrvUnits(stat.getMinimum());
                    rowEntries[3] = Globals.timeStringAbbrvUnits(stat.getMaximum());
                    rowEntries[4] = Globals.timeStringAbbrvUnits(stat.getMean());
                    rowEntries[5] = Globals.timeStringAbbrvUnits(stat.getStdDev());
                    rowEntries[6] = Globals.timeStringAbbrvUnits(stat.getRMS());
                    rowEntries[7] = Globals.timeStringAbbrvUnits(stat.getSum());

                    // add totals column if requested.

                    if (useTotalColumn) {
                        if ((totalRows[1] == -1) || (totalRows[1] > i))
                            rowEntries[8] = " 100.00 %";
                        else
                            rowEntries[8] = String.format("%7.2f",
                                    100.0 * stat.getSum() /
                                            rowStat.get(totalRows[1]).getSum()) + " %";
                    }
                } else if ((rowUnit.get(i) == TableUnit.SIZE_UNIT) ||
                        (rowUnit.get(i) == TableUnit.RATE_UNIT)) {
                    // size or rate statistic ... create column entries

                    String rateApnd = "";
                    if (rowUnit.get(i) == TableUnit.RATE_UNIT) rateApnd = "/sec";
                    rowEntries[2] = Globals.memoryUnit((long) stat.getMinimum()) + rateApnd;
                    rowEntries[3] = Globals.memoryUnit((long) stat.getMaximum()) + rateApnd;
                    rowEntries[4] = Globals.memoryUnit((long) stat.getMean()) + rateApnd;
                    rowEntries[5] = Globals.memoryUnit((long) stat.getStdDev()) + rateApnd;
                    rowEntries[6] = Globals.memoryUnit((long) stat.getRMS()) + rateApnd;
                    rowEntries[7] = Globals.memoryUnit((long) stat.getSum()) + rateApnd;

                    // add totals column if requested.

                    if (useTotalColumn) {
                        int rowType = 0;
                        if (rowUnit.get(i) == TableUnit.RATE_UNIT) rowType = 2;
                        if (totalRows[rowType] == -1)
                            rowEntries[8] = "--NA--";
                        else
                            rowEntries[8] = String.format("%7.2f",
                                    100.0 * stat.getSum() /
                                            rowStat.get(totalRows[rowType]).getSum()) + " %";
                    }
                }
            }
        }

        // add column header as last row and return to caller

        rowData.add(colHdr);
        return rowData;
    }

    /**
     * Static function that dumps a formatted statistics table to a string.
     *
     * @param hdr            The header appended to the beginning of every line.
     * @param useTotalColumn A flag indicating if the "Totals" column should be
     *                       output.
     * @param tableRowColHdr The table/row column header string.
     * @param rowName        The row name header list.
     * @param rowStat        The row statistic list.
     * @param rowUnit        The row unit list (a TableUnit).
     * @param totalRows      An array for Size (bytes), Time, and Rate totals
     *                       row index. If an entry is -1 that total row is not
     *                       output.
     * @param column0        The total table width returned to the caller.
     * @return The returned formatted string for this table.
     */
    public static String statTable(String hdr, boolean useTotalColumn,
                                   String tableRowColHdr,
                                   ArrayList<String> rowName,
                                   ArrayList<Statistic> rowStat,
                                   ArrayList<TableUnit> rowUnit,
                                   int[] totalRows,
                                   int[] column0) {
        // get statistic column names

        String s = "";
        String[][] colhdr = fileIOColumnNames(useTotalColumn);

        // rowName contains a list of all applicable row headers,
        // rowStat contains a list of associated Statistic objects, and
        // rowUnit contains a list of the associated TableUnit.
        // TableUnit = NO_UNIT implies a blank line
        // entry i, where rowName.get(i).equals("") and rowStat.get(i) == null,
        // is the demarcation between memory and time entries.

        // create entries array and loop over each row of statistic objects

        String[][] entries = new String[rowStat.size()][colhdr[0].length];
        for (int i = 0; i < rowStat.size(); ++i) {
            // get next row of statistic and entries. If statistic is null add a row
            // of empty strings.

            Statistic stat = rowStat.get(i);
            String[] rowEntries = entries[i];
            if (stat == null) {
                for (int j = 0; j < rowEntries.length; ++j) rowEntries[j] = "";
            } else {
                // non-null statistic ... determine if row is size, time, or rate.

                rowEntries[0] = Integer.toString(stat.getCount());
                if (rowUnit.get(i) == TableUnit.TIME_UNIT) {
                    // time row ... create column entries

                    rowEntries[1] = Globals.timeStringAbbrvUnits(stat.getMinimum());
                    rowEntries[2] = Globals.timeStringAbbrvUnits(stat.getMaximum());
                    rowEntries[3] = Globals.timeStringAbbrvUnits(stat.getMean());
                    rowEntries[4] = Globals.timeStringAbbrvUnits(stat.getStdDev());
                    rowEntries[5] = Globals.timeStringAbbrvUnits(stat.getRMS());
                    rowEntries[6] = Globals.timeStringAbbrvUnits(stat.getSum());

                    // add totals column if requested.

                    if (useTotalColumn) {
                        if ((totalRows[1] == -1) || (totalRows[1] > i))
                            rowEntries[7] = "100.00";
                        else
                            rowEntries[7] = String.format("%6.2f",
                                    100.0 * stat.getSum() /
                                            rowStat.get(totalRows[1]).getSum());
                    }
                } else if ((rowUnit.get(i) == TableUnit.SIZE_UNIT) ||
                        (rowUnit.get(i) == TableUnit.RATE_UNIT)) {
                    // size or rate statistic ... create column entries

                    String rateApnd = "";
                    if (rowUnit.get(i) == TableUnit.RATE_UNIT) rateApnd = "/sec";
                    rowEntries[1] = Globals.memoryUnit((long) stat.getMinimum()) + rateApnd;
                    rowEntries[2] = Globals.memoryUnit((long) stat.getMaximum()) + rateApnd;
                    rowEntries[3] = Globals.memoryUnit((long) stat.getMean()) + rateApnd;
                    rowEntries[4] = Globals.memoryUnit((long) stat.getStdDev()) + rateApnd;
                    rowEntries[5] = Globals.memoryUnit((long) stat.getRMS()) + rateApnd;
                    rowEntries[6] = Globals.memoryUnit((long) stat.getSum()) + rateApnd;

                    // add totals column if requested.

                    if (useTotalColumn) {
                        int rowType = 0;
                        if (rowUnit.get(i) == TableUnit.RATE_UNIT) rowType = 2;
                        if (totalRows[rowType] == -1)
                            rowEntries[7] = "--NA--";
                        else
                            rowEntries[7] = String.format("%6.2f",
                                    100.0 * stat.getSum() /
                                            rowStat.get(totalRows[rowType]).getSum());
                    }
                }
            }
        }

        // now find the maximum column width for all columns ... first column 0
        // which is the row name header.

        int[] maxColWidth = new int[colhdr[0].length + 1];
        maxColWidth[0] = tableRowColHdr.length();
        for (int i = 0; i < rowName.size(); ++i) {
            String name = rowName.get(i);
            if (maxColWidth[0] < name.length()) maxColWidth[0] = name.length();
        }

        // now for all column headers set as maximum column

        for (int j = 0; j < colhdr[0].length; ++j)
            maxColWidth[j + 1] = colhdr[0][j].length();

        for (int i = 1; i < colhdr.length; ++i)
            for (int j = 0; j < colhdr[0].length; ++j)
                if (maxColWidth[j + 1] < colhdr[i][j].length())
                    maxColWidth[j + 1] = colhdr[i][j].length();

        // now test entry lengths to against maximum column widths

        for (int i = 0; i < rowName.size(); ++i) {
            String[] rowEntries = entries[i];
            for (int j = 0; j < rowEntries.length; ++j)
                if (maxColWidth[j + 1] < rowEntries[j].length())
                    maxColWidth[j + 1] = rowEntries[j].length();
        }

        // add 3 more spaces to maximum column widths to give each column needed
        // separation

        int colSum = 0;
        for (int i = 0; i < maxColWidth.length; ++i) {
            maxColWidth[i] += 3;
            colSum += maxColWidth[i];
        }

        // now build column header string

        for (int i = 0; i < colhdr.length; ++i) {
            if (i == colhdr.length - 1)
                s += hdr + Globals.leftJustifyString(tableRowColHdr,
                        maxColWidth[0]);
            else
                s += hdr + Globals.repeat(" ", maxColWidth[0]);

            for (int j = 0; j < colhdr[0].length; ++j)
                s += Globals.centerString(colhdr[i][j], maxColWidth[j + 1], true);
            s += NL;
        }
        s += hdr + Globals.repeat("-", colSum) + NL;

        // now build table string

        for (int i = 0; i < entries.length; ++i) {
            // get row name and row entry list ... if row is empty then add NL

            String rnam = rowName.get(i);
            String[] rowEntries = entries[i];
            if (rnam.equals("")) {
                if (i > 0) s += NL;
            } else {
                // row not empty ... add each column to row and then NL

                s += hdr + Globals.leftJustifyString(rnam, maxColWidth[0]);
                for (int j = 0; j < rowEntries.length; ++j)
                    s += Globals.leftJustifyString(rowEntries[j], maxColWidth[j + 1]);
                s += NL;
            }
        }

        // done ... set table width for return and return string

        column0[0] = maxColWidth[0];
        return s;
    }

    /**
     * Returns the unique set of server names gathered from all input statistics.
     *
     * @return The unique set of server names gathered from all input statistics.
     */
    public HashSet<String> getUniqueServerSet() {
        HashSet<String> uniqueServerNames = new HashSet<String>();
        if (aTotalSizeStat != null)
            uniqueServerNames.addAll(aTotalSizeStat.keySet());
        if (aReadSizeStat != null)
            uniqueServerNames.addAll(aReadSizeStat.keySet());
        if (aWriteSizeStat != null)
            uniqueServerNames.addAll(aWriteSizeStat.keySet());
        if (aTotalTimeStat != null)
            uniqueServerNames.addAll(aTotalTimeStat.keySet());
        if (aReadTimeStat != null)
            uniqueServerNames.addAll(aReadTimeStat.keySet());
        if (aWriteTimeStat != null)
            uniqueServerNames.addAll(aWriteTimeStat.keySet());
        if (aRenameTimeStat != null)
            uniqueServerNames.addAll(aRenameTimeStat.keySet());
        if (aDeleteTimeStat != null)
            uniqueServerNames.addAll(aDeleteTimeStat.keySet());
        if (aTotalRateStat != null)
            uniqueServerNames.addAll(aTotalRateStat.keySet());
        if (aReadRateStat != null)
            uniqueServerNames.addAll(aReadRateStat.keySet());
        if (aWriteRateStat != null)
            uniqueServerNames.addAll(aWriteRateStat.keySet());
        if (aIOFailureCount != null)
            uniqueServerNames.addAll(aIOFailureCount.keySet());

        return uniqueServerNames;
    }

    /**
     * The table unit used to assemble the statistics output tables. Row types
     * include none (NO_UNIT), size (SIZE_UNIT) for bytes, time (TIME_UNIT), and
     * rate (RATE_UNIT) which is size per time.
     *
     * @author jrhipp
     */
    public static enum TableUnit {
        NO_UNIT,
        SIZE_UNIT,
        TIME_UNIT,
        RATE_UNIT;
    }

    /**
     * Returns the row lists of server statistics for the input server tag.
     * Called to build a server statistics output table.
     *
     * @param srvrTag   The input server tag for which the output table row lists
     *                  will be assembled.
     * @param rowName   The list of row names.
     * @param rowStat   The list of row statistics.
     * @param rowUnit   The list of row units.
     * @param totalRows A flag of three entries for each row type (size, time, or
     *                  rate) that indicates the row that the totals output is
     *                  defined. If -1 that totals output is not provided.
     * @return True any totalsRows setting is defined.
     */
    private boolean getValidStats(String srvrTag,
                                  ArrayList<String> rowName,
                                  ArrayList<Statistic> rowStat,
                                  ArrayList<TableUnit> rowUnit,
                                  int[] totalRows) {
        // set the total column flag to false and see if both read and write size
        // statistics were defined for this server.

        boolean useTotalColumn = false;
        if ((aReadSizeStat != null) && aReadSizeStat.containsKey(srvrTag) &&
                (aWriteSizeStat != null) && aWriteSizeStat.containsKey(srvrTag)) {
            // read and write statistics for this server were defined. set size ([0])
            // totals to current row entry index and set use totals column flag to
            // true.

            totalRows[0] = rowName.size();
            useTotalColumn = true;

            // add row name, stat, and unit for total size, read size, and write size

            rowName.add("Total Size");
            rowStat.add(aTotalSizeStat.get(srvrTag));
            rowUnit.add(TableUnit.SIZE_UNIT);
            rowName.add("Read Size");
            rowStat.add(aReadSizeStat.get(srvrTag));
            rowUnit.add(TableUnit.SIZE_UNIT);
            rowName.add("Write Size");
            rowStat.add(aWriteSizeStat.get(srvrTag));
            rowUnit.add(TableUnit.SIZE_UNIT);

            // add a blank line

            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            // add row name, stat, and unit for total time, read time, and write time

            totalRows[1] = rowName.size();
            rowName.add("Total Time");
            rowStat.add(aTotalTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
            rowName.add("Read Time");
            rowStat.add(aReadTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
            rowName.add("Write Time");
            rowStat.add(aWriteTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);

            // add row name, stat, and unit for rename time if defined.

            if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag)) {
                rowName.add("Rename Time");
                rowStat.add(aRenameTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add row name, stat, and unit for delete time if defined.

            if ((aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
                rowName.add("Delete Time");
                rowStat.add(aDeleteTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add a blank line

            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            // add row name, stat, and unit for total rate, read rate, and write rate

            totalRows[2] = rowName.size();
            rowName.add("Total Rate");
            rowStat.add(aTotalRateStat.get(srvrTag));
            rowUnit.add(TableUnit.RATE_UNIT);
            rowName.add("Read Rate");
            rowStat.add(aReadRateStat.get(srvrTag));
            rowUnit.add(TableUnit.RATE_UNIT);
            rowName.add("Write Rate");
            rowStat.add(aWriteRateStat.get(srvrTag));
            rowUnit.add(TableUnit.RATE_UNIT);
        } else if ((aReadSizeStat != null) && aReadSizeStat.containsKey(srvrTag)) {
            // write statistics for this server were not defined but read statistics
            // were ... define read size row, stat, and unit only

            rowName.add("Read Size");
            rowStat.add(aReadSizeStat.get(srvrTag));
            rowUnit.add(TableUnit.SIZE_UNIT);

            // add a blank line

            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            // if rename or delete time statistics were defined then add total time
            // row

            if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag) ||
                    (aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
                totalRows[1] = rowName.size();
                useTotalColumn = true;
                rowName.add("Total Time");
                rowStat.add(aTotalTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add time name, stat, and unit row for read

            rowName.add("Read Time");
            rowStat.add(aReadTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);

            // add row name, stat, and unit for rename time if defined.

            if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag)) {
                rowName.add("Rename Time");
                rowStat.add(aRenameTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add row name, stat, and unit for delete time if defined.

            if ((aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
                rowName.add("Delete Time");
                rowStat.add(aDeleteTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add a blank line

            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            // add row name, stat, and unit for read rate.

            rowName.add("Read Rate");
            rowStat.add(aReadRateStat.get(srvrTag));
            rowUnit.add(TableUnit.RATE_UNIT);
        } else if ((aWriteSizeStat != null) && aWriteSizeStat.containsKey(srvrTag)) {
            // read statistics for this server were not defined but write statistics
            // were ... define write size row, stat, and unit only

            rowName.add("Write Size");
            rowStat.add(aWriteSizeStat.get(srvrTag));
            rowUnit.add(TableUnit.SIZE_UNIT);

            // add a blank line

            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            // if rename or delete time statistics were defined then add total time
            // row

            if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag) ||
                    (aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
                totalRows[1] = rowName.size();
                useTotalColumn = true;
                rowName.add("Total Time");
                rowStat.add(aTotalTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add time name, stat, and unit row for write

            rowName.add("Write Time");
            rowStat.add(aWriteTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);

            // add row name, stat, and unit for rename time if defined.

            if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag)) {
                rowName.add("Rename Time");
                rowStat.add(aRenameTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add row name, stat, and unit for delete time if defined.

            if ((aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
                rowName.add("Delete Time");
                rowStat.add(aDeleteTimeStat.get(srvrTag));
                rowUnit.add(TableUnit.TIME_UNIT);
            }

            // add a blank line

            rowName.add("");
            rowStat.add(null);
            rowUnit.add(TableUnit.NO_UNIT);

            // add row name, stat, and unit for write rate.

            rowName.add("Write Rate");
            rowStat.add(aWriteRateStat.get(srvrTag));
            rowUnit.add(TableUnit.RATE_UNIT);
        } else if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag) &&
                (aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
            // read and write statistics for this server were not defined but both
            // rename and delete time were ... add a totals time row and then add the
            // row name, statistic, and unit for the totals row, rename row, and the
            // delete row.

            totalRows[1] = rowName.size();
            useTotalColumn = true;
            rowName.add("Total Time");
            rowStat.add(aTotalTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
            rowName.add("Rename Time");
            rowStat.add(aRenameTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
            rowName.add("Delete Time");
            rowStat.add(aDeleteTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
        } else if ((aRenameTimeStat != null) && aRenameTimeStat.containsKey(srvrTag)) {
            // read, write, and delete statistics for this server were not defined but both
            // rename was ... add a rename row, statistic, and unit

            rowName.add("Rename Time");
            rowStat.add(aRenameTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
        } else if ((aDeleteTimeStat != null) && aDeleteTimeStat.containsKey(srvrTag)) {
            // read, write, and rename statistics for this server were not defined but both
            // delete was ... add a delete row, statistic, and unit

            rowName.add("Delete Time");
            rowStat.add(aDeleteTimeStat.get(srvrTag));
            rowUnit.add(TableUnit.TIME_UNIT);
        }

        // return the use total column flag and exit

        return useTotalColumn;
    }

    /**
     * Get the column headers for a server statistics table.
     *
     * @param useTotalColumn Add the "Totals" column if this flag is true.
     * @return The multi-line array of column headers.
     */
    private static String[][] fileIOColumnNames(boolean useTotalColumn) {
        int botRow = 0;
        String[][] colhdr;

        // see if totals row should be added

        if (useTotalColumn) {
            botRow = 1;
            colhdr = new String[2][8];
            for (int i = 0; i < 7; ++i)
                colhdr[0][i] = "";
            colhdr[0][7] = "% of";
        } else
            colhdr = new String[1][7];

        // add column headers

        colhdr[botRow][0] = "Count";
        colhdr[botRow][1] = "Minimum";
        colhdr[botRow][2] = "Maximum";
        colhdr[botRow][3] = "Mean";
        colhdr[botRow][4] = "Std. Dev.";
        colhdr[botRow][5] = "RMS";
        colhdr[botRow][6] = "Sum";
        if (useTotalColumn)
            colhdr[botRow][7] = "Total";

        return colhdr;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the IOMI object name.
     *
     * @return The IOMI object name.
     */
    public String getIOMIObjectName() {
        return aObjectName;
    }

    /**
     * Returns the current instance count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     *
     * @return The current instance count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     */
    public int getCurrentInstanceCount() {
        return aCurrInstanceCount;
    }

    /**
     * Returns the maximum instance count seen thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     *
     * @return The maximum instance count seen thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     */
    public int getMaximumInstanceCount() {
        return aMaxInstanceCount;
    }

    /**
     * Returns the total instance count created thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     *
     * @return The total instance count created thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     */
    public int getTotalInstanceCount() {
        return aTotalInstanceCount;
    }

    /**
     * Returns the current loaded count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     *
     * @return The current loaded count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     */
    public int getCurrentLoadedCount() {
        return aCurrLoadedCount;
    }

    /**
     * Returns the maximum loaded count seen thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     *
     * @return The maximum loaded count seen thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     */
    public int getMaximumLoadedCount() {
        return aMaxLoadedCount;
    }

    /**
     * Returns the total loaded count created thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     *
     * @return The total loaded count created thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     */
    public int getTotalLoadedCount() {
        return aTotalLoadedCount;
    }

    /**
     * Returns the current reference count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     *
     * @return The current reference count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     */
    public int getCurrentReferenceCount() {
        return aCurrReferenceCount;
    }

    /**
     * Returns the maximum reference count seen thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     *
     * @return The maximum reference count seen thus far for this IOMI object on
     * the host for which this IOMIObjectState is defined.
     */
    public int getMaximumReferenceCount() {
        return aMaxReferenceCount;
    }

    /**
     * Returns the current allocated instance memory for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     *
     * @return The current instance count for this IOMI object on the
     * host for which this IOMIObjectState is defined.
     */
    public long getCurrentInstanceMemory() {
        return aCurrInstanceMemory;
    }

    /**
     * Returns the maximum allocated memory seen thus far on this host for which
     * this IOMIObjectState is defined.
     *
     * @return The maximum allocated memory seen thus far on this host for which
     * this IOMIObjectState is defined.
     */
    public long getMaximumInstanceMemory() {
        return aMaxInstanceMemory;
    }

    /**
     * Returns the total memory allocated for this IOMIObjectState on this host
     * for since this state object was instantiated.
     *
     * @return The total memory allocated for this IOMIObjectState on this host
     * for since this state object was instantiated.
     */
    public long getTotalInstanceMemory() {
        return aTotalInstanceMemory;
    }

    /**
     * Returns the file IO total memory statistic detailing the total amount of
     * data read and/or written to/from disk for all file servers defined for
     * this object state.
     *
     * @return The file IO total memory statistic detailing the total amount of
     * data read and/or written to/from disk for all file servers defined
     * for this object state.
     */
    public Statistic getCummulativeFileTotalMemoryStatistic() {
        return getFileTotalMemoryStatistic("ALL");
    }

    /**
     * Returns the file IO total memory statistic detailing the total amount of
     * data read and/or written to/from disk for the specific file server defined
     * by the input server tag.
     *
     * @param srvrTag The input server tag for which all reads and writes to disk
     *                are detailed in the returned statistic.
     * @return The file IO total memory statistic detailing the total amount of
     * data read and/or written to/from disk for the specific file server
     * defined by the input server tag.
     */
    public Statistic getFileTotalMemoryStatistic(String srvrTag) {
        if (aTotalSizeStat == null) return null;
        return aTotalSizeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total read memory statistic detailing the amount of
     * data read from disk for all file servers defined for this object state.
     *
     * @return The file IO total read memory statistic detailing the amount of
     * data read from disk for all file servers defined for this object
     * state.
     */
    public Statistic getCummulativeFileReadMemoryStatistic() {
        return getFileReadMemoryStatistic("ALL");
    }

    /**
     * Returns the file IO read memory statistic detailing the amount of
     * data read from disk for the specific file server defined by the input
     * server tag.
     *
     * @param srvrTag The input server tag for which all reads from disk
     *                are detailed in the returned statistic.
     * @return The file IO read memory statistic detailing the amount of
     * data read from disk for the specific file server defined by the
     * input server tag.
     */
    public Statistic getFileReadMemoryStatistic(String srvrTag) {
        if (aReadSizeStat == null) return null;
        return aReadSizeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total write memory statistic detailing the amount of
     * data written to disk for all file servers defined for this object state.
     *
     * @return The file IO total write memory statistic detailing the amount of
     * data written to disk for all file servers defined for this object
     * state.
     */
    public Statistic getCummulativeFileWriteMemoryStatistic() {
        return getFileWriteMemoryStatistic("ALL");
    }

    /**
     * Returns the file IO write memory statistic detailing the amount of
     * data written to disk for the specific file server defined by the input
     * server tag.
     *
     * @param srvrTag The input server tag for which all writes to disk
     *                are detailed in the returned statistic.
     * @return The file IO write memory statistic detailing the amount of
     * data written to disk for the specific file server defined by the
     * input server tag.
     */
    public Statistic getFileWriteMemoryStatistic(String srvrTag) {
        if (aWriteSizeStat == null) return null;
        return aWriteSizeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent reading or writing objects of the type for which this state
     * was defined.
     *
     * @return The file IO total time statistic detailing the total amount of
     * time spent reading or writing objects of the type for which this
     * state was defined.
     */
    public Statistic getCummulativeFileTotalTimeStatistic() {
        if (aTotalTimeStat == null) return null;
        return aTotalTimeStat.get("ALL");
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent reading or writing objects of this type, for which this state
     * object was defined, to and from disk.
     *
     * @param srvrTag The input server tag for which all read and write times to
     *                disk are detailed in the returned statistic.
     * @return The file IO total time statistic detailing the total amount of
     * time spent reading or writing objects of this type, for which this
     * state object was defined, to and from disk.
     */
    public Statistic getFileTotalTimeStatistic(String srvrTag) {
        if (aTotalTimeStat == null) return null;
        return aTotalTimeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent reading objects of the type for which this state was defined.
     *
     * @return The file IO total time statistic detailing the total amount of
     * time spent reading objects of the type for which this state was
     * defined.
     */
    public Statistic getCummulativeFileReadTimeStatistic() {
        return getFileReadTimeStatistic("ALL");
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent reading objects of this type, for which this state
     * object was defined, from disk.
     *
     * @param srvrTag The input server tag for which all read times from disk
     *                are detailed in the returned statistic.
     * @return The file IO total time statistic detailing the total amount of
     * time spent reading objects of this type, for which this
     * state object was defined, from disk.
     */
    public Statistic getFileReadTimeStatistic(String srvrTag) {
        if (aReadTimeStat == null) return null;
        return aReadTimeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent writing objects of the type for which this state was defined.
     *
     * @return The file IO total time statistic detailing the total amount of
     * time spent writing objects of the type for which this state was
     * defined.
     */
    public Statistic getCummulativeFileWriteTimeStatistic() {
        return getFileWriteTimeStatistic("ALL");
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent writing objects of this type, for which this state
     * object was defined, to disk.
     *
     * @param srvrTag The input server tag for which all write times to disk
     *                are detailed in the returned statistic.
     * @return The file IO total time statistic detailing the total amount of
     * time spent writing objects of this type, for which this
     * state object was defined, to disk.
     */
    public Statistic getFileWriteTimeStatistic(String srvrTag) {
        if (aWriteTimeStat == null) return null;
        return aWriteTimeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent renaming objects of the type for which this state was defined.
     *
     * @return The file IO total time statistic detailing the total amount of
     * time spent renaming objects of the type for which this state was
     * defined.
     */
    public Statistic getCummulativeFileRenameTimeStatistic() {
        return getFileRenameTimeStatistic("ALL");
    }

    /**
     * Returns the file IO time statistic detailing the amount of
     * time spent renaming objects of this type, for which this state
     * object was defined, on disk.
     *
     * @param srvrTag The input server tag for which all rename times on disk
     *                are detailed in the returned statistic.
     * @return The file IO time statistic detailing the amount of time spent
     * renaming objects of this type, for which this state object was
     * defined, on disk.
     */
    public Statistic getFileRenameTimeStatistic(String srvrTag) {
        if (aRenameTimeStat == null) return null;
        return aRenameTimeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total time statistic detailing the total amount of
     * time spent deleting objects of the type for which this state was defined.
     *
     * @return The file IO total time statistic detailing the total amount of
     * time spent deleting objects of the type for which this state was
     * defined.
     */
    public Statistic getCummulativeFileDeleteTimeStatistic() {
        return getFileDeleteTimeStatistic("ALL");
    }

    /**
     * Returns the file IO time statistic detailing the amount of
     * time spent deleting objects of this type, for which this state
     * object was defined, from disk.
     *
     * @param srvrTag The input server tag for which all delete times from disk
     *                are detailed in the returned statistic.
     * @return The file IO time statistic detailing the amount of time spent
     * deleting objects of this type, for which this state object was
     * defined, from disk.
     */
    public Statistic getFileDeleteTimeStatistic(String srvrTag) {
        if (aDeleteTimeStat == null) return null;
        return aDeleteTimeStat.get(srvrTag);
    }

    /**
     * Returns the file IO total read/write (average) rate statistic detailing
     * the total read and write rates for all file servers defined for this
     * object state.
     *
     * @return The file IO total read/write (average) rate statistic detailing
     * the total read and write rates for all file servers defined for
     * this object state.
     */
    public Statistic getCummulativeFileTotalRateStatistic() {
        return getFileTotalRateStatistic("ALL");
    }

    /**
     * Returns the file IO read/write rate statistic detailing the read/write
     * rates for the specific file server defined by the input server tag.
     *
     * @param srvrTag The input server tag for which all read/write rates are
     *                detailed in the returned statistic.
     * @return The file IO read/write rate statistic detailing the read/write
     * rates for the specific file server defined by the input server tag.
     */
    public Statistic getFileTotalRateStatistic(String srvrTag) {
        if (aTotalRateStat == null) return null;
        return aTotalRateStat.get(srvrTag);
    }

    /**
     * Returns the file IO total read rate statistic detailing the total read
     * rate for all file servers defined for this object state.
     *
     * @return The file IO total read rate statistic detailing the total read
     * rate for all file servers defined for this object state.
     */
    public Statistic getCummulativeFileReadRateStatistic() {
        return getFileReadRateStatistic("ALL");
    }

    /**
     * Returns the file IO read rate statistic detailing the read rates for the
     * specific file server defined by the input server tag.
     *
     * @param srvrTag The input server tag for which all read rates are
     *                detailed in the returned statistic.
     * @return The file IO read rate statistic detailing the read rates for the
     * specific file server defined by the input server tag.
     */
    public Statistic getFileReadRateStatistic(String srvrTag) {
        if (aReadRateStat == null) return null;
        return aReadRateStat.get(srvrTag);
    }

    /**
     * Returns the file IO total write rate statistic detailing the total write
     * rate for all file servers defined for this object state.
     *
     * @return The file IO total write rate statistic detailing the total write
     * rate for all file servers defined for this object state.
     */
    public Statistic getCummulativeFileWriteRateStatistic() {
        return getFileWriteRateStatistic("ALL");
    }

    /**
     * Returns the file IO write rate statistic (bytes/sec) detailing the read
     * rates for the specific file server defined by the input server tag.
     *
     * @param srvrTag The input server tag for which all write rates are
     *                detailed in the returned statistic.
     * @return The file IO write rate statistic detailing the write rates
     * (bytes/sec) for the specific file server defined by the input
     * server tag.
     */
    public Statistic getFileWriteRateStatistic(String srvrTag) {
        if (aWriteRateStat == null) return null;
        return aWriteRateStat.get(srvrTag);
    }

    /**
     * Returns the total IO Failure count map for all servers (and the cumulative
     * result defined by "ALL") used by this object state.
     *
     * @return The total IO Failure count map for all servers (and the cumulative
     * result defined by "ALL") used by this object state.
     */
    public ConcurrentHashMap<String, Integer> getIOFailureCountMap() {
        return aIOFailureCount;
    }

    /**
     * Returns the set of server tags that are defined for the input parameter and
     * unit names.
     *
     * @param srvrName  If defined only this server name is defined in the set,
     *                  If, and only if, some statistic is defined for this
     *                  server. If empty (""), all server names are returned in
     *                  the set that have defined statistics.
     * @param paramName The parameter name ("Total", "Read", "Write", "Rename",
     *                  or "Delete").
     * @param unitName  Unit name ("Size", "Time", or "Rate").
     * @return The set of all servers that have the requested parameter and unit
     * statistics if srvrName = "", otherwise just srvrName if, and only
     * if, it has the requested statistic defined.
     */
    public HashSet<String> getServerIOStatisticSet(String srvrName,
                                                   String paramName,
                                                   String unitName) {
        HashSet<String> srvrSet = new HashSet<String>();

        // get the map of available servers defined for the input parameter name
        // and unit

        ConcurrentHashMap<String, Statistic> statMap = getServerStatistic(paramName, unitName);
        if (statMap != null) {
            // add all servers (except "ALL") if srvrName = "" ... otherwise only
            // add srvrName if it has a statistic defined.

            if (srvrName.equals("")) {
                for (String srvName : statMap.keySet())
                    if (!srvName.equals("ALL"))
                        srvrSet.add(srvName);
            } else if (statMap.get(srvrName) != null)
                srvrSet.add(srvrName);
        }

        // return set.

        return srvrSet;
    }


    /**
     * Returns the set of server tags that have IO failures defined for the
     * input srvrName. If none are defined and empty set is returned. If the
     * input srvrName = "" all severs that have IO failure statistics are
     * returned.
     */
    public HashSet<String> getServerIOFailureSet(String srvrName) {
        HashSet<String> srvrSet = new HashSet<String>();

        // return all servers with IO failures if srvrName = "". Otherwise, return
        // only srvrName if, and only if, it has IO Failures. Otherwise return an
        // empty set.

        if (srvrName.equals("")) {
            if (aIOFailureCount != null) {
                for (String srvName : aIOFailureCount.keySet())
                    if (!srvName.equals("ALL"))
                        srvrSet.add(srvName);
            }
        } else if (aIOFailureCount.get(srvrName) != null)
            srvrSet.add(srvrName);

        // return set.

        return srvrSet;
    }

    /**
     * Returns the IO failure count for the input server.
     *
     * @param srvrTag The input server tag for which the IO failure count will be
     *                returned.
     * @return The IO failure count for the input server.
     */
    public int getIOFailureCount(String srvrTag) {
        if (aIOFailureCount == null) return 0;
        Integer fc = aIOFailureCount.get(srvrTag);
        if (fc == null) return 0;
        return fc;
    }

    /**
     * Returns the total file IO failure count defining all file IO failures that
     * occurred for all file servers defined for this object state.
     *
     * @return The total file IO failure count defining all file IO failures that
     * occurred for all file servers defined for this object state.
     */
    public int getCummulativeIOFailureCount() {
        return getIOFailureCount("ALL");
    }

    /**
     * Returns the total file IO no-wait count (1 msec or less) that occurred
     * for all file servers defined for this object state.
     *
     * @return The total file IO no-wait count (1 msec or less) that occurred
     * for all file servers defined for this object state.
     */
    public int getIOWait0Count() {
        return aIOWait0Count;
    }

    /**
     * Returns the total IO delay statistic accumulated by all threads that
     * create and use the objects for which this state was defined. This includes
     * both IO wait time and thread blocking for other threads reading or
     * writing this object.
     *
     * @return The total IO delay statistic accumulated by all threads that
     * create and use the objects for which this state was defined. This
     * includes both IO wait time and thread blocking for other threads
     * reading or writing this object.
     */
    public Statistic getTotalIODelayStatistic() {
        return aTotalIODelayStat;
    }

    /**
     * Returns the total IO wait time statistic accumulated by all threads that
     * create and use the objects for which this state was defined.
     *
     * @return The total IO wait time statistic accumulated by all threads that
     * create and use the objects for which this state was defined.
     */
    public Statistic getIOWaitTimeStatistic() {
        return aIOWaitTimeStat;
    }

    /**
     * Returns the total IO blocking time statistic accumulated by all threads
     * that create and use the objects for which this state was defined.
     *
     * @return The total IO blocking time statistic accumulated by all threads
     * that create and use the objects for which this state was defined.
     */
    public Statistic getIOBlockingTimeStatistic() {
        return aIOBlockingTimeStat;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Sets this object states current instance count and memory to zero. This
     * function is only called by the IOMICumulativeHostState when a new
     * IOMIHostState object is created for an already defined host (a second
     * instance caused by a recycled Fabric host JVM).
     */
    public void zeroCurrentInstanceMemory() {
        aCurrInstanceCount = 0;
        aCurrLoadedCount = 0;
        aCurrInstanceMemory = 0;
        aCurrReferenceCount = 0;
    }

    /**
     * Synchronized function to increment the current object instance count.
     */
    private synchronized void incrementCurrentInstanceCount() {
        ++aCurrInstanceCount;
    }

    /**
     * Synchronized function to increment the current object loaded count.
     */
    private synchronized void incrementCurrentLoadedCount() {
        ++aCurrLoadedCount;
    }

    /**
     * Synchronized function to decrement the current object instance count.
     */
    private synchronized void decrementCurrentInstanceCount() {
        --aCurrInstanceCount;
    }

    /**
     * Synchronized function to decrement the current object loaded count.
     */
    private synchronized void decrementCurrentLoadedCount() {
        --aCurrLoadedCount;
    }

    /**
     * Synchronized function to increment the total object instance count.
     */
    private synchronized void incrementTotalInstanceCount() {
        ++aTotalInstanceCount;
    }

    /**
     * Synchronized function to increment the total object loaded count.
     */
    private synchronized void incrementTotalLoadedCount() {
        ++aTotalLoadedCount;
    }

    /**
     * Synchronized function to set the maximum in-core object instance count.
     */
    private synchronized void setMaximumInstanceCount() {
        if (aMaxInstanceCount < aCurrInstanceCount)
            aMaxInstanceCount = aCurrInstanceCount;
    }

    /**
     * Synchronized function to set the maximum in-core object loaded count.
     */
    private synchronized void setMaximumLoadedCount() {
        if (aMaxLoadedCount < aCurrLoadedCount)
            aMaxLoadedCount = aCurrLoadedCount;
    }

    /**
     * Synchronized function to increment the current object instance memory.
     */
    private synchronized void incrementCurrentInstanceMemory(long memory) {
        aCurrInstanceMemory += memory;
    }

    /**
     * Synchronized function to decrement the current object instance memory.
     */
    private synchronized void decrementCurrentInstanceMemory(long memory) {
        aCurrInstanceMemory -= memory;
        if (aCurrInstanceMemory < 0) aCurrInstanceMemory = 0;
    }

    /**
     * Synchronized function to increment the total object instance memory.
     */
    private synchronized void incrementTotalInstanceMemory(long memory) {
        aTotalInstanceMemory += memory;
    }

    /**
     * Synchronized function to set the maximum object instance memory.
     */
    private synchronized void setMaximumInstanceMemory() {
        if (aMaxInstanceMemory < aCurrInstanceMemory)
            aMaxInstanceMemory = aCurrInstanceMemory;
    }

    public synchronized void incrementReferenceCount() {
        ++aCurrReferenceCount;
        if (aMaxReferenceCount < aCurrReferenceCount)
            aMaxReferenceCount = aCurrReferenceCount;
    }

    public synchronized void decrementReferenceCount() {
        --aCurrReferenceCount;
    }

    /**
     * public function to increment all load counts (this includes current,
     * total, and maximum).
     */
    public void incrementLoadCount() {
        incrementCurrentLoadedCount();
        incrementTotalLoadedCount();
        setMaximumLoadedCount();
    }

    /**
     * public function to increment all instance count and memory values (this
     * includes current, total, and maximum).
     *
     * @param memory The memory amount by which the object memory is incremented.
     */
    public void incrementInstanceMemory(long memory) {
        incrementCurrentInstanceCount();
        incrementTotalInstanceCount();
        setMaximumInstanceCount();
        incrementMemory(memory);
    }

    /**
     * public function to increment all instance memory values (this
     * includes current, total, and maximum).
     *
     * @param memory The memory amount by which the object memory is incremented.
     */
    public void incrementMemory(long memory) {
        incrementCurrentInstanceMemory(memory);
        incrementTotalInstanceMemory(memory);
        setMaximumInstanceMemory();
    }

    /**
     * public function to decrement the current instance memory and count.
     *
     * @param memory    The memory amount by which the current object memory is
     *                  decremented.
     * @param extraLoad A flag indicating the object being destroyed was composed
     *                  of two separate loads (e.g. MatrixBlock held its data and
     *                  the transpose of its data).
     */
    public void decrementInstanceMemory(long memory, boolean extraLoad) {
        decrementCurrentInstanceCount();
        decrementCurrentInstanceMemory(memory);
        decrementCurrentLoadedCount();
        if (extraLoad) decrementCurrentLoadedCount();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Called when a File IO Read operation is completed to update the time,
     * size (memory), and failures (if any) of the read for a specific server
     * from which the read was performed.
     *
     * @param srvrTag  The server from which the read was made.
     * @param memory   The amount of data (bytes) read from the server.
     * @param time     The time (msec) to read the data.
     * @param failures The number of failures while reading the data.
     */
    public void updateFileRead(String srvrTag, long memory, long time,
                               int failures) {
        // add total and read size, time, and rate statistics for the input server
        // and the cumulative ("ALL") server.

        aTotalSizeStat = getTagStatisticMap(aTotalSizeStat);
        setTagStatistic(srvrTag, aTotalSizeStat, memory);
        setTagStatistic("ALL", aTotalSizeStat, memory);
        aReadSizeStat = getTagStatisticMap(aReadSizeStat);
        setTagStatistic(srvrTag, aReadSizeStat, memory);
        setTagStatistic("ALL", aReadSizeStat, memory);
        aTotalTimeStat = getTagStatisticMap(aTotalTimeStat);
        setTagStatistic(srvrTag, aTotalTimeStat, time);
        setTagStatistic("ALL", aTotalTimeStat, time);
        aReadTimeStat = getTagStatisticMap(aReadTimeStat);
        setTagStatistic(srvrTag, aReadTimeStat, time);
        setTagStatistic("ALL", aReadTimeStat, time);

        if (time == 0) ++time;
        aTotalRateStat = getTagStatisticMap(aTotalRateStat);
        setTagStatistic(srvrTag, aTotalRateStat, 1000.0 * memory / time);
        setTagStatistic("ALL", aTotalRateStat, 1000.0 * memory / time);
        aReadRateStat = getTagStatisticMap(aReadRateStat);
        setTagStatistic(srvrTag, aReadRateStat, 1000.0 * memory / time);
        setTagStatistic("ALL", aReadRateStat, 1000.0 * memory / time);
        updateIOFailures(srvrTag, failures);
    }

    /**
     * Called when a File IO Write operation is completed to update the time,
     * size (memory), and failures (if any) of the write for a specific server
     * from which the write was performed.
     *
     * @param srvrTag  The server from which the write was made.
     * @param memory   The amount of data (bytes) written to the server.
     * @param time     The time (msec) to write the data.
     * @param failures The number of failures while writing the data.
     */
    public void updateFileWrite(String srvrTag, long memory, long time,
                                int failures) {
        // add total and write size, time, and rate statistics for the input server
        // and the cumulative ("ALL") server.

        aTotalSizeStat = getTagStatisticMap(aTotalSizeStat);
        setTagStatistic(srvrTag, aTotalSizeStat, memory);
        setTagStatistic("ALL", aTotalSizeStat, memory);
        aWriteSizeStat = getTagStatisticMap(aWriteSizeStat);
        setTagStatistic(srvrTag, aWriteSizeStat, memory);
        setTagStatistic("ALL", aWriteSizeStat, memory);
        aTotalTimeStat = getTagStatisticMap(aTotalTimeStat);
        setTagStatistic(srvrTag, aTotalTimeStat, time);
        setTagStatistic("ALL", aTotalTimeStat, time);
        aWriteTimeStat = getTagStatisticMap(aWriteTimeStat);
        setTagStatistic(srvrTag, aWriteTimeStat, time);
        setTagStatistic("ALL", aWriteTimeStat, time);

        if (time == 0) ++time;
        aTotalRateStat = getTagStatisticMap(aTotalRateStat);
        setTagStatistic(srvrTag, aTotalRateStat, 1000.0 * memory / time);
        setTagStatistic("ALL", aTotalRateStat, 1000.0 * memory / time);
        aWriteRateStat = getTagStatisticMap(aWriteRateStat);
        setTagStatistic(srvrTag, aWriteRateStat, 1000.0 * memory / time);
        setTagStatistic("ALL", aWriteRateStat, 1000.0 * memory / time);
        updateIOFailures(srvrTag, failures);
    }

    /**
     * Called when a File IO rename operation is completed to update the time
     * and failures (if any) of the rename for a specific server on which the
     * rename was performed.
     *
     * @param srvrTag  The server on which the rename was made.
     * @param time     The time (msec) to rename the file.
     * @param failures The number of failures while renaming the file.
     */
    public void updateFileRename(String srvrTag, long time, int failures) {
        // add total and rename time statistics for the input server
        // and the cumulative ("ALL") server.

        aTotalTimeStat = getTagStatisticMap(aTotalTimeStat);
        setTagStatistic(srvrTag, aTotalTimeStat, time);
        setTagStatistic("ALL", aTotalTimeStat, time);
        aRenameTimeStat = getTagStatisticMap(aRenameTimeStat);
        setTagStatistic(srvrTag, aRenameTimeStat, time);
        setTagStatistic("ALL", aRenameTimeStat, time);
        updateIOFailures(srvrTag, failures);
    }

    /**
     * Called when a File IO delete operation is completed to update the time
     * and failures (if any) of the delete for a specific server on which the
     * delete was performed.
     *
     * @param srvrTag  The server on which the delete was made.
     * @param time     The time (msec) to delete the file.
     * @param failures The number of failures while deleting the file.
     */
    public void updateFileDelete(String srvrTag, long time, int failures) {
        // add total and rename time statistics for the input server
        // and the cumulative ("ALL") server.

        aTotalTimeStat = getTagStatisticMap(aTotalTimeStat);
        setTagStatistic(srvrTag, aTotalTimeStat, time);
        setTagStatistic("ALL", aTotalTimeStat, time);
        aDeleteTimeStat = getTagStatisticMap(aDeleteTimeStat);
        setTagStatistic(srvrTag, aDeleteTimeStat, time);
        setTagStatistic("ALL", aDeleteTimeStat, time);
        updateIOFailures(srvrTag, failures);
    }

    /**
     * Called when a File IO operation results in one or more failures on the
     * specific input file server.
     *
     * @param srvrTag  The server on which the failures occurred.
     * @param failures The number of failures that occurred.
     */
    private void updateIOFailures(String srvrTag, int failures) {
        if (failures > 0) {
            aIOFailureCount = getTagFailureMap(aIOFailureCount);
            addFailures(srvrTag, aIOFailureCount, failures);
            addFailures("ALL", aIOFailureCount, failures);
        }
    }

    /**
     * Called when a File IO operation is made to wait by the owning IOMIManager
     * object. The total delay time and the IO wait time statistic are both
     * updated.
     *
     * @param time The total IO wait time (msec) that accrued.
     */
    public void updateIOWait(long time) {
        // if time = 0 then increment the io wait 0 count ... otherwise update the
        // statistics

        if (time == 0)
            synchronized (this) {
                ++aIOWait0Count;
            }
        else {
            aTotalIODelayStat = getStatistic(aTotalIODelayStat);
            synchronized (aTotalIODelayStat) {
                aTotalIODelayStat.add(time);
            }
            aIOWaitTimeStat = getStatistic(aIOWaitTimeStat);
            synchronized (aIOWaitTimeStat) {
                aIOWaitTimeStat.add(time);
            }
        }
    }

    /**
     * Called when a thread trying to access the object controlled data (that is
     * read from or written to disk) is blocked while another thread actually
     * loads the data.
     *
     * @param time The time spent blocking (msec) by the thread.
     */
    public void updateIOBlocking(long time) {
        if (time > 1) {
            aTotalIODelayStat = getStatistic(aTotalIODelayStat);
            synchronized (aTotalIODelayStat) {
                aTotalIODelayStat.add(time);
            }
            aIOBlockingTimeStat = getStatistic(aIOBlockingTimeStat);
            synchronized (aIOBlockingTimeStat) {
                aIOBlockingTimeStat.add(time);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Adds the input IOMIObjectState to this one. Used to provide a cumulative
     * representative from many hosts that define an IOMIObjectState for the same
     * IOMIInterface object class (i.e. aObjectName == iorms.aObjectName).
     *
     * @param iorms The input IOMIObjectState to be added to this one.
     */
    public void add(IOMIObjectState iorms) {
        // add counts

        aCurrInstanceCount += iorms.aCurrInstanceCount;
        aCurrLoadedCount += iorms.aCurrLoadedCount;
        aCurrReferenceCount += iorms.aCurrReferenceCount;
        aTotalInstanceCount += iorms.aTotalInstanceCount;
        aTotalLoadedCount += iorms.aTotalLoadedCount;
        if (aMaxInstanceCount < iorms.aMaxInstanceCount)
            aMaxInstanceCount = iorms.aMaxInstanceCount;
        if (aMaxLoadedCount < iorms.aMaxLoadedCount)
            aMaxLoadedCount = iorms.aMaxLoadedCount;
        if (aMaxReferenceCount < iorms.aMaxReferenceCount)
            aMaxReferenceCount = iorms.aMaxReferenceCount;

        // add memory

        aCurrInstanceMemory += iorms.aCurrInstanceMemory;
        aTotalInstanceMemory += iorms.aTotalInstanceMemory;
        if (aMaxInstanceMemory < iorms.aMaxInstanceMemory)
            aMaxInstanceMemory = iorms.aMaxInstanceMemory;

        // add total io, read, write, rename, and delete size, time and rate stats.

        aTotalSizeStat = addMap(aTotalSizeStat, iorms.aTotalSizeStat);
        aReadSizeStat = addMap(aReadSizeStat, iorms.aReadSizeStat);
        aWriteSizeStat = addMap(aWriteSizeStat, iorms.aWriteSizeStat);
        aTotalTimeStat = addMap(aTotalTimeStat, iorms.aTotalTimeStat);
        aReadTimeStat = addMap(aReadTimeStat, iorms.aReadTimeStat);
        aWriteTimeStat = addMap(aWriteTimeStat, iorms.aWriteTimeStat);
        aRenameTimeStat = addMap(aRenameTimeStat, iorms.aRenameTimeStat);
        aDeleteTimeStat = addMap(aDeleteTimeStat, iorms.aDeleteTimeStat);
        aTotalRateStat = addMap(aTotalRateStat, iorms.aTotalRateStat);
        aReadRateStat = addMap(aReadRateStat, iorms.aReadRateStat);
        aWriteRateStat = addMap(aWriteRateStat, iorms.aWriteRateStat);

        // add any failures

        if (iorms.aIOFailureCount != null) {
            if (aIOFailureCount == null)
                aIOFailureCount = new ConcurrentHashMap<String, Integer>();
            for (Map.Entry<String, Integer> e : iorms.aIOFailureCount.entrySet()) {
                Integer cnt = aIOFailureCount.get(e.getKey());
                if (cnt == null) {
                    aIOFailureCount.put(e.getKey(), new Integer(0));
                }

                aIOFailureCount.put(e.getKey(), aIOFailureCount.get(e.getKey()) +
                        e.getValue());
            }
        }

        // add io wait stats

        aIOWait0Count += iorms.aIOWait0Count;
        if (iorms.aIOWaitTimeStat != null) {
            if (aIOWaitTimeStat == null) aIOWaitTimeStat = new Statistic();
            aIOWaitTimeStat.add(iorms.aIOWaitTimeStat);
        }

        // add io blocking stats

        if (iorms.aIOBlockingTimeStat != null) {
            if (aIOBlockingTimeStat == null) aIOBlockingTimeStat = new Statistic();
            aIOBlockingTimeStat.add(iorms.aIOBlockingTimeStat);
        }

        // add total io delay stats

        if (iorms.aTotalIODelayStat != null) {
            if (aTotalIODelayStat == null) aTotalIODelayStat = new Statistic();
            aTotalIODelayStat.add(iorms.aTotalIODelayStat);
        }
    }

    /**
     * Clears all stat objects and resets counts and memory to 0.
     * Used to initialize a summary object before calling add(...).
     */
    public void clear() {
        aCurrInstanceCount = aMaxInstanceCount = aTotalInstanceCount = 0;
        aCurrLoadedCount = aMaxLoadedCount = aTotalLoadedCount = 0;
        aCurrReferenceCount = aMaxReferenceCount = 0;
        aCurrInstanceMemory = aMaxInstanceMemory = aTotalInstanceMemory = 0;

        aTotalSizeStat = aTotalTimeStat = aTotalRateStat = null;
        aReadSizeStat = aReadTimeStat = aReadRateStat = null;
        aWriteSizeStat = aWriteTimeStat = aWriteRateStat = null;
        aRenameTimeStat = aDeleteTimeStat = null;
        aIOFailureCount = null;
        aIOWait0Count = 0;

        aTotalIODelayStat = aIOWaitTimeStat = aIOBlockingTimeStat = null;
    }

    /**
     * Private function to return the input statistic, if it is defined, or a new
     * one otherwise.
     *
     * @param inStat The input statistic that will be returned if it is not null.
     * @return The input statistic if it is defined or a new one otherwise.
     */
    private synchronized Statistic getStatistic(Statistic inStat) {
        return (inStat == null) ? new Statistic() : inStat;
    }

    /**
     * Adds the map of statistic addMap to the input thisMap if addMap is defined.
     * This map, modified or not, is returned.
     *
     * @param thisMap The map into which addMap will be added before returning it
     *                to the caller.
     * @param addMap  The map to be added into thisMap if it is defined.
     * @return The map of statistic addMap to the input thisMap if addMap is
     * defined.
     */
    private ConcurrentHashMap<String, Statistic>
    addMap(ConcurrentHashMap<String, Statistic> thisMap,
           ConcurrentHashMap<String, Statistic> addMap) {
        // if the add map is null simply return thisMap

        if (addMap != null) {
            // addMap is not null ... if thisMap is null make a new one

            if (thisMap == null)
                thisMap = new ConcurrentHashMap<String, Statistic>();

            // loop through all entries in addMap

            for (Map.Entry<String, Statistic> e : addMap.entrySet()) {
                // see if current entry from addMap is in thisMap ... if not add it

                Statistic stat = thisMap.get(e.getKey());
                if (stat == null) {
                    stat = new Statistic();
                    thisMap.put(e.getKey(), stat);
                }

                // add the addMap statistic to the thisMap statistic

                stat.add(e.getValue());
            }
        }

        // done ... return thisMap

        return thisMap;
    }

    /**
     * Returns the input statistics map, if it is defined, or a new one otherwise.
     *
     * @param statMap The input statistics map that will be returned if it is
     *                defined.
     * @return The input statistics map, if it is defined, or a new one otherwise.
     */
    private synchronized ConcurrentHashMap<String, Statistic>
    getTagStatisticMap(ConcurrentHashMap<String, Statistic> statMap) {
        return (statMap == null) ? new ConcurrentHashMap<String, Statistic>() : statMap;
    }

    /**
     * Returns the input failure map, if it is defined, or a new one otherwise.
     *
     * @param failMap The input failure map that will be returned if it is
     *                defined
     * @return The input failure map, if it is defined, or a new one otherwise.
     */
    private synchronized ConcurrentHashMap<String, Integer>
    getTagFailureMap(ConcurrentHashMap<String, Integer> failMap) {
        return (failMap == null) ? new ConcurrentHashMap<String, Integer>() : failMap;
    }

    /**
     * Adds the input long value to the statistic stored in the input statistics
     * map (statMap) at key srvrTag. If it is not defined a new statistic is
     * created for the input tag and then the value is added to the statistic.
     *
     * @param srvrTag The server tag key to be discovered in the input statistics
     *                map
     * @param statMap The input statistics map for which value will be added to
     *                the statistic at key srvrTag.
     * @param value   The long value to be added to the statistic at key srvrTag
     *                in the input map statMap.
     */
    private void setTagStatistic(String srvrTag,
                                 ConcurrentHashMap<String, Statistic> statMap,
                                 long value) {
        Statistic stat;

        // get the statistic associated with the input key srvrTag and see if it
        // exists

        synchronized (statMap) {
            stat = statMap.get(srvrTag);
            if (stat == null) {
                // does not exist ... create a new statistic and add it to the map
                // associated with the input key srvrTag.

                stat = new Statistic();
                statMap.put(srvrTag, stat);
            }
        }

        // add the input value to the statistic and return

        synchronized (stat) {
            stat.add(value);
        }
    }

    /**
     * Adds the input double value to the statistic stored in the input statistics
     * map (statMap) at key srvrTag. If it is not defined a new statistic is
     * created for the input tag and then the value is added to the statistic.
     *
     * @param srvrTag The server tag key to be discovered in the input statistics
     *                map
     * @param statMap The input statistics map for which value will be added to
     *                the statistic at key srvrTag.
     * @param value   The double value to be added to the statistic at key srvrTag
     *                in the input map statMap.
     */
    private void setTagStatistic(String srvrTag,
                                 ConcurrentHashMap<String, Statistic> statMap,
                                 double value) {
        Statistic stat;

        // get the statistic associated with the input key srvrTag and see if it
        // exists

        synchronized (statMap) {
            stat = statMap.get(srvrTag);
            if (stat == null) {
                // does not exist ... create a new statistic and add it to the map
                // associated with the input key srvrTag.

                stat = new Statistic();
                statMap.put(srvrTag, stat);
            }
        }

        // add the input value to the statistic and return

        synchronized (stat) {
            stat.add(value);
        }
    }

    /**
     * Adds the input failure count to the failures stored in the map failureMap
     * at key srvrTag. If it is not defined a new Integer is created for the
     * input tag and the value is added to it.
     *
     * @param srvrTag    The server tag key to be discovered in the input failure
     *                   map
     * @param failureMap The input failure map for which the input failure count
     *                   will be added for the key srvrTag.
     * @param failures   The failure count to be added at key srvrTag in the
     *                   input map statMap.
     */
    private void addFailures(String srvrTag,
                             ConcurrentHashMap<String, Integer> failureMap,
                             int failures) {
        // get the failure count associated with the input key srvrTag and see if it
        // exists

        synchronized (failureMap) {
            Integer failCount = failureMap.get(srvrTag);
            if (failCount == null) {
                // does not exist ... create a new Integer and add it to the map
                // associated with the input key srvrTag.

                failCount = new Integer(0);
                failureMap.put(srvrTag, failCount);
            }

            // add the input value to the Integer and return

            failureMap.put(srvrTag, failureMap.get(srvrTag) + failures);
        }
    }
}
