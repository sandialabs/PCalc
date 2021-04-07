/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *	BSD Open Source License.
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *
 *	1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *	2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *	3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *	4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc.gui.controller.impl.process;

import gov.sandia.gmp.pcalc.gui.controller.impl.PCalcGuiController;
import gov.sandia.gmp.pcalc.gui.files.FileManager;

import java.io.*;

/**
 * PCalcRunner is a class that handles the creation and management of a Process
 * in which a new instance of PCalc is ran.  When started, this class will create
 * a temporary config file in the current working directory, and start
 * the process with PCalc set to load that.  Upon termination of this process,
 * either by natural means, or termination, the temporary file is then deleted.
 */
public class PCalcProcess {

    private static final String BASE_FILE_NAME = "pcalc-tmp-input";
    private static final String MY_LOC;
    private static final String PCALC_NAME = "pcalc";
    private static final String SUBDIR = "bin";

    static {
        if (FileManager.environmentIsValid()) {
            MY_LOC = FileManager.PCALC_DIR + SUBDIR + File.separatorChar;
        } else {
            MY_LOC = null;
        }
    }

    private final PCalcProcess thiz = this;
    private final String contents;
    private final ProcessDelegate rd;
    private final String uniqueFileLoc;
    private final ProcessDelegate myDel;

    private Thread th;
    private Process p;
    private volatile boolean isRunning;
    private OutputStream pin;
    private InputStream pout;
    private InputStream perr;

    private class Runner implements Runnable {

        @Override
        public void run() {
            //Run this jar file with the file path to the config file in non gui mode.
            //The command will look like:  /bin/pcalc <config file name>
            String cmd = MY_LOC + PCALC_NAME + " " + uniqueFileLoc;
            try {
                p = Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
                delete(uniqueFileLoc);
                return;
            }
            myDel.pcalcStarted(thiz);
            int exit = 0;
            try {
                exit = p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                myDel.pcalcExited(thiz, 1);
                return;
            }
            myDel.pcalcExited(thiz, exit);
        }

    }

    /**
     * Creates a new PCalcRunner, which can later run and manage a Process that
     * runs a new instance of PCalc.
     *
     * @param contents - The contents of the config file to use when starting the new PCalc Process.
     * @param rd       - The delegate for this runner.
     * @throws IllegalArgumentException If the system environment does not specify the variable to
     *                                  find the location of the directory that PCalc is run from.
     */
    public PCalcProcess(String contents, ProcessDelegate rd) {
        if (!FileManager.environmentIsValid()) {
            throw new IllegalStateException("No value for environment variable for the PCalc binary thus" +
                    " cannot find out how to run another version of self.");
        }
        this.rd = rd;
        this.contents = contents;
        uniqueFileLoc = FileManager.PCALC_DIR + BASE_FILE_NAME + System.nanoTime() + "." + PCalcGuiController.EXTENSION;
        myDel = new ProcessDelegate() {

            @Override
            public void pcalcExited(PCalcProcess sender, int exit) {
                exited(sender, exit);
            }

            @Override
            public void pcalcStarted(PCalcProcess sender) {
                started(sender);
            }
        };
    }

    private void exited(PCalcProcess sender, int exit) {
        isRunning = false;
        delete(uniqueFileLoc);
        rd.pcalcExited(sender, exit);
    }

    private void started(PCalcProcess sender) {
        perr = p.getErrorStream();
        pout = p.getInputStream();
        pin = p.getOutputStream();
        isRunning = true;
        rd.pcalcStarted(sender);
    }

    private static void delete(String path) {
        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            return;
        }
        f.delete();
    }

    /**
     * Returns the absolute file name for the file that the newly spawned PCalc Process will use
     * as its configuration file.
     *
     * @return - The config file for the new process.
     */
    public String getFileAbsoluteName() {
        return uniqueFileLoc;
    }

    /**
     * Attempts to start a process that will run a PCalc calculation.
     * This will create the temporary config file in the same
     * directory this program was run in.  Once the process ends,
     * the temporary file is deleted.  The file name for the temporary
     * file is {@literal "pcalc-tmp-input<System.nanoTime()>.<EXTENSION>"}.
     * If a file with this name already exists, it is then replaced by
     * a new one.
     *
     * @throws IOException If the file for the configuration could not be saved.
     */
    public void start() throws IOException {
        //Save the properties object to a temporary file.
        BufferedOutputStream bos;
        File f = new File(uniqueFileLoc);
        if (f.exists()) {
            f.delete();
            f.createNewFile();
        }
        bos = new BufferedOutputStream(new FileOutputStream(f));
        bos.write(contents.getBytes());
        bos.close();
        //Start a new Thread to start the new Process (and wait on it).
        th = new Thread(new Runner(), "PCalc spawner.");
        th.start();
    }

    /**
     * Terminates the running PCalc process asynchronously.
     * The IO streams of the process will be held, so calls to {@link #getErrorStream()}, {@link #getInputStream()},
     * and {@link #getOutputStream()} will still return the streams of that destroyed process, until another process
     * is started.
     */
    public void terminate() {
        //Kill the process
        if (p == null)
            return;
        p.destroy();
        th = null;
        delete(uniqueFileLoc);
    }

    /**
     * @return If a Process managed by this PCalcRunner is currently alive.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns the Output stream as defined by {@link Process#getOutputStream()}.  If a process
     * has been started, and then subsequently finished either by a call to {@link #terminate()}
     * or on its own, then the Output stream of that process will be returned by this method
     * until another process is started.
     *
     * @return The OutputStream of the currently running process, the OutputStream of the last run process, or {@code null}.
     */
    public OutputStream getOutputStream() {
        return pin;
    }

    /**
     * Returns the Input stream as defined by {@link Process#getInputStream()}.  If a process
     * has been started, and then subsequently finished either by a call to {@link #terminate()}
     * or on its own, then the Input stream of that process will be returned by this method
     * until another process is started.
     *
     * @return The InputStream of the currently running process, the InputStream of the last run process, or {@code null}.
     */
    public InputStream getInputStream() {
        return pout;
    }

    /**
     * Returns the Error stream as defined by {@link Process#getErrorStream()}.  If a process
     * has been started, and then subsequently finished either by a call to {@link #terminate()}
     * or on its own, then the Error stream of that process will be returned by this method
     * until another process is started.
     *
     * @return The ErrorStream of the currently running process, the ErrorStream of the last run process, or {@code null}.
     */
    public InputStream getErrorStream() {
        return perr;
    }

}
