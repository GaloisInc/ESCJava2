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



public class WildRefExpr extends Expr {
  public /*@non_null*/ Expr expr;

  public int locOpenBracket;

  public int locCloseBracket;


  public int getStartLoc() { return expr.getStartLoc(); }
  public int getEndLoc() { return expr.getEndLoc(); }


// Generated boilerplate constructors:

  /**
   ** Construct a raw WildRefExpr whose class invariant(s) have not
   ** yet been established.  It is the caller's job to
   ** initialize the returned node's fields so that any
   ** class invariants hold.
   **/
  //@ requires I_will_establish_invariants_afterwards
  protected WildRefExpr() {}    //@ nowarn Invariant,NonNullInit


// Generated boilerplate methods:

  public final int childCount() {
     return 1;
  }

  public final Object childAt(int index) {
          /*throws IndexOutOfBoundsException*/
     if (index < 0)
        throw new IndexOutOfBoundsException("AST child index " + index);
     int indexPre = index;

     int sz;

     if (index == 0) return this.expr;
     else index--;

     throw new IndexOutOfBoundsException("AST child index " + indexPre);
  }   //@ nowarn Exception

  public final String toString() {
     return "[WildRefExpr"
        + " expr = " + this.expr
        + " locOpenBracket = " + this.locOpenBracket
        + " locCloseBracket = " + this.locCloseBracket
        + "]";
  }

  public final int getTag() {
     return TagConstants.WILDREFEXPR;
  }

  public final void accept(javafe.ast.Visitor v) { 
    if (v instanceof Visitor) ((Visitor)v).visitWildRefExpr(this);
  }

  public final Object accept(javafe.ast.VisitorArgResult v, Object o) { 
    if (v instanceof VisitorArgResult) return ((VisitorArgResult)v).visitWildRefExpr(this, o); else return null;
  }

  public void check() {
     this.expr.check();
  }

  //@ ensures \result!=null
  public static WildRefExpr make(/*@non_null*/ Expr expr, int locOpenBracket, int locCloseBracket) {
     //@ set I_will_establish_invariants_afterwards = true
     WildRefExpr result = new WildRefExpr();
     result.expr = expr;
     result.locOpenBracket = locOpenBracket;
     result.locCloseBracket = locCloseBracket;
     return result;
  }
}
