/* Copyright 2000, 2001, Compaq Computer Corporation */

package javafe.ast;

import javafe.util.Assert;
import javafe.util.Location;
import javafe.util.ErrorSet;


// Convention: unless otherwise noted, integer fields named "loc" refer
// to the location of the first character of the syntactic unit


/** Represents a LocalVariableDeclarationStatement.
 *  The modifiers field of LocalVarDecl allow for future extensibility.
 */

public class LocalVarDecl extends GenericVarDecl {
  public VarInit init;


  // The "locAssignOp" field is used only if "init" is non-null
  public int locAssignOp;


  private void postCheck() {
    // Check invariants on modifiers...
    // should be liberal...
  }

  public int getEndLoc() {
    if (init==null)
      return super.getEndLoc();

    return init.getEndLoc();
  }


// Generated boilerplate constructors:

  /**
   ** Construct a raw LocalVarDecl whose class invariant(s) have not
   ** yet been established.  It is the caller's job to
   ** initialize the returned node's fields so that any
   ** class invariants hold.
   **/
  //@ requires I_will_establish_invariants_afterwards
  protected LocalVarDecl() {}    //@ nowarn Invariant,NonNullInit


// Generated boilerplate methods:

  public final int childCount() {
     int sz = 0;
     if (this.pmodifiers != null) sz += this.pmodifiers.size();
     return sz + 3;
  }

  public final Object childAt(int index) {
          /*throws IndexOutOfBoundsException*/
     if (index < 0)
        throw new IndexOutOfBoundsException("AST child index " + index);
     int indexPre = index;

     int sz;

     sz = (this.pmodifiers == null ? 0 : this.pmodifiers.size());
     if (0 <= index && index < sz)
        return this.pmodifiers.elementAt(index);
     else index -= sz;

     if (index == 0) return this.id;
     else index--;

     if (index == 0) return this.type;
     else index--;

     if (index == 0) return this.init;
     else index--;

     throw new IndexOutOfBoundsException("AST child index " + indexPre);
  }   //@ nowarn Exception

  public final String toString() {
     return "[LocalVarDecl"
        + " modifiers = " + this.modifiers
        + " pmodifiers = " + this.pmodifiers
        + " id = " + this.id
        + " type = " + this.type
        + " locId = " + this.locId
        + " init = " + this.init
        + " locAssignOp = " + this.locAssignOp
        + "]";
  }

  public final int getTag() {
     return TagConstants.LOCALVARDECL;
  }

  public final void accept(Visitor v) { v.visitLocalVarDecl(this); }

  public final Object accept(VisitorArgResult v, Object o) {return v.visitLocalVarDecl(this, o); }

  public void check() {
     super.check();
     if (this.pmodifiers != null)
        for(int i = 0; i < this.pmodifiers.size(); i++)
           this.pmodifiers.elementAt(i).check();
     if (this.id == null) throw new RuntimeException();
     this.type.check();
     if (this.init != null)
        this.init.check();
     postCheck();
  }

  //@ requires type.syntax
  //@ requires locId!=javafe.util.Location.NULL
  //@ ensures \result!=null
  public static LocalVarDecl make(int modifiers, ModifierPragmaVec pmodifiers, /*@non_null*/ Identifier id, /*@non_null*/ Type type, int locId, VarInit init, int locAssignOp) {
     //@ set I_will_establish_invariants_afterwards = true
     LocalVarDecl result = new LocalVarDecl();
     result.modifiers = modifiers;
     result.pmodifiers = pmodifiers;
     result.id = id;
     result.type = type;
     result.locId = locId;
     result.init = init;
     result.locAssignOp = locAssignOp;
     return result;
  }
}
