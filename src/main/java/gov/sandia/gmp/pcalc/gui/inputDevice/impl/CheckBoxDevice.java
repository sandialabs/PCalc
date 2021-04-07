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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * CheckBoxDevice InputDevice.  Creates a Series of check boxes, one for each of the values in the list of
 * restrictions.
 */
public class CheckBoxDevice extends AbstractInputDevice implements ActionListener {

    private final JPanel p;
    private final Field f;
    private List<JCheckBox> boxes;
    private boolean ext;

    /**
     * Creates a CheckBoxDevice, which creates a JCheckBox for each of the values of the Fields.
     *
     * @param f
     * @param idd
     */
    public CheckBoxDevice(Field f, InputDeviceDelegate idd) {
        super(new ImmutableList<Field>(f), idd);
        this.f = f;
        if (f.getRestrictions() == null) {
            throw new IllegalArgumentException("CheckBoxDevices require Fields that have Restrictions.");
        }
        boxes = new LinkedList<JCheckBox>();
        ext = false;
        p = createPanel();
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JLabel l = new PCalcLabel(f.getName(), f.getShortNotes(), f.getLongNotes());
        panel.add(l);
        JPanel fil = new JPanel();
        Gbc g = new Gbc();
        g.fill = Gbc.HORIZONTAL;
        g.weightx = 1.0;
        g.gridwidth = Gbc.REMAINDER;
        g.gridx = 1;
        g.gridy = 0;
        panel.add(fil, g);
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jp.setBorder(BorderFactory.createTitledBorder(""));
        addBoxes(jp);
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

    private void addBoxes(JPanel jp) {
        int row = 0;
        int col = 0;
        Insets checkboxInsets = new Insets(0, 5, 0, 0);
        for (String s : f.getRestrictions()) {
            JCheckBox jc = new JCheckBox();
            boxes.add(jc);
            jc.addActionListener(this);
            jc.setName(s);
            Gbc g = new Gbc();
            g.gridx = col * 2;
            g.gridy = row;
            g.gridheight = 1;
            g.gridwidth = 1;
            g.insets = checkboxInsets;
            jp.add(jc, g);
            JLabel jl = new JLabel(s);
            jl.setHorizontalAlignment(SwingConstants.LEFT);
            g = new Gbc();
            g.gridx = col * 2 + 1;
            g.gridy = row;
            g.gridheight = 1;
            g.gridwidth = 1;
            g.fill = Gbc.HORIZONTAL;
            jp.add(jl, g);
            col++;
            if (col == 5) {
                Gbc gbc = createFiller();
                gbc.gridx = col * 2;
                gbc.gridy = row;
                jp.add(new JPanel(), gbc);
                col = 0;
                row++;
            }
        }
        Gbc gbc = createFiller();
        gbc.gridx = col * 2;
        gbc.gridy = row;
        jp.add(new JPanel(), gbc);
    }

    private Gbc fg;

    private Gbc createFiller() {
        if (fg == null) {
            fg = new Gbc();
            fg.fill = Gbc.HORIZONTAL;
            fg.gridwidth = Gbc.REMAINDER;
            fg.weightx = 1.0;
            fg.gridheight = 1;
        }
        return fg;
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
        if (sb.getValue() == null) {
            for (JCheckBox jc : boxes) {
                jc.setSelected(false);
            }
            ext = false;
            return;
        }
        List<String> strs = getList(sb.getValue().replaceAll(",", " ").split(" "));
        //pick out the empty strings.
        for (Iterator<String> i = strs.iterator(); i.hasNext(); ) {
            String s = i.next();
            if (s.equals("")) {
                i.remove();
            }
        }
        for (JCheckBox jc : boxes) {
            jc.setSelected(strs.contains(jc.getName()));
            strs.remove(jc.getName());
        }
        if (!strs.isEmpty()) {
            throw new IllegalArgumentException("Extra unknown values for this checkbox device: " + strs + " " + strs.size() + "\nallowed: " + getNames());
        }
        ext = false;
    }

    private List<String> getList(String[] ss) {
        List<String> list = new LinkedList<String>();
        for (String s : ss) {
            list.add(s);
        }
        return list;
    }

    private List<String> getNames() {
        List<String> list = new LinkedList<String>();
        for (JCheckBox jc : boxes) {
            list.add(jc.getName());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(f, getValString()));
    }

    private String getValString() {
        StringBuilder sb = new StringBuilder();
        for (JCheckBox jc : boxes) {
            if (jc.isSelected()) {
                sb.append(jc.getName() + " ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return new ImmutableList<InputType>(InputType.CHECKBOX);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!ext) {
            del.setField(this, f, getValString(), true);
        }
    }

}
