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
package gov.sandia.gmp.iomihistory;

import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Globals.TableAlignment;

/**
 * Defines all output table definitions that use a subset of data for output.
 * These tables include "Current", "Instance", "Reference", "Cache", "Load",
 * "IOQueue", "IOProcess", "ThreadDelay", and "VMMemory". An inner class is
 * defined for each of these table types that defines the operations to be
 * output, the table column headers, and the table title.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class IOMIHistoryTableUseData extends HistoryTable {
    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing current
     * history results. These include the current "instance", "reference",
     * "cache", "load", "IO queue", and "IO process" counts, and the cache hit
     * percentage.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableCurrent extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableCurrent(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "Current" table output.

            HistoryOperation[] ops = {IOMIHistoryState.CREATE,
                    IOMIHistoryState.DESTROY,
                    IOMIHistoryState.ADDREFERENCE,
                    IOMIHistoryState.REMOVEREFERENCE,
                    IOMIHistoryState.ADDCACHE,
                    IOMIHistoryState.REMOVECACHEREREF,
                    IOMIHistoryState.REMOVECACHECNTLMT,
                    IOMIHistoryState.REMOVECACHEMEMLMT,
                    IOMIHistoryState.RELOAD,
                    IOMIHistoryState.LOADDATA,
                    IOMIHistoryState.QUEUEIO,
                    IOMIHistoryState.REMOVEQUEUEIO,
                    IOMIHistoryState.ALLOWIO,
                    IOMIHistoryState.COMPLETEIOREAD,
                    IOMIHistoryState.COMPLETEIOWRITE,
                    IOMIHistoryState.COMPLETEIORENAME,
                    IOMIHistoryState.COMPLETEIODELETE};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7           8            9          10       11           12          13
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Current", "Current", "Current", "Cache", "Current", "Current", "Current"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "Instance", "Reference", "Cache", "Hit", "Data Load", "IO Queue", "IO Process"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count", "%", "Count", "Count", "Count"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Current Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.CREATE)] -
                    counts[useMap.get(IOMIHistoryState.DESTROY)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.ADDREFERENCE)] -
                    counts[useMap.get(IOMIHistoryState.REMOVEREFERENCE)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.ADDCACHE)] -
                    counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)] -
                    counts[useMap.get(IOMIHistoryState.REMOVECACHECNTLMT)] -
                    counts[useMap.get(IOMIHistoryState.REMOVECACHEMEMLMT)]);
            int car = counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)] +
                    counts[useMap.get(IOMIHistoryState.RELOAD)];
            if (car == 0)
                dataRow[10] = "-----";
            else
                dataRow[10] = String.format("%7.3f",
                        (double) 100.0 * counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)] /
                                car);
            dataRow[11] = "" + (counts[useMap.get(IOMIHistoryState.LOADDATA)] -
                    counts[useMap.get(IOMIHistoryState.DESTROY)]);
            dataRow[12] = "" + (counts[useMap.get(IOMIHistoryState.QUEUEIO)] -
                    counts[useMap.get(IOMIHistoryState.REMOVEQUEUEIO)]);
            dataRow[13] = "" + (counts[useMap.get(IOMIHistoryState.ALLOWIO)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIOREAD)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIOWRITE)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIORENAME)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIODELETE)]);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing instance
     * history results. These include "create", "destroy", and current instance
     * counts.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableInstance extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableInstance(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "Instance" table output.

            HistoryOperation[] ops = {IOMIHistoryState.CREATE, IOMIHistoryState.DESTROY};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7           8            9
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Add", "Remove", "Current"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "Instance", "Instace", "Instance"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Instance Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.CREATE)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.DESTROY)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.CREATE)] -
                    counts[useMap.get(IOMIHistoryState.DESTROY)]);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing reference
     * history results. These include "addreference", "removereference", and the
     * current reference count.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableReference extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableReference(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "Reference" table output.

            HistoryOperation[] ops = {IOMIHistoryState.ADDREFERENCE,
                    IOMIHistoryState.REMOVEREFERENCE};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7           8            9
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Add", "Remove", "Current"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "Reference", "Reference", "Reference"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Reference Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.ADDREFERENCE)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.REMOVEREFERENCE)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.ADDREFERENCE)] -
                    counts[useMap.get(IOMIHistoryState.REMOVEREFERENCE)]);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing cache
     * history results. These include "addcache", "removecacereref",
     * "removecachecntlmt", "removecachememlmt", "reload", and the current
     * cache count and cache hit percentage.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableCache extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableCache(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "Cache" table output.

            HistoryOperation[] ops = {IOMIHistoryState.ADDCACHE,
                    IOMIHistoryState.REMOVECACHEREREF,
                    IOMIHistoryState.REMOVECACHECNTLMT,
                    IOMIHistoryState.REMOVECACHEMEMLMT,
                    IOMIHistoryState.RELOAD};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7        8               9              10              11         12        13
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Add", "Remove Cache", "Remove Cache", "Remove Cache", "Current", "", "Cache"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "Cache", "Re-Reference", "Entry Limit", "Memory Limit", "Cache", "Reload", "Hit"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count", "Count", "Count", "Count", "%"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Cache Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.ADDCACHE)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.REMOVECACHECNTLMT)]);
            dataRow[10] = "" + (counts[useMap.get(IOMIHistoryState.REMOVECACHEMEMLMT)]);
            dataRow[11] = "" + (counts[useMap.get(IOMIHistoryState.ADDCACHE)] -
                    counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)] -
                    counts[useMap.get(IOMIHistoryState.REMOVECACHECNTLMT)] -
                    counts[useMap.get(IOMIHistoryState.REMOVECACHEMEMLMT)]);
            dataRow[12] = "" + (counts[useMap.get(IOMIHistoryState.RELOAD)]);
            int car = counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)] +
                    counts[useMap.get(IOMIHistoryState.RELOAD)];
            if (car == 0)
                dataRow[13] = "-----";
            else
                dataRow[13] = String.format("%7.3f",
                        (double) 100.0 * counts[useMap.get(IOMIHistoryState.REMOVECACHEREREF)] /
                                car);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing
     * "Load (Data)" history results. These include "loaddata", "destroy", and
     * the current load count.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableLoad extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableLoad(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "Load" table output.

            HistoryOperation[] ops = {IOMIHistoryState.LOADDATA,
                    IOMIHistoryState.DESTROY};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7            8            9
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Add", "Remove", "Current"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "Load Data", "Load Data", "Loaded"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Load Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.LOADDATA)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.DESTROY)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.LOADDATA)] -
                    counts[useMap.get(IOMIHistoryState.DESTROY)]);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing
     * "Queue IO" history results. These include "queueio", "removequeueio", and
     * the current queue count.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableIOQueue extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableIOQueue(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "IO Queue" table output.

            HistoryOperation[] ops = {IOMIHistoryState.QUEUEIO,
                    IOMIHistoryState.REMOVEQUEUEIO};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7            8           9
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Add", "Remove", "Current"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "IO Queue", "IO Queue", "IO Queue"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History IO Queue Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.QUEUEIO)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.REMOVEQUEUEIO)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.QUEUEIO)] -
                    counts[useMap.get(IOMIHistoryState.REMOVEQUEUEIO)]);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing IO process
     * history results. These include "allowio", "completeread", "completewrite",
     * "completerename", "completedelete", and the current process count.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableIOProcess extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableIOProcess(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "IO Process" table output.

            HistoryOperation[] ops = {IOMIHistoryState.ALLOWIO,
                    IOMIHistoryState.COMPLETEIOREAD,
                    IOMIHistoryState.COMPLETEIOWRITE,
                    IOMIHistoryState.COMPLETEIORENAME,
                    IOMIHistoryState.COMPLETEIODELETE};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7             8                9                10                 11                 12
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Allow", "Complete Read", "Complete Write", "Complete Rename", "Complete Delete", "Current"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "IO Process", "IO Process", "IO Process", "IO Processt", "IO Process", "IO Process"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Count", "Count", "Count", "Count", "Count"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History IO Process Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.ALLOWIO)]);
            dataRow[8] = "" + (counts[useMap.get(IOMIHistoryState.COMPLETEIOREAD)]);
            dataRow[9] = "" + (counts[useMap.get(IOMIHistoryState.COMPLETEIOWRITE)]);
            dataRow[10] = "" + (counts[useMap.get(IOMIHistoryState.COMPLETEIORENAME)]);
            dataRow[11] = "" + (counts[useMap.get(IOMIHistoryState.COMPLETEIODELETE)]);
            dataRow[12] = "" + (counts[useMap.get(IOMIHistoryState.ALLOWIO)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIOREAD)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIOWRITE)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIORENAME)] -
                    counts[useMap.get(IOMIHistoryState.COMPLETEIODELETE)]);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing instance
     * history results. These include "threaddelay", the current thread delay
     * count, and the thread delay time.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableThreadDelay extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableThreadDelay(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "Thread Delay" table output.

            HistoryOperation[] ops = {IOMIHistoryState.THREADDELAY};
            aUseOperation = ops;

            //                     0        1             2        3       4             5               6             7        8
            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "", "",},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "Delay", "Delay"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Count", "Time"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Thread Delay Count Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // insert requested operations

            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.THREADDELAY)]);
            dataRow[8] = Globals.elapsedTimeString3(0, aExtrLong);
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing "Virtual
     * Machine Memory" history results. These include "vmmemorycheck", "gcstart",
     * "gcend", and the current memory check count and memory amount, and the
     * current manual gc count, and saved memory amount.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableVMMemory extends IOMIHistoryTableUseData {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableVMMemory(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // Set of operations to be used for "VMMemory" table output.

            HistoryOperation[] ops = {IOMIHistoryState.VMMEMORYCHECK,
                    IOMIHistoryState.GCSTART,
                    IOMIHistoryState.GCEND};
            aUseOperation = ops;

            //                     0        1            2       3             4             5         6         7         8
            String[][] colHdr = {{"", "", "", "", "Time Since", "Memory", "", "Manual", ""},
                    {"Entry", "", "", "Time Since", "Previous", "Check", "Memory", "GC", "Memory"},
                    {"Index", "Operation", "Time", "Start", "Entry", "Count", "Used", "Count", "Reclaimed"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Memory Usage Table";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested useData for this table type.
         *
         * @param dataRow The row of data entries.
         * @param counts  The column entry count arrays.
         * @param useMap  The map of HistoryOperation(s) associated with the index
         *                in the count array.
         */
        @Override
        protected void getUseData(String[] dataRow, int[] counts,
                                  HashMap<HistoryOperation, Integer> useMap) {
            // set the current time, the time since start, and the time since the
            // last entry.

            dataRow[2] = Globals.getTimeStamp(aCurrTime);
            dataRow[3] = Globals.elapsedTimeString3(aStrtTime, aCurrTime);
            dataRow[4] = Globals.elapsedTimeString3(aLastTime, aCurrTime);

            // insert requested operations

            dataRow[5] = "" + (counts[useMap.get(IOMIHistoryState.VMMEMORYCHECK)]);
            if (aOpi == IOMIHistoryState.VMMEMORYCHECK)
                dataRow[6] = Globals.memoryUnit(aExtrLong);
            else
                dataRow[6] = "-----";
            dataRow[7] = "" + (counts[useMap.get(IOMIHistoryState.GCEND)]);
            if (aOpi == IOMIHistoryState.GCEND)
                dataRow[8] = Globals.memoryUnit(aExtrLong);
            else
                dataRow[8] = "-----";
        }
    }

    //###########################################################################

    /**
     * Last valid entry time.
     */
    protected long aLastEntryTime = -1;

    /**
     * The array of operations to be used. Defined by each specific derived type
     * from the TableDefinitionUseData object.
     */
    protected HistoryOperation[] aUseOperation = null;

    /**
     * Standard constructor.
     *
     * @param iomihoh The IOMIHostObjectHistory object from which the data is
     *                taken for output.
     */
    protected IOMIHistoryTableUseData(IOMIHistoryList iomihoh) {
        super(iomihoh);
    }

    /**
     * Outputs all data stored in the history container to the returned array
     * list. The data is output in a column table where the columns include:
     * entry index (0->n), operation, ioIndex (or name if the input interface
     * is not null), time of event, elapsed time since start, elapsed time
     * since last entry of this ioIndex, and the elapsed time since previous
     * entry.
     *
     * @param ii An index interface to convert IOMIObject indexes to human
     *           readable form. If null the index is used directly in the
     *           output.
     */
    @Override
    protected ArrayList<String[]> getData(HistoryIndexInterface ii) {
        // create list and get history values as an array on ordinal. Create a
        // map to hold the most recent time entry of each discovered IOMI object
        // index. Finally, create a set to hold the necessary "current" operations.

        ArrayList<String[]> rowData = new ArrayList<String[]>(1024);
        HistoryOperation[] ops = IOMIHistoryState.values();

        HashMap<HistoryOperation, Integer> useMap = new HashMap<HistoryOperation, Integer>();
        for (int i = 0; i < aUseOperation.length; ++i)
            useMap.put(aUseOperation[i], i);

        int[] counts = new int[aUseOperation.length];

        // set the history byte container index i and entry operation count to
        // zero

        int i = 0;
        int entryCnt = 0;

        // set some time variables to initial conditions

        resetTimes();

        // loop over all data

        while (i < aHistory.size()) {
            // get the next operation type and check for type TIME

            i = getNextType(i, ops);
            if (aLastEntryTime == -1) aLastEntryTime = aStrtTime;
            if (!aOpi.isTime()) {
                // read index and times

                i = getNextTimes(i);
                i = getNextIndex(i);
                i = getExtraLong(i, aOpi.hasExtraLong());

                // see if operation is one of the required set

                Integer countIndex = useMap.get(aOpi);
                if (countIndex != null) {
                    // increment count arrays

                    ++counts[countIndex];

                    // now build the data ... make the container and add it to the data
                    // list

                    String[] dataRow = new String[aColHdr[0].length];
                    rowData.add(dataRow);
                    adjustLastIndex(entryCnt);

                    // add the entry count and increment and the op name

                    dataRow[0] = "" + entryCnt++;
                    dataRow[1] = aOpi.name();

                    // add the indx (or human readable form if ii is defined) and the
                    // time elapsed since the last time this index was found)

                    getIndexData(dataRow, ii);

                    // set the current time, the time since start, and the time since the
                    // last entry.

                    dataRow[3] = Globals.getTimeStamp(aCurrTime);
                    dataRow[4] = Globals.elapsedTimeString3(aStrtTime, aCurrTime);
                    dataRow[6] = Globals.elapsedTimeString3(aLastEntryTime, aCurrTime);
                    aLastEntryTime = aCurrTime;

                    // set the current time, the time since start, and the time since the
                    // last entry.

                    getUseData(dataRow, counts, useMap);
                }
            }
        }

        // done return data

        return rowData;
    }

    /**
     * The requested useData for this table type.
     *
     * @param dataRow The row of data entries.
     * @param counts  The column entry count arrays.
     * @param useMap  The map of HistoryOperation(s) associated with the index
     *                in the count array.
     */
    protected abstract void getUseData(String[] dataRow, int[] counts,
                                       HashMap<HistoryOperation, Integer> useMap);
}
