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
import gov.sandia.gmp.pcalc.gui.common.InputType;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import java.util.List;

/**
 * Abstraction of InputDevice implementations.
 */
public abstract class AbstractInputDevice implements InputDevice {

    /**
     * The list of Fields this InputDevice manages.
     */
    protected final ImmutableList<Field> fields;

    /**
     * The delegate to this InputDevice.
     */
    protected final InputDeviceDelegate del;

    /**
     * Creates an AbstractInputDevice.  This Constructor will set the {@code fields} and {@code del}
     * protected values and call {@link InputDeviceDelegate#registerProperties(ImmutableList)} with the
     * given list of fields.  It will also check the provided Fields with a list of accepted types provided
     * by implementations of this class.  Calling the registration method again could cause problems.  Only this
     * Constructor should call that method.
     *
     * @param fs  - The list of Fields this InputDevice will manage.
     * @param idd - The delegate for this InputDevice.
     * @throws IllegalArgumentException If a Field in {@code fs} is not
     *                                  found in the list of accepted types.
     */
    public AbstractInputDevice(ImmutableList<Field> fs, InputDeviceDelegate idd) {
        this.del = idd;
        fields = fs;
        idd.registerDevice(this);
        List<InputType> types = getAcceptedTypes();
        for (Field f : fs) {
            if (!types.contains(f.getType())) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + ", Type: " + f.getType() + " not allowed.  Acceptable types: " + types);
            }
        }
    }

    /**
     * Returns a list of the InputTypes this InputDevice accepts.  For instance a
     * device that manages boolean input would have the value {@link InputType#BOOL} as
     * the only element in the list.
     *
     * @return A list of accepted InputTypes.
     */
    protected abstract ImmutableList<InputType> getAcceptedTypes();

    @Override
    public ImmutableList<Field> getControlledFields() {
        return fields;
    }

}
