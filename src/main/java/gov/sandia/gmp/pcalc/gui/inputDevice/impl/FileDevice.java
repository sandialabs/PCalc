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
package gov.sandia.gmp.pcalc.gui.inputDevice.impl;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.InputType;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.textBox.TextBoxDevice;
import gov.sandia.gmp.pcalc.gui.util.Gbc;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * An Implementation of an InputDevice that handles choosing a file or directory.
 */
public class FileDevice extends TextBoxDevice {

    private static final String CURRENT_DIR = ".";

    private boolean dir = false;
    private final boolean longName;
    private ActionListener al = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            getFile();
        }
    };
    private Field base;


    /**
     * Creates a FileDevice that will allow the user to choose a file or directory.
     * This is the same as calling new FileDevice(f, idd, remember, longName, <b>null</b>),
     * {@link #FileDevice(Field, InputDeviceDelegate, boolean, boolean, Field)}.
     *
     * @param f         - The Field to represent the chosen file/directory.
     * @param idd       - The delegate for this InputDevice.
     * @param remembers - If this Device should remember the last few values it was set to.
     * @param longName  - If the path for the selected file/directory should be expressed as the ({@code true})
     *                  absolute path, or ({@code false}) just the name of the file/directory selected.
     * @throws IllegalArgumentException If <i>f</i> is not of type {@code FILE} or {@code DIRECTORY}.
     */
    public FileDevice(Field f, InputDeviceDelegate idd, boolean remembers, boolean longName) {
        super(f, idd, remembers);
        dir = f.getType().equals(InputType.DIRECTORY);
        this.longName = longName;
        createButton();
        base = null;
    }

    /**
     * Creates a FileDevice that will allow the user to choose a file or directory.
     *
     * @param f         - The Field to represent the chosen file/directory.
     * @param idd       - The delegate for this InputDevice.
     * @param remembers - If this Device should remember the last few values it was set to.
     * @param longName  - If the path for the selected file/directory should be expressed as the ({@code true})
     *                  absolute path, or ({@code false}) just the name of the file/directory selected.
     * @param base      - A Field to use as the value of the base directory in which all JFileChooser options will
     *                  start from.  If it is {@code null}, then the current working directory is used.
     * @throws IllegalArgumentException If <i>f</i> is not of type {@code FILE} or {@code DIRECTORY}.
     */
    public FileDevice(Field f, InputDeviceDelegate idd, boolean remembers, boolean longName, Field base) {
        this(f, idd, remembers, longName);
        this.base = base;
    }

    private void createButton() {
        JButton b = new JButton("Browse");
        b.addActionListener(al);
        Gbc g = new Gbc();
        g.gridx = 8;
        g.gridy = 0;
        g.gridwidth = Gbc.REMAINDER;
        p.add(b, g);
    }

    /**
     * Returns the base path for a JFileChooser to start from.
     *
     * @return the base path for a JFileChooser to start from.
     */
    private String getBasePath() {
        String s;
        //If there is no base Field to use, then just default to the current dir.
        if (base == null) {
            s = CURRENT_DIR;
        } else {
            //Otherwise ues the value for the base Field.
            s = del.getValueFor(base);
            //Unless it is also null, then just default.
            if (s == null)
                s = CURRENT_DIR;
        }
        return s;
    }

    private void getFile() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(getBasePath()));
        fc.setDialogTitle(f.getName());
        if (dir) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        fc.showDialog(p, "Open");
        File fi = fc.getSelectedFile();
        if (fi == null) {
            return;
        }
        if (longName)
            setValue(fi.getAbsolutePath());
        else
            setValue(fi.getName());
        update();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.FILE, InputType.DIRECTORY);
    }

}
