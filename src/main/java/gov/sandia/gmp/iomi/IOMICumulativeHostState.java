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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.hostconfiguration.HostConfiguration;
import gov.sandia.gmp.util.hostconfiguration.HostConfiguration.HostConfigurationCategory;
import gov.sandia.gmp.util.statistics.Statistic;

/**
 * Generally run on client applications to accumulate all Fabric IOMIHostState
 * objects returned from the run tasks. This object acts a stand-alone
 * repository of the run state performance for all participating host servers
 * during the execution lifetime of a specific client application.
 * <p>
 * This object can be written to disk and re-read at a later time for further
 * analysis. The toString method provides a return string that chronicles all
 * hosts, specific groups to which they belong, and a Total cumulative summary.
 * The entire object covers all IO and run-time processing amounts and times.
 *
 * @author Jrhipp
 */
@SuppressWarnings("serial")
public class IOMICumulativeHostState implements Serializable {
    /**
     * The map of all hosts saved by their host name. Multiple instances are
     * saved temporally by their start time (this can occur if the host starts
     * and stops and restarts one or more times during the applications
     * execution).
     */
    private HashMap<String, TreeMap<Long, IOMIHostState>> aHostMap = null;

    /**
     * The map of all cumulative groups. A group is defined as any supported
     * category in the HostConfiguration class. The performance of all hosts in
     * a requested category can by grouped together here to form a cumulative
     * summary of the gorups peformance.
     */
    private HashMap<String, IOMIHostState> aHostGrpMap = null;

    /**
     * The total cummulative summary host state of all participating individual
     * host states.
     */
    private IOMIHostState aAllHosts = null;

    /**
     * A map of all participating groups and their associated host set. The key
     * in this map is also a key in the host group map (aHostGrpMap).
     */
    private HashMap<String, HashSet<String>> aGrpSet = null;

    /**
     * Set to true if the cumulative host states needs updating (new individual
     * host states have been added since the last update).
     */
    private boolean aUpdate = false;

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public IOMICumulativeHostState() {
        aHostMap = new HashMap<String, TreeMap<Long, IOMIHostState>>();
        aHostGrpMap = new HashMap<String, IOMIHostState>();
        aAllHosts = new IOMIHostState("ALL");
        aGrpSet = new HashMap<String, HashSet<String>>();
    }

    /**
     * Standard constructor. Creates the cumulative host state from the definition
     * in the provided input file.
     */
    public IOMICumulativeHostState(String filePath) throws IOException {
        aHostMap = new HashMap<String, TreeMap<Long, IOMIHostState>>();
        aHostGrpMap = new HashMap<String, IOMIHostState>();
        aAllHosts = new IOMIHostState("ALL");
        aGrpSet = new HashMap<String, HashSet<String>>();
        read(filePath);
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Reads this cummulative host state from the provided input path/file name.
     *
     * @param pathFileName The path/file name containing the cumulative host
     *                     state to be read.
     * @throws IOException
     */
    public void read(String pathFileName) throws IOException {
        System.out.println("Reading File: " + pathFileName);
        System.out.println("");
        FileInputBuffer fib = new FileInputBuffer(pathFileName);
        read(fib);
        fib.close();
    }

    /**
     * Reads this cumulative host state from the provided input file input buffer.
     *
     * @param fib The file input buffer from which the cumulative host state will
     *            be read.
     * @throws IOException
     */
    public void read(FileInputBuffer fib) throws IOException {
        // clear the host map and group map

        aHostMap.clear();
        aHostGrpMap.clear();

        // read the title and date and output

        String title = fib.readString();
        String dte = fib.readString();
        System.out.println("  Title:      " + title);
        System.out.println("  Write Date: " + dte);
        System.out.println("");

        // read the cumulative host state

        aAllHosts.read(fib);

        // read the number of cumulative group host states and read each

        int n = fib.readInt();
        for (int i = 0; i < n; ++i) {
            String grpName = fib.readString();
            IOMIHostState newState = new IOMIHostState(grpName);
            newState.read(fib);
            aHostGrpMap.put(grpName, newState);
        }

        // read the number of individual host states that comprise this cumulative
        // host state and read each

        n = fib.readInt();
        for (int i = 0; i < n; ++i) {
            String hostName = fib.readString();
            int m = fib.readInt();
            TreeMap<Long, IOMIHostState> hostEntriesMap;
            hostEntriesMap = new TreeMap<Long, IOMIHostState>();
            for (int j = 0; j < m; ++j) {
                long entryStrtTime = fib.readLong();
                IOMIHostState newState = new IOMIHostState(hostName);
                newState.read(fib);
                hostEntriesMap.put(entryStrtTime, newState);
            }
            aHostMap.put(hostName, hostEntriesMap);
        }
    }

    /**
     * Write this host state to the provided path/file.
     *
     * @param title        A descriptive title.
     * @param pathFileName The path/file into which this cumulative host state
     *                     shall be written.
     * @throws IOException
     */
    public void write(String title, String pathFileName) throws IOException {
        updateCummulativeHostState(true);
        FileOutputBuffer fob = new FileOutputBuffer(pathFileName);
        write(title, fob);
        fob.close();
    }

    /**
     * Writes this cumulative host state into the provided file output buffer.
     *
     * @param title A descriptive title.
     * @param fob   The file output buffer into which this cumulative host state
     *              shall be written.
     * @throws IOException
     */
    public void write(String title, FileOutputBuffer fob) throws IOException {
        // write title and date

        fob.writeString(title);
        fob.writeString(Globals.getTimeStamp());

        // write cumulative host state

        aAllHosts.write(fob);

        // write cumulative group state count and each cumulative group state

        fob.writeInt(aHostGrpMap.size());
        for (String grpName : aHostGrpMap.keySet()) {
            fob.writeString(grpName);
            aHostGrpMap.get(grpName).write(fob);
        }

        // write number of contributing host states and then write each indivdual
        // entry to disk

        fob.writeInt(aHostMap.size());
        for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                aHostMap.entrySet()) {
            fob.writeString(e.getKey());
            TreeMap<Long, IOMIHostState> hostEntriesMap = e.getValue();
            fob.writeInt(hostEntriesMap.size());
            for (Map.Entry<Long, IOMIHostState> eEntry :
                    hostEntriesMap.entrySet()) {
                fob.writeLong(eEntry.getKey());
                eEntry.getValue().write(fob);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns The IOMIHostState given the input server name or null if it does
     * not exist.
     *
     * @param hostName The host name of the IOMIHostState to be returned.
     * @return The IOMIHostState given the input server name or null if it does
     * not exist.
     */
    public IOMIHostState getHostState(String hostName) {
        if (hostName.equals("ALL"))
            return aAllHosts;
        else if (aHostGrpMap.containsKey(hostName))
            return aHostGrpMap.get(hostName);
        else {
            TreeMap<Long, IOMIHostState> hostStateEntry;
            hostStateEntry = aHostMap.get(hostName);
            if (hostStateEntry != null) {
                return getPrimaryHostState(hostStateEntry);
            }
        }
        return null;
    }

    /**
     * Returns the only host entry or the cumulative (tag = -1) for the case
     * where a host server has more than one entry.
     *
     * @param hostEntries The map of entries for some host state.
     * @return The only host entry or the cumulative (tag = -1) for the case
     * where a host server has more than one entry.
     */
    public IOMIHostState getPrimaryHostState(TreeMap<Long, IOMIHostState> hostEntries) {
        if ((hostEntries == null) || (hostEntries.size() == 0))
            return null;
        else if (hostEntries.size() > 1)
            return hostEntries.get(-1L);
        else
            return hostEntries.lastEntry().getValue();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a map of HOSTNAME associated with a double rank fractional
     * percentage of some host state statistic. If hostName is empty then all
     * hosts are ranked.
     * <p>
     * The value defined for param and totlParam is the host state "Task" or a
     * client specified PROCESS_NAME.
     * <p>
     * The type of statistic that the ranking can be compiled for include:
     * "MINIMUM", "MAXIMUM", "MEAN", "STANDARD_DEVIATION", "RMS", or "SUM".
     *
     * @param param     The host state "Task" or client specified PROCESS NAME.
     * @param totlParam The total host state "Task" or client specified PROCESS
     *                  NAME.
     * @param st        The statistic type to be ranked. Can be "MINIMUM",
     *                  "MAXIMUM", "MEAN", "STANDARD_DEVIATION", "RMS", or "SUM".
     * @return The ranking map,
     */
    public HashMap<String, Double> rankHostStatistic(String param,
                                                     String totlParam,
                                                     Statistic.StatType st) {
        double totl, valu;
        Statistic stat, totlStat;

        // initialize and update cumulative host states if they need it

        HashMap<String, Double> rank = null;
        if (aUpdate) updateCummulativeHostState(true);

        // get the requested total statistic and get the requested
        // host state(s)

        String rankName = "";
        totlStat = aAllHosts.getHostStatistic(totlParam);
        totl = totlStat.getResult(st);
        for (IOMIHostState hostState : getHostSet("")) {
            // create the host name ranking description and get the requested
            // host state statistic and its value

            rankName = getRankName("", "", hostState.getHostName());
            stat = hostState.getHostStatistic(param);
            valu = stat.getResult(st);

            // if the rank map is still null create a new one

            if (rank == null) rank = new HashMap<String, Double>();

            // add the rank (value) as a % of the total to the rank map

            rank.put(rankName, 100.0 * valu / totl);
        }

        // done ... return ranking map

        return rank;
    }

    /**
     * Returns a map of HOSTNAME associated with a double rank fractional
     * percentage of some host state parameter. If hostName is empty then all
     * hosts are ranked.
     * <p>
     * The value defined for param can be any of the following strings:
     * <p>
     * "GC Manual Count",
     * "GC Manual Time",
     * "GC Total Count",
     * "GC Total Time", and
     * "Process Overhead".
     *
     * @param param The host state parameter to be ranked which can be any
     *              of the following strings:
     * @return The ranking map.
     */
    public HashMap<String, Double> rankHostParameter(String param) {
        double totl, valu;

        // initialize and update cumulative host states if they need it

        String rankName = "";
        HashMap<String, Double> rank = null;
        if (aUpdate) updateCummulativeHostState(true);

        // get the requested total parameter and get the requested
        // host state(s)

        totl = aAllHosts.getHostParameter(param);
        for (IOMIHostState hostState : getHostSet("")) {
            // create the host name ranking description and get the requested
            // host state parameter

            rankName = getRankName("", "", hostState.getHostName());
            valu = hostState.getHostParameter(param);

            // if the rank map is still null create a new one

            if (rank == null) rank = new HashMap<String, Double>();

            // add the rank (value) as a % of the total to the rank map

            rank.put(rankName, 100.0 * valu / totl);
        }

        // done ... return ranking map

        return rank;
    }

    /**
     * Returns a map of HOSTNAME.OBJECTNAME.SERVERNAME associated with a double
     * rank fractional percentage of some IO file statistic given the input host,
     * object and file server names. If hostName is empty then all hosts are
     * ranked. If objectName is empty then all object types are ranked. If
     * srvrName is empty then all file server names are ranked.
     * <p>
     * The statistic type is defined by the input parameters param, totlParam,
     * and paramUnit. Valid entries for param and totlParam include: "Total",
     * "Read", "Write", "Rename", "Delete". Valid values for paramUnit include:
     * "Size", "Time", or "Rate". If either param or totlParam are set to either
     * "Rename" or "Delete" then the only valid unit recognized is "Time".
     * <p>
     * The value for param is the file IO parameter name to be ranked. For
     * example, if param = "Read" and totlParam = "Total" then rankings are
     * constructed for read time relative to total file IO time. If totlParam =
     * "Read", however, then the rankings are relative to total file read time.
     * <p>
     * The type of statistic that the ranking can be compiled for include:
     * "MINIMUM", "MAXIMUM", "MEAN", "STANDARD_DEVIATION", "RMS", or "SUM".
     *
     * @param hostName   The input host name to be ranked (all if empty).
     * @param objectName The input object name to be ranked (all if empty).
     * @param param      The object state IO delay statistic to be ranked. Valid
     *                   values include:
     *                   "Total IO Delay",
     *                   "IO Wait Time", and
     *                   "IO Blocking Time"
     * @param totlParam  The object state IO delay statistic against which the
     *                   statistic defined by param is normalized.
     * @param st         The statistic type to be ranked. Can be "MINIMUM",
     *                   "MAXIMUM", "MEAN", "STANDARD_DEVIATION", "RMS", or "SUM".
     * @return The ranking map,
     */
    public HashMap<String, Double> rankObjectDelayStatistic(String hostName,
                                                            String objectName,
                                                            String param,
                                                            String totlParam,
                                                            Statistic.StatType st) {
        double totl, valu;
        IOMIObjectState totlObjectState;
        Statistic stat, totlStat;

        //**** should throw error if hostName and objectName are both assigned

        // initialize and update cumulative host states if they need it

        String rankName = "", rankNameC = "";
        HashMap<String, Double> rank = null;
        if (aUpdate) updateCummulativeHostState(true);

        // get all object states from the total ("ALL") host state and make sure
        // it is defined

        totlObjectState = aAllHosts.getObjectState("ALL");
        if (totlObjectState != null) {
            // get the total requested delay statistic and make sure it is defined

            totlStat = totlObjectState.getObjectDelayStatistic(totlParam);
            if (totlStat != null) {
                // get the requested cumulative statistic and get the requested
                // host state(s)

                totl = totlStat.getResult(st);
                for (IOMIHostState hostState : getHostSet(hostName)) {
                    // create the host name ranking description and get the requested
                    // object state(s)

                    rankName = getRankName("", hostName, hostState.getHostName());
                    for (IOMIObjectState objectState :
                            hostState.getObjectSet(objectName)) {
                        // append the object type ranking the rank name and get the
                        // requested delay statistic and get is result

                        rankNameC = getRankName(rankName, objectName,
                                objectState.getIOMIObjectName());
                        stat = objectState.getObjectDelayStatistic(param);
                        valu = stat.getResult(st);

                        // if the rank map is still null create a new one

                        if (rank == null) rank = new HashMap<String, Double>();

                        // add the rank (value) as a % of the total to the rank map

                        rank.put(rankNameC, 100.0 * valu / totl);
                    }
                }
            }
        }

        // done ... return ranking map

        return rank;
    }

    /**
     * Returns a map of HOSTNAME.OBJECTNAME associated with a double
     * rank fractional percentage of some IOMIObjectState parameter given the
     * input host and object names. If hostName is empty then all hosts are
     * ranked. If objectName is empty then all object types are ranked.
     * <p>
     * The input parameter name is defined as any one of the following:
     * <p>
     * "IO Zero Wait 0 Count",
     * "Current Instance Count",
     * "Maximum Instance Count",
     * "Total Instance Count",
     * "Current Instance Memory",
     * "Maximum Instance Memory", and
     * "Total Instance Memory".
     *
     * @param hostName   The input host name to be ranked (all if empty).
     * @param objectName The input object name to be ranked (all if empty).
     * @param param      The IOMIObjectState parameter name including:
     *                   "IO Zero Wait 0 Count",
     *                   "Current Instance Count",
     *                   "Maximum Instance Count",
     *                   "Total Instance Count",
     *                   "Current Instance Memory",
     *                   "Maximum Instance Memory", and
     *                   "Total Instance Memory".
     * @return The ranking map,
     */
    public HashMap<String, Double> rankObjectParameter(String hostName,
                                                       String objectName,
                                                       String param) {
        double totl, valu;
        IOMIObjectState totlObjectState;

        //**** should throw error if hostName and objectName are both assigned

        // initialize and update cumulative host states if they need it

        String rankName = "", rankNameC = "";
        HashMap<String, Double> rank = null;
        if (aUpdate) updateCummulativeHostState(true);

        // get all object states from the total ("ALL") host state and make sure
        // it is defined

        totlObjectState = aAllHosts.getObjectState("ALL");
        if (totlObjectState != null) {
            // get the requested total parameter and get the requested
            // host state(s)

            totl = totlObjectState.getObjectParameter(param);
            for (IOMIHostState hostState : getHostSet(hostName)) {
                // create the host name ranking description and get the requested
                // object state(s)

                rankName = getRankName("", hostName, hostState.getHostName());
                for (IOMIObjectState objectState :
                        hostState.getObjectSet(objectName)) {
                    // append the object name ranking and get the requested parameter

                    rankNameC = getRankName(rankName, objectName,
                            objectState.getIOMIObjectName());
                    valu = objectState.getObjectParameter(param);

                    // if the rank map is still null create a new one

                    if (rank == null) rank = new HashMap<String, Double>();

                    // add the rank (value) as a % of the total to the rank map

                    rank.put(rankNameC, 100.0 * valu / totl);
                }
            }
        }

        // done ... return ranking map

        return rank;
    }

    /**
     * Returns a map of HOSTNAME.OBJECTNAME.SERVERNAME associated with a double
     * rank fractional percentage of some IO file statistic given the input host,
     * object and file server names. If hostName is empty then all hosts are
     * ranked. If objectName is empty then all object types are ranked. If
     * srvrName is empty then all file server names are ranked.
     * <p>
     * The statistic type is defined by the input parameters param, totlParam,
     * and paramUnit. Valid entries for param and totlParam include: "Total",
     * "Read", "Write", "Rename", "Delete". Valid values for paramUnit include:
     * "Size", "Time", or "Rate". If either param or totlParam are set to either
     * "Rename" or "Delete" then the only valid unit recognized is "Time".
     * <p>
     * The value for param is the file IO parameter name to be ranked. For
     * example, if param = "Read" and totlParam = "Total" then rankings are
     * constructed for read time relative to total file IO time. If totlParam =
     * "Read", however, then the rankings are relative to total file read time.
     * <p>
     * The type of statistic that the ranking can be compiled for include:
     * "MINIMUM", "MAXIMUM", "MEAN", "STANDARD_DEVIATION", "RMS", or "SUM".
     *
     * @param hostName   The input host name to be ranked (all if empty).
     * @param objectName The input object name to be ranked (all if empty).
     * @param srvrName   The input file server name to be ranked (all if empty).
     * @param param      The file IO parameter name ("Total", "Read", "Write",
     *                   "Rename", or "Delete").
     * @param totlParam  The file IO parameter name ("Total", "Read", "Write",
     *                   "Rename", or "Delete") against which the parameter name
     *                   input (param) is compared in the rankings. IOW, if param
     *                   = "Read" and totlParam = "Total" then rankings are read
     *                   time relative to total file IO time. If totlParam =
     *                   "Read" then the rankings are relative to total file read
     *                   time.
     * @param paramUnit  The file IO unit type ("Size", "Time", or "Rate"). Only
     *                   "Time" is allowed if either the param and totlParam are
     *                   set to "Rename" or "Delete".
     * @param st         The statistic type to be ranked. Can be "MINIMUM",
     *                   "MAXIMUM", "MEAN", "STANDARD_DEVIATION", "RMS", or "SUM".
     * @return The ranking map,
     */
    public HashMap<String, Double> rankFileIOStatistic(String hostName,
                                                       String objectName,
                                                       String srvrName,
                                                       String param,
                                                       String totlParam,
                                                       String paramUnit,
                                                       Statistic.StatType st) {
        double totl, valu;
        IOMIObjectState totlObjectState;
        ConcurrentHashMap<String, Statistic> totlSrvrMap;
        Statistic srvrStat, totlStat;

        //**** should throw error if hostName, objectName, and srvrName are all
        //**** assigned

        // initialize and update cumulative host states if they need it

        String rankName = "", rankNameC = "", rankNameS = "";
        HashMap<String, Double> rank = null;
        if (aUpdate) updateCummulativeHostState(true);

        // get all object states from the total ("ALL") host state and make sure
        // it is defined

        totlObjectState = aAllHosts.getObjectState("ALL");
        if (totlObjectState != null) {
            // get the map of all servers associated with the requested statistic
            // make sure it is defined

            totlSrvrMap = totlObjectState.getServerStatistic(totlParam, paramUnit);
            if (totlSrvrMap != null) {
                // get the cumulative server statistic and make sure it is defined

                totlStat = totlSrvrMap.get("ALL");
                if (totlStat != null) {
                    // get the requested statistic from the total and get the requested
                    // host state(s)

                    totl = totlStat.getResult(st);
                    for (IOMIHostState hostState : getHostSet(hostName)) {
                        // create the host name ranking description and get the requested
                        // object state(s)

                        rankName = getRankName("", hostName, hostState.getHostName());
                        for (IOMIObjectState objectState :
                                hostState.getObjectSet(objectName)) {
                            // append the object name ranking and get the requested file
                            // server(s)

                            rankNameC = getRankName(rankName, objectName,
                                    objectState.getIOMIObjectName());
                            for (String srvName :
                                    objectState.getServerIOStatisticSet(srvrName, param, paramUnit)) {
                                // get the requested server statistic and result ... append the
                                // server rank name

                                srvrStat = objectState.getServerStatistic(param, paramUnit).get(srvName);
                                valu = srvrStat.getResult(st);
                                rankNameS = getRankName(rankNameC, srvrName, srvName);

                                // if the rank map is still null create a new one

                                if (rank == null) rank = new HashMap<String, Double>();

                                // add the rank (value) as a % of the total to the rank map

                                rank.put(rankNameS, 100.0 * valu / totl);
                            }
                        }
                    }
                }
            }
        }

        // done ... return ranking map

        return rank;
    }

    /**
     * Returns a set with one entry (the host with the input host name). If the
     * input host name is empty ("") then all hosts (except the total cumulative
     * host) are returned.
     *
     * @param hostName The input host name for which a single entry set will be
     *                 returned unless equal to "".
     * @return The host set.
     */
    public HashSet<IOMIHostState> getHostSet(String hostName) {
        HashSet<IOMIHostState> hostSet;
        hostSet = new HashSet<IOMIHostState>();

        // add all host states except "ALL" if hostName = ""
        // otherwise add only hostName

        if (hostName == "") {
            for (String grpName : aHostGrpMap.keySet())
                hostSet.add(getHostState(grpName));
            for (String hstName : aHostMap.keySet())
                hostSet.add(getHostState(hstName));
        } else
            hostSet.add(getHostState(hostName));

        // return host set

        return hostSet;
    }

    /**
     * Returns a map of HOSTNAME.OBJECTNAME.SERVERNAME associated with a double
     * rank fractional percentage of IO failure rates given the input host, object
     * and file server names. If hostName is empty then all hosts are ranked. If
     * objectName is empty then all object types are ranked. If srvrName is empty
     * then all file server names are ranked.
     *
     * @param hostName   The input host name to be ranked (all if empty).
     * @param objectName The input object name to be ranked (all if empty).
     * @param srvrName   The input file server name to be ranked (all if empty).
     * @return The ranking map,
     */
    public HashMap<String, Double> rankFileIOFailures(String hostName,
                                                      String objectName,
                                                      String srvrName) {
        double totl, valu;
        IOMIObjectState totlObjectState;

        //**** should throw error if hostName, objectName, and srvrName are all
        //**** assigned

        // initialize and update cumulative host states if they need it

        String rankName = "", rankNameC = "", rankNameS = "";
        HashMap<String, Double> rank = null;
        if (aUpdate) updateCummulativeHostState(true);

        // get the total host object state and make sure it is defined

        totlObjectState = aAllHosts.getObjectState("ALL");
        if (totlObjectState != null) {
            // get the total IO failures from all servers and see if there were any

            totl = totlObjectState.getCummulativeIOFailureCount();
            if (totl > 0) {
                // had failures ... get the host

                for (IOMIHostState hostState : getHostSet(hostName)) {
                    // build its rank name and loop over all object states

                    rankName = getRankName("", hostName, hostState.getHostName());
                    for (IOMIObjectState objectState :
                            hostState.getObjectSet(objectName)) {
                        // build object state rank name and loop over all IO file servers

                        rankNameC = getRankName(rankName, objectName,
                                objectState.getIOMIObjectName());
                        for (String srvName : objectState.getServerIOFailureSet(srvrName)) {
                            // get object states IO file failures and create the final rank
                            // name for this file server

                            valu = objectState.getIOFailureCountMap().get(srvName);
                            rankNameS = getRankName(rankNameC, srvrName, srvName);

                            // if the rank map is still null create a new one

                            if (rank == null) rank = new HashMap<String, Double>();

                            // add the rank (value) as a % of the total to the rank map

                            rank.put(rankNameS, 100.0 * valu / totl);
                        }
                    }
                }
            }
        }

        // done ... return rank map

        return rank;
    }

    /**
     * Returns the input rank name (rankName) unless selectName is not empty. In
     * that case currName is returned, if rankName is empty, or the name
     * rankName.currName is returned otherwise.
     *
     * @param rankName   The input rank name
     * @param selectName The input selection name. Not used in the result except
     *                   if not empty a new rankName is returned.
     * @param currName   The new rankName or rankName extension if selectName is
     *                   defined.
     * @return The new rank name.
     */
    public String getRankName(String rankName, String selectName, String currName) {
        if (selectName.equals("")) {
            if (rankName.equals(""))
                rankName = currName;
            else
                rankName += "." + currName;
        }

        return rankName;
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Parses the cumulative host map (aHostMap) and adds all group names for the
     * input category (hcc) to a group set and then all hosts to their respective
     * cumulative group set.
     *
     * @param hc  The host configuration file.
     * @param hcc The host category for which a group set will be added.
     */
    public void addGroupSets(HostConfiguration hc, HostConfigurationCategory hcc) {
        // find the number of entries of each group name from the set of input
        // category for each host in aHostMap

        HashMap<String, Integer> entrySet = new HashMap<String, Integer>();
        for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                aHostMap.entrySet()) {
            // get this next hosts group name for the requested category and see if
            // it is defined (not "")

            String grpName = hc.getParameter(e.getKey(), hcc);
            if (!grpName.equals("")) {
                // get the group name from the entry set ... if it is not there add
                // it ... increment the host count for that group name

                Integer cnt = entrySet.get(grpName);
                if (cnt == null) cnt = new Integer(0);
                entrySet.put(grpName, cnt + 1);
            }
        }

        // only add groups if more than one was found

        if (entrySet.size() > 1) {
            // again loop over all entries in host map

            for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                    aHostMap.entrySet()) {
                // for the current host entry get the group name and see if it is
                // defined in the host group name map

                String grpName = hc.getParameter(e.getKey(), hcc);
                IOMIHostState grpState = aHostGrpMap.get(grpName);
                if (grpState == null) {
                    // not defined ... add it

                    grpState = new IOMIHostState(grpName, aAllHosts.getStartTime());
                    aHostGrpMap.put(grpName, grpState);
                }

                // add the current host to the groups host set

                grpState.addHostToSet(e.getKey());
            }
        }
    }

    /**
     * Adds the input group host set (names) to the input group name in the
     * group name map.
     *
     * @param grpName The group name into which the hosts in the input host set
     *                will be added.
     * @param grpSet  The set of hosts to be add into the input group (name).
     */
    public void addGroupSet(String grpName, HashSet<String> grpSet) {
        // put the group name with the host set into the group set map and see if
        // the group name is defined in the host group map ... if not create it ...
        // otherwise, clear it

        aGrpSet.put(grpName, grpSet);
        IOMIHostState grpState = aHostGrpMap.get(grpName);
        if (grpState == null) {
            // not defined ... add it

            grpState = new IOMIHostState(grpName);
            aHostGrpMap.put(grpName, grpState);
        } else
            grpState.clearHostSet();

        // lop over all entries in the host map and see if the input group set
        // contains any of them ... if it does add them to the groups cumulative
        // host state

        for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                aHostMap.entrySet()) {
            if (grpSet.contains(e.getKey()))
                grpState.addHostToSet(e.getKey());
        }
    }

    /**
     * Adds the host state entry into the internal host state map (aHostMap).
     *
     * @param newHostState The host state to be added.
     */
    public void addHostState(IOMIHostState newHostState) {
        TreeMap<Long, IOMIHostState> hostEntries;
        IOMIHostState hostState, lastState;

        // get host name from input state and see if the host map already contains
        // and entry ... if necessary the input state will be added to the host
        // map, the "ALL" host state host set, and any group state host sets if
        // the input host is a member of their group set.

        aUpdate = true;
        String hostName = newHostState.getHostName();
        hostEntries = aHostMap.get(hostName);
        if (hostEntries == null) {
            // this is a new host name ... create a tree map to hold possible multiple
            // entries and add it to the host map

            hostEntries = new TreeMap<Long, IOMIHostState>();
            aHostMap.put(hostName, hostEntries);

            // add the host name to the "ALL" hosts state and check all groups to see
            // if the host should be added to any of those

            aAllHosts.addHostToSet(hostName);
            for (Map.Entry<String, HashSet<String>> e : aGrpSet.entrySet()) {
                // see if the group set contains the input host name

                if (e.getValue().contains(hostName)) {
                    // this group contains the input host ... see if the group has been
                    // added to the group map

                    IOMIHostState grpState = aHostGrpMap.get(e.getKey());
                    if (grpState == null) {
                        // not yet added ... create the new group state and add it to the
                        // group map

                        grpState = new IOMIHostState(e.getKey());
                        aHostGrpMap.put(e.getKey(), grpState);
                    }

                    // add the new host to the group state host set

                    grpState.addHostToSet(hostName);
                }
            }
        }

        // See if the input host is already added to the entry set for the input
        // state

        hostState = hostEntries.get(newHostState.getStartTime());
        if (hostState == null) {
            // not added ... this is a new start time or the first entry ... see if
            // this is a new start time

            if (hostEntries.size() > 0) {
                // older existing states are defined ... zero the current instance and
                // memory of the last entry

                lastState = hostEntries.lastEntry().getValue();
                lastState.zeroCurrentInstanceMemory();
            }

            // add the new entry

            hostEntries.put(newHostState.getStartTime(), newHostState);
        } else {
            // current host exists ... see if last update is after the current entries
            // last update ... if so replace the current entry with the new one

            if (hostState.getLastUpdateTime() < newHostState.getLastUpdateTime())
                hostEntries.put(newHostState.getStartTime(), newHostState);
        }

        // update the hostEntries total (-1) if multiple entries exist for this one
        // host

        updateMultipleHostEntries(hostName, hostEntries);
    }

    /**
     * Called to see if multiple IOMIHostState entries exist for the same host.
     * If they do then the host server was restarted for some reason producing a
     * second (or multiple) new host state with a different start time than the
     * original. If two or more exist a cumulative entry (saved with a time tag
     * of -1) is created or retreived, cleared, and has all other earlier
     * entries added into it.
     *
     * @param hostName    The host for which multiple entries (if they exist) will
     *                    be updated.
     * @param hostEntries The map of all time stamped versions of the input hosts
     *                    host state.
     */
    private void updateMultipleHostEntries(String hostName,
                                           TreeMap<Long, IOMIHostState> hostEntries) {
        IOMIHostState hostState;

        // see if multiple entries exist

        if (hostEntries.size() > 1) {
            // get the cummulative entry or create it if it does not exist

            hostState = hostEntries.get(-1L);
            if (hostState == null) {
                hostState = new IOMIHostState(hostName);
                hostEntries.put(-1L, hostState);
            }

            // clear the cumulative entry and add all other earlier entries into the
            // cumulative entry

            hostState.clear();
            for (Map.Entry<Long, IOMIHostState> e :
                    hostEntries.entrySet()) {
                if (e.getKey() != -1L) hostState.add(e.getValue());
            }
        }
    }

    /**
     * Updates the cumulative host state (aAllHosts) from the the map of all
     * represented hosts (aHostMap). If the input update group state flag is true
     * then group states are also updated.
     *
     * @param updateGroupStates If true group states are also updated.
     */
    public void updateCummulativeHostState(boolean updateGroupStates) {
        // clear the cumulative host state and loop over all entries in the host
        // map

        aAllHosts.clear();
        for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                aHostMap.entrySet()) {
            // for this host add all entries ... past and present

            for (Map.Entry<Long, IOMIHostState> eEntries :
                    e.getValue().entrySet()) {
                aAllHosts.add(eEntries.getValue());
            }
        }

        // done set the cumulative host update time

        aAllHosts.setUpdateTime();

        // if the update group state flag is true do it

        if (updateGroupStates) {
            for (String grpName : aHostGrpMap.keySet())
                updateGroupHostState(grpName);
        }

        // reset the update flag and exit

        aUpdate = false;
    }

    /**
     * Update the input group.
     *
     * @param grpName The group host state to be updated.
     */
    private void updateGroupHostState(String grpName) {
        // get the group and make sure it exists

        IOMIHostState grpState = aHostGrpMap.get(grpName);
        if (grpState != null) {
            // clear it and loop over all host entries in the map

            grpState.clear();
            for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                    aHostMap.entrySet()) {
                // get the first entry ... if multiple entries this will be the sum
                // total (tag -1), otherwise their is only one entry which also works

                if (grpState.containsHostInSet(e.getKey())) {
                    grpState.add(e.getValue().firstEntry().getValue());
                }
            }

            // set update time and exit

            grpState.setUpdateTime();
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Standard toString method.
     */
    @Override
    public String toString() {
        return toString("");
    }

    /**
     * The toString method that output the cumulative host state (aAllHosts),
     * all cumulative group host states (aHostGrpMap), and all individual host
     * states (aHostMap) into a printable string.
     *
     * @param hdr An input header that is pre-pended in front of each line in the
     *            returned string.
     * @return A string representing this object.
     */
    public String toString(String hdr) {
        String s = "";

        // make sure at least one host was defined

        if (aHostMap.size() > 1) {
            // add cumulative host state and all cumulative group host states

            s = aAllHosts.toString(hdr);
            for (String grpName : aHostGrpMap.keySet())
                s += aHostGrpMap.get(grpName).toString(hdr);
        }

        // add each individual host state to the output string

        for (Map.Entry<String, TreeMap<Long, IOMIHostState>> e :
                aHostMap.entrySet()) {
            TreeMap<Long, IOMIHostState> hostEntriesMap = e.getValue();
            for (Map.Entry<Long, IOMIHostState> eEntry :
                    hostEntriesMap.entrySet()) {
                s += eEntry.getValue().toString(hdr);
            }
        }

        // return string and exit

        return s;
    }
}
