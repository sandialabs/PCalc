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

import java.util.LinkedList;
import java.util.List;

/**
 * Class to implement chained logical statements depending on the values of fields.
 * For example:<br />
 * <br />
 * ( f is x AND f' is y) OR (f is y NOR f' is y)
 * <p>
 * Which would be satisfied if f = x and f' = y or when f != y and f' != y.
 */
public class ChainDependency implements Dependency {

    private final ImmutableList<InputInsertion> deps;
    private List<Node> nodes;
    private final DepDelegate contr;

    /**
     * Represents a "Link" in the chain.
     */
    private class Node {
        public SingleDependency link;
        public DepLogic linkLogic;

        public Node(SingleDependency link, DepLogic linkLogic) {
            this.link = link;
            this.linkLogic = linkLogic;
        }

        @Override
        public String toString() {
            return "(" + link.toString() + " " + linkLogic + ")";
        }

        @Override
        public int hashCode() {
            return (link.hashCode() | linkLogic.hashCode()) / (linkLogic.ordinal() * 311);
        }

        @Override
        public boolean equals(Object o) {
            Node n;
            if (o instanceof Node) {
                n = (Node) o;
                return link.equals(n.link) && linkLogic.equals(n.linkLogic);
            }
            return false;
        }

    }

    /**
     * Creates an empty ChainDependency.  This will have no parameters and no logic statements.  A new statement
     * can be added by a call to {@link ChainDependency#addLink(List, DepLogic, DepLogic)}.  This will allow for
     * complicated statements such as {@code (x || x) && (x !| x)}, where {@code x} is any Field that is to be
     * checked for the correct value.  To create this example, add a link with {@code xs} as the {@code params} arg,
     * {@link DepLogic#OR} as the {@code pLogic} and anything as the {@code linkLogic}.  Then add another with {@code xs}
     * as the {@code params} arg, {@link DepLogic#NOR} as the {@code pLogic}, and finally {@link DepLogic#AND} as the
     * {@code linkLogic}.  Any number of these statements can be chained together.
     * <br />
     * <br />
     * It is important to note that more than 3 statements chained together are not represented by something that looks like
     * this:
     * <p align=center>{@code (s1) | (s2) | (s3)}</p>
     * But rather like this:
     * <p align=center>{@code ((s1) | (s2)) | (s3)}</p>
     * Meaning a link in the chain is computed with it's own satisfaction value and the result of ALL previous links.  Order
     * of operation is not considered, and all computation is strictly left to right.
     *
     * @param deps  - The InputInsertions to show once this Dependency is satisfied.
     * @param contr - The delegate for this Dependency.
     */
    public ChainDependency(List<InputInsertion> deps, DepDelegate contr) {
        nodes = new LinkedList<Node>();
        this.deps = new ImmutableList<InputInsertion>(deps);
        this.contr = contr;
    }

    /**
     * Adds a link to this chain of dependency statements.  Each link is handled the same way as a
     * {@link SingleDependency}.  In addition to this however, each link and the previous one will
     * be combined with a call to {@link DepLogic#compute(boolean, boolean)}.  For the first link, this
     * will be ignored and only the satisfaction of it will be computed.
     *
     * @param params    - The set of values Fields that must match the value they are bound to in order for this
     *                  Dependency to be satisfied.
     * @param pLogic    - The Logic to use with the parameters.
     * @param linkLogic - The logic to use in chaining this statement to the previous one.
     */
    public void addLink(List<StringBinding<Field>> params, DepLogic pLogic, DepLogic linkLogic) {
        if (params == null || pLogic == null || linkLogic == null) {
            throw new NullPointerException();
        }
        nodes.add(new Node(new SingleDependency(params, pLogic, deps, contr), linkLogic));
    }

    @Override
    public boolean isSatisfied() {
        if (nodes.isEmpty()) {
            return true;
        }
        //First link just check if it is satisfied, forget the link logic.
        Node first = nodes.remove(0);
        boolean ret = first.link.isSatisfied();
        //Then for the rest check the previous with the current and link it together.
        for (Node n : nodes) {
            ret = n.linkLogic.compute(ret, n.link.isSatisfied());
        }
        nodes.add(0, first);
        return ret;
    }

    @Override
    public ImmutableList<InputInsertion> getDeps() {
        return deps;
    }

    @Override
    public boolean equals(Object o) {
        ChainDependency cd;
        if (o instanceof ChainDependency) {
            cd = (ChainDependency) o;
            return contr.equals(cd.contr) && deps.equals(cd.deps);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return deps.hashCode();
    }

    @Override
    public String toString() {
        return nodes.toString();
    }

    @Override
    public ImmutableList<StringBinding<Field>> getParams() {
        List<StringBinding<Field>> list = new LinkedList<StringBinding<Field>>();
        for (Node n : nodes) {
            list.addAll(n.link.getParams());
        }
        return new ImmutableList<StringBinding<Field>>(list);
    }

}
