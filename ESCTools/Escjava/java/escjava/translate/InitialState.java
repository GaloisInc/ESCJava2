/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava.translate;


import java.util.Hashtable;
import java.util.Enumeration;

import javafe.ast.*;
import javafe.tc.TypeSig;
import javafe.util.Location;

import escjava.ast.GhostDeclPragma;
import escjava.ast.TagConstants;
import escjava.ast.NaryExpr;

import escjava.backpred.FindContributors;


/**
 ** This class provides two methods used in the generation of a
 ** verification condition for a method or constructor (see section 8
 ** of ESCJ 16). <p>
 **/

public final class InitialState {
  private Hashtable premap;
  private Expr is;

  public InitialState(FindContributors scope) {
    premap = new Hashtable();
    ExprVec conjuncts = ExprVec.make();

    // static fields and instance variables
    Enumeration enum = scope.fields();
    while (enum.hasMoreElements()) {
	  FieldDecl fd = (FieldDecl)enum.nextElement();
	  
	  VariableAccess va = TrAnExpr.makeVarAccess(fd, Location.NULL);
	  VariableAccess variant = addMapping(fd);

	  // g@pre == g    and    f@pre == f
	  conjuncts.addElement(GC.nary(TagConstants.ANYEQ, variant, va));
	  Expr typeCorrect;
	  if (Modifiers.isStatic(fd.modifiers)) {
	    // TypeCorrect[[ g ]]
	    typeCorrect = TrAnExpr.typeCorrect(fd);
	  } else {
	    // FieldTypeCorrect[[ f ]]
	    typeCorrect = TrAnExpr.fieldTypeCorrect(fd);
	  }
	  conjuncts.addElement(typeCorrect);
    }

    // elems@pre == elems
    conjuncts.addElement(GC.nary(TagConstants.ANYEQ,
				 addMapping(GC.elemsvar.decl), GC.elemsvar));
    // ElemsTypeCorrect[[ elems ]]
    conjuncts.addElement(TrAnExpr.elemsTypeCorrect(GC.elemsvar.decl));

    // LS == asLockSet(LS)
    conjuncts.addElement(GC.nary(TagConstants.ANYEQ, GC.LSvar,
				 GC.nary(TagConstants.ASLOCKSET, GC.LSvar)));

    // alloc@pre == alloc
    conjuncts.addElement(GC.nary(TagConstants.ANYEQ,
				 addMapping(GC.allocvar.decl), GC.allocvar));

    // conjoin the conjuncts
    is = GC.and(conjuncts);
  }

  private VariableAccess addMapping(GenericVarDecl vd) {
    VariableAccess variant = GetSpec.createVarVariant(vd, "pre");
    premap.put(vd, variant);
    return variant;
  }
  
  public Hashtable getPreMap() {
    return premap;
  }
  
  public Expr getInitialState() {
    return is;
  }
}
