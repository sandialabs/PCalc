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
package gov.sandia.gmp.pcalc.gui.controller.impl.config;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.files.FileManager;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A ConfigManager manages the saving and loading of configuration for users.
 */
public class ConfigManager {

    /**
     * Property String to get the current user's username.
     * To be used as such: <br /><br />
     * System.getProperty(ConfigManager.USER_PROP)
     */
    public static final String USER_PROP = "user.name";
    private static final int NUM_CONFIGS = 10;

    private final String username;
    private final Map<String, LinkedList<String>> configs;

    /**
     * Creates an instance of ConfigManager that will manage the configuration info for a given user.
     *
     * @param username - The user to manage config info for.
     */
    public ConfigManager(String username) {
        if (username == null) {
            throw new NullPointerException("Can't configure with a null username!");
        }
        this.username = username;
        configs = new HashMap<String, LinkedList<String>>();
    }

    /**
     * Loads the configuration information for the user for this ConfigManager instance.
     */
    public void loadConfigFile() {
        if (!FileManager.environmentIsValid())
            return;
        File f = null;
        f = FileManager.getConfigForUser(username);
        if (f == null) {
            return;
        }
        populateConfigInfo(f);
    }

    private void populateConfigInfo(File f) {
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            return;
        }
        String line = null;
        String prevName = null;
        List<String> currList = null;
        try {
            while ((line = bf.readLine()) != null) {
                //If it starts with a tab, then its a content line
                if (line.startsWith("\t")) {
                    //Cut out the tab and add it to the list.
                    currList.add(line.substring(1));
                    //If it doesn't start with a tab, then its a name line
                } else {
                    //Put the previous set of info into the map, since it has now ended.
                    if (prevName != null) {
                        configs.put(prevName, new LinkedList<String>(currList));
                    }
                    //reset the name and list.
                    prevName = line;
                    currList = new LinkedList<String>();
                }
            }
            if (prevName != null) {
                configs.put(prevName, new LinkedList<String>(currList));
            }
            bf.close();
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Saves the configuration info for the user for this ConfigManager.
     */
    public void saveConfigInfo() {
        if (!FileManager.environmentIsValid())
            return;
        File f = null;
        try {
            f = FileManager.getConfigForUser(username);
            if (f == null) {
                f = FileManager.createConfigForUser(username);
            }
        } catch (IOException e) {
            return;
        }
        writeConfigInfo(f);
    }

    private void writeConfigInfo(File f) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
        } catch (IOException e) {
            return;
        }
        try {
            for (Map.Entry<String, LinkedList<String>> m : configs.entrySet()) {
                //No use in saving something that has nothing to save.
                if (m.getValue().size() == 0)
                    continue;
                bw.write(m.getKey() + '\n');
                for (String s : m.getValue()) {
                    bw.write('\t' + s + '\n');
                }
            }
            bw.close();
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Gets the configuration information for the given Field relative to the user for this ConfigManager.
     * The information is represented in the list as each element of the list being
     * one line of the configuration info.
     *
     * @param f - The Field of which to get the configuration information for.
     * @return A list of the configuration information in lines.
     */
    public ImmutableList<String> getConfigForField(Field f) {
        LinkedList<String> ret = configs.get(f.getName());
        if (ret == null) {
            ret = new LinkedList<String>();
            configs.put(f.getName(), ret);
        }
        return new ImmutableList<String>(ret);
    }

    /**
     * Adds a line to the configuration information for the
     * given Field relative to the user for this CongifManager.
     * If the number of lines already in the config info
     *
     * @param f
     * @param line
     */
    public void addConfigForField(Field f, String line) {
        String l = line.trim();
        if (l.equals(""))
            return;
        LinkedList<String> list = configs.get(f.getName());
        if (list == null) {
            list = new LinkedList<String>();
        }
        configs.put(f.getName(), list.contains(l) ?
                reshuffle(list, l) :
                rotateIn(list, l));

    }

    private LinkedList<String> rotateIn(LinkedList<String> list, String l) {
        if (list.size() >= NUM_CONFIGS) {
            //If the max number of config items is currently set, then
            //just get rid of the last one.
            list.remove(list.size() - 1);
        }
        list.addFirst(l);
        return list;
    }

    private LinkedList<String> reshuffle(LinkedList<String> list, String l) {
        list.remove(l);
        list.addFirst(l);
        return list;
    }

}
