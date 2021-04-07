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
package gov.sandia.gmp.pcalc.gui.inputDevice.impl.textBox;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.InputType;
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.files.FileManager;
import gov.sandia.gmp.pcalc.gui.inputDevice.AbstractInputDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Implementation of a TextBox InputDevice for single line entries.
 */
public class TextBoxDevice extends AbstractInputDevice {

    /**
     * The Field in which this InputDevice represents.
     */
    protected final Field f;

    /**
     * The JPanel to add items to, being the main JPenl in which this InputDevice will display itself with.
     */
    protected final JPanel p;
    private TextBoxInputDevice myDevice;

    /**
     * Creates a TextBoxDevice.  A TextBoxDevice is an InputDevice that is simply a name with an input field.
     * This type of InputDevice can also remember the last few values that it was set to, depending
     * on {@code remembers}.<br /><br />
     * If this value is set to {@code true} then the input field will be an editable
     * JComboBox.  The user can directly input the vlaue into the combo box, or use the drop down menu to
     * select previous values.  Whenever the current configuration state is saved, or a PCalc calculation
     * is ran, the information on the previous 10 values will be saved for future use.
     * <br /><br />
     * If {@code remembers} is {@code false} then the input field will be a plain JTextField.
     *
     * @param fs        - The Field for this InputDevice to allow editing of.
     * @param idd       - The InputDeviceDelegate for this InputDevice.
     * @param remembers - Whether or not this TextBoxDevice should remember the last few values it was set to.
     */
    public TextBoxDevice(Field fs, InputDeviceDelegate idd, boolean remembers) {
        super(new ImmutableList<Field>(fs), idd);
        if (remembers && FileManager.environmentIsValid()) {
            myDevice = new RTextBoxDevice(fs, idd);
        } else {
            myDevice = new NTextBoxDevice(fs, idd);
        }
        p = (JPanel) myDevice.getGuiComponent();
        f = fs;
    }

    @Override
    public Component getGuiComponent() {
        return p;
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        myDevice.setValue(vals);
    }

    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return myDevice.getValues();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return InputType.ALL;
    }

    /**
     * Sets the Enabled/Editable state of this TextBoxDevice.
     *
     * @param b - Whether or not this TextBoxDevice should be Enabled and Editable.
     */
    protected void setEditable(boolean b) {
        myDevice.setEditable(b);
    }

    /**
     * Causes the value for this InputDevice to be sent to the delegate.
     */
    protected void update() {
        myDevice.update();
    }

    /**
     * Sets the value of this TextBoxDevice's input area.
     *
     * @param text - The string to be the new value.
     */
    protected void setValue(String txt) {
        myDevice.setValue(txt);
    }

    /**
     * Returns the value of this TextBoxDevice's input area.
     *
     * @return the value of this TextBoxDevice's input area.
     */
    protected String getValue() {
        return myDevice.getValue();
    }

}
