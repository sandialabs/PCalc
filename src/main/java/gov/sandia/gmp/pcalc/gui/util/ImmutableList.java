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
package gov.sandia.gmp.pcalc.gui.util;

import java.io.Serializable;
import java.util.*;

/**
 * This ImmutableList type implements List, but for all methods that would normally modify the List,
 * an {@code UnsupportedOperationException} is thrown.
 * Those methods are:<br />
 * <ul>
 * <li>{@code add(E)}</li>
 * <li>{@code add(int, E)}</li>
 * <li>{@code addAll(Collection<? extends E>)}</li>
 * <li>{@code addAll(int, Collection<? extends E>)}</li>
 * <li>{@code clear()}</li>
 * <li>{@code remove(Object)}</li>
 * <li>{@code remove(int)}</li>
 * <li>{@code removeAll(Collection<? extends E)}</li>
 * <li>{@code retainAll(Collection<? extends E)}</li>
 * <li>{@code set(int, E)}</li>
 * </ul>
 * <p>
 * Also, for the Iterator:<br />
 * <ul>
 * <li>{@code remove()}</li>
 * </ul>
 * And for the ListIterator:<br />
 * <ul>
 * <li>{@code remove()}</li>
 * <li>{@code add(E)}</li>
 * <li>{@code set(E)}</li>
 * </ul>
 *
 * @param <E> The type for this list to contain.
 */
public final class ImmutableList<E> implements List<E>, Serializable, Cloneable {

    private static final long serialVersionUID = -2499277703564923478L;
    private final LinkedList<E> list;
    /**
     * An unmodifiable constant for the empty list.
     */
    private static final ImmutableList<Object> EMPTY_LIST = new ImmutableList<Object>();

    /**
     * Returns the immutable empty list for the type parameter <i>T</i>.
     *
     * @param <T> - The type of empty list to return;
     * @return The empty list of the desired type.
     */
    @SuppressWarnings("unchecked")
    public static final <T> ImmutableList<T> emptyList() {
        return (ImmutableList<T>) EMPTY_LIST;
    }

    /**
     * A special ListIterator to stop a user from modifying the list
     * through it.
     *
     * @param <E2> Same as {@code E}.
     */
    private final class ImmutableIterator<E2> implements ListIterator<E2> {

        private final ListIterator<E2> it;

        /**
         * Creates this ImmutableIterator from the given ListIterator.
         * For all supported operations ({@link ImmutableList}) this will
         * have the same functionality as the given ListIterator, otherwise
         * an UnsupportedOperationException will be thrown.
         *
         * @param it - The ListIterator to use.
         */
        public ImmutableIterator(ListIterator<E2> it) {
            this.it = it;
        }

        /**
         * Attempts to create an ImmutableIterator from the given Iterator.
         * If the Iterator is not an instance of ListIterator, then an
         * ClassCastException is thrown.  If it is an instance of ListIterator
         * then for all supported operations ({@link ImmutableList}) this will
         * have the same functionality as the given ListIterator, otherwise
         * an UnsupportedOperationException will be thrown.
         *
         * @param it - The Iterator to attempt to use.
         */
        public ImmutableIterator(Iterator<E2> it) {
            this((ListIterator<E2>) it);
        }

        @Override
        public void add(Object arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return it.hasPrevious();
        }

        @Override
        public E2 next() {
            return it.next();
        }

        @Override
        public int nextIndex() {
            return it.nextIndex();
        }

        @Override
        public E2 previous() {
            return it.previous();
        }

        @Override
        public int previousIndex() {
            return it.previousIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            return it.equals(o) && (o instanceof ImmutableIterator);
        }

        @Override
        public int hashCode() {
            return it.hashCode();
        }

        @Override
        public String toString() {
            return "(Immutable " + it.toString() + ")";
        }

    }

    /**
     * Creates an ImmutableList from the given List of the same type.
     *
     * @param fromList - The list to create this ImmutableList from.  A copy is made for use,
     *                 so later changes to {@code fromList} will not affect this ImmutableList.
     */
    public ImmutableList(List<E> fromList) {
        list = new LinkedList<E>(fromList);
    }

    /**
     * Creates an ImmutableList from the given array of the same type.
     *
     * @param fromArr - The array to create this ImmutableList from.
     */
    public ImmutableList(E... fromArr) {
        list = new LinkedList<E>();
        for (E e : fromArr) {
            list.add(e);
        }
    }

    /**
     * Creates an ImmutableList from the given List of the same type, added with the arguments from the variatic parameter.
     *
     * @param fromList - The base list to create this ImmutableList from.  A copy is made for use,
     *                 so later changes to {@code fromList} will not affect this ImmutableList.
     * @param fromArr- The array to add to {@code fromList} and create this ImmutableList from.
     */
    public ImmutableList(ImmutableList<E> fromList, E... fromArr) {
        list = new LinkedList<E>(fromList);
        for (E e : fromArr) {
            list.add(e);
        }
    }

    @Override
    public boolean add(E arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int arg0, E arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends E> arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object arg0) {
        return list.contains(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return list.containsAll(arg0);
    }

    @Override
    public E get(int arg0) {
        return list.get(arg0);
    }

    @Override
    public int indexOf(Object arg0) {
        return list.indexOf(arg0);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new ImmutableIterator<E>(list.iterator());
    }

    @Override
    public int lastIndexOf(Object arg0) {
        return list.lastIndexOf(arg0);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ImmutableIterator<E>(list.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int arg0) {
        return new ImmutableIterator<E>(list.listIterator());
    }

    @Override
    public boolean remove(Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(int arg0, E arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<E> subList(int arg0, int arg1) {
        return list.subList(arg0, arg1);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        return list.toArray(arg0);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o) && (o instanceof ImmutableList);
    }

    @Override
    public String toString() {
        return "(Immutable " + list.toString() + ")";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ImmutableList<E>(list);
    }

}
