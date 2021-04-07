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
 * Field implementation.
 */
public class FieldImpl implements Field {

    private final String name;
    private final InputType type;
    private final ImmutableList<String> rest;
    private final String sNotes, lNotes;
    private final String def;
    private final int hash;

    /**
     * Creates an instance of a Field, which is a description of an input value.
     *
     * @param name   - The name of the input.
     * @param sNotes - Any sort of description or hints for the input.  {@code null} is acceptable, meaning
     *               there are no notes.
     * @param type   - The type of input.
     * @param rest   - A list of restrictions the values must adhere to.  {@code null} is acceptable, meaning
     *               there are no restrictions.
     * @param def    - A default value for this Field, can be {@code null} meaning that no value is acceptable.
     * @throws NullPointerException If {@code name} or {@code type} are {@code null}.
     */
    public FieldImpl(String name, String sNotes, String lNotes, InputType type, List<String> rest, String def) throws NullPointerException {
        this.name = name;
        this.sNotes = sNotes;
        this.lNotes = lNotes;
        this.type = type;
        if (rest != null)
            this.rest = new ImmutableList<String>(rest);
        else
            this.rest = null;
        this.def = def;
        if (name == null || type == null) {
            throw new NullPointerException();
        }
        hash = makeHash();
    }

    private int makeHash() {
        int hashC = name.hashCode();
        if (sNotes != null) {
            hashC ^= sNotes.hashCode();
        }
        hashC |= type.hashCode();
        if (def != null) {
            hashC ^= def.hashCode();
        }
        if (rest != null) {
            hashC *= rest.hashCode();
        }
        return hashC;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputType getType() {
        return type;
    }

    @Override
    public ImmutableList<String> getRestrictions() {
        return rest;
    }

    @Override
    public String getLongNotes() {
        return lNotes;
    }

    @Override
    public String getDef() {
        return def;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Field))
            return false;

        Field f = (Field) o;
        boolean ret = name.equals(f.getName());
        if (sNotes == null) {
            ret &= (f.getShortNotes() == null);
        } else {
            ret &= sNotes.equals(f.getShortNotes());
        }
        ret &= type.equals(f.getType());
        //Only test the lists if this is true so far.
        if (ret) {
            if (rest != null)
                return rest.equals(f.getRestrictions());
            return ret &= (f.getRestrictions() == null);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String getShortNotes() {
        return sNotes;
    }

}
