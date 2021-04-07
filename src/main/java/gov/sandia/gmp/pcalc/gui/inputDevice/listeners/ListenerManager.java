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
package gov.sandia.gmp.pcalc.gui.inputDevice.listeners;

import gov.sandia.gmp.pcalc.gui.common.Field;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Manages Field Listeners for any given number of Fields.
 */
public class ListenerManager {


    private Map<Field, List<FieldListener>> fieldLs;

    /**
     * Creates a ListenerManager that has no managed listeners.
     */
    public ListenerManager() {
        fieldLs = new HashMap<Field, List<FieldListener>>();
    }

    /**
     * Adds a FieldListener to a Field.
     *
     * @param f  - The Field to be listened to.
     * @param fl - The Field listener to call.
     */
    public void addFieldListener(Field f, FieldListener fl) {
        List<FieldListener> lf = fieldLs.get(f);
        if (lf == null) {
            lf = new LinkedList<FieldListener>();
        }
        if (!lf.contains(fl)) {
            lf.add(fl);
        }
        fieldLs.put(f, lf);
    }

    /**
     * Removes a FieldListener from a Field.
     *
     * @param f  - The Field to remove the listener from.
     * @param fl - The Listener to remove.
     */
    public void removeFieldListener(Field f, FieldListener fl) {
        List<FieldListener> lf = fieldLs.get(f);
        if (lf == null) {
            return;
        }
        lf.remove(fl);
        fieldLs.put(f, lf);
    }


    /**
     * Fires all FieldListeners for the given Field, which should have
     * just changed values to {@code value}.
     *
     * @param f     - The Field that has changed.
     * @param value - The value it is changing to.
     */
    public void fireFieldListeners(Field f, String value) {
        List<FieldListener> lf = fieldLs.get(f);
        if (lf == null) {
            return;
        }
        for (FieldListener fl : lf) {
            fl.fieldChanged(f, value);
        }
    }


}
