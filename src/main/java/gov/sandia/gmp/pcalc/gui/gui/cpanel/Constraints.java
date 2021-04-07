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

import java.awt.*;

/**
 * Constraints contains helper functions for creating constraints for
 * grid bag layouts.  This code has been further modified by Brian Kraus.
 */
public class Constraints {

    private Constraints() {
    }

    /**
     * @return constraint that fills all remaining vertical space
     */
    public static Gbc fillerGBC() {
        Gbc lGBC = new Gbc();
        lGBC.gridheight = Gbc.REMAINDER;
        lGBC.weighty = 1.0;
        return lGBC;
    }

    /**
     * @return constraint that fills all remaining horizontal space
     */
    public static Gbc fillerHorizontalGBC() {
        Gbc lGBC = new Gbc();
        lGBC.gridwidth = Gbc.REMAINDER;
        lGBC.weightx = 1.0;
        return lGBC;
    }

    /**
     * @return use this constraint for labels that should be inset from
     * the top left of a panel
     */
    public static Gbc firstLabelGBC() {

        Gbc lGBC = new Gbc();
        lGBC.gridwidth = 1;
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.insets = new Insets(15, 5, 0, 0);
        lGBC.anchor = Gbc.FIRST_LINE_START;

        return lGBC;
    }

    /**
     * @return use this constraint for items that should be inset from
     * the top left of a panel
     */
    public static Gbc firstItemGBC() {
        Gbc lGBC;
        lGBC = new Gbc();
        lGBC.gridwidth = Gbc.REMAINDER;
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.weightx = 1.0;
        lGBC.insets = new Insets(15, 10, 0, 10);
        lGBC.anchor = Gbc.FIRST_LINE_START;
        return lGBC;
    }

    /**
     * @return constraint to use for labels that are not at the end of a line
     */
    public static Gbc internalLabelGBC() {
        Gbc lGBC = new Gbc();
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.insets = new Insets(5, 5, 0, 0);
        lGBC.anchor = Gbc.LINE_START;

        return lGBC;
    }

    /**
     * @return constraint to use for labels that are at the end of a line
     */
    public static Gbc internalEOLLabelGBC() {
        Gbc lGBC = internalLabelGBC();
        lGBC.gridwidth = Gbc.REMAINDER;
        lGBC.weightx = 1.0;

        return lGBC;
    }

    /**
     * @return constraint to use for items that are not at the end of a line
     */
    public static Gbc internalItemGBC() {
        Gbc lGBC;
        lGBC = new Gbc();
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.weightx = 1.0;
        lGBC.insets = new Insets(5, 10, 0, 10);
        lGBC.anchor = Gbc.LINE_START;

        return lGBC;
    }

    /**
     * @return constraint to use for labels that are at the end of a line
     */
    public static Gbc internalEOLItemGBC() {
        Gbc lGBC = internalItemGBC();
        lGBC.gridwidth = Gbc.REMAINDER;
        return lGBC;
    }

    /**
     * @return constraint to use for items that are not at the end of a line
     * and that should not stretch horizontally
     */
    public static Gbc internalItemNoStretchGBC() {
        Gbc lGBC = internalItemGBC();
        lGBC.weightx = 0.0;
        return lGBC;
    }

    /**
     * @return constraint to use for buttons that are not at the end of a line
     */
    public static Gbc internalButtonGBC() {
        Gbc lGBC;
        lGBC = new Gbc();
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.weightx = 0.0;
        lGBC.insets = new Insets(5, 10, 0, 10);
        lGBC.anchor = Gbc.LINE_START;

        return lGBC;
    }

    /**
     * @return constraint to use for buttons that are at the end of a line
     */
    public static Gbc internalEOLButtonGBC() {
        Gbc lGBC = internalButtonGBC();
        lGBC.gridwidth = Gbc.REMAINDER;
        return lGBC;
    }

    /**
     * @return constraint to use for panels that are nested inside other panels
     */
    public static Gbc internalPanelGBC() {
        Gbc lGBC;
        lGBC = new Gbc();
        lGBC.gridwidth = Gbc.REMAINDER;
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.weightx = 1.0;
        lGBC.insets = new Insets(15, 10, 0, 10);
        lGBC.anchor = Gbc.FIRST_LINE_START;

        return lGBC;
    }

    /**
     * @return constraint to use for panels that are nested inside other panels.
     * Has less inset that the regular internalPanelGBC()
     */
    public static Gbc internalPanelRegularSpacingGBC() {
        Gbc lGBC = internalPanelGBC();
        lGBC.insets = new Insets(5, 10, 0, 10);
        return lGBC;
    }

    /**
     * @return constraint to use for panels that are nested inside other panels,
     * but with 0 inset on all edges
     */
    public static Gbc internalPanelNoBorder() {
        Gbc lGBC = internalPanelGBC();
        lGBC.insets = new Insets(0, 0, 0, 0);
        return lGBC;
    }

    /**
     * @return constraint to use for filling all remaining space
     */
    public static Gbc fillAll() {
        Gbc lGBC = internalPanelGBC();
        lGBC.gridheight = 1;
        lGBC.fill = Gbc.BOTH;
        lGBC.weighty = 1.0;

        return lGBC;
    }

    /**
     * @return constraint to use for filling all remaining space
     */
    public static Gbc fillAllVerticalOnly() {
        Gbc lGBC = internalPanelGBC();
        lGBC.gridheight = 1;
        lGBC.fill = Gbc.VERTICAL;
        lGBC.weighty = 1.0;
        lGBC.weightx = 0.0;
        return lGBC;
    }

    /**
     * @return constraint to use for filling all remaining space while allowing
     * no room for insets
     */
    public static Gbc fillAllNoInsets() {
        Gbc lGBC = internalPanelGBC();
        lGBC.gridheight = 1;//Gbc.REMAINDER;
        lGBC.fill = Gbc.BOTH;
        lGBC.weighty = 1.0;
        lGBC.insets = new Insets(0, 0, 0, 0);

        return lGBC;
    }

    /**
     * @return constraints for filling all remaining space but with some space allowed on the left
     */
    public static Gbc fillAllNoBorderGBC() {
        Gbc lGBC = Constraints.fillAll();
        lGBC.insets = new Insets(0, 10, 0, 0);
        lGBC.anchor = Gbc.LINE_START;

        return lGBC;
    }

    /**
     * @return constraints for the left panel in a pair of panels arranged as two columns
     * that should occupy the same amount of vertical space and evenly split the horizontal space
     */
    public static Gbc leftPanelGBC() {
        Gbc lGBC = new Gbc();
        lGBC.gridwidth = 1;
        lGBC.gridheight = 1;
        lGBC.fill = Gbc.BOTH;
        lGBC.weightx = 1.0;
        lGBC.weighty = 1.0;
        lGBC.insets = new Insets(5, 10, 10, 10);
        lGBC.anchor = Gbc.LINE_START;
        return lGBC;
    }

    /**
     * @return constraints for the right panel in a pair of panels arranged as two columns
     * that should occupy the same amount of vertical space and evenly split the horizontal space
     */
    public static Gbc rightPanelGBC() {
        Gbc lGBC = leftPanelGBC();
        lGBC.gridwidth = Gbc.REMAINDER;
        return lGBC;
    }

    /**
     * @return constraint to use for horizontal separators
     */
    public static Gbc separator() {
        Gbc lGBC;
        lGBC = new Gbc();
        lGBC.gridwidth = Gbc.REMAINDER;
        lGBC.fill = Gbc.HORIZONTAL;
        lGBC.weightx = 1.0;
        lGBC.insets = new Insets(15, 10, 10, 10);
        lGBC.anchor = Gbc.LINE_START;
        return lGBC;
    }

    /**
     * @return Constraints for the name part of a FieldPanel.
     */
    public static Gbc nameConstraints() {
        Gbc gbc = new Gbc();
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.gridwidth = Gbc.REMAINDER;
        gbc.fill = Gbc.HORIZONTAL;
        gbc.anchor = Gbc.FIRST_LINE_START;
        return gbc;
    }

    /**
     * @return Constraints for input Fields for the PCalc gui.
     */
    public static Gbc inputFieldConstraints() {
        Gbc gbc = new Gbc();
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.fill = Gbc.HORIZONTAL;
        gbc.anchor = Gbc.LINE_START;
        gbc.gridwidth = Gbc.REMAINDER;
        return gbc;
    }
}
