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
import gov.sandia.gmp.pcalc.gui.inputDevice.AbstractInputDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * And implementation of an InputDevice, solely for the purpose of taking up a line of space.
 */
public class FillerDevice extends AbstractInputDevice {

    public static final int HEIGHT = 20;
    private final JPanel panel;

    /**
     * Creates a FillerDevice, which is an InputDevice made to take up a line of space.
     * It can also have a line through it, for nice definitions of boundaries.
     *
     * @param hRule - Whether or not this FillerDevice should have a horizontal rule in it.
     * @param idd   - The input device delegate, which is cannot be {@code null}.
     */
    public FillerDevice(boolean hRule, InputDeviceDelegate idd) {
        super(ImmutableList.<Field>emptyList(), idd);
        if (hRule)
            panel = new LinePanel();
        else
            panel = new JPanel();
        panel.setPreferredSize(new Dimension(0, HEIGHT));
    }

    @Override
    public Component getGuiComponent() {
        return panel;
    }

    @Override
    public void setValue(List<StringBinding<Field>> vals) {
    }

    @Override
    public ImmutableList<StringBinding<Field>> getValues() {
        return ImmutableList.<StringBinding<Field>>emptyList();
    }

    @Override
    protected ImmutableList<InputType> getAcceptedTypes() {
        return ImmutableList.<InputType>emptyList();
    }

    private class LinePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        public void paintComponent(Graphics g) {
            g.drawLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2);
        }
    }
}
