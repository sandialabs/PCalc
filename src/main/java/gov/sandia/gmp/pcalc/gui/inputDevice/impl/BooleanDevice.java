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
import java.util.List;

/**
 * InputDevice for boolean values.
 */
public class BooleanDevice extends AbstractInputDevice implements ActionListener {

    private final JPanel p;
    private final Field f;
    private JCheckBox jcb;

    /**
     * Creates a BooleanDevice InputDevice that represents a single Field as selectable by
     * a {@code JCheckBox}.  If the check box is selected, that acts as the value for the Field
     * being {@code true}.
     *
     * @param f   - The Field to show.
     * @param idd - The delegate for this device.
     */
    public BooleanDevice(Field f, InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(f), idd);
        this.f = f;
        p = createPanel();
    }

    private JPanel createPanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jcb = new JCheckBox();
        jcb.setName(f.getName());
        jcb.addActionListener(this);
        Gbc gbc = new Gbc();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        jp.add(jcb, gbc);
        JLabel l = new PCalcLabel(f.getName(), f.getShortNotes(), f.getLongNotes());
        gbc = new Gbc();
        gbc.weightx = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = Gbc.FIRST_LINE_START;
        jp.add(l, gbc);
        JPanel filler = new JPanel();
        filler.setVisible(true);
        gbc = new Gbc();
        gbc.weightx = 1.0;
        gbc.gridx = 2;
        gbc.fill = Gbc.REMAINDER;
        gbc.gridy = 0;
        jp.add(filler, gbc);
        return jp;
    }

    @Override
    public Component getGuiComponent() {
        return p;
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        if (vals.size() != 1) {
            throw new IllegalArgumentException("Boolean Device only has 1 field: " + vals.size() + " were provided.");
        }
        StringBinding<Field> sb = vals.get(0);
        if (!sb.getObj().equals(f)) {
            throw new IllegalArgumentException("Field: " + sb.getObj() + " is not managed by this device.\n" + f + " is.");
        }
        String val = sb.getValue();
        if (val != null && val.equals(InputType.TRUE_VAL)) {
            jcb.setSelected(true);
        } else if (val != null && val.equals(InputType.FALSE_VAL)) {
            jcb.setSelected(false);
        } else {
            throw new IllegalArgumentException(val + " does not match \"" + InputType.TRUE_VAL + "\" or \"" + InputType.FALSE_VAL
                    + "\" for BooleanDevice.");
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(f, jcb.isSelected() + ""));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        del.setField(this, f, jcb.isSelected() + "", true);
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.BOOL);
    }

}
