/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava.ast;

import javafe.ast.ASTNode;
import javafe.ast.ASTDecoration;
import javafe.ast.ModifierPragma;
import javafe.ast.ModifierPragmaVec;
import javafe.ast.GenericVarDecl;
import javafe.ast.ASTDecoration;
import javafe.ast.RoutineDecl;
import javafe.ast.MethodDecl;
import javafe.ast.ClassDecl;
import javafe.ast.TypeDecl;
import javafe.tc.TypeSig;
import javafe.ast.Type;
import javafe.ast.TypeName;
import javafe.ast.ArrayType;
import javafe.ast.PrimitiveType;
import javafe.ast.FormalParaDecl;
import javafe.ast.FormalParaDeclVec;
import javafe.ast.*;
import escjava.Main;

import javafe.util.*;
import java.util.Enumeration;


public final class Utils {

    /** Finds and returns the first modifier pragma of <code>vdecl</code>
     * that has the tag <code>tag</code>, if any.  If none, returns
     * <code>null</code>.<p>
     *
     * Note, if you want to know whether a variable is <code>non_null</code>,
     * use method <code>NonNullPragma</code> instead, for it properly
     * handles inheritance of <code>non_null</code> pragmas.
     **/


    static public ModifierPragma findModifierPragma(/*@ non_null */ GenericVarDecl vdecl,
                                                    int tag) {
	return findModifierPragma(vdecl.pmodifiers,tag);
    }

    static public ModifierPragma findModifierPragma(ModifierPragmaVec mp,
                                                    int tag) {
        if (mp != null) {
            for (int j = 0; j < mp.size(); j++) {
                ModifierPragma prag= mp.elementAt(j);
                if (prag.getTag() == tag)
                    return prag;
            }
        }
        return null;  // not present
    }

    static public void removeModifierPragma(/*@ non_null */ GenericVarDecl vdecl, int tag) {
	removeModifierPragma(vdecl.pmodifiers,tag);
    }

    static public void removeModifierPragma(ModifierPragmaVec p, int tag) {
        if (p != null) {
            for (int j = 0; j < p.size(); j++) {
                ModifierPragma prag= p.elementAt(j);
                if (prag.getTag() == tag) {
			p.removeElementAt(j);
			--j;
		}
            }
        }
    }

    static public boolean isModel(javafe.ast.FieldDecl fd) {
	return isModel(fd.pmodifiers);
    }

    static public boolean isModel(ModifierPragmaVec m) {
	if (m == null) return false;
	return findModifierPragma(m,TagConstants.MODEL) != null;
    }

	// Used for designator expressions, as in a modifies clause.
    static public boolean isModel(Expr e) {
	if (e instanceof VariableAccess) {
	    VariableAccess va = (VariableAccess)e;
	    if (va.decl instanceof FieldDecl) {
		return isModel( (FieldDecl)va.decl );
	    }
	    System.out.println("ISMODEL-VA " + va.decl.getClass());
        } else if (e instanceof FieldAccess) {
	    return isModel( ((FieldAccess)e).decl );
	} else if (e instanceof NothingExpr) {
	    return true; 
	} else if (e instanceof EverythingExpr) {
	    return false;
	} else if (e instanceof ArrayRefExpr) {
	    return isModel( ((ArrayRefExpr)e).array );
	}
	else System.out.println("ISMODEL " + e.getClass());
	return false; // default
    }

    static protected abstract class BooleanDecoration extends ASTDecoration {
	private static final Object decFALSE = new Object();
	private static final Object decTRUE = new Object();
	public BooleanDecoration(String s) {
	    super(s);
	}
	public void set(ASTNode o, boolean b) {
	    set(o, b?decTRUE:decFALSE);
	}
	public boolean isTrue(ASTNode o) {
	    Object res = get(o);
	    if (res == null) {
		boolean b = calculate(o);
		set(o,b);
		return b;
	    }
	    return res == decTRUE;
	}
	public abstract boolean calculate(ASTNode o);
    }

    static private BooleanDecoration pureDecoration = new BooleanDecoration("pure") {
        public boolean calculate(ASTNode o) {
	    RoutineDecl rd = (RoutineDecl)o;
	    if (findModifierPragma(rd.pmodifiers,TagConstants.PURE)!=null)
		return true;
	    else if (findModifierPragma(rd.parent.pmodifiers,TagConstants.PURE)
			!= null)
		return true;
	    else if (rd instanceof MethodDecl) {
		MethodDecl md = (MethodDecl)rd;
		Set direct = javafe.tc.PrepTypeDeclaration.inst.getOverrides(md.parent, md);
		Enumeration e = direct.elements();
		while (e.hasMoreElements()) {
		    MethodDecl directMD = (MethodDecl)e.nextElement();
		    if (isPure(directMD)) { 
			return true;
		    }
	        }
		return false;
	    } else {
		return false;
	    }
	}
    };
    static public boolean isPure(RoutineDecl rd) {
	return pureDecoration.isTrue(rd);
    }
    static public void setPure(RoutineDecl rd) {
	pureDecoration.set(rd,true);
    }

    private static final BooleanDecoration functionDecoration = new BooleanDecoration("function") {
	public boolean calculate(ASTNode o) {
	    RoutineDecl rd = (RoutineDecl)o;
	    if (findModifierPragma(rd.pmodifiers,TagConstants.FUNCTION)
			!= null) return true;
	    if (!isPure(rd)) return false;
	    // If non-static, the owning class must be immutable
	    if (!Modifiers.isStatic(rd.modifiers)) {
		if ( ! isImmutable(rd.parent) ) return false;
	    }
	    // All argument types must be primitive or immutable
	    FormalParaDeclVec args = rd.args;
	    for (int i=0; i<args.size(); ++i) {
		FormalParaDecl f = args.elementAt(i);
		Type t = f.type;
		if (t instanceof TypeName) t = TypeSig.getSig((TypeName)t);
		if (t instanceof PrimitiveType) continue;
		if (t instanceof ArrayType) return false;
		if (t instanceof TypeSig) {
		    if (! isImmutable(((TypeSig)t).getTypeDecl())) return false;
		}
	    }
	    return true;
	}
    };
    public static boolean isFunction(RoutineDecl rd) {
	// FIXME - try all methods being true
	if (rd instanceof MethodDecl) return true;
	return functionDecoration.isTrue(rd);
    }

    private static final BooleanDecoration immutableDecoration = new BooleanDecoration("immutable") {
	public boolean calculate(ASTNode o) {
	    return findModifierPragma(((TypeDecl)o).pmodifiers, TagConstants.PURE)
			!= null ||
		   findModifierPragma(((TypeDecl)o).pmodifiers, TagConstants.IMMUTABLE) != null;
	}
    };
    public static boolean isImmutable(TypeDecl cd) {
	return immutableDecoration.isTrue(cd);
    }
}
