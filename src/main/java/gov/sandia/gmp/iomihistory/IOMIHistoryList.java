package gov.sandia.gmp.iomihistory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for tracking the operational history of all IOMIObjects
 * manipulated by a single host. This object is useful in two respects. First,
 * as a debug class so that issues with IOMIObject synchronization in the
 * IOMIManager can be tracked and deciphered. And secondly, as a means for
 * saving object histories in conjunction with LSINVTaskHistory which can
 * be combined to learn many performance pitfalls with how LSINV tasks are
 * assigned.
 * <p>
 * This object is created by the IOMIManager if requested by the application
 * user. The history object periodically writes itself to disk (the path is
 * also defined by the user) and tracks the number of entries written and the
 * data size of the output files.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class IOMIHistoryList extends HistoryList implements Serializable {
    /**
     * Map of all table types associated with their identifying string (see
     * initialize()).
     */
    private HashMap<String, HistoryTable> aTableMap;

    /**
     * Standard constructor.
     *
     * @param pth      The path to where history files are written.
     * @param hostname The name of the host.
     * @throws IOException
     */
    public IOMIHistoryList(String pth, String hostname) throws IOException {
        super(pth, hostname);
    }

    /**
     * Standard constructor. Reads summary from the input file name.
     *
     * @param fn The input file name from which summary information of an existing
     *           IOMIHistoryList is read.
     * @throws IOException
     */
    public IOMIHistoryList(String fn) throws IOException {
        super(fn);
    }

    /**
     * Initializes the table map.
     */
    @Override
    protected void initialize() throws IOException {
        aTableMap = new HashMap<String, HistoryTable>();
        aTableMap.put("ALL", new IOMIHistoryTableOpIndex.IOMIHistoryTableAll(this));
        aTableMap.put("OPERATION", new IOMIHistoryTableOpIndex.IOMIHistoryTableOperation(this));
        aTableMap.put("OBJECT", new IOMIHistoryTableOpIndex.IOMIHistoryTableObject(this));
        aTableMap.put("OPERATIONOBJECT", new IOMIHistoryTableOpIndex.IOMIHistoryTableOperationObject(this));
        aTableMap.put("CURRENT", new IOMIHistoryTableUseData.IOMIHistoryTableCurrent(this));
        aTableMap.put("INSTANCE", new IOMIHistoryTableUseData.IOMIHistoryTableInstance(this));
        aTableMap.put("REFERENCE", new IOMIHistoryTableUseData.IOMIHistoryTableReference(this));
        aTableMap.put("CACHE", new IOMIHistoryTableUseData.IOMIHistoryTableCache(this));
        aTableMap.put("LOAD", new IOMIHistoryTableUseData.IOMIHistoryTableLoad(this));
        aTableMap.put("IOQUEUE", new IOMIHistoryTableUseData.IOMIHistoryTableIOQueue(this));
        aTableMap.put("IOPROCESS", new IOMIHistoryTableUseData.IOMIHistoryTableIOProcess(this));
        aTableMap.put("VMMEMORY", new IOMIHistoryTableUseData.IOMIHistoryTableVMMemory(this));
        aTableMap.put("THREADDELAY", new IOMIHistoryTableUseData.IOMIHistoryTableThreadDelay(this));
    }

    /**
     * Returns the requested table, or null if the input type is not defined.
     *
     * @param type The requested HistoryTable type.
     * @return
     */
    public HistoryTable getOutputTable(String type) {
        return aTableMap.get(type.toUpperCase());
    }

    /**
     * Loads all files found at aInPath into the history container.
     *
     * @throws IOException
     */
    public void loadAll() throws IOException {
        loadAll("Object");
    }

    /**
     * Loads the ith file found at aInPath into the history container.
     *
     * @param i The index of the ith file to load.
     * @throws IOException
     */
    public void loadFile(int i) throws IOException {
        loadFile("Object", i);
    }

    /**
     * Returns the operation start list for all or a subset of operations stored
     * in the history map. If the input history operation (op) and object index
     * (objIndex) are both null then the start index of all operations are
     * returned. If the history operation is null (op==null) but the object
     * index is not, then only the start index of all operation inputs that
     * were defined for the input object index are returned. If the object index
     * is null (objIndex==null) but the history operation is not, then only the
     * start index of all operations that were defined as the entry operation are
     * returned. Finally, if neither the history operation nor the object index
     * is null then only start indices where both the operation and object index
     * are a match are returned in the start index list.
     * <p>
     * Note all lists return the requested entries + those that are of type TIME
     * These are required to track absolute timings in the returned list.
     *
     * @param op       The history operation for which only those entries that
     *                 match shall be included in the returned start index list.
     *                 If null the history operation match is ignored.
     * @param objIndex The object index for which only those entries that match
     *                 shall be included in the returned start index list. If null
     *                 the history operation match is ignored.
     * @return The start index list containing only those entries that
     * met the input criteria (and TIME).
     */
    public ArrayList<int[]> getOperationStartList(HistoryOperation op,
                                                  Integer objIndex) {
        ArrayList<int[]> startList = new ArrayList<int[]>(aHistory.size() / 14);
        HistoryOperation[] ops = IOMIHistoryState.values();

        int i = 0;
        if ((op == null) && (objIndex == null)) {
            // get all entries

            int e = 0;
            while (i < aHistory.size()) {
                byte type = aHistory.get(i);
                int[] v = {e, i};
                startList.add(v);
                i += ops[type].operationLength();
                ++e;
            }
        } else if (op == null) {
            // get only entries that match the input object index

            int e = 0;
            while (i < aHistory.size()) {
                byte type = aHistory.get(i);
                if (ops[type].hasIndex()) {
                    int indx = getInt(i + 1);
                    if (indx == objIndex) {
                        int[] v = {e, i};
                        startList.add(v);
                    }
                } else if (type == IOMIHistoryState.TimeOrdinal) {
                    int[] v = {e, i};
                    startList.add(v);
                }
                i += ops[type].operationLength();
                ++e;
            }
        } else if (objIndex == null) {
            // get only entries that match the input HistoryOperation

            int e = 0;
            while (i < aHistory.size()) {
                byte type = aHistory.get(i);
                if ((type == op.ordinal()) || (type == IOMIHistoryState.TimeOrdinal)) {
                    int[] v = {e, i};
                    startList.add(v);
                }
                i += ops[type].operationLength();
                ++e;
            }
        } else {
            // get only entries that match both the input HistoryOperation and object
            // index

            int e = 0;

            if (ops[op.ordinal()].hasIndex()) {
                while (i < aHistory.size()) {
                    byte type = aHistory.get(i);
                    if (type == op.ordinal()) {
                        int indx = getInt(i + 1);
                        if (indx == objIndex) {
                            int[] v = {e, i};
                            startList.add(v);
                        }
                    } else if (type == IOMIHistoryState.TimeOrdinal) {
                        int[] v = {e, i};
                        startList.add(v);
                    }
                    i += ops[type].operationLength();
                    ++e;
                }
            }
        }

        startList.trimToSize();
        return startList;
    }

    /**
     * Add the input HistoryOperation to the history file.
     *
     * @param histOp The history operation to be input.
     * @param ioIndx The IOMIObject index. Output for most operations with the
     *               exception of TIME, OUTPUTHISTORY, VMMEMORYUSE, and
     *               GCMEMORYUSE.
     * @throws IOException
     */
    public void add(HistoryOperation histOp, int ioindx) throws IOException {
        add(histOp, ioindx, 0L);
    }

    /**
     * Adds the new history operation to the history file. If the operation does
     * not use an IOMIObject index or the extra duration amount (extrAmt) then
     * the inputs are ignored.
     *
     * @param histOp  The history operation to be input.
     * @param ioIndx  The IOMIObject index. Output for most operations with the
     *                exception of TIME, OUTPUTHISTORY, VMMEMORYUSE, and
     *                GCMEMORYUSE.
     * @param extrAmt An extra duration amount that is stored. These can be a
     *                virtual machine used memory amount if the operations are
     *                VMMEMORYUSE, or GCMEMORYUSE, or a thread delay amount if
     *                the operation is THREADDELAY.
     * @throws IOException
     */
    public void add(HistoryOperation histOp, int ioIndx, long extrAmt)
            throws IOException {
        synchronized (aHistory) {
            // reset final output flag and get elapsed time offset

            byte etim = setOperation(histOp);

            // add ioIndx (if required), the calculated delta time, and the
            // extraAmount (if required)

            if (histOp.hasIndex()) putInteger(ioIndx);
            if (histOp.hasExtraLong()) putLong(extrAmt);

            // see if history file needs to be written (is full)

            checkHistoryFileOutput("Object", etim);
        }
    }

    /**
     * Writes the final output file which includes the summary file and the last
     * history data (if aHistory.size() > 0) to disk. This only occurs once. The
     * data is not rewritten with successive calls unless more data is added to
     * the history container (call to add(...)).
     *
     * @throws IOException
     */
    public synchronized void writeFinalFile() throws IOException {
        writeFinalFile("Object");
    }

    /**
     * Returns the time ordinal.
     */
    @Override
    protected byte timeOrdinal() {
        return (byte) IOMIHistoryState.TIME.ordinal();
    }

    /**
     * Returns the output ordinal.
     */
    @Override
    protected byte outputOrdinal() {
        return (byte) IOMIHistoryState.OUTPUTHISTORY.ordinal();
    }
}

