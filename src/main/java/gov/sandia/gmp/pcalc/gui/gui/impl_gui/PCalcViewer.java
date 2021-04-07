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

import gov.sandia.gmp.pcalc.gui.controller.impl.process.Console;
import gov.sandia.gmp.pcalc.gui.util.Gbc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Instances of this class can show the output and allow some control over a running PCalc process.
 */
public class PCalcViewer implements Console {

    private final PCalcViewer thiz = this;
    private final ViewerDelegate vd;
    private final JFrame frame;
    private JButton button;
    private JLabel label;
    private static final String RUN_LABEL = "Running PCalc...";
    private static final String DONE_LABEL = "PCalc finished";
    private JTextArea cons;

    private final static String CANCEL_BUTTON = "Cancel";
    private final ActionListener cList = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            vd.cancelPressed();
        }
    };

    private final static String DONE_BUTTON = "Close";
    private final ActionListener dList = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            vd.donePressed();
        }
    };

    /**
     * The panel that takes control of the frame when a PCalc process is running.
     * {@code paintComponent} code taken from an example from http://filthyrichclients.org/,
     * the GlassPane example from Chapter 9, and the Drop Shadow example from Chapter 16.
     * {@code createDropShadow} and {@code geGaussianBlur} is also from Chapter 16.
     */
    private class LoadingPanel extends JPanel {

        private static final long serialVersionUID = 443205797669748867L;

        public LoadingPanel() {
            addComponents();
        }

        private void addComponents() {
            this.setPreferredSize(new Dimension(900, 700));
            this.setMinimumSize(new Dimension(800, 400));
            this.setLayout(new GridBagLayout());
            label = new JLabel();
            Gbc gbc = new Gbc();
            gbc.gridheight = 1;
            gbc.gridwidth = 3;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 5, 0, 0);
            this.add(label, gbc);
            button = new JButton("");
            gbc = new Gbc();
            gbc.gridx = 3;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 5, 0, 0);
            this.add(button, gbc);
            cons = new JTextArea();
            cons.setLineWrap(true);
            cons.setWrapStyleWord(true);
            cons.setEditable(false);
            cons.setFont(new Font("Courier New", Font.PLAIN, 12));
            gbc = new Gbc();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridheight = Gbc.REMAINDER;
            gbc.gridwidth = Gbc.REMAINDER;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = Gbc.BOTH;
            JScrollPane sp = new JScrollPane(cons);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setPreferredSize(new Dimension(700, 400));
            this.add(sp, gbc);
        }
    }

    /**
     * Sets the title for the JFrame that this PCalcViewer will create to show itself in.
     *
     * @param txt - The title for this window.
     */
    public void setTitle(String txt) {
        frame.setTitle(txt);
    }

    /**
     * Creates an instance of a PCalcViewer with the given delegate.
     *
     * @param vd - The delegate.
     */
    public PCalcViewer(ViewerDelegate vd) {
        this.vd = vd;
        frame = new JFrame();
        LoadingPanel lp = new LoadingPanel();
        frame.setLayout(new BorderLayout());
        frame.add(lp);
        frame.setSize(lp.getPreferredSize());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowListener() {

            @Override
            public void windowActivated(WindowEvent arg0) {
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                thiz.vd.windowClosing();
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }
        });
    }

    /**
     * Sets the running state of this Viewer.
     * The state should match the running state of a
     * PCalc process.
     *
     * @param b - {@code true} if the viewer is in the running state.
     */
    public void setActive(boolean b) {
        setState(b);
    }

    /**
     * Either shows or hides the PCalcViewer frame.
     *
     * @param vis - show or hide the PCalcViewer frame.
     */
    public void setVisible(boolean vis) {
        frame.setVisible(vis);
    }

    @Override
    public void clear() {
        cons.setText("");
    }

    private void setState(boolean running) {
        if (running) {
            button.setText(CANCEL_BUTTON);
            button.removeActionListener(dList);
            button.addActionListener(cList);
            button.setBackground(Color.YELLOW);
            label.setText(RUN_LABEL);
        } else {
            button.setText(DONE_BUTTON);
            button.removeActionListener(cList);
            button.addActionListener(dList);
            button.setBackground(Color.GREEN);
            label.setText(DONE_LABEL);
        }
    }

    @Override
    public synchronized void append(String s) {
        cons.append(s);
        cons.setCaretPosition(cons.getText().length());
    }

    /**
     * Asks the user a question, to which the reply is
     * either yes, no, or cancel.
     *
     * @param txt - The question.
     * @return The result of a call to {@link JOptionPane#showConfirmDialog(java.awt.Component,
     * Object, String, int) }.
     */
    public int promptUser(String txt) {
        return JOptionPane.showConfirmDialog(frame, txt, "PCalcProcess is wondering:", JOptionPane.YES_NO_OPTION);
    }

}
