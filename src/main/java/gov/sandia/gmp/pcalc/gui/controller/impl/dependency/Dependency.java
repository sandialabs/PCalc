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
import gov.sandia.gmp.pcalc.gui.controller.impl.InputInsertion;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

/**
 * A Dependency is something that can either be satisfied or not.
 * The general method for determining this is by a set of parameters
 * that are Fields and the values they should be according to a set of
 * DepLogic statements that bind them together.  Dependencies also hold
 * the things that should be shown in the PCalc gui once this dependency
 * is satisfied.
 */
public interface Dependency {

    /**
     * Decided if this dependency has all of its required value pars satisfied as per its {@code DepLogic}.
     *
     * @return {@code true} if this dependency is satisfied, otherwise {@code false}.
     */
    public abstract boolean isSatisfied();

    /**
     * Returns the list of InputInsertions that should be shown when this dependency is satisfied.
     *
     * @return the list of InputInsertions that should be shown when this dependency is satisfied.
     */
    public ImmutableList<InputInsertion> getDeps();

    /**
     * Returns a list of all the Fields and the values in which they are compared with when deciding if this
     * Dependency is satisfied or not.
     *
     * @return a list of all the Fields and the values in which they are compared with when deciding if this
     * Dependency is satisfied or not.
     */
    public ImmutableList<StringBinding<Field>> getParams();

}