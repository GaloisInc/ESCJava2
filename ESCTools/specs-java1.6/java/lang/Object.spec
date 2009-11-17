// @(#)$Id: Object.spec 2246 2006-12-18 03:03:10Z chalin $

// Copyright (C) 2002 Iowa State University

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

package java.lang;
import java.lang.reflect.Array;

/** JML's specification of java.lang.Object.
 *
 * @version $Revision: 2246 $
 * @author Gary T. Leavens
 * @author Specifications from Compaq SRC's ESC/Java
 * @author David R. Cok
 */
public class Object {

    /** A data group for the complete state of this object.  
     */
    //@ public non_null model JMLDataGroup objectState;

    /** Use this for the private state holding benevolent side effects */
    //@ public non_null model JMLDataGroup privateState; in objectState;

    /** The Object that has a field pointing to this Object.
     * Used to specify (among other things) injectivity (see
     * the ESC/Java User's Manual).
     */
    //@ ghost public nullable Object owner;
        // NB It is inconvenient to include owner in objectState,
        // because generally we do not change the owner after initialization


    /*@ public normal_behavior
      @   assignable \nothing;
      @*/
    public /*@ pure @*/ Object();


    /*@ public normal_behavior
      @   ensures \result == \typeof(this);
      @*/
    //-@ function // Not dependent on the state, just on the object identity
    public /*@ pure @*/ final /*@non_null*/ Class getClass();

    //@ public model int theHashCode; in objectState;

    /*@  public normal_behavior
      @     assignable privateState;
      @     ensures (* \result is a hash code for this object *);
      @     ensures \result == theHashCode;
      @ also public normal_behavior
      @     requires \typeof(this) == \type(Object);
      @     assignable privateState;
      @     ensures \result == System.identityHashCode(this);
      @*/
    public int hashCode();

    //  FIXME - how do we ensure the following
    //     (\forall Object o,oo; oo.equals(o) ==> o.theHashCode == oo.theHashCode);

    /*@  public normal_behavior
      @     requires obj != null;
      @     ensures (* \result is true when obj is "equal to" this object *);
      @  also
      @   public normal_behavior
      @     requires this == obj;
      @     ensures \result;
      @  also
      @   public normal_behavior
      @     requires obj != null && \typeof(this) == \type(Object);
      @     ensures \result <==> this == obj;
      @ also public normal_behavior
      @    ensures obj == null ==> !\result;
        // FIXME - the following causes inconsistency when equals is used in annotations
      @ //also public normal_behavior
      @  //  requires obj != null;
      @   // ensures \result == obj.equals(this);
      @*/
    public /*@ pure @*/ boolean equals(/*@ nullable \readonly @*/ Object obj);

    /*@ protected normal_behavior
      @   requires this instanceof Cloneable;
      @   assignable \nothing;
		// Note: clone is magic.  Object.clone ensures the following
		// strict equality when it is called, and subclasses are
		// expected to fulfill it by calling super.clone();
      @   ensures \typeof(\result) == \typeof(this);
      @  // ensures (* \result is a clone of this *);
      @*/
    /* FIXME  - seems not to reason with isArray @
      @ also protected normal_behavior
      @   requires this.getClass().isArray();
      @   assignable \nothing;
      @   ensures \elemtype(\typeof(\result)) == \elemtype(\typeof(this));
      @   //ensures ((\peer \peer Object[])\result).length
      @   //         == ((\peer \peer Object[])this).length;
      @   //ensures (\forall int i;
      @    //            0<=i && i < ((\peer \peer Object[])this).length;
      @    //            ((\peer \peer Object[])\result)[i]
      @    //             == ((\peer \peer Object[])this)[i] );
      @ also
      @   requires this.getClass().isArray();
      @   // FIXME requires \elemtype(\typeof(this)).isPrimitive();
      @   assignable \nothing;
      @   ensures \elemtype(\typeof(\result)) == \elemtype(\typeof(this));
      @   ensures Array.getLength(\result) == Array.getLength(this);
      @   ensures (\forall int i; 0<=i && i < Array.getLength(this);
      @                Array.get(\result,i).equals(Array.get(this,i))  );
      @*/
    /*@
      @ also protected exceptional_behavior
      @   requires !(this instanceof Cloneable);
      @   assignable \nothing;
      @   signals_only CloneNotSupportedException;
      @*/
// FIXME - is it always true that \result != this
// FIXME - ifnot some derived classes will want to ensure this
/*
      @ also protected normal_behavior
      @   requires \elemtype(\typeof(this)) <: \type(Object);
      @   assignable \nothing;
      @   ensures \result != this;
      @   ensures \elemtype(\typeof(\result)) == \elemtype(\typeof(this));
      @   ensures ((Object[])\result).length == ((Object[])this).length;
      @   ensures (\forall int i; 0<=i && i < ((Object[])this).length;
      @                ((Object[])\result)[i] == ((Object[])this)[i] );
      @ also protected normal_behavior
      @   requires \elemtype(\typeof(this)) == \type(int);
      @   assignable \nothing;
      @   ensures \result != this;
      @   ensures \elemtype(\typeof(\result)) == \elemtype(\typeof(this));
      @   ensures ((int[])\result).length == ((int[])this).length;
      @   ensures (\forall int i; 0<=i && i < ((int[])this).length;
      @                ((int[])\result)[i] == ((int[])this)[i] );
      @ also protected normal_behavior
      @   requires \elemtype(\typeof(this)) == \type(byte);
      @   assignable \nothing;
      @   ensures \result != this;
      @   ensures \elemtype(\typeof(\result)) == \elemtype(\typeof(this));
      @   ensures ((byte[])\result).length == ((byte[])this).length;
      @   ensures (\forall int i; 0<=i && i < ((byte[])this).length;
      @                ((byte[])\result)[i] == ((byte[])this)[i] );
	// FIXME - needs the above replicated for each primitive type
*/
    protected /*@non_null*/ Object clone() throws CloneNotSupportedException;

    /** Use theString as the (pure) model value of toString() */
    //@ public model non_null String theString; in objectState;

    /*@   public normal_behavior
      @     assignable privateState;
      @     ensures \result == theString;
      @     ensures (* \result is a string representation of this object *);
      @ also
      @   public normal_behavior
      @     requires \typeof(this) == \type(Object);
      @     assignable privateState;
      @     ensures \result.equals(getClass().getName() + "@" + 
                                     Integer.toHexString(theHashCode));
      @*/
    public /*@ non_null @*/ String toString();

    public final void notify();

    public final void notifyAll();

    //@ public behavior
    //@   requires timeout >= 0L;
          // FIXME also check thread ownership - IllegalMonitorStateException
    //@   assignable \not_specified;
    //@ also public exceptional_behavior
    //@   requires timeout < 0;
    //@   assignable \not_specified;
    //@   signals_only IllegalArgumentException;
    public final void wait(long timeout) throws InterruptedException;

    //@ public behavior
    //@   requires timeout >= 0L;
    //@   requires 0 <= nanos;
    //@   requires nanos < 1000000;
    //@   assignable \not_specified;
          // FIXME also check thread ownership - IllegalMonitorStateException
    //@ also public exceptional_behavior
    //@   requires timeout < 0 || nanos < 0 || nanos >= 1000000;
    //@   assignable \not_specified;
    //@   signals_only IllegalArgumentException;
    public final void wait(long timeout, int nanos)
        throws InterruptedException;

    public final void wait() throws InterruptedException;

    /*@ protected normal_behavior
      @   requires objectTimesFinalized == 0 ; // FIXME && \lockset.isEmpty();
      @   assignable objectTimesFinalized, objectState;
      @   ensures objectTimesFinalized == 1;
      @ also protected exceptional_behavior
      @   requires objectTimesFinalized == 1;
      @   assignable \not_specified;
      @   signals (Exception) true; // FIXME - what exception?
      @*/
    protected void finalize() throws Throwable;

    /** The number of times this object has been finalized.
     */
    //@ protected ghost int objectTimesFinalized = 0; // not part of objectState

}
