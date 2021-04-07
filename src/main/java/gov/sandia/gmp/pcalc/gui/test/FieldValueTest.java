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
package gov.sandia.gmp.pcalc.gui.test;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.InputType;
import gov.sandia.gmp.pcalc.gui.controller.impl.FieldValue;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldImpl;
import org.junit.Test;

import static org.junit.Assert.*;


public class FieldValueTest {

    private FieldValue createFieldValue(Field f) {
        return new FieldValue(f);
    }

    @Test
    public void testGetters() {
        Field f1 = new FieldImpl("", null, null, InputType.STRING, null, null);
        Field f2 = new FieldImpl("2", null, null, InputType.STRING, null, null);
        FieldValue fv = createFieldValue(f1);
        assertEquals(f1, fv.getField());

        FieldValue fv2 = createFieldValue(f2);
        assertEquals(f2, fv2.getField());
    }

    /*
     * Testing CORRECT values.
     */

    @Test
    public void testCorrectValue_STRING() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.STRING, null, null));
        String lastVal = "any other String";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(lastVal, fv.getValue());

        lastVal = "a;slfdkjas;ldkfj09 30943t 3 4thrnd oiw0954h tiuq haesjh4t [9q834u angv -9w48g qoewjrg a043wu ";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(lastVal, fv.getValue());

        lastVal = "";
        fv.setValue("any string");
        fv.setValue(lastVal);
        assertEquals(null, fv.getValue());

    }

    @Test
    public void testCorrectValue_BOOL() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.BOOL, null, null));
        fv.setValue(InputType.TRUE_VAL);

        assertEquals(InputType.TRUE_VAL, fv.getValue());

        fv.setValue(InputType.FALSE_VAL);

        assertEquals(InputType.FALSE_VAL, fv.getValue());
    }

    @Test
    public void testCorrectValue_CHECKBOX() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.CHECKBOX, null, null));
        String val = "one two three four";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1, 2, 3, 4";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1,2,3,4";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1,     2,               3,4";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "";
        fv.setValue(val);

        assertEquals(null, fv.getValue());
    }

    @Test
    public void testCorrectValue_DIRECTORY() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DIRECTORY, null, null));
        String lastVal = "any other String";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(lastVal, fv.getValue());

        lastVal = "a;slfdkjas;ldkfj09 30943t 3 4thrnd oiw0954h tiuq haesjh4t [9q834u angv -9w48g qoewjrg a043wu ";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(lastVal, fv.getValue());

        lastVal = "";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(null, fv.getValue());
    }

    @Test
    public void testCorrectValue_FILE() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.FILE, null, null));
        String lastVal = "any other String";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(lastVal, fv.getValue());

        lastVal = "a;slfdkjas;ldkfj09 30943t 3 4thrnd oiw0954h tiuq haesjh4t [9q834u angv -9w48g qoewjrg a043wu ";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(lastVal, fv.getValue());

        lastVal = "";
        fv.setValue("any string");
        fv.setValue(lastVal);

        assertEquals(null, fv.getValue());
    }

    @Test
    public void testCorrectValue_INT() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT, null, null));
        String val = "100";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "-33454100";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1233454100";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "";
        fv.setValue(val);

        assertEquals(null, fv.getValue());
    }

    @Test
    public void testCorrectValue_DOUBLE() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE, null, null));
        String val = "100";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "100.0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "-0.02233";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "23440.00234";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "-0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "";
        fv.setValue(val);

        assertEquals(null, fv.getValue());

        val = "Infinity";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "-Infinity";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "NaN";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = ".345e32";
        fv.setValue(val);

        assertEquals(val, fv.getValue());
    }

    @Test
    public void testCorrectValue_INT_ARR() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT_ARR, null, null));
        String val = "100";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "100 2";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "100,2";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "100,   34";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "100 2 3 54 5 2 0 90 -9";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "";
        fv.setValue(val);

        assertEquals(null, fv.getValue());
    }

    @Test
    public void testCorrectValue_DOUBLE2_INT1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2_INT1, null, null));
        String val = "100 40 23";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1.00 4.0 23";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "0 -.0234 42";

        fv.setValue(val);

        assertEquals(val, fv.getValue());
    }

    @Test
    public void testCorrectValue_DOUBLE_ARR() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE_ARR, null, null));
        String val = "10.0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "10.0 .2";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1.00,2.0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "100.89676,   34";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "10.0 2 3 5.4 5 2 0 9.0 -.9";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "";
        fv.setValue(val);

        assertEquals(null, fv.getValue());
    }

    @Test
    public void testCorrectValue_DOUBLE3() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE3, null, null));
        String val = "1.00 40 23";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1.00 4.0 2.3";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "0 -.0234 4.2";
        fv.setValue(val);

        assertEquals(val, fv.getValue());
    }

    @Test
    public void testCorrectValue_DOUBLE2() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2, null, null));
        String val = "1.00 23";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "1.00 4.0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "0 -.0234";

        fv.setValue(val);

        assertEquals(val, fv.getValue());
    }

    @Test
    public void testCorrectValue_DOUBLE1_INT1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE1_INT1, null, null));
        String val = "-1.00 23";

        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = ".02344 0";
        fv.setValue(val);

        assertEquals(val, fv.getValue());

        val = "0 -234";
        fv.setValue(val);

        assertEquals(val, fv.getValue());
    }

    /*
     * Testing INCORRECT values.
     */
    @Test
    public void testIncorrectValue_STRING() {
        assertTrue(true);
    }

    @Test
    public void testIncorrectValue_DIRECTORY() {
        assertTrue(true);
    }

    @Test
    public void testIncorrectValue_FILE() {
        assertTrue(true);
    }

    @Test
    public void testIncorrectValue_CHECKBOX() {
        assertTrue(true);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_BOOL1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.BOOL, null, null));
        String val = "3";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_BOOL2() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.BOOL, null, null));
        String val = "true ";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_BOOL3() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.BOOL, null, null));
        String val = " true";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_BOOL4() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.BOOL, null, null));
        String val = "rue";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT, null, null));
        String val = "true";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT2() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT, null, null));
        String val = "0.0";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT3() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT, null, null));
        String val = "234253452345243523";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE, null, null));
        String val = "ads";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE2() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE, null, null));
        String val = "--23";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT_ARR1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT_ARR, null, null));
        String val = "--23";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT_ARR2() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT_ARR, null, null));
        String val = "23 3434-223";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT_ARR3() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT_ARR, null, null));
        String val = "                                                                           d     9";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_INT_ARR4() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.INT_ARR, null, null));
        String val = "0.0";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE2_INT11() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2_INT1, null, null));
        String val = "0.0";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE2_INT12() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2_INT1, null, null));
        String val = "0 1 4.4";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE2_INT13() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2_INT1, null, null));
        String val = "0.8 134.314159 44 7";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE_ARR1() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE_ARR, null, null));
        String val = "0.8 134.314159 44-7";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE_ARR2() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE_ARR, null, null));
        String val = "not even numbers";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE31() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE3, null, null));
        String val = "NaN Infinity -Infinity 3.14159";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE32() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE3, null, null));
        String val = "0 2.71828182897090";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE21() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2, null, null));
        String val = "0 2.71828182897090 aa";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE22() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2, null, null));
        String val = "abab";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE23() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE2, null, null));
        String val = "354 03-4";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE1_INT11() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE3, null, null));
        String val = "354 03.4";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE1_INT12() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE3, null, null));
        String val = "03.4";
        fv.setValue(val);
    }

    @Test(expected = Exception.class)
    public void testIncorrectValue_DOUBLE1_INT13() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.DOUBLE3, null, null));
        String val = "354";
        fv.setValue(val);
    }

    /*
     * Tests DEFUALTING values.
     */

    @Test
    public void testDefaulting() throws Exception {
        String defVal = "defaultium";
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.STRING, null, defVal));
        assertEquals(defVal, fv.getValue());

        fv.setValue("blah blah");
        assertFalse(defVal.equals(fv.getValue()));
        fv.setValue("");
        assertEquals(defVal, fv.getValue());
        fv.setValue("blah blah");
        assertFalse(defVal.equals(fv.getValue()));
        fv.setValue(null);
        assertEquals(defVal, fv.getValue());
        fv.setValue(defVal);
        assertEquals(defVal, fv.getValue());

        Field defField = new FieldImpl("", null, null, InputType.STRING, null, defVal);
        fv = createFieldValue(defField);

        fv.setValue("blah blah");
        assertFalse(defVal.equals(fv.getValue()));
        fv.setValue("");
        assertEquals(defVal, fv.getValue());
        fv.setValue("blah blah");
        assertFalse(defVal.equals(fv.getValue()));
        fv.setValue(null);
        assertEquals(defVal, fv.getValue());
        fv.setValue(defVal);
        assertEquals(defVal, fv.getValue());

        defVal = "2343553";
        Field f2 = new FieldImpl("", null, null, InputType.INT, null, defVal);
        fv = createFieldValue(f2);
        assertEquals(defVal, fv.getValue());
        fv.setValue("42");
        assertFalse(defVal.equals(fv.getValue()));
        fv.setValue("");
        assertEquals(defVal, fv.getValue());
    }

    /*
     * Test HASVALUE.
     */

    @Test
    public void testHasValue_Normal() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.STRING, null, null));
        String val = "val";
        fv.setValue(val);
        assertTrue(fv.hasValue(val));

        val = "345545-33 plus 33";
        fv.setValue(val);
        assertTrue(fv.hasValue(val));

        fv.setValue("not this other thing");
        assertFalse(fv.hasValue(val));
        val = "one";
        fv.setValue("one two three");
        assertFalse(fv.hasValue(val));
    }

    @Test
    public void testHasValue_Checkbox() throws Exception {
        FieldValue fv = createFieldValue(new FieldImpl("", null, null, InputType.CHECKBOX, null, null));
        String val = "val";
        fv.setValue(val);
        assertTrue(fv.hasValue(val));

        val = "345545-33 plus 33";
        fv.setValue(val);
        assertFalse(fv.hasValue(val));

        fv.setValue("not this other thing");
        assertFalse(fv.hasValue(val));
        val = "one";
        fv.setValue("one two three");
        assertTrue(fv.hasValue(val));
    }


}
