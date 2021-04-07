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
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.gui.impl_gui.PCalcLabel;
import gov.sandia.gmp.pcalc.gui.inputDevice.AbstractInputDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.util.Gbc;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of InputDevice that is used for inputting passwords.
 */
public class PasswordDevice extends AbstractInputDevice implements FocusListener, ActionListener {

    private JPasswordField jp;
    private JPanel p;
    private Field f;
    private boolean shouldUpd;

    /**
     * Creates a PasswordDevice which allows a password value to be input for the Field <i>f</i>.
     *
     * @param f   - The Field to allow inputting of passwords for.
     * @param idd - The delegate for this InputDevice.
     */
    public PasswordDevice(Field f, InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(f), idd);
        this.f = f;
        p = new JPanel();
        initPanel(p);
        shouldUpd = true;
    }

    private void initPanel(JPanel p2) {
        p.setLayout(new GridBagLayout());
        p.setLayout(new GridBagLayout());
        jp = new JPasswordField();
        JLabel jl = new PCalcLabel(f.getName(), f.getShortNotes(), f.getLongNotes());
        Gbc gbc = new Gbc();
        gbc.fill = Gbc.HORIZONTAL;
        gbc.weightx = 0;
        gbc.anchor = Gbc.FIRST_LINE_START;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        p.add(jl, gbc);
        gbc = new Gbc();
        gbc.fill = Gbc.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridheight = 1;
        gbc.gridwidth = 5;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 0, 0);
        jp.addFocusListener(this);
        jp.addActionListener(this);
        p.add(jp, gbc);
    }

    @Override
    public Component getGuiComponent() {
        return p;
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
        jp.setText(sb.getValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        char[] pass = jp.getPassword();
        String s = new String(pass);
        Arrays.fill(pass, '\0');
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(f, s));
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.STRING);
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

    private void update() {
        if (shouldUpd) {
            shouldUpd = false;
            char[] pass = jp.getPassword();
            String s = new String(pass);
            Arrays.fill(pass, '\0');
            del.setField(this, f, s, true);
            shouldUpd = true;
        }
    }

}
