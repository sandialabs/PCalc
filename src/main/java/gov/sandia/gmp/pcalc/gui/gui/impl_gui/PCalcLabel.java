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
package gov.sandia.gmp.pcalc.gui.gui.impl_gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A JLabel implementation with the ability to set tooltip text and have the label spawn a frame with
 * a given string inside for longer winded notes.
 */
public class PCalcLabel extends JLabel implements MouseListener {

    private static final long serialVersionUID = 1010236494429875453L;
    private static final Border BORDER = BorderFactory.createLineBorder(new Color(0, 190, 239));
    private final String sNotes, lNotes;
    private JFrame nFrame;
    private boolean nullNotes;

    /**
     * Creates an PCalcLabel instance with the the label text as {@code label},
     * the tooltip text as {@code sNotes} and the frame popup text as {@code lNotes}.
     *
     * @param label  - The JLabel text.
     * @param sNotes - The tooltip text.
     * @param lNotes - The popup text.
     */
    public PCalcLabel(String label, String sNotes, String lNotes) {
        super(label);
        this.sNotes = sNotes;
        this.lNotes = lNotes;
        nullNotes = lNotes == null;
        setNotes();
    }

    private void setNotes() {
        this.setToolTipText(sNotes);
        this.addMouseListener(this);
        nFrame = new JFrame();
        nFrame.setTitle(this.getText() + " notes.");
        JTextArea jta = new JTextArea(lNotes);
        jta.setEditable(false);
        jta.setWrapStyleWord(true);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        jsp.setPreferredSize(new Dimension(550, 200));
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        nFrame.add(jsp);
        nFrame.pack();
    }

    private void showNotes() {
        if (!nullNotes) {
            nFrame.setLocationRelativeTo(this);
            nFrame.setVisible(true);
        }
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        showNotes();
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        if (!nullNotes)
            this.setBorder(BORDER);
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        this.setBorder(null);
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

}
