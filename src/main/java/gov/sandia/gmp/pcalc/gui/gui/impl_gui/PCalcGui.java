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

import gov.sandia.gmp.pcalc.gui.common.Section;
import gov.sandia.gmp.pcalc.gui.common.Section.SectionComparer;
import gov.sandia.gmp.pcalc.gui.common.Subsection;
import gov.sandia.gmp.pcalc.gui.controller.GuiController;
import gov.sandia.gmp.pcalc.gui.gui.Gui;
import gov.sandia.gmp.pcalc.gui.gui.cpanel.CollapsiblePanel;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDevice;
import gov.sandia.gmp.pcalc.gui.util.Gbc;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;

/**
 * The DefaultGui for the PCalc configuration file.
 */
public class PCalcGui extends WindowAdapter implements Gui {

    private final JFrame frame;
    private final GuiController gcont;
    private final JTabbedPane sectionTab;
    private final Map<Section, TabPanel> sectionToPanel;
    private boolean initDone;

    private final List<TabPanel> tabs = new LinkedList<TabPanel>();
    private static final Dimension SIZE = new Dimension(1000, 700);
    private static final int VERT_SCROLL_SPEED = 25;
    private static final String BASE_TITLE = "PCalc";
    private static final TabComparator T_COMP = new TabComparator();


    /**
     * The panel that will reside as the parent container for each tab
     * in the JTabbedPane.
     */
    private class TabPanel extends JPanel {

        private static final long serialVersionUID = -1802844411718550531L;
        private final JPanel filler;
        private final Gbc fillerGbc;
        private final Map<Subsection, CollapsiblePanel> m;
        private final Section mySection;
        private final List<Subsection> subs;
        private boolean isNew;
        private final JScrollPane jsp;

        /**
         * Creates a blank TabPanel.
         */
        public TabPanel(Section s) {
            super();
            mySection = s;
            filler = new JPanel();
            fillerGbc = new Gbc();
            fillerGbc.weighty = 1.0;
            fillerGbc.fill = Gbc.BOTH;
            setLayout(new GridBagLayout());
            setMinimumSize(SIZE);
            subs = new LinkedList<Subsection>();
            m = new HashMap<Subsection, CollapsiblePanel>();
            jsp = createScrollPane(this);
            this.validate();
        }

        private JScrollPane createScrollPane(JPanel tp) {
            JScrollPane sp = new JScrollPane(tp);
            sp.setWheelScrollingEnabled(true);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.getVerticalScrollBar().setUnitIncrement(VERT_SCROLL_SPEED);
            sp.setPreferredSize(sp.getSize());
            return sp;
        }

        /**
         * Returns the JScrollPane to use with this TabPane.
         * The JScrollPane has already been initialized properly, so
         * no added setup is required.
         *
         * @return A JScrollPane to use instead of this TabPane.
         */
        public JScrollPane getScrollPane() {
            return jsp;
        }

        /**
         * Returns the Section this TabPane is representing.
         *
         * @return the Section this TabPane is representing.
         */
        public Section getSection() {
            return mySection;
        }

        /**
         * Sets whether or not a new item has been added, or an old item has been removed.
         * The argument should be {@code true} if either something was added or removed.
         * It should be {@code false} if this item was visited.  This is so the TabPanel
         * can indicate when it should display itself in a manner indicating a change
         * has occurred, or that the user has viewed those changes and thus should return
         * to a normal state.
         *
         * @param b - Whether or not something has changed in this TabPane.
         */
        public void setNew(boolean b) {
            isNew = b;
            if (isNew) {
                sectionTab.setBackgroundAt(getIndex(), Color.YELLOW);
            } else {
                sectionTab.setBackgroundAt(getIndex(), Color.LIGHT_GRAY);
            }
        }

        /**
         * Returns whether or not something has changed and those changes have not been viewed by
         * the user.
         *
         * @return Whether or not something has changed and those changes have not been viewed by
         * the user.
         */
        public boolean isNew() {
            return isNew;
        }

        /**
         * Adds a Subsection to this TabPane, representing the given Subsection
         * as a Collapsible Panel.
         *
         * @param s - The Subsection to add.
         */
        public void addSubsection(Subsection s) {
            if (m.get(s) != null) {
                //Then this subsection is already in the panel
                return;
            }
            this.invalidate();
            this.removeAll();
            CollapsiblePanel cp = new CollapsiblePanel(s.name, true);
            m.put(s, cp);
            subs.add(s);
            Collections.sort(subs, Subsection.S_COMP);
            for (Subsection sub : subs) {
                cp = m.get(sub);
                Gbc gbc = new Gbc();
                gbc.gridwidth = Gbc.REMAINDER;
                gbc.weightx = 1.0;
                gbc.fill = Gbc.HORIZONTAL;
                add(cp, gbc);
            }
            add(filler, fillerGbc);
            this.validate();
            this.repaint();
        }

        /**
         * Returns the index of this tabbed pane within the list of tabbed panel tabs.
         *
         * @return the index of this tabbed pane within the list of tabbed panel tabs.
         */
        public int getIndex() {
            return tabs.indexOf(this);
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return m.hashCode();
        }

        /**
         * Returns the CollapsiblePanel for the given Subsection, or
         * {@code null} if one does not exist.
         *
         * @param s - Subsection.
         * @return The CollapsiblePanel for s.
         */
        public CollapsiblePanel getSubsection(Subsection s) {
            return m.get(s);
        }

        /**
         * Removes the Collapsible panel from the TabPanel for the given Subsection.
         *
         * @param s - The Subsection to remove.
         */
        public void removeSubsection(Subsection s) {
            JPanel p = m.get(s);
            if (p == null) {
                return;
            }
            this.invalidate();
            remove(p);
            m.remove(s);
            subs.remove(s);
            this.validate();
            this.repaint();
        }
    }

    /**
     * Runnable version of showing the frame.
     */
    private class FrameSetter implements Runnable {
        private boolean vis;

        public FrameSetter(boolean vis) {
            this.vis = vis;
        }

        @Override
        public void run() {
            frame.setVisible(vis);
        }

    }

    /**
     * Runnable version to add a Section.
     */
    private class SectionAdder implements Runnable {
        private Section s;

        public SectionAdder(Section s) {
            this.s = s;
        }

        @Override
        public void run() {
            TabPanel tp = sectionToPanel.get(s);
            if (tp == null) {
                tp = new TabPanel(s);
                sectionToPanel.put(s, tp);
            }
            //Add the section to the list of sections.
            tabs.add(tp);
            //We're going to do alot of junk, so don't make it bother repainting or anything.
            sectionTab.invalidate();
            //Start from scratch.
            sectionTab.removeAll();
            //Sort the order of the sections to show.
            Collections.sort(tabs, T_COMP);
            //Then add them in this order.
            for (TabPanel tab : tabs) {
                JScrollPane sp = tab.getScrollPane();
                sectionTab.addTab(tab.getSection().getTitle(), sp);
                sectionTab.setMnemonicAt(tab.getIndex(), tab.getSection().getMnemonicKey());
                //Don't set the panel to be new, if it wasn't previously new.
                if (!initDone) {
                    tab.setNew(false);
                } else {
                    tab.setNew(tab.isNew());
                }
            }
            //This is the panel that was being added, so it is always new.
            if (!initDone) {
                tp.setNew(false);
            } else {
                tp.setNew(true);
            }
            sectionTab.validate();
            sectionTab.repaint();
        }

    }

    private static final class TabComparator implements Comparator<TabPanel> {
        private static final SectionComparer S_COMP = Section.COMP;

        @Override
        public int compare(TabPanel arg0, TabPanel arg1) {
            return S_COMP.compare(arg0.getSection(), arg1.getSection());
        }
    }

    private class SectionRemover implements Runnable {
        private Section s;

        public SectionRemover(Section s) {
            this.s = s;
        }

        @Override
        public void run() {
            TabPanel tp = sectionToPanel.get(s);
            if (tp == null) {
                throw new IllegalStateException("No Section for " + s);
            }
            sectionTab.invalidate();
            sectionTab.removeTabAt(tp.getIndex());
            tabs.remove(tp);
            sectionTab.validate();
            tp.repaint();
        }
    }

    /**
     * Runnable version to add a Subsection.
     */
    private class SubsectionAdder implements Runnable {
        private Subsection sub;

        public SubsectionAdder(Subsection s) {
            sub = s;
        }

        @Override
        public void run() {
            TabPanel tp = sectionToPanel.get(sub.s);
            if (tp == null) {
                throw new IllegalStateException("No Section for " + sub.s);
            }
            tp.invalidate();
            tp.addSubsection(sub);
            tp.validate();
        }
    }

    /**
     * Runnable version to remove a Subsection.
     */
    private class SubsectionRemover implements Runnable {
        private Subsection sub;

        public SubsectionRemover(Subsection sub) {
            this.sub = sub;
        }

        @Override
        public void run() {
            TabPanel tp = sectionToPanel.get(sub.s);
            tp.invalidate();
            tp.removeSubsection(sub);
            tp.validate();
        }

    }

    /**
     * Runnable version to repaint the frame.
     */
    private class Repainter implements Runnable {

        @Override
        public void run() {
            Component c = sectionTab.getSelectedComponent();
            if (!(c instanceof Container))
                throw new RuntimeException("Tab panel is not a Container");
            Container con = (Container) c;
            con.doLayout();
            frame.repaint();
        }
    }

    /**
     * Runnable Version to add InputDevices.
     */
    private class InputAdder implements Runnable {
        private Component c;
        private Subsection sub;

        public InputAdder(Component c, Subsection sub) {
            this.c = c;
            this.sub = sub;
        }

        @Override
        public void run() {
            TabPanel tp = sectionToPanel.get(sub.s);
            if (initDone) {
                tp.setNew(true);
            }
            JPanel p = tp.getSubsection(sub);
            p.add(c);
            tp.doLayout();
            tp.repaint();
        }

    }

    /**
     * Runnable Version to remove InputDevices.
     */
    private class InputRemover implements Runnable {
        private Component c;
        private Subsection sub;

        public InputRemover(Component c, Subsection sub) {
            this.c = c;
            this.sub = sub;
        }

        @Override
        public void run() {
            TabPanel tp = sectionToPanel.get(sub.s);
            JPanel p = tp.getSubsection(sub);
            p.remove(c);
            if (initDone) {
                tp.setNew(true);
            }
            tp.doLayout();
            tp.repaint();
        }

    }

    /**
     * Creates a PCalcGui which is a GUI frontend that is told what to display,
     * and it handles the where and how.
     *
     * @param c - The controller for this gui.
     */
    public PCalcGui(GuiController c) {
        initDone = false;
        this.gcont = c;
        frame = new JFrame(BASE_TITLE);
        frame.setSize(SIZE);
        frame.setMinimumSize(new Dimension(800, 400));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        sectionTab = new JTabbedPane();
        sectionTab.addChangeListener(new ChangeListener() {

            private int previous = -1;

            @Override
            public void stateChanged(ChangeEvent arg0) {
                int index = sectionTab.getSelectedIndex();
                if (index == -1) {
                    previous = index;
                    return;
                }
                //If the previous is not set, then don't do anything but update it.
                if (previous == -1) {
                    previous = index;
                } else {
                    tabs.get(previous).setNew(false);
                    previous = index;
                }
                tabs.get(index).setNew(false);

            }
        });
        sectionTab.setPreferredSize(new Dimension(1400, 900));
        frame.add(sectionTab, BorderLayout.CENTER);
        sectionToPanel = new HashMap<Section, TabPanel>();
        frame.setJMenuBar(createMenu());
        //Add the window listener so we can try to save before I exit.
        frame.addWindowListener(this);
    }


    private JMenuBar createMenu() {
        JMenuBar jmb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                save();
            }
        });
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        save.setMnemonic(KeyEvent.VK_S);
        file.add(save);
        JMenuItem saveAs = new JMenuItem("Save As");
        saveAs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveAs();
            }
        });
        file.add(saveAs);
        file.add(new JSeparator());
        JMenuItem load = new JMenuItem("Open");
        load.setMnemonic(KeyEvent.VK_O);
        load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        load.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                load();
            }
        });
        file.add(load);
        file.add(new JSeparator());
        JMenuItem newItem = new JMenuItem("New");
        newItem.setMnemonic(KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                newProperty();
            }
        });
        file.add(newItem);
        file.add(new JSeparator());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                exit();
            }
        });
        file.add(exit);
        jmb.add(file);

        JMenu run = new JMenu("Run");
        JMenuItem runItem = new JMenuItem("Run");
        runItem.setMnemonic(KeyEvent.VK_R);
        runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
        runItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                run();
            }
        });
        run.add(runItem);
        jmb.add(run);
        return jmb;
    }

    private void save() {
        gcont.saveProperties();
    }

    private void saveAs() {
        gcont.savePropertiesAs();
    }

    private void load() {
        gcont.loadProperties();
    }

    private void run() {
        gcont.runPCalc();
    }

    private void newProperty() {
        gcont.createNewProperties();
    }

    private void exit() {
        gcont.exit();
    }

    @Override
    public File getFile(FileNameExtensionFilter f, String txt, File current) {
        JFileChooser chooser = new JFileChooser(txt);
        chooser.setCurrentDirectory(current);
        chooser.setFileFilter(f);
        File fi;
        int ret = chooser.showDialog(frame, txt);
        if (ret != JFileChooser.APPROVE_OPTION)
            return null;
        fi = chooser.getSelectedFile();
        return fi;
    }

    @Override
    public void setVisible(boolean visible) {
        SwingUtilities.invokeLater(new FrameSetter(visible));
    }

    /**
     * Runs the given Runnable instance on the EDT thread.
     * If the calling thread is the EDT thread then it is run immediately.
     * If the calling thread is not the EDT thread then {@link SwingUtilities#invokeAndWait(Runnable)} is called.
     *
     * @param r - The Runnable instance to run on the EDT thread.
     */
    private void runOnSwing(Runnable r) {
        if (SwingUtilities.isEventDispatchThread() || !initDone) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addSection(Section s) {
        runOnSwing(new SectionAdder(s));
        update();
    }

    @Override
    public void addSubsection(Subsection sub) {
        runOnSwing(new SubsectionAdder(sub));
        update();
    }

    @Override
    public void removeSubsection(Subsection sub) {
        runOnSwing(new SubsectionRemover(sub));
        update();
    }

    private void update() {
        if (frame.isVisible())
            runOnSwing(new Repainter());
    }

    @Override
    public void showErrorMessage(String txt) {
        JOptionPane.showMessageDialog(frame, txt, "PCalc says:", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public int promptUser(String txt) {
        return JOptionPane.showConfirmDialog(frame, txt, "PCalc is wondering:", JOptionPane.YES_NO_CANCEL_OPTION);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exit();
    }

    @Override
    public void setTitleFileIdentifier(String path) {
        if (path == null) {
            frame.setTitle(BASE_TITLE);
        } else {
            frame.setTitle(BASE_TITLE + " - " + path);
        }
    }

    @Override
    public void addInputDevice(InputDevice id, Subsection sub) {
        runOnSwing(new InputAdder(id.getGuiComponent(), sub));
    }

    @Override
    public void removeInputDevice(InputDevice id, Subsection sub) {
        runOnSwing(new InputRemover(id.getGuiComponent(), sub));
    }

    @Override
    public void removeSection(Section s) {
        runOnSwing(new SectionRemover(s));
    }

    @Override
    public void initDone() {
        initDone = true;
    }

    @Override
    public void setInfoDescription(String txt) {
        if (txt == null) {
            throw new NullPointerException("Info Description is null.");
        }
        JMenu m = new JMenu("Info");
        JTextArea text = new JTextArea(txt);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
        JScrollPane jsp = new JScrollPane(text);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setWheelScrollingEnabled(true);
        jsp.setPreferredSize(new Dimension(600, 300));
        m.add(jsp);
        JMenuBar jmb = frame.getJMenuBar();
        jmb.add(m);

    }

}
