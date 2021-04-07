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

/**
 * A class to associate a single object with a string usually as a value for that object.
 *
 * @param <T> - The object type to associate with a String.
 */
public class StringBinding<T> {

    /**
     * The various object to associate with a value.
     */
    private final T thing;

    /**
     * The value for {@code thing}.
     */
    private final String val;

    /**
     * Creates a binding between a various object type and a String.
     *
     * @param obj - The object to associate with a String.
     * @param val - The String to associate with an object.
     */
    public StringBinding(T obj, String val) {
        thing = obj;
        this.val = val;
    }

    /**
     * @return The Object for this Binding.
     */
    public T getObj() {
        return thing;
    }

    /**
     * @return The String for this Binding.
     */
    public String getValue() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        StringBinding<?> sb;
        if (o instanceof StringBinding<?>) {
            sb = (StringBinding<?>) o;
            return thing.equals(sb.thing) && val.equals(sb.val);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return thing.hashCode() ^ val.hashCode();
    }

    @Override
    public String toString() {
        return "<" + thing + ", " + val + ">";
    }

}
