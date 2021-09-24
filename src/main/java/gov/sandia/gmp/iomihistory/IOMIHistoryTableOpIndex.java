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

import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Globals.TableAlignment;

/**
 * Defines all output table definitions that use an operation or index for
 * output. These tables include "All", "Operation", "Object", and
 * "OperationObject". An inner class is defined for each of these table types
 * that defines the table column headers, and the table title.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class IOMIHistoryTableOpIndex extends HistoryTable {
    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing all
     * history entries.
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableAll extends IOMIHistoryTableOpIndex {
        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableAll(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // define column header

            String[][] colHdr = {{"", "", "", "", "", "Time Since", "Time Since", "Other Data"},
                    {"Entry", "", "IOMI", "", "Time Since", "Previous", "Previous", "(Memory/"},
                    {"Index", "Operation", "Object", "Time", "Start", "Object Entry", "Entry", "Time)"}};
            aColHdr = colHdr;
            aHdr = "    ";
            aTitle = "IOMIManager State History Output";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested Op/Index Data for this table type.
         *
         * @param dataRow  The row of data entries.
         * @param entryCnt The current operation entry count which is incremented
         *                 and returned to the caller.
         * @param ii       An index interface to convert IOMIObject indexes to human
         *                 readable form. If null the index is used directly in the
         *                 output.
         */
        @Override
        protected int getOpIndexData(ArrayList<String[]> rowData, int entryCnt,
                                     HistoryIndexInterface ii) {
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

            // if the extra amount was defined convert to time/memory and save it

            if (aExtrLong == -1)
                dataRow[7] = "";
            else if ((aOpi == IOMIHistoryState.VMMEMORYCHECK) ||
                    (aOpi == IOMIHistoryState.GCEND))
                dataRow[7] = Globals.memoryUnit(aExtrLong);
            else if (aOpi == IOMIHistoryState.THREADDELAY)
                dataRow[7] = Globals.elapsedTimeString3(0, aExtrLong);

            return entryCnt;
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing only
     * history entries for a specific HistoryOperation (aOp).
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableOperation extends IOMIHistoryTableOpIndex {
        /**
         * The specific history operation for which a table will be constructed.
         */
        private HistoryOperation aOp = null;

        /**
         * The array of column headers. Can be one of 4 possible configurations.
         * See the constructor for details.
         */
        private String[][][] aColHdrA = null;

        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         */
        public IOMIHistoryTableOperation(IOMIHistoryList iomihoh) {
            super(iomihoh);

            // define column header

            String[][][] colHdr = {{{"", "", "", "", "Time Since", "Time Since", ""},
                    {"Entry", "IOMI", "", "Time Since", "Previous", "Previous", "Thread"},
                    {"Index", "Object", "Time", "Start", "Object Entry", "Entry", "Delay"}},
                    {{"", "", "", "", "Time Since", "Time Since"},
                            {"Entry", "IOMI", "", "Time Since", "Previous", "Previous"},
                            {"Index", "Object", "Time", "Start", "Object Entry", "Entry"}},
                    {{"", "", "", "Time Since", "Other Data"},
                            {"Entry", "", "Time Since", "Previous", "Memory"},
                            {"Index", "Time", "Start", "Entry", "Use"}},
                    {{"", "", "", "Time Since"},
                            {"Entry", "", "Time Since", "Previous"},
                            {"Index", "Time", "Start", "Entry"}}};

            // just pick CREATE to begin with

            aColHdrA = colHdr;
            setOperation(IOMIHistoryState.CREATE);
        }

        /**
         * Defines a new operation for which a table is constructed.
         *
         * @param op
         */
        @Override
        public void setOperation(HistoryOperation op) {
            // determine which column structure to use

            int i = 0;
            aOp = op;
            if (!aOp.hasIndex()) i += 2;
            if (!aOp.hasExtraLong()) ++i;

            // reinitialize table

            aColHdr = aColHdrA[i];
            aTitle = "IOMIManager State History Output (Operation = " +
                    aOp.name() + ")";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested Op/Index Data for this table type.
         *
         * @param dataRow  The row of data entries.
         * @param entryCnt The current operation entry count which is incremented
         *                 and returned to the caller.
         * @param ii       An index interface to convert IOMIObject indexes to human
         *                 readable form. If null the index is used directly in the
         *                 output.
         */
        @Override
        protected int getOpIndexData(ArrayList<String[]> rowData, int entryCnt,
                                     HistoryIndexInterface ii) {
            if (aOpi == aOp) {
                // now build the data ... make the container and add it to the data
                // list

                String[] dataRow = new String[aColHdr[0].length];
                rowData.add(dataRow);
                adjustLastIndex(entryCnt);

                // add the entry count and increment and the op name

                dataRow[0] = "" + entryCnt++;

                // add the indx (or human readable form if ii is defined) and the
                // time elapsed since the last time this index was found)

                int li = -1;
                if (aIndx[0] > -1) {
                    // index is defined ... add indx (or human readable form), current
                    // time, time since start, time since last entry with index, and
                    // time since last entry ... set last index (li=6)

                    if (ii == null)
                        dataRow[1] = "" + aIndx[0];
                    else
                        dataRow[1] = ii.getIOIndexName(aIndx[0]);
                    dataRow[2] = Globals.getTimeStamp(aCurrTime);
                    dataRow[3] = Globals.elapsedTimeString3(aStrtTime, aCurrTime);
                    dataRow[4] = Globals.elapsedTimeString3(aLastIndexTime, aCurrTime) +
                            getLastIndexEntry();
                    dataRow[5] = Globals.elapsedTimeString3(aLastEntryTime, aCurrTime);
                    li = 6;
                } else {
                    // index is not defined ... set current time, time since last
                    // start, and time since last entry ... set last index (li=4)

                    dataRow[1] = Globals.getTimeStamp(aCurrTime);
                    dataRow[2] = Globals.elapsedTimeString3(aStrtTime, aCurrTime);
                    dataRow[3] = Globals.elapsedTimeString3(aLastEntryTime, aCurrTime);
                    li = 4;
                }
                aLastEntryTime = aCurrTime;

                // if the extra amount was defined convert to time/memory and save it

                if (aOpi.hasExtraLong()) {
                    if ((aOpi == IOMIHistoryState.VMMEMORYCHECK) ||
                            (aOpi == IOMIHistoryState.GCEND))
                        dataRow[li] = Globals.memoryUnit(aExtrLong);
                    else if (aOpi == IOMIHistoryState.THREADDELAY)
                        dataRow[li] = Globals.elapsedTimeString3(0, aExtrLong);
                    else
                        dataRow[li] = "";
                }
            }

            return entryCnt;
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing only
     * history entries for a specific IOMI object index (aIOMIIndex).
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableObject extends IOMIHistoryTableOpIndex {
        /**
         * The object index for which the table is constructed.
         */
        private int aIOMIIndex = -1;

        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         *
         * @throws IOException
         */
        public IOMIHistoryTableObject(IOMIHistoryList iomihoh)
                throws IOException {
            super(iomihoh);

            // define column header

            String[][] colHdr = {{"", "", "", "", "Time Since", ""},
                    {"Entry", "", "", "Time Since", "Previous", "Thread"},
                    {"Index", "Operation", "Time", "Start", "Entry", "Delay"}};

            // just pick CREATE to begin with

            aColHdr = colHdr;
            setIndex(0);
        }

        /**
         * Defines a new operation for which a table is constructed.
         *
         * @param op
         */
        @Override
        public void setIndex(int indx) throws IOException {
            // reinitialize table

            aIOMIIndex = indx;
            aTitle = "IOMIManager State History Output (IOMI Object Index = " +
                    aIOMIIndex + ")";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested Op/Index Data for this table type.
         *
         * @param dataRow  The row of data entries.
         * @param entryCnt The current operation entry count which is incremented
         *                 and returned to the caller.
         * @param ii       An index interface to convert IOMIObject indexes to human
         *                 readable form. If null the index is used directly in the
         *                 output.
         */
        @Override
        protected int getOpIndexData(ArrayList<String[]> rowData, int entryCnt,
                                     HistoryIndexInterface ii) {
            // if index is aIOMIIndex

            if (aIndx[0] == aIOMIIndex) {
                // now build the data ... make the container and add it to the data
                // list

                String[] dataRow = new String[aColHdr[0].length];
                rowData.add(dataRow);
                adjustLastIndex(entryCnt);

                // add the entry count and increment and the op name

                dataRow[0] = "" + entryCnt++;
                dataRow[1] = aOpi.name();

                // index is not defined ... set current time, time since last
                // start, and time since last entry ... set last index (li=4)

                dataRow[2] = Globals.getTimeStamp(aCurrTime);
                dataRow[3] = Globals.elapsedTimeString3(aStrtTime, aCurrTime);
                dataRow[4] = Globals.elapsedTimeString3(aLastEntryTime, aCurrTime);
                aLastEntryTime = aCurrTime;

                // if the extra amount was defined convert to time/memory and save it

                if (aOpi.hasExtraLong())
                    dataRow[5] = Globals.elapsedTimeString3(0, aExtrLong);
                else
                    dataRow[5] = "";
            }

            return entryCnt;
        }
    }

    //###########################################################################

    /**
     * Derived TableDefinition class that formulates a table containing only
     * history entries for a specific HistoryOperation (aOp).
     *
     * @author jrhipp
     */
    public static class IOMIHistoryTableOperationObject extends IOMIHistoryTableOpIndex {
        /**
         * The specific history operation for which a table will be constructed.
         */
        private HistoryOperation aOp = null;

        /**
         * The object index for which the table is constructed.
         */
        private int aIOMIIndex = -1;

        /**
         * The array of column headers. Can be one of 4 possible configurations.
         * See the constructor for details.
         */
        private String[][][] aColHdrA = null;

        /**
         * Default constructor. Defines the title, and column header and initializes
         * the object.
         *
         * @throws IOException
         */
        public IOMIHistoryTableOperationObject(IOMIHistoryList iomihoh)
                throws IOException {
            super(iomihoh);

            // define column header

            String[][][] colHdr = {{{"", "", "", "Time Since", "Other Data"},
                    {"Entry", "", "Time Since", "Previous", "Memory"},
                    {"Index", "Time", "Start", "Entry", "Use"}},
                    {{"", "", "", "Time Since"},
                            {"Entry", "", "Time Since", "Previous"},
                            {"Index", "Time", "Start", "Entry"}}};

            // just pick CREATE to begin with

            aColHdrA = colHdr;
            setOperationIndex(IOMIHistoryState.CREATE, 0);
        }

        /**
         * Defines a new operation for which a table is constructed.
         *
         * @param op
         */
        @Override
        public void setOperationIndex(HistoryOperation op, int indx) throws IOException {
            // determine which column structure to use

            int i = 0;
            aOp = op;
            if (!aOp.hasExtraLong()) ++i;

            if (!aOp.hasIndex()) {
                throw new IOException("Error: History Operation: " + aOp.name() +
                        ", does not support an IOMIObject Index ...");
            }

            // reinitialize table

            aIOMIIndex = indx;
            aColHdr = aColHdrA[i];
            aTitle = "IOMIManager State History Output (Operation = " +
                    aOp.name() + ", IOMI Object Index = " + aIOMIIndex + ")";
            initialize(TableAlignment.CENTER);
        }

        /**
         * The requested Op/Index Data for this table type.
         *
         * @param dataRow  The row of data entries.
         * @param entryCnt The current operation entry count which is incremented
         *                 and returned to the caller.
         * @param ii       An index interface to convert IOMIObject indexes to human
         *                 readable form. If null the index is used directly in the
         *                 output.
         */
        @Override
        protected int getOpIndexData(ArrayList<String[]> rowData, int entryCnt,
                                     HistoryIndexInterface ii) {
            if ((aOpi == aOp) && (aIndx[0] == aIOMIIndex)) {
                // now build the data ... make the container and add it to the data
                // list

                String[] dataRow = new String[aColHdr[0].length];
                rowData.add(dataRow);
                adjustLastIndex(entryCnt);

                // add the entry count and increment and the op name

                dataRow[0] = "" + entryCnt++;

                // index is not defined ... set current time, time since last
                // start, and time since last entry ... set last index (li=4)

                dataRow[1] = Globals.getTimeStamp(aCurrTime);
                dataRow[2] = Globals.elapsedTimeString3(aStrtTime, aCurrTime);
                dataRow[3] = Globals.elapsedTimeString3(aLastEntryTime, aCurrTime);
                aLastEntryTime = aCurrTime;

                // if the extra amount was defined convert to time/memory and save it

                if (aOpi.hasExtraLong()) {
                    if ((aOpi == IOMIHistoryState.VMMEMORYCHECK) ||
                            (aOpi == IOMIHistoryState.GCEND))
                        dataRow[4] = Globals.memoryUnit(aExtrLong);
                    else if (aOpi == IOMIHistoryState.THREADDELAY)
                        dataRow[4] = Globals.elapsedTimeString3(0, aExtrLong);
                    else
                        dataRow[4] = "";
                }
            }

            return entryCnt;
        }
    }

    //###########################################################################

    /**
     * Last valid entry time.
     */
    protected long aLastEntryTime = -1;

    /**
     * Standard constructor.
     *
     * @param iomihoh The IOMIHostObjectHistory object from which the data is
     *                taken for output.
     */
    protected IOMIHistoryTableOpIndex(IOMIHistoryList iomihoh) {
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
        // create list and get history values as an array on ordinal.

        ArrayList<String[]> rowData = new ArrayList<String[]>(1024);
        HistoryOperation[] ops = IOMIHistoryState.values();

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
                // Not a Time Type ... read index and times

                i = getNextTimes(i);
                i = getNextIndex(i);
                i = getExtraLong(i, aOpi.hasExtraLong());
                entryCnt = getOpIndexData(rowData, entryCnt, ii);
            }
        }

        return rowData;
    }

    /**
     * The requested Op/Index Data for this table type.
     *
     * @param dataRow The row of data entries.
     * @param opCnt   The current operation count which is incremented and
     *                returned to the caller.
     * @param ii      An index interface to convert IOMIObject indexes to human
     *                readable form. If null the index is used directly in the
     *                output.
     */
    protected abstract int getOpIndexData(ArrayList<String[]> rowData, int opCnt,
                                          HistoryIndexInterface ii);
}
