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
import gov.sandia.gmp.pcalc.gui.files.FileManager;
import gov.sandia.gmp.pcalc.gui.inputDevice.listeners.FieldListener;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import java.util.List;

/**
 * Delegate for InputDevices.
 */
public interface InputDeviceDelegate {

    /**
     * Attempts to set the value of the Field identified by {@code f} to the value {@code val}.
     *
     * @param origin  - The InputDevice setting the Field.
     * @param f       - The Property name.
     * @param val     - The Property value.
     * @param showErr - Flag to indicate whether or not the controller will alert the user to invalid values.
     * @return If the set was a success, meaning the value was accepted by the field without issue.
     */
    public boolean setField(InputDevice origin, Field f, String val, boolean showErr);

    /**
     * Registers the InputDevice with the InputDeviceDelegate, so the delegate knows what fields
     * are being managed.  This method should only be called by
     * {@link AbstractInputDevice#AbstractInputDevice(ImmutableList, InputDeviceDelegate)}.
     *
     * @param fs - The Fields to register.
     */
    public void registerDevice(InputDevice id);

    /**
     * Returns the value for the Field {@code f}.
     *
     * @param f - The Field to get the value for.
     * @return the value for the Field {@code f}.
     * @throws IllegalArgumentException If {@code f} is not found.
     */
    public String getValueFor(Field f);

    /**
     * Adds a FieldListener to a Field.
     *
     * @param f  - The Field to be listened to.
     * @param fl - The Field listener to call.
     */
    public void addFieldListener(Field f, FieldListener fl);

    /**
     * Removes a FieldListener from a Field.
     *
     * @param f  - The Field to remove the listener from.
     * @param fl - The Listener to remove.
     */
    public void removeFieldListener(Field f, FieldListener fl);

    /**
     * Works like {@link InputDeviceDelegate#setField(InputDevice, Field, String, boolean)}, except allows for
     * multiple fields to be set at once.
     */
    public boolean setFields(InputDevice origin, List<StringBinding<Field>> vals, boolean showErr);

    /**
     * Gets the configuration information for the given field.  The configuration information is found
     * in the config file for the current user.
     *
     * @param f - The Field to get configuration info for, or null if {@link FileManager#environmentIsValid()}
     *          is {@code false}.
     * @return A List of Strings where each element is one line of the information found for the given Field.
     */
    public ImmutableList<String> getConfigForField(Field f);

    /**
     * Adds a line to the configuration for the specified field.
     * Up to a maximum of 10 lines can be saved per Field.
     *
     * @param f    - The field to add a config line.
     * @param line - The line to add.
     */
    public void addConfigForField(Field f, String line);

}
