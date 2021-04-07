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
package gov.sandia.gmp.pcalc.gui.gui;

import gov.sandia.gmp.pcalc.gui.common.Section;
import gov.sandia.gmp.pcalc.gui.common.Subsection;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDevice;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * A Gui in the realm of PCalc is some font end user interface that is configurable by
 * adding Sections, Subsections, and InputDevices.  An input device is added to a Subsection,
 * and a Subsection to a Section.  This allows for dynamic addition and removal of displaying
 * input devices without having to worry about how it is organized on screen.
 */
public interface Gui {

    /**
     * Sets the Gui to be visible or not.
     *
     * @param visible - Whether or not Gui should be visible.
     */
    public void setVisible(boolean visible);

    /**
     * Adds a new Section for Properties to the Gui.
     *
     * @param s - The section to add.
     */
    public void addSection(Section s);

    /**
     * Removes the Section and all of its children from the Gui.
     *
     * @param s - The Section to remove.
     */
    public void removeSection(Section s);

    /**
     * Adds a new PropertyField to this Gui.  The PropertyField knows which section
     * is should belong to.
     *
     * @param pf - The PropertyField to add.
     */
    public void addInputDevice(InputDevice id, Subsection sub);

    /**
     * Removes the given InputDevice from the gui.
     *
     * @param id  - The InputDevice to remove.
     * @param sub - The Subsection where the InputDevice is.
     */
    public void removeInputDevice(InputDevice id, Subsection sub);

    /**
     * Adds the given Subsection to the Gui.
     *
     * @param sub - The Subsection to add.
     */
    public void addSubsection(Subsection sub);

    /**
     * Removes the given Subsection from the Gui.
     *
     * @param sub - The Subsection to remove.
     */
    public void removeSubsection(Subsection sub);

    /**
     * Displays the given error message to the user.
     *
     * @param txt - The message to display.
     */
    public void showErrorMessage(String txt);

    /**
     * Asks the user a question, to which the reply is
     * either yes, no, or cancel.
     *
     * @param txt - The question.
     * @return The result of a call to {@link JOptionPane#showConfirmDialog(java.awt.Component, Object, String, int) }.
     */
    public int promptUser(String txt);

    /**
     * Tasks the Gui with retrieving from the user a File.
     *
     * @param f       - The set of file name extensions to use as a filter.
     * @param txt     - The text to use as the acceptance button's text.
     * @param current - The File to use as the current location to start looking for other Files.
     * @return The file the user chose, or {@code null} if none was chosen.
     */
    public File getFile(FileNameExtensionFilter f, String txt, File current);

    /**
     * Sets the title of the GUI to have a path in the title identifying what file is currently being modified.
     * For example:<br />
     * "PCalc - /usr/bjkraus/pcalc/examples/pcalc_pred_greatcircle.properties"
     * Would be the title if "/usr/bjkraus/pcalc/examples/pcalc_pred_greatcircle.properties"
     * was the argument to this method.  It is not required to display the "PCalc - " portion of this title, but recommended.
     * It is only required to show the complete string of the argument in the title immediately after this method is
     * invoked.
     * <br />
     * <br />
     * If {@code null} is the value of the argument, then no identifier is shown.  For example:<br />
     * "PCalc" would be a possible title for the Gui as a result.
     *
     * @param path - The path for the file currently being modified.
     */
    public void setTitleFileIdentifier(String path);

    /**
     * Informs the Gui as to when the initial set of additions is complete, normally done before the Gui is visible.
     * This way the Gui can optimized operation while the Gui is not being displayed, and then react accordingly
     * when it is.
     */
    public void initDone();

    /**
     * Sets the info for the program that should be displayed somehow, like in a info panel or something.
     * This information is about the PCalc program, such as the use and version number.
     *
     * @param txt - The textual information already formatted.
     */
    public void setInfoDescription(String txt);

}
