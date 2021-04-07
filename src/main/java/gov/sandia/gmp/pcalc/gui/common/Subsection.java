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
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDevice;

import java.util.Comparator;


/**
 * A Subsection is a an area of the gui that resides in a Section. All input devices with the same
 * specified Subsection will appear in that Subsection.
 * There can be multiple Subsections per Section.  No Subsection
 * can be displayed in multiple locations.
 */
public class Subsection {

    /**
     * The Section this Subsection resides in.
     */
    public final Section s;

    /**
     * The name of this Subsection.
     */
    public final String name;

    private final ShowTracker<InputDevice> children;
    private final int pos;

    /**
     * A comparator for sorting the Subsections by preferred position.
     */
    public static final SubsectionComparator S_COMP = new SubsectionComparator();

    /**
     * Creates a Subsection which represents a smaller are to place Fields than
     * a Section.
     *
     * @param parent - The Section to which this Subsection will reside.
     * @param name   - The name of this Subsection.
     * @param pos    - The index of the preferred position for this Subsection.  Smaller values being the top of
     *               the Section,
     *               and increasing towards the bottom.  If two Subsections have equal position, then their ordering relative
     *               to each other is undefined.
     */
    public Subsection(Section parent, String name, int pos) {
        s = parent;
        this.name = name;
        children = new ShowTracker<InputDevice>();
        this.pos = pos;
    }

    /**
     * Returns the preferred index of the position this Subsection wished to have.
     * Smaller values being the top of the Section, and increasing towards the bottom.
     * If two Subsections have equal position, then their ordering relative to each other is undefined.
     *
     * @return The preferred position this Subsection wants.
     */
    public int getPreferredPosition() {
        return pos;
    }

    @Override
    public int hashCode() {
        return s.hashCode() ^ name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        Subsection os;
        if (o instanceof Subsection) {
            os = (Subsection) o;
            return s.equals(os.s) && name.equals(os.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Adds a child to this Subsection.  The child is tracked so this Subsection can identify when it has no
     * children.
     *
     * @param d - The child to add.
     */
    public void addChild(InputDevice d) {
        children.setShowing(d, true);
    }

    /**
     * Removes a child from this Subsection.  The child is tracked so this Subsection can identify when it has no
     * children.
     *
     * @param d - The child to remove.
     */
    public void removeChild(InputDevice d) {
        children.setShowing(d, false);
    }

    /**
     * Returns {@code true} when this Subsection has no children showing (present).
     *
     * @return {@code true} when this Subsection has no children showing (present).
     */
    public boolean empty() {
        return children.empty();
    }

    private static final class SubsectionComparator implements Comparator<Subsection> {

        @Override
        public int compare(Subsection arg0, Subsection arg1) {
            return arg0.getPreferredPosition() - arg1.getPreferredPosition();
        }

    }

}
