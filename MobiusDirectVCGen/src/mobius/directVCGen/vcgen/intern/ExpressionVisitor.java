package mobius.directVCGen.vcgen.intern;

import javafe.ast.AmbiguousMethodInvocation;
import javafe.ast.AmbiguousVariableAccess;
import javafe.ast.ArrayInit;
import javafe.ast.ArrayRefExpr;
import javafe.ast.ArrayType;
import javafe.ast.BinaryExpr;
import javafe.ast.CastExpr;
import javafe.ast.ClassLiteral;
import javafe.ast.CondExpr;
import javafe.ast.ErrorType;
import javafe.ast.Expr;
import javafe.ast.FieldAccess;
import javafe.ast.FieldDecl;
import javafe.ast.FormalParaDecl;
import javafe.ast.GenericVarDecl;
import javafe.ast.InstanceOfExpr;
import javafe.ast.JavafePrimitiveType;
import javafe.ast.LiteralExpr;
import javafe.ast.LocalVarDecl;
import javafe.ast.MethodInvocation;
import javafe.ast.NewArrayExpr;
import javafe.ast.NewInstanceExpr;
import javafe.ast.ParenExpr;
import javafe.ast.PrimitiveType;
import javafe.ast.ThisExpr;
import javafe.ast.Type;
import javafe.ast.TypeName;
import javafe.ast.UnaryExpr;
import javafe.ast.VarInit;
import javafe.ast.VariableAccess;
import javafe.util.ErrorSet;
import mobius.directVCGen.formula.Bool;
import mobius.directVCGen.formula.Formula;
import mobius.directVCGen.formula.Num;
import mobius.directVCGen.formula.Ref;
import mobius.directVCGen.vcgen.struct.Post;
import mobius.directVCGen.vcgen.struct.VCEntry;
import escjava.ast.Modifiers;
import escjava.ast.TagConstants;
import escjava.sortedProver.Lifter.Term;
import escjava.sortedProver.NodeBuilder.Sort;
import escjava.translate.UniqName;

public class ExpressionVisitor extends ABasicVisitor {
	ExpressionVCGen vcg;
	public ExpressionVisitor() {
		vcg = new ExpressionVCGen(this);
	}
	public Object visitBinaryExpr(BinaryExpr expr, Object o) {
		
		//System.out.println(TagConstants.toString(expr.op));
		VCEntry post = (VCEntry) o;
		switch(expr.op) {
			case TagConstants.EQ:
				return vcg.equals(expr, post);
				
			case TagConstants.OR:
			case TagConstants.AND:
			case TagConstants.BITOR:
			case TagConstants.BITXOR:
			case TagConstants.BITAND:
			case TagConstants.NE:

			case TagConstants.GE:
			case TagConstants.GT:
			case TagConstants.LE:
			case TagConstants.LT:
			case TagConstants.LSHIFT:
			case TagConstants.RSHIFT:
			case TagConstants.URSHIFT:
			case TagConstants.ADD:
			case TagConstants.SUB:
			case TagConstants.DIV:
			case TagConstants.MOD:
			case TagConstants.STAR:
		// jml specific operators
			case TagConstants.IMPLIES:
			case TagConstants.EXPLIES:
			case TagConstants.IFF: // equivalence (equality)
			case TagConstants.NIFF:     // discrepance (xor)
			case TagConstants.SUBTYPE:
			case TagConstants.DOTDOT:
				throw new IllegalArgumentException("Unmanaged construct :" +
						TagConstants.toString(expr.op) +" " +  expr);
			default:
				throw new IllegalArgumentException("Unknown construct :" +
						TagConstants.toString(expr.op) +" " +  expr);
		}
	}
	


	public Object visitLiteralExpr(LiteralExpr expr,  Object o) {
		VCEntry vce = (VCEntry) o;
		Post result = vce.post;
		Term term = null;
		//System.out.println(TagConstants.toString(expr.tag));
		switch (expr.tag) {
			case TagConstants.BOOLEANLIT:
				term = result.substWith(Bool.value((Boolean)expr.value));
				break;
			case TagConstants.INTLIT:
				term = result.substWith(Num.value((Integer)expr.value));
				break;
			case TagConstants.LONGLIT:
				term = result.substWith(Num.value((Long)expr.value));
				break;
			case TagConstants.BYTELIT:
				result.substWith(Num.value((Byte)expr.value));
				break;
			case TagConstants.SHORTLIT: 
				term = result.substWith(Num.value((Short)expr.value));
				break;
			case TagConstants.FLOATLIT:
				term = result.substWith(Num.value((Float)expr.value));
				break;
			case TagConstants.CHARLIT:
				result.substWith(Num.value((Character)expr.value));
				break;
			case TagConstants.DOUBLELIT:
				term = result.substWith(Num.value((Double)expr.value));
				break;
			case TagConstants.STRINGLIT:
				term = result.substWith(Ref.strValue((String)expr.value));
				break;
			case TagConstants.NULLLIT:
				term = result.substWith(Ref.Null());
				break;
			default:
				throw new IllegalArgumentException("Unknown construct :" +
						TagConstants.toString(expr.tag) +" " +  expr);
		}
		return new Post(result.var, term);
	}

	public Object visitUnaryExpr(UnaryExpr expr, Object o) {
		//System.out.println(TagConstants.toString(expr.op));
		VCEntry post = (VCEntry) o;
		switch(expr.op) {
			case TagConstants.POSTFIXINC:
				//return vcGenPostfixInc(expr, post);
			case TagConstants.INC:
				//return vcGenInc(expr, post);
			case TagConstants.POSTFIXDEC:
				//return vcGenPostfixDec(expr, post);
			case TagConstants.DEC:
				//return vcGenDec(expr, post);
				return post.post;
			case TagConstants.BITNOT:
			case TagConstants.UNARYADD:
			case TagConstants.UNARYSUB:
			case TagConstants.NOT:
			

				throw new IllegalArgumentException("Unmanaged construct :" +
						TagConstants.toString(expr.op) +" " +  expr);
			default:
				throw new IllegalArgumentException("Unknown construct :" +
						TagConstants.toString(expr.op) +" " +  expr);
		}
	}
	
	public Object visitVariableAccess(VariableAccess m, Object o) {
		VCEntry res = (VCEntry) o;
		
		Sort s = null;
		String name = UniqName.variable(m.decl);
		
		GenericVarDecl decl = m.decl;
			
		if (decl instanceof LocalVarDecl && ((LocalVarDecl)decl).source != null) {
			decl = ((LocalVarDecl)decl).source;				
			
			if (decl instanceof FieldDecl) {
				FieldDecl d = (FieldDecl)decl;
				if (Modifiers.isStatic(d.getModifiers()))
					s = Formula.getCurrentLifter().typeToSort(d.type);
				else
					s = Formula.getCurrentLifter().registerMapSort(Formula.getCurrentLifter().sortRef, Formula.getCurrentLifter().typeToSort(d.type));
				//ErrorSet.caution("VariableAccess " + name + " -> " + s);
			} else if (decl instanceof LocalVarDecl || decl instanceof FormalParaDecl) {
				GenericVarDecl g = (GenericVarDecl)decl;
				s = Formula.getCurrentLifter().typeToSort(g.type);
				//ErrorSet.caution("VariableAccess local " + name + " -> " + s);
			} else {
				ErrorSet.caution("unknown decl in VariableAccess " + m.decl.getClass());
			}
		}
//		res.post.substWith(Expression.var(name, s));
		return  res;
	}

	

	  public /*@non_null*/ Object visitVarInit(/*@non_null*/ VarInit x, Object o) {
	    return visitASTNode(x, o);
	  }

	  public /*@non_null*/ Object visitArrayInit(/*@non_null*/ ArrayInit x, Object o) {
	    return visitVarInit(x, o);
	  }

	  public /*@non_null*/ Object visitExpr(/*@non_null*/ Expr x, Object o) {
	    throw new IllegalArgumentException("Illegal expr!!!!");
	  }

	  public /*@non_null*/ Object visitThisExpr(/*@non_null*/ ThisExpr x, Object o) {
	    return visitExpr(x, o);// variable particuliere
	  }

	  public /*@non_null*/ Object visitArrayRefExpr(/*@non_null*/ ArrayRefExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitNewInstanceExpr(/*@non_null*/ NewInstanceExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitNewArrayExpr(/*@non_null*/ NewArrayExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitCondExpr(/*@non_null*/ CondExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitInstanceOfExpr(/*@non_null*/ InstanceOfExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitCastExpr(/*@non_null*/ CastExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitParenExpr(/*@non_null*/ ParenExpr x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitAmbiguousVariableAccess(/*@non_null*/ AmbiguousVariableAccess x, Object o) {
	    return visitExpr(x, o);
	  }


	  public /*@non_null*/ Object visitFieldAccess(/*@non_null*/ FieldAccess x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitAmbiguousMethodInvocation(/*@non_null*/ AmbiguousMethodInvocation x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitMethodInvocation(/*@non_null*/ MethodInvocation x, Object o) {
	    return visitExpr(x, o);
	  }

	  public /*@non_null*/ Object visitClassLiteral(/*@non_null*/ ClassLiteral x, Object o) {
	    return visitExpr(x, o);
	  }


	  public /*@non_null*/ Object visitType(/*@non_null*/ Type x, Object o) {
	    return visitASTNode(x, o);
	  }

	  public /*@non_null*/ Object visitErrorType(/*@non_null*/ ErrorType x, Object o) {
	    return visitType(x, o);
	  }

	  public /*@non_null*/ Object visitPrimitiveType(/*@non_null*/ PrimitiveType x, Object o) {
	    return visitType(x, o);
	  }

	  public /*@non_null*/ Object visitJavafePrimitiveType(/*@non_null*/ JavafePrimitiveType x, Object o) {
	    return visitPrimitiveType(x, o);
	  }

	  public /*@non_null*/ Object visitTypeName(/*@non_null*/ TypeName x, Object o) {
	    return visitType(x, o);
	  }

	  public /*@non_null*/ Object visitArrayType(/*@non_null*/ ArrayType x, Object o) {
	    return visitType(x, o);
	  }
	  
}
