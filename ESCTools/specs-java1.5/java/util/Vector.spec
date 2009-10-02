// @(#)$Id: Vector.spec 2449 2007-04-26 19:11:31Z mikolas $

// Copyright (C) 1998, 1999 Iowa State University

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


/** JML's specification of java.util.Vector.
 * Some of this specification is taken from ESC/Java.
 * @version $Revision: 2449 $
 * @author Clyde Ruby
 * @author Gary T. Leavens
 * @author David Cok
 * @author Erik Poll
 * @author Mikolas Janota
 */
public class Vector extends AbstractList
       implements List, RandomAccess, Cloneable, java.io.Serializable
{

    //@ public model int maxCapacity; in objectState; 

    protected /*@ spec_public @*/ int capacityIncrement;
    // not included in objectState, because this never changes, except
    // perhaps by direct assignment

    /*@ public invariant maxCapacity >= 0 
      @          && elementCount <= maxCapacity;
      @*/

    // protected members (for type checking in the java.util package
    // or subclasses)
    protected Object[] elementData; //@ in objectState; 
    //@                   maps elementData[*] \into objectState;

    //@ invariant elementData.owner == this; 
    //@ invariant elementData.length >= maxCapacity;

    protected /*@ spec_public @*/ int elementCount; //@ in objectState;

    //@ public invariant 0 <= elementCount;

    // I'm commenting out the following, since this is guaranteed by the postcondition of size()
    // and this only baffles the prover.
    //>>@ public invariant elementCount == size();


    // Public Constructors

    /*@  public normal_behavior
      @    requires initialCapacity >= 0 && capacityIncrement >= 0;
      @    ensures maxCapacity == initialCapacity
      @        && this.capacityIncrement == capacityIncrement;
      @    ensures elementType == \type(Object) && containsNull;
      @    ensures isEmpty();
      @    ensures elementCount == 0;
      @*/
    public /*@ pure @*/ Vector (int initialCapacity, int capacityIncrement);

    /*@  public normal_behavior
      @    requires initialCapacity >= 0;
      @    ensures maxCapacity == initialCapacity
      @        && capacityIncrement == 0;
      @    ensures elementType == \type(Object) && containsNull;
      @    ensures isEmpty();
      @    ensures elementCount == 0;
      @*/
    public /*@ pure @*/ Vector(int initialCapacity);

    /*@  public normal_behavior
      @    assignable objectState;
      @    assignable  maxCapacity, capacityIncrement, elementType, containsNull;
      @    ensures containsNull;
      @    ensures maxCapacity > 0 && capacityIncrement == 0;
      @    ensures elementCount == 0;
      @    ensures content.theSize == 0;
      @    ensures elementType == \type(Object);
      @    ensures isEmpty();
      @*/
    public /*@ pure @*/ Vector();

    /*@ public normal_behavior
      @    requires c != null;
      @    assignable objectState;
      @    assignable maxCapacity, capacityIncrement, elementType, containsNull;
      @    ensures elementCount == c.size();
      @    ensures this.elementType == c.elementType;
      @    ensures this.containsNull == c.containsNull;
      @    ensures this.content.theSize == c.content.theSize;
      @    ensures (\forall Object o; c.contains(o) <==> this.contains(o));
               // FIXME - what about duplicate entries?
      @*/
    public /*@ pure @*/ Vector(/*@non_null*/ Collection c);


    /*@ public normal_behavior
      @    requires anArray != null;
      @    requires elementCount <= anArray.length;
      @    requires elementType <: \elemtype(\typeof(anArray));
      @    assignable anArray[0 .. elementCount - 1];
      @    ensures (\forall int i; 0 <= i && i < elementCount;
      @                             get(i) == anArray[i]);
      @    ensures !containsNull ==> !\nonnullelements(anArray);
      @ also public exceptional_behavior
      @    requires anArray == null;
      @    assignable \not_specified;
      @    signals_only NullPointerException;
      @ also public exceptional_behavior
           requires anArray != null;
           requires (\exists int i; 0<=i && i<elementCount;
                                 !(elementType <: \elemtype(\typeof(anArray))));
      @    assignable \not_specified;
           signals_only ArrayStoreException;
      @*/
    public synchronized void copyInto(Object[] anArray);

    /*@ public normal_behavior
      @    assignable objectState;
      @    ensures \not_modified(theString,theHashCode);
               // theHashCode is not changed because the result does not
               // change equals
      @    ensures \not_modified(elementCount,containsNull,elementType);
      @    ensures \not_modified(content.theSize);
      @    ensures (\forall int i; 0<=i && i<size(); get(i) == \old(get(i)));
      @    ensures maxCapacity == elementCount;
      @*/
    public synchronized void trimToSize();

    //@  public normal_behavior
    //@  {|
    //@    requires minCapacity <= maxCapacity;
    //@    assignable \nothing;
    //@    ensures true;
    //@  also
    //@    requires minCapacity > maxCapacity;
    //@    assignable objectState;
    //@    ensures \not_modified(theString,theHashCode);
    //@           // theHashCode is not changed because the result does not
    //@           // changes equals
    //@    ensures \not_modified(elementCount,containsNull,elementType);
    //@          // FIXME - this is the implementation described for Vector
    //@          // is it required of subclasses?
    /*@    ensures maxCapacity == 
      @ 	(capacityIncrement > 0 &&
      @                 \old(maxCapacity+capacityIncrement)>=minCapacity ?
      @                      \old(maxCapacity+capacityIncrement)  :
      @                 capacityIncrement == 0 &&
      @                 \old(maxCapacity*2) >= minCapacity ?
      @                      \old(maxCapacity*2)  :
      @                      minCapacity);
      @*/
    /*-@     ensures (\forall int i; 0<=i && i<elementCount;
      @			elementData[i] == \old(elementData[i]));
      @*/
    //@ |}
    public synchronized void ensureCapacity(int minCapacity);

    /*@  public normal_behavior
      @  {|
      @    requires 0 <= newSize;
      @    requires newSize <= elementCount;
      @    assignable objectState; 
      @    ensures \not_modified(theString,theHashCode);
      @    ensures \not_modified(containsNull,elementType);
      @    ensures elementCount == newSize; */
    /*-@   ensures (\forall int i; 0<=i && i<elementCount;
      @			elementData[i] == \old(elementData[i])); */
    /*@  also
      @    old int oldSize = elementCount;
      @    requires newSize > elementCount;
      @    assignable objectState;
      @    ensures \not_modified(theString,theHashCode);
      @    ensures \not_modified(containsNull,elementType); */
    /*-@
      @    ensures (\forall int i; 0<=i && i<oldSize;
      @			elementData[i] == \old(elementData[i])); */
    /*@    ensures (\forall int i; oldSize<=i && i < newSize;
      @                           get(i) == null);
      @  |}
      @  also exceptional_behavior
      @    requires newSize < 0;
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public synchronized void setSize(int newSize) throws ArrayIndexOutOfBoundsException;

    /*@  public normal_behavior
      @    ensures \result == maxCapacity;
      @*/
    public /*@ pure @*/ synchronized int capacity();

    /*@ also public normal_behavior
      @    ensures \result == elementCount;
      @*/
    public /*@ pure @*/ synchronized int size();

    /*@ also public normal_behavior
      @    ensures \result == (elementCount == 0);
      @*/
    public /*@ pure @*/ synchronized boolean isEmpty();

    // FIXME
    /*@  public normal_behavior
      @    ensures (* \result is an Enumeration of this Vector *);
      @    ensures \result != null && \result.elementType == elementType
      @         && \result.returnsNull == containsNull;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ Enumeration elements();


    // specification inherited
    public /*@ pure @*/ boolean contains(Object elem);

    // specification inherited
    public /*@ pure @*/ int indexOf(Object elem);

    /*@  public normal_behavior
      @    requires 0 <= index && index < elementCount;
      @    ensures \result == -1 || ( index <= \result && \result < elementCount);
      @    ensures \result == -1 <==> (\forall int i; index<=i && i<size(); 
                                                !nullequals(elem,get(i)));
      @    ensures \result != -1 ==> nullequals(elem,get(\result));
      @    ensures (\forall int i; index<=i && i<\result; !nullequals(elem,get(i)));
             // FIXME - exceptions
      @*/
    public synchronized /*@ pure @*/ int indexOf(Object elem, int index);

    // inherits specification
    public /*@ pure @*/ synchronized int lastIndexOf (Object elem);

    /*@ public normal_behavior
      @    requires 0 <= index && index < elementCount;
      @    ensures -1 <= \result &&  \result <= index;
      @    ensures \result == -1 <==> (\forall int i; 0<=i && i<=index; 
                                                !nullequals(elem,get(i)));
      @    ensures \result != -1 ==> get(\result).equals(elem);
      @    ensures (\forall int i; \result<i && i<=index; 
                                                !nullequals(elem,get(i)));
             // FIXME - exceptions
      @*/
    public /*@ pure @*/ synchronized int lastIndexOf(Object elem, int index);

    /*@  public normal_behavior
      @    requires 0 <= index && index < elementCount;
      @    ensures \result == get(index);
      @ implies_that
      @    requires 0 <= index && index < elementCount;
      @    ensures \result == null || \typeof(\result) <: elementType;
      @    ensures !containsNull ==> \result != null;
      @*/
    public /*@ pure @*/ synchronized Object elementAt(int index);

    /*@  public normal_behavior
      @    requires 0 < elementCount;
      @    ensures \result == get(0);
      @ also
      @  public exceptional_behavior
      @    requires 0 == elementCount;
      @    signals_only NoSuchElementException;
      @
      @ implies_that public normal_behavior
      @    requires elementCount > 0;
      @    ensures \result == null || \typeof(\result) <: elementType;
      @    ensures !containsNull ==> \result != null;
      @*/
    public /*@ pure @*/ synchronized Object firstElement() throws NoSuchElementException;

    /*@  public normal_behavior
      @    requires 0 < elementCount;
      @    ensures \result == get((int)(elementCount-1));
      @ also
      @  public exceptional_behavior
      @    requires 0 == elementCount;
      @    signals_only NoSuchElementException;
      @ implies_that 
      @  public normal_behavior
      @    requires elementCount > 0;
      @    ensures \result == null || \typeof(\result) <: elementType;
      @    ensures !containsNull ==> \result != null;
      @*/
    public /*@ pure @*/ synchronized Object lastElement() throws NoSuchElementException;

    /*@  public normal_behavior
      @    requires 0 <= index && index < elementCount;
      @    requires \typeof(obj) <: elementType;
      @    requires containsNull || obj != null;
      @    assignable objectState;
      @    ensures elementCount == \old(elementCount);
      @    ensures nullequals(obj, get(index));
      @    ensures (\forall int i; 0<=i && i<elementCount && i != index;
                           get(i) == \old(get(i)));
      @ also
      @  public exceptional_behavior
      @    requires !(0 <= index && index < elementCount);
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public synchronized void setElementAt(Object obj, int index);

    /*@  public normal_behavior
      @    requires 0 <= index && index < elementCount;
      @    assignable objectState;
      @    ensures  elementCount == \old(elementCount)-1;
      @    ensures (\forall int i; 0<=i && i<index;
                           get(i) == \old(get(i)));
      @    ensures (\forall int i; index<i && i<\old(elementCount);
                           get(i-1) == \old(get(i)));
      @ also
      @  public exceptional_behavior
      @    requires !(0 <= index && index < elementCount);
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public synchronized void removeElementAt(int index);

    /*@  public normal_behavior
      @    requires obj==null || \typeof(obj) <: elementType;
      @    requires !containsNull ==> obj != null;
      @    requires 0 <= index && index < elementCount;
      @    assignable objectState;
      @    ensures \not_modified(containsNull,elementType);
      @    ensures get(index) == obj;
      @    ensures elementCount == \old(elementCount)+1;
      @    ensures (\forall int i; 0<=i && i<index;
                           get(i) == \old(get(i)));
      @    ensures (\forall int i; index<=i && i<\old(elementCount);
                           get(i+1) == \old(get(i)));
      @ also
      @   public exceptional_behavior
      @    requires !(0 <= index && index <= elementCount);
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @ also
      @  public normal_behavior
      @    requires 0 <= index && index < elementCount;
      @    requires \typeof(this) == \type(java.util.Vector);
      @    {|
      @      requires elementCount < maxCapacity;
      @      modifies objectState;
      @    also
      @      requires elementCount == maxCapacity;
      @      {|
      @        requires capacityIncrement > 0;
      @        assignable objectState;
      @        ensures maxCapacity == \old(maxCapacity) + capacityIncrement;
      @      also
      @        requires capacityIncrement == 0;
      @        assignable objectState;
      @        ensures maxCapacity == \old(maxCapacity) * 2;
      @      |}
      @    |}
      @*/ 
    public synchronized void insertElementAt(Object obj, int index);

    /*@  public normal_behavior
      @    requires \typeof(obj) <: elementType;
      @    requires containsNull || obj!=null;
      @    assignable objectState;
      @    ensures \not_modified(containsNull,elementType);
      @    ensures (obj == null & get(elementCount-1) == null) 
      @            || get(elementCount-1).equals(obj); // or == ?
      @    ensures elementCount == \old(elementCount)+1;
      @ also
      @  public normal_behavior
      @  requires \typeof(this) == \type(java.util.Vector);
      @  {|
      @    requires elementCount < maxCapacity;
      @    assignable objectState;
      @    ensures \not_modified(maxCapacity) && \not_modified(capacityIncrement);
      @  also
      @    requires elementCount == maxCapacity;
      @    {|
      @      requires 0 < capacityIncrement 
      @            && maxCapacity <= Integer.MAX_VALUE - capacityIncrement;
      @      assignable objectState;
      @      ensures maxCapacity == \old(maxCapacity) + capacityIncrement;
      @    also
      @      requires capacityIncrement == 0 && maxCapacity == 0;
      @      assignable objectState;
      @      ensures maxCapacity == 1;
      @    also
      @      requires capacityIncrement == 0
      @            && maxCapacity > 0
      @            && maxCapacity < Integer.MAX_VALUE/2;
      @      assignable objectState;
      @      ensures maxCapacity == \old(maxCapacity) * 2;
      @    |}
      @  |}
      @*/
    public synchronized void addElement(Object obj);

    /*@  public normal_behavior
      @    requires !containsNull ==> obj != null;
      @    requires obj==null || \typeof(obj) <: elementType;
      @  {|
      @    requires contains(obj);
      @    assignable objectState;
      @    ensures elementCount == \old(elementCount)-1 && \result;
             // FIXME - removes first matching element
      @  also
      @    requires !contains(obj);
      @    assignable \nothing;
      @    ensures !\result;
      @  |}
      @*/
    public synchronized boolean removeElement(Object obj);

    /*@  public normal_behavior
      @    assignable objectState;
      @    ensures isEmpty();
      @    ensures elementCount == 0;
      @*/
    public synchronized void removeAllElements();

    /*@ also public behavior
      @ ensures \result != null;
      @ ensures ((Vector)\result).elementCount == elementCount;
      @ ensures ((Vector)\result).containsNull == containsNull;
      @ ensures ((Vector)\result).elementType == elementType;
      @ ensures (\forall int i;0<=i && i<elementCount; get(i) == ((Vector)\result).get(i));
      @*/
    public /*@ pure @*/ synchronized Object clone(); // Overrides Object

    /*@ also public normal_behavior
      @    ensures \result != null && \fresh(\result);
      @    ensures \result.length == elementCount
      @      && (\forall int i; 0 <= i && i < size();
      @                         \result[i] == get(i));
      @*/
    public /*@ pure @*/ synchronized Object[] toArray();

    /*@ also
      @ public normal_behavior
      @    requires a != null
      @             && (\forall int i; 0 <= i && i < elementCount;
      @                     get(i) == null ||
      @                     \typeof(get(i)) <: \elemtype(\typeof(a)) );
      @    {|
      @       requires a.length < elementCount;
      @       assignable \nothing;
      @       ensures \result != null && \fresh(\result)
      @         && \result.length == elementCount
      @         && (\forall int i; 0 <= i && i < elementCount;
      @                            \result[i] == get(i));
      @     also
      @       requires a.length == elementCount;
      @       assignable a[0 .. (elementCount-1)];
      @       ensures \result == a
      @         && (\forall int i; 0 <= i && i < elementCount;
      @                            \result[i] == get(i));
      @     also
      @       requires a.length > elementCount;
      @       assignable a[0 .. elementCount];
      @       ensures \result == a
      @         && (\forall int i; 0 <= i && i < elementCount;
      @                            \result[i] == get(i))
      @         && \result[elementCount] == null;
      @    |}
      @*/
    public synchronized Object[] toArray(Object[] a);

     // FIXME - all inherited?
    /*@ also
      @  public normal_behavior
      @    requires 0 <= index && index < size();
      @    ensures !containsNull ==> \result != null;
      @    //-@ ensures \result == elementData[index];
      @ also
      @  public exceptional_behavior
      @    requires !(0 <= index && index < size());
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public /*@ pure @*/ synchronized Object get(int index);

     // FIXME - all inherited?
    /*@ also
      @  public normal_behavior
      @    requires 0 <= index && index < size();
      @    requires element == null || \typeof(element) <: elementType;
      @    requires containsNull || element != null;
      @    assignable objectState;
      @    ensures elementCount == \old(elementCount);
      @    ensures (element == null & get(index) == null) 
      @            || get(index).equals(element); // or == ?
      @ also
      @  public exceptional_behavior
      @    requires !(0 <= index && index < size());
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public synchronized Object set(int index, Object element);

     // FIXME - all inherited?
    /*@ also
      @ public normal_behavior
      @    requires o == null || \typeof(o) <: elementType;
      @    requires containsNull || o != null;
      @    assignable objectState;
      @    ensures (o == null & get(elementCount-1) == null) 
      @            || get(elementCount-1).equals(o); // or == ?
      @    ensures elementCount == \old(elementCount)+1;
      @*/
    public synchronized boolean add(Object o);

     // FIXME - all inherited?
    /*@ also
      @ public normal_behavior
      @  {|
      @    requires contains(o);
      @    assignable objectState;
      @    ensures \result;
      @    ensures elementCount == \old(elementCount)-1;
      @  also
      @    requires !contains(o);
      @    ensures !\result;
      @  |}
      @*/
    public boolean remove(Object o);

     // FIXME - all inherited?
    /*@ also public normal_behavior
      @    requires 0 <= index && index <= size();
      @    requires element == null || \typeof(element) <: elementType;
      @    requires containsNull || element != null;
      @    assignable objectState;
      @    ensures elementCount == \old(elementCount)+1;
      @    ensures (element == null & get(index) == null) 
      @            || get(index).equals(element); // or == ?
      @ also
      @   public exceptional_behavior
      @    requires !(0 <= index && index <= size());
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public void add(int index, Object element);

     // FIXME - all inherited?
    /*@ also
      @  public normal_behavior
      @    requires 0 <= index && index < size();
      @    assignable objectState;
      @    ensures elementCount == \old(elementCount)-1;
      @ also
      @  public exceptional_behavior
      @    requires !(0 <= index && index < size());
      @    assignable \nothing;
      @    signals_only ArrayIndexOutOfBoundsException;
      @*/
    public synchronized Object remove(int index);

     // FIXME - all inherited?
    /*@ also
      @  public normal_behavior
      @    assignable objectState;
      @    ensures isEmpty();
      @*/
    public void clear();

    // specification inherited from List
    public /*@ pure @*/ synchronized boolean containsAll(Collection c);

    // specification inherited from List
    public synchronized boolean addAll(Collection c);

    // specification inherited from List
    public synchronized boolean removeAll(Collection c);

    // specification inherited from List
    public synchronized boolean retainAll(Collection c);

    // specification inherited from List
    public synchronized boolean addAll(int index, Collection c);

    // specification inherited from List
    public /*@ pure @*/ synchronized boolean equals(Object o);

    // specification inherited from List
    public /*@ pure @*/ synchronized int hashCode();

    /*@  also
      @  public normal_behavior
      @    assignable privateState;
      @    ensures (* \result is a string representation of this Vector *);
      @*/
    public synchronized String toString();

    // specification inherited from List
    //@ pure
    public synchronized List subList(int fromIndex, int toIndex);

    // specification inherited from AbstractList
    protected void removeRange(int fromIndex, int toIndex);



}
