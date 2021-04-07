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
 * An implementation of Field that allows this Field to default to the value of another field.
 * When {@link #getDef()} is called, this class will query the delegate and return the
 * current value of another Field, specified upon construction.
 */
public class FieldDef implements Field {

    private final String name;
    private final InputType type;
    private final ImmutableList<String> rest;
    private final String sNotes, lNotes;
    private final Field def;
    private final InputDeviceDelegate del;
    private final int hash;

    /**
     * Creates an instance of FieldDef.  This instance's default value is the current value of another Field
     * <i>def</i>.
     *
     * @param name   - The name of this Field.
     * @param sNotes - Short notes for this Field.
     * @param lNotes - Long notes for this Field.
     * @param type   - The InputType for this Field.
     * @param rest   - A list of restriction for this Field.
     * @param def    - The Field who's current value will be used as this instance's default value.
     * @param del    - The delegate for this InputDevice.
     */
    public FieldDef(String name, String sNotes, String lNotes,
                    InputType type, List<String> rest, Field def, InputDeviceDelegate del) {
        this.name = name;
        this.sNotes = sNotes;
        this.lNotes = lNotes;
        this.type = type;
        this.rest = rest == null ? null : new ImmutableList<String>(rest);
        this.def = def;
        this.del = del;
        hash = makeHash();
    }

    private int makeHash() {
        int hashC = name.hashCode();
        if (sNotes != null) {
            hashC ^= sNotes.hashCode();
        }
        hashC |= type.hashCode();
        if (getDef() != null) {
            hashC ^= getDef().hashCode();
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
        return del.getValueFor(def);
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
