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
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.controller.impl.dependency.Dependency;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.textBox.TextBoxDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.listeners.FieldListener;

/**
 * A TextBoxDevice that has the ability to disable itself given the state of a supplied Dependency.
 */
public class DisableableTextBoxDevice extends TextBoxDevice implements FieldListener {

    private final Dependency d;

    /**
     * Creates a DisableableTextBoxDevice, which is a TextBoxDevice that can disable itself
     * if a given dependency is not met.
     *
     * @param f   - The Field for this TextBoxDevice.
     * @param idd - The Delegate.
     * @param d   - The Dependency, which if satisfied will enable this device, otherwise this device
     *            will be disabled.
     */
    public DisableableTextBoxDevice(Field f, InputDeviceDelegate idd, Dependency d, boolean remembers) {
        super(f, idd, remembers);
        this.d = d;
        for (StringBinding<Field> sb : d.getParams()) {
            del.addFieldListener(sb.getObj(), this);
            fieldChanged(sb.getObj(), sb.getValue());
        }
    }

    @Override
    public void fieldChanged(Field changed, String val) {
        boolean b = d.isSatisfied();
        setEditable(b);
    }

}
