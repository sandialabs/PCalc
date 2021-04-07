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
 * A specific implementation of an InputDevice to handle the gcPoints rules.
 */
public class GcPoints extends AbstractInputDevice {

    private final Listener lis = new Listener();

    private final static Field gcPoints = new FieldImpl("gcNpoints", "The number of points along the great circle.",
            "<int>   [no Default]\n\n" +
                    "The number of points that will positioned along the great circle path." +
                    "  Ignored if gcSpacing is also specified.", InputType.INT, null, null);
    private final static Field gcSpace = new FieldImpl("gcSpacing", "The approximate spacing, in degrees, between" +
            " adjacent points.",
            "<double>   [no Default]\n\n" +
                    "The approximate spacing, in degrees, between adjacent points." +
                    "  The actual spacing may be reduced somewhat from the specified value in order for an" +
                    " integer number of equally spaced points to span the length of the great circle." +
                    "  Takes precedence over gcNpoints if both are specified.", InputType.DOUBLE, null, null);

    private final JPanel p;
    private final ButtonGroup bg;
    private boolean firstSel;
    private boolean set;
    private JRadioButton jr1, jr2;
    private JTextField jtfPoints, jtfSpacing;

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
     * Creates an instance of GcPoints.  This device doesn't require any Fields, because it is
     * specifically written to have it's own.
     *
     * @param idd - The delegate for this InputDevice.
     */
    public GcPoints(InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(gcPoints, gcSpace), idd);
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
        JLabel glatL = new PCalcLabel(gcPoints.getName(), gcPoints.getShortNotes(), gcPoints.getLongNotes());
        jp.add(glatL);
        jtfPoints = new JTextField();
        jtfPoints.addActionListener(lis);
        jtfPoints.addFocusListener(lis);
        g = new Gbc();
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfPoints, g);
        jr2 = new JRadioButton();
        bg.add(jr2);
        jr2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                b2Sel(arg0);
            }
        });
        jp.add(jr2);
        JLabel glonL = new PCalcLabel(gcSpace.getName(), gcSpace.getShortNotes(), gcSpace.getLongNotes());
        g = new Gbc();
        g.gridx = 1;
        g.gridy = 1;
        jp.add(glonL);
        jtfSpacing = new JTextField();
        jtfSpacing.addActionListener(lis);
        jtfSpacing.addFocusListener(lis);
        g = new Gbc();
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfSpacing, g);
        JPanel tab = new JPanel();
        g = new Gbc();
        g.gridx = 0;
        g.gridy = 2;
        jp.add(tab, g);
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
            return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(gcPoints, jtfPoints.getText()),
                    new StringBinding<Field>(gcSpace, null));
        }
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(gcPoints, null),
                new StringBinding<Field>(gcSpace, jtfSpacing.getText()));
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        set = false;
        if (vals.size() != this.getControlledFields().size()) {
            throw new IllegalArgumentException("Invalid number of fields: " + vals.size() + " should be " + this.getControlledFields().size() + ".");
        }
        StringBinding<Field> space = null, points = null;
        for (StringBinding<Field> sb : vals) {
            if (sb.getObj().equals(gcSpace)) {
                space = sb;
            } else if (sb.getObj().equals(gcPoints)) {
                points = sb;
            } else {
                throw new IllegalArgumentException("Unknown field " + sb.getObj());
            }
        }
        if (space == null || points == null) {
            throw new NullPointerException("A value was not set - points: " + points + ", space: " + space);
        }
        if (points.getValue() != null && space.getValue() == null) {
            jr1.setSelected(true);
            firstSel = true;
            jtfPoints.setText(points.getValue());
            jtfSpacing.setText("");
            configureFields();
        } else {
            jr2.setSelected(true);
            firstSel = false;
            jtfPoints.setText("");
            jtfSpacing.setText(space.getValue());
            configureFields();
        }
        set = true;
    }

    private void configureFields() {
        if (firstSel) {
            jtfPoints.setEnabled(true);
            jtfPoints.setEditable(true);
            jtfSpacing.setEnabled(false);
            jtfSpacing.setEditable(false);
        } else {
            jtfPoints.setEnabled(false);
            jtfPoints.setEditable(false);
            jtfSpacing.setEnabled(true);
            jtfSpacing.setEditable(true);
        }
    }

    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return getInput();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.INT, InputType.DOUBLE);
    }

}
