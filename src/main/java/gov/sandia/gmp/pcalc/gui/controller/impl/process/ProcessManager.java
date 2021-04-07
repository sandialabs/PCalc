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

import gov.sandia.gmp.pcalc.gui.gui.impl_gui.PCalcViewer;
import gov.sandia.gmp.pcalc.gui.gui.impl_gui.ViewerDelegate;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * ProcessManager will manage multiple running instances of a PCalc process in
 * separate windows, each can be closed individually at a time.
 */
public class ProcessManager {

    private static final List<Delegate> dels;
    private static int instanceNum = 0;
    private static final String DATE_FORMAT_NOW = "HH:mm:ss";
    private static boolean exit = false;

    static {
        dels = new LinkedList<Delegate>();
    }

    private class Delegate implements ProcessDelegate, ViewerDelegate {

        public PCalcProcess pcp;
        public PCalcViewer pv;
        public int instance;

        @Override
        public void cancelPressed() {
            if (pcp.isRunning()) {
                pcp.terminate();
                pv.setActive(false);
            }
        }

        @Override
        public void donePressed() {
            pv.setVisible(false);
            dels.remove(this);
            if (exit && getNumberActive() == 0) {
                System.exit(0);
            }
        }

        @Override
        public void pcalcStarted(PCalcProcess sender) {
            pv.setVisible(true);
            pv.setActive(true);
            new Thread(new StreamRedirector(pcp.getInputStream(), pv)).start();
            new Thread(new StreamRedirector(pcp.getErrorStream(), pv)).start();
        }

        @Override
        public void pcalcExited(PCalcProcess sender, int exitStatus) {
            pv.setActive(false);
        }

        @Override
        public void windowClosing() {
            if (pcp.isRunning()) {
                if (pv.promptUser("Are you sure you want to close" +
                        " and terminate the process running in this window? (PCalcProcess #" + instance
                        + ")") != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            pcp.terminate();
            donePressed();
        }

    }

    /**
     * Creates a new PCalc Process that will run with the configuration info found
     * in the <i>content</i> string.  The new Process will be managed by this
     * ProcessManager, until it terminates.
     *
     * @param content - The info for the new PCalc Process to use as a configuration file.
     */
    public void createNewPCalcProcess(String content, String currentConfig) {
        Delegate d = new Delegate();
        d.instance = ++instanceNum;
        PCalcViewer pv = new PCalcViewer(d);
        d.pv = pv;
        PCalcProcess pcp = new PCalcProcess(content, d);
        d.pcp = pcp;
        setTitle(d, currentConfig);
        try {
            pcp.start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        dels.add(d);
    }

    private void setTitle(Delegate d, String currentConfig) {
        if (currentConfig == null) {
            currentConfig = "<no file>";
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String title = "PCalc Process #" + d.instance + ": " +
                currentConfig + " started " + sdf.format(cal.getTime());
        d.pv.setTitle(title);
    }

    /**
     * Terminates all running Processes managed by this ProcessManager.
     * Useful for when the application is about to terminate itself.<br /><br />
     * <p>
     * If this ProcessManager is set to exit when all running processes are
     * terminated, then after the processes are terminated the method will call
     * {@code System.exit(0)}.
     */
    public void terminateAll() {
        for (Delegate d : dels) {
            if (d.pcp.isRunning()) {
                d.pcp.terminate();
            }
        }
        dels.clear();
        if (exit) {
            System.exit(0);
        }
    }

    /**
     * Returns the number of currently running PCalcProcesses.
     *
     * @return the number of currently running PCalcProcesses.
     */
    public int getNumberActive() {
        return dels.size();
    }

    /**
     * Tells the process manager if it should call {@code System.exit(0)} when the number of
     * running processes reaches 0.  This is used when the gui is told to exit, but doesn't
     * want to call {@code System.exit()} itself to keep the windows for each process open.  The Process Manager
     * will then exit once all windows are closed.<br /><br />
     * <p>
     * If this method is called with the value {@code true} when there are no running processes,
     * then {@code System.exit(0)} will be called immediately.  See {@link ProcessManager#getNumberActive()}.
     *
     * @param b Whether or not the ProcessManager should call {System.exit(0)} when there are no more running
     *          processes.  Default value is {@code false}.
     */
    public void setExitWhenDone(boolean b) {
        exit = b;
        if (exit && (getNumberActive() == 0)) {
            System.exit(0);
        }
    }

}
