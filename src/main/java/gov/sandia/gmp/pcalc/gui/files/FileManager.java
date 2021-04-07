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
package gov.sandia.gmp.pcalc.gui.files;

import gov.sandia.gmp.pcalc.gui.controller.impl.config.ConfigManager;

import java.io.File;
import java.io.IOException;

public final class FileManager {

    /**
     * The directory to the PCalc binary.  This file path
     * will always end with the current value for {@link File#separator}.
     * There is the possibility that the required environment variable is
     * not found.  To check if this is the case, use {@link #environmentIsValid()}.
     * If this is false, then the value for this field is not valid and must not be used.
     */
    public static final String PCALC_DIR;
    private static final String DIR_ENV_VAR = "PCALCDIR";
    private static final String RESOURCE_DIR = "resources";
    private static final String CONFIG_NAME_PREFIX = "config_";
    private static boolean validEnvironment = false;


    static {
        String tmp = System.getenv(DIR_ENV_VAR);
        if (tmp == null) {
            validEnvironment = false;
            PCALC_DIR = "";
        } else {
            validEnvironment = true;
            if (!tmp.endsWith(File.separator)) {
                tmp = tmp + File.separator;
            }
            PCALC_DIR = new String(tmp);
        }
    }

    private FileManager() {
    }

    /**
     * Gets the config file for the current user, as determined by the
     * system property {@code user.name}.
     * If no config file is found, then {@code null} is returned.
     * If the environment variable for finding the PCalc binary is
     * not found (as determined by {@link #environmentIsValid()}) then
     * {@code null} is returned.
     *
     * @return A File object representing the config file for the given user or {@code null}.
     */
    public static File getConfigForCurrentUser() {
        return getConfigForUser(System.getProperty(ConfigManager.USER_PROP));
    }

    /**
     * Attemps to find the Gui config file for the given user.
     * If no config file is found, then {@code null} is returned.
     * If the environment variable for finding the PCalc binary is
     * not found (as determined by {@link #environmentIsValid()}) then
     * {@code null} is returned.  If the config file for the user doesn't exist,
     * then {@code null} is returned.  If the resource directory doesn't exist, and
     * couldn't be created, then {@code null} is returned.  {@link FileManager#createConfigForUser()}.
     *
     * @param user - The username for the user to get the config file for.
     * @return A File object representing the config file for the given user or {@code null}.
     * @throws NullPointerException If the username is {@code null}.
     */
    public static File getConfigForUser(String user) {
        if (!environmentIsValid()) {
            return null;
        }
        if (user == null) {
            throw new NullPointerException("Null username.");
        }

        File rDir;
        try {
            rDir = getAndCreateResourceDir();
        } catch (IOException e) {
            return null;
        }
        File ret = new File(rDir.getAbsolutePath() + CONFIG_NAME_PREFIX + user);
        if (ret.exists())
            return ret;
        return null;
    }

    /**
     * Attempts to create a config file for the current user.  If the file is created, then it is also returned.
     * If the location of the PCalc directory cannot be determined (as by {@link #validEnvironment})
     * then {@code null} is returned.
     *
     * @return The created config file for the current user.
     * @throws IOException If the resources directory doesn't exist, and could not be created.
     *                     If the config file could not be created.
     */
    public static File createConfigForCurrentUser() throws IOException {
        return createConfigForUser(System.getProperty(ConfigManager.USER_PROP));
    }

    /**
     * Attempts to create a config file for the given user.  If the file is created, then it is also returned.
     * If the location of the PCalc directory cannot be determined (as by {@link #validEnvironment})
     * then {@code null} is returned.
     *
     * @param user - The username of the user to create the file for.
     * @return The created config file for the given user.
     * @throws IOException If the resources directory doesn't exist, and could not be created.
     *                     If the config file could not be created.
     */
    public static File createConfigForUser(String user) throws IOException {
        if (!environmentIsValid()) {
            return null;
        }
        if (user == null) {
            throw new NullPointerException("Null username.");
        }
        getAndCreateResourceDir();
        File f = new File(PCALC_DIR + RESOURCE_DIR + File.separator + CONFIG_NAME_PREFIX + user);
        if (f.exists()) {
            return f;
        }
        if (!f.createNewFile()) {
            throw new IOException("Could not create config file for user: " + user);
        }
        return f;
    }

    private static File getAndCreateResourceDir() throws IOException {
        File rDir = new File(PCALC_DIR + RESOURCE_DIR);
        if (!rDir.exists() || !rDir.isDirectory()) {
            if (!rDir.mkdir()) {
                throw new IOException(RESOURCE_DIR + "could not be created.");
            }
        }
        return rDir;
    }

    /**
     * Returns whether or not a valid environment variable to indicate the location of the PCalc binary
     * was found.  If the return value is {@code false} then the value of {@link #PCALC_DIR} must not be used.
     *
     * @return {@code true} if the PCalc binary location could be determined, otherwise {@code false}.
     */
    public static boolean environmentIsValid() {
        return validEnvironment;
    }
}