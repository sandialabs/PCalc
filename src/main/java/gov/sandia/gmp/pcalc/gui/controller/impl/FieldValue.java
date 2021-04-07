/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *  BSD Open Source License.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *  4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc.gui.controller.impl;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.InputType;

/**
 * A model for each property in the configuration file.
 */
public class FieldValue {

    /**
     * The Field for this object to hold and manage a value for.
     */
    private final Field f;

    /**
     * The current value of this property.
     */
    private String value;

    private int hash;

    /**
     * Creates a PropertyField from a Field.
     *
     * @param f - The Field to match.
     */
    public FieldValue(Field f) {
        this.f = f;
        value = f.getDef();
        hash = f.hashCode();
    }

    /**
     * Returns the value of this property.  The value is trimmed before the following considerations, and
     * return.  If the current value is the empty string ({@literal ""}),
     * then the default value is returned.  This default value can be {@code null}, meaning that this value
     * is not set.
     *
     * @return The String form of the value of this Property.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The Field who's value this is.
     */
    public Field getField() {
        return f;
    }

    /**
     * Sets the value of this Property to the given value.
     *
     * @param val - The new value for this Property.  If this value is {@code null}, then it will be set to this
     *            PropertyField's default value.
     * @throws Exception - If {@code val} is an invalid value for this FieldValue.
     */
    public void setValue(String val) throws Exception {
        String v;
        if (val != null && val.trim().equals("")) {
            val = null;
        }
        if (val == null) {
            //Checkboxes shouldn't reset to default, because "" is meaningful.
            //Booleans can't be null, they should always have a default value.
            if (f.getType().equals(InputType.CHECKBOX))
                v = null;
            else
                v = f.getDef();
        } else {
            v = val;
        }
        if (!checkRestrictions(v)) {
            throw new IllegalArgumentException("For the property " + f.getName() + " the value " + v +
                    " does not fit in the restrictions " + f.getRestrictions());
        }
        if (v != null)
            f.getType().validate(v);
        value = v;
    }

    /**
     * If the given property has its restriction field set, then this
     * method will check if the value to be set for that Property follows its restrictions.
     */
    private boolean checkRestrictions(String v) {
        if (v == null)
            return true;
        //If there are no restrictions then its valid!
        if (f.getRestrictions() == null) {
            return true;
        }
        //Check to make sure it is in the restrictions
        if (f.getType().equals(InputType.CHECKBOX) || f.getType().equals(InputType.BOOL))
            return checkCheckbox(v);
        return checkNormal(v);
    }

    private boolean checkNormal(String v) {
        if (!f.getRestrictions().contains(v))
            return false;
        return true;
    }

    private boolean checkCheckbox(String v) {
        v = v.replaceAll(",", " ");
        String[] vals = v.split(" ");
        for (String s : vals) {
            if (s.equals("")) {
                continue;
            }
            if (!f.getRestrictions().contains(s)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Field: " + f + " value: " + value;
    }

    @Override
    public boolean equals(Object o) {
        FieldValue fv;
        if (o instanceof FieldValue) {
            fv = (FieldValue) o;
            return f.equals(fv.f) && value.equals(fv.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Queries this FieldValue to ask if it contains the given String.
     * Either the value of this FieldValue will equal the String, or this
     * FieldValue has a set of values, and the String is included in that.
     * <br /><br />
     * If this FieldValue is for a Field of type {@code CHECKBOX} and the current
     * value is {@literal "one two three"}, then {@code hasValue("one")} will return {@code true},
     * where {@code hasValue("one two")} will return {@code false}.  Essentially, {@literal "one"}
     * is a single value, and included in the list {@literal ["one" "two" "three"]}.  {@literal "one two"}
     * is also a single value, and is not in the list.
     *
     * @param s - The String to look for.
     * @return If s is included in this FieldValue's set of values.
     */
    public boolean hasValue(String s) {
        if (value == null) {
            return s == null;
        }
        if (f.getType().equals(InputType.CHECKBOX)) {
            s = s.replaceAll(",", " ");
            String[] vals = s.split(" ");
            for (String str : vals) {
                if (s.equals("")) {
                    continue;
                }
                if (str.equals(s)) {
                    return true;
                }
            }
            return false;
        }
        return value.equals(s);
    }

}
