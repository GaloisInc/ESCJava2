/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava.ast;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javafe.ast.*;
import javafe.util.Location;
import javafe.util.Assert;
import escjava.parser.EscPragmaParser;
import escjava.ast.TagConstants;

public class EscPrettyPrint extends DelegatingPrettyPrint {
    public EscPrettyPrint() { }

    public EscPrettyPrint(PrettyPrint self, PrettyPrint del) {
        super(self, del);
    }

    public void print(OutputStream o, LexicalPragma lp) {
        if (lp.getTag() == TagConstants.NOWARNPRAGMA) {
            write(o, "//@ ");
            write(o, TagConstants.toString(TagConstants.NOWARN));
            NowarnPragma nwp = (NowarnPragma)lp;
            for (int i = 0; i < nwp.checks.size(); i++) {
                if (i == 0) write(o, ' ');
                else write(o, ", ");
                write(o, nwp.checks.elementAt(i).toString());
            }
            write(o, "\n");
        } else writeln(o, "// Unknown LexicalPragma (tag = " + lp.getTag() + 
		" " + TagConstants.toString(lp.getTag()) + ')');
    }

    public void exsuresPrintDecl(OutputStream o, GenericVarDecl d) {
        if (d == null)
            write(o, "<null GenericVarDecl>");
        else {
            self.print(o, d.type);
            if (!(d.id.equals(TagConstants.ExsuresIdnName))) {
                write (o, ' ');
                write(o, d.id.toString());
            }
        }
    }

    public void print(OutputStream o, int ind, TypeDeclElemPragma tp) {
        int tag = tp.getTag();
        switch (tag) {
            case TagConstants.AXIOM:
            case TagConstants.INVARIANT:
            case TagConstants.REPRESENTS:
	    case TagConstants.CONSTRAINT: {
                Expr e = tag == TagConstants.REPRESENTS ?
			    ((NamedExprDeclPragma)tp).expr :
			    ((ExprDeclPragma)tp).expr;
                write(o, "/*@ "); 
                write(o, TagConstants.toString(
				tp.isRedundant() ? TagConstants.makeRedundant(tag)
						 : tag)); 
                write(o, ' ');
                self.print(o, ind, e);
                write(o, "; */");
                break;
            }

            case TagConstants.MODELDECLPRAGMA: {
                FieldDecl d = ((ModelDeclPragma)tp).decl;
                /*
                 * Below is a "//@" to prevent illegal nested /* ...  comments
                 * that otherwise might result from any attached modifier pragmas.
                 *
                 * We rely on the fact that no ESC modifier can generate newlines
                 * when pretty printed.  !!!!
                 */
                write(o, "//@ model ");
                self.print(o, ind, d, true); 
                // write(o, "  */\n");
                write(o, "\n");
                break;
            }

            case TagConstants.GHOSTDECLPRAGMA: {
                FieldDecl d = ((GhostDeclPragma)tp).decl;
                /*
                 * Below is a "//@" to prevent illegal nested /* ...  comments
                 * that otherwise might result from any attached modifier pragmas.
                 *
                 * We rely on the fact that no ESC modifier can generate newlines
                 * when pretty printed.  !!!!
                 */
                write(o, "//@ ghost ");
                self.print(o, ind, d, true); 
                // write(o, "  */\n");
                write(o, "\n");
                break;
            }
            case TagConstants.STILLDEFERREDDECLPRAGMA: {
                Identifier v = ((StillDeferredDeclPragma)tp).var;
                write(o, "/*@ ");
                write(o, TagConstants.toString(TagConstants.STILL_DEFERRED));
                write(o, " "); 
                write(o,v.toString()); 
                write(o, "; */");
                break;
            }
            default:
                write(o, "/* Unknown TypeDeclElemPragma (tag = " + TagConstants.toString(tag) + ") */");
                break;
        }
    }

    public void print(OutputStream o, int ind, ModifierPragmaVec v) {
	int n = v.size();
	for (int i=0; i<n; ++i) {
		print(o,ind,v.elementAt(i));
	}
    }

    public void print(OutputStream o, int ind, ModifierPragma mp) {
        int tag = mp.getTag();
        switch (tag) {
            case TagConstants.ALSO:
                write(o, "/*@ "); 
                write(o, TagConstants.toString(tag)); 
                write(o, " */");
                break;

	    case TagConstants.CLOSEPRAGMA:
	    case TagConstants.OPENPRAGMA:
			// FIXME - do these but they have to be part of a 
			// bigger pragma; also indenting?
                Assert.fail("Open and close pragmas are not yet handled.");
                break;

	    case TagConstants.MODEL_PROGRAM:
                write(o, "/*@ "); 
                write(o, TagConstants.toString(tag)); 
                write(o, "{...} */"); // FIXME - do all of model program
                break;

            case TagConstants.END:
            case TagConstants.ALSO_REFINE:
            case TagConstants.HELPER:
            case TagConstants.EXAMPLE:
            case TagConstants.EXCEPTIONAL_EXAMPLE:
            case TagConstants.FOR_EXAMPLE:
            case TagConstants.IMPLIES_THAT:
            case TagConstants.INSTANCE:
            case TagConstants.NORMAL_EXAMPLE:
            case TagConstants.PURE:
            case TagConstants.SPEC_PROTECTED: // SC HPT AAST 3
            case TagConstants.MONITORED:
            case TagConstants.NON_NULL:
            case TagConstants.SPEC_PUBLIC:
            case TagConstants.UNINITIALIZED:
            case TagConstants.WRITABLE_DEFERRED:
                write(o, "/*@ "); 
                write(o, TagConstants.toString(tag)); 
                write(o, " */");
                break;

            case TagConstants.GHOST:
            case TagConstants.MODEL:
		break;

	    case TagConstants.NO_WACK_FORALL:
	    case TagConstants.OLD:
                write(o, "/*@ "); 
                write(o, TagConstants.toString(tag)); 
                write(o, " */");

            case TagConstants.ALSO_ENSURES:
            case TagConstants.ALSO_REQUIRES:
            case TagConstants.ENSURES:
            case TagConstants.DIVERGES:
            case TagConstants.POSTCONDITION:
            case TagConstants.PRECONDITION:
            case TagConstants.WHEN:
            case TagConstants.MONITORED_BY: // from EscPragmaParser.continuePragma(Token)
            case TagConstants.READABLE_IF:
            case TagConstants.REQUIRES:
            case TagConstants.WRITABLE_IF: {
                Expr e = ((ExprModifierPragma)mp).expr;
                write(o, "/*@ "); 
                if (mp.isRedundant())
                    write(o, TagConstants.toString(TagConstants.makeRedundant(tag))); 
                else
                    write(o, TagConstants.toString(tag)); 
                write(o, ' ');
                self.print(o, ind, e); 
                write(o, "; */");
                break;
            }

                // All redundant tokens should not exist in the AST
                // anymore; they are represented with redundant fields in
                // the AST nodes.
            case TagConstants.ASSERT_REDUNDANTLY:
            case TagConstants.ASSIGNABLE_REDUNDANTLY:
            case TagConstants.ASSUME_REDUNDANTLY:
            case TagConstants.CONSTRAINT_REDUNDANTLY:
            case TagConstants.DECREASES_REDUNDANTLY:
            case TagConstants.DECREASING_REDUNDANTLY:
            case TagConstants.DIVERGES_REDUNDANTLY:
            case TagConstants.DURATION_REDUNDANTLY:
            case TagConstants.ENSURES_REDUNDANTLY:
            case TagConstants.EXSURES_REDUNDANTLY:
            case TagConstants.HENCE_BY_REDUNDANTLY: 
            case TagConstants.INVARIANT_REDUNDANTLY: 
            case TagConstants.LOOP_INVARIANT_REDUNDANTLY: 
            case TagConstants.MAINTAINING_REDUNDANTLY:
            case TagConstants.MEASURED_BY_REDUNDANTLY:
            case TagConstants.MODIFIABLE_REDUNDANTLY:
            case TagConstants.MODIFIES_REDUNDANTLY:
            case TagConstants.POSTCONDITION_REDUNDANTLY:
            case TagConstants.PRECONDITION_REDUNDANTLY:
            case TagConstants.REPRESENTS_REDUNDANTLY:
            case TagConstants.REQUIRES_REDUNDANTLY:
            case TagConstants.SIGNALS_REDUNDANTLY:
            case TagConstants.WHEN_REDUNDANTLY:
            case TagConstants.WORKING_SPACE_REDUNDANTLY:
                Assert.fail("Redundant keywords should not be in AST!: "
				+ TagConstants.toString(tag));
                break;

	    case TagConstants.DURATION:
	    case TagConstants.WORKING_SPACE:
            case TagConstants.ALSO_MODIFIES:
            case TagConstants.ASSIGNABLE:
            case TagConstants.MEASURED_BY:
            case TagConstants.MODIFIABLE:
            case TagConstants.MODIFIES: {
                Expr e = ((CondExprModifierPragma)mp).expr;
                Expr p = ((CondExprModifierPragma)mp).cond;
                write(o, "/*@ "); 
                if (mp.isRedundant())
                    write(o, TagConstants.toString(TagConstants.makeRedundant(tag))); 
                else
                    write(o, TagConstants.toString(tag)); 
                write(o, ' ');
                self.print(o, ind, e); 
		if (p != null) {
		    write(o, " if ");
		    self.print(o, ind, p); 
		}
                write(o, "; */");
                break;
            }
                
            case TagConstants.ALSO_EXSURES: 
            case TagConstants.EXSURES:
            case TagConstants.SIGNALS: {
                VarExprModifierPragma vemp = (VarExprModifierPragma)mp;
                write(o, "/*@ "); 
                if (vemp.isRedundant())
                    write(o, TagConstants.toString(TagConstants.makeRedundant(tag)));
                else
                    write(o, TagConstants.toString(tag));
                write(o, " ("); 
                //self.print(o, vemp.arg);
                exsuresPrintDecl(o, vemp.arg); 
                write(o, ") ");
                self.print(o, ind, vemp.expr); 
                write(o, "; */");
                break;
            }
                
            case TagConstants.BEHAVIOR:
            case TagConstants.EXCEPTIONAL_BEHAVIOR:
            case TagConstants.NORMAL_BEHAVIOR:
                write(o, "/*@ "); 
                write(o, TagConstants.toString(tag));
                write(o, " */");
                break;

            default:
                write(o, "/* Unknown ModifierPragma (tag = " + TagConstants.toString(tag) + ") */");
                break;
        }
    }

    public void print(OutputStream o, int ind, StmtPragma sp) {
        int tag = sp.getTag();
        switch (tag) {
            case TagConstants.UNREACHABLE:
                write(o, "/*@ "); 
                write(o, TagConstants.toString(tag)); 
                write(o, " */");
                break;

            case TagConstants.ASSERT: {
                Expr e = ((ExprStmtPragma)sp).expr;
                Expr l = ((ExprStmtPragma)sp).label;
                write(o, "/*@ "); 
                if (sp.isRedundant())
                    write(o, TagConstants.toString(TagConstants.makeRedundant(tag)));
                else
                    write(o, TagConstants.toString(tag)); 
                write(o, " ");
                self.print(o, ind, e);
                if (l != null) {
                    write(o, " : ");
                    self.print(o, ind, l);
                }
                write(o, "; */");
                break;
            }

            case TagConstants.HENCE_BY:
            case TagConstants.ASSUME:
            case TagConstants.DECREASES:
            case TagConstants.DECREASING:
            case TagConstants.MAINTAINING:
            case TagConstants.LOOP_INVARIANT:
            case TagConstants.LOOP_PREDICATE: {
                Expr e = ((ExprStmtPragma)sp).expr;
                write(o, "/*@ "); 
                if (sp.isRedundant())
                    write(o, TagConstants.toString(TagConstants.makeRedundant(tag)));
                else
                    write(o, TagConstants.toString(tag)); 
                write(o, ' ');
                self.print(o, ind, e); 
                write(o, "; */");
                break;
            }

            case TagConstants.SETSTMTPRAGMA: {
                SetStmtPragma s = (SetStmtPragma)sp;
                write(o, "/*@ ");
                write(o, TagConstants.toString(TagConstants.SET));
                write(o, " ");
                self.print(o, ind, s.target);
                write(o, " = ");
                self.print(o, ind, s.value);
                write(o, "; */");
                break;
            }

            default:
                write(o, "/* Unknown StmtPragma (tag = " + TagConstants.toString(tag) + ") */");
                break;
        }
    }

  /** Print a guarded command.  Assumes that <code>g</code> should be
    printed starting at the current position of <code>o</code>.  It
    does <em>not</em> print a new-line at the end of the statement.
    However, if the statement needs to span multiple lines (for
    example, because it has embedded statements), then these lines are
    indented by <code>ind</code> spaces. */

  public void print(OutputStream o, int ind, GuardedCmd g) {
    if (g == null) {
      writeln(o, "<null Stmt>");
      return;
    }

    int tag = g.getTag();
    switch (tag) {
    case TagConstants.SKIPCMD:
      write(o, "SKIP");
      return;

    case TagConstants.RAISECMD:
      write(o, "RAISE");
      return;

    case TagConstants.ASSERTCMD:
      write(o, "ASSERT ");
      print(o, ind, ((ExprCmd)g).pred);
      return;

    case TagConstants.ASSUMECMD:
      write(o, "ASSUME ");
      print(o, ind, ((ExprCmd)g).pred);
      return;

    case TagConstants.GETSCMD: {
      GetsCmd gc = (GetsCmd)g;
      if (escjava.Main.options().nvu)
	write(o, gc.v.decl.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(gc.v.decl));
      write(o, " = ");
      print(o, ind, gc.rhs);
      return;
    }

    case TagConstants.SUBGETSCMD: {
      SubGetsCmd sgc = (SubGetsCmd)g;
      if (escjava.Main.options().nvu)
	write(o, sgc.v.decl.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(sgc.v.decl));
      write(o, "[");
      print(o, ind, sgc.index);
      write(o, "] = ");
      print(o, ind, sgc.rhs);
      return;
    }

    case TagConstants.SUBSUBGETSCMD: {
      SubSubGetsCmd ssgc = (SubSubGetsCmd)g;
      if (escjava.Main.options().nvu)
	write(o, ssgc.v.decl.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(ssgc.v.decl));
      write(o, "[");
      print(o, ind, ssgc.index1);
      write(o, "][");
      print(o, ind, ssgc.index2);
      write(o, "] = ");
      print(o, ind, ssgc.rhs);
      return;
    }

    case TagConstants.RESTOREFROMCMD: {
      RestoreFromCmd gc = (RestoreFromCmd)g;
      write(o, "RESTORE ");
      if (escjava.Main.options().nvu)
	write(o, gc.v.decl.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(gc.v.decl));
      write(o, " FROM ");
      print(o, ind, gc.rhs);
      return;
    }

    case TagConstants.VARINCMD: {
      VarInCmd vc = (VarInCmd)g;
      write(o, "VAR");
      printVarVec(o, vc.v);
      writeln(o, " IN");
      spaces(o, ind+INDENT);
      print(o, ind+INDENT, vc.g);
      writeln(o);
      spaces(o, ind);
      write(o, "END");
      return;
    }

    case TagConstants.DYNINSTCMD: {
      DynInstCmd dc = (DynInstCmd)g;
      writeln(o, "DynamicInstanceCmd \"" + dc.s + "\" IN");
      spaces(o, ind+INDENT);
      print(o, ind+INDENT, dc.g);
      writeln(o);
      spaces(o, ind);
      write(o, "END");
      return;
    }

    case TagConstants.SEQCMD: {
      SeqCmd sc = (SeqCmd)g;
      int len = sc.cmds.size();
      if (len == 0) write(o, "SKIP");
      else if (len == 1) print(o, ind, sc.cmds.elementAt(0));
      else {
	for (int i = 0; i < len; i++) {
	  if (i != 0) {
	    writeln(o, ";");
	    spaces(o, ind);
	  }
	  print(o, ind, sc.cmds.elementAt(i));
	}
      }
      return;
    }

    case TagConstants.CALL: {
      Call call = (Call)g;
      if (call.inlined) {
	write(o, "INLINED ");
      }
      write(o, "CALL "+ call.spec.dmd.getId());
      print(o, ind, call.args );
      if (escjava.Main.options().showCallDetails) {
	writeln(o, " {");
	spaces(o, ind+INDENT);
	printSpec(o, ind+INDENT, call.spec );
	writeln(o);
	spaces(o, ind);
	writeln(o, "DESUGARED:");
	spaces(o, ind+INDENT);
	print(o, ind+INDENT, call.desugared);
	writeln(o);
	spaces(o, ind);
	write(o, "}");
      }
      return;
    }

    case TagConstants.TRYCMD: {
      CmdCmdCmd tc = (CmdCmdCmd)g;
      write(o, "{");
      spaces(o, INDENT-1 );
      print(o, ind+INDENT, tc.g1);
    
      writeln(o);
      spaces(o, ind);
      write(o, "!");
      spaces(o, INDENT-1 );
      print(o, ind+INDENT, tc.g2);
      writeln(o);
    
      spaces(o, ind);
      write(o, "}");
      return;
    }

    case TagConstants.LOOPCMD: {
      LoopCmd lp = (LoopCmd)g;
      writeln(o, "LOOP");
      printCondVec(o, ind+INDENT, lp.invariants,
		   TagConstants.toString(TagConstants.LOOP_INVARIANT));
      printDecrInfoVec(o, ind+INDENT, lp.decreases,
		       TagConstants.toString(TagConstants.DECREASES));

      int nextInd = ind+INDENT;
      if (lp.tempVars.size() != 0) {
	spaces(o, nextInd);
	write(o, "VAR");
	printVarVec(o, lp.tempVars);
	writeln(o, " IN");
	nextInd += INDENT;
      }

      spaces(o, nextInd);
      print(o, nextInd, lp.guard);
      // let a double-semicolon denote the break between the "guard" and "body"
      writeln(o, ";;");
      spaces(o, nextInd);
      print(o, nextInd, lp.body);
      writeln(o);

      if (lp.tempVars.size() != 0) {
	spaces(o, ind+INDENT);
	writeln(o, "END");
      }

      if (escjava.Main.options().showLoopDetails) {
	spaces(o, ind);
	writeln(o, "PREDICATES:");
	for (int i = 0; i < lp.predicates.size(); i++) {
	    spaces(o, ind+INDENT);
	    print(o, ind+INDENT, lp.predicates.elementAt(i));
	    writeln(o);
	}

	/*
	spaces(o, ind);
	writeln(o, "INVARIANTS:");
	for (int i = 1; i < lp.invariants.size(); i++) {
	    spaces(o, ind+INDENT);
	    print(o, ind+INDENT, lp.invariants.elementAt(i).pred);
	    writeln(o);
	}
	*/
	
	spaces(o, ind);
	writeln(o, "DESUGARED:");
	spaces(o, ind+INDENT);
	print(o, ind+INDENT, lp.desugared);
	writeln(o);
      }

      spaces(o, ind);
      write(o, "END");
      return;
    }

    case TagConstants.CHOOSECMD: {
      CmdCmdCmd cc = (CmdCmdCmd)g;
      write(o, "{");
      spaces(o, INDENT-1 );
      print(o, ind+INDENT, cc.g1);
    
      writeln(o);
      spaces(o, ind);
      writeln(o, "[]");
    
      spaces(o, ind+INDENT);
      print(o, ind+INDENT, cc.g2);
      writeln(o);
    
      spaces(o, ind);
      write(o, "}");
      return;
    }

    default:
      write(o, "UnknownTag<" + tag + ":");
      write(o, TagConstants.toString(tag) + ">");
      return;
    }
  }

  private void printVarVec(OutputStream o, GenericVarDeclVec vars) {
    for (int i = 0; i < vars.size(); i++) {
      GenericVarDecl vd = vars.elementAt(i);
      write(o, ' ');
      if (false) {
	// Someday we may have special variables for map types
	write(o, "Map[");
	// write(o, ((FieldDecl)vd).parent.id.toString());
	write(o, " -> ");
	print(o, vd.type);
	write(o, "]");
      } else print(o, vd.type);
      write(o, ' ');
      if (escjava.Main.options().nvu)
	write(o, vd.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(vd));
      if (i != vars.size()-1) {
	write(o, ';');
      }
    }
  }

  public void printSpec(OutputStream o, int ind, Spec spec) {
	write(o, "SPEC ");

	ModifierPragmaVec local = spec.dmd.original.pmodifiers;
	ModifierPragmaVec combined = ModifierPragmaVec.make();

	for (int i=0; i<spec.dmd.requires.size(); i++)
	    combined.addElement(spec.dmd.requires.elementAt(i));
	for (int i=0; i<spec.dmd.modifies.size(); i++)
	    combined.addElement(spec.dmd.modifies.elementAt(i));
	for (int i=0; i<spec.dmd.ensures.size(); i++)
	    combined.addElement(spec.dmd.ensures.elementAt(i));
	for (int i=0; i<spec.dmd.exsures.size(); i++)
	    combined.addElement(spec.dmd.exsures.elementAt(i));

	spec.dmd.original.pmodifiers = combined;
	print(o, ind+INDENT, spec.dmd.original,
	      spec.dmd.getContainingClass().id,
	      false);
	spec.dmd.original.pmodifiers = local;

	
	spaces(o, ind);
	write(o, "targets ");
	print(o, ind, spec.targets);
	writeln(o, ";");

	spaces(o, ind);
	write(o, "prevarmap { ");
	boolean first=true;
	for(Enumeration e = spec.preVarMap.keys(); e.hasMoreElements(); )
	  {
	    if( !first )
		write(o, ", ");
	    else
	      first = false;
	    
	    GenericVarDecl vd = (GenericVarDecl)e.nextElement();
	    VariableAccess va = (VariableAccess)spec.preVarMap.get(vd);
	    print( o, vd );
	    write(o, "->" );
	    print( o, ind, va );
	  }
	writeln(o, " };");

	printCondVec(o, ind, spec.pre, "pre");
	printCondVec(o, ind, spec.post, "post");
	return;
  }

  public void printCondVec(OutputStream o, int ind, ConditionVec cv,
			   String name) {
    for(int i=0; i<cv.size(); i++)
      {
	spaces(o, ind);
	write(o, name+" ");
	printCond(o, ind, cv.elementAt(i));
	writeln(o, ";");
      }
  }

  public void printDecrInfoVec(OutputStream o, int ind,
			       DecreasesInfoVec div, String name) {
    for (int i = 0; i < div.size(); i++) {
      spaces(o, ind);
      write(o, name+" ");
      print(o, ind, div.elementAt(i).f);
      writeln(o, ";");
    }
  }

  public void printCond(OutputStream o, int ind, Condition cond) {
    write(o, TagConstants.toString(cond.label)+": ");
    print(o, ind, cond.pred );
  }

  public void print(OutputStream o, int ind, VarInit e) {
    int tag = e.getTag();
    switch (tag) {

    case TagConstants.VARIABLEACCESS: {
      VariableAccess lva = (VariableAccess)e;
      if (escjava.Main.options().nvu)
	write(o, lva.decl.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(lva.decl));
      break;
    }

    case TagConstants.IMPLIES:
    case TagConstants.EXPLIES:
    case TagConstants.IFF:
    case TagConstants.NIFF:
    case TagConstants.SUBTYPE: {
      BinaryExpr be = (BinaryExpr)e;
      self.print(o, ind, be.left);
      write(o, ' '); write(o, TagConstants.toString(be.op)); write(o, ' ');
      self.print(o, ind, be.right);
      break;
    }

    case TagConstants.SYMBOLLIT:
      write(o, (String) (((LiteralExpr)e).value));
      break;

    case TagConstants.LABELEXPR: {
      LabelExpr le = (LabelExpr)e;
      write(o, "(");
      write(o, (le.positive ? TagConstants.toString(TagConstants.LBLPOS) :
		              TagConstants.toString(TagConstants.LBLNEG)));
      write(o, " ");
      write(o, le.label.toString());
      write(o, ' ');
      self.print(o, ind, le.expr);
      write(o, ")");
      break;
    }

    case TagConstants.LOCKSETEXPR:
      write(o, TagConstants.toString(TagConstants.LS));
      break;

    case TagConstants.ELEMSNONNULL:
    case TagConstants.ELEMTYPE:
    case TagConstants.FRESH:
    case TagConstants.WACK_NOWARN:
    case TagConstants.NOWARN_OP:
    case TagConstants.WARN:
    case TagConstants.WARN_OP:
    case TagConstants.WACK_DURATION:
    case TagConstants.WACK_WORKING_SPACE:
    case TagConstants.SPACE:
    case TagConstants.MAX:
    case TagConstants.PRE:
    case TagConstants.TYPEOF:
      write(o, TagConstants.toString(tag));
      self.print(o, ind, ((NaryExpr)e).exprs);
      break;

    case TagConstants.NOT_MODIFIED: 
      write(o, TagConstants.toString(tag));
      write(o, '(');
      self.print(o, ind, ((NotModifiedExpr)e).expr);
      write(o, ')');
      break;

    case TagConstants.DTTFSA: {
      ExprVec es = ((NaryExpr)e).exprs;
      write(o, TagConstants.toString(tag));
      write(o, '(');
      self.print(o, ((TypeExpr)es.elementAt(0)).type);
      for (int i = 1; i < es.size(); i++) {
	  write(o, ", ");
	  self.print(o, ind, es.elementAt(i));
      }
      write(o, ')');
      break;
    }

    case TagConstants.NUM_OF:{
      NumericalQuantifiedExpr qe = (NumericalQuantifiedExpr)e;
      write(o, "(");
      write(o, TagConstants.toString(tag));
      write(o, " ");
      String prefix = "";
      for( int i=0; i<qe.vars.size(); i++) {
	GenericVarDecl decl = qe.vars.elementAt(i);
	write(o, prefix );
	if (i == 0) self.print(o, decl.type);
	write(o, ' ');
	if (escjava.Main.options().nvu)
	  write(o, decl.id.toString());
	else
	  write(o, escjava.translate.UniqName.variable(decl));
	prefix = ", ";
      }
      write(o, "; ");
      self.print(o, ind, qe.expr);
      write(o, ')');
      break;
    }

    case TagConstants.SUM:
    case TagConstants.PRODUCT:
    case TagConstants.MIN:
    case TagConstants.MAXQUANT:{
      GeneralizedQuantifiedExpr qe = (GeneralizedQuantifiedExpr)e;
      write(o, "(");
      write(o, TagConstants.toString(tag));
      write(o, " ");
      String prefix = "";
      for( int i=0; i<qe.vars.size(); i++) {
	GenericVarDecl decl = qe.vars.elementAt(i);
	write(o, prefix );
	if (i == 0) self.print(o, decl.type);
	write(o, ' ');
	if (escjava.Main.options().nvu)
	  write(o, decl.id.toString());
	else
	  write(o, escjava.translate.UniqName.variable(decl));
	prefix = ", ";
      }
      write(o, "; ");
      self.print(o, ind, qe.expr);
      write(o, "; ");
      self.print(o, ind, qe.rangeExpr);
      write(o, ')');
      break;
    }
    case TagConstants.FORALL:
    case TagConstants.EXISTS: {
      QuantifiedExpr qe = (QuantifiedExpr)e;
      write(o, "(");
      write(o, TagConstants.toString(tag));
      write(o, " ");
      String prefix = "";
      for( int i=0; i<qe.vars.size(); i++) {
	GenericVarDecl decl = qe.vars.elementAt(i);
	write(o, prefix );
	if (i == 0) self.print(o, decl.type);
	write(o, ' ');
	if (escjava.Main.options().nvu)
	  write(o, decl.id.toString());
	else
	  write(o, escjava.translate.UniqName.variable(decl));
	prefix = ", ";
      }
      write(o, "; ");
      self.print(o, ind, qe.expr);
      write(o, ')');
      break;
    }

    case TagConstants.RESEXPR:
      write(o, TagConstants.toString(TagConstants.RES));
      break;

    case TagConstants.BOOLEANLIT: {
      String comment = (String)EscPragmaParser.informalPredicateDecoration.get(e);
      if (comment != null) {
	LiteralExpr le = (LiteralExpr)e;
	Boolean b = (Boolean)le.value;
	Assert.notFalse(b.booleanValue() == true);

	write(o, "(*");
	write(o, comment);
	write(o, "*)");
      } else {
	super.print(o, ind, e);
      }
      break;
    }

    case TagConstants.SUBSTEXPR: {
      SubstExpr subst = (SubstExpr)e;
      write(o, "[subst ");
      self.print(o, ind, subst.val);
      write(o, " for ");
      if (escjava.Main.options().nvu)
	write(o, subst.var.id.toString());
      else
	write(o, escjava.translate.UniqName.variable(subst.var));
      write(o, " in ");
      self.print(o, ind, subst.target);
      write(o, "]");
      break;
    }

    case TagConstants.TYPEEXPR:
      write(o, TagConstants.toString(TagConstants.TYPE));
      write(o, "(");
      self.print(o, ((TypeExpr)e).type);
      write(o, ")");
      break;

    case TagConstants.WILDREFEXPR: {
      WildRefExpr we = (WildRefExpr)e;
      print( o, ind, we.expr );
      write(o, "[*]");
      break;
    }

    case TagConstants.GUARDEXPR: {
      GuardExpr ge = (GuardExpr)e;
      write ( o, "(GUARD ");
      write ( o, escjava.translate.UniqName.locToSuffix(ge.locPragmaDecl) + " ");
      print( o, ind, ge.expr );
      write(o, ")");
      break;
    }

    case TagConstants.ALLOCLT:
    case TagConstants.ALLOCLE:
    case TagConstants.ANYEQ:
    case TagConstants.ANYNE:
    case TagConstants.ARRAYLENGTH:
    case TagConstants.ARRAYFRESH:
    case TagConstants.ARRAYSHAPEMORE:
    case TagConstants.ARRAYSHAPEONE:
    case TagConstants.ASELEMS:
    case TagConstants.ASFIELD:
    case TagConstants.ASLOCKSET:
    case TagConstants.BOOLAND:
    case TagConstants.BOOLEQ:
    case TagConstants.BOOLIMPLIES:
    case TagConstants.BOOLNE:
    case TagConstants.BOOLNOT:
    case TagConstants.BOOLOR:
    case TagConstants.CAST:
    case TagConstants.CLASSLITERALFUNC:
    case TagConstants.CONDITIONAL:
    case TagConstants.ECLOSEDTIME:
    case TagConstants.FCLOSEDTIME:
    case TagConstants.FLOATINGADD:
    case TagConstants.FLOATINGDIV:
    case TagConstants.FLOATINGEQ:
    case TagConstants.FLOATINGGE:
    case TagConstants.FLOATINGGT:
    case TagConstants.FLOATINGLE:
    case TagConstants.FLOATINGLT:
    case TagConstants.FLOATINGMOD:
    case TagConstants.FLOATINGMUL:
    case TagConstants.FLOATINGNE:
    case TagConstants.FLOATINGNEG:
    case TagConstants.FLOATINGSUB:
    case TagConstants.INTEGRALADD:
    case TagConstants.INTEGRALAND:
    case TagConstants.INTEGRALDIV:
    case TagConstants.INTEGRALEQ:
    case TagConstants.INTEGRALGE:
    case TagConstants.INTEGRALGT:
    case TagConstants.INTEGRALLE:
    case TagConstants.INTEGRALLT:
    case TagConstants.INTEGRALMOD:
    case TagConstants.INTEGRALMUL:
    case TagConstants.INTEGRALNE:
    case TagConstants.INTEGRALNEG:
    case TagConstants.INTEGRALNOT:
    case TagConstants.INTEGRALOR:
    case TagConstants.INTSHIFTL:
    case TagConstants.LONGSHIFTL:
    case TagConstants.INTSHIFTR:
    case TagConstants.LONGSHIFTR:
    case TagConstants.INTSHIFTRU:
    case TagConstants.LONGSHIFTRU:
    case TagConstants.INTEGRALSUB:
    case TagConstants.INTEGRALXOR:
    case TagConstants.IS:
    case TagConstants.ISALLOCATED:
    case TagConstants.ISNEWARRAY:
    case TagConstants.LOCKLE:
    case TagConstants.LOCKLT:
    case TagConstants.REFEQ:
    case TagConstants.REFNE:
    case TagConstants.SELECT:
    case TagConstants.STORE:
    case TagConstants.STRINGCAT:
    case TagConstants.TYPEEQ:
    case TagConstants.TYPENE:
    case TagConstants.TYPELE:
    case TagConstants.VALLOCTIME:
      write(o, TagConstants.toString(tag));
      self.print(o, ind, ((NaryExpr)e).exprs);
      break;

    case TagConstants.METHODCALL:
      write(o, ((NaryExpr)e).methodName.toString());
      self.print(o, ind, ((NaryExpr)e).exprs);
      break;

    case TagConstants.EVERYTHINGEXPR:
      write(o, TagConstants.toString(TagConstants.EVERYTHING));
      break;
    case TagConstants.NOTHINGEXPR:
      write(o, TagConstants.toString(TagConstants.NOTHING));
      break;
    case TagConstants.NOTSPECIFIEDEXPR:
      write(o, TagConstants.toString(TagConstants.NOT_SPECIFIED));
      break;

    default:
      Assert.notFalse(tag<=javafe.tc.TagConstants.LAST_TAG,
	"illegal attempt to pass tag #" + tag + " (" +
	TagConstants.toString(tag) + ") to javafe");
      super.print(o, ind, e);
      break;
    }
  }

  public void print(OutputStream o, Type t) {

    switch ( t.getTag()) {
    case TagConstants.ANY:
      write(o, "anytype" );
      break;

    case TagConstants.TYPECODE:
    case TagConstants.LOCKSET:
      write(o, TagConstants.toString(t.getTag()) );
      break;

    default:
      super.print( o, t );
    }
  }

    public String toString(int tag) {
	// Best version available in the back end:
	return escjava.ast.TagConstants.toString(tag);
    }
}
