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
package gov.sandia.gmp.pcalc.gui.controller.impl;

import gov.sandia.gmp.pcalc.gui.common.Subsection;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDevice;

/**
 * FieldInsertion object, which is a correlation between a PropertyField and the subsection it belongs in
 */
public class InputInsertion {

    /**
     * The InputDevice.
     */
    public final InputDevice id;

    /**
     * The Subsection the InputDevice resides in.
     */
    public final Subsection sub;

    /**
     * Creates a FieldInsertion object, which is a correlation between a InputDevice and the subsection it belongs in.
     *
     * @param id  - The {@code InputDevice}.
     * @param sub - The {@code Subsection}.
     */
    public InputInsertion(InputDevice id, Subsection sub) {
        this.id = id;
        this.sub = sub;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InputInsertion))
            return false;
        InputInsertion fi = (InputInsertion) o;
        return id.equals(fi.id) && sub.equals(fi.sub);
    }

    @Override
    public int hashCode() {
        return id.hashCode() ^ sub.hashCode();
    }

    @Override
    public String toString() {
        return id + " in " + sub;
    }
}
