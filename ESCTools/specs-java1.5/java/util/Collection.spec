// @(#)$Id: Collection.spec 71985 2008-07-14 20:09:17Z chalin $

// Adapted in part from Compaq SRC's specification for ESC/Java

// Copyright (C) 2000, 2002 Iowa State University
// Modified 2007, Mobius Project, Systems Research Group, 
//                University College Dublin, Ireland

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

/** JML's specification of java.util.Collection.
 * Part of this specification is adapted from ESC/Java.
 * @version $Revision: 71985 $
 * @author Gary T. Leavens
 * @author Brandon Shilling
 * @author Erik Poll 
 * @author David Cok
 */
public interface Collection {

    //@ public model instance JMLDataGroup localState; in objectState;

    /** A utility method that compares possibly-null objects with equals,
        according to how the Collection classes do the comparison. 
        The presumption that o==oo ==> o.equals(oo) for non-null o,oo is
        also used to help proofs along.
     */
    /*@ public normal_behavior
      @   ensures \result <==> ( o==oo || (o != null && o.equals(oo)));
      @ static public model pure boolean nullequals(Object o, Object oo);
      @*/
    
    /**
     * The (more specific) type of our elements (set by the user of the
     * object to state what is allowed to be added to the collection, and
     * hence what is guaranteed to be retrieved from the collection).  It is
     * not adjusted based on the content of the collection.
     **/
    //@ instance ghost public \TYPE elementType; in localState;

    /**
     * True iff we are allowed to contain null (not whether we do in fact
     * contain null).
     **/
    //@ instance ghost public boolean containsNull; in localState;


    //-@ immutable pure
    /*@ public static model class Content {

            public \bigint theSize;

            //@ public normal_behavior
            //@    ensures true;
            //-@ function pure
            public boolean containsObject(Object o);
        }
      @*/

    //@ public model non_null instance Content content; in localState;
    //@ public invariant content.owner == this;
    //@ public invariant content.theSize >= 0;

    /*@ public normal_behavior
          ensures \result <==> ( content.theSize == 0
                              && containsNull == true
                              && elementType == Object.class);
        pure
         public model boolean initialCollection();
      @*/
    //------------------------------------------------------------

    /*@ public normal_behavior
      @    ensures \result == content.theSize;
      @*/
    /*@ pure @*/ int size();

    /*@ public normal_behavior
      @  ensures \result <==> (content.theSize == 0);
      @*/
    /*@ pure @*/ boolean isEmpty();

    /*@ public behavior
      @   ensures (!containsNull && o == null) ==> !\result;
      @   ensures (o == null) || \typeof(o) <: elementType || !\result;
      @   ensures content.theSize == 0 ==> !\result;
      @   ensures containsObject(o) ==> \result;
      @   //ensures \result <==> (\exists Object oo; containsObject(oo) &&  oo.equals(o)); // FIXME - this crashes Simplify
          // various exceptions may get thrown, but are optional,
          // so they are specified in the implementing classes
          signals_only RuntimeException;
      @*/
    /*@ pure @*/ boolean contains(/*@nullable*/ Object o);

    /*@ public normal_behavior
      @   ensures !containsNull && o == null ==> !\result;
      @   ensures (o == null) || \typeof(o) <: elementType || !\result;
      @   ensures content.theSize == 0 ==> !\result;
      @   ensures \result ==> contains(o);
      @   ensures \result <==> content.containsObject(o);
      @*/
    //@ public model pure boolean containsObject(Object o);


    /*@ public normal_behavior
      @   ensures \result != null;
      @   ensures \fresh(\result);
      @   ensures \result.elementType == elementType;
      @   ensures containsNull == \result.returnsNull;
      @*/
    /* FIXME
      @   ensures (\forall int i; 0 <= i && i < size();
      @                 contains(\result.nthNextElement(i)));
      @   ensures (\forall Object o; contains(o) ==>
      @              (\exists int i; 0 <= i && i < size(); 
      @                 o == \result.nthNextElement(i)));
      @   ensures size() > 0 ==> \result.hasNext((int)(size()-1));
      @   ensures !\result.hasNext((int)(size()));
      @   ensures_redundantly
      @           (\forall int i; 0 <= i && i < size();
      @                 this.contains(\result.nthNextElement(i)));
      @   ensures_redundantly size() != 0 ==> \result.moreElements;
      @*/
    /*@ non_null @*/ /*@ pure @*/ Iterator iterator();

    /*@ public normal_behavior
      @   ensures \result != null;
      @   ensures containsNull || \nonnullelements(\result);
      @   ensures \result.length == content.theSize;
      @   ensures \typeof(\result) == \type(Object[]);
       // NOTE: does not worry about duplicate entries - see subclasses for that
           // FIXME - is the following ok
      // @   ensures (\forall int i; 0<=i && i<\result.length; containsObject(\result[i]));
      @   ensures (\forall Object o; containsObject(o) <==> Arrays.contains(\result,o));
      @*/
    /*@ pure @*//*@non_null*/ Object[] toArray();
       
    /*@ public normal_behavior
      @   requires a!= null;
      @   //requires elementType <: \elemtype(\typeof(a));
      @   requires (\forall Object o; containsObject(o);
      @                                \typeof(o) <: \elemtype(\typeof(a)));
      @   {|
      @     requires content.theSize <= a.length;
      @     assignable a[*];
      @     ensures \result == a;
      @     ensures (\forall Object o; containsObject(o) <==> Arrays.contains(\result,content.theSize,o));
      @     ensures (\forall int i; content.theSize <= i && i < a.length; 
      @                             \result[i] == null);
      @   also
      @     requires content.theSize > a.length;
      @     assignable \nothing;
      @     ensures \result != null;
      @     ensures \fresh(\result) && \result.length == content.theSize;
      @     ensures (\forall Object o; containsObject(o) <==> Arrays.contains(\result,o));
      @     ensures \typeof(\result) == \typeof(a);
      @   |}
      @ also
      @ public exceptional_behavior
      @   requires a == null;
      @   assignable \nothing;
      @   signals_only NullPointerException ;
      @ also
      @ public exceptional_behavior
      @   requires a != null;
      @   //requires !(elementType <: \elemtype(\typeof(a)));
      @   requires !(\forall Object o; containsObject(o);
      @                                \typeof(o) <: \elemtype(\typeof(a)));
      @   assignable a[*];
      @   signals_only ArrayStoreException ;
      @*/
    /*@ non_null @*/ Object[] toArray(/*@non_null*/ Object[] a) throws NullPointerException, ArrayStoreException;

    /*@ public behavior
      @   requires !containsNull ==> o != null;
      @   requires  (o == null) || \typeof(o) <: elementType;
      @   assignable objectState; 
      @   ensures \not_modified(containsNull,elementType);
      @   ensures \old(!contains(o)) ==> \result;
      @   ensures \result ==> content.theSize == \old(content.theSize+1);
      @   ensures !\result ==> content.theSize == \old(content.theSize);
      @   ensures containsObject(o);
      @   ensures (\forall Object _o; \old(containsObject(_o)) ==> containsObject(_o));
      @   ensures (\forall Object oo; o != oo; containsObject(oo) ==> \old(containsObject(oo)));
      @   ensures \old(containsObject(null)) ==> containsObject(null);
      @   ensures o != null ==> (containsObject(null) ==> \old(containsObject(null)));
          // Various unspecified exceptions may be thrown if the element
          // is not allowed to be added to the Collection, or the add
          // operation is not supported.
          
      @*/
    boolean add(/*@nullable*/ Object o);

    /*@ public behavior
      @   requires !containsNull ==> o != null;
      @   requires  (o == null) || \typeof(o) <: elementType;
      @   assignable objectState; 
      @   ensures \not_modified(containsNull,elementType);
      @   ensures \old(contains(o)) <==> \result;
      @   ensures \result ==> content.theSize == \old(content.theSize-1);
      @   ensures !\result ==> content.theSize == \old(content.theSize);
      @   ensures (\forall Object oo; !oo.equals(o); \old(containsObject(oo)) ==> containsObject(oo));
      @   ensures (\forall Object oo; containsObject(oo) ==> \old(containsObject(oo)));
      @   ensures (o!=null && \old(contains(null))) ==> contains(null);
      @   ensures containsObject(null) ==> \old(contains(null));
          // Various unspecified exceptions may be thrown if the element
          // is not allowed to be removed from the Collection, or the remove
          // operation is not supported.
      @*/
    boolean remove(/*@nullable*/ Object o) throws RuntimeException;

    /*@ public behavior
      @   requires c != null && c != this;
      @   ensures \result <==> 
                    (\forall Object o; \old(c.contains(o)) ==> contains(o));
      @ also public normal_behavior
          requires c == this;
          ensures \result;
      @ also public exceptional_behavior
      @  requires c == null;
      @  signals_only NullPointerException;
      @*/
              // FIXME - also the optional exceptions
    /*@ pure @*/ boolean containsAll(/*@non_null*/ Collection c) throws NullPointerException;

    // FIXME - what if c == this in the following calls?

    /*@ public behavior
      @   requires c != null;
      @   requires c.elementType <: elementType;
      @   requires !containsNull ==> !c.containsNull;
      @   assignable objectState; 
      @   ensures \not_modified(containsNull,elementType);
      @   ensures content.theSize >= \old(content.theSize);
      @   ensures !\result <==> (content.theSize == \old(content.theSize));
      @   ensures (\forall Object o; \old(c.contains(o) || contains(o)) <==> contains(o));
      @   ensures \old(c.contains(null) || contains(null)) <==> contains(null);
          // See note in add about exceptions
      @ also public exceptional_behavior
      @  requires c == null;
      @  assignable \nothing;
      @  signals_only NullPointerException;
      @*/
              // FIXME - also the optional exceptions
    boolean addAll(/*@non_null*/ Collection c) throws NullPointerException;

    /*@ public behavior
      @   requires c != null;
      @   assignable objectState; 
      @   ensures \not_modified(containsNull,elementType);
      @   ensures (\forall Object o; ( contains(o) || \old(c.contains(o))) <==>
                                        \old(contains(o)));
      @   ensures (contains(null) || \old(c.contains(null))) <==> \old(contains(null));
      @   ensures content.theSize <= \old(content.theSize);
      @   ensures !\result <==> (content.theSize == \old(content.theSize));
      @   ensures c == this ==> isEmpty();
          // See note in remove about exceptions
      @ also public exceptional_behavior
      @  requires c == null;
      @  assignable \nothing;
      @  signals_only NullPointerException;
      @*/
              // FIXME - also the optional exceptions
    boolean removeAll(/*@non_null*/ Collection c) throws NullPointerException;

    /*@ public behavior
      @   requires c != null;
      @   assignable objectState; 
      @   ensures \not_modified(containsNull,elementType);
          // See note in remove about exceptions
      @   ensures (\forall Object o; contains(o) <==>
				    \old(contains(o) && c.contains(o)) );
      @   ensures contains(null) <==> \old(c.contains(null) && contains(null));
      @   ensures content.theSize <= \old(content.theSize);
      @   ensures !\result <==> (content.theSize == \old(content.theSize));
      @   ensures !\result ==> (\forall Object o; contains(o) <==> \old(contains(o)));
      @   ensures !\result ==> (contains(null) <==> \old(contains(null)));
      @   ensures c == this ==> !\result;
          // See note in remove about exceptions
      @ also public exceptional_behavior
      @  requires c == null;
      @  assignable \nothing;
      @  signals_only NullPointerException;
      @*/
              // FIXME - also the optional exceptions
    boolean retainAll(/*@non_null*/ Collection c) throws NullPointerException;

    /*@ public behavior
      @   assignable objectState; 
      @   ensures \not_modified(containsNull,elementType);
      @   ensures isEmpty();
      @   ensures_redundantly size() == 0;
          // FIXME See note in remove about exceptions
      @*/
    void clear();

    // FIXME
    // Note: if the collections are equal they must have the same size and
    // must at least contain elements in common.  However, the latter two
    // conditions are not sufficient to imply equality for arbitrary collections.
    /*@ also public normal_behavior
      @   requires c != null && c instanceof Collection;
      @   ensures \result ==> ( \forall Object oo; 
      @                         contains(oo) <==> ((Collection)c).contains(oo));
      @   ensures \result ==> (content.theSize == ((Collection)c).content.theSize);
      // Other behavior is inherited
      @*/
    boolean equals(/*@nullable*/ Object c);

    // Specification is inherited
    int hashCode();
    
}
