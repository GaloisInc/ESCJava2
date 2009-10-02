// @(#)$Id: ArrayList.spec 1532 2005-08-19 18:41:37Z jkiniry $

// Copyright (C) 2004 Iowa State University

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

/** JML's specification of ArrayList.
 *  @author Ajani Thomas
 *  @author Gary T. Leavens
 */
public class ArrayList extends AbstractList
    implements List, RandomAccess, Cloneable, java.io.Serializable
{

    /*@ public normal_behavior
      @   ensures \result <==> (
      @                  initialAbstractList() &&
      @                  capacity == initialCapacity &&
      @                  isEmpty() &&
      @                  containsNull && elementType == \type(Object));
      @ public pure model boolean initialArrayList(int initialCapacity);
      @*/

    /** This represents the size of the array used to store the elements 
     *  in the ArrayList.
     **/
    /*@ public model int capacity; in objectState;
      @*/

    // METHODS AND CONSTRUCTORS
    /*@ public normal_behavior
      @   requires 0 <= initialCapacity;
      @   ensures initialArrayList(initialCapacity);
      @ also
      @  public exceptional_behavior
      @   requires initialCapacity < 0;
      @   assignable \nothing;
      @   signals_only IllegalArgumentException;
      @*/
    /*@ pure @*/ public ArrayList(int initialCapacity) throws IllegalArgumentException;

    /*@ public normal_behavior
      @   ensures initialArrayList(10);
      @*/
    /*@ pure @*/ public ArrayList();

    /*@  public normal_behavior
      @   requires c != null;
      @   requires c.size()*1.1 <= Integer.MAX_VALUE;
      @   ensures this.size() == c.size();
      @   ensures (\forall int i; 0 <= i && i < c.size();
      @                     this.get(i) == (c.iterator().nthNextElement(i)));
      @   ensures capacity == c.size()*1.1;
      @   ensures containsNull == c.containsNull;
      @   ensures elementType == c.elementType;
      @ also
      @  public exceptional_behavior
      @   requires c == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    /*@ pure @*/ public ArrayList(Collection c) throws NullPointerException;

    /*@  public normal_behavior
      @   assignable objectState;
      @   ensures \not_modified(containsNull,elementType,content.theSize,content,
                                                   theString,theHashCode);
      @   ensures capacity == this.size();
      @*/
    public void trimToSize();

    /*@  public normal_behavior
      @   assignable objectState;
      @   ensures capacity >= minCapacity;
      @   ensures \not_modified(containsNull,elementType,content.theSize,content);
      @   ensures \not_modified(theString,theHashCode);
      @*/
    public void ensureCapacity(int minCapacity);

    // specification inherited from List
    public /*@ pure @*/ int size();

    // specification inherited from List
    public /*@ pure @*/ boolean isEmpty();

    // specification inherited from List
    public /*@ pure @*/ boolean contains(Object elem);

    // specification inherited from List
    public /*@ pure @*/ int indexOf(Object elem);

    // specification inherited from List
    public /*@ pure @*/ int lastIndexOf(Object elem);

    // specification inherited from Object
    /*@ also
      @  public normal_behavior
      @   ensures \result != this;
      @   ensures \result.equals(this);
      @   ensures ((List)\result).containsNull == this.containsNull;
      @   ensures ((List)\result).elementType == this.elementType;
      @   ensures ((ArrayList)\result).content.theSize == this.content.theSize;
      @   //-@ ensures List.equals(((List)\result).content,this.content);
      @   ensures ((ArrayList)\result).theString == this.theString;
      @   ensures ((ArrayList)\result).theHashCode == this.theHashCode;
      @*/
    public /*@ pure @*/ Object clone();

    // specification inherited from List
    public /*@ pure @*/ Object[] toArray();

    // specification inherited from List
    public Object[] toArray(Object[] a);

    // specification inherited from List
    public /*@ pure @*/ Object get(int index);

    // specification inherited from List
    public Object set(int index, Object element);

    // specification inherited from List
    public boolean add(Object o);

    // specification inherited from List
    public void add(int index, Object element);

    // specification inherited from List
    public Object remove(int index);

    // specification inherited from List
    public void clear();

    // specification inherited from List
    public boolean addAll(Collection c);

    // specification inherited from List
    public boolean addAll(int index, Collection c);

    // specification inherited from AbstractList
    protected void removeRange(int fromIndex, int toIndex);
} 
