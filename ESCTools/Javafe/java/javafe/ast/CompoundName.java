/* Copyright 2000, 2001, Compaq Computer Corporation */

package javafe.ast;

import javafe.util.Assert;
import javafe.util.Location;
import javafe.util.ErrorSet;


// Convention: unless otherwise noted, integer fields named "loc" refer
// to the location of the first character of the syntactic unit


public class CompoundName extends Name {
  //@ invariant length>1

  //@ invariant ids!=null
  //@ invariant ids.count == length
  public /*@non_null*/ IdentifierVec ids;


  //@ invariant locIds.length == length
  /*@ invariant (\forall int i; (0<=i && i<locIds.length)
			==> locIds[i]!=Location.NULL) */
  public /*@non_null*/ int[] locIds;


  //@ invariant locDots.length == length-1
  /*@ invariant (\forall int i; (0<=i && i<locDots.length)
			==> locDots[i]!=Location.NULL) */
  public /*@non_null*/ int[] locDots;


  /** Ensure there's at least two identifiers in this name. */
  private void postMake() {
    Assert.notFalse(locIds.length > 1);
  }

  /** Check invariants on sizes. */
  private void postCheck() {
    Assert.notFalse(ids.size() == locIds.length);
    Assert.notFalse(locIds.length > 1);
    Assert.notFalse(locIds.length == locDots.length+1);
  }

  /** Return a String representation of <code>this</code> in Java's
    syntax. */

  public String printName() {
    int sz = ids.size();
    StringBuffer result = new StringBuffer(10*sz);
    for(int i = 0; i < sz; i++) {
      if (i != 0) result.append('.');
      result.append(ids.elementAt(i).toString());
    }
    return result.toString();
  }

  /** Return true if <code>other</code> is a <code>Name</code> that
    is component-wise equal to <code>this</code>. */

  public boolean equals(Object other) {
    if (other instanceof CompoundName) {
      Name o = (CompoundName)other;
      int sz = this.ids.size();
      if (sz != o.size()) return false;
      for(int i = 0; i < sz; i++) {
	if (this.ids.elementAt(i) != o.identifierAt(i)) return false;
      }
      return true;
    } else
      return false;
  }

  /** Return a <code>Name</code> consisting of the first
    <code>len</code> identifiers of <code>this</code>.  Requires that
    <code>len</code> is greater than zero and less than the length of
    <code>this</code>. */

  public Name prefix(int len) {
    Assert.precondition(len > 0 && len <= ids.size());
    if (len == 1)
      return SimpleName.make(ids.elementAt(0), locIds[0]);

    Identifier[] newIds = new Identifier[len];
    int[] newLocIds = new int[len];
    int[] newLocDots = new int[len-1];
    for(int i = 0; i < len; i++) newIds[i] = this.ids.elementAt(i);
    System.arraycopy(locIds, 0, newLocIds, 0, len);
    System.arraycopy(locDots, 0, newLocDots, 0, len-1);
    return make(IdentifierVec.make(newIds), newLocIds, newLocDots);
  }

  /** Return a hash code for <code>this</code> such that two
    <code>Name</code>s that are <code>equals</code> have the same hash
    code. */

  public int hashCode() {
    int result = 0;
    for(int i = 0, sz = ids.size(); i < sz; i++)
      result += ids.elementAt(i).hashCode();
    return result;
  }

    /**
     ** Return the first <code>len</code> identifiers in
     ** <code>this</code> in an array.  Requires that <code>len</code>
     ** be between 0 and length of <code>this</code> inclusive.
     **/
  public String[] toStrings(int len) {
    Assert.precondition(0<=len && len<=ids.size());
    if (len == 0) return emptyStringArray;
    String[] result = new String[len];
    for(int i = 0; i < len; i++)
      result[i] = ids.elementAt(i).toString();
    return result;
  }

  /** Return the number of identifiers in <code>this</code>. */
  public int size() { return ids.size(); }

  /** Return the ith identifier of <code>this</code>. */
  public Identifier identifierAt(int i)
	/*throws ArrayIndexOutOfBoundsException*/ {
    return ids.elementAt(i);
  }

  /** Return the location of the ith identifier of <code>this</code>. */
  public int locIdAt(int i) { return locIds[i]; }
    
  /** Return the location of the dot after the ith identifier of
    <code>this</code>. */
  public int locDotAfter(int i) { return locDots[i]; }

  public int getStartLoc() { return locIds[0]; }


  //@ requires ids.count>1
  //@ requires locIds.length == ids.count
  /*@ requires (\forall int i; (0<=i && i<locIds.length)
			==> locIds[i]!=Location.NULL) */
  //@ requires locDots.length == ids.count-1
  /*@ requires (\forall int i; (0<=i && i<locDots.length)
			==> locDots[i]!=Location.NULL) */
  //
  //@ ensures \result!=null
  public static CompoundName make(/*@non_null*/ IdentifierVec ids, /*@non_null*/ int[] locIds, /*@non_null*/ int[] locDots) {
     //@ set I_will_establish_invariants_afterwards = true
     CompoundName result = new CompoundName();
     //@ set result.length = ids.count
     result.ids = ids;
     result.locIds = locIds;
     result.locDots = locDots;
     result.postMake();
     return result;
  }


// Generated boilerplate constructors:

  /**
   ** Construct a raw CompoundName whose class invariant(s) have not
   ** yet been established.  It is the caller's job to
   ** initialize the returned node's fields so that any
   ** class invariants hold.
   **/
  //@ requires I_will_establish_invariants_afterwards
  protected CompoundName() {}    //@ nowarn Invariant,NonNullInit


// Generated boilerplate methods:

  public final int childCount() {
     int sz = 0;
     if (this.ids != null) sz += this.ids.size();
     return sz + 2;
  }

  public final Object childAt(int index) {
          /*throws IndexOutOfBoundsException*/
     if (index < 0)
        throw new IndexOutOfBoundsException("AST child index " + index);
     int indexPre = index;

     int sz;

     sz = (this.ids == null ? 0 : this.ids.size());
     if (0 <= index && index < sz)
        return this.ids.elementAt(index);
     else index -= sz;

     if (index == 0) return this.locIds;
     else index--;

     if (index == 0) return this.locDots;
     else index--;

     throw new IndexOutOfBoundsException("AST child index " + indexPre);
  }   //@ nowarn Exception

  public final String toString() {
     return "[CompoundName"
        + " ids = " + this.ids
        + " locIds = " + this.locIds
        + " locDots = " + this.locDots
        + "]";
  }

  public final int getTag() {
     return TagConstants.COMPOUNDNAME;
  }

  public final void accept(Visitor v) { v.visitCompoundName(this); }

  public final Object accept(VisitorArgResult v, Object o) {return v.visitCompoundName(this, o); }

  public void check() {
     super.check();
     if (this.ids == null) throw new RuntimeException();
     if (this.locIds == null) throw new RuntimeException();
     if (this.locDots == null) throw new RuntimeException();
     postCheck();
  }

}
