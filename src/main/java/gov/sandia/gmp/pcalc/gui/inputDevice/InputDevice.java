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
package gov.sandia.gmp.pcalc.gui.inputDevice;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import java.awt.*;
import java.util.List;

/**
 * This interface describes an InputDevice, which is a part of the PCalc
 * Gui that takes a list of Fields for it to create a graphical representation
 * for how to input values for those Fields.  The user will interact directly with
 * the PCalc Gui, which consists of one or more of these InputDevices for various
 * Fields.
 */
public interface InputDevice {

    /**
     * Each input device must have a graphical representation
     * of how the user can use this device to input values.
     *
     * @return The JPanel for this input device.
     */
    public Component getGuiComponent();


    /**
     * Each InputDevice must be able to configure the graphical
     * portion of itself such that the controlled fields are correctly
     * represented with their current values.  It is also important
     * that if {@link #getValues()} is called immediately after,
     * equivalent bindings between the Fields and their values and the input
     * to this method be returned.<br /><br />
     * For example:  If this InputDevice manages only one Field, being
     * a boolean input.  When this method is called with that Field
     * bound to a string value indicating {@code true}, then before the
     * method returns the graphical element representing the Field must
     * indicate that its value is true.  If immediately after a call to this
     * method {@link #getValues()} is called, then the controlled boolean Field
     * is returned with the value indicating {@code true} bound to it.
     *
     * @param vals - The values to configure the graphical portion of this InputDevice to.
     * @throws IllegalArgumentException If the given Fields or values are not correct.  For example:
     *                                  <ul>
     *                                  <li>There are more Fields than this device controls.</li>
     *                                  <li>The values are not acceptable for the associated Fields.</li>
     *                                  <li>A Field does not match any of the controlled Field.</li>
     *                                  </ul>
     */
    public void setValue(List<StringBinding<Field>> vals);

    /**
     * @return A list of the Fields this InputDevice accepts input for.
     */
    public ImmutableList<Field> getControlledFields();

    /**
     * Returns a list of the controlled Fields and the input values for
     * each one.  The input values can come from one or both of two locations:<br /><br />
     * <ol>
     * <li>The user has made input through the graphical portion of this InputDevice.</li>
     * <li>A call to {@link #setValue(ImmutableList)}.</li>
     * </ol>
     *
     * @return The controlled Fields bound with their current value.
     */
    public ImmutableList<StringBinding<Field>> getValues();

}
