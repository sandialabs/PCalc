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

import gov.sandia.gmp.pcalc.gui.common.Field;
import gov.sandia.gmp.pcalc.gui.common.StringBinding;
import gov.sandia.gmp.pcalc.gui.controller.impl.FieldValue;
import gov.sandia.gmp.pcalc.gui.controller.impl.InputInsertion;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import java.util.LinkedList;
import java.util.List;

public class SingleDependency implements Dependency {

    private final ImmutableList<StringBinding<Field>> values;
    private final DepDelegate contr;
    private final DepLogic myLogic;

    /**
     * The list of device to show if this Dependency is satisfied.
     */
    private final ImmutableList<InputInsertion> deps;

    /**
     * Creates a Dependency which is a logical association between a set of Fields and their requirements,
     * and a set of InputDevices to show once the requirements are met.
     *
     * @param requirements - The FieldsValues and the values that are required for their satisfaction.
     * @param logic        - The type of logic to use with the requirements.
     * @param deps         - The list of InputDevices to show once the requirements are met.
     * @param contr        - The Delegate for this Dependency.
     */
    public SingleDependency(List<StringBinding<Field>> requirements, DepLogic logic, List<InputInsertion> deps, DepDelegate contr) {
        this.values = new ImmutableList<StringBinding<Field>>(requirements);
        this.deps = deps == null ? null : new ImmutableList<InputInsertion>(deps);
        myLogic = logic;
        this.contr = contr;
    }

    @Override
    public boolean isSatisfied() {
        List<StringBinding<FieldValue>> list = new LinkedList<StringBinding<FieldValue>>();
        for (StringBinding<Field> sb : values) {
            list.add(new StringBinding<FieldValue>(contr.getFieldValueFor(sb.getObj()), sb.getValue()));
        }
        return myLogic.isSatisfied(list);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        SingleDependency d;
        if (o instanceof SingleDependency) {
            d = (SingleDependency) o;
            boolean ret = values.equals(d.values);
            //Only calculate the equality of the lists if we have to.
            if (!ret)
                return ret;
            return deps.equals(d.deps);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Dependency of: " + values + ", with insertions: " + deps;
    }

    @Override
    public ImmutableList<InputInsertion> getDeps() {
        return deps;
    }

    @Override
    public ImmutableList<StringBinding<Field>> getParams() {
        return values;
    }

}
