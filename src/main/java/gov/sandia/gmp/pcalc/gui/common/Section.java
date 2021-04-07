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
package gov.sandia.gmp.pcalc.gui.common;

import gov.sandia.gmp.pcalc.gui.controller.impl.ShowTracker;

import java.awt.event.KeyEvent;
import java.util.Comparator;

/**
 * The various sections in the configuration file.
 */
public enum Section {

    /**
     * The section for Predictors.
     */
    PREDICTORS("Predictors", KeyEvent.VK_P, 3),

    /**
     * The section to configure the input values.
     */
    INPUT("Input Parameters", KeyEvent.VK_I, 1),

    /**
     * The section to configure the output values.
     */
    OUTPUT("Output Parameters", KeyEvent.VK_O, 2),

    /**
     * The General section for general stuff.
     */
    GENERAL("General", KeyEvent.VK_G, 0),

    /**
     * The Section for the Model Query values.
     */
    MODEL_Q("Model Query", KeyEvent.VK_M, 3);
    //Model_Q and Predictors are never shown at the same time, so they can have the same position.

    private final String title;
    private final int pos;
    private final int mnKey;
    private final ShowTracker<Subsection> children;

    /**
     * Creates a Section.
     *
     * @param title       - The title for this Section.
     * @param mnemonicKey - A mnemonic key to be used in gui elements to select this Section.
     * @param pos         - A position for this Section to attempt to be in.  This correlates to the index of
     *                    a list.
     */
    private Section(String title, int mnemonicKey, int pos) {
        this.title = title;
        mnKey = mnemonicKey;
        children = new ShowTracker<Subsection>();
        this.pos = pos;
    }

    /**
     * Returns the preferred tab position this section should occupy in the tab pane.
     *
     * @return the preferred tab position this section should occupy in the tab pane.
     */
    public int getPreferredPosition() {
        return pos;
    }

    /**
     * Returns the title for this section.
     *
     * @return The title for this section.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The key to be used as the Mnemonic for this Section, or {@code KeyEvent.VK_UNDEFINED}.
     */
    public int getMnemonicKey() {
        return mnKey;
    }

    /**
     * Adds a child to this Section.  The child is tracked so this Section can identify when it has no
     * children.
     *
     * @param s - The child to add.
     */
    public void addChild(Subsection s) {
        children.setShowing(s, true);
    }

    /**
     * Removes a child from this Section.  The child is tracked so this Section can identify when it has no
     * children.
     *
     * @param s - The child to remove.
     */
    public void removeChild(Subsection s) {
        children.setShowing(s, false);
    }

    /**
     * Returns {@code true} when this Section has no children showing (present).
     *
     * @return {@code true} when this Section has no children showing (present).
     */
    public boolean empty() {
        return children.empty();
    }

    /**
     * A class to compare Sections in my own way.
     */
    public static final class SectionComparer implements Comparator<Section> {

        private SectionComparer() {
        }

        @Override
        public int compare(Section arg0, Section arg1) {
            return arg0.getPreferredPosition() - arg1.getPreferredPosition();
        }

    }

    /**
     * Instance of a comparator to use with Sections that compares by the preferred position.
     * {@link Section#getPreferredPosition()}
     */
    public static final SectionComparer COMP = new SectionComparer();

}
