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
package gov.sandia.gmp.pcalc.gui.inputDevice.impl.specific;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.InputType;
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.gui.impl_gui.PCalcLabel;
import gov.sandia.gmp.pcalc.gui.inputDevice.AbstractInputDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldImpl;
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

/**
 * A specific implementation of InputDevice for the GcDistance rules.
 */
public class GcDistance extends AbstractInputDevice {

    private final Listener lis = new Listener();

    private final static Field gcEnd = new FieldImpl("gcEnd", "Lat Lon",
            "<2 doubles>   [no Default]\n\n" +
                    "The latitude in degrees and longitude in degrees of the end of the great circle." +
                    "  Takes precedence over gcDistance/gcAzimuth if both methods are specified.", InputType.DOUBLE2, null, null);
    private final static Field gcDist = new FieldImpl("gcDistance", "Epicentral distance in degrees",
            "<double>   [no Default]\n\n" +
                    "Epicentral distance in degrees from gcStart to the end of great circle.  Ignored if gcEnd is specified.",
            InputType.DOUBLE, null, null);
    private final static Field gcAz = new FieldImpl("gcAzimuth", "The azimuthal direction in degrees",
            "<double>   [no Default]\n\n" +
                    "The azimuthal direction in degrees to move from gcStart in order to arrive at the end" +
                    " of the great circle. Ignored if gcEnd is specified.", InputType.DOUBLE, null, null);

    private final JPanel p;
    private final ButtonGroup bg;
    private boolean firstSel;
    private boolean set;
    private JRadioButton jr1, jr2;
    private JTextField jtfEnd, jtfDist, jtfAz;

    private class Listener implements ActionListener, FocusListener {

        @Override
        public void focusGained(FocusEvent arg0) {
        }

        @Override
        public void focusLost(FocusEvent arg0) {
            if (set) {
                JTextField jtf = (JTextField) arg0.getSource();
                if (jtf.isEditable() && jtf.isEditable()) {
                    update();
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (set) {
                set = false;
                update();
                set = true;
            }
        }

    }

    /**
     * Creates an instance of GcDistance, which doesn't require any Fields because it
     * creates it's own specifically for this job.
     *
     * @param idd
     */
    public GcDistance(InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(gcEnd, gcDist, gcAz), idd);
        bg = new ButtonGroup();
        p = createPanel();
        set = true;
    }

    private JPanel createPanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jr1 = new JRadioButton();
        bg.add(jr1);
        jr1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                b1Sel(arg0);

            }
        });
        Gbc g = new Gbc();
        jp.add(jr1, g);
        JLabel glatL = new PCalcLabel(gcEnd.getName(), gcEnd.getShortNotes(), gcEnd.getLongNotes());
        jp.add(glatL);
        jtfEnd = new JTextField();
        jtfEnd.addActionListener(lis);
        jtfEnd.addFocusListener(lis);
        g = new Gbc();
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfEnd, g);
        jr2 = new JRadioButton();
        bg.add(jr2);
        jr2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                b2Sel(arg0);
            }
        });
        jp.add(jr2);
        JLabel glonL = new PCalcLabel(gcDist.getName(), gcDist.getShortNotes(), gcDist.getLongNotes());
        g = new Gbc();
        g.gridx = 1;
        g.gridy = 1;
        jp.add(glonL);
        jtfDist = new JTextField();
        jtfDist.addActionListener(lis);
        jtfDist.addFocusListener(lis);
        g = new Gbc();
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfDist, g);
        JPanel tab = new JPanel();
        g = new Gbc();
        g.gridx = 0;
        g.gridy = 2;
        jp.add(tab, g);
        JLabel gcentL = new PCalcLabel(gcAz.getName(), gcAz.getShortNotes(), gcAz.getLongNotes());
        g = new Gbc();
        g.gridx = 1;
        g.gridy = 2;
        g.gridwidth = 1;
        g.gridheight = 1;
        jp.add(gcentL, g);
        jtfAz = new JTextField();
        jtfAz.addActionListener(lis);
        jtfAz.addFocusListener(lis);
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.gridx = 2;
        g.gridy = 2;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfAz, g);
        jr1.setSelected(true);
        jr2.setSelected(false);
        return jp;
    }

    private void b1Sel(ActionEvent arg0) {
        JRadioButton b = (JRadioButton) arg0.getSource();
        if (b.isSelected()) {
            firstSel = true;
            configureFields();
            del.setFields(this, getInput(), true);
        }
    }

    private void b2Sel(ActionEvent arg0) {
        JRadioButton b = (JRadioButton) arg0.getSource();
        if (b.isSelected()) {
            firstSel = false;
            configureFields();
            del.setFields(this, getInput(), true);
        }
    }

    @Override
    public Component getGuiComponent() {
        return p;
    }

    private void update() {
        for (StringBinding<Field> sb : getInput()) {
            del.setField(this, sb.getObj(), sb.getValue(), true);
        }
    }

    @SuppressWarnings("unchecked")
    private ImmutableList<StringBinding<Field>> getInput() {
        if (firstSel) {
            return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(gcEnd, jtfEnd.getText()),
                    new StringBinding<Field>(gcDist, null),
                    new StringBinding<Field>(gcAz, null));
        }
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(gcEnd, null),
                new StringBinding<Field>(gcDist, jtfDist.getText()),
                new StringBinding<Field>(gcAz, jtfAz.getText()));
    }


    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        set = false;
        if (vals.size() != this.getControlledFields().size()) {
            throw new IllegalArgumentException("Invalid number of fields: " + vals.size() + " should be " + this.getControlledFields().size() + ".");
        }
        StringBinding<Field> end = null, dis = null, az = null;
        for (StringBinding<Field> sb : vals) {
            if (sb.getObj().equals(gcEnd)) {
                end = sb;
            } else if (sb.getObj().equals(gcDist)) {
                dis = sb;
            } else if (sb.getObj().equals(gcAz)) {
                az = sb;
            } else {
                throw new IllegalArgumentException("Unknown field " + sb.getObj());
            }
        }
        if (end == null || dis == null || az == null) {
            throw new NullPointerException("A value was not set - dis: " + dis + ", end: " + end + ", az: " + az);
        }
        if (end.getValue() != null || dis.getValue() == null || az.getValue() == null) {
            jr1.setSelected(true);
            firstSel = true;
            jtfEnd.setText(end.getValue());
            jtfDist.setText("");
            jtfAz.setText("");
            configureFields();
        } else {
            jr2.setSelected(true);
            firstSel = false;
            jtfEnd.setText("");
            jtfDist.setText(dis.getValue());
            jtfAz.setText(az.getValue());
            configureFields();
        }
        set = true;
    }

    private void configureFields() {
        if (firstSel) {
            jtfEnd.setEnabled(true);
            jtfEnd.setEditable(true);
            jtfDist.setEnabled(false);
            jtfDist.setEditable(false);
            jtfAz.setEnabled(false);
            jtfAz.setEditable(false);
        } else {
            jtfAz.setEnabled(true);
            jtfAz.setEditable(true);
            jtfEnd.setEnabled(false);
            jtfEnd.setEditable(false);
            jtfDist.setEnabled(true);
            jtfDist.setEditable(true);
        }
    }

    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return getInput();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.DOUBLE2, InputType.DOUBLE);
    }

}
