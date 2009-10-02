// @(#)$Id: AbstractList.spec 72003 2008-07-15 15:05:48Z chalin $

// Copyright (C) 2001 Iowa State University

// This file is part of JML

// JML is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// JML is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with JML; see the file COPYING.  If not, write to
// the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.


package java.util;

/** JML's specification of java.util.AbstractList.
 * @version $Revision: 72003 $
 * @author Gary T. Leavens
 * @author David R. Cok
 */
public abstract class AbstractList extends AbstractCollection implements List {

    /*@ public normal_behavior
      @   ensures \result <==> ( initialAbstractCollection() && initialList());
      @ public pure model boolean initialAbstractList();
      @*/

    /*@ protected normal_behavior
          ensures initialAbstractList();
      @*/
    /*@ pure @*/ protected AbstractList();

    // specification inherited from List
    public boolean add(Object o);

    // specification inherited from List
    abstract public /*@ pure @*/ Object get(int index);

    // specification inherited from List
    public Object set(int index, Object element) throws UnsupportedOperationException;

    // specification inherited from List
    public void add(int index, Object element) throws UnsupportedOperationException;

    // specification inherited from List
    public Object remove(int index) throws UnsupportedOperationException;

    // Search Operations

    // specification inherited from List
    public /*@ pure @*/ int indexOf(Object o);

    // specification inherited from List
    public /*@ pure @*/ int lastIndexOf(Object o);

    // Bulk Operations

    // specification inherited from List
    public void clear();

    // specification inherited from List
    public boolean addAll(int index, Collection c);

    // Iterators

    // specification inherited from List
    public /*@ pure @*//*@non_null*/ Iterator iterator();

    public /*@ pure @*/ ListIterator listIterator();

    // specification inherited from List
    public /*@ pure @*/ ListIterator listIterator(final int index);

    // specification inherited from List
    public /*@ pure @*/ List subList(int fromIndex, int toIndex);

    // Comparison and hashing

    // specification inherited from Object
    public /*@ pure @*/ boolean equals(Object o);

    // specification inherited from Object
    public /*@ pure @*/ int hashCode();

    /*@ protected normal_behavior
      @  requires 0 <= fromIndex && fromIndex < size();
      @  {|
      @     requires fromIndex >= toIndex;
      @     assignable \nothing;
      @   also
      @     old int mn = toIndex < size() ? toIndex : size();
      @     requires fromIndex < toIndex;
      @     ensures \not_modified(containsNull,elementType);
      @     assignable objectState;
      @     ensures size() == \old(size()) - (mn-fromIndex);
      @     ensures (\forall int i; 0<=i && i < fromIndex;
                               get(i) == \old(get(i)));
      @     ensures (\forall int i; mn<=i && i < \old(size());
                               get(i-mn+fromIndex) == \old(get(i)));
      @  |}
      @ also protected exceptional_behavior
      @  requires fromIndex < 0 || fromIndex >= size();
      @  assignable \nothing;
      @  signals_only IndexOutOfBoundsException;
      @*/
    protected void removeRange(int fromIndex, int toIndex);

    transient protected int modCount; //@ in objectState;
        //@ initially modCount == 0;

}

class SubList extends AbstractList {

    SubList(AbstractList list, int fromIndex, int toIndex);

    public Object set(int index, Object element);

    public Object get(int index);

    public int size();

    public void add(int index, Object element);

    public Object remove(int index);

    protected void removeRange(int fromIndex, int toIndex);

    public boolean addAll(Collection c);

    public boolean addAll(int index, Collection c);

    public Iterator iterator();

    public ListIterator listIterator(int index);

    public List subList(int fromIndex, int toIndex);
}



class RandomAccessSubList extends SubList implements RandomAccess {

    RandomAccessSubList(AbstractList list, int fromIndex, int toIndex);

    public List subList(int fromIndex, int toIndex);
}
