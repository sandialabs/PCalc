package gov.sandia.gmp.iomihistory;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.gmp.util.containers.arraylist.ArrayListByte;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Globals.TableAlignment;

/**
 * Primary Table Definition super class used to output history. Manages table
 * construction where the derived class defines the column structure (in the
 * constructor) and defines a single function getData() that is used to
 * build the data for the structure. The output can be defined to any print
 * stream (PrintStream) such as System.out or a file using the single function
 * <p>
 * outputData(PrintStream prntr, int repeatHdr, boolean destroyData,
 * IOMIHostObjectHistoryIndexInterface ii);
 * <p>
 * where the repeatHdr is used to repeat the header at a set interval during
 * the output for easy parsing. If set <= zero the header is only output once
 * (at the beginning of the table). The attached interface can be null, in
 * which case the IOMIObject indexes are printed in the tables. If it is not
 * null then it defines a single function that can be used to decode the
 * index into a human readable character string (e.g. 1022 = "chol_14_10").
 * <p>
 * Once this function is called the data table is saved so that multiple
 * calls with different streams can be made if desired. The user should call
 * resetData() to nullify the data and free the space (perhaps considerable
 * for large histories). The flag destroyData can be used to accomplish the
 * same thing in the call above by setting it equal to true.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class HistoryTable implements Serializable {
    /**
     * Size of a long in bytes.
     */
    private static final int aLongSize = Long.SIZE / 8;

    /**
     * Size of an int in bytes.
     */
    private static final int aIntSize = Integer.SIZE / 8;

    /**
     * The total table width (calculated).
     */
    private int aTableWidth = 0;

    /**
     * The table title (set by the derived class).
     */
    protected String aTitle = "";

    /**
     * A multi-line table header (set by the derived class).
     */
    protected String[][] aColHdr = null;

    /**
     * The column width of each table column (calculated).
     */
    private int[] aColWidth = null;

    /**
     * The additional data spacing used in table output (calculated).
     */
    private String[] aColHdrSpc = null;

    /**
     * The column alignment property (set by the derived class, or separately
     * by the caller).
     */
    private TableAlignment[] aColAlign = null;

    /**
     * The Table header (calculated).
     */
    private String[] aTableHdr = null;

    /**
     * The table header/data separator line (calculated).
     */
    private String aTableHdrSep = "";

    /**
     * The table separator line (calculated).
     */
    private String aTableSep = "";

    /**
     * The pre-pend header (set by the derived class, or separately by the
     * caller).
     */
    protected String aHdr = "";

    /**
     * The table data (calculated by the derived class).
     */
    private ArrayList<String[]> aRowData = null;

    /**
     * The history list.
     */
    protected ArrayListByte aHistory = null;

    /**
     * An 8 byte buffer to convert long times to bytes
     */
    private ByteBuffer aLongBuf = ByteBuffer.allocate(8);

    /**
     * A 2 byte buffer to convert short elapsed times in seconds to bytes.
     */
    private ByteBuffer aIntegerBuf = ByteBuffer.allocate(4);

    /**
     * The HistoryList whose data will be used. Assigned at construction.
     */
    private HistoryList aHistList = null;

    //*************************************************************************

    /**
     * Used by derived types to define the current entry operation.
     */
    protected HistoryOperation aOpi = null;

    /**
     * Used by derived types to define the current entry time.
     */
    protected long aCurrTime = -1;

    /**
     * Used by derived types to define the previous entry time.
     */
    protected long aLastTime = -1;

    /**
     * Used by derived types to define the previous object index entry time.
     */
    protected long aLastIndexTime = -1;

    /**
     * Used by derived types to define the previous object index entry count.
     */
    protected int aLastIndexEntryCnt = -1;

    /**
     * Used by derived types to define the history start time.
     */
    protected long aStrtTime = -1;

    /**
     * Used by derived types to define the last "TIME" entry time.
     */
    protected long aBaseTime = -1;

    /**
     * Used by derived types to define the "extra long" entry.
     */
    protected long aExtrLong = -1;

    /**
     * Used by derived types to define an "extra byte" entry.
     */
    protected byte aExtrByte = -1;

    /**
     * Used by derived types to define the current entry index (if defined).
     * The one used by IOMI is index 0, others can utilize subsequent indices
     */
    protected int[] aIndx = {-1, -1, -1, -1};

    /**
     * Used by derived types to define the current elapsed time beyond the last
     * base time entry.
     */
    protected int aETim = -1;

    /**
     * Used by derived types to contain the previous entry time for each unique
     * index found in the history file.
     */
    HashMap<Integer, Long> aLastIndexTimeMap = null;

    /**
     * Used by derived types to contain the previous entry count for each unique
     * index found in the history file.
     */
    HashMap<Integer, Integer> aLastIndexEntryCntMap = null;

    /**
     * Standard constructor.
     */
    protected HistoryTable(HistoryList histList) {
        aLongBuf.position(0);
        aLongBuf.mark();
        aIntegerBuf.position(0);
        aIntegerBuf.mark();
        aHistList = histList;
        if (histList != null) aHistory = histList.getHistory();
    }

    /**
     * Can be set by the caller to change the column alignment style.
     *
     * @param ta The new column alignment style.
     */
    public void setColAlignment(TableAlignment ta) {
        for (int i = 0; i < aColAlign.length; ++i) aColAlign[i] = ta;
    }

    /**
     * Can be set by the caller to change the column alignment style for each
     * column separately.
     *
     * @param colalgn The new column alignment vector.
     * @throws IOException
     */
    public void setColAlignment(TableAlignment[] colalgn) throws IOException {
        if (colalgn.length != aColHdr[0].length)
            throw new IOException("Error: Input column alignment array length (" +
                    colalgn.length + ") is different than the column" +
                    " header length (" + aColHdr[0].length);
    }

    /**
     * Can be set by the caller to change the pre-pend header (defaults in the
     * derived class constructor).
     *
     * @param hdr
     */
    public void setPrePendHeader(String hdr) {
        aHdr = hdr;
        if (aRowData != null) buildTable();
    }

    /**
     * Can be called by the caller to force the calculation of the data. Useful
     * if the history has changed.
     */
    public void setData() {
        setData(null);
    }

    /**
     * Can be called by the caller to force the calculation of the data. Useful
     * if the history has changed.
     *
     * @param ii The interface that can turn the data row IOMI object index into
     *           a human readable character string.
     */
    public void setData(HistoryIndexInterface ii) {
        aRowData = getData(ii);
        if (aRowData.size() > 0) buildTable();
    }

    /**
     * Resets the data (nullifies).
     */
    public void resetData() {
        aRowData = null;
    }

    /**
     * Primary function used by the caller to output the data in a table on the
     * input print stream. The header is printed once at the beginning of the
     * table. The data container is nullified on exit. IOMIObject indexes are
     * printed directly.
     *
     * @param prntr     The PrintStream into which the data is written.
     * @param repeatHdr The header repeat interval (once at the top if <= 0).
     */
    public void outputData(PrintStream prntr) {
        outputData(prntr, 0, true, null);
    }

    /**
     * Primary function used by the caller to output the data in a table on the
     * input print stream. If repeatHdr > 0 then the header is repeated by that
     * interval between data row outputs (for readability). Otherwise, it is
     * printed once at the beginning of the table. The data container is
     * nullified on exit. IOMIObject indexes are printed directly.
     *
     * @param prntr     The PrintStream into which the data is written.
     * @param repeatHdr The header repeat interval (once at the top if <= 0).
     */
    public void outputData(PrintStream prntr, int repeatHdr) {
        outputData(prntr, repeatHdr, true, null);
    }

    /**
     * Primary function used by the caller to output the data in a table on the
     * input print stream. If repeatHdr > 0 then the header is repeated by that
     * interval between data row outputs (for readability). Otherwise, it is
     * printed once at the beginning of the table. If destroyData is true the
     * data container is nullified on exit. IOMIObject indexes are printed
     * directly.
     *
     * @param prntr       The PrintStream into which the data is written.
     * @param repeatHdr   The header repeat interval (once at the top if <= 0).
     * @param destroyData Destroys the data on exit if true.
     */
    public void outputData(PrintStream prntr, int repeatHdr, boolean destroyData) {
        outputData(prntr, repeatHdr, destroyData, null);
    }

    /**
     * Primary function used by the caller to output the data in a table on the
     * input print stream. If repeatHdr > 0 then the header is repeated by that
     * interval between data row outputs (for readability). Otherwise, it is
     * printed once at the beginning of the table. If destroyData is true the
     * data container is nullified on exit. If the interface function is
     * defined the IOMIOjbect index is converted to a human readable form.
     * Otherwise, the index is output directly.
     *
     * @param prntr       The PrintStream into which the data is written.
     * @param repeatHdr   The header repeat interval (once at the top if <= 0).
     * @param destroyData Destroys the data on exit if true.
     * @param ii          The IOMIObject index converter (to human readable form).
     */
    public void outputData(PrintStream prntr, int repeatHdr,
                           boolean destroyData,
                           HistoryIndexInterface ii) {
        // load the data if it is not defined.

        if (aRowData == null) setData(ii);

        // center title in tableWidth

        prntr.print(aTableSep);
        prntr.print(aHdr + aHistList.getHostName() + NL);
        prntr.print(aHdr + Globals.getTimeStamp(aHistList.getStartTime()) + NL);
        prntr.print(aHdr + NL);
        prntr.print(aHdr + NL);
        prntr.print(aHdr + Globals.centerString(aTitle, aTableWidth, true) + NL);
        prntr.print(aHdr + NL);

        // print header now if it is to be printed one time only ... print data

        if ((repeatHdr <= 0) || (aRowData.size() == 1)) printHeader(prntr);
        for (int i = 0; i < aRowData.size(); ++i) {
            if ((repeatHdr > 0) && (i % repeatHdr == 0) && (i != aRowData.size() - 1))
                printHeader(prntr);
            String row = getTableDataRow(aHdr, aRowData.get(i), aColWidth, aColHdrSpc,
                    aColAlign);
            prntr.print(row);
        }
        prntr.print(aTableSep);
        prntr.println("");
        prntr.println("");

        // free the data table if requested

        if (destroyData) resetData();
    }

    /**
     * Called by the derived class constructor to finish initialization.
     *
     * @param ta The initial table column alignment.
     */
    protected void initialize(TableAlignment ta) {
        aColAlign = new TableAlignment[aColHdr[0].length];
        for (int i = 0; i < aColAlign.length; ++i) aColAlign[i] = ta;
        aColWidth = new int[aColHdr[0].length];
        aColHdrSpc = new String[aColHdr[0].length];
    }

    /**
     * Defines the table once the row data is available.
     */
    private void buildTable() {
        aTableWidth = buildTableWidths(aHdr, aColHdr, aRowData, 2,
                aColWidth, aColHdrSpc);
        aTableHdr = makeTableHeader(aHdr, aColHdr, aColWidth);
        aTableHdrSep = aHdr + Globals.repeat("-", aTableWidth) + NL;
        aTableSep = aHdr + Globals.repeat("#", aTableWidth) + NL;
    }

    /**
     * Prints the header information and line separators.
     *
     * @param prntr The output PrintStream into which the header is output.
     */
    private void printHeader(PrintStream prntr) {
        prntr.print(aTableHdrSep);
        for (int j = 0; j < aTableHdr.length; ++j) prntr.print(aTableHdr[j]);
        prntr.print(aTableHdrSep);
    }

    /**
     * Returns a long from the history file beginning at location i.
     *
     * @param i The location in the history file from which the long is read.
     * @return The long stored in the history file beginning at location i.
     */
    private long getLong(int i) {
        aLongBuf.reset();
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.put(aHistory.get(i++));
        aLongBuf.reset();
        Long L = aLongBuf.getLong();
        aLongBuf.reset();
        return L;
    }

    /**
     * Returns an integer from the history file beginning at location i.
     *
     * @param i The location in the history file from which an integer is read.
     * @return The integer stored in the history file beginning at location i.
     */
    private int getInt(int i) {
        aIntegerBuf.reset();
        aIntegerBuf.put(aHistory.get(i++));
        aIntegerBuf.put(aHistory.get(i++));
        aIntegerBuf.put(aHistory.get(i++));
        aIntegerBuf.put(aHistory.get(i++));
        aIntegerBuf.reset();
        int I = aIntegerBuf.getInt();
        aIntegerBuf.reset();
        return I;
    }

    /**
     * Called by derived classes to set the IOMIObject index and last index time
     * into data rows for output in a table.
     *
     * @param dataRow The data row into which the index and last index time is
     *                to be set.
     * @param ii      A converter that converts an IOMIObject index into a human
     *                readable string.
     */
    protected void getIndexData(String[] dataRow,
                                HistoryIndexInterface ii) {
        if (aIndx[0] == -1) {
            dataRow[2] = "-----";
            dataRow[5] = "-----";
        } else {
            if (ii == null)
                dataRow[2] = "" + aIndx[0];
            else
                dataRow[2] = ii.getIOIndexName(aIndx[0]);
            dataRow[5] = Globals.elapsedTimeString3(aLastIndexTime, aCurrTime) +
                    getLastIndexEntry();
        }
    }

    /**
     * Called by derived types to reset the time values.
     */
    protected void resetTimes() {
        aOpi = null;
        aCurrTime = aLastTime = aLastIndexTime = aStrtTime = aBaseTime = -1;
        aLastIndexEntryCnt = -1;
        aExtrLong = -1;
        aIndx[0] = aIndx[1] = aIndx[2] = aIndx[3] = aETim = -1;
        aLastIndexTimeMap = new HashMap<Integer, Long>();
        aLastIndexEntryCntMap = new HashMap<Integer, Integer>();
    }

    /**
     * Called by derived classes to obtain the next operation type. If this type
     * is "TIME" then it is read and set into base time.
     *
     * @param i   The current history container index.
     * @param ops The array of possible history operations.
     * @return The new history container index for the next read.
     */
    protected int getNextType(int i, HistoryOperation[] ops) {
        byte type = aHistory.get(i++);
        aOpi = ops[type];
        if (aOpi.isTime()) {
            // TIME type get new base time and set start time if not defined.

            aBaseTime = getLong(i);
            i += aLongSize;
            if (aStrtTime == -1) aStrtTime = aBaseTime;
        }

        return i;
    }

    /**
     * Called by derived classes to obtain the next IOMIObject index. If not
     * defined the index is set to -1.
     *
     * @param i The current history container index.
     * @return The new history container index for the next read.
     */
    protected int getNextIndex(int i) {
        aIndx[0] = -1;
        if (aOpi.hasIndex()) {
            aIndx[0] = getInt(i);
            i += aIntSize;
        }

        return i;
    }

    /**
     * Called by derived classes to obtain the next IOMIObject index. If not
     * defined the index is set to -1.
     *
     * @param i The current history container index.
     * @return The new history container index for the next read.
     */
    protected int getNextIndex(int i, int j, boolean validIndex) {
        aIndx[j] = -1;
        if (validIndex) {
            aIndx[j] = getInt(i);
            i += aIntSize;
        }

        return i;
    }

    /**
     * Returns an extra long and increments the history container counter if
     * the input boolean is true.
     *
     * @param i              The index into the history container from which the
     *                       long is read.
     * @param validExtraLong A boolean which if true indicates that the history
     *                       container will read the long into aExtrLong,
     *                       increment the history container counter (i), and
     *                       return the new container index (i+8).
     * @return The new container index (i+8).
     */
    protected int getExtraLong(int i, boolean validExtraLong) {
        aExtrLong = -1;
        if (validExtraLong) {
            aExtrLong = getLong(i);
            i += aLongSize;
        }

        return i;
    }

    /**
     * Returns an extra byte and increments the history container counter if
     * the input boolean is true.
     *
     * @param i              The index into the history container from which the
     *                       byte is read.
     * @param validExtraByte A boolean which if true indicates that the history
     *                       container will read the byte into aExtrByte,
     *                       increment the history container counter (i), and
     *                       return the new container index (i+1).
     * @return The new container index (i+1).
     */
    protected int getExtraByte(int i, boolean validExtraByte) {
        aExtrByte = -1;
        if (validExtraByte) aExtrByte = aHistory.get(i++);

        return i;
    }

    /**
     * Returns the index name as an integer or a human readable form if the
     * HistoryIndexInterface (ii) is not null.
     *
     * @param j  The index position of aIndx to read.
     * @param ii The human readable interface function.
     * @return The index aIndx[j] as a string.
     */
    protected String getIndexName(int j, HistoryIndexInterface ii) {
        if (ii == null)
            return "" + aIndx[j];
        else
            return ii.getIOIndexName(aIndx[j]);
    }

    /**
     * Called by derived classes to obtain the elapsed time and "extra amount".
     * If the "extra amount" is not defined it is set to -1. This function also
     * assigns the last and current times, and if defined, the last index time.
     *
     * @param i The current history container index.
     * @return The new history container index for the next read.
     */
    protected int getNextTimes(int i) {
        aETim = (int) aHistory.get(i++) - Byte.MIN_VALUE;

        // set the previous current time to last time and get the next one.
        // (the elapsed time is stored in seconds ... convert to msec to add)

        aLastTime = aCurrTime;
        if (aLastTime == -1) aLastTime = aBaseTime;
        aCurrTime = aBaseTime + 1000 * aETim;

        return i;
    }

    /**
     * Adjusts the last object index and entry count if the object index is
     * defined (aIndx[0] != 0). Both the last index time and entry are defined.
     *
     * @param entryCnt
     */
    protected void adjustLastIndex(int entryCnt) {
        // get the current index (indx) last time value and set it into
        // lastIndexTime ... update the map with the new time.

        aLastIndexTime = -1;
        aLastIndexEntryCnt = -1;
        if (aIndx[0] > -1) {
            Long itim = aLastIndexTimeMap.get(aIndx[0]);
            if (itim == null) {
                aLastIndexTime = aStrtTime;
            } else {
                aLastIndexTime = itim;
                aLastIndexEntryCnt = aLastIndexEntryCntMap.get(aIndx[0]);
            }
            aLastIndexTimeMap.put(aIndx[0], aCurrTime);
            aLastIndexEntryCntMap.put(aIndx[0], entryCnt);
        }
    }

    /**
     * Returns the last index entry count as a string for output.
     *
     * @return The last index entry count as a string for output.
     */
    protected String getLastIndexEntry() {
        if (aLastIndexEntryCnt == -1)
            return " (INIT)";
        else
            return " (" + aLastIndexEntryCnt + ")";
    }

    /**
     * Defined in TableDefinitionOpIndex to set the object index for a table.
     *
     * @param iomiObjIndex The object index to be output in a table.
     * @throws IOException
     */
    public void setIndex(int iomiObjIndex) throws IOException {
        // no code
    }

    /**
     * Defined in TableDefinitionOpIndex to set the history operation for a table.
     *
     * @param op The history operation to be output in a table.
     * @throws IOException
     */
    public void setOperation(HistoryOperation op) {
        // no code
    }

    /**
     * Defined in TableDefinitionOpIndex to set the object index and history
     * operation for a table.
     *
     * @param op    The history operation to be output in a table.
     * @param index The object index to be output in a table.
     * @throws IOException
     */
    public void setOperationIndex(HistoryOperation op, int index) throws IOException {
        // no code
    }

    /**
     * The function that retrieves the table data. Defined by the derived
     * classes.
     *
     * @param ii The IOMIObject index filter used to convert the index to a
     *           human readable form. Can be null (print index directly).
     * @return The list of all data.
     */
    protected abstract ArrayList<String[]>
    getData(HistoryIndexInterface ii);

    /**
     * Calculates column width and spacings and the table width given the input
     * line pre-pend header, the multi-line column header (colHdr), the data,
     * and the additional column space (colspc). The colWidth, dataColWidth, and
     * colHdrSpc must all be pre-sized to the number of columns (colHdr[0].length
     * or data[0].length which must be equal).
     *
     * @param hdr       A pre-pend header to the beginning of every output line.
     * @param colHdr    A multi-line column header.
     * @param data      The data [row][column]
     * @param colspc    Additional character spacing between columns.
     * @param colWidth  The maximum character width of each column (includes
     *                  headers and data). Returned result.
     * @param colHdrSpc Additional data character padding space for each column.
     *                  Returned result.
     * @return The total table width.
     */
    public static int buildTableWidths(String hdr, String[][] colHdr,
                                       ArrayList<String[]> data, int colspc,
                                       int[] colWidth, String[] colHdrSpc) {
        int[] dataColWidth = new int[data.get(0).length];

        // adjust column widths from the data

        String[] dataRow;
        for (int i = 0; i < data.size(); ++i) {
            dataRow = data.get(i);
            for (int j = 0; j < dataRow.length; ++j) {
                if (i == 0) {
                    colWidth[j] = dataRow[j].length();
                    dataColWidth[j] = dataRow[j].length();
                } else {
                    if (colWidth[j] < dataRow[j].length())
                        colWidth[j] = dataRow[j].length();
                    if (dataColWidth[j] < dataRow[j].length())
                        dataColWidth[j] = dataRow[j].length();
                }
            }
        }

        // if column headers are defined adjust the column widths with those

        if (colHdr != null) {
            String[] colHdrRow;
            for (int i = 0; i < colHdr.length; ++i) {
                colHdrRow = colHdr[i];
                for (int j = 0; j < colHdrRow.length; ++j) {
                    if (colWidth[j] < colHdrRow[j].length())
                        colWidth[j] = colHdrRow[j].length();
                }
            }
        }

        // now colWidth has largest value for each column ... add colspc and
        // calculate table width

        int tableWidth = 0;
        for (int j = 0; j < colWidth.length; ++j) {
            colWidth[j] += colspc;
            tableWidth += colWidth[j];
        }

        // create data column padding

        for (int j = 0; j < colWidth.length; ++j)
            colHdrSpc[j] = Globals.repeat(" ", (colWidth[j] - dataColWidth[j]) / 2);

        // return table width

        return tableWidth;
    }

    /**
     * Returns a multi-line table header as an array of rows.
     *
     * @param hdr      The header pre-pended to the beginning of every output line.
     * @param colHdr   The multi-line table header ([row][column]).
     * @param colWidth The header column widths.
     * @return The multi-line table header.
     */
    public static String[] makeTableHeader(String hdr, String[][] colHdr,
                                           int[] colWidth) {
        String[] tableHdr = null;

        // output column headers if defined

        if (colHdr != null) {
            tableHdr = new String[colHdr.length];
            for (int i = 0; i < colHdr.length; ++i) {
                // if last row of header print rowColHdr in first column followed by
                // last column header row ... otherwise just add spaces

                tableHdr[i] = hdr;

                String[] colHdrRow = colHdr[i];
                for (int j = 0; j < colHdrRow.length; ++j) {
                    tableHdr[i] += Globals.centerString(colHdrRow[j], colWidth[j], true);
                }
                tableHdr[i] += NL;
            }
        }

        return tableHdr;
    }

    /**
     * Produces a table data row given the input data row (dataRow), the column
     * widths (colWidth), the column data header spacing (colHdrSpc), and the
     * column alignment (colAlign).
     *
     * @param hdr       The header pre-pended to the beginning of every output
     *                  line.
     * @param dataRow   The row of data to be output.
     * @param colWidth  The column widths.
     * @param colHdrSpc The column data header spacing
     * @param colAlign  The column alignments.
     * @return The formatted data row.
     */
    public static String getTableDataRow(String hdr, String[] dataRow,
                                         int[] colWidth, String[] colHdrSpc,
                                         TableAlignment[] colAlign) {
        String outputRow = hdr;

        // now print data rows

        for (int j = 0; j < dataRow.length; ++j)
            outputRow += Globals.alignString(dataRow[j], colHdrSpc[j],
                    colWidth[j], colAlign[j]);
        outputRow += NL;

        return outputRow;
    }
}
