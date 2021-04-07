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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

/**
 * A TextBoxDevice that Remembers.  In other words, this is another Implementation of a text box like input device
 * that will store the previous values so the user can select them later, making switching between different
 * sets of values easy.
 */
final class RTextBoxDevice implements ActionListener, FocusListener, DocumentListener, TextBoxInputDevice {

    /**
     * The Field for this RTextBoxDevice.
     */
    private final Field f;
    private final InputDeviceDelegate del;
    private final ImmutableList<Field> controlledFields;

    /**
     * The JPanel in which to add components to, that this InputDevice will use to display itself.
     */
    private final JPanel panel;
    private final JComboBox cb;
    private boolean update;
    private String currentVal;

    /**
     * Creates an RTextBoxDevice, which is another implementation of a TextBoxDevice that remembers the
     * last few values.
     *
     * @param f   - The Field for this InputDevice to configure.
     * @param idd - The InputDeviceDelegate to delegate this InputDevice.
     */
    public RTextBoxDevice(Field f, InputDeviceDelegate idd) {
        this.f = f;
        controlledFields = new ImmutableList<Field>(f);
        this.del = idd;
        panel = new JPanel();
        cb = new JComboBox();
        setUpPanel();
        update = true;
        currentVal = null;
    }

    private void setUpPanel() {
        panel.setLayout(new GridBagLayout());
        addLabel();
        addCombo();
    }

    private void addCombo() {
        cb.setEditable(true);
        //Set up the combo values, and also sets currentVal to be the default.
        //Because when you set the selected item, it calls the document listener which updates currentVal.
        setComboValues(del.getValueFor(f));
        cb.addActionListener(this);
        cb.setPreferredSize(new Dimension(-1, 20));
        JTextComponent tc = (JTextComponent) cb.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(this);
        Gbc g = new Gbc();
        g.fill = Gbc.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 5, 0, 5);
        panel.add(cb, g);
    }

    /**
     * Sets the combo values to be the values of what the config info is for this field.
     * It then set the selected item for the JComboBox to be {@code current}.
     * {@see JComboBox#setSelectedItem(Object)}
     *
     * @param current - The value to be the selected item for the JComboBox.
     */
    private void setComboValues(String current) {
        List<String> list = del.getConfigForField(f);
        if (list == null) {
            return;
        }
        cb.removeAllItems();
        for (String s : list) {
            cb.addItem(s);
        }
        cb.setSelectedItem(current);
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {
        changedUpdate(arg0);
    }

    @Override
    public void insertUpdate(DocumentEvent arg0) {
        updateVal(arg0);
    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
        updateVal(arg0);
    }

    private void updateVal(DocumentEvent arg0) {
        try {
            currentVal = arg0.getDocument().getText(0, arg0.getDocument().getLength());
        } catch (BadLocationException e) {
            return;
        }
    }

    private void addLabel() {
        PCalcLabel pl = new PCalcLabel(f.getName(), f.getShortNotes(), f.getLongNotes());
        Gbc g = new Gbc();
        g.weightx = 0;
        g.weighty = 0;
        panel.add(pl, g);
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
        update = false;
        setComboValues(sb.getValue());
        update = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        String val = getValue();
        //At this time we can be sure that the user means for the currentVal to be the
        //value for this TBD, so go ahead and add it to the config info.
        if (val != null) {
            del.addConfigForField(f, val);
            setComboValues(val);
        }
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(f, val));
    }

    /**
     * Sets current value of this device's editable combo box.  Does not call update.
     *
     * @param txt - The new value of the editable combo box.
     */
    @Override
    public void setValue(String txt) {
        update = false;
        cb.setSelectedItem(txt);
        update = true;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        update();
    }

    @Override
    public String getValue() {
        return currentVal;
    }

    @Override
    public void update() {
        if (!update) {
            return;
        }
        String s = getValue();
        if (s != null)
            del.addConfigForField(f, s);
        del.setField(this, f, s, true);
        update = false;
        setComboValues(del.getValueFor(f));
        update = true;
    }

    @Override
    public void focusGained(FocusEvent arg0) {
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        update();
    }

    @Override
    public void setEditable(boolean b) {
        cb.setEditable(b);
        cb.setEnabled(b);
    }

    @Override
    public ImmutableList<Field> getControlledFields() {
        return controlledFields;
    }

}
