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
package gov.sandia.gmp.pcalc.gui.gui.cpanel;

import gov.sandia.gmp.pcalc.gui.util.Gbc;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * CollapsiblePanel contains two internal panels.  One of them is a
 * title bar and the other one is actual content.  The title bar has
 * an arrow that can be clicked to toggle whether or not the content
 * panel is displayed.  This provides a way to roll-up / roll-down
 * panels depending on their usefulness at any given time.  These
 * are a good way to prevent clutter in a GUI.
 * <p>
 * Modified heavily from code idea posted by Craig Wood at:
 * http://www.coderanch.com/t/341737/Swing-AWT-SWT-JFace/java/Expand-Collapse-Panels
 * Then modified by Brian Kraus based from Ben Hamlet's version.
 */
public class CollapsiblePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private boolean init;

    private JPanel mTitleBar;
    private JLabel mTitleBarLabel;
    private JPanel mContentPanel;

    private JLabel mFiller;

    private boolean mOpened;

    /**
     * Default constructor, just sets the label for the title bar.  The
     * content panel, when set, will be closed.
     *
     * @param aLabel use this text for the title portion of the panel
     */
    public CollapsiblePanel(String aLabel) {
        this(aLabel, false);
    }

    /**
     * Constructor that sets the text for the panel title bar
     * and whether or not it is opened.
     *
     * @param aLabel  use this text for the title portion of the panel
     * @param aOpened true if the content panel should start out open
     *                and false otherwise
     */
    public CollapsiblePanel(String aLabel, boolean aOpened) {
        // This object listens to it's own mouse events
        addMouseListener(new MouseAdapter() {
            /**
             * If the user clicked on the title bar icon that signifies opening/closing the content
             * pane then update both the title bar panel and the content panel
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if (Utilities.clickArea().contains(e.getPoint())) {
                    toggleTitleBar();
                    toggleContentPanel();
                    revalidateParent();
                }
            }
        });
        setLayout(new GridBagLayout());
        mOpened = aOpened;
        init = false;

        // Create the title bar
        mTitleBar = new JPanel(new FlowLayout(FlowLayout.LEADING));
        mTitleBar.setBackground(Utilities.background());
        mTitleBar.setPreferredSize(new Dimension(200, 25));

        // Add the image and label
        mTitleBarLabel = new JLabel(aLabel, currentIcon(), JLabel.LEFT);
        mTitleBar.add(mTitleBarLabel);

        // Create a blank content panel
        //mContentPanel = aContent;
        mContentPanel = new JPanel();
        mContentPanel.setLayout(new GridBagLayout());
        mContentPanel.setSize(1000, 200);
        Gbc gb = new Gbc();
        gb.fill = Gbc.BOTH;
        gb.weightx = 1.0;
        gb.weighty = 1.0;
        gb.gridx = 0;
        gb.gridy = 1;
        gb.anchor = Gbc.LINE_START;
        add(mContentPanel, gb);

        // Add in the label and content panels with a line around them
        setBorder(new TitledBorder(""));
        Gbc gbc = Constraints.internalPanelNoBorder();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(mTitleBar, gbc);

        // Filler to force the actual contents to the top, only used when the
        // content panel is hidden
        mFiller = new JLabel("");
        mFiller.setVisible(false);
        add(mFiller, Constraints.fillAllNoInsets());

        // Show / hide based on input
        if (!mOpened) {
            mContentPanel.setVisible(false);
            mFiller.setVisible(true);
        }
        setRequestFocusEnabled(true);
        init = true;
    }

    /**
     * @param aLabel update the title bar to have this text for the label
     */
    public void setLabelText(String aLabel) {
        mTitleBarLabel.setText(aLabel);
    }

    @Override
    public Component add(Component c) {
        if (init) {
            mContentPanel.add(c, Constraints.internalEOLItemGBC());
            return c;
        }
        return super.add(c);
    }

    @Override
    public void add(Component c, Object constraints) {
        if (init) {
            mContentPanel.add(c, constraints);
        } else {
            super.add(c, constraints);
        }
    }

    @Override
    public void remove(Component c) {
        if (init) {
            mContentPanel.remove(c);
        } else {
            super.remove(c);
        }
    }

    /**
     * Toggle the title bar icon between the opened and closed states whenever
     * it is clicked
     */
    public void toggleTitleBar() {
        mOpened = !mOpened;
        mTitleBarLabel.setIcon(currentIcon());
        repaint();
    }

    /**
     * Show or hide the content panel, whichever is opposite of it's current state
     */
    private void toggleContentPanel() {
        boolean lShowing = mContentPanel.isShowing();
        mContentPanel.setVisible(!lShowing);
        mFiller.setVisible(lShowing);
        getParent().validate();
    }

    /**
     * @return current title bar icon.  This changes depending on if the content
     * panel is visible or not.
     */
    private ImageIcon currentIcon() {
        return mOpened ? Utilities.openIcon() : Utilities.closedIcon();
    }

    /**
     * Forces validate() on the top level parent component of this CollapsiblePanel.
     * This is needed to prevent resizing weirdness from occurring when these panels
     * are collapsed and opened.
     */
    private void revalidateParent() {
        Container lParent = getParent();
        while (lParent.getParent() != null)
            lParent = lParent.getParent();
        lParent.validate();
    }

    /**
     * Helper class for the collabsible panel.  Includes some
     * static methods that keep the GUI components drawing the
     * same and prevents duplicated Icons.
     */
    private static class Utilities {

        /**
         * @return background color for CollapsiblePanel title bars
         */
        public static Color background() {
            return new Color(220, 220, 220);
        }

        /**
         * @return clickable region within CollapsiblePanel title bars
         */
        public static Rectangle clickArea() {
            if (mClickArea == null) {
                mClickArea = new Rectangle(1, 5, w + 7, h + 7);
            }

            return mClickArea;
        }

        /**
         * @return icon to use for CollapsiblePanels with opened content panels
         */
        public static ImageIcon openIcon() {

            if (mOpenIcon == null) {
                // Create an image for drawing the icon
                BufferedImage lOpenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = lOpenImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill the image background the same as the title bar
                g2.setPaint(background());
                g2.fillRect(0, 0, w, h);

                // Open (down facing triangle) points
                int[] x = {0, w / 2, w};
                int[] y = {0, (int) (h * (3.0 / 4.0)), 0};
                Polygon p = new Polygon(x, y, 3);

                // Draw a filled triangle with the base color
                g2.setPaint(Color.LIGHT_GRAY);
                g2.fill(p);

                // ...then outline with black
                g2.setPaint(Color.BLACK);
                g2.draw(p);
                g2.dispose();

                // Store the icon
                mOpenIcon = new ImageIcon(lOpenImage);
            }

            return mOpenIcon;
        }

        /**
         * @return icon to use for CollapsiblePanels with closed content panels
         */
        public static ImageIcon closedIcon() {
            if (mClosedIcon == null) {
                // Create an image for drawing the icon
                BufferedImage lClosedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = lClosedImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill the image background the same as the title bar
                g2.setPaint(background());
                g2.fillRect(0, 0, w, h);

                // Closed (right facing triangle) points
                int[] x = {(int) (h * (1.0 / 4.0)), h, (int) (h * (1.0 / 4.0))};
                int[] y = {0, w / 2, w};
                Polygon p = new Polygon(x, y, 3);

                // Draw a filled triangle with the base color
                g2.setPaint(Color.LIGHT_GRAY);
                g2.fill(p);

                // ...then outline with black
                g2.setPaint(Color.BLACK);
                g2.draw(p);
                g2.dispose();

                // Store the icon
                mClosedIcon = new ImageIcon(lClosedImage);
            }

            return mClosedIcon;
        }

        private static int w = 10;
        private static int h = 10;

        private static ImageIcon mOpenIcon = null;
        private static ImageIcon mClosedIcon = null;
        private static Rectangle mClickArea = null;
    }
}