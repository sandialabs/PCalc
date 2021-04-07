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
 * InputDevice for JComboBox style selection.
 */
public class RestrictedSelectionDevice extends AbstractInputDevice implements ActionListener {

    private final JPanel p;
    private final Field f;
    private volatile boolean ext;
    private JComboBox jc;

    /**
     * Creates a RestrictedSelectionDevice, which takes a field and shows it's list of restrictions as selections in
     * a JComboBox.  It is best to use a Field that has a default value, as the JComboBox will look like it will have
     * a pre-selected value if it does not.
     *
     * @param f   - The Field to display.
     * @param idd - The delegate for this InputDevice.
     * @throws IllegalArgumentException If the Field does not have any restrictions.
     */
    public RestrictedSelectionDevice(Field f,
                                     InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(f), idd);
        if (f.getRestrictions() == null) {
            throw new IllegalArgumentException("This RestrictedSelectionDevice must be used with a Field that has restrictions.");
        }
        this.f = f;
        p = createPanel();
        ext = false;
    }

    private JPanel createPanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        JLabel jl = new PCalcLabel(f.getName(), f.getShortNotes(), f.getLongNotes());
        jp.add(jl);
        jc = new JComboBox();
        jc.addActionListener(this);
        for (String s : f.getRestrictions()) {
            jc.addItem(s);
        }
        if (f.getDef() != null) {
            jc.setSelectedItem(f.getDef());
        }
        Gbc g = new Gbc();
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jc, g);
        JPanel filler = new JPanel();
        new Gbc();
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.gridwidth = Gbc.REMAINDER;
        jp.add(filler, g);
        return jp;
    }

    @Override
    public Component getGuiComponent() {
        return p;
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        ext = true;
        if (vals.size() != 1) {
            throw new IllegalArgumentException("Restricted Selection Device only has 1 field: " + vals.size() + " were provided.");
        }
        StringBinding<Field> sb = vals.get(0);
        if (!sb.getObj().equals(f)) {
            throw new IllegalArgumentException("Field: " + sb.getObj() + " is not managed by this device.\n" + f + " is.");
        }
        jc.setSelectedItem(sb.getValue());
        ext = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(f, (String) jc.getSelectedItem()));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!ext) {
            del.setField(this, f, (String) jc.getSelectedItem(), true);
        }
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.STRING, InputType.DOUBLE, InputType.DOUBLE1_INT1,
                InputType.DOUBLE2, InputType.DOUBLE2_INT1, InputType.DOUBLE3, InputType.DOUBLE_ARR,
                InputType.INT, InputType.INT_ARR, InputType.FILE, InputType.DIRECTORY);
    }

}
