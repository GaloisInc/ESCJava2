/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava.ast;


import java.util.Hashtable;

import javafe.ast.*;
import escjava.ast.Visitor;      // Work around 1.0.2 compiler bug
import escjava.ast.TagConstants; // Work around 1.0.2 compiler bug
import escjava.ast.GeneratedTags;// Work around 1.0.2 compiler bug
import escjava.ast.AnOverview;   // Work around 1.0.2 compiler bug
import javafe.util.Assert;
import javafe.util.Location;


// Convention: unless otherwise noted, integer fields named "loc"g refer
// to the locaction of the first character of the syntactic unit

//# TagBase javafe.tc.TagConstants.LAST_TAG + 1
//# VisitorRoot javafe.ast.Visitor




public class SimpleStmtPragma extends StmtPragma {
  public int tag;

  public int loc;


  public final int getTag() { return tag; }

  private void postCheck() {
    boolean goodtag = (tag == TagConstants.UNREACHABLE);
    Assert.notFalse(goodtag);
  }

  public int getStartLoc() { return loc; }


// Generated boilerplate constructors:

  /**
   ** Construct a raw SimpleStmtPragma whose class invariant(s) have not
   ** yet been established.  It is the caller's job to
   ** initialize the returned node's fields so that any
   ** class invariants hold.
   **/
  //@ requires I_will_establish_invariants_afterwards
  protected SimpleStmtPragma() {}    //@ nowarn Invariant,NonNullInit


// Generated boilerplate methods:

  public final int childCount() {
     return 0;
  }

  public final Object childAt(int index) {
          /*throws IndexOutOfBoundsException*/
     if (index < 0)
        throw new IndexOutOfBoundsException("AST child index " + index);
     int indexPre = index;

     int sz;

     throw new IndexOutOfBoundsException("AST child index " + indexPre);
  }   //@ nowarn Exception

  public final String toString() {
     return "[SimpleStmtPragma"
        + " tag = " + this.tag
        + " loc = " + this.loc
        + "]";
  }

  public final void accept(javafe.ast.Visitor v) { 
    if (v instanceof Visitor) ((Visitor)v).visitSimpleStmtPragma(this);
  }

  public final Object accept(javafe.ast.VisitorArgResult v, Object o) { 
    if (v instanceof VisitorArgResult) return ((VisitorArgResult)v).visitSimpleStmtPragma(this, o); else return null;
  }

  public void check() {
     postCheck();
  }

  //@ ensures \result!=null
  public static SimpleStmtPragma make(int tag, int loc) {
     //@ set I_will_establish_invariants_afterwards = true
     SimpleStmtPragma result = new SimpleStmtPragma();
     result.tag = tag;
     result.loc = loc;
     return result;
  }
}
