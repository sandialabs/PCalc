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
package gov.sandia.gmp.pcalc.gui.controller.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to keep track of what object should be showing in the gui or not.
 *
 * @param <E> - The type of Object to track.
 */
public class ShowTracker<E> {

    /**
     * The object used to determine if something is set in the records.
     * To determine whether or not an object of type E is showing, query the record
     * with that object as the key.  If this object is returned then it is showing,
     * otherwise it was not.
     */
    private static final Object MARK = new Object();

    private Map<E, Object> rec;

    /**
     * Creates a ShowTracker for E, such that nothing is showing before any calls to {@link #setShowing(Object, boolean)}.
     */
    public ShowTracker() {
        rec = new HashMap<E, Object>();
    }

    /**
     * Asks the tracker if the given object is showing or not.  If the object was
     * never set to be showing or not with {@link #setShowing(Object, boolean)} it
     * will return {@code false}.
     *
     * @param e - The Object to check for.
     * @return Whether or not it is showing.  {@code true} means it it showing,
     * and {@code false} means it is not.
     */
    public boolean isShowing(E e) {
        return rec.get(e) == MARK;
    }

    /**
     * Sets the state of whether or not a given object is showing.  If the object was never set
     * to be showing or not it will return {@code false}.
     *
     * @param e    - The object to track.
     * @param show - Whether or not it is showing.  {@code true} means it it showing,
     *             and {@code false} means it is not.
     */
    public void setShowing(E e, boolean show) {
        if (show) {
            rec.put(e, MARK);
        } else {
            rec.remove(e);
        }
    }

    /**
     * @return Returns a Set of all the showing object that this ShowTracker knows about.
     * This set is a copy of the current set, so future modifications to this ShowTracker will not
     * update the set returned by this method.
     */
    public Set<E> getShowing() {
        Set<E> sf = new HashSet<E>(rec.keySet());
        return sf;
    }

    /**
     * Returns {@code true} if this tracker has no showing elements.
     * If this method does return {@code true}, then a call to {@link #getShowing()} will return
     * something equivalent to the empty set.
     *
     * @return {@code true} if this tracker has no showing elements.
     */
    public boolean empty() {
        return rec.keySet().isEmpty();
    }

}
