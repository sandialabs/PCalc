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
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.util.Gbc;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that will take a Field that represents and Input File for PCalc and
 * show the possible column header selections either loaded from the Input File or
 * chosen by the user.
 */
public class InputAttribute extends AbstractInputDevice implements ActionListener {

    private final Field inpAttr;
    private final JPanel p;
    private ImmutableList<JComboBox> jcs;
    private final int numPerRow = 4;
    private final String blank = "";
    private final Field inF;
    private boolean fireAction;
    private final InputAttribute thiz = this;
    private final ComboListener lis = new ComboListener();

    private class ComboListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (fireAction) {
                updateBoxes();
                del.setField(thiz, inpAttr, getInput(), true);
            }
        }

    }

    /**
     * Creates an Instance of InputAttribute which will allow the choosing of the column headers
     * manually, or loaded from the file that the Field <i>inF</i> represents.
     *
     * @param inpAttr - The Field for this InputDevice.
     * @param inF     - The Field that represents the Innput File to choose column headers for.
     * @param idd     - The delegate for this InputDevice.
     */
    public InputAttribute(Field inpAttr, Field inF, InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(inpAttr), idd);
        this.inpAttr = inpAttr;
        if (inpAttr.getRestrictions() == null) {
            throw new IllegalArgumentException("InputAttributes must have restrictions.");
        }
        fireAction = false;
        createBoxes();
        p = createPanel();
        setBoxes();
        fireAction = true;
        this.inF = inF;
    }

    private void createBoxes() {
        List<JComboBox> boxes = new LinkedList<JComboBox>();
        int size = inpAttr.getRestrictions().size();
        for (int i = 0; i < size; i++) {
            JComboBox jc = new JComboBox();
            jc.addActionListener(lis);
            jc.setName("box: " + i);
            boxes.add(jc);
        }
        jcs = new ImmutableList<JComboBox>(boxes);
    }

    private void setBoxes() {
        for (JComboBox jc : jcs) {
            jc.removeAllItems();
            jc.addItem(blank);
            for (String s : inpAttr.getRestrictions()) {
                jc.addItem(s);
            }
        }
    }

    private void updateBoxes() {
        boolean old = fireAction;
        fireAction = false;
        List<Object> selections = new LinkedList<Object>();
        for (JComboBox jc : jcs) {
            Object o = jc.getSelectedItem();
            if (o != null && !o.equals(blank) && !selections.contains(o)) {
                selections.add(o);
            }
        }
        syncBoxes(selections);
        fireAction = old;
    }

    private void syncBoxes(List<Object> vals) {
        if (fireAction) {
            throw new IllegalStateException();
        }
        List<JComboBox> boxes = new LinkedList<JComboBox>(jcs);
        setBoxes();
        int i = 0;
        int size = boxes.size();
        for (i = 0; i < size; i++) {
            if (vals.size() > 0) {
                Object sel = vals.remove(0);
                boxes.remove(0).setSelectedItem(sel);
                removeSelection(boxes, sel);
            } else {
                boxes.remove(0).setSelectedItem(blank);
            }
        }
    }

    private void removeSelection(List<JComboBox> boxes, Object item) {
        for (JComboBox b : boxes) {
            b.removeItem(item);
        }
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JLabel lab = new PCalcLabel(inpAttr.getName(), inpAttr.getShortNotes(), inpAttr.getLongNotes());
        panel.add(lab);
        JPanel fil = new JPanel();
        Gbc g = new Gbc();
        g.fill = Gbc.HORIZONTAL;
        g.weightx = 1.0;
        g.gridwidth = Gbc.REMAINDER;
        panel.add(fil, g);
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jp.setBorder(BorderFactory.createTitledBorder(""));
        JButton b = new JButton("Load from input file");
        b.addActionListener(this);
        g = new Gbc();
        g.gridwidth = 2;
        g.gridheight = 1;
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = Gbc.FIRST_LINE_START;
        g.insets = new Insets(5, 5, 5, 5);
        jp.add(b, g);
        JPanel fill = new JPanel(null);
        g = new Gbc();
        g.fill = Gbc.HORIZONTAL;
        g.gridwidth = Gbc.REMAINDER;
        g.gridx = 1;
        g.gridy = 0;
        g.weightx = 1.0;
        jp.add(fill, g);
        int y = 1;
        int x = 0;
        for (int i = 0; i < jcs.size(); i++) {
            JLabel l = new JLabel("Col #" + i);
            g = new Gbc();
            g.gridx = x * 2;
            g.gridy = y;
            g.gridheight = 1;
            g.gridwidth = 1;
            g.weightx = 0;
            g.insets = new Insets(0, 5, 0, 5);
            jp.add(l, g);
            g = new Gbc();
            g.gridx = x * 2 + 1;
            g.gridy = y;
            g.weightx = 0.0;
            JComboBox jc = jcs.get(i);
            jp.add(jc, g);
            x++;
            if (x == numPerRow) {
                g = new Gbc();
                g.fill = Gbc.HORIZONTAL;
                g.weightx = 1.0;
                g.gridx = x * 2;
                g.gridy = y;
                g.gridwidth = Gbc.REMAINDER;
                JPanel filler = new JPanel(null);
                jp.add(filler, g);
                x = 0;
                y++;
            }
        }
        g = new Gbc();
        g.fill = Gbc.HORIZONTAL;
        g.weightx = 1.0;
        g.gridy = y;
        g.gridwidth = Gbc.REMAINDER;
        JPanel filler = new JPanel(null);
        jp.add(filler, g);
        g = new Gbc();
        g.fill = Gbc.BOTH;
        g.gridwidth = Gbc.REMAINDER;
        g.gridheight = Gbc.REMAINDER;
        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 1.0;
        g.weighty = 1.0;
        panel.add(jp, g);
        return panel;
    }

    @Override
    public Component getGuiComponent() {
        return p;
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
        fireAction = false;
        if (vals.size() != this.getControlledFields().size()) {
            throw new IllegalArgumentException("Should have been passed " + this.getControlledFields().size()
                    + " fields, but was instead passed " + vals.size());
        }
        StringBinding<Field> sb = vals.get(0);
        if (!sb.getObj().equals(inpAttr)) {
            throw new IllegalArgumentException("Fields don't match: [" + inpAttr + "] versus [" + sb.getObj() + "]");
        }
        if (sb.getValue() == null) {
            setBlankValues();
        } else {
            List<Object> list = getListFromString(sb.getValue());
            syncBoxes(list);
        }
        fireAction = true;
    }

    private List<Object> getListFromString(String str) {
        List<Object> list = new LinkedList<Object>();
        String[] vs = str.replaceAll(",", " ").split(" ");
        for (String s : vs) {
            if (!s.equals("")) {
                list.add(s);
            }
        }
        return list;
    }

    private String getInput() {
        StringBuilder sb = new StringBuilder();
        for (JComboBox b : jcs) {
            Object o = b.getSelectedItem();
            if (!o.equals(blank)) {
                sb.append(" " + o);
            }
        }
        String res = sb.toString();
        if (res.length() > 1) {
            return res.substring(1);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(inpAttr, getInput()));
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.CHECKBOX);
    }

    private void setBlankValues() {
        syncBoxes(new LinkedList<Object>());
    }

    private List<Object> getHeaderFromFile(File file) {
        BufferedReader bis;
        try {
            bis = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            return null;
        }
        String l;
        try {
            while ((l = bis.readLine()) != null) {
                if (!l.startsWith("#")) {
                    return getListFromString(l);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        fireAction = false;
        String val = del.getValueFor(inF);
        File file;
        if (val != null && !val.equals("")) {
            file = new File(val);
            if (!file.exists()) {
                setBlankValues();
                fireAction = true;
                return;
            }
        } else {
            setBlankValues();
            fireAction = true;
            return;
        }
        List<Object> vals = getHeaderFromFile(file);
        if (vals != null) {
            syncBoxes(vals);
            del.setField(this, inpAttr, getInput(), true);
        }
        fireAction = true;
    }

}
