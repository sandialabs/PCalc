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
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.controller.impl.FieldValue;
import gov.sandia.gmp.pcalc.gui.controller.impl.InputInsertion;
import gov.sandia.gmp.pcalc.gui.controller.impl.dependency.ChainDependency;
import gov.sandia.gmp.pcalc.gui.controller.impl.dependency.DepDelegate;
import gov.sandia.gmp.pcalc.gui.controller.impl.dependency.DepLogic;
import gov.sandia.gmp.pcalc.gui.controller.impl.dependency.SingleDependency;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DependencyTest implements DepDelegate {

    private static Map<Field, FieldValue> vals;
    private List<StringBinding<Field>> list;
    private final List<InputInsertion> dummy = new LinkedList<InputInsertion>();

    @Before
    public void createFields() {
        vals = new HashMap<Field, FieldValue>();
    }

    @Test
    public void test_Single_And() {
        //One argument
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        list = makeList();
        String corrVal = "true";
        list.add(makeSb(f1, corrVal));
        SingleDependency sd = new SingleDependency(list, DepLogic.AND, dummy, this);
        assertFalse(sd.isSatisfied());
        setValue(f1, "false");
        assertFalse(sd.isSatisfied());
        setValue(f1, corrVal);
        assertTrue(sd.isSatisfied());
        //Two arguments.
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        list.add(makeSb(f2, corrVal));
        sd = new SingleDependency(list, DepLogic.AND, dummy, this);
        assertFalse(sd.isSatisfied());
        setValue(f1, "false");
        setValue(f2, corrVal);
        assertFalse(sd.isSatisfied());
        setValue(f2, "false");
        assertFalse(sd.isSatisfied());
        setValue(f2, corrVal);
        setValue(f1, corrVal);
        assertTrue(sd.isSatisfied());
    }

    @Test
    public void test_Single_Or() {
        //One argument
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        list = makeList();
        String corrVal = "true";
        list.add(makeSb(f1, corrVal));
        SingleDependency sd = new SingleDependency(list, DepLogic.OR, dummy, this);
        assertFalse(sd.isSatisfied());
        setValue(f1, "false");
        assertFalse(sd.isSatisfied());
        setValue(f1, corrVal);
        assertTrue(sd.isSatisfied());
        //Two arguments.
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        list.add(makeSb(f2, corrVal));
        sd = new SingleDependency(list, DepLogic.OR, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        setValue(f2, corrVal);
        assertTrue(sd.isSatisfied());
        setValue(f2, "false");
        assertFalse(sd.isSatisfied());
        setValue(f2, corrVal);
        setValue(f1, corrVal);
        assertTrue(sd.isSatisfied());
    }

    @Test
    public void test_Single_Xor() {
        //One argument
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        list = makeList();
        String corrVal = "true";
        list.add(makeSb(f1, corrVal));
        SingleDependency sd = new SingleDependency(list, DepLogic.XOR, dummy, this);
        assertFalse(sd.isSatisfied());
        setValue(f1, "false");
        assertFalse(sd.isSatisfied());
        setValue(f1, corrVal);
        assertTrue(sd.isSatisfied());
        //Two arguments.
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        list.add(makeSb(f2, corrVal));
        sd = new SingleDependency(list, DepLogic.XOR, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        setValue(f2, corrVal);
        assertTrue(sd.isSatisfied());
        setValue(f2, "false");
        assertFalse(sd.isSatisfied());
        setValue(f2, corrVal);
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
    }

    @Test
    public void test_Single_Nand() {
        //One argument
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        list = makeList();
        String corrVal = "true";
        list.add(makeSb(f1, corrVal));
        SingleDependency sd = new SingleDependency(list, DepLogic.NAND, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        assertTrue(sd.isSatisfied());
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
        //Two arguments.
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        list.add(makeSb(f2, corrVal));
        sd = new SingleDependency(list, DepLogic.NAND, dummy, this);
        assertFalse(sd.isSatisfied());
        setValue(f1, "false");
        setValue(f2, corrVal);
        assertFalse(sd.isSatisfied());
        setValue(f2, "false");
        assertTrue(sd.isSatisfied());
        setValue(f2, corrVal);
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
    }

    @Test
    public void test_Single_Nor() {
        //One argument
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        list = makeList();
        String corrVal = "true";
        list.add(makeSb(f1, corrVal));
        SingleDependency sd = new SingleDependency(list, DepLogic.NOR, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        assertTrue(sd.isSatisfied());
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
        //Two arguments.
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        list.add(makeSb(f2, corrVal));
        sd = new SingleDependency(list, DepLogic.NOR, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        setValue(f2, corrVal);
        assertTrue(sd.isSatisfied());
        setValue(f2, "false");
        assertTrue(sd.isSatisfied());
        setValue(f2, corrVal);
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
    }

    @Test
    public void test_Single_Xnor() {
        //One argument
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        list = makeList();
        String corrVal = "true";
        list.add(makeSb(f1, corrVal));
        SingleDependency sd = new SingleDependency(list, DepLogic.XNOR, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        assertTrue(sd.isSatisfied());
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
        //Two arguments.
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        list.add(makeSb(f2, corrVal));
        sd = new SingleDependency(list, DepLogic.XNOR, dummy, this);
        assertTrue(sd.isSatisfied());
        setValue(f1, "false");
        setValue(f2, corrVal);
        assertTrue(sd.isSatisfied());
        setValue(f2, "false");
        assertFalse(sd.isSatisfied());
        setValue(f2, corrVal);
        setValue(f1, corrVal);
        assertFalse(sd.isSatisfied());
    }

    @Test
    public void test_Chain_Or_Single() {
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        String tr = "true";
        String fl = "false";
        setValue(f1, fl);
        setValue(f2, fl);
        list = makeList();
        list.add(makeSb(f1, tr));
        list.add(makeSb(f2, tr));
        ChainDependency cd = new ChainDependency(dummy, this);
        cd.addLink(list, DepLogic.OR, DepLogic.OR);
        assertFalse(cd.isSatisfied());
        assertFalse(cd.isSatisfied());
        assertFalse(cd.isSatisfied());
        assertFalse(cd.isSatisfied());
        setValue(f1, tr);
        assertTrue(cd.isSatisfied());
        setValue(f1, fl);
        setValue(f2, tr);
        assertTrue(cd.isSatisfied());
        setValue(f1, tr);
        assertTrue(cd.isSatisfied());
    }

    @Test
    public void test_Chain_Small1() {
        // (false || true) && (true || false)
        ChainDependency cd = new ChainDependency(dummy, this);
        String tr = "true";
        String fl = "false";
        list = makeList();
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        setValue(f1, fl);
        list.add(makeSb(f1, tr));
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        setValue(f2, tr);
        list.add(makeSb(f2, tr));

        cd.addLink(list, DepLogic.OR, DepLogic.XNOR);
        assertTrue(cd.isSatisfied());

        Field f3 = new FieldImpl("f3", null, null, InputType.STRING, null, null);
        setValue(f3, tr);
        list.add(makeSb(f3, tr));
        Field f4 = new FieldImpl("f4", null, null, InputType.STRING, null, null);
        setValue(f4, fl);
        list.add(makeSb(f4, tr));

        cd.addLink(list, DepLogic.OR, DepLogic.AND);
        assertTrue(cd.isSatisfied());
    }

    @Test
    public void test_Chain_Small2() {
        //(true ^ false) ^ (true &! false)
        ChainDependency cd = new ChainDependency(dummy, this);
        String tr = "true";
        String fl = "false";
        list = makeList();
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        setValue(f1, tr);
        list.add(makeSb(f1, tr));
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        setValue(f2, fl);
        list.add(makeSb(f2, tr));

        cd.addLink(list, DepLogic.XOR, DepLogic.XNOR);
        assertTrue(cd.isSatisfied());

        Field f3 = new FieldImpl("f3", null, null, InputType.STRING, null, null);
        setValue(f3, tr);
        list.add(makeSb(f3, tr));
        Field f4 = new FieldImpl("f4", null, null, InputType.STRING, null, null);
        setValue(f4, fl);
        list.add(makeSb(f4, tr));

        cd.addLink(list, DepLogic.NAND, DepLogic.XOR);
        assertTrue(cd.isSatisfied());
    }

    @Test
    public void test_Chain_Small3() {
        //(false !| true) !^ (false | true)
        ChainDependency cd = new ChainDependency(dummy, this);
        String tr = "true";
        String fl = "false";
        list = makeList();
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        setValue(f1, fl);
        list.add(makeSb(f1, tr));
        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        setValue(f2, tr);
        list.add(makeSb(f2, tr));

        cd.addLink(list, DepLogic.NOR, DepLogic.XNOR);
        assertTrue(cd.isSatisfied());

        Field f3 = new FieldImpl("f3", null, null, InputType.STRING, null, null);
        setValue(f3, fl);
        list.add(makeSb(f3, tr));
        Field f4 = new FieldImpl("f4", null, null, InputType.STRING, null, null);
        setValue(f4, tr);
        list.add(makeSb(f4, tr));

        cd.addLink(list, DepLogic.OR, DepLogic.XNOR);
        assertFalse(cd.isSatisfied());
    }

    @Test
    public void test_Chain_Large() {
        // (false !& true) ^ (false | true) !| (false & false) & (true !^ false)
        ChainDependency cd = new ChainDependency(dummy, this);
        String tr = "true";
        String fl = "false";
        list = makeList();
        Field f1 = new FieldImpl("f1", null, null, InputType.STRING, null, null);
        setValue(f1, fl);
        list.add(makeSb(f1, tr));
        Field f8 = new FieldImpl("f8", null, null, InputType.STRING, null, null);
        setValue(f8, tr);
        list.add(makeSb(f8, tr));

        cd.addLink(list, DepLogic.NAND, DepLogic.XNOR);

        Field f2 = new FieldImpl("f2", null, null, InputType.STRING, null, null);
        setValue(f2, fl);
        list.add(makeSb(f2, tr));
        Field f3 = new FieldImpl("f3", null, null, InputType.STRING, null, null);
        setValue(f3, tr);
        list.add(makeSb(f3, tr));

        cd.addLink(list, DepLogic.OR, DepLogic.XOR);

        Field f4 = new FieldImpl("f4", null, null, InputType.STRING, null, null);
        setValue(f4, fl);
        list.add(makeSb(f4, tr));
        Field f5 = new FieldImpl("f5", null, null, InputType.STRING, null, null);
        setValue(f5, fl);
        list.add(makeSb(f5, tr));

        cd.addLink(list, DepLogic.AND, DepLogic.NOR);

        Field f6 = new FieldImpl("f6", null, null, InputType.STRING, null, null);
        setValue(f6, tr);
        list.add(makeSb(f6, tr));
        Field f7 = new FieldImpl("f7", null, null, InputType.STRING, null, null);
        setValue(f7, fl);
        list.add(makeSb(f7, tr));

        cd.addLink(list, DepLogic.XNOR, DepLogic.AND);
        assertTrue(cd.isSatisfied());


    }

    private void setValue(Field f, String val) {
        FieldValue fv = vals.get(f);
        if (fv == null) {
            fv = new FieldValue(f);
            vals.put(f, fv);
        }
        try {
            fv.setValue(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<StringBinding<Field>> makeList() {
        return new LinkedList<StringBinding<Field>>();
    }

    private StringBinding<Field> makeSb(Field f1, String val) {
        return new StringBinding<Field>(f1, val);
    }

    @Override
    public FieldValue getFieldValueFor(Field obj) {
        FieldValue fv = vals.get(obj);
        if (fv == null) {
            fv = new FieldValue(obj);
            vals.put(obj, fv);
        }
        return fv;
    }

}
