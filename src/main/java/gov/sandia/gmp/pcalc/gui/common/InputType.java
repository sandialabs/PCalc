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
package gov.sandia.gmp.pcalc.gui.common;

import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines the types of inputs the Gui will accept, and how they are inserted into the {@code PropertiesPlusGMP} object.
 */
public enum InputType {
    /**
     * The Integer type of input.
     *
     * @expects A single Integer value.
     */
    INT("An Integer.") {
        @Override
        public void validate(String s) throws Exception {
            Integer.parseInt(s);
        }
    },

    /**
     * The Double type of input.
     *
     * @expects A single Double value.
     */
    DOUBLE("A Double.") {
        @Override
        public void validate(String s) throws Exception {
            Double.parseDouble(s);
        }
    },

    /**
     * The File type of input.
     *
     * @expects A single Absolute File Path value.
     */
    FILE("A File Path.") {
        @Override
        public void validate(String s) throws Exception {
            //All Filenames are valid.
        }
    },

    /**
     * The Directory type of input
     *
     * @expects A single Absolute Directory Path value.
     */
    DIRECTORY("A Directory Path.") {
        @Override
        public void validate(String s)
                throws Exception {
            //All Directories are valid.
        }
    },

    /**
     * The String type of input.
     *
     * @expects A String.
     */
    STRING("Any String.") {
        @Override
        public void validate(String s) throws Exception {
            //All Strings are valid.
        }
    },

    /**
     * The Integer Array type of input.
     *
     * @expects A set of Integers with one space or comma character in between them, such as: "1, 234, 22, 9983".
     */
    INT_ARR("An list of Integers, delimited by ',' or ' '.") {
        @Override
        public void validate(String s) throws Exception {
            String[] vals = filterEmpty(s.replaceAll(",", " ").split(" "));
            for (int i = 0; i < vals.length; i++) {
                Integer.parseInt(vals[i]);
            }
        }
    },

    /**
     * The Bool type of input.
     *
     * @expects either "true" or "false", ignoring case.
     */
    BOOL("Either 'true' or 'false', ignoring case.") {
        @Override
        public void validate(String s)
                throws Exception {
            s = s.toLowerCase();
            if (!s.equals(TRUE_VAL) && !s.equals(FALSE_VAL)) {
                throw new IllegalArgumentException("Value: " + s + " is not \"true\" or \"false\".");
            }
        }
    },

    /**
     * The Checkbox type of input.  This input implies there will be a set of 1 or more Checkboxes (not radio buttons).
     *
     * @expects The values of all the checked boxes in a single string, with a space in between each or commas.
     * {@code (" " delimited, "," or ", " with 1 or more spaces.)}
     */
    CHECKBOX("The values of all the checked boxes in a single string, with a space in between each.") {
        @Override
        public void validate(String s)
                throws Exception {
            //Checkboxes can come in so many forms, that I can't really tell if it is valid or not.
        }
    },

    /**
     * Specifier for 2 doubles followed by one integer.  Such as "4.0 3.14 9".
     *
     * @expects A string with 2 double values followed by an int value, each separated by either a space or comma.
     */
    DOUBLE2_INT1("2 Doubles and 1 Integer, each separated by either a space or comma.") {
        @Override
        public void validate(String s)
                throws Exception {
            String[] vals = s.replaceAll(",", " ").split(" ");
            vals = filterEmpty(vals);
            if (vals.length != 3) {
                throw new IllegalArgumentException("Must have 3 arguments for 2 doubles and 1 int.");
            }
            Double.parseDouble(vals[0]);
            Double.parseDouble(vals[1]);
            Integer.parseInt(vals[2]);
        }
    },

    /**
     * Specified for an array of doubles.  Either space or comma delimited.
     *
     * @expects A String of consecutive double values separated by either comma or space.
     */
    DOUBLE_ARR("A String of consecutive double values separated by either comma or space.") {
        @Override
        public void validate(String s)
                throws Exception {
            String[] vals = s.replaceAll(",", " ").split(" ");
            vals = filterEmpty(vals);
            for (String ss : vals) {
                Double.parseDouble(ss);
            }
        }
    },

    /**
     * Specifier for 3 doubles.  Such as "4.0 3.14 9.7".
     *
     * @expects A string with 3 double values each separated by either a space or comma.
     */
    DOUBLE3("A string with 3 double values each separated by either a space or comma.") {
        @Override
        public void validate(String s)
                throws Exception {
            String[] vals = s.replaceAll(",", " ").split(" ");
            vals = filterEmpty(vals);
            if (vals.length != 3) {
                throw new IllegalArgumentException("Must have 3 arguments for 3 doubles.");
            }
            Double.parseDouble(vals[0]);
            Double.parseDouble(vals[1]);
            Double.parseDouble(vals[2]);
        }
    },

    /**
     * Specifier for 2 doubles.  Such as "4.0 9.7".
     *
     * @expects A string with 2 double values each separated by either a space or comma.
     */
    DOUBLE2("A string with 2 double values each separated by either a space or comma.") {
        @Override
        public void validate(String s)
                throws Exception {
            String[] vals = s.replaceAll(",", " ").split(" ");
            vals = filterEmpty(vals);
            if (vals.length != 2) {
                throw new IllegalArgumentException("Must have 2 arguments for 2 doubles.");
            }
            Double.parseDouble(vals[0]);
            Double.parseDouble(vals[1]);
        }
    },

    /**
     * Specifier for 1 double followed by 1 integer.  Such as "4.0 9".
     *
     * @expects A string with a double value followed by an int value, separated by either a space or comma.
     */
    DOUBLE1_INT1("A string with a double value followed by an int value, separated by either a space or comma.") {
        @Override
        public void validate(String s)
                throws Exception {
            String[] vals = s.replaceAll(",", " ").split(" ");
            vals = filterEmpty(vals);
            if (vals.length != 2) {
                throw new IllegalArgumentException("Must have 2 arguments for 1 double and 1 int.");
            }
            Double.parseDouble(vals[0]);
            Integer.parseInt(vals[1]);
        }
    };

    /**
     * Validates the given String against this type of InputType's expected format.
     * See the documentation on each of the InputTypes to see what they expect.
     *
     * @param s - The String to check.
     * @throws Exception If {@code s} is not valid for this InputType.
     */
    public abstract void validate(String s) throws Exception;

    /**
     * String value for boolean {@code true}.
     */
    public static final String TRUE_VAL = "true";

    /**
     * String value for boolean {@code false}.
     */
    public static final String FALSE_VAL = "false";
    private final String str;

    /**
     * A list of all the InputTypes, so specifying all values is easy.
     */
    public static final ImmutableList<InputType> ALL;

    static {
        List<InputType> tmp = new LinkedList<InputType>();
        for (InputType it : InputType.values()) {
            tmp.add(it);
        }
        ALL = new ImmutableList<InputType>(tmp);
    }

    private InputType(String s) {
        str = s;
    }

    /**
     * Returns a String that explains what type of input this type expects.
     *
     * @return String.
     */
    public String getExpectedString() {
        return str;
    }

    private static String[] filterEmpty(String[] in) {
        int count = 0;
        for (int i = 0; i < in.length; i++) {
            //Count how many non empty spaces we have.
            if (!in[i].equals("")) {
                count++;
            }
        }
        String[] out = new String[count];
        count = 0;
        for (int i = 0; i < in.length; i++) {
            if (!in[i].equals("")) {
                out[count] = in[i];
                count++;
            }
        }
        return out;
    }
}
