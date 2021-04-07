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
package gov.sandia.gmp.pcalc.gui.controller.impl.dependency;

import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.controller.impl.FieldValue;

import java.util.List;

/**
 * Defines the types of logic to use when trying to figure out if this dependency is satisfied or not.
 */
public enum DepLogic {
    /**
     * All PropValPairs must be satisfied.
     */
    AND {
        @Override
        public boolean isSatisfied(List<StringBinding<FieldValue>> sbs) {
            boolean ret = true;
            for (StringBinding<FieldValue> sb : sbs) {
                ret &= sb.getObj().hasValue(sb.getValue());
            }
            return ret;
        }

        @Override
        public boolean compute(boolean b1, boolean b2) {
            return b1 && b2;
        }

        @Override
        public String getString() {
            return "&&";
        }
    },
    /**
     * Any PropValPairs can be satisfied.
     */
    OR {
        @Override
        public boolean isSatisfied(List<StringBinding<FieldValue>> sbs) {
            boolean ret = false;
            for (StringBinding<FieldValue> sb : sbs) {
                ret |= sb.getObj().hasValue(sb.getValue());
            }
            return ret;
        }

        @Override
        public boolean compute(boolean b1, boolean b2) {
            return b1 || b2;
        }

        @Override
        public String getString() {
            return "||";
        }
    },
    /**
     * Only 1 PropValPair can be satisfied.
     */
    XOR {
        @Override
        public boolean isSatisfied(List<StringBinding<FieldValue>> sbs) {
            boolean ret = false;
            for (StringBinding<FieldValue> sb : sbs) {
                ret ^= sb.getObj().hasValue(sb.getValue());
            }
            return ret;
        }

        @Override
        public boolean compute(boolean b1, boolean b2) {
            return b1 ^ b2;
        }

        @Override
        public String getString() {
            return "^";
        }
    },

    /**
     * A PropValPair must be unsatisfied.
     */
    NAND {
        @Override
        public boolean isSatisfied(List<StringBinding<FieldValue>> sbs) {
            boolean ret = true;
            for (StringBinding<FieldValue> sb : sbs) {
                ret &= !sb.getObj().hasValue(sb.getValue());
            }
            return ret;
        }

        @Override
        public boolean compute(boolean b1, boolean b2) {
            return (!b1) && (!b2);
        }

        @Override
        public String getString() {
            return "!&&";
        }
    },

    /**
     * All PropValPairs must be unsatisfied.
     */
    NOR {
        @Override
        public boolean isSatisfied(List<StringBinding<FieldValue>> sbs) {
            boolean ret = false;
            for (StringBinding<FieldValue> sb : sbs) {
                ret |= !sb.getObj().hasValue(sb.getValue());
            }
            return ret;
        }

        @Override
        public boolean compute(boolean b1, boolean b2) {
            return (!b1) || (!b2);
        }

        @Override
        public String getString() {
            return "!||";
        }
    },

    /**
     * All but 1 PropValPair must be unsatisfied.
     */
    XNOR {
        @Override
        public boolean isSatisfied(List<StringBinding<FieldValue>> sbs) {
            boolean ret = false;
            for (StringBinding<FieldValue> sb : sbs) {
                ret ^= !sb.getObj().hasValue(sb.getValue());
            }
            return ret;
        }

        @Override
        public boolean compute(boolean b1, boolean b2) {
            return (!b1) ^ (!b2);
        }

        @Override
        public String getString() {
            return "!^";
        }
    };

    /**
     * Returns whether or not the given list of PropValPairs satisfies this Dependency Logic type.
     *
     * @param fvs - The list to decide on.
     * @return Whether or not this DepLogic is happy with {@code prs}.
     */
    public abstract boolean isSatisfied(List<StringBinding<FieldValue>> sbs);

    /**
     * Computes the outcome of the two input values.
     *
     * @param b1 - The first input.
     * @param b2 - The second input.
     * @return Based on this logic type, {@code true or false}.
     */
    public abstract boolean compute(boolean b1, boolean b2);

    /**
     * Returns the string to be used for the toString method.
     *
     * @return the string to be used for the toString method.
     */
    public abstract String getString();

    @Override
    public String toString() {
        return getString();
    }
}