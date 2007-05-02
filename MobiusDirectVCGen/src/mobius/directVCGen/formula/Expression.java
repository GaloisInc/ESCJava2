package mobius.directVCGen.formula;

import javafe.ast.FormalParaDecl;
import javafe.ast.GenericVarDecl;
import escjava.ast.TagConstants;
import escjava.sortedProver.Lifter.FnTerm;
import escjava.sortedProver.Lifter.QuantVariable;
import escjava.sortedProver.Lifter.QuantVariableRef;
import escjava.sortedProver.Lifter.Term;
import escjava.sortedProver.NodeBuilder.Sort;
import escjava.translate.UniqName;

public class Expression {
	
	/** counter to create anonymous variables */
	private static int [] varCounters = {0, 0, 0, 0, 0};
	public static String result = "\\result";


	public static QuantVariable var(String str) {
		return Formula.lf.mkQuantVariable(str, Formula.sort);
	}
	public static QuantVariable var(String name, Sort s) {
		return Formula.lf.mkQuantVariable(name, s);
	}
	public static QuantVariable var(GenericVarDecl decl) {
		return Formula.lf.mkQuantVariable(decl, UniqName.variable(decl));
	}
	
	/**
	 * Create an anonymous variable of given type.
	 * @param s the sort of the variable
	 * @return a newly created variable with a name that will not interfere
	 * with any other. It is <b>Unique</b>!
	 */
	public static QuantVariable var(Sort s) {
		String name = "#";
		if(s == Bool.sort) {
			name += "b" + varCounters[0] ;
			varCounters[0]++;
		} 
		else if(s == Ref.sort) {
			name += "r" + varCounters[1] ;
			varCounters[1]++;
		}
		else if(s == Num.sortInt) {
			name += "i" + varCounters[2] ;
			varCounters[2]++;
		}
		else if(s == Num.sortReal) {
			name += "f" + varCounters[3] ;
			varCounters[3]++;
		}
		else {
			name += "x" + varCounters[4] ;
			varCounters[4]++;
		}
		return Formula.lf.mkQuantVariable(name, s);
	}
	
	public static QuantVariableRef rvar(Sort s) {
		return refFromVar(var(s));
	}
	public static QuantVariableRef rvar(FormalParaDecl arg) {
		return refFromVar(Formula.lf.mkQuantVariable(arg, UniqName.variable(arg)));
	}
	public static QuantVariableRef rvar(String str, Sort s) {
		return refFromVar(var(str, s));
	}
	public static QuantVariableRef rvar(GenericVarDecl decl) {
		return refFromVar(var(decl));
	}	

	public static QuantVariableRef refFromVar(QuantVariable qv) {
		return Formula.lf.mkQuantVariableRef(qv);
	}
	
	
	public static Term bitor(Term l, Term r) {
		if(l.getSort() != r.getSort())
			throw new IllegalArgumentException("The sort of " + l + 
					" is different from the sort of " + r + ".");
		FnTerm t = null;
		if(l.getSort() == Bool.sort) {
			t = Formula.lf.mkFnTerm(Formula.lf.symBoolFn, new Term[] {l, r});
			t.tag = TagConstants.BITOR;
		}
		else if (l.getSort() == Num.sortInt) {
			t = Formula.lf.mkFnTerm(Formula.lf.symIntFn, new Term[] {l, r});
			t.tag = TagConstants.BITOR;
		}
		else if (l.getSort() == Num.sortReal) {
			t = Formula.lf.mkFnTerm(Formula.lf.symRealFn, new Term[] {l, r});
			t.tag = TagConstants.BITOR;
		}
		else {
			throw new IllegalArgumentException("The sort " + l.getSort() + " is invalid!"); 
		}
		return t;
	}
	public static Term bitxor(Term l, Term r) {
		if(l.getSort() != r.getSort())
			throw new IllegalArgumentException("The sort of " + l + 
					" is different from the sort of " + r + ".");
		FnTerm t = null;
		if(l.getSort() == Bool.sort) {
			t = Formula.lf.mkFnTerm(Formula.lf.symBoolFn, new Term[] {l, r});
			t.tag = TagConstants.BITXOR;
		}
		else if (l.getSort() == Num.sortInt) {
			t = Formula.lf.mkFnTerm(Formula.lf.symIntFn, new Term[] {l, r});
			t.tag = TagConstants.BITXOR;
		}
		else if (l.getSort() == Num.sortReal) {
			t = Formula.lf.mkFnTerm(Formula.lf.symRealFn, new Term[] {l, r});
			t.tag = TagConstants.BITXOR;
		}
		else {
			throw new IllegalArgumentException("The sort " + l.getSort() + " is invalid!"); 
		}
		return t;
	}
	public static Term bitand(Term l, Term r) {
		if(l.getSort() != r.getSort())
			throw new IllegalArgumentException("The sort of " + l + 
					" is different from the sort of " + r + ".");
		FnTerm t = null;
		if(l.getSort() == Bool.sort) {
			t = Formula.lf.mkFnTerm(Formula.lf.symBoolFn, new Term[] {l, r});
			t.tag = TagConstants.BITAND;
		}
		else if (l.getSort() == Num.sortInt) {
			t = Formula.lf.mkFnTerm(Formula.lf.symIntFn, new Term[] {l, r});
			t.tag = TagConstants.BITAND;
		}
		else if (l.getSort() == Num.sortReal) {
			t = Formula.lf.mkFnTerm(Formula.lf.symRealFn, new Term[] {l, r});
			t.tag = TagConstants.BITAND;
		}
		else {
			throw new IllegalArgumentException("The sort " + l.getSort() + " is invalid!"); 
		}
		return t;
	}

	
	/**
	 * @deprecated
	 */
	public static FnTerm sym(String name, Sort s) {
		return Formula.lf.symbolRef (name, s);
	}

	
}
