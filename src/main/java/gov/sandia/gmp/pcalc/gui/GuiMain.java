/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *  BSD Open Source License.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *  4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc.gui;

import gov.sandia.gmp.pcalc.PCalc;
import gov.sandia.gmp.pcalc.gui.controller.impl.PCalcGuiController;

import java.io.IOException;
import java.text.ParseException;

/**
 * A Single main class for both PCalc and the Gui.
 * By default PCalc will show when using this main, but
 * with the command line argument of "--gui" main will
 * call PCalcGui's main instead.
 */
public final class GuiMain {

    private GuiMain() {
    }

    private static final int MAX_ARGS = 2;

    //Flags.
    public static final String GUI_FLAG = "--gui";
    private static final String HELP_FLAG = "--help";
    private static final String VERSION_FLAG = "--version";

    private static String filePath = null;
    private static boolean guiMode = false;
    private static boolean printHelp = false;

    /**
     * Begins the PCalc program by calling {@link #begin(String[])}.
     * If {@code begin} throws and exception, the usage information will be printed out and the
     * program will exit with error code 1.
     *
     * @param args - The command line arguments to PCalc.  See {@link #begin(String[])}.
     */
    public static void main(String[] args) {
        try {
            begin(args);
        } catch (RuntimeException e) {
            e.printStackTrace();
            usage();
            System.exit(1);
        }
    }

    /**
     * Parses the command line arguments and runs either the PCalcGui or just the plain PCalc.
     * These are the valid argument schemes ({@literal [<config file>] [--gui]}):
     * <ol>
     * <li>1 Argument: {@literal <config file>} - PCalc will be ran with this argument as the configuration
     *  file for PCalc.</li>
     * <li>1 Argument: "--gui" - The PCalcGui will be ran with default settings.</li>
     * <li>1 Argument: "--version" - PCalc will print the version and exit immediately.
     * <li>2 Arguments: {@literal <config file>} "--gui" - The PCalcGui will be ran with the configuration from the
     * config file.  No order is necessary for these arguments.</li>
     * <li>1 Argument: "--help" - The program will print out usage information and exit.  All other
     * arguments are ignored.</li>
     * </ol>
     *
     * @param args - Command line arguments.
     * @throws RuntimeException If the command line arguments do not match a scheme above, or there are too many.
     */
    public static void begin(String[] args) throws RuntimeException {
        //Check the number of arguments.
        if (args.length > MAX_ARGS) {
            throw new IllegalArgumentException("Too many arguments.  Max is: " + MAX_ARGS + " and was given:"
                    + args.length);
        }
        try {
            parseCommandLine(args);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        if (printHelp) {
            usage();
            return;
        }
        runPCalc(guiMode, filePath);
    }

    private static void parseCommandLine(String[] args) throws ParseException {
        //Parse arguments, make sure no argument is present more than once.
        int pos = 0;
        for (String s : args) {
            if (s.equals(VERSION_FLAG)) {
                System.out.println(PCalc.getVersion());
                System.exit(0);
            }
            if (s.equals(GUI_FLAG)) {
                if (guiMode) {
                    throw new ParseException("Too many " + GUI_FLAG + " flags.", pos);
                }
                guiMode = true;
            } else if (s.equals(HELP_FLAG)) {
                printHelp = true;
                return;
            } else {
                if (filePath != null) {
                    throw new ParseException("Too many file paths.", pos);
                }
                filePath = s;
            }
            pos++;
        }
    }

    private static void runPCalc(boolean hasGui, String pcalcPath) {
        if (!hasGui) {
            runBatch(pcalcPath);
        } else {
            if (pcalcPath == null) {
                new PCalcGuiController();
                return;
            }
            new PCalcGuiController(pcalcPath);
            return;
        }
    }

    private static void runBatch(String path) {
        if (path == null) {
            System.err.println("Cannot use normal mode without a file path argument specified.");
            System.err.flush();
            usage();
            return;
        }
        String[] newArgs = new String[1];
        newArgs[0] = path;
        try {
            PCalc.main(newArgs);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    private static final void usage() {
        System.out.println("PCalc (with GUI) " +
                "usage:\n\"" + HELP_FLAG + "\" will print this message." +
                "\n0 arguments:  Error.  PCalc requires at least 1 arguement, either a config file, or the " + GUI_FLAG + " flag.\n1 argument:   " +
                "If " + GUI_FLAG + " then the Gui is displayed with no loaded file. " +
                "If " + VERSION_FLAG + " then the version of PCalc is printed to stdout and this program will exit immediately with exit code 0." +
                "  Otherwise pcalc is run with the argument as the path to the config file.\n2 arguments:  " +
                "One argument must be \"" + GUI_FLAG + "\" and the second is a path to a file.  There is no particular order" +
                " required.\n" +
                ">" + MAX_ARGS + " will result in an error.\n" +
                "Supplying \"" + GUI_FLAG + "\" twice or a file path twice will also result in an error.");
        System.out.flush();
    }

}
