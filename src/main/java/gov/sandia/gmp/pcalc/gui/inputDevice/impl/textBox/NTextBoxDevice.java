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
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.gui.impl_gui.PCalcLabel;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.util.Gbc;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

final class NTextBoxDevice implements ActionListener, FocusListener, TextBoxInputDevice {

    /**
     * The JPanel to add items to, being the main JPenl in which this InputDevice will display itself with.
     */
    private final JPanel panel;
    private final InputDeviceDelegate del;
    private final ImmutableList<Field> controlledFields;

    /**
     * The Field in which this InputDevice represents.
     */
    private Field f;
    private JTextField jtf;
    private volatile boolean shouldUpd;

    /**
     * Creates a TextBoxDevice for one field, that will allow the user to input any text
     * as the possible value for that field.
     *
     * @param f         - The Field for this InputDevice to represent.
     * @param idd       - The delegate for this device.
     * @param remembers - If this InputDevice should remember the last few values it was set to.
     */
    public NTextBoxDevice(Field f, InputDeviceDelegate idd) {
        this.f = f;
        controlledFields = new ImmutableList<Field>(f);
        del = idd;
        panel = new JPanel();
        initPanel(panel);
        shouldUpd = true;
    }

    private void initPanel(JPanel p) {
        p.setLayout(new GridBagLayout());
        JLabel jl = new PCalcLabel(f.getName(), f.getShortNotes(), f.getLongNotes());
        Gbc gbc = new Gbc();
        gbc.fill = Gbc.HORIZONTAL;
        gbc.weightx = 0;
        gbc.anchor = Gbc.FIRST_LINE_START;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        p.add(jl, gbc);
        addTextField(p);
    }

    private void addTextField(JPanel p) {
        jtf = new JTextField();
        Gbc gbc = new Gbc();
        gbc.fill = Gbc.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridheight = 1;
        gbc.gridwidth = 5;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        jtf.addFocusListener(this);
        jtf.addActionListener(this);
        p.add(jtf, gbc);
    }


    @Override
    public void setValue(String text) {
        jtf.setText(text);
        update();
    }

    @Override
    public String getValue() {
        return jtf.getText();
    }


    @Override
    public void setEditable(boolean b) {
        jtf.setEnabled(b);
        jtf.setEditable(b);
    }


    @Override
    public Component getGuiComponent() {
        return panel;
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        int count = vals.size();
        if (count != 1) {
            throw new IllegalArgumentException("This TextBoxDevice controls 1 Field, " + vals.size() + " were provided.");
        }
        StringBinding<Field> sb = vals.get(0);
        if (!sb.getObj().equals(f)) {
            throw new IllegalArgumentException("Field: " + sb.getObj() + " is not managed by this device.\n" + f + " is.");
        }
        shouldUpd = false;
        setValue(sb.getValue());
        shouldUpd = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(f, getValue()));
    }

    @Override
    public void update() {
        if (shouldUpd) {
            String s = getValue();
            shouldUpd = false;
            del.setField(this, f, s, true);
            setValue(del.getValueFor(f));
            shouldUpd = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        update();
    }

    @Override
    public void focusGained(FocusEvent arg0) {
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        update();
    }

    @Override
    public ImmutableList<Field> getControlledFields() {
        return controlledFields;
    }

}
