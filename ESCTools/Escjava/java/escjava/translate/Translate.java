/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava.translate;


import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

import javafe.ast.*;
import javafe.util.Location;
import javafe.util.StackVector;
import javafe.util.Assert;
import javafe.util.Set;
import javafe.util.ErrorSet;
import javafe.util.Info;
import javafe.tc.ConstantExpr;
import javafe.tc.TypeSig;

import escjava.Main;

import escjava.ast.*;
import escjava.ast.TagConstants;
import escjava.tc.FlowInsensitiveChecks;

import escjava.backpred.FindContributors;

import escjava.tc.Types;
import escjava.tc.TypeCheck;

public final class Translate {

  /**
   ** Translates the body of a method or constructor, as described in
   ** ESCJ 16, section 8. <p>
   **
   ** Requires:  <code>rd.body != null</code>
   **
   **
   ** predictedSynTargs: for correct translation, this must contain an
   **                    upper bound for the syntactic targets of the
   **                    result of this call.  A value of null is
   **                    taken to represent the set of all variables &
   **                    is hence always a safe value.
   **
   ** Note: passing a value of <the empty set> for predictedSynTargs
   **       will give a translation missing assert statements for
   **       checking call preconditions, but otherwise correct.  The
   **       resulting computation runs faster than passing null, while
   **       still having the same set of targets.  escjava.Main is
   **       currently using this trick as a kludge to compute the
   **       syntactic targets upper bound.
   **/

  public GuardedCmd trBody(RoutineDecl rd, FindContributors scope,
			   Hashtable premap, Set predictedSynTargs,
			   Translate inlineParent, boolean issueCautions) {

    // Reset the state of the AuxInfo module if this is top-level call to trBody
    if (inlineParent == null) {
      AuxInfo.reset();
    }

    // Reset the internal state of <code>this</code>
    this.rdCurrent = rd;
    this.scope = scope;
    this.predictedSynTargs = predictedSynTargs;
    this.inlineParent = inlineParent;
    if (inlineParent == null) {
      this.inConstructorContext = isStandaloneConstructor(rd);
    } else {
      this.inConstructorContext = inlineParent.inConstructorContext &&
                             rdCurrent.parent == inlineParent.rdCurrent.parent;
    }
    this.issueCautions = issueCautions;

    if (Info.on) {
      System.out.print("trBody: ");
      for (Translate ttIR = inlineParent;
	   ttIR != null;
	   ttIR = ttIR.inlineParent) {
	System.out.print("  ");
      }
      System.out.println(TypeCheck.inst.getSig(rd.parent).toString() + "." +
			 TypeCheck.inst.getRoutineName(rd) +
			 TypeCheck.inst.getSignature(rd));
      System.out.flush();
    }

    code.clear(); code.push();  // this mark popped by "spill"
    declaredLocals.clear();
    temporaries.clear(); temporaries.push();  // this mark popped by "spill"
    tmpcount = 0;

    GC.thisvar.decl.type = scope.originType;

    code.push();  // this mark popped below
    
    /*
     * Translate body:
     */
    if (rd.getTag() == TagConstants.METHODDECL) {
      if (! Modifiers.isSynchronized(rd.modifiers)) {
	// non-synchronized method
	trStmt(rd.body);
      } else if (! Modifiers.isStatic(rd.modifiers)) {
	// synchronized instance method
	trSynchronizedBody(GC.thisvar, rd.body, rd.locOpenBrace);
      } else {
	// synchronized static method
	trSynchronizedBody(GC.nary(TagConstants.CLASSLITERALFUNC,
				   getClassObject(rd.parent)),
			   rd.body, rd.locOpenBrace);
      }      
    } else {
      Assert.notFalse(rd.getTag() == TagConstants.CONSTRUCTORDECL);
      trConstructorBody((ConstructorDecl)rd, premap);
    }


    // "code" now contains two marks followed by what ESCJ 16 calls "CS"
    // (except for the "assume !isAllocated(objectToBeConstructed, alloc)",
    // which has already been prepended to "code" here)
    if (Main.traceInfo > 0 &&
	(inlineParent != null || Main.traceInfo > 1)) {
      // add a label to track the implicit return ("falling off the end
      // of the routine")
      int loc = rd.getEndLoc();
      Assert.notFalse(loc != Location.NULL);
      GuardedCmd g = traceInfoLabelCmd(loc, loc, "ImplicitReturn", loc);
      code.addElement(g);
    }
    code.addElement(GC.gets(GC.ecvar, GC.ec_return));
    code.addElement(GC.trycmd(GC.seq(GuardedCmdVec.popFromStackVector(code)),
			      GC.skip()));
    if (rd.getTag() == TagConstants.CONSTRUCTORDECL) {
      code.addElement(GC.gets(GC.resultvar, GC.thisvar));
    }
    GuardedCmd body = spill();
    // "code" is now empty:
    Assert.notFalse(code.vectors()==1 && code.size()==0);

    // Finally, if there are any formal parameters, wrap "body" in a
    // statement that saves and restores the values of the formal parameters
    GuardedCmd res;
    if (rd.args.size() == 0) {
      res = body;
    }
    else {
	Hashtable paramMap = GetSpec.makeSubst(rd.args, "pre");
	
	declaredLocals.push();  // this mark popped by "popDeclBlock"
	code.push();  // this mark popped by "popDeclBlock"
	
	VariableAccess[] ppp = new VariableAccess[rd.args.size() * 2];
	for (int i = 0; i < rd.args.size(); i++) {
	    FormalParaDecl arg = rd.args.elementAt(i);
	    VariableAccess va = (VariableAccess)paramMap.get(arg);
	    declaredLocals.addElement(va.decl);
	    ppp[2*i] = TrAnExpr.makeVarAccess(arg, Location.NULL);
	    ppp[2*i+1] = va;
	}
	for (int i = 0; i < ppp.length; i += 2) {
	    code.addElement(GC.gets(ppp[i+1], ppp[i]));
	}
	code.addElement(body);
	for (int i = 0; i < ppp.length; i += 2) {
	    code.addElement(GC.restoreFrom(ppp[i], ppp[i+1]));
	}
	res = popDeclBlock();
    }
    return res;
  }

  /** Return <code>true</code> when <code>rd</code> is a constructor
    * that does not call a sibling constructor.
    **/

  private boolean isStandaloneConstructor(/*@ non_null */ RoutineDecl rd) {
    if (!(rd instanceof ConstructorDecl)) {
      return false;
    }
    ConstructorDecl cd = (ConstructorDecl)rd;
    // From here on, return "true" unless there is a call to a sibling
    // constructor.

    if (cd.body.getTag() != TagConstants.BLOCKSTMT) {
      return true;
    }
    GenericBlockStmt body = (GenericBlockStmt)cd.body;

    if (body.stmts.size() == 0) {
      return true;
    }
    Stmt s0 = body.stmts.elementAt(0);

    if (s0.getTag() != TagConstants.CONSTRUCTORINVOCATION) {
      return true;
    }
    ConstructorInvocation ccall = (ConstructorInvocation)s0;

    return ccall.decl.parent != cd.parent;
  }

    /**
     ** Auxiliary routine used by trBody to translate the body of a
     ** constructor, as described in ESCJ 16, section 8. <p>
     **
     ** Requires:  <code>cd.body != null</code>
     **/
    private void trConstructorBody(ConstructorDecl cd,
				   Hashtable premap) {
	// assume !isAllocated(objectToBeConstructed, alloc);
	{
	    Expr isAllocated = GC.nary(TagConstants.ISALLOCATED,
				       GC.objectTBCvar, GC.allocvar);
	    code.addElement(GC.assume(GC.not(isAllocated)));
	}


	/*
	 * Find the call to the superclass or sibling constructor, if
	 * any.  In particular, set both "body" and "ccall" to
	 * non-"null" values if "cd.body" starts with a constructor
	 * call.  ("ccall" is non-"null" only if "body" is, too.)
	 */
	GenericBlockStmt body = null;
	ConstructorInvocation ccall = null;
	if (cd.body.getTag() == TagConstants.BLOCKSTMT) {
	    body = (GenericBlockStmt)cd.body;
	    if (1 <= body.stmts.size()) {
		Stmt s0 = body.stmts.elementAt(0);
		if (s0.getTag() == TagConstants.CONSTRUCTORINVOCATION) {
		    ccall = (ConstructorInvocation)s0;
		}
	    }
	}


	if (ccall==null) {
	    /*
	     * Here "cd" refers to a constructor of class "Object"
	     * that does not call any sibling constructor.
	     *
	     * The code that used to be here can be found in revision
	     * 1.87 of this file (Translate.java); it is probably
	     * somewhat "rotted", though.
	     */
	    Assert.notFalse(Types.isSameType(TypeSig.getSig(cd.parent),
					     Types.javaLangObject()));
	    Assert.notImplemented("Checking of Object constructor body");
	}


	TypeDecl tdecl = cd.parent;
	TypeSig type = TypeSig.getSig(tdecl);
	try {
	    if (!type.isTopLevelType())
		Inner.firstThis0 = Inner.getEnclosingInstanceArg(cd);

	    trConstructorCallStmt(ccall);
	} finally {
	    Inner.firstThis0 = null;
	}


	// assume typeof(this) <: T
	TypeExpr texpr = TypeExpr.make(tdecl.getStartLoc(),
				       tdecl.getEndLoc(),
				       type);
	Expr goodType = GC.nary(TagConstants.TYPELE,
				GC.nary(TagConstants.TYPEOF, GC.thisvar),
				texpr);
	code.addElement(GC.assume(goodType));
	
	// assume objectToBeConstructed == this;
	code.addElement(GC.assume(GC.nary(TagConstants.REFEQ,
					  GC.objectTBCvar,
					  GC.thisvar)));

	/*
	 * Insert this.this$0 = this$0arg if we are an inner-class constructor:
	 */
	{
	    TypeSig T = TypeSig.getSig(cd.parent);
	    if (!T.isTopLevelType()) {
		FieldDecl this0 = Inner.getEnclosingInstanceField(T);
		FormalParaDecl this0arg = Inner.getEnclosingInstanceArg(cd);
		
		code.addElement(GC.subgets(
			   TrAnExpr.makeVarAccess(this0, Location.NULL),
			   GC.thisvar,
			   TrAnExpr.makeVarAccess(this0arg, Location.NULL)));
	    }
	}

	
	if (ccall.decl.parent != cd.parent) {
	    // superclass (not sibling) constructor call:
	    instanceInitializers(cd.parent);
	}

	// call "trStmt" on the rest of the body:
	declaredLocals.push();  // this mark popped by "wrapUpDeclBlock"
	code.push();            // this mark popped by "wrapUpDeclBlock"
	for (int i = 1; i < body.stmts.size(); i++) {
	    trStmt(body.stmts.elementAt(i));
	}
	wrapUpDeclBlock();
    }



  private TypeExpr getClassObject(TypeDecl tdClass) {
    Type type = TypeSig.getSig(tdClass);
    TypeExpr texpr = TypeExpr.make(tdClass.getStartLoc(), tdClass.getEndLoc(),
				   type);
    return texpr;
  }
  

  /** This method implements "InstanceInitializers", as described in
    * section 8.1 of ESCJ 16.
    **/  
  private void instanceInitializers(/*@ non_null */ TypeDecl tdecl) {
    // First, provide zero-equivalent values for fields in first-inherited
    // interfaces
    if (tdecl instanceof ClassDecl) {
      Enumeration enum = GetSpec.getFirstInheritedInterfaces((ClassDecl)tdecl);
      while (enum.hasMoreElements()) {
	TypeDecl tdSuperInterface = (TypeDecl)enum.nextElement();
	instanceInitializeZero(tdSuperInterface);
      }
    }
    // Then, provide zero-equivalent values for fields in "tdecl"
    instanceInitializeZero(tdecl);

    // Finally, compute the programmer-supplied initial values and assign
    // the fields appropriately.  (Note, first-inherited interfaces don't
    // have programmer-supplied initial values for instance fields, since
    // the only instance fields in interfaces are ghost fields and they
    // don't have initial values.)
    for (int i = 0; i < tdecl.elems.size(); i++) {
      TypeDeclElem tde = tdecl.elems.elementAt(i);
      if (tde instanceof GhostDeclPragma)
	  tde = ((GhostDeclPragma)tde).decl;

      if (tde.getTag() == TagConstants.INITBLOCK) {
	InitBlock ib = (InitBlock)tde;
	if (!Modifiers.isStatic(ib.modifiers)) {
	  trStmt(ib.block);
	}
      } else if (tde.getTag() == TagConstants.FIELDDECL) {
	FieldDecl fd = (FieldDecl)tde;
	if (!Modifiers.isStatic(fd.modifiers) && fd.init != null) {
	  Assert.notFalse(fd.locAssignOp != Location.NULL);
	  // e= Expr[[ fd.init ]]
	  Expr e = ptrExpr(fd.init);
	  // WriteCheck[[ f[this], e ]]
	  VariableAccess f = TrAnExpr.makeVarAccess(fd, Location.NULL);
	  Expr lhs = GC.select(f, GC.thisvar);
	  writeCheck(lhs, fd.init, e, fd.locAssignOp, true);
	  // f[this] = e
	  code.addElement(GC.subgets(f, GC.thisvar, e));
	}
      }
    }
  }

  /** Called by <code>instanceInitializers</code>.
   **/
  private void instanceInitializeZero(/*@ non_null */ TypeDecl tdecl) {
    for (int i = 0; i < tdecl.elems.size(); i++) {
      TypeDeclElem tde = tdecl.elems.elementAt(i);
      if (tde instanceof GhostDeclPragma)
	  tde = ((GhostDeclPragma)tde).decl;

      if (tde.getTag() == TagConstants.FIELDDECL) {
	FieldDecl fd = (FieldDecl)tde;
	if (!Modifiers.isStatic(fd.modifiers)) {
	  // f[this] = <default value>
	  VariableAccess fdref = TrAnExpr.makeVarAccess(fd, Location.NULL);
	  Expr defaultValue = null;
	  switch (fd.type.getTag()) {
	    case TagConstants.BOOLEANTYPE:
	      defaultValue = GC.falselit;
	      break;
	      
	    case TagConstants.INTTYPE:
	    case TagConstants.LONGTYPE:
	    case TagConstants.CHARTYPE:
	    case TagConstants.BYTETYPE:
	    case TagConstants.SHORTTYPE:
	      defaultValue = GC.zerolit;
	      break;

	    case TagConstants.ARRAYTYPE:
	    case TagConstants.TYPENAME:
	    case TagConstants.TYPESIG:
	      if (GetSpec.NonNullPragma(fd) != null) {
		defaultValue = temporary(fd.id.toString() + "@zero",
					 fd.getStartLoc());
	      } else {
		defaultValue = GC.nulllit;
	      }
	      break;

	    case TagConstants.DOUBLETYPE:
	    case TagConstants.FLOATTYPE:
	      defaultValue = GC.nary(TagConstants.CAST, GC.zerolit,
				     GC.typeExpr(fd.type));
	      break;

	    case TagConstants.TYPECODE:
	      // TYPE fields have no default value:
	      defaultValue = temporary(fd.id.toString() + "@zero",
				       fd.getStartLoc());
	      break;

	    case TagConstants.NULLTYPE:
	    default:
	      /*@ unreachable */
	      Assert.fail("Unexpected type tag");
	      break;
	  }
	  if (defaultValue!=null)
	    code.addElement(GC.subgets(fdref, GC.thisvar, defaultValue));
	}
      }
    }
  }

  //// Instance variables

  /** References the routine currently being checked by trBody. */

  private RoutineDecl rdCurrent;

  /** Singly-linked list of the inline parents.  Is <code>null</code>
    * if this translation is not being inlined.
    **/

  private Translate inlineParent;

  /** Indicates whether to issue cautions.  Value is set from
    * outer call to trBody and also used by nested/recursive calls.
    **/

  private boolean issueCautions;

  /** Indicates whether or not the current routine is in a "constructor
    * context", meaning that it is a constructor being checked or a
    * method in the same class that's being inlined into the constructor.
    **/

  private boolean inConstructorContext;

  /** Contains the guarded commands generated so far for the current
      method.  As the translation enters into Java blocks,
      <code>code</code> gets pushed.  As blocks are left,
      <code>code</code> is poped into a <code>GuardedCmdVec</code> which
      is wrapped inside a BLOCK guarded command that gets appended onto
      <code>code</code>. */

  private final StackVector code = new StackVector();

  /** List of loop invariant pragmas seen so far (and not yet used)
      within the current method.  
  */

  private ExprStmtPragmaVec loopinvariants = ExprStmtPragmaVec.make();

  /** List of loop decreases pragmas seen so far (and not yet used)
      within the current method.  
  */

  private ExprStmtPragmaVec loopDecreases = ExprStmtPragmaVec.make();

  private ExprStmtPragmaVec loopPredicates = ExprStmtPragmaVec.make();
  
  protected LocalVarDeclVec skolemConstants = LocalVarDeclVec.make();

  /** Contains the local Java variables declared in the current
      <em>block</em> so far for the current block and enclosing blocks
      of the current method.  This variable is maintained parallel to
      <code>code</code>: Each time a Java block is entered,
      <code>declaredLocals</code> gets pushed; when a block is left,
      <code>declaredLocals</code> is popped into a
      <code>GenericVarDeclVec</code> that gets wrapped inside a BLOCK
      guarded command. */

  private final StackVector declaredLocals = new StackVector();


  /** Contains the temporaries that generated for the current method
      that haven't yet been declared in a VARINCMD. */

  private final StackVector temporaries = new StackVector();


  /** Describes the current scope. */

  private FindContributors scope;

  /**
   ** Describes the current predicted set of synTargs. <p>
   **
   ** If non-null, then represents an *upper* bound on freeVars of the
   ** result of the current trBody(...) call.
   **/

  private Set predictedSynTargs;

  /**
   ** Describes what aspects of an inlined call to check and what aspects
   ** to either assert or simply ignore.  A call (MethodInvocation,
   ** ConstructorInvocation, NewInstanceExpr) may map to an
   ** <code>InlineSettings</code> object in which the
   ** <code>nextInlineCheckDepth</code> and
   ** <code>nextInlineDepthPastCheck</code> fields are unused. <p>
   **
   ** This variable is used only for some call-site specific inlining,
   ** in particular, currently only to handle the -inlineConstructors flag.
   ** Other reasons for inlining are taken care of in method
   ** <code>computeInlineSettings</code>.
   **/

  public static final ASTDecoration inlineDecoration =
                              new ASTDecoration("inlineDecoration");


  //// Generation of variables to put into guarded commands

  /** Pops temporaries and code, and makes these into a VARINCMD
      command that is returned.  If there are no temporaries, only
      the code is returned. */
  
  private GuardedCmd spill() {
    GuardedCmd body= GC.seq(GuardedCmdVec.popFromStackVector(code));
    GenericVarDeclVec temps= GenericVarDeclVec.popFromStackVector(temporaries);
    return GC.block(temps, body);
  }


    /**
     ** Make a fresh version of a special variable to save it in. <p>
     **
     ** Preconditon: v accesses a special variable.
     **
     ** (This partially handles ESCJ 23b, case 4.)
     **/
    private VariableAccess adorn(VariableAccess v) {
	Assert.precondition(v.decl.locId == UniqName.specialVariable);

	VariableAccess result= GC.makeVar(v.decl.id, v.decl.locId);
	result.loc= v.getStartLoc();

	temporaries.addElement(result.decl);
	return result;
    }


    /**
     ** Make a fresh "boolean" variable to hold the initialized status
     ** of a Java variable that is marked as "uninitialized". <p>
     **
     ** Preconditon: v accesses a normal Java variable.
     **
     ** (This partially handles ESCJ 23b, case 13.)
     **/
    private VariableAccess initadorn(LocalVarDecl d) {
	Identifier idn= Identifier.intern(d.id + "@init");

	VariableAccess result= GC.makeVar(idn, d.locId);
	result.loc= Location.NULL;

	return result;
    }


  //// Statement translation

  // Utility routines

  private boolean isRecursiveInvocation(/*@ non_null */ RoutineDecl r) {
    Translate t = this;
    while (t != null) {
      if (t.rdCurrent == r) {
	return true;  // the routine has been visited before
      }
      t = t.inlineParent;
    }
    return false;
  }

  /** Reduces number of stack marks by 1. */
  
  private GuardedCmd opBlockCmd(Expr label) {
    GuardedCmd g= GC.seq(GuardedCmdVec.popFromStackVector(code));
    GuardedCmd ifc= GC.ifcmd(GC.nary(TagConstants.ANYEQ, GC.ecvar, label),
			     GC.skip(), GC.raise());
    return GC.trycmd(g, ifc);
  }
  
  private Expr breakLabel(Stmt s) {return GC.symlit(s, "L_");}
  private Expr continueLabel(Stmt s) {return GC.symlit(s, "C_");}

  /** Emits the commands <code>EC= label; raise</code> to
      <code>code</code>. */
  
  private void raise(Expr label) {
    code.addElement(GC.gets(GC.ecvar, label));
    code.addElement(GC.raise());
  }

  /** Computes purity information for Java expression <code>e</code>,
      translates <code>e</code> (emitting any code needed to account for
      impurities or side effects in the expression), and emits code that
      performs a <code>RAISE label</code> command if the expression
      evaluates to <code>false</code>.  As usual, emitted code
      is appended to <code>code</code> and temporaries are appended to
      <code>temporaries</code>. */
  
  private void Guard(Expr e, Expr label) {
    Expr grd= ptrExpr(e);
    code.push();  // popped off by boxPopFromStackVector
    code.addElement(GC.assume(grd));
    code.addElement(GC.skip());
    code.push();  // popped off by boxPopFromStackVector
    code.addElement(GC.assumeNegation(grd));
    raise(label);
    GuardedCmd ifc= GC.boxPopFromStackVector(code);
    code.addElement(ifc);
  }
  
  /** Appends to <code>code</code> commands that make up a loop with
      nominal body <code>guard;body</code>, where <code>label</code> is raised
      within <code>body</code> to terminate the loop.  The vector
      <code>J</code> contains the user-declared loop invariant pragmas.
      The vector <code>decreases</code> contains the user-declared bound
      function pragmas.
      The scope of the variables in <code>tempVars</code> is the nominal
      body; this method will wrap an appropriate <code>var .. in .. end</code>
      command around these.
  */
  
  private void makeLoop(int sLoop, int eLoop, int locHotspot,
			GenericVarDeclVec tempVars,
			GuardedCmd guard, GuardedCmd body,
			ExprStmtPragmaVec J, ExprStmtPragmaVec decreases,
			LocalVarDeclVec skolemConsts, ExprStmtPragmaVec P,
			Expr label) {

    code.push();  // this mark popped by "opBlockCmd"

    // construct old variants of the variables that are targets of the loop.
    code.push();
    temporaries.push();
    GuardedCmd S = GC.seq(guard, body);
    Set normalTargets = Targets.normal(GC.block(tempVars, S));
    Hashtable h = GetSpec.makeSubst(normalTargets.elements(), "loopold");

    for (Enumeration keys = h.keys(); keys.hasMoreElements(); ) {
	GenericVarDecl vd = (GenericVarDecl) keys.nextElement();
	VariableAccess va = (VariableAccess) h.get(vd);
	temporaries.addElement(va.decl);
	code.addElement(GC.assume(GC.nary(TagConstants.ANYEQ, VariableAccess.make(vd.id, sLoop, vd), va)));
    }

    ConditionVec invs = ConditionVec.make();
    for (int i = 0; i < J.size(); i++) {
      ExprStmtPragma loopinv = J.elementAt(i);
      Expr pred = TrAnExpr.trSpecExpr(loopinv.expr, null, h);
      Condition cond = GC.condition(TagConstants.CHKLOOPINVARIANT,
				    pred,
				    loopinv.getStartLoc());
      invs.addElement(cond);      
    }

    DecreasesInfoVec decs = DecreasesInfoVec.make();
    for (int i = 0; i < decreases.size(); i++) {
      ExprStmtPragma d = decreases.elementAt(i);
      Expr de = TrAnExpr.trSpecExpr(d.expr);
      int loc = d.getStartLoc();
      VariableAccess fOld = temporary("decreases", loc, loc);
      DecreasesInfo di = new DecreasesInfo(loc, de, fOld);
      decs.addElement(di);
    }

    ExprVec preds = ExprVec.make();
    for (int i = 0; i < P.size(); i++) {
	ExprStmtPragma looppred = P.elementAt(i);
	preds.addElement(TrAnExpr.trSpecExpr(looppred.expr, null, h));
    }

    // If we ever implement the "safe" (as opposed to "fast") version of
    // loops, then "invs" should be extended with loop object invariant
    // conditions, and "guard" should be prefixed by a sequence of
    // TargetTypeCorrect assume commands, per ESCJ 16.

    LoopCmd loop = GC.loop(sLoop, eLoop, locHotspot, h,
			   invs, decs, skolemConsts, preds,
			   tempVars, guard, body);
    switch (Main.loopTranslation) {
      case Main.LOOP_FAST:
      case Main.LOOP_FALL_THRU:
	desugarLoopFast(loop);
	break;

      case Main.LOOP_SAFE:
	desugarLoopSafe(loop);
	break;

      default:
	Assert.fail("unexpected loop translation scheme: " + Main.loopTranslation);
    }

    code.addElement(loop);

    code.addElement(spill());

    code.addElement(opBlockCmd(label));
  }

  /** Desugars <code>loop</code> according to the fast option.
      In particular, sets <code>loop.desugared</code> to the desugaring. */
  
  private void desugarLoopFast(LoopCmd loop) {
    // A fast-desugared loop has the shape:
    //   var V in  J;B;S;J;B;S;J;..;fail  end
    // where "V" is the list of temporary variables used within the
    // loop, "J" is the command that checks loop invariants, "B" is
    // the guard command, and "S" is the rest of the body of the
    // loop.
    //
    // The number of repetitions of "J;B;S" is determined by
    // "Main.loopUnrollCount".  After these repetitions is another
    // "J", and if "Main.loopUnrollHalf" is "true", then there is
    // then one more "B".  As shown above, the sequence ends with
    // a "fail" command.
    //
    // If "Main.traceInfo" is positive, then each "J" is immediately
    // preceded by a command of the form:
    //   assume (lblpos trace.LoopIter^L#i true)
    // where "L" is the source location of the loop and "i" is the
    // iteration count.

    // Build a command that checks loop invariants
    code.push();  // this mark popped below
    checkLoopInvariants(loop);
    GuardedCmd J = GC.seq(GuardedCmdVec.popFromStackVector(code));

    code.push(); // this mark popped below after for loop

    String locString = UniqName.locToSuffix(loop.getStartLoc()) + "#";

    int numOfComponents = 3 * Main.loopUnrollCount +
                          (Main.loopUnrollHalf ? 2 : 1);
    int iComp = 0;
    int i = 0;
    for ( ; true; i++) {
      code.push();  // this mark popped below

      // -- J --
      Assert.notFalse(iComp != numOfComponents);
      if (Main.traceInfo > 0) {
	String label = locString + i;
	code.addElement(traceInfoLabelCmd(loop.getStartLoc(),
					  loop.getEndLoc(),
					  "LoopIter", label));
      }
      code.addElement(J);
      iComp++;
      if (iComp == numOfComponents) {
	break;
      }
      // -- B --
      addLoopDecreases(loop, 0);  // fOld = F;
      GuardedCmd B = loop.guard;
      if (Main.traceInfo > 0 && 0 < i) {
	B = cloneGuardedCommand(B);
      }
      code.addElement(B);
      iComp++;
      if (iComp == numOfComponents) {
	break;
      }
      // -- S --
      GuardedCmd S = loop.body;
      if (Main.traceInfo > 0 && 0 < i) {
	S = cloneGuardedCommand(S);
      }
      code.addElement(S);
      addLoopDecreases(loop, 1);  // check 0 <= fOld;
      addLoopDecreases(loop, 2);  // check F < fOld;
      iComp++;

      GuardedCmd iter = wrapUpUnrolledIteration(locString, i, loop.tempVars);
      code.addElement(iter);

      Assert.notFalse(iComp != numOfComponents);
    }

    // pop once more to get J or J;B as the case might be
    GuardedCmd iter = wrapUpUnrolledIteration(locString, i, loop.tempVars);
    code.addElement(iter);

    // Stop unrolling here
    if (Main.loopTranslation != Main.LOOP_FALL_THRU) {
      code.addElement(GC.fail());
    }

    loop.desugared = GC.seq(GuardedCmdVec.popFromStackVector(code));
  }

  //@ requires 0 <= iteration;
  private GuardedCmd wrapUpUnrolledIteration(/*@ non_null */ String locString,
					     int iteration,
					     /*@ non_null */ GenericVarDeclVec tempVars) {
    GuardedCmd body = GC.seq(GuardedCmdVec.popFromStackVector(code));
    body = GC.block(tempVars, body);
    GuardedCmd iter = DynInstCmd.make(locString + iteration, body);
    return iter;
  }

  /** Adds to <code>code</code> the various pieces of the translation of
    * the decreases pragma.  The pieces are:
    * <ul>
    * <li> 0. fOld = F;
    * <li> 1. check 0 <= fOld;
    * <li> 2. check F < fOld;
    * </ul>
    **/

  //@ requires 0 <= piece && piece < 3;
  //@ modifies code.elementCount;
  private void addLoopDecreases(/*@ non_null */ LoopCmd loop,
				int piece) {
    for (int i = 0; i < loop.decreases.size(); i++) {
      DecreasesInfo di = loop.decreases.elementAt(i);
      switch (piece) {
	case 0: // fOld = F;
	  code.addElement(GC.gets(di.fOld, di.f));
	  break;
	case 1: // check 0 <= fOld;
	  addCheck(loop.locHotspot, TagConstants.CHKDECREASES_BOUND,
		   GC.nary(TagConstants.INTEGRALLE, GC.zerolit, di.fOld),
		   di.loc);
	  break;
	case 2: // check F < fOld;
	  addCheck(loop.locHotspot, TagConstants.CHKDECREASES_DECR,
		   GC.nary(TagConstants.INTEGRALLT, di.f, di.fOld),
		   di.loc);
	  break;
	default:
	  //@ unreachable
	  Assert.fail("addLoopDecreases: unexpected piece number");
	  break;
      }
    }
  }


    /** targets is set of GenericVarDecls. aTargets is set of ATargets.
     */


    public GuardedCmd modifyATargets(Set aTargets, int loc) {
	code.push();
	for (Enumeration e = aTargets.elements(); e.hasMoreElements();) {
	    ATarget at = (ATarget)e.nextElement();
	    VariableAccess va = VariableAccess.make(at.x.id, loc, at.x);

	    if (at.indices.length == 0 || 
		(at.indices[0] == null && 
		 (at.indices.length == 1 || at.indices[1] == null))) {
		// x, x[*], x[*][*]
		//System.out.println("x = " + at.x.id.toString() + ", at.indices.length = " + at.indices.length);
		code.addElement(modify(va, loc));
	    } else if (at.indices.length == 1) {
		// x[e]
		VariableAccess newVal = temporary(va.id.toString(), loc, loc);
		code.addElement(GC.subgets(va, at.indices[0], newVal));
	    } else if (at.indices[0] == null) {
		// x[*][e]
		VariableAccess newVal = temporary(va.id.toString(), loc, loc);
		VariableAccess var1 = GC.makeVar("i", loc);
		VariableAccess var2 = GC.makeVar("j", loc);

		Expr a = GC.implies(GC.nary(TagConstants.ANYNE, var2, at.indices[1]),
				    GC.nary(TagConstants.ANYEQ, 
					    GC.select(GC.select(va, var1), var2), 
					    GC.select(GC.select(newVal, var1), var2)));
		code.addElement(GC.assume(GC.forall(var1.decl,GC.forall(var2.decl, a ))));
		code.addElement(GC.gets(va, newVal));			  
	    } else if (at.indices[1] == null) {
		// x[e][*]
		VariableAccess newVal = temporary(va.id.toString(), loc, loc);
		VariableAccess var1 = GC.makeVar("i", loc);
		VariableAccess var2 = GC.makeVar("j", loc);

		Expr a = GC.implies(GC.and( GC.nary(TagConstants.ANYNE, var1, at.indices[0]),
					    GC.nary(TagConstants.IS, var2, TrAnExpr.trType(Types.intType))),
				    GC.nary(TagConstants.ANYEQ, 
					    GC.select(GC.select(va, var1), var2), 
					    GC.select(GC.select(newVal, var1), var2)));
 		code.addElement(GC.assume(GC.forall(var1.decl, GC.forall(var2.decl, a))));
		code.addElement(GC.gets(va, newVal));			  
	    } else {
		// x[e][e]
		VariableAccess newVal = temporary(va.id.toString(), loc, loc);
		code.addElement(GC.subsubgets(va, at.indices[0], at.indices[1], newVal));
	    }
	}
	
	return GC.seq(GuardedCmdVec.popFromStackVector(code));
    }

    public GuardedCmd modify(Set simpleTargets, int loc) {
	code.push();
	for (Enumeration e = simpleTargets.elements(); e.hasMoreElements();) {
	    GenericVarDecl at = (GenericVarDecl)e.nextElement();
	    VariableAccess va = VariableAccess.make(at.id, loc, at);	    
	    code.addElement(modify(va, loc));
	}
	
	return GC.seq(GuardedCmdVec.popFromStackVector(code));
    }

  /** Desugars <code>loop</code> according to the safe option.
    * In particular, sets <code>loop.desugared</code> to the desugaring.
    **/
  
  public void desugarLoopSafe(LoopCmd loop) {
    // Build a command that checks loop invariants safely

    code.push();  // this mark popped below
    checkLoopInvariants(loop);
    code.addElement(GC.fail());
    GuardedCmd checkInvariantsInitially = GC.seq(GuardedCmdVec.popFromStackVector(code));

    code.push();  // this mark popped shortly
    addLoopDecreases(loop, 0);  // fOld = F;
    code.addElement(loop.guard);
    code.addElement(loop.body);
    addLoopDecreases(loop, 1);  // check 0 <= fOld;
    addLoopDecreases(loop, 2);  // check F < fOld;
    GuardedCmd S = GC.seq(GuardedCmdVec.popFromStackVector(code));

    Set vds = Targets.normal(GC.block(loop.tempVars, S));
    GuardedCmd modifyGc = modify(vds, loop.locStart);

    if( Main.preciseTargets ) {
	Set aTargets = ATarget.compute( GC.block(loop.tempVars, loop ));
	modifyGc = modifyATargets( aTargets, S.getStartLoc());
    }

    code.push();  // this mark popped below
    code.addElement(modifyGc);

    for (Enumeration e = vds.elements(); e.hasMoreElements();) {
	GenericVarDecl vd = (GenericVarDecl)e.nextElement();
	
	if (!vd.id.toString().endsWith("@init")) {
	    code.addElement(GC.assume(TrAnExpr.targetTypeCorrect(vd, loop.oldmap)));
	}
    }
    for (int i = 0; i < loop.invariants.size(); i++) {
      Condition cond = loop.invariants.elementAt(i);
      code.addElement(GC.assume(cond.pred));
    }

    if (Main.traceInfo > 0) {
	String label = UniqName.locToSuffix(loop.getStartLoc());
	code.addElement(traceInfoLabelCmd(loop, "LoopIter"));
    }

    code.addElement(DynInstCmd.make(UniqName.locToSuffix(loop.getStartLoc()), S));

    checkLoopInvariants(loop);
    code.addElement(GC.fail());
    GuardedCmd checkInvariantsAfterIteration = GC.seq(GuardedCmdVec.popFromStackVector(code));

    loop.desugared = GC.choosecmd(checkInvariantsInitially,
				  checkInvariantsAfterIteration);
  }

  /** Add to "code" checks for all loop invariants of "loop".
    **/

  private void checkLoopInvariants(/*@ non_null */ LoopCmd loop) {
    for (int i = 0; i < loop.invariants.size(); i++) {
      Condition cond = loop.invariants.elementAt(i);
      addCheck(loop.locHotspot, cond);
    }
  }

  //@ requires Main.traceInfo > 0;
  private GuardedCmd traceInfoLabelCmd(/*@ non_null */ ASTNode ast,
				       /*@ non_null */ String traceInfoTag) {
    int sloc = ast.getStartLoc();
    int eloc = ast.getEndLoc();
    return traceInfoLabelCmd(sloc, eloc, traceInfoTag, sloc);
  }

  //@ requires Main.traceInfo > 0;
  //@ requires loc != Location.NULL;
  private GuardedCmd traceInfoLabelCmd(/*@ non_null */ ASTNode ast,
				       /*@ non_null */ String traceInfoTag,
				       int loc) {
    return traceInfoLabelCmd(ast.getStartLoc(), ast.getEndLoc(),
			     traceInfoTag, loc);
  }

  //@ requires Main.traceInfo > 0;
  //@ requires loc != Location.NULL;
  private GuardedCmd traceInfoLabelCmd(int sloc, int eloc,
				       /*@ non_null */ String traceInfoTag,
				       int loc) {
    return traceInfoLabelCmd(sloc, eloc, traceInfoTag,
			     UniqName.locToSuffix(loc));
  }

    
  private GuardedCmd traceInfoLabelCmd(int sloc, int eloc,
				       /*@ non_null */ String traceInfoTag,
				       String sortSeq) {
    Assert.notFalse(Main.traceInfo > 0);

    String str = "trace." + traceInfoTag + "^" + sortSeq;
    Identifier id = Identifier.intern(str);
    Expr l = LabelExpr.make(sloc, eloc, true, id, GC.truelit);
    return GC.assume(l);
  }

  /** This method returns a guarded command <code>G</code> that is
    * like <code>gc</code> except that where <code>gc</code> contains
    * a mutable command, <code>G</code> contains a fresh copy of that
    * command.  Thus, the resulting command is as good as a fresh copy
    * of <code>gc</code>, since all other guarded commands are to be
    * read-only after construction.<p>
    *
    * There is only one mutable command, namely an "assume" command
    * of the form:
    * <pre>
    *     assume (lblpos id true)
    * </pre>
    * where "id" is a trace label.  A fresh copy of this command consists
    * of a fresh assume command with a fresh labeled expression.  However,
    * the "id" reference may be shared in the fresh command.
    **/

  private GuardedCmd cloneGuardedCommand(/*@ non_null */ GuardedCmd gc) {
    switch (gc.getTag()) {
    case TagConstants.SKIPCMD:
    case TagConstants.RAISECMD:
    case TagConstants.ASSERTCMD:
    case TagConstants.GETSCMD:
    case TagConstants.SUBGETSCMD:
    case TagConstants.SUBSUBGETSCMD:
    case TagConstants.RESTOREFROMCMD:
      return gc;

    case TagConstants.ASSUMECMD: {
      ExprCmd ec = (ExprCmd)gc;
      if (ec.pred.getTag() == TagConstants.LABELEXPR) {
	LabelExpr le = (LabelExpr)ec.pred;
	if (le.positive && le.expr == GC.truelit) {
	  String str = le.label.toString();
	  if (ErrorMsg.isTraceLabel(str)) {
	    return GC.assume(LabelExpr.make(le.sloc, le.eloc, true,
					    le.label, GC.truelit));
	  }
	}
      }
      return gc;
    }

    case TagConstants.VARINCMD: {
      VarInCmd vc = (VarInCmd)gc;
      GuardedCmd body = cloneGuardedCommand(vc.g);
      if (body != vc.g) {
	return GC.block(vc.v, body);
      }
      return gc;
    }

    case TagConstants.DYNINSTCMD: {
      DynInstCmd dc = (DynInstCmd)gc;
      GuardedCmd body = cloneGuardedCommand(dc.g);
      if (body != dc.g) {
	return DynInstCmd.make(dc.s, body);
      }
      return gc;
    }

    case TagConstants.SEQCMD: {
      SeqCmd sc = (SeqCmd)gc;
      int len = sc.cmds.size();
      GuardedCmd[] cmds = null;  // allocate this array lazily
      for (int i = 0; i < len; i++) {
	GuardedCmd c = sc.cmds.elementAt(i);
	GuardedCmd g = cloneGuardedCommand(c);
	if (g != c) {
	  if (cmds == null) {
	    cmds = new GuardedCmd[len];
	    // all elements up until now have been the same
	    for (int j = 0; j < i; j++) {
	      cmds[j] = sc.cmds.elementAt(j);
	    }
	  }
	  cmds[i] = g;
	} else if (cmds != null) {
	  cmds[i] = g;
	}
      }
      if (cmds != null) {
	return GC.seq(GuardedCmdVec.make(cmds));
      }
      return gc;
    }

    case TagConstants.CALL: {
      Call call = (Call)gc;
      GuardedCmd desugared = cloneGuardedCommand(call.desugared);
      if (desugared != call.desugared) {
	Call newCall = Call.make(call.args, call.scall, call.ecall,
				 call.inlined);
	newCall.rd = call.rd;
	newCall.spec = call.spec;
	newCall.desugared = desugared;
	return newCall;
      }
      return gc;
    }

    case TagConstants.TRYCMD: {
      CmdCmdCmd tc = (CmdCmdCmd)gc;
      GuardedCmd g1 = cloneGuardedCommand(tc.g1);
      GuardedCmd g2 = cloneGuardedCommand(tc.g2);
      if (g1 != tc.g1 || g2 != tc.g2) {
	return GC.trycmd(g1, g2);
      }
      return gc;
    }

    case TagConstants.LOOPCMD: {
      LoopCmd lp = (LoopCmd)gc;
      GuardedCmd guard = cloneGuardedCommand(lp.guard);
      GuardedCmd body = cloneGuardedCommand(lp.body);
      LoopCmd newLoop = GC.loop(lp.locStart, lp.locEnd, lp.locHotspot, lp.oldmap,
				lp.invariants, lp.decreases,
				lp.skolemConstants, lp.predicates, lp.tempVars,
				guard, body);
      newLoop.desugared = cloneGuardedCommand(lp.desugared);
      // A desugared loop contains trace info labels, and thus:
      Assert.notFalse(newLoop.desugared != lp.desugared);
      return newLoop;
    }

    case TagConstants.CHOOSECMD: {
      CmdCmdCmd cc = (CmdCmdCmd)gc;
      GuardedCmd g1 = cloneGuardedCommand(cc.g1);
      GuardedCmd g2 = cloneGuardedCommand(cc.g2);
      if (g1 != cc.g1 || g2 != cc.g2) {
	return GC.choosecmd(g1, g2);
      }
      return gc;
    }

    default:
      //@ unreachable
      Assert.notFalse(false);
      return null;
    }
  }

  /** Pops <code>declaredLocals</code> and <code>code</code> and then
      appends <code>code</code> with a VARINCMD (if there were any new
      declared locals) or a sequence of commands (otherwise).  The
      new code becomes the body of the VARINCMD or the sequence of
      commands. */

  private void wrapUpDeclBlock() {
    if (code.size() == 0) {
      declaredLocals.pop();
      code.pop();
    } else {
      if (declaredLocals.size() == 0) {
	declaredLocals.pop();
	code.merge();
      } else {
	code.addElement(popDeclBlock());
      }
    }
  }

  /** Pops the code and declared local variables, makes these into a
    * command (usually a VAR .. IN .. END command, but possibly a
    * sequence command if there are no declared local variables).
    * The command is then returned.
    **/
  
  private GuardedCmd popDeclBlock() {
    GuardedCmd body= GC.seq(GuardedCmdVec.popFromStackVector(code));
    // The following "if" statement seems to be a bug, because it
    // fails to pop "declaredLocals".  Better would be not to even
    // check the "if", but to always pop from the stack vector, and
    // then let "GC.block" do the optimization.  --KRML, 29 Sep 1999
    if (declaredLocals.size() == 0)
      return body;
    GenericVarDeclVec locals
      = GenericVarDeclVec.popFromStackVector(declaredLocals);
    return GC.block(locals, body);
  }
  
  /** Translate <code>stmt</code> into a sequence of guarded commands
      that are appended to <code>code</code>.  Temporaries generated for
      expressions in <code>stmt</code> are added to
      <code>temporaries</code>.  Loop invariant pragmas are added to
      <code>loopinvariants</code>.  Decreases pragmas are added to
      <code>loopDecreases</code>. */

  private void trStmt(/*@ non_null */ Stmt stmt) {
    int tag = stmt.getTag();
    switch (tag) {
      
      case TagConstants.RETURNSTMT: 
	{
	  ReturnStmt r = (ReturnStmt)stmt;
	  if (r.expr != null)
	      code.addElement(GC.gets(GC.resultvar, ptrExpr(r.expr)));
	  if (Main.traceInfo > 0) {
	      // add a label to track the return
	      GuardedCmd g = traceInfoLabelCmd(r, "Return", r.loc);	
	      code.addElement(g);
	  }
	  raise(GC.ec_return);
	  return;
	}
      
      case TagConstants.THROWSTMT: 
	{
	  ThrowStmt t = (ThrowStmt)stmt;
	  code.addElement(GC.gets(GC.xresultvar, ptrExpr(t.expr)));
	  nullCheck(t.expr, GC.xresultvar, t.getStartLoc());
	  if (Main.traceInfo > 0) {
	      // add a label to track the throw
	      GuardedCmd g = traceInfoLabelCmd(t, "Throw", t.loc);
	      code.addElement(g);
	  }
	  raise(GC.ec_throw);
	  return;
	}
      
      case TagConstants.SWITCHSTMT: 
	{
	  SwitchStmt c = (SwitchStmt)stmt;
	  VariableAccess e = fresh(c.expr, c.expr.getStartLoc(), "switch");
	  code.addElement( GC.gets( e, ptrExpr( c.expr )));

	  // we walk thru the switch body, building up the GC
	  // at each case label, we wrap up the GC generated so far
	  // on the lhs of a box, and put the new assume on the rhs.
	
	  code.push();
	  code.addElement(GC.assume(GC.falselit));
	
	  for(int i=0; i<c.stmts.size(); i++) {
	    Stmt s = c.stmts.elementAt(i);

	    if( s.getTag() != TagConstants.SWITCHLABEL ) {
	      // just a regular statement
	      trStmt( s );
	    } else {
	    
	      SwitchLabel sl = (SwitchLabel)s;
	    
	      GuardedCmdVec vec = GuardedCmdVec.popFromStackVector(code);
	      GuardedCmd boxLhs = GC.block(GenericVarDeclVec.make(),
					   GC.seq(vec));

	      Expr C;
	      if( sl.expr != null ) {

		C = GC.nary(s.getStartLoc(),s.getEndLoc(),
			    TagConstants.INTEGRALEQ,
			    e, TrAnExpr.trSpecExpr(sl.expr));
	      } else {

		C = GC.truelit;
		for(int j=0; j<c.stmts.size(); j++) {

		  Stmt s2 = c.stmts.elementAt(j);
		  if( s2.getTag() == TagConstants.SWITCHLABEL ) {

		    SwitchLabel sl2 = (SwitchLabel)s2;

		    if( sl2.expr != null )
		      C = GC.and(s.getStartLoc(),s.getEndLoc(),
				  C,
				  GC.nary(s.getStartLoc(),s.getEndLoc(),
					  TagConstants.INTEGRALNE,
					  e, TrAnExpr.trSpecExpr(sl2.expr)));
		  }
		}
	      }
	    
	      GuardedCmd boxRhs = GC.assume(C);
	      if (Main.traceInfo > 0) {
		  // add a label to track the switch branch taken
		  GuardedCmd g = traceInfoLabelCmd(s, "Switch");
		  boxRhs = GC.seq(g, boxRhs);
	      }  
	      GuardedCmd box = GC.choosecmd(boxLhs, boxRhs);
	    
	      code.push();
	      code.addElement(box);
	    
	    }
	  }
	
	  GuardedCmd body = GC.seq(GuardedCmdVec.popFromStackVector(code));
	  GuardedCmd block = GC.blockL(c, body);
	  code.addElement(block);

	  return;
	}
      
      case TagConstants.BLOCKSTMT: 
	{
	  GenericBlockStmt b = (GenericBlockStmt)stmt;
	  declaredLocals.push();  // this mark popped by "wrapUpDeclBlock"
	  code.push();            // this mark popped by "wrapUpDeclBlock"

	  for (int i= 0; i < b.stmts.size(); i++)
	    trStmt(b.stmts.elementAt(i));

	  wrapUpDeclBlock();
	  return;
	}

      case TagConstants.WHILESTMT: 
	{
	  WhileStmt w = (WhileStmt)stmt;
	  Expr bLabel = breakLabel(w);

	  temporaries.push();  // this mark popped below

	  code.push();  // this mark popped below
	  Guard(w.expr, bLabel);
	  GuardedCmd guardCmd =
	    GC.seq(GuardedCmdVec.popFromStackVector(code));

	  ExprStmtPragmaVec invariants = loopinvariants;
          loopinvariants = ExprStmtPragmaVec.make();
	  ExprStmtPragmaVec decreases = loopDecreases;
          loopDecreases = ExprStmtPragmaVec.make();
	  ExprStmtPragmaVec predicates = loopPredicates;
	  loopPredicates = ExprStmtPragmaVec.make();
	  LocalVarDeclVec skolemConsts = skolemConstants;
	  skolemConstants = LocalVarDeclVec.make();

          code.push();  // this mark popped by "opBlockCmd"
          trStmt(w.stmt);
	  GuardedCmd bodyCmd = opBlockCmd(continueLabel(w));

	  makeLoop(w.getStartLoc(), w.getEndLoc(), w.locGuardOpenParen,
		   GenericVarDeclVec.popFromStackVector(temporaries),
		   guardCmd, bodyCmd, invariants, decreases,
		   skolemConsts, predicates, bLabel);
	  
	  return;
	}
      
      case TagConstants.DOSTMT: 
	{
	  DoStmt d = (DoStmt)stmt;
	  Expr bLabel = breakLabel(d);

	  // the following 3 marks are popped below
	  temporaries.push();
	  code.push();

	  ExprStmtPragmaVec invariants = loopinvariants;
	  loopinvariants = ExprStmtPragmaVec.make();
	  ExprStmtPragmaVec decreases = loopDecreases;
          loopDecreases = ExprStmtPragmaVec.make();
	  ExprStmtPragmaVec predicates = loopPredicates;
	  loopPredicates = ExprStmtPragmaVec.make();
	  LocalVarDeclVec skolemConsts = skolemConstants;
	  skolemConstants = LocalVarDeclVec.make();

          code.push(); // this mark popped by "opBlockCmd"
          trStmt(d.stmt);
	  code.addElement(opBlockCmd(continueLabel(d)));

	  Guard(d.expr, bLabel);

	  GuardedCmd body = GC.seq(GuardedCmdVec.popFromStackVector(code));

	  makeLoop(d.getStartLoc(), d.getEndLoc(), d.loc,
		   GenericVarDeclVec.popFromStackVector(temporaries),
		   GC.skip(), body, invariants, decreases,
		   skolemConsts, predicates, bLabel);
	  return;
	}
      
      case TagConstants.FORSTMT: 
	{
	  ForStmt x = (ForStmt)stmt;

	  declaredLocals.push();  // this mark popped by "wrapUpDeclBlock"
	  code.push();            // this mark popped by "wrapUpDeclBlock"
	  
	  // initializers
	  for (int i= 0; i < x.forInit.size(); i++)
	    trStmt(x.forInit.elementAt(i));

	  Expr bLabel = breakLabel(x);

	  temporaries.push();  // this mark popped below

	  ExprStmtPragmaVec invariants = loopinvariants;
	  loopinvariants = ExprStmtPragmaVec.make();
	  ExprStmtPragmaVec decreases = loopDecreases;
          loopDecreases = ExprStmtPragmaVec.make();
	  ExprStmtPragmaVec predicates = loopPredicates;
	  loopPredicates = ExprStmtPragmaVec.make();
	  LocalVarDeclVec skolemConsts = skolemConstants;
	  skolemConstants = LocalVarDeclVec.make();

	  code.push();  // this mark popped below
	  Guard(x.test, bLabel);
	  GuardedCmd guardCmd =
	    GC.seq(GuardedCmdVec.popFromStackVector(code));

          code.push(); // this mark popped below

          code.push(); // this mark popped by "opBlockCmd"
          trStmt(x.body);
	  code.addElement(opBlockCmd(continueLabel(x)));

	  for(int i=0; i < x.forUpdate.size(); i++)
	    ptrExpr(x.forUpdate.elementAt(i));

	  GuardedCmd bodyCmd = GC.seq(GuardedCmdVec.popFromStackVector(code));

	  makeLoop(x.getStartLoc(), x.getEndLoc(), x.locFirstSemi,
		   GenericVarDeclVec.popFromStackVector(temporaries),
		   guardCmd, bodyCmd, invariants, decreases,
		   skolemConsts, predicates, bLabel);

	  wrapUpDeclBlock();
	  return;
	}

      case TagConstants.IFSTMT: 
	{
	  IfStmt i = (IfStmt)stmt;
	  Expr test = ptrExpr(i.expr);

	  code.push();
	  if (Main.traceInfo > 0) {
	      // add a label to track the if branch taken
	      GuardedCmd g = traceInfoLabelCmd(i.thn, "Then");
	      code.addElement(g);
	  }  
	  trStmt(i.thn);
	  GuardedCmd thn = GC.seq(GuardedCmdVec.popFromStackVector(code));

	  code.push();          
	  if (Main.traceInfo > 0) {
	      // add a label to track the if branch taken
	      GuardedCmd g = traceInfoLabelCmd(i.els, "Else");
	      code.addElement(g);
	  }  
	  trStmt(i.els);
	  GuardedCmd els = GC.seq(GuardedCmdVec.popFromStackVector(code));

	  code.addElement(GC.ifcmd(test, thn, els));
	  return;
	}
      
      case TagConstants.BREAKSTMT: 
	{
	  BreakStmt b = (BreakStmt)stmt;
	  Stmt label = TypeCheck.inst.getBranchLabel(b);
	  if (Main.traceInfo > 0) {
	      // add a label to track the break
	      GuardedCmd g = traceInfoLabelCmd(b, "Break", b.loc);
	      code.addElement(g);
	  }  
	  raise(breakLabel(label));
	  return;
	}
      
      case TagConstants.CONTINUESTMT: 
	{ 
	  ContinueStmt c = (ContinueStmt)stmt;
	  Stmt label = TypeCheck.inst.getBranchLabel(c);
	  if (Main.traceInfo > 0) {
	      // add a label to track the continue
	      GuardedCmd g = traceInfoLabelCmd(c, "Continue", c.loc);
	      code.addElement(g);
	  }  
	  raise(continueLabel(label));
	  return;
	}
      
      case TagConstants.SYNCHRONIZESTMT: 
	{
	  SynchronizeStmt x = (SynchronizeStmt)stmt;
	  int xStart = x.getStartLoc();
	  int xEnd = x.getEndLoc();
	  VariableAccess mu = fresh(x.expr, x.expr.getStartLoc(),
				    "synchronized");
	  Expr e = ptrExpr(x.expr);
	  code.addElement(GC.gets(mu,e));

	  nullCheck(x.expr, mu, x.locOpenParen);

	  trSynchronizedBody(mu, x.stmt, x.locOpenParen);
	  return;
	}

      
      case TagConstants.EVALSTMT: 
	{
	  EvalStmt x = (EvalStmt)stmt;
	  ptrExpr(x.expr);
	  return;
	}
      
      case TagConstants.LABELSTMT: 
	{
	  LabelStmt x = (LabelStmt)stmt;
          code.push(); // this mark popped by "opBlockCmd"
 	  trStmt(x.stmt);
	  code.addElement(opBlockCmd(breakLabel(x.stmt)));
          return;
	}
      
      case TagConstants.SKIPSTMT: 
	return;

      case TagConstants.TRYFINALLYSTMT: 
	{
	  TryFinallyStmt x = (TryFinallyStmt)stmt;
	  int xStart = x.getStartLoc();
	  int xEnd = x.getEndLoc();
	  GuardedCmd temp;

	  code.push();
	  trStmt(x.tryClause);
	  GuardedCmd c0 = GC.seq(GuardedCmdVec.popFromStackVector(code));

	  code.push();
	  VariableAccess ecSave = adorn(GC.ecvar);
	  VariableAccess resultSave = adorn(GC.resultvar);
	  VariableAccess xresultSave = adorn(GC.xresultvar); 

	  if (Main.traceInfo > 0) {
	    // add a label to track that the finally clause is entered because
	    // an exception was raised
	    GuardedCmd g = traceInfoLabelCmd(x, "FinallyBegin", x.locFinally);
	    code.addElement(g);
	  }
	  code.addElement(GC.assume(GC.nary(xStart,xEnd,
					    TagConstants.ANYEQ,
					    GC.ecvar, ecSave)));
	  code.addElement(GC.assume(GC.nary(xStart,xEnd,
					    TagConstants.REFEQ,
					    GC.resultvar, resultSave)));
	  code.addElement(GC.assume(GC.nary(xStart,xEnd,
					    TagConstants.REFEQ,
					    GC.xresultvar, xresultSave)));

	  code.push();
	  trStmt(x.finallyClause);
	  temp = DynInstCmd.make(UniqName.locToSuffix(x.getStartLoc()) + "#n",
				 GC.seq(GuardedCmdVec.popFromStackVector(code)));
	  code.addElement(temp);

	  code.addElement(GC.gets(GC.ecvar, ecSave));
	  code.addElement(GC.gets(GC.resultvar, resultSave));
	  code.addElement(GC.gets(GC.xresultvar, xresultSave));
	  if (Main.traceInfo > 0) {
	    // add a label to track that the finally clause is exited when it
	    // was entered due to that an exception was raised
	    GuardedCmd g = traceInfoLabelCmd(x, "FinallyEnd", x.getEndLoc());
	    code.addElement(g);
	  }
	  code.addElement(GC.raise());

	  GuardedCmd c1 = GC.seq(GuardedCmdVec.popFromStackVector(code));

	  code.addElement(GC.trycmd(c0,c1));

	  code.push();
	  trStmt(x.finallyClause);
	  temp = DynInstCmd.make(UniqName.locToSuffix(x.getStartLoc()) + "#x",
				 GC.seq(GuardedCmdVec.popFromStackVector(code)));
	  code.addElement(temp);

	  return;
	}
      
      case TagConstants.TRYCATCHSTMT: 
	{
	  TryCatchStmt x = (TryCatchStmt)stmt;
	  int xStart = x.getStartLoc();
	  int xEnd = x.getEndLoc();

	  code.push();
	  trStmt(x.tryClause);
	  GuardedCmd tryGC = GC.seq(GuardedCmdVec.popFromStackVector(code));


	  GuardedCmd els = GC.raise();

	  for(int i=x.catchClauses.size()-1; i>=0; i--) {
	    CatchClause cc = (CatchClause)x.catchClauses.elementAt(i);
	    int ccStart=cc.getStartLoc();
	    int ccEnd=cc.getEndLoc();

	    // tst is typeof(XRES) <: Ti
	    Expr tst = GC.nary(ccStart,ccEnd,
			       TagConstants.TYPELE,
			       GC.nary(ccStart,ccEnd,
				       TagConstants.TYPEOF,
				       GC.xresultvar),
			       TypeExpr.make(ccStart,ccEnd,
					     cc.arg.type));

	    if( i==0 ) {
	      // extend tst with a disjunct XRES==null
	      tst = GC.or(ccStart,ccEnd,
			  tst,
			  GC.nary(ccStart,ccEnd,
				  TagConstants.REFEQ,
				  GC.xresultvar,
				  GC.nulllit));
	    }
				  

	    code.push();
	    declaredLocals.addElement(cc.arg);
	    VariableAccess arg = VariableAccess.make( cc.arg.id, ccStart,
						      cc.arg );
	  
	    code.addElement(GC.assume(GC.nary(ccStart,ccEnd,
					      TagConstants.ANYEQ,
					      arg,
					      GC.xresultvar)));
	    trStmt(cc.body);
	    GuardedCmd thn = GC.seq(GuardedCmdVec.popFromStackVector(code));

	    els = GC.ifcmd(tst, thn, els);
	  }

	
	  GuardedCmd handler =
	    GC.ifcmd( GC.nary(xStart,xEnd,
			      TagConstants.ANYNE,
			      GC.ecvar,
			      GC.ec_throw),
		      GC.raise(),
		      els );

	  code.addElement(GC.trycmd(tryGC,handler));

	  return;
	}
      
      case TagConstants.VARDECLSTMT: 
	{
	  LocalVarDecl vd = ((VarDeclStmt)stmt).decl;
	  declaredLocals.addElement(vd);
	  boolean isUninitialized = false;
	  if (vd.pmodifiers != null) {
	    for (int i= 0; i < vd.pmodifiers.size(); i++) {
	      ModifierPragma prag= vd.pmodifiers.elementAt(i);
	      if (prag.getTag() == TagConstants.UNINITIALIZED) {
		VariableAccess init = initadorn(vd);
		declaredLocals.addElement(init.decl);
		setInitVar(vd, init);
		isUninitialized = true;
		break;  // don't look for any further UNINITIALIZED modifiers
	      }
	    }
	  }

	  if (null != vd.init) {
	    Assert.notFalse(vd.locAssignOp != Location.NULL);
	    VariableAccess lhs = TrAnExpr.makeVarAccess(vd, vd.getStartLoc());
	    Expr rval = ptrExpr(vd.init);
	    if (! isUninitialized) {
	      writeCheck(lhs, vd.init, rval, vd.locAssignOp, false);
	    }
	    code.addElement(GC.gets(lhs, rval));
	  }
	  return;
	}
      
      case TagConstants.CONSTRUCTORINVOCATION:
	//@ unreachable
	// If the following assert breaks, there's something wrong in
	// "trBody" where the constructor call is split up from the rest of
	// the constructor body.
	Assert.fail("constructor invocation handled in TrConstructorCallStmt");
	return;

      case TagConstants.UNREACHABLE:
	addCheck(stmt, TagConstants.CHKCODEREACHABILITY, GC.falselit);
	return;

      case TagConstants.SETSTMTPRAGMA: {
	SetStmtPragma s = (SetStmtPragma)stmt;

	Assert.notFalse(s.target instanceof FieldAccess);
	FieldAccess fa = (FieldAccess)s.target;

	Expr lhs= trFieldAccess(true, fa);
	writeCheck(lhs, s.value, TrAnExpr.trSpecExpr(s.value), s.locOp, false);

        VariableAccess field = VariableAccess.make(fa.id, fa.locId, fa.decl);
	if (Modifiers.isStatic(fa.decl.modifiers)) {
	    code.addElement(GC.gets( field,
				     TrAnExpr.trSpecExpr(s.value)));
	} else {
	    Expr obj = ((ExprObjectDesignator)fa.od).expr;
	    code.addElement(GC.subgets( field,
					TrAnExpr.trSpecExpr(obj),
					TrAnExpr.trSpecExpr(s.value) ));
	}

	break;
      }

      case TagConstants.ASSUME:
	{
	  ExprStmtPragma x = (ExprStmtPragma)stmt;
	  Expr p = TrAnExpr.trSpecExpr(x.expr);
	  code.addElement(GC.assume(p));
	  return;
	}

      case TagConstants.ASSERT:
	{
	  ExprStmtPragma x = (ExprStmtPragma)stmt;
	  Expr p = TrAnExpr.trSpecExpr(x.expr);
	  code.addElement(GC.check(x.getStartLoc(), TagConstants.CHKASSERT,
				   p, Location.NULL));
	  return;
	}

      case TagConstants.LOOP_INVARIANT:
	{
	  ExprStmtPragma x = (ExprStmtPragma)stmt;
	  loopinvariants.addElement(x);
	  return;
	}

      case TagConstants.DECREASES:
	{
	  ExprStmtPragma x = (ExprStmtPragma)stmt;
	  loopDecreases.addElement(x);
	  return;
	}

      case TagConstants.LOOP_PREDICATE:
	{
	  ExprStmtPragma x = (ExprStmtPragma)stmt;
	  loopPredicates.addElement(x);
	  return;
	}

      case TagConstants.SKOLEMCONSTANTPRAGMA:
	  {
	      SkolemConstantPragma p = (SkolemConstantPragma)stmt;
	      skolemConstants.append(p.decls);
	      break;
	  }

      case TagConstants.CLASSDECLSTMT: 
	  if (this.issueCautions) {
	      ErrorSet.caution(stmt.getStartLoc(),
			       "Not checking block-level types");
	  }	  
	  //Assert.notImplemented("block-level types");
	  return;

      default:
	//@ unreachable
	Assert.notFalse(false);
	return;
    }
  }


  //@ requires loc != Location.NULL;
  private void trSynchronizedBody(/*@ non_null */ Expr mu,
				  /*@ non_null */ Stmt stmt, int loc) {
    // check LockingOrderViolation: mutexLE(max(LS),mu) || LS[mu]
    addCheck(loc,
	     TagConstants.CHKLOCKINGORDER,
	     GC.or(GC.nary(TagConstants.LOCKLE,
			   GC.nary(TagConstants.MAX, GC.LSvar),
			   mu),
		   GC.nary(TagConstants.SELECT, GC.LSvar, mu)));

    VariableAccess newLS = adorn(GC.LSvar);

    // e1 is ( lockLE(max(LS),mu) && mu == max(newLS) )
    Expr e1 = GC.and(
		      // lockLE(max(LS),mu)
		      GC.nary(TagConstants.LOCKLE,
			      GC.nary(TagConstants.MAX, GC.LSvar),
			      mu),
		      // mu == max(newLS)
		      GC.nary(TagConstants.REFEQ,
			      mu,
			      GC.nary(TagConstants.MAX, newLS)));

    // e2 is ( LS[mu] && newLS == LS )
    Expr e2 = GC.and(
		      // LS[mu]
		      GC.select(GC.LSvar, mu ),
		      // newLS == LS
		      GC.nary(TagConstants.REFEQ, newLS, GC.LSvar));

    // assume (e1 || e2)
    code.addElement(GC.assume(GC.or(e1, e2)));

    // assume newLS == store(LS,mu,boolTrue)
    code.addElement(GC.assume(GC.nary(TagConstants.REFEQ, // or LOCKSETEQ?
				      newLS,
				      GC.nary(TagConstants.STORE,
					      GC.LSvar, mu, GC.truelit))));

    // assume newLS == asLockSet(newLS)
    code.addElement(GC.assume(GC.nary(TagConstants.REFEQ, // or LOCKSETEQ?
				      newLS,
				      GC.nary(TagConstants.ASLOCKSET,
					      newLS))));

    // Translate the body, using the new LS
    VariableAccess oldLS = GC.LSvar;
    GC.LSvar = newLS;
    trStmt(stmt);
    GC.LSvar = oldLS;
  }

  /** This method implements "TrConstructorCallStmt" as described in
    * section 6 of ESCJ 16.
    **/
  
  private void trConstructorCallStmt(ConstructorInvocation ci) {

      /*
       * Check if this is a constructor for an inner class; if so, we
       * need to pass an enclosing instance as the first argument.
       */
      TypeSig resultType = TypeSig.getSig(ci.decl.parent);
      boolean inner = !resultType.isTopLevelType();

      // translate arguments
      int argsSize = ci.args.size() + (inner ? 1 : 0);
      ExprVec rawArgs = ci.args.copy();
      ExprVec args = ExprVec.make(argsSize);

      if (inner) {
	  Expr rawExpr = ci.enclosingInstance;
	  Assert.notNull(rawExpr);
	  rawArgs.insertElementAt(rawExpr, 0);

	  Purity.decorate(rawExpr);
	  Expr arg = trExpr(true, rawExpr);
	  args.addElement(arg);

	  // do nullCheck here rather than non-null check in call(...):
	  nullCheck(rawExpr, arg, ci.locDot);
      }
      for (int i = 0; i < ci.args.size(); i++) {
	  Expr rawExpr = ci.args.elementAt(i);
	  Purity.decorate(rawExpr);
	  // protect all but the last argument
	  Expr arg = trExpr(i != ci.args.size()-1, rawExpr);
	  args.addElement(arg);
      }

      InlineSettings is = (InlineSettings)inlineDecoration.get(ci);
      code.addElement(call(ci.decl, rawArgs, args, scope,
			   ci.getStartLoc(), ci.getEndLoc(), 
			   ci.locOpenParen, true, is));
      // this = RES
      code.addElement(GC.gets(GC.thisvar, GC.resultvar));
  }

  //// Expression translation

  /** Extends the code array with a command that evaluates e and
      returns an expession which denotes this value in the poststate
      of that command. If <code>protect</code> is
      true, then the expression returned will depend only on values of
      temporary variables (that is, the expression returned will not be
      affected by changes to program variables).<p>
      If <code>protect</code> is <code>true</code>, then the value returned
      is certain to be of type <code>VariableAccess</code>. */
  //@ ensures protect ==> \result instanceof VariableAccess
  private Expr protect(boolean protect, Expr e, int loc) {
    if (protect) {
      VariableAccess result = fresh(e, loc);
      code.addElement(GC.gets(result, e));
      return result;
    } else return e;
  }

  //@ ensures protect ==> \result instanceof VariableAccess
  private Expr protect(boolean protect, Expr e, int loc, String suffix) {
    if (protect) {
      VariableAccess result = fresh(e, loc, suffix);
      code.addElement(GC.gets(result, e));
      return result;
    } else return e;
  }

  /** Purify and translate expr without protection */
  private Expr ptrExpr(VarInit expr) {
    Purity.decorate(expr);
    return trExpr(false, expr);
  }

  /** Translate <code>expr</code> into a sequence of guarded commands
      that are appended to <code>code</code> and return an expression
      denoting the value of the expression.    If <code>protect</code> is
      true, then the expression return will depend only on values of
      temporary variables (that is, the expression returned will not be
      affected by changes in program variables).  New temporaries may be
      generated; these are added to <code>temporaries</code>. */

  
  private Expr trExpr(boolean protect, VarInit expr) {
    int tag = expr.getTag();

    switch (tag) {
      case TagConstants.ARRAYINIT: 
	{
	  ArrayInit x = (ArrayInit)expr;
	  int xStart = x.getStartLoc();
	  int xEnd = x.getEndLoc();
	
	  int len = x.elems.size();
	  boolean isPure[] = new boolean[len];
	  Expr[] elems     = new Expr[len];

	  // set isPure[i] to true if {E_{i+1},...,E_n} are all pure
	  if( len != 0 ) isPure[len-1] = true;
	  for(int i=len-2; i>=0; i-- ) {
	    isPure[i] = isPure[i+1] && !Purity.impure(x.elems.elementAt(i+1));
	  }

	  for(int i=0; i<len; i++ )
	    elems[i] = trExpr(isPure[i], x.elems.elementAt(i));

	  // Construct variables
	  VariableAccess a = fresh(x, xStart, "arrayinit");
	  VariableAccess newallocvar = adorn(GC.allocvar);

	  // assume !isAllocated(a, alloc)
	  code.addElement(GC.assume(GC.not(xStart, xEnd,
					   GC.nary(xStart, xEnd,
						   TagConstants.ISALLOCATED,
						   a, GC.allocvar))));
	  // assume isAllocated(a, alloc')
	  code.addElement(GC.assume(GC.nary(xStart, xEnd,
					    TagConstants.ISALLOCATED,
					    a,
					    newallocvar )));
	  // assume a != null
	  code.addElement(GC.assume(GC.nary(xStart, xEnd,
					    TagConstants.REFNE,
					    a,
					    GC.nulllit )));
	  // assume typeof(a) == array(T)
	  Expr typeofa = GC.nary(xStart, xEnd,
				 TagConstants.TYPEOF, a);
	  Expr arrayT = TypeExpr.make(xStart, xEnd,
				      TypeCheck.inst.getType(x));

	  code.addElement(GC.assume(GC.nary(xStart, xEnd,
					    TagConstants.TYPEEQ,
					    typeofa, arrayT )));

	  // assume arrayLength(a) == n
	  code.addElement(GC.assume(GC.nary(xStart, xEnd,
					    TagConstants.INTEGRALEQ,
					    GC.nary(xStart, xEnd,
						    TagConstants.ARRAYLENGTH,
						    a),
					    LiteralExpr.make(TagConstants.INTLIT,
							     new Integer(len),
							     xStart))));	

	  // elems[a][i] = ei
	  Expr elemsAta = GC.nary(xStart, xEnd,
				  TagConstants.SELECT,
				  GC.elemsvar, a);
	  for(int i=0; i<len; i++ ) {
	    Expr iLit =
	      LiteralExpr.make(TagConstants.INTLIT, new Integer(i), xStart);
	    Expr elemsAtaAti = GC.nary(xStart, xEnd,
				       TagConstants.SELECT,
				       elemsAta, iLit);
	    code.addElement(GC.assume(GC.nary(xStart, xEnd,
					      TagConstants.REFEQ,
					      elemsAtaAti, elems[i] )));
	  }

	  // assume that everything allocated is an array
	  code.addElement(GC.assume(predEvathangsAnArray(GC.allocvar,
							 newallocvar)));
	  // alloc = alloc'				 
	  code.addElement(GC.gets(GC.allocvar, newallocvar));

	  return a;
	}
                    
      case TagConstants.THISEXPR: {
	  ThisExpr t = (ThisExpr)expr;
	  if (t.classPrefix!=null)
	      return trExpr(protect, Inner.unfoldThis(t));

	  // We ignore "protect" here, since "this" is (almost) never
	  // assigned to.  (In the one case where "this" is assigned--
	  // after the super-or-sibling constructor call in a
	  // constructor--"protect" is not needed.
	  return GC.thisvar;
      }

	// Literals
      case TagConstants.BOOLEANLIT: 
      case TagConstants.CHARLIT:
      case TagConstants.DOUBLELIT: 
      case TagConstants.FLOATLIT:
      case TagConstants.INTLIT:
      case TagConstants.LONGLIT:
      case TagConstants.NULLLIT:
      case TagConstants.STRINGLIT:
	return (Expr)expr;

      case TagConstants.ARRAYREFEXPR:
	{
	  ArrayRefExpr x= (ArrayRefExpr)expr;
	  Expr array= trExpr(Purity.impure(x.index), x.array);
	  Expr index= trExpr(false, x.index);

	  arrayAccessCheck(x.array, array, x.index, index, x.locOpenBracket);

	  Expr a= GC.select(GC.elemsvar, array);
	  return protect(protect, GC.select(a, index), x.locOpenBracket);
	}

      case TagConstants.NEWINSTANCEEXPR:
	{ 
	  NewInstanceExpr ni= (NewInstanceExpr)expr;

	  ExprVec rawArgs = ni.args.copy();
	  ExprVec args = ExprVec.make(ni.args.size());

	  if (ni.anonDecl!=null) {
              if (this.issueCautions) {
		  ErrorSet.caution(ni.anonDecl.loc,
				   "Not checking body of anonymous class" +
				   " (subclass of " +
				   ni.type.name.printName() + ")");
	      }
	  }

	  // translate enclosing instance argument if present:
	  if (ni.enclosingInstance!=null) {
	      rawArgs.insertElementAt(ni.enclosingInstance, 0);
	      Expr arg = trExpr(true, ni.enclosingInstance);
	      args.addElement(arg);

	      // do nullCheck here rather than non-null check in call(...):
	      nullCheck(ni.enclosingInstance, arg, ni.locDot);
	  }

	  // translate normal arguments
	  for (int i = 0; i < ni.args.size(); i++) {
	    // protect all but the last argument
	    Expr arg = trExpr(i != ni.args.size()-1, ni.args.elementAt(i));
	    args.addElement(arg);
	  }

	  InlineSettings is = (InlineSettings)inlineDecoration.get(ni);
	  code.addElement(call(ni.decl, rawArgs, args, scope,
			       ni.loc, ni.getEndLoc(), ni.locOpenParen,
			       false, is));
	  
	  {   Expr tType = TypeExpr.make(ni.loc, ni.getEndLoc(), ni.type);
	      Expr resType = GC.nary(TagConstants.TYPEOF, GC.resultvar);
	      if (ni.anonDecl!=null) {
		  //  assume typeof(RES) != T  (for anonymous new)
		  code.addElement(GC.assume(GC.nary(TagConstants.TYPENE,
						    resType,
						    tType)));
	      } else {
		  // assume typeof(RES) == T  (for ordinary new)
		  code.addElement(GC.assume(GC.nary(TagConstants.TYPEEQ,
						    resType,
						    tType)));
	      }
	  }

	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  PrettyPrint.write(baos, "new!");
	  PrettyPrint.inst.print(baos, ni.type);
	  return protect(protect, GC.resultvar, ni.locOpenParen, baos.toString());
	}

      case TagConstants.NEWARRAYEXPR:
	{
	  NewArrayExpr x= (NewArrayExpr)expr;
	  int nd= x.dims.size();

	  if (x.init!=null) {
	      return trExpr(true, x.init);
	  }

	  // Construct variables
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  PrettyPrint.write(baos, "new!");
	  PrettyPrint.inst.print(baos, x.type);
	  for (int i = 0; i < nd; i++) {
	    PrettyPrint.write(baos, "[]");
	  }
	  VariableAccess result= fresh(x, x.loc, baos.toString());
	  VariableAccess newallocvar= adorn(GC.allocvar);

	  // Evaluate length in each dimension
	  Expr[] dims= new Expr[nd];
	  for (int i= 0; i < nd; i++) {
	    boolean protectDim= false;
	    for (int j= i+1; j < nd && ! protectDim; j++)
	      protectDim= Purity.impure(x.dims.elementAt(j));
	    dims[i]= trExpr(protectDim, x.dims.elementAt(i));
	    // "nd" squared iterations, but nd should be small
	  }

	  // Check for negative array sizes
	  for (int i= 0; i < nd; i++) {
	    Expr nonneg= GC.nary(TagConstants.INTEGRALLE, GC.zerolit, dims[i]);
	    Condition cond= GC.condition(TagConstants.CHKNEGATIVEARRAYSIZE,
					 nonneg, Location.NULL);
	    Expr je= x.dims.elementAt(i);
	    code.addElement(GC.check(x.locOpenBrackets[i], cond,
				     trim(x.dims.elementAt(i))));
	  }
	  
	  // Construct call to arrayFresh
	  Expr shape= GC.nary(TagConstants.ARRAYSHAPEONE, dims[nd-1]);
	  Type type= ArrayType.make(x.type, Location.NULL);
	  for (int i= nd-2; 0 <= i; i--) {
	    shape= GC.nary(TagConstants.ARRAYSHAPEMORE, dims[i], shape);
	    type= ArrayType.make(type, Location.NULL);
	  }
	  Expr[] arrayFreshArgs= {
	    result, GC.allocvar, newallocvar, GC.elemsvar, shape,
	    GC.typeExpr(type), GC.zeroequiv(x.type)
	  };
	  Expr arrayFresh= GC.nary(x.getStartLoc(), x.getEndLoc(),
				   TagConstants.ARRAYFRESH,
				   ExprVec.make(arrayFreshArgs));

	  // Emit the Assume and a Gets commands
	  code.addElement(GC.assume(arrayFresh));
	  code.addElement(GC.assume(predEvathangsAnArray(GC.allocvar,
							 newallocvar)));
	  Expr ownerNull = predArrayOwnerNull(GC.allocvar, newallocvar, 
					      result);
	  if (ownerNull != null)
	      code.addElement(GC.assume(ownerNull));
	  code.addElement(GC.gets(GC.allocvar, newallocvar));

	  // Return the variable containing the newly-allocated array
	  return result;
	}

      case TagConstants.CONDEXPR:
	{
	  CondExpr x= (CondExpr)expr;
	  // ifCmd((tr(x.test), tr(x.thn), tr(x.els))
	  Expr test= trExpr(false, x.test);
	  VariableAccess result= fresh(x, x.locQuestion, "cond");
	  
	  code.push();
	  if (Main.traceInfo > 0) {
	      // add a label to track the if branch taken
	      GuardedCmd g = traceInfoLabelCmd(x.thn, "Then");
	      code.addElement(g);
	  }  
          Expr thnP= trExpr(false, x.thn);
	  code.addElement(GC.gets(result, thnP));
	  GuardedCmd thenbody= GC.seq(GuardedCmdVec.popFromStackVector(code));
	  
	  code.push();
	  if (Main.traceInfo > 0) {
	      // add a label to track the if branch taken
	      GuardedCmd g = traceInfoLabelCmd(x.els, "Else");
	      code.addElement(g);
	  }  
          Expr elsP= trExpr(false, x.els);
	  code.addElement(GC.gets(result, elsP));
	  GuardedCmd elsebody= GC.seq(GuardedCmdVec.popFromStackVector(code));
	  
	  code.addElement(GC.ifcmd(test, thenbody, elsebody));
	  return result;
	}

      case TagConstants.INSTANCEOFEXPR:
	{
	  InstanceOfExpr x= (InstanceOfExpr)expr;
	  Expr obj = trExpr(protect, x.expr);

	  Expr isOfType = GC.nary(x.getStartLoc(), x.getEndLoc(), 
			 TagConstants.IS, obj,
			 TypeExpr.make(x.type.getStartLoc(),
				       x.type.getEndLoc(),
				       x.type));
	  Expr notNull = GC.nary(x.getStartLoc(), x.getEndLoc(), 
			 TagConstants.REFNE, obj, GC.nulllit);

	  return GC.and(x.getStartLoc(), x.getEndLoc(),
			 isOfType,
			 notNull);
	}

      case TagConstants.CASTEXPR:
	{
	  CastExpr x= (CastExpr)expr;
	  Expr result= trExpr(protect, x.expr);
	  Expr te = GC.typeExpr(x.type);
	  if (Types.isReferenceType(x.type)) {
	    addCheck(expr,
		     TagConstants.CHKCLASSCAST,
		     GC.nary(TagConstants.IS, result, te));
	  }
	  result = GC.nary(TagConstants.CAST, result, te);
	  return result;
	}

      case TagConstants.CLASSLITERAL:
	{
	  ClassLiteral x= (ClassLiteral)expr;
	  return GC.nary(x.getStartLoc(), x.getEndLoc(),
			 TagConstants.CLASSLITERALFUNC,
			 TypeExpr.make(x.type.getStartLoc(),
				       x.type.getEndLoc(),
				       x.type));
	}

	// Binary expressions
      case TagConstants.OR: case TagConstants.AND:
	{
	  BinaryExpr x= (BinaryExpr)expr;
	  VariableAccess result= fresh(x, x.locOp,
				       (tag == TagConstants.OR ?
					"cor" : "cand"));
	  Expr left= trExpr(false, x.left);

	  // Create a chunk of code that evaluates the right expr and
	  // saves its value in "result"
	  code.push();
	  Expr right= trExpr(false, x.right);
	  code.addElement(GC.gets(result, right));
	  GuardedCmd rightbody= GC.seq(GuardedCmdVec.popFromStackVector(code));

	  GuardedCmd thn, els;
	  if (tag == TagConstants.OR) {
	    thn= GC.gets(result, GC.truelit);
	    els= rightbody;
	  } else {
	    thn= rightbody;
	    els= GC.gets(result, GC.falselit);
	  }
	  if (Main.traceInfo > 0) {
	      // add labels to track which operands are evaluated
	      GuardedCmd g = traceInfoLabelCmd(x, "FirstOpOnly", x.locOp);
	      if (tag == TagConstants.OR) {
		  thn = GC.seq(g, thn);
	      }
	      else {
		  els = GC.seq(g, els);
	      }
	  }   
	  code.addElement(GC.ifcmd(left, thn, els));
	  return result;
	}

      case TagConstants.BITOR: case TagConstants.BITXOR:
      case TagConstants.BITAND: case TagConstants.NE:
      case TagConstants.EQ: case TagConstants.GE:
      case TagConstants.GT: case TagConstants.LE:
      case TagConstants.LT: case TagConstants.LSHIFT:
      case TagConstants.RSHIFT: case TagConstants.URSHIFT:
      case TagConstants.ADD: case TagConstants.SUB:
      case TagConstants.STAR:
      case TagConstants.DIV: case TagConstants.MOD:
	{
	  BinaryExpr x= (BinaryExpr)expr;
	  Expr left= trExpr(Purity.impure(x.right), x.left);
	  Expr right= trExpr(false, x.right);
	  if (tag == TagConstants.DIV || tag == TagConstants.MOD) {
	    if (Types.isIntegralType(TypeCheck.inst.getType(x.right))) {
	    addCheck(x.locOp, TagConstants.CHKARITHMETIC,
		     GC.nary(TagConstants.INTEGRALNE, right, GC.zerolit));
	    }
	  }
	  int newtag= TrAnExpr.getGCTagForBinary(x);
	  return protect(protect, GC.nary(x.getStartLoc(), x.getEndLoc(),
					  newtag, left, right),
			 x.locOp);
	}

      case TagConstants.ASSIGN:
	{
	  BinaryExpr x= (BinaryExpr)expr;
	  Expr right= x.right;
	  Expr left= x.left;

	  int ltag = left.getTag();
	  if (ltag == TagConstants.VARIABLEACCESS) {
	    VariableAccess lhs= (VariableAccess)left;
	    Expr rval= trExpr(false, right);
	    writeCheck(lhs, right, rval, x.locOp, false);
	    code.addElement(GC.gets(lhs, rval));
	    VariableAccess init= getInitVar(lhs.decl);
	    if (init != null)
	      code.addElement(GC.gets(init, GC.truelit));
	    return protect(protect, left, x.locOp, lhs.decl.id.toString() + "=");
	    
	  } else if (ltag == TagConstants.FIELDACCESS) {
	    Expr lhs= trFieldAccess(true, (FieldAccess)left);
	    Expr rval= trExpr(false, right);
	    String name;
	    writeCheck(lhs, right, rval, x.locOp, false);
	    if (lhs.getTag() == TagConstants.VARIABLEACCESS) {
	      VariableAccess vaLhs = (VariableAccess)lhs;
	      name = vaLhs.decl.id.toString();
	      code.addElement(GC.gets(vaLhs, rval));
	    } else {
	      NaryExpr target= (NaryExpr)lhs;
	      VariableAccess field= (VariableAccess)target.exprs.elementAt(0);
	      name = field.decl.id.toString();
	      Expr obj= target.exprs.elementAt(1);
	      code.addElement(GC.subgets(field, obj, rval));
	    }
	    return protect(protect, lhs, x.locOp, name + "=");
	    
	  } else if (ltag == TagConstants.ARRAYREFEXPR) {
	    ArrayRefExpr lhs= (ArrayRefExpr)left;

	    Expr array= trExpr(true, lhs.array);
	    Expr index= trExpr(true, lhs.index);
	    Expr rval= trExpr(false, right);

	    arrayAccessCheck(lhs.array, array, lhs.index, index, lhs.locOpenBracket);
	    if (! isFinal(TypeCheck.inst.getType(lhs.array))) {
	      addCheck(x.locOp,
		       TagConstants.CHKARRAYSTORE,
		       GC.nary(TagConstants.IS, rval,
			       GC.nary(TagConstants.ELEMTYPE,
				       GC.nary(TagConstants.TYPEOF, array))),
		       Location.NULL, lhs.array);
	    }

	    code.addElement(GC.subsubgets(GC.elemsvar, array, index, rval));
	    Expr a= GC.select(GC.elemsvar, array);
	    return protect(protect, GC.select(a, index), x.locOp);
	    
	  } else {
	    Assert.fail("Unexpected tag " + TagConstants.toString(ltag)
			+ " (" + ltag + ")");
	  }
	}

      case TagConstants.INC: case TagConstants.DEC:
      case TagConstants.POSTFIXINC: case TagConstants.POSTFIXDEC:
      case TagConstants.ASGMUL:
      case TagConstants.ASGADD: case TagConstants.ASGSUB:
      case TagConstants.ASGLSHIFT: case TagConstants.ASGRSHIFT:
      case TagConstants.ASGURSHIFT: case TagConstants.ASGBITAND:
      case TagConstants.ASGBITOR: case TagConstants.ASGBITXOR:
      case TagConstants.ASGDIV: case TagConstants.ASGREM:
	{
	  Expr right, left;
	  int op;
	  int locOp;
	  Type rType;
	  if (expr instanceof UnaryExpr) {
	    UnaryExpr u= (UnaryExpr) expr;
	    right= GC.onelit;
	    rType = Types.intType;
	    left= u.expr;
	    op= TrAnExpr.getGCTagForUnary(u);
	    locOp = u.locOp;
	  } else {
	    BinaryExpr x= (BinaryExpr)expr;
	    right= x.right;
	    rType = TypeCheck.inst.getType(right);
	    left= x.left;
	    op= TrAnExpr.getGCTagForBinary(x);
	    locOp = x.locOp;
	  }
	  Type lType = TypeCheck.inst.getType(left);
	  boolean returnold= (tag == TagConstants.POSTFIXINC
			      || tag == TagConstants.POSTFIXDEC);

	  int ltag = left.getTag();
	  if (ltag == TagConstants.VARIABLEACCESS) {
	    VariableAccess lhs= (VariableAccess)left;
	    readCheck(lhs, lhs.getStartLoc());

	    Expr oldLval= protect(Purity.impure(right) || returnold, lhs,
				  locOp, "old!" + lhs.decl.id.toString());
	    Expr rval= trExpr(false, right);

	    if (tag == TagConstants.ASGDIV || tag == TagConstants.ASGREM) {
	      Assert.notFalse(expr instanceof BinaryExpr);
	      if (Types.isIntegralType(TypeCheck.inst.getType(right))) {
		addCheck(locOp, TagConstants.CHKARITHMETIC,
			 GC.nary(TagConstants.INTEGRALNE, rval, GC.zerolit));
	      }
	    }

	    rval= GC.nary(expr.getStartLoc(), expr.getEndLoc(), op, oldLval, rval);
	    rval= addRelAsgCast(rval, lType, rType);

	    writeCheck(lhs, null, rval, locOp, false);
	    code.addElement(GC.gets(lhs, rval));
	    if (returnold) {
	      return oldLval;
	    } else {
	      return protect(protect, lhs, locOp, lhs.decl.id.toString() + "=");
	    }
	    
	  } else if (ltag == TagConstants.FIELDACCESS) {
	    FieldAccess lhs = (FieldAccess)left;
            Expr lval= trFieldAccess(true, lhs);
	    readCheck(lval, lhs.locId);

	    String name = lhs.decl.id.toString();
	    Expr oldLval = protect(Purity.impure(right) || returnold, lval,
				   locOp, "old!" + name);
            Expr rval= trExpr(false, right);
	    if (tag == TagConstants.ASGDIV || tag == TagConstants.ASGREM) {
	      Assert.notFalse(expr instanceof BinaryExpr);
	      if (Types.isIntegralType(TypeCheck.inst.getType(right))) {
		addCheck(locOp, TagConstants.CHKARITHMETIC,
			 GC.nary(TagConstants.INTEGRALNE, rval, GC.zerolit));
	      }
	    }

	    rval= GC.nary(expr.getStartLoc(), expr.getEndLoc(),
			  op, oldLval, rval);
	    rval= addRelAsgCast(rval, lType, rType);

            writeCheck(lval, null, rval, locOp, false);
            if (lval.getTag() == TagConstants.VARIABLEACCESS) {
              code.addElement(GC.gets((VariableAccess)lval, rval));
	    } else {
              NaryExpr target= (NaryExpr)lval;
	      VariableAccess field= (VariableAccess)target.exprs.elementAt(0);
              Expr obj= target.exprs.elementAt(1);
              code.addElement(GC.subgets(field, obj, rval));
	    }
	    if (returnold) {
	      return oldLval;
	    } else {
	      return protect(protect, lval, locOp, name + "=");
	    }
	    
	  } else if (ltag == TagConstants.ARRAYREFEXPR) {
	    ArrayRefExpr lhs= (ArrayRefExpr)left;

	    Expr array= trExpr(true, lhs.array);
	    Expr index= trExpr(true, lhs.index);
	    arrayAccessCheck(lhs.array, array, lhs.index, index, lhs.locOpenBracket);
	    Expr oldLval = protect(Purity.impure(right) || returnold,
				   GC.select(GC.select(GC.elemsvar, array),
					     index),
				   locOp, "old");

	    Expr rval= trExpr(false, right);
	    if (tag == TagConstants.ASGDIV || tag == TagConstants.ASGREM) {
	      Assert.notFalse(expr instanceof BinaryExpr);
	      if (Types.isIntegralType(TypeCheck.inst.getType(right))) {
		addCheck(locOp, TagConstants.CHKARITHMETIC,
			 GC.nary(TagConstants.INTEGRALNE, rval, GC.zerolit));
	      }
	    }

	    rval= GC.nary(expr.getStartLoc(), expr.getEndLoc(),
			  op, oldLval, rval);
	    rval= addRelAsgCast(rval, lType, rType);
    
	    code.addElement(GC.subsubgets(GC.elemsvar, array, index, rval));
	    if (returnold) {
	      return oldLval;
	    } else {
	      Expr a= GC.select(GC.elemsvar, array);
	      return protect(protect, GC.select(a, index), locOp);
	    }
	    
	  } else {
	    Assert.fail("Unexpected tag " + TagConstants.toString(ltag)
			+ " (" + ltag + ")");
	  }
	}

	// Unary expressions
      case TagConstants.UNARYADD:
	{
	  // drop UnaryADD
	  UnaryExpr x= (UnaryExpr)expr;
	  return trExpr(protect, x.expr);
	}
	
      case TagConstants.UNARYSUB:
      case TagConstants.NOT: case TagConstants.BITNOT:
	{
	  UnaryExpr x= (UnaryExpr)expr;
	  Expr x2= trExpr(false, x.expr);
	  int newtag= TrAnExpr.getGCTagForUnary(x);
	  return protect(protect, GC.nary(x.getStartLoc(), x.getEndLoc(),
					  newtag, x2),
			 x.locOp);
	}

      case TagConstants.PARENEXPR:
	{
	  ParenExpr x= (ParenExpr)expr;
	  return trExpr(protect, x.expr);
	}

      case TagConstants.VARIABLEACCESS:
	{
	  VariableAccess x= (VariableAccess)expr;
	  readCheck(x, x.getStartLoc());
	  return protect(protect, x, x.getStartLoc(), x.decl.id.toString());
	}
      
      case TagConstants.FIELDACCESS:
	{
	  FieldAccess fa = (FieldAccess)expr;
	  Expr result = trFieldAccess(false, fa);
	  if (fa.decl != Types.lengthFieldDecl)
	    readCheck(result, fa.locId);
	  return protect(protect, result, fa.locId, fa.decl.id.toString());
	}
      
      case TagConstants.METHODINVOCATION:
	{
	  return trMethodInvocation(protect, (MethodInvocation)expr);
	}

      default:
	//@ unreachable;
	Assert.fail("UnknownTag<" + tag + ">");
	return null;
    }
  }

  private static Expr addRelAsgCast(Expr rval, Type lType, Type rType) {
    if (!(lType instanceof PrimitiveType))
      return rval;
    
    switch (lType.getTag()) {
      case TagConstants.BYTETYPE:
      case TagConstants.SHORTTYPE:
      case TagConstants.CHARTYPE:
	break;  // cast needed
      case TagConstants.INTTYPE:
	if (Types.isLongType(rType) || Types.isFloatingPointType(rType)) {
	  break;  // cast needed
	} else {
	  return rval;
	}
      case TagConstants.LONGTYPE:
	if (Types.isFloatingPointType(rType)) {
	  break;  // cast needed
	} else {
	  return rval;
	}
      case TagConstants.FLOATTYPE:
      case TagConstants.DOUBLETYPE:
	return rval;
      default:
	return rval;
    }
    return GC.nary(TagConstants.CAST, rval, GC.typeExpr(lType));
  }
  
  /** Returns the guarded-command expression:
    * <pre>
    * (FORALL o :: !isAllocated(o, allocOld) && isAllocated(o, allocNew)
    *              ==> isNewArray(o))
    * </pre>
    **/
  
  private static Expr predEvathangsAnArray(VariableAccess allocOld,
					   VariableAccess allocNew) {
    LocalVarDecl odecl = UniqName.newBoundVariable('o');
    VariableAccess o = TrAnExpr.makeVarAccess(odecl, Location.NULL);

    Expr e0 = GC.not(GC.nary(TagConstants.ISALLOCATED, o, allocOld));
    Expr e1 = GC.nary(TagConstants.ISALLOCATED, o, allocNew);
    Expr e2 = GC.nary(TagConstants.ISNEWARRAY, o);

    Expr body = GC.implies(GC.and(e0, e1), e2);
			   
    // TBW:  "e2" should be the trigger of the following quantification
    return GC.forall(odecl, body);
  }


  /** Returns the guarded-command expression:
    * <pre>
    * (FORALL o :: !isAllocated(o, allocOld) && isAllocated(o, allocNew)
    *              ==> o.owner == null)
    * </pre>
    **/  
  private static Expr predArrayOwnerNull(VariableAccess allocOld,
					 VariableAccess allocNew,
					 VariableAccess arr) {
      // get java.lang.Object
      TypeSig obj = Types.javaLangObject();
      
      FieldDecl owner = null; // make the compiler happy
      boolean notFound = false;
      try {
	  owner = Types.lookupField(obj, Identifier.intern("owner"),
				    obj);
      }
      catch (javafe.tc.LookupException e) {
	  notFound = true;
      }
      // if we couldn't find the owner ghost field, there's nothing to do
      if (notFound)
	  return null;
      
      VariableAccess ownerVA = TrAnExpr.makeVarAccess(owner, Location.NULL);

      LocalVarDecl odecl = UniqName.newBoundVariable('o');
      VariableAccess o = TrAnExpr.makeVarAccess(odecl, Location.NULL);

      Expr e0 = GC.not(GC.nary(TagConstants.ISALLOCATED, o, allocOld));
      Expr e1 = GC.nary(TagConstants.ISALLOCATED, o, allocNew);
      Expr e2 =  GC.nary(TagConstants.REFEQ, GC.select(ownerVA, arr),
			 GC.nulllit);
      
    
      Expr body = GC.implies(GC.and(e0, e1), e2);
      
      return GC.forall(odecl, body);
  }
  

  //// Should be moved type javafe.tc.Types

  /** Returns true if there can be no subtypes of <code>t</code>.  The
      definition of final used by this method is as follows.  Reference
      types are "final" if they have the <code>final</code> modifier.
      Array types are "final" if their element types satisfy
      <code>isFinal</code>.  Primitive types are "final". */

  public static boolean isFinal(Type t) {
    int tag= t.getTag();
    switch (tag) {
      case TagConstants.BOOLEANTYPE:
      case TagConstants.INTTYPE:
      case TagConstants.LONGTYPE:
      case TagConstants.CHARTYPE:
      case TagConstants.FLOATTYPE:
      case TagConstants.DOUBLETYPE:
      case TagConstants.BYTETYPE:
      case TagConstants.SHORTTYPE:
	return true;

      case TagConstants.ARRAYTYPE:
	return isFinal(((ArrayType)t).elemType);

      case TagConstants.TYPENAME:
	t= TypeCheck.inst.getSig((TypeName)t);
      case TagConstants.TYPESIG:
	TypeSig ts= (TypeSig)t;
	return Modifiers.isFinal(ts.getTypeDecl().modifiers);

      default:
	//@ unreachable
	Assert.fail("Unexpected tag " + TagConstants.toString(tag) + " ("
+ tag + ")");
	return false;
    }
  }

  //// Factor processing of FieldAccess nodes

  /** Returns either a <code>VariableAccess</code> if <code>fa</code>
      is a class variable or a <code>SELECT</code> application if
      <code>fa</code> is an instance variable access, or an
      <code>ARRAYLENGTH</code> application if <code>fa</code> is
      the final array field <code>length</code>.  In the second
      case, will emit code for computing the object designator and also
      a check to ensure that object is not null. */

  private Expr trFieldAccess(boolean protectObject, FieldAccess fa) {
    VariableAccess va= TrAnExpr.makeVarAccess(fa.decl, fa.locId);

    int tag= fa.od.getTag();
    if (Modifiers.isStatic(fa.decl.modifiers)) {
      // static field
      switch (tag) {
	case TagConstants.TYPEOBJECTDESIGNATOR:
	case TagConstants.SUPEROBJECTDESIGNATOR:
	  break;
	case TagConstants.EXPROBJECTDESIGNATOR: {
	  ExprObjectDesignator od= (ExprObjectDesignator)fa.od;
	  Expr discardResult = trExpr(false, od.expr);
	  break;
	}
	default:
	  //@ unreachable
	  Assert.fail("Unexpected tag " + TagConstants.toString(tag)
		      + " (" + tag + ")");
	  break;
      }
      return va;
	  
    } else {
      // instance variable
      Expr obj;
      switch (tag) {
	case TagConstants.EXPROBJECTDESIGNATOR: {
	  ExprObjectDesignator od= (ExprObjectDesignator)fa.od;
	  obj= trExpr(protectObject, od.expr);
	  nullCheck(od.expr, obj, fa.od.locDot);
	  break;
	}
	case TagConstants.SUPEROBJECTDESIGNATOR:
	  obj= GC.thisvar;
	  break;
	case TagConstants.TYPEOBJECTDESIGNATOR:
	  // This case is not legal Java and should already have been
	  // checked by the type checker
	  //@ unreachable
	  Assert.fail("Unexpected tag");
	  obj= null;  // dummy assignment
	  break;
	default:
	  //@ unreachable
	  Assert.fail("Unexpected tag " + TagConstants.toString(tag)
		      + " (" + tag + ")");
	  obj= null;  // dummy assignment
	  break;
      }

      Expr res;
      if (fa.decl == Types.lengthFieldDecl) {
	return GC.nary(fa.getStartLoc(), fa.getEndLoc(),
		       TagConstants.ARRAYLENGTH, obj);
      } else {
	return GC.nary(fa.getStartLoc(), fa.getEndLoc(),
		       TagConstants.SELECT, va, obj);
      }
    }
  }

  
  /** This translation of method invocation follows section 4.1 of
    * ESCJ 16.
    **/
  
  private Expr trMethodInvocation(boolean protect, MethodInvocation mi) {

    boolean isStatic = Modifiers.isStatic(mi.decl.modifiers);
    // for holding the translated arguments
    ExprVec args = ExprVec.make(mi.args.size() + 1);
    ExprVec argsRaw = ExprVec.make(mi.args.size() + 1);
    Expr nullcheckArg = null;  // Java expression
    /*@ readable_if nullcheckArg != null */
    int nullcheckLoc = Location.NULL /*@ uninitialized */;

    int tag= mi.od.getTag();
    switch (tag) {
      case TagConstants.TYPEOBJECTDESIGNATOR: {
	Assert.notFalse(isStatic);  // non-static is not legal Java
	// the arguments are translated below
	break;
      }
      case TagConstants.EXPROBJECTDESIGNATOR: {
	ExprObjectDesignator od = (ExprObjectDesignator)mi.od;
	if (isStatic) {
	  Expr discardResult = trExpr(false, od.expr);
	} else {
	  // protect "self" if there are more arguments
	  Expr self = trExpr(mi.args.size() != 0, od.expr);
	  args.addElement(self);
	  argsRaw.addElement(od.expr);
	  nullcheckArg = od.expr;
	  nullcheckLoc = od.locDot;
	}
	// the (rest of) the arguments are translated below
	break;
      }
      case TagConstants.SUPEROBJECTDESIGNATOR: {
	if (! isStatic) {
	  args.addElement(GC.thisvar);
	  argsRaw.addElement(ThisExpr.make(null, mi.od.getStartLoc()));
	}
	// the (rest of) the arguments are translated below
	break;
      }
      default:
	//@ unreachable
	Assert.fail("Unexpected tag " + TagConstants.toString(tag)
		    + " (" + tag + ")");
	break;
    }
    

    // translate remaining arguments
    for (int i = 0; i < mi.args.size(); i++) {
      // protect all but the last argument
      Expr argRaw = mi.args.elementAt(i);
      Expr arg = trExpr(i != mi.args.size()-1, argRaw);
      args.addElement(arg);
      argsRaw.addElement(argRaw);
    }

    if (nullcheckArg != null) {
      nullCheck(nullcheckArg, args.elementAt(0), nullcheckLoc);
    }

    InlineSettings is = (InlineSettings)inlineDecoration.get(mi);
    code.addElement( call( mi.decl, argsRaw, args, scope, mi.locId, 
			   mi.getEndLoc(), mi.locOpenParen, false, is));
    return protect(protect, GC.resultvar, mi.locOpenParen,
		   mi.decl.id.toString());
  }

  
  //// Helper methods for generating check assertions/assumptions

  /** Emit a check at location <code>loc</code> that guarded command
    * expression <code>e</code>, which was translated from the Java
    * expression <code>E</code>, is not <code>null</code>.  If <code>E</code>
    * denotes an expression that is guaranteed not to be <code>null</code>,
    * no check is emitted.
    **/
  
  private void nullCheck(VarInit E, Expr e, int loc) {
    nullCheck(E, e, loc, TagConstants.CHKNULLPOINTER, Location.NULL);
  }

  private void nullCheck(VarInit E, Expr e, int loc,
			 int errorName, int locPragma) {
    // start by peeling off parentheses and casts
    E = trim(E);

    switch (E.getTag()) {
      case TagConstants.THISEXPR:
	return;

      case TagConstants.VARIABLEACCESS: {
	GenericVarDecl d = ((VariableAccess)E).decl;
	SimpleModifierPragma nonNullPragma = GetSpec.NonNullPragma(d);
	if (nonNullPragma != null && !Main.guardedVC) {
	  LabelInfoToString.recordAnnotationAssumption(nonNullPragma.getStartLoc());
	  return;
	}
	break;  // perform check
      }

      case TagConstants.FIELDACCESS: {
	FieldDecl fd = ((FieldAccess)E).decl;
	SimpleModifierPragma nonNullPragma = GetSpec.NonNullPragma(fd);
	if (nonNullPragma != null && !Main.guardedVC) {
	  if (Modifiers.isStatic(fd.modifiers) ||
	      rdCurrent.getTag() != TagConstants.CONSTRUCTORDECL ||
	      rdCurrent.parent != fd.parent) {
	    LabelInfoToString.recordAnnotationAssumption(nonNullPragma.getStartLoc());
	    return;
	  }
	}
	break;  // perform check
      }

      default:
	break;  // perform check
    }
    
    Expr nullcheck = GC.nary(TagConstants.REFNE, e, GC.nulllit);
    addCheck(loc, errorName, nullcheck, locPragma, E);
  }
  
  /** Peels off parentheses and casts from <code>E</code> and returns the
    * result
    **/
  
  private VarInit trim(VarInit E) {
    while (true) {
      if (E.getTag() == TagConstants.PARENEXPR) {
	E = ((ParenExpr)E).expr;
      } else if (E.getTag() == TagConstants.CASTEXPR) {
	E = ((CastExpr)E).expr;
      } else {
	return E;
      }
    }
  }
  
  /** Emit the checks that <code>array</code> is non-null and that
      <code>index</code> is inbounds for <code>array</code>.  Implements
      the ArrayAccessCheck function of ESCJ16. */

  private void arrayAccessCheck(Expr Array, Expr array,
				Expr Index, Expr index,
				int locOpenBracket) {
    nullCheck(Array, array, locOpenBracket);

    Expr length= GC.nary(TagConstants.ARRAYLENGTH, array);
    Expr indexNeg = GC.nary(TagConstants.INTEGRALLE, GC.zerolit, index);
    addCheck(locOpenBracket, TagConstants.CHKINDEXNEGATIVE, indexNeg,
	     Location.NULL, trim(Index));
    Expr indexTooBig = GC.nary(TagConstants.INTEGRALLT, index, length);
    addCheck(locOpenBracket, TagConstants.CHKINDEXTOOBIG, indexTooBig);
  }

  /** Used by <code>readCheck</code> and <code>writeCheck</code> to
      accumulate the list of mutexes protecting a shared variable.
      Thus, these methods are not thread re-entrant. */
  private ExprVec mutexList= ExprVec.make(new Expr[5]);

  /** Insert checks done before reading variables.  The argument
      <code>va</code> is the variable being read; it must be either a
      <code>VariableAccess</code> (in the case of local variables and class
      fields) or a <code>SELECT</code> <code>NaryExpr</code> (in the case
      of instance fields).  The argument <code>locId</code> is the location
      of the variable or field being read.  It is used to determine the location
      of the "wrong" part of the check's label.

      <p> This method implements ReadCheck as defined in ESCJ16.
      Handles reads of local, class, and instance variables (ESCJ16
      splits these into two ReadCheck functions). */

  private void readCheck(Expr va, int locId) {
    Assert.notFalse(locId != Location.NULL);
    // "d" is the declaration of the variable being read
    GenericVarDecl d;
    Expr actualSelf = null;
    if (va.getTag() == TagConstants.SELECT) {
      NaryExpr sel= (NaryExpr)va;
      d= ((VariableAccess)sel.exprs.elementAt(0)).decl;
      actualSelf = sel.exprs.elementAt(1);
    } else {
      d= ((VariableAccess)va).decl;
    }

    if (d.pmodifiers == null) return;

    Hashtable map = null;

    mutexList.removeAllElements();
    ModifierPragma firstMonitoredPragma = null;
    for (int i= 0; i < d.pmodifiers.size(); i++) {
      ModifierPragma prag= d.pmodifiers.elementAt(i);
      int tag= prag.getTag();
      switch (tag) {
	case TagConstants.NON_NULL:
	case TagConstants.SPEC_PUBLIC:
	case TagConstants.WRITABLE_IF:
	  break;
	  
	case TagConstants.UNINITIALIZED:
	  // "uninitialized" can be used only with local variables
	  Assert.notFalse(va.getTag() != TagConstants.SELECT);
	  VariableAccess init= getInitVar((LocalVarDecl) d);
	  addCheck(locId, TagConstants.CHKINITIALIZATION, init, prag);
	  break;

	case TagConstants.DEFINED_IF:
	  map = initializeRWCheckSubstMap(map, actualSelf, locId);
	  Expr dc = TrAnExpr.trSpecExpr(((ExprModifierPragma)prag).expr, map, null);
	  addCheck(locId, TagConstants.CHKDEFINEDNESS, dc, prag);
	  break;

	case TagConstants.MONITORED_BY: {
	  ExprModifierPragma emp = (ExprModifierPragma)prag;
	  map = initializeRWCheckSubstMap(map, actualSelf, locId);
	  mutexList.addElement(TrAnExpr.trSpecExpr(emp.expr, map, null));
	  if (firstMonitoredPragma == null)
	    firstMonitoredPragma = prag;
	  break;
	}

	case TagConstants.MONITORED:
	  Assert.notFalse(d instanceof FieldDecl);
	  if (Modifiers.isStatic(d.modifiers)) {
	    mutexList.addElement(getClassObject(((FieldDecl)d).parent));
	  } else {
	    mutexList.addElement(actualSelf);
	  }
	  if (firstMonitoredPragma == null)
	    firstMonitoredPragma = prag;
	  break;

	default:
	  Assert.fail("Unexpected tag \"" + TagConstants.toString(tag)
		      + "\" (" + tag + ")");
      }
    }

    if (mutexList.size() != 0) {
      Expr onelocked= GC.falselit;
      for (int i= mutexList.size()-1; 0 <= i; i--) {
	Expr mu= mutexList.elementAt(i);
	onelocked= GC.or(GC.and(GC.nary(TagConstants.REFNE, mu, GC.nulllit),
				GC.nary(TagConstants.SELECT, GC.LSvar, mu)),
			 onelocked);
      }
      if (rdCurrent instanceof ConstructorDecl && actualSelf != null) {
	// In constructors, always allow access to the fields of the object
	// being constructed.
	// Note: The following could be optimized so that if "actualSelf"
	// is ``obviously'' "this", then the check could be omitted altogether.
	onelocked = GC.or(GC.nary(TagConstants.REFEQ, actualSelf, GC.thisvar),
			  onelocked);
      }
      addCheck(locId, TagConstants.CHKSHARING, onelocked,
	       firstMonitoredPragma);
    }
    mutexList.removeAllElements(); // Help the garbage collector...
  }

  /** Insert checks done before writing variables, as prescribed by
    * WriteCheck in ESCJ 16.  Handles writes of local, class, and
    * instance variables (ESCJ 16 splits these into two WriteCheck
    * functions).<p>
    *
    * The argument <code>va</code> is the variable being written; it must
    * be either a <code>VariableAccess</code> (in the case of local variables
    * and class fields) or a <code>SELECT</code> <code>NaryExpr</code> (in
    * the case of instance fields).<p>
    *
    * The argument <code>rval</code> is the guarded command expression being
    * written into <code>va</code>.  The argument <code>Rval</code> is either
    * the Java expression from which <code>rval</code> was translated, or
    * <code>null</code> if <code>rval</code> was somehow synthesized.<p>
    *
    * The argument <code>locAssignOp</code> is the location of the assignment
    * operator that prescribes the write.  It is used to determine the location
    * of the "wrong" part of the check's label.<p>
    *
    * The argument <code>inInitializerContext</code> indicates whether or not
    * the expression whose write check is to be performed is the initializing
    * expression of a field.
    **/

  private void writeCheck(Expr va, VarInit Rval, Expr rval, int locAssignOp,
			  boolean inInitializerContext) {
    Assert.notFalse(locAssignOp != Location.NULL);
    // "d" is the declaration of the variable being written
    GenericVarDecl d;
    Expr actualSelf = null;
    if (va.getTag() == TagConstants.SELECT) {
      NaryExpr sel= (NaryExpr)va;
      d= ((VariableAccess)sel.exprs.elementAt(0)).decl;
      actualSelf = sel.exprs.elementAt(1);
    } else {
      d= ((VariableAccess)va).decl;
    }

    // Handle non_null variables
    SimpleModifierPragma nonNullPragma = GetSpec.NonNullPragma(d);
    if (nonNullPragma != null) {
      if (Rval == null) {
	Expr nullcheck = GC.nary(TagConstants.REFNE, rval, GC.nulllit);
	addCheck(locAssignOp, TagConstants.CHKNONNULL, nullcheck,
		 nonNullPragma.getStartLoc());
      } else if (!Main.excuseNullInitializers || !inInitializerContext ||
		 trim(Rval).getTag() != TagConstants.NULLLIT) {
	nullCheck(Rval, rval, locAssignOp, TagConstants.CHKNONNULL,
		  nonNullPragma.getStartLoc());
      }
    }

    if (d.pmodifiers == null) return;

    Hashtable map = null;

    mutexList.removeAllElements();
    Expr onenotnull= GC.falselit;
    ModifierPragma firstMonitoredPragma = null;
    for (int i= 0; i < d.pmodifiers.size(); i++) {
      ModifierPragma prag= d.pmodifiers.elementAt(i);
      int tag= prag.getTag();
      switch (tag) {
	case TagConstants.UNINITIALIZED:
	case TagConstants.DEFINED_IF:
	case TagConstants.SPEC_PUBLIC:
	case TagConstants.NON_NULL:		// handled above
	  break;

	case TagConstants.WRITABLE_IF:
	  map = initializeRWCheckSubstMap(map, actualSelf, locAssignOp);
	  Expr dc = TrAnExpr.trSpecExpr(((ExprModifierPragma)prag).expr, map, null);
	  addCheck(locAssignOp, TagConstants.CHKWRITABLE, dc, prag);
	  break;

	case TagConstants.MONITORED_BY: {
	  ExprModifierPragma emp = (ExprModifierPragma)prag;
	  map = initializeRWCheckSubstMap(map, actualSelf, locAssignOp);
	  mutexList.addElement(TrAnExpr.trSpecExpr(emp.expr, map, null));
	  if (firstMonitoredPragma == null)
	    firstMonitoredPragma = prag;
	  break;
	}

	case TagConstants.MONITORED:
	  Assert.notFalse(d instanceof FieldDecl);
	  if (Modifiers.isStatic(d.modifiers)) {
	    mutexList.addElement(getClassObject(((FieldDecl)d).parent));
	  } else {
	    mutexList.addElement(actualSelf);
	  }
          onenotnull= GC.truelit;
	  if (firstMonitoredPragma == null)
	    firstMonitoredPragma = prag;
	  break;

	default:
	  Assert.fail("Unexpected tag \"" + TagConstants.toString(tag)
		      + "\" (" + tag + ")");
      }
    }

    if (mutexList.size() != 0) {
      Expr allnullorlocked= GC.truelit;
      for (int i= mutexList.size()-1; 0 <= i; i--) {
	Expr mu= mutexList.elementAt(i);
	onenotnull= GC.or(GC.nary(TagConstants.REFNE, mu, GC.nulllit),
			  onenotnull);
	allnullorlocked=
	  GC.and(GC.or(GC.nary(TagConstants.REFEQ, mu, GC.nulllit),
		       GC.select(GC.LSvar, mu)),
		 allnullorlocked);
      }
      Expr p = GC.and(onenotnull, allnullorlocked);
      if (rdCurrent instanceof ConstructorDecl && actualSelf != null) {
	// In constructors, always allow access to the fields of the object
	// being constructed.
	// Note: The following could be optimized so that if "actualSelf"
	// is ``obviously'' "this", then the check could be omitted altogether.
	p = GC.or(GC.nary(TagConstants.REFEQ, actualSelf, GC.thisvar), p);
      }
      addCheck(locAssignOp, TagConstants.CHKSHARING, p, firstMonitoredPragma);
    }
    mutexList.removeAllElements(); // Help the garbage collector...
  }

  /** The following method is used in readCheck and writeCheck to lazily
    * construct a substitution map (which may also create another temporary
    * variable) */
  
  private Hashtable initializeRWCheckSubstMap(Hashtable prevMap,
					      Expr actualSelf,
					      int loc) {
    if (actualSelf == null) {
      // no map needed
      Assert.notFalse(prevMap == null);
      return null;
    } else if (prevMap != null) {
      return prevMap;
    } else {
      Hashtable map = new Hashtable();
      VariableAccess vaSelf;
      if (actualSelf instanceof VariableAccess) {
	vaSelf = (VariableAccess)actualSelf;
      } else {
	vaSelf = (VariableAccess)protect(true, actualSelf, loc, "od");
      }
      map.put(GC.thisvar.decl, vaSelf);
      return map;
    }
  }
  
  /** Calls <code>GC.check</code> to create a check and appends the
      result to <code>code</code>. */

  //@ modifies code.elementCount;
  private void addCheck(int locUse, Condition cond) {
    code.addElement(GC.check(locUse, cond));
  }
  
  /** Calls <code>GC.check</code> to create a check and appends the
      result to <code>code</code>. */

  //@ modifies code.elementCount;
  private void addCheck(int locUse, int errorName, Expr pred) {
    addCheck(locUse, errorName, pred, Location.NULL);
  }
  
  /** Calls <code>GC.check</code> to create a check and appends the
      result to <code>code</code>. */

  //@ modifies code.elementCount;
  private void addCheck(ASTNode use, int errorName, Expr pred) {
    code.addElement(GC.check(use.getStartLoc(),
			     errorName, pred,
			     Location.NULL));
  }

  /** Calls <code>GC.check</code> to create a check and appends the
      result to <code>code</code>. */

  //@ modifies code.elementCount;
  private void addCheck(int locUse, int errorName, Expr pred, int locPragmaDecl) {
    code.addElement(GC.check(locUse, errorName, pred, locPragmaDecl));
  }
  
  /** Calls <code>GC.check</code> to create a check and appends the
      result to <code>code</code>. */

  //@ modifies code.elementCount;
  private void addCheck(int locUse, int errorName, Expr pred, int locPragmaDecl,
			Object aux) {
    code.addElement(GC.check(locUse, errorName, pred, locPragmaDecl, aux));
  }
  
  /** Calls <code>GC.check</code> to create a check and appends the
      result to <code>code</code>. */

  //@ modifies code.elementCount;
  private void addCheck(int locUse, int errorName, Expr pred, ASTNode prag) {
    code.addElement(GC.check(locUse, errorName, pred, prag.getStartLoc()));
  }

  /** Return the <code>VariableAccesss</code> associated with
      <code>d</code> by a call to <code>setInitVar</code>.  If none has
      been associated with <code>d</code>, returns <code>null</code>. */

  private static VariableAccess getInitVar(GenericVarDecl d) {
    return (VariableAccess)Purity.translateDecoration.get(d);
  }

  /** Associates <code>init</code> with <code>d</code>; will be
      returned by a call to <code>getInitVar</code>. */

  private static void setInitVar(GenericVarDecl d, VariableAccess init) {
    Purity.translateDecoration.set(d, init);
  }

  /** Modifies a GC designator. GC designator can include SubstExpr. */
  
  private GuardedCmd modify(VariableAccess va, int loc) {
    VariableAccess newVal = temporary(va.id.toString(), loc, loc);
    return GC.gets(va, newVal);
  }
  
  private GuardedCmd modify(Expr e, Hashtable pt, int loc) {
    VariableAccess newVal = temporary("after@" + UniqName.locToSuffix(loc),
				      e.getStartLoc(), e.getStartLoc());

    int etag = e.getTag();
    if (etag == TagConstants.VARIABLEACCESS) {
      // "e" is a single variable
      return GC.gets( (VariableAccess)e, newVal );
    }

    Assert.notFalse(etag == TagConstants.SELECT);
    NaryExpr nary = (NaryExpr)e;
    e = nary.exprs.elementAt(0);
    Expr index = nary.exprs.elementAt(1);
    if (pt != null) {
      index = GC.subst(Location.NULL, Location.NULL, pt, index);
    }
    etag = e.getTag();
    if (etag == TagConstants.VARIABLEACCESS) {
      // The given "e" had the form "f[index]"
      return GC.subgets((VariableAccess)e, index, newVal);
    }

    // The given "e" had the form "elems[array][index]"
    Assert.notFalse(etag == TagConstants.SELECT);
    nary = (NaryExpr)e;
    VariableAccess elems = (VariableAccess)nary.exprs.elementAt(0);
    Expr array = nary.exprs.elementAt(1);
    if (pt != null) {
      array = GC.subst(Location.NULL, Location.NULL, pt, array);
    }
    return GC.subsubgets(elems, array, index, newVal);
  }


    // the inlining depth at which to perform checking
    public int inlineCheckDepth = 0;
    // the number of levels of inlining after the level that is checked
    public int inlineDepthPastCheck = 0;
    
  /** Creates and desugars a call node, extended to allow the possibility
   of inlining a call */
  
  private Call call(RoutineDecl rd, ExprVec argsRaw, ExprVec args,
		    FindContributors scope,
		    int scall, int ecall, int locOpenParen,
		    boolean superOrSiblingConstructorCall,
		    InlineSettings inline) {

    //save the current status of checking assertions
    boolean useGlobalStatus = NoWarn.useGlobalStatus;

    // obtain the appropriate inlining flags
    inline = computeInlineSettings(rd, argsRaw, inline, scall);

    // create call
    Call call = Call.make( args, scall, ecall, inline != null);
    call.rd = rd;

    // now figure out desugared part
    
    String description;
    Spec spec;
    if (inline != null) {
	if (inline.getSpecForInline)
	    spec = GetSpec.getSpecForInline(call.rd, scope);
	else {
	    Set synTargs = predictedSynTargs;
	    if (synTargs == null)
		synTargs = new Set();
	    spec = GetSpec.getSpecForBody(call.rd, scope, synTargs, null);
	}
	description = "inlined call";
    }
    else {
	spec = GetSpec.getSpecForCall( call.rd, scope, predictedSynTargs );
	description = "call";
    }
    call.spec = spec;

    Assert.notFalse( spec.dmd.args.size() == call.args.size(),
		     "formal args: " + spec.dmd.args.size()
		     + " actualargs: " + call.args.size() );

    // now start creating code and temporaries
    code.push();         // this mark popped by "spill"
    temporaries.push();  // this mark popped by "spill"

    // create pt = { p* -> p*@L, wt*@pre -> wt*@L }
    
    Vector ptDomain = new Vector();
    for(int i=0; i<spec.dmd.args.size(); i++)
      ptDomain.addElement( spec.dmd.args.elementAt(i) );
    for(Enumeration e = spec.preVarMap.elements(); e.hasMoreElements(); )
      ptDomain.addElement( ((VariableAccess)e.nextElement()).decl );
    Hashtable pt = GetSpec.makeSubst( ptDomain.elements(),
				      UniqName.locToSuffix(call.scall) );

    /* if the dontCheckPreconditions flag is set, turn off the following 
       checks for non_null parameters and preconditions */
    if (inline != null) {
      globallyTurnOffChecks(inline.dontCheckPreconditions);
    }

    // var p*@L = e* in
    VariableAccess[] piLs = new VariableAccess[ call.args.size() ];
    for(int i=0; i<spec.dmd.args.size(); i++) {
      GenericVarDecl pi = spec.dmd.args.elementAt(i);
      piLs[i] = (VariableAccess)pt.get( pi );
      temporaries.addElement( piLs[i].decl );
      SimpleModifierPragma nonnull = GetSpec.NonNullPragma(pi);
      if (nonnull != null && !pi.id.toString().equals("this$0arg")) {
	Expr argRaw = argsRaw.elementAt(i);
	nullCheck(argRaw, call.args.elementAt(i),
		  argRaw.getStartLoc(),
		  TagConstants.CHKNONNULL, nonnull.getStartLoc());
      }
      code.addElement(GC.gets(piLs[i], call.args.elementAt(i)));
    }

    // check all preconditions
    for(int i=0; i<spec.pre.size(); i++) {
	Condition cond = spec.pre.elementAt(i);
	addCheck(locOpenParen,
		 cond.label,
		 GC.subst( call.scall, call.ecall, pt, cond.pred ),
		 cond.locPragmaDecl);
    }

    if (inline != null && Main.traceInfo > 0) {
      // add a label to say that a routine is being called
      GuardedCmd g = traceInfoLabelCmd(call.scall, call.ecall,
				       "Call", call.scall);
      code.addElement(g);
    }

    // var w*@L = w in
    for(Enumeration e = spec.preVarMap.keys(); e.hasMoreElements(); )
	{
	    GenericVarDecl w = (GenericVarDecl)e.nextElement();
	    VariableAccess wPre = (VariableAccess)spec.preVarMap.get(w);
	    VariableAccess wL = (VariableAccess)pt.get( wPre.decl );
	    Assert.notNull( wL );
	    VariableAccess wAccess = VariableAccess.make( w.id, call.scall, w );
	    
	    temporaries.addElement( wL.decl );
	    code.addElement( GC.gets( wL, wAccess ) );
	}
	
    // restore original checking of warnings
    NoWarn.useGlobalStatus = useGlobalStatus;


    if (inline != null) {
	// insert the translated body, with appropriate substitutions of
	// formals for the new names provided above
	Translate trInline = new Translate();
	trInline.inlineCheckDepth = inline.nextInlineCheckDepth;
	trInline.inlineDepthPastCheck = inline.nextInlineDepthPastCheck;

	// turn off body checks if the appropriate flag is set
	globallyTurnOffChecks(inline.dontCheckInlinedBody);

	GuardedCmd body = trInline.trBody(rd, scope, null, predictedSynTargs,
					  this, this.issueCautions);
	body = substituteGC(pt, body);
	code.addElement(body);

        // check all postconditions
	for(int i=0; i<spec.post.size(); i++) {
	    Condition cond = spec.post.elementAt(i);
	    addCheck(rd.getEndLoc(),
		     cond.label,
		     GC.subst( call.scall, call.ecall, pt, cond.pred ),
		     cond.locPragmaDecl);
	}
	if (Main.traceInfo > 1) {
	  // add a label to say that the inlined call has returned
	  GuardedCmd g = traceInfoLabelCmd(call.scall, call.ecall,
					   "CallReturn", call.scall);
	  code.addElement(g);
	}
	// restore original checking of warnings
	NoWarn.useGlobalStatus = useGlobalStatus;
    }

    else {
	// modify IndexSubst[[ D*, pt ]]
	for(int i=0; i<spec.targets.size(); i++)
	    {
		Expr target = spec.targets.elementAt(i);
		code.addElement(modify(target, pt, scall));
	    }

	// modify EC, RES, XRES
	code.addElement(modify(GC.ecvar, scall));
	code.addElement(modify(GC.resultvar, scall));
	code.addElement(modify(GC.xresultvar, scall));
						 
	// assume postconditions
	for(int i=0; i<spec.post.size(); i++) {
	  Condition cond = spec.post.elementAt(i);
	  code.addElement(GC.assumeAnnotation(cond.locPragmaDecl,
					      GC.subst(call.scall, call.ecall,
						       pt, cond.pred)));
	}
    }
    
    if( spec.dmd.throwsSet != null && spec.dmd.throwsSet.size() != 0 ) {	
	// #if (! superOrSiblingConstructorCall)
	//   assume EC == ec$return [] assume EC == ec$throw; raise
	// #else
	//   assume EC == ec$return []
	//   assume EC == ec$throw; assume !isAllocated(objectToBeConstructed, alloc); raise
	// #end
	
	code.push();
	code.addElement( GC.assume( GC.nary(TagConstants.ANYEQ, GC.ecvar, GC.ec_return )));
	
	code.push();
	if (Main.traceInfo > 0) {
	    // add a label to track whether the method invocation throws an
	    // exception
	    GuardedCmd g = traceInfoLabelCmd(scall, ecall,
					     "RoutineException", scall);
	    code.addElement(g);
	}  
	GuardedCmd cmd = GC.assume( GC.nary(TagConstants.ANYEQ, GC.ecvar, GC.ec_throw ));
	code.addElement( cmd );
	
	if (superOrSiblingConstructorCall) {
	    Expr isAllocated = GC.nary(TagConstants.ISALLOCATED,
				       GC.objectTBCvar, GC.allocvar);
	    code.addElement(GC.assume(GC.not(isAllocated)));
	}
	code.addElement( GC.raise() );
	
	code.addElement( GC.boxPopFromStackVector(code) );
    }
    
    // extract code and temporaries created, and put into call.desugared
    
    call.desugared = DynInstCmd.make(UniqName.locToSuffix(call.scall), spill());

    // all done
    return call;
  }

  /** Computes the inlining settings for a given call.  A return value of
    * <code>null</code> means "don't inline".
    **/

  private InlineSettings computeInlineSettings(/*@ non_null */ RoutineDecl rd,
					       /*@ non_null */ ExprVec argsRaw,
					       InlineSettings inline,
					       int scall) {
    // Try to use the given inline settings, but bag out if the routine
    // doesn't have a body
    if (inline != null) {
	if (rd.body == null) {
	    // if we don't have the routine's body, we can't inline it
	    // (does this ever happen? --jbs)
	    if (this.issueCautions) {
		ErrorSet.caution(scall, "Couldn't inline call because the routine's body was not found");
	    }
	    return null;
	}
	// TBW:  should there be a check for isRecursiveInvocation here?
	return new InlineSettings(inline,
				  inlineCheckDepth, inlineDepthPastCheck);
    }

    if (rd.body == null) {
      return null;
    }

    // Set the inlining bits appropriately, according to any given "helper"
    // pragma, but only if this is a non-recursive call.
    if (Helper.isHelper(rd) && !isRecursiveInvocation(rd)) {
      return new InlineSettings(false, false, true,
				inlineCheckDepth, inlineDepthPastCheck);
    }

    // Set the inlining bits appropriately, according to the
    // flag -inlineFromConstructors.
    if (Main.inlineFromConstructors && inConstructorContext &&
	!isRecursiveInvocation(rd)) {
      // inline if "rd" is an instance method in the same class as rdCurrent
      if (rd instanceof MethodDecl && !Modifiers.isStatic(rd.modifiers) &&
	  rd.parent == rdCurrent.parent) {
	// ...and the method is not overridable
	if (!FlowInsensitiveChecks.isOverridable((MethodDecl)rd)) {
	  // ...and the method is clearly being invoked on the "this" object
	  Assert.notFalse(1 <= argsRaw.size());  // it's an instance method!
	  VarInit e0 = argsRaw.elementAt(0);
	  e0 = trim(e0);
	  if (e0.getTag() == TagConstants.THISEXPR &&
	      ((ThisExpr)e0).classPrefix == null) {
	    // inline it!
	    return new InlineSettings(false, false, true,
				      inlineCheckDepth, inlineDepthPastCheck);
	  }
	}
      }
    }

    // Set the inlining bits appropriately, according to the two
    // inlining depth flags.
    // Also set the inlining depth flags for the next level appropriately.
    // We don't inline constructors because of problems with checking
    // the constructor for Object.
    if ((inlineCheckDepth > 0 || inlineDepthPastCheck > 0) &&
	rd instanceof MethodDecl && rd.body != null) {

      InlineSettings is = new InlineSettings(true, true, true,
					     inlineCheckDepth,
					     inlineDepthPastCheck);
      if (inlineCheckDepth > 1) {
	// don't check anything until we've reached the check depth
	is.nextInlineCheckDepth--;
      } else if (inlineCheckDepth == 1) {
	// check the body at the check depth
	is.dontCheckInlinedBody = false;
	is.getSpecForInline = false;
	is.nextInlineCheckDepth--;
      } else if (inlineCheckDepth == 0) {
	// check the preconditions of calls from the check depth
	is.dontCheckPreconditions = false;
	is.nextInlineCheckDepth--;
	is.nextInlineDepthPastCheck--;
      } else {
	// don't check anything lower than the check depth
	is.nextInlineDepthPastCheck--;
      }

      return is;
    }

    // don't inline
    return null;
  }

    /** If the flag is true, set all assertion checks to assumes.  Otherwise,
	make sure that regular checking of assertions is performed.  **/
    public static void globallyTurnOffChecks(boolean flag) {
	if (flag) {
	    // turn precondition checks into assumes
	    NoWarn.useGlobalStatus = true;
	    NoWarn.globalStatus = TagConstants.CHK_AS_ASSUME;
	}
	else
	    NoWarn.useGlobalStatus = false;
    }
    

    /** When a call is inlined, we need to substitute the new names given to
	its formal parameters for its original names in the inlined body. **/
    private static GuardedCmd substituteGC(Hashtable pt, GuardedCmd gc) {
	int tag = gc.getTag();
	switch (tag) {
	case TagConstants.SKIPCMD:
	case TagConstants.RAISECMD:
	    return gc;
	case TagConstants.ASSERTCMD:
	case TagConstants.ASSUMECMD:
	    {
		ExprCmd exprcmd = (ExprCmd) gc;
		return ExprCmd.make(exprcmd.cmd,
				    Substitute.doSubst(pt, exprcmd.pred),
				    exprcmd.loc);
	    }
	case TagConstants.GETSCMD:
	    {
		GetsCmd getscmd = (GetsCmd) gc;
		return GetsCmd.make((VariableAccess) 
				    Substitute.doSubst(pt, getscmd.v),
				    Substitute.doSubst(pt, getscmd.rhs));
	    }
	case TagConstants.SUBGETSCMD:
	    {
		SubGetsCmd sgetscmd = (SubGetsCmd) gc;
		return SubGetsCmd.make((VariableAccess) 
				       Substitute.doSubst(pt, sgetscmd.v),
				       Substitute.doSubst(pt, sgetscmd.rhs),
				       Substitute.doSubst(pt, sgetscmd.index));
	    }
	case TagConstants.SUBSUBGETSCMD:
	    {
		SubSubGetsCmd ssgetscmd = (SubSubGetsCmd) gc;
		return SubSubGetsCmd.make((VariableAccess)
					  Substitute.doSubst(pt, ssgetscmd.v),
					  Substitute.doSubst(pt, ssgetscmd.rhs),
					  Substitute.doSubst(pt, ssgetscmd.index1),
					  Substitute.doSubst(pt, ssgetscmd.index2));
	    }
	case TagConstants.RESTOREFROMCMD:
	    {
		RestoreFromCmd rfcmd = (RestoreFromCmd) gc;
		return RestoreFromCmd.make((VariableAccess)
					   Substitute.doSubst(pt, rfcmd.v),
					   Substitute.doSubst(pt, rfcmd.rhs));
	    }
	case TagConstants.SEQCMD:
	    {
		SeqCmd scmd = (SeqCmd) gc;
		int size = scmd.cmds.size();
		GuardedCmdVec vec = GuardedCmdVec.make(size);
		for (int i = 0; i < size; i++)
		    vec.addElement(substituteGC(pt, scmd.cmds.elementAt(i)));
		return SeqCmd.make(vec);
	    }
	case TagConstants.VARINCMD: 
	    {
		VarInCmd vicmd = (VarInCmd) gc;
		return GC.block(vicmd.v, substituteGC(pt, vicmd.g));
	    }
	case TagConstants.DYNINSTCMD: 
	    {
		DynInstCmd dc = (DynInstCmd) gc;
		return DynInstCmd.make(dc.s, substituteGC(pt, dc.g));
	    }
	case TagConstants.TRYCMD:
	case TagConstants.CHOOSECMD:
	    {
		CmdCmdCmd cccmd = (CmdCmdCmd) gc;
		return CmdCmdCmd.make(cccmd.cmd,
				      substituteGC(pt, cccmd.g1),
				      substituteGC(pt, cccmd.g2));
	    }
	case TagConstants.CALL: 
	    {
		Call call = (Call) gc;
		int size = call.args.size();
		ExprVec vec = ExprVec.make(size);
		for (int i = 0; i < size; i++)
		    vec.addElement(Substitute.doSubst(pt, 
						      call.args.elementAt(i)));
		Call res = Call.make(vec, call.scall, call.ecall, 
				     call.inlined);
		res.desugared = substituteGC(pt, call.desugared);
		res.rd = call.rd;
		res.spec = call.spec;
		return res;
	    }
	case TagConstants.LOOPCMD: 
	    {
		LoopCmd lcmd = (LoopCmd) gc;
		LoopCmd res = GC.loop(lcmd.locStart, lcmd.locEnd, lcmd.locHotspot, 
				      lcmd.oldmap,
				      lcmd.invariants, lcmd.decreases,
				      lcmd.skolemConstants, lcmd.predicates, 
				      lcmd.tempVars, lcmd.guard,
				      lcmd.body);
		res.desugared = substituteGC(pt, lcmd.desugared);
		return res;
	    }
	default:
	    //@ unreachable
	    Assert.fail("Unknown kind of guarded command encountered");
	    return null;
	}
    }


    /** Destructively change the trace labels in <code>gc</code> to insert
      * sequence numbers that are used by the ErrorMsg class in printing trace
      * labels in the correct execution order.  This method requires that
      * trace labels do not yet contain sequence numbers.
      **/

    public static void addTraceLabelSequenceNumbers(/*@ non_null */ GuardedCmd gc) {
      // order the body's trace labels by execution order
      if (Main.traceInfo > 0) {
	orderTraceLabels(gc, 0);
      }
    }


    /** Walk through the guarded command translation of a method, adding
	a unique number to its location sequence, in order to sort
	trace labels in order of execution.  This is a destructive
	operation.
    **/
    private static int orderTraceLabels(GuardedCmd gc, int count) {
	int tag = gc.getTag();
	switch (tag) {
	case TagConstants.SKIPCMD:
	case TagConstants.RAISECMD:
	case TagConstants.ASSERTCMD:
	case TagConstants.GETSCMD:
	case TagConstants.SUBGETSCMD:
	case TagConstants.SUBSUBGETSCMD:
	case TagConstants.RESTOREFROMCMD:
	    return count;
	case TagConstants.ASSUMECMD:
	    {
		Expr pred = ((ExprCmd) gc).pred;
		if (pred.getTag() == TagConstants.LABELEXPR) {
		    LabelExpr le = (LabelExpr) pred;
		    count = orderTraceLabel(le, count);
		}
		return count;
	    }
	case TagConstants.SEQCMD:
	    {
		SeqCmd scmd = (SeqCmd) gc;
		int size = scmd.cmds.size();
		for (int i = 0; i < size; i++)
		    count = orderTraceLabels(scmd.cmds.elementAt(i), count);
		return count;
	    }
	case TagConstants.VARINCMD: 
	    {
		VarInCmd vicmd = (VarInCmd) gc;
		return orderTraceLabels(vicmd.g, count);
	    }
	case TagConstants.DYNINSTCMD: 
	    {
		DynInstCmd dc = (DynInstCmd) gc;
		return orderTraceLabels(dc.g, count);
	    }
	case TagConstants.TRYCMD:
	case TagConstants.CHOOSECMD:
	    {
		CmdCmdCmd cccmd = (CmdCmdCmd) gc;
		count = orderTraceLabels(cccmd.g1, count);
		return orderTraceLabels(cccmd.g2, count);
	    }
	case TagConstants.CALL: 
	    {
		Call call = (Call) gc;
		return orderTraceLabels(call.desugared, count);
	    }
	case TagConstants.LOOPCMD: 
	    {
		LoopCmd lcmd = (LoopCmd) gc;
		return orderTraceLabels(lcmd.desugared, count);
	    }
	default:
	    //@ unreachable
	    Assert.fail("Unknown kind of guarded command encountered");
	    return -1;
	}
    }

    /** If the given label is a trace label, add the <code>count</code> number 
	to the given label expression's label name, so that trace labels will
	sort correctly.
    **/
    private static int orderTraceLabel(LabelExpr le, int count) {
	String name = le.label.toString();
	if (ErrorMsg.isTraceLabel(name)) {
	    // check that we aren't touching a label that has already had a
	    // number prefixed to it
	    Assert.notFalse(name.indexOf(",") == -1);
	    int k = name.indexOf("^");
	    Assert.notFalse(k != -1);
	    name = name.substring(0, k+1) + String.valueOf(count) + "," +
		name.substring(k+1);
	    le.label = Identifier.intern(name);
	    count++;
	}
	
	return count;
    }


    /***************************************************
     *                                                 *
     * Generating temporary variables:		       *
     *                                                 *
     ***************************************************/

    /**
     ** Countains the number of temporaries generated for the method
     ** currently being translated. <p>
     **
     ** Reset to 0 on entry to trExpr. <p>
     **/
    private int tmpcount;


    /**
     ** Generate a temporary for the result of a given expression.<p>
     **
     ** This partially implements ESCJ 23b, case 6. <p>
     **/
    //@ requires e!=null
    private VariableAccess fresh(VarInit e, int loc) {
      return fresh(e, loc, null);
    }

    //@ requires e!=null
    private VariableAccess fresh(VarInit e, int loc, String suffix) {
      String name = "tmp" + tmpcount++;
      if (suffix != null) {
	name += "!" + suffix;
      }
      return temporary(name, e.getStartLoc(), loc);
    }

    /**
     ** Generate a temporary variable with prefix s and associated
     ** expression location information loc.
     **/
    private VariableAccess temporary(String s, int locAccess) {
      return temporary(s, locAccess, Location.NULL);
    }

    private VariableAccess temporary(String s, int locAccess, int locIdDecl) {
      // As per ESCJ 23b, case 6, we do not use locId:
      if (locIdDecl == Location.NULL) {
	locIdDecl = UniqName.temporaryVariable;
      }

      Identifier idn= Identifier.intern(s);
      VariableAccess result= GC.makeVar(idn, locIdDecl);
      temporaries.addElement(result.decl);
      result.loc= locAccess;

      return result;
    }
}
