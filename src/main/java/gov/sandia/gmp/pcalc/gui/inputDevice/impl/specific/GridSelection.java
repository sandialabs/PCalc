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
 * Special InputDevice for the grid selection methods.
 */
public class GridSelection extends AbstractInputDevice {

    private final static Field glat = new FieldImpl("gridRangeLat",
            "The minimum latitude, maximum latitude and number of latitudes.",
            "<2 doubles, 1 int>   [no Default]\n\n" +
                    "The minimum latitude, maximum latitude and number of latitudes.",
            InputType.DOUBLE2_INT1, null, null);
    private final static Field glon = new FieldImpl("gridRangeLon", "The first longitude," +
            " last longitude and number of longitudes.",
            "<2 doubles, 1 int>   [no Default] \n\n" +
                    "The first longitude, last longitude and number of longitudes.",
            InputType.DOUBLE2_INT1, null, null);
    /**
     * The Field for gridCenter.
     */
    public final static Field gcent = new FieldImpl("gridCenter",
            "Latitude and longitude, in degrees, of the center of the grid.",
            "<2 doubles>   [no Default]\n\n" +
                    "Latitude and longitude, in degrees, of the center of the grid.\n\n" +
                    "Ignored if gridRangeLat and gridRangeLon are specified, required otherwise.",
            InputType.DOUBLE2, null, null);

    private final JPanel p;
    private final ButtonGroup bg;
    private boolean firstSel;
    private boolean set;
    private JRadioButton jr1, jr2;
    private JTextField jtfLat, jtfLon, jtfCent;

    private final Listener lis = new Listener();

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
     * Creates a GridSelection InputDevice, which creates its own Fields to show.
     *
     * @param idd - The delegate for this InputDevice.
     */
    public GridSelection(InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(glat, glon, gcent), idd);
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
        JLabel glatL = new PCalcLabel(glat.getName(), glat.getShortNotes(), glat.getLongNotes());
        jp.add(glatL);
        jtfLat = new JTextField();
        jtfLat.addActionListener(lis);
        jtfLat.addFocusListener(lis);
        g = new Gbc();
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfLat, g);
        JPanel tab = new JPanel();
        g = new Gbc();
        g.gridx = 0;
        g.gridy = 1;
        jp.add(tab, g);
        JLabel glonL = new PCalcLabel(glon.getName(), glon.getShortNotes(), glon.getLongNotes());
        g = new Gbc();
        g.gridx = 1;
        g.gridy = 1;
        jp.add(glonL);
        jtfLon = new JTextField();
        jtfLon.addActionListener(lis);
        jtfLon.addFocusListener(lis);
        g = new Gbc();
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfLon, g);
        jr2 = new JRadioButton();
        bg.add(jr2);
        jr2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                b2Sel(arg0);
            }
        });
        jp.add(jr2);
        JLabel gcentL = new PCalcLabel(gcent.getName(), gcent.getShortNotes(), gcent.getLongNotes());
        jp.add(gcentL);
        jtfCent = new JTextField();
        jtfCent.addActionListener(lis);
        jtfCent.addFocusListener(lis);
        g.gridwidth = Gbc.REMAINDER;
        g.weightx = 1.0;
        g.fill = Gbc.HORIZONTAL;
        g.insets = new Insets(0, 5, 0, 0);
        jp.add(jtfCent, g);
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
            return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(glat, jtfLat.getText()),
                    new StringBinding<Field>(glon, jtfLon.getText()),
                    new StringBinding<Field>(gcent, null));
        }
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(glat, null),
                new StringBinding<Field>(glon, null),
                new StringBinding<Field>(gcent, jtfCent.getText()));
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        set = false;
        if (vals.size() != this.getControlledFields().size()) {
            throw new IllegalArgumentException("Invalid number of fields: " + vals.size() + " should be " + this.getControlledFields().size() + ".");
        }
        StringBinding<Field> lon = null, lat = null, cen = null;
        for (StringBinding<Field> sb : vals) {
            if (sb.getObj().equals(glon)) {
                lon = sb;
            } else if (sb.getObj().equals(glat)) {
                lat = sb;
            } else if (sb.getObj().equals(gcent)) {
                cen = sb;
            } else {
                throw new IllegalArgumentException("Unknown field " + sb.getObj());
            }
        }
        if (lon == null || lat == null || cen == null) {
            throw new NullPointerException("A value was not set - lat: " + lat + ", lon: " + lon + ", cen: " + cen);
        }
        if (lon.getValue() != null || lat.getValue() != null || cen.getValue() == null) {
            jr1.setSelected(true);
            firstSel = true;
            jtfLat.setText(lat.getValue());
            jtfLon.setText(lon.getValue());
            jtfCent.setText(cen.getValue());
            configureFields();
        } else {
            jr2.setSelected(true);
            firstSel = false;
            jtfLat.setText("");
            jtfLon.setText("");
            jtfCent.setText(cen.getValue());
            configureFields();

        }
        set = true;
    }

    private void configureFields() {
        if (firstSel) {
            jtfLat.setEnabled(true);
            jtfLat.setEditable(true);
            jtfLon.setEnabled(true);
            jtfLon.setEditable(true);
            jtfCent.setEnabled(false);
            jtfCent.setEditable(false);
        } else {
            jtfCent.setEnabled(true);
            jtfCent.setEditable(true);
            jtfLat.setEnabled(false);
            jtfLat.setEditable(false);
            jtfLon.setEnabled(false);
            jtfLon.setEditable(false);
        }
    }

    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return getInput();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.DOUBLE2, InputType.DOUBLE2_INT1);
    }

}
