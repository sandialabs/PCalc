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

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

import gov.sandia.gmp.util.containers.arraylist.ArrayListByte;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;

/**
 * A class for tracking the operational history of all IOMIObjects. This object
 * is useful in two respects. First, as a debug class so that issues with
 * IOMIObject synchronization in the IOMIManager can be tracked and deciphered.
 * And secondly, as a means for saving object histories in conjunction with
 * derived types from this abstract base class (e.g. LSINVHistoryList) which
 * can be combined to learn many performance pitfalls with how LSINV tasks are
 * assigned.
 * <p>
 * This is an abstract base class for IOMIHistoryList so that much of its code
 * can be reused by consumers of IOMI (e.g. LSINVHistoryList).
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class HistoryList implements Serializable {
    /**
     * Maximum storage size before writing a file to output.
     */
    protected static final int aStoreSize = 4194304;

    /**
     * Total byte range (255).
     */
    private static final int aByteRange = Byte.MAX_VALUE - Byte.MIN_VALUE;

    /**
     * The host name for which this history object was constructed.
     */
    private String aHostName = "";

    /**
     * The path to which the object dumps it's data.
     */
    private String aOutPath = "";

    /**
     * New input path to read from.
     */
    private String aInPath = "";

    /**
     * An 8 byte buffer to convert long times to bytes
     */
    private ByteBuffer aLongBuf = ByteBuffer.allocate(8);

    /**
     * A 2 byte buffer to convert short elapsed times in seconds to bytes.
     */
    private ByteBuffer aIntegerBuf = ByteBuffer.allocate(4);

    /**
     * The time when this history object was constructed.
     */
    private long aStartTime = -1;

    /**
     * The time when this history object was constructed.
     */
    private long aEndTime = -1;

    /**
     * The last update time.
     */
    private long aLastUpdate = -1;

    /**
     * Total number of task inputs written to this history file.
     */
    protected int aOpInputs = -1;

    /**
     * True if output is finalized.
     */
    protected boolean aOutputFinal = false;

    /**
     * The history list.
     */
    protected ArrayListByte aHistory = null;

    /**
     * The number of output files written to disk.
     */
    private int aOutFileCount = 0;

    /**
     * The number of bytes written to disk.
     */
    private long aOutFileSize = 0;

    /**
     * Standard constructor.
     *
     * @param pth      The path where history files are to be written.
     * @param hostname The host name associated with the history files.
     * @throws IOException
     */
    public HistoryList(String pth, String hostname) throws IOException {
        // assign inputs and initialize

        aHostName = hostname;
        aOutPath = pth;
        aStartTime = (new Date()).getTime();
        aOpInputs = 0;
        File f = new File(aOutPath);
        if (!f.exists()) {
            String s = "Error: History list output path: \"" + pth +
                    "\"" + NL +
                    "       Does not exist for host: " + hostname + " ...";
            throw new IOException(s);
        }

        // create storage and initialize tables

        aHistory = new ArrayListByte(aStoreSize);
        aLongBuf.position(0);
        aLongBuf.mark();
        aIntegerBuf.position(0);
        aIntegerBuf.mark();
        initialize();
    }

    /**
     * Read an existing history file at file name fn.
     *
     * @param fn The history summary file to be read.
     * @throws IOException
     */
    public HistoryList(String fn) throws IOException {
        // load summary and assign input path to output path

        loadSummary(fn);
        aInPath = aOutPath;

        // output summary information

        System.out.println("   History List  Input:");
        System.out.println("     Host Name             = " + aHostName);
        System.out.println("     Original Output Path  = \"" + aOutPath + "\"");
        System.out.println("     Start Time            = " +
                Globals.getTimeStamp(aStartTime));
        System.out.println("     End Time              = " +
                Globals.getTimeStamp(aEndTime));
        System.out.println("     Elapsed Time          = " +
                Globals.elapsedTimeString3(aStartTime, aEndTime));
        System.out.println("     Operation Inputs      = " + aOpInputs);
        System.out.println("     Partial Files Written = " + aOutFileCount);
        System.out.println("     Total File Bytes      = " + aOutFileSize);
        System.out.println("");
        System.out.println("");

        // create storage and initialize tables

        aHistory = new ArrayListByte(aStoreSize);
        aLongBuf.position(0);
        aLongBuf.mark();
        aIntegerBuf.position(0);
        aIntegerBuf.mark();
        initialize();
    }

    /**
     * Returns the History container (a byte list).
     *
     * @return The History container (a byte list).
     */
    public ArrayListByte getHistory() {
        return aHistory;
    }

    /**
     * Returns the host name.
     *
     * @return The host name.
     */
    public String getHostName() {
        return aHostName;
    }

    /**
     * Returns the start time.
     *
     * @return The start time.
     */
    public long getStartTime() {
        return aStartTime;
    }

    /**
     * Standard toString function. Returns summary information.
     *
     * @return Summary information.
     */
    @Override
    public String toString() {
        return toString("  ");
    }

    /**
     * Returns the summary information of this history object. The output
     * functions must be used to access the history data.
     *
     * @param hdr Header pre-pended to every output line.
     * @return The summary string.
     */
    public String toString(String hdr) {
        String s = "";
        s += hdr + "   History List  Input:" + NL;
        s += hdr + "     Host Name             = " + aHostName + NL;
        s += hdr + "     Original Output Path  = \"" + aOutPath + "\"" + NL;
        s += hdr + "     Start Time            = " +
                Globals.getTimeStamp(aStartTime) + NL;
        s += hdr + "     End Time              = " +
                Globals.getTimeStamp(aEndTime) + NL;
        s += hdr + "     Elapsed Time          = " +
                Globals.elapsedTimeString3(aStartTime, aEndTime) + NL;
        s += hdr + "     Operation Inputs      = " + aOpInputs + NL;
        s += hdr + "     Partial Files Written = " + aOutFileCount + NL;
        s += hdr + "     Total File Bytes      = " + aOutFileSize + NL + NL;
        return s;
    }

    /**
     * Read a history summary file from disk.
     *
     * @param fn The history summary file to be loaded.
     * @throws IOException
     */
    public void loadSummary(String fn) throws IOException {
        FileInputBuffer fib = new FileInputBuffer(fn);
        aHostName = fib.readString();
        aOutPath = fib.readString();
        aStartTime = fib.readLong();
        aEndTime = fib.readLong();
        aOpInputs = fib.readInt();
        aOutFileCount = fib.readInt();
        aOutFileSize = fib.readLong();
        fib.close();
    }

    /**
     * Loads all files found at aInPath into the history container.
     *
     * @param fnExt The file name extension appended to beginning of all written
     *              and read filenames ... [aHostName]_[fnExt]History_[i]
     * @throws IOException
     */
    public void loadAll(String fnExt) throws IOException {
        aHistory.clear();
        aHistory.ensureCapacity((int) aOutFileSize);
        for (int i = 0; i < aOutFileCount; ++i) loadOneFile(fnExt, i);
    }

    /**
     * Loads the ith file found at aInPath into the history container.
     *
     * @param fnExt The file name extension appended to beginning of all written
     *              and read filenames ... [aHostName]_[fnExt]History_[i]
     * @param i     The index of the ith file to load.
     * @throws IOException
     */
    public void loadFile(String fnExt, int i) throws IOException {
        aHistory.clear();
        loadOneFile(fnExt, i);
    }

    /**
     * Appends the contents of the ith history file found at aInPath into the
     * history container. The container is not cleared by this function.
     *
     * @param fnExt The file name extension appended to beginning of all written
     *              and read filenames ... [aHostName]_[fnExt]History_[i]
     * @param i     The index of the ith file to append into aHistory.
     * @throws IOException
     */
    private void loadOneFile(String fnExt, int i) throws IOException {
        // open buffer

        String fp = aInPath + File.separator + aHostName +
                "_" + fnExt + "History_" + i;
        FileInputBuffer fib = new FileInputBuffer(fp);

        // read data and close buffer

        byte[] data = fib.readBytes();
        fib.close();

        // populate history

        for (int k = 0; k < data.length; ++k) aHistory.add(data[k]);
    }

    /**
     * Sets a new input file path.
     *
     * @param fn The new input file path.
     */
    public void setInputFilePath(String fn) {
        aInPath = fn;
    }

    /**
     * Appends the input long L into the history file
     *
     * @param L The long to be appended to the history file.
     */
    protected void putLong(Long L) {
        aLongBuf.putLong(L);
        aLongBuf.reset();
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aHistory.add(aLongBuf.get());
        aLongBuf.reset();
    }

    /**
     * Appends the input integer I into the history file
     *
     * @param I The integer to be appended to the history file.
     */
    protected void putInteger(int I) {
        aIntegerBuf.putInt(I);
        aIntegerBuf.reset();
        aHistory.add(aIntegerBuf.get());
        aHistory.add(aIntegerBuf.get());
        aHistory.add(aIntegerBuf.get());
        aHistory.add(aIntegerBuf.get());
        aIntegerBuf.reset();
    }

    /**
     * Returns an integer from the history file beginning at location i.
     *
     * @param i The location in the history file from which an integer is read.
     * @return The integer stored in the history file beginning at location i.
     */
    protected int getInt(int i) {
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

    protected byte setOperation(HistoryOperation histOp) {
        // reset final output flag and get elapsed time offset

        aOutputFinal = false;
        byte etim = getElapsedTime();

        // increment the input count and add the input operation and calculated
        // delta time

        ++aOpInputs;
        byte type = (byte) histOp.ordinal();
        aHistory.add(type);
        aHistory.add(etim);

        return etim;
    }

    /**
     * Outputs a file write operator to the history file and writes the file if
     * the size exceeds the limit setting.
     *
     * @param fnExt The output file name extension prefix.
     * @param etim  The time offset (from the last TIME operator) in seconds.
     * @throws IOException
     */
    protected void checkHistoryFileOutput(String fnExt, byte etim)
            throws IOException {
        if (aHistory.size() > aStoreSize - 1024) outputNextHistoryFile(fnExt, etim);
    }

    /**
     * Returns the elapsed time since the last update. If the elapsed time exceeds
     * 255 seconds then a new TIME operation with the current time is added to
     * the history container and the elapsed time of zero is returned. If this is
     * the first entry into the container a TIME operation is automatically
     * inserted.
     *
     * @return The elapsed time since the last update.
     */
    protected byte getElapsedTime() {
        // get the current time and elapsed time

        long tm = (new Date()).getTime();
        long etim = (tm - aLastUpdate) / 1000;

        // if time offset exceeds byte range then add a new TIME entry ...
        // scale elapsed time by Byte minimum to make ready for input

        if ((etim > aByteRange) || (aHistory.size() == 0)) {
            // increment input count, add operation, set new update time to current
            // time, add the time to history, and reset elapsed time to 0

            ++aOpInputs;
            aHistory.add(timeOrdinal());
            aLastUpdate = tm;
            putLong(aLastUpdate);
            etim = 0;
        }
        etim += Byte.MIN_VALUE;

        return (byte) etim;
    }

    /**
     * Writes the final output file which includes the summary file and the last
     * history data (if aHistory.size() > 0) to disk. This only occurs once. The
     * data is not rewritten with successive calls unless more data is added to
     * the history container (call to add(...)).
     *
     * @param fnExt The file name extension appended to beginning of all
     *              written and read filenames ...
     *              [aHostName]_[fnExt]History_[i]
     * @throws IOException
     */
    protected synchronized void writeFinalFile(String fnExt)
            throws IOException {
        if (!aOutputFinal) {
            aOutputFinal = true;
            if (aHistory.size() > 0) {
                byte etim = getElapsedTime();
                outputNextHistoryFile(fnExt, etim);
            }
            writeSummary(fnExt);
        }
    }

    /**
     * Closes out this history object by writing the summary and last data file
     * (if not already written).
     *
     * @param fnExt The file name extension appended to beginning of all written
     *              and read filenames ... [aHostName]_[fnExt]History_[i]
     * @throws IOException
     */
    private void writeSummary(String fnExt) throws IOException {
        if (aEndTime == -1) aEndTime = (new Date()).getTime();
        String fp = aOutPath + File.separator + aHostName +
                "_" + fnExt + "History_Summary";
        FileOutputBuffer fob = new FileOutputBuffer(fp);
        fob.writeString(aHostName);
        fob.writeString(aOutPath);
        fob.writeLong(aStartTime);
        fob.writeLong(aEndTime);
        fob.writeInt(aOpInputs);
        fob.writeInt(aOutFileCount);
        fob.writeLong(aOutFileSize);
        fob.close();
    }

    /**
     * Adds the output history operation to the history file and dumps the next
     * set of data from aHistory to disk.
     *
     * @param fnExt The file name extension appended to beginning of all
     *              written and read filenames ...
     *              [aHostName]_[fnExt]History_[i]
     * @param etim  The time at which the output state is written to file.
     * @throws IOException
     */
    protected void outputNextHistoryFile(String fnExt, byte etim)
            throws IOException {
        ++aOpInputs;
        aHistory.add(outputOrdinal());
        aHistory.add(etim);
        writeNextFile(fnExt);
    }

    /**
     * Dumps the next set of data from aHistory to disk
     *
     * @param fnExt The file name extension appended to beginning of all written
     *              and read filenames ... [aHostName]_[fnExt]History_[i]
     * @throws IOException
     */
    private void writeNextFile(String fnExt) throws IOException {
        String fp = aOutPath + File.separator + aHostName +
                "_" + fnExt + "History_" + aOutFileCount++;
        FileOutputBuffer fob = new FileOutputBuffer(fp);
        fob.writeBytes(aHistory.getArray(), 0, aHistory.size());
        fob.close();

        aOutFileSize += aHistory.size();
        aHistory.clear();
    }

    /**
     * Abstract initialization function to further initialized derived classes.
     */
    protected abstract void initialize() throws IOException;

    /**
     * Returns the time ordinal.
     *
     * @return The time ordinal.
     */
    protected abstract byte timeOrdinal();

    /**
     * Returns the output ordinal.
     *
     * @return The output ordinal.
     */
    protected abstract byte outputOrdinal();
}
