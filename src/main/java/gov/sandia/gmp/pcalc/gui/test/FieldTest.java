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
package gov.sandia.gmp.pcalc.gui.test;

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.InputType;
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldDef;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldImpl;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.inputDevice.listeners.FieldListener;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class FieldTest implements InputDeviceDelegate {

    private Field createFieldImpl(String name, String sNotes, String lNotes, InputType type, List<String> rest, String def) {
        return new FieldImpl(name, sNotes, lNotes, type, rest, def);
    }

    @Test
    public void testGetters1() {
        String sNotes = "sNotes";
        String name = "test";
        String lNotes = "lNotes";
        InputType inT = InputType.STRING;
        LinkedList<String> rest = new LinkedList<String>();
        String def = "default";
        Field f = createFieldImpl(name, sNotes, lNotes, inT, rest, def);
        assertEquals(name, f.getName());
        assertEquals(sNotes, f.getShortNotes());
        assertEquals(lNotes, f.getLongNotes());
        assertEquals(inT, f.getType());
        assertEquals(rest, f.getRestrictions());
        assertEquals(def, f.getDef());

        sNotes = "sNotes - this is not very short.";
        name = "test McTesterson";
        lNotes = "lNotes - this is not very long.";
        inT = InputType.INT_ARR;
        rest = new LinkedList<String>();
        rest.add("something");
        rest.add("a;ja;lkfja;dslkfjajf;alksjf;aja;sdkjflkasdlllllllll     5     ");
        def = "NOT_THE_DEFAULT";
        f = createFieldImpl(name, sNotes, lNotes, inT, rest, def);
        assertEquals(name, f.getName());
        assertEquals(sNotes, f.getShortNotes());
        assertEquals(lNotes, f.getLongNotes());
        assertEquals(inT, f.getType());
        assertEquals(rest, f.getRestrictions());
        assertEquals(def, f.getDef());
    }

    @Test
    public void testEquals1() {
        String sNotes = "sNotes";
        String name = "test";
        String lNotes = "lNotes";
        InputType inT = InputType.STRING;
        LinkedList<String> rest = new LinkedList<String>();
        rest.add("restricitons compulated.");
        String def = "default";
        Field f = createFieldImpl(name, sNotes, lNotes, inT, rest, def);
        Field f2 = createFieldImpl(name, sNotes, lNotes, inT, rest, def);
        assertEquals(f, f2);
        assertEquals(f.hashCode(), f2.hashCode());

        sNotes = "short joe copper";
        name = "this is not a test.";
        lNotes = "long john silvers";
        inT = InputType.DOUBLE1_INT1;
        rest = new LinkedList<String>();
        rest.add("restricitons compulated.");
        rest.add("articulating splines.");
        def = "defaultimux Maximus!";
        f = createFieldImpl(name, sNotes, lNotes, inT, rest, def);
        f2 = createFieldImpl(name, sNotes, lNotes, inT, rest, def);
        assertEquals(f, f2);
        assertEquals(f.hashCode(), f2.hashCode());
    }

    private Field createFieldDef(String name, String sNotes, String lNotes, InputType type, List<String> rest, Field def, InputDeviceDelegate del) {
        return new FieldDef(name, sNotes, lNotes, type, rest, def, del);
    }

    private static final String defVal = "theValue";

    @Test
    public void testGetters2() {
        Field def = createFieldImpl("", "", "", InputType.BOOL, null, null);
        String sNotes = "sNotes";
        String name = "test";
        String lNotes = "lNotes";
        InputType inT = InputType.STRING;
        LinkedList<String> rest = new LinkedList<String>();
        Field f = createFieldDef(name, sNotes, lNotes, inT, rest, def, this);
        assertEquals(name, f.getName());
        assertEquals(sNotes, f.getShortNotes());
        assertEquals(lNotes, f.getLongNotes());
        assertEquals(inT, f.getType());
        assertEquals(rest, f.getRestrictions());
        assertEquals(defVal, f.getDef());

        sNotes = "sNotes - this is not very short.";
        name = "test McTesterson";
        lNotes = "lNotes - this is not very long.";
        inT = InputType.INT_ARR;
        rest = new LinkedList<String>();
        rest.add("something");
        rest.add("a;ja;lkfja;dslkfjajf;alksjf;aja;sdkjflkasdlllllllll     5     ");
        f = createFieldDef(name, sNotes, lNotes, inT, rest, def, this);
        assertEquals(name, f.getName());
        assertEquals(sNotes, f.getShortNotes());
        assertEquals(lNotes, f.getLongNotes());
        assertEquals(inT, f.getType());
        assertEquals(rest, f.getRestrictions());
        assertEquals(defVal, f.getDef());
    }

    @Test
    public void testEquals2() {
        Field def = createFieldImpl("", "", "", InputType.BOOL, null, null);
        String sNotes = "sNotes";
        String name = "test";
        String lNotes = "lNotes";
        InputType inT = InputType.STRING;
        LinkedList<String> rest = new LinkedList<String>();
        rest.add("restricitons compulated.");
        Field f = createFieldDef(name, sNotes, lNotes, inT, rest, def, this);
        Field f2 = createFieldDef(name, sNotes, lNotes, inT, rest, def, this);
        assertEquals(f, f2);
        assertEquals(f.hashCode(), f2.hashCode());

        sNotes = "short joe copper";
        name = "this is not a test.";
        lNotes = "long john silvers";
        inT = InputType.DOUBLE1_INT1;
        rest = new LinkedList<String>();
        rest.add("restricitons compulated.");
        rest.add("articulating splines.");
        f = createFieldDef(name, sNotes, lNotes, inT, rest, def, this);
        f2 = createFieldDef(name, sNotes, lNotes, inT, rest, def, this);
        assertEquals(f, f2);
        assertEquals(f.hashCode(), f2.hashCode());
    }

    /*
     * Dummy implementations.  The only constraint is it returns defVal for getValueFor().
     */

    @Override
    public boolean setField(InputDevice origin, Field f, String val,
                            boolean showErr) {
        return true;
    }

    @Override
    public void registerDevice(InputDevice id) {
    }

    @Override
    public String getValueFor(Field f) {
        return defVal;
    }

    @Override
    public void addFieldListener(Field f, FieldListener fl) {
    }

    @Override
    public void removeFieldListener(Field f, FieldListener fl) {
    }

    @Override
    public boolean setFields(InputDevice origin,
                             List<StringBinding<Field>> vals, boolean showErr) {
        return true;
    }

    @Override
    public ImmutableList<String> getConfigForField(Field f) {
        return ImmutableList.emptyList();
    }

    @Override
    public void addConfigForField(Field f, String line) {
    }

}
