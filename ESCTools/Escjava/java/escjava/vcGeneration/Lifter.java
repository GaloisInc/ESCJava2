package escjava.vcGeneration;

import javafe.ast.*;
import javafe.tc.*;
import javafe.util.*;
import escjava.ast.*;
import escjava.ast.Modifiers;
import escjava.tc.GhostEnv;
import escjava.translate.*;
import escjava.vcGeneration.NodeBuilder.PredSymbol;
import escjava.vcGeneration.NodeBuilder.SAny;
import escjava.vcGeneration.NodeBuilder.SBool;
import escjava.vcGeneration.NodeBuilder.SPred;
import escjava.vcGeneration.NodeBuilder.STerm;
import escjava.vcGeneration.NodeBuilder.SValue;
import escjava.vcGeneration.NodeBuilder.Sort;
import escjava.ast.TagConstants;
import escjava.prover.Atom;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Pattern;

public class Lifter extends EscNodeBuilder
{
	final static boolean doTrace = false;
	
	private void trace(String msg)
	{
		Assert.notFalse (doTrace);
		ErrorSet.caution(msg);
	}
	
	EscNodeBuilder dumpBuilder;
	final Hashtable fnTranslations = new Hashtable();
	final ArrayList stringConstants = new ArrayList();
	final ArrayList distinctSymbols = new ArrayList();
	
	class SortVar extends Sort
	{
		private Sort ref;
		
		public SortVar()
		{
			super("sortVar", null, null, null);
		}
		
		void refSet()
		{
			if (ref == null) {
				if (dumpBuilder != null)
					ref = sortRef;
				else
					Assert.fail("ref == null");
			}
		}
		
		public Sort/*?*/ getSuperSort()
		{
			refSet();
			return ref.getSuperSort();
		}

		public Sort/*?*/ getMapFrom()
		{
			refSet();			
			return ref.getMapFrom();
		}

		public Sort/*?*/ getMapTo()
		{
			refSet();			
			return ref.getMapTo();
		}
		
		public boolean isFinalized()
		{
			if (ref == null) return false;
			if (ref instanceof SortVar)
				return ((SortVar)ref).isFinalized();
			return true;
		}
		
		boolean occurCheck(Sort s)
		{
			if (s == this)
				return true;
				
			if (s instanceof SortVar && !((SortVar)s).isFinalized())
			{
				return false;
			}
			else if (s.getMapFrom() != null) {
				return occurCheck(s.getMapFrom()) || occurCheck(s.getMapTo());
			} else return false;
		}
		
		public void assign(Sort s)
		{
			Assert.notFalse(ref == null);
			if (doTrace)
				trace("assign: ?" + id + " <- " + s);
			if (occurCheck(s))
				ErrorSet.error("cyclic sort found");
			else
				ref = s;
		}
		
		public Sort theRealThing()
		{
			if (dumpBuilder != null)
				refSet();
			
			if (ref != null && ref instanceof SortVar)
				return ref.theRealThing();
			return ref == null ? this : ref;
		}
		
		public String toString()
		{
			if (ref == null) return "?" + id;
			else return ref.toString();
		}
	}
	
	abstract class Term
	{	
		abstract public Sort getSort();
		abstract public void infer();
		
		public void printTo(StringBuffer sb)
		{
			sb.append(super.toString());		
		}
		
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			printTo(sb);
			return sb.toString();
		}
		
		abstract public STerm dump();
		
		public SPred dumpPred()
		{	
			//ErrorSet.caution("( dumpPred");
			Assert.notFalse(follow(getSort()) == sortPred);
			SPred p = (SPred)dump();
			//ErrorSet.caution(" dumpPred )");
			return p;
		}
		
		public SAny dumpAny()
		{
			Assert.notFalse(follow(getSort()) != sortPred);
			return (SAny)dump();
		}
		
		public SValue dumpValue()
		{
			Assert.notFalse(getSort().isSubSortOf(sortValue));
			return (SValue)dump();
		}
		
		public SInt dumpInt()
		{
			Assert.notFalse(getSort().isSubSortOf(sortInt));
			return (SInt)dump();
		}
		
		public SBool dumpBool()
		{
			Assert.notFalse(getSort().isSubSortOf(sortBool));
			return (SBool)dump();
		}
		
		public SReal dumpReal()
		{
			Assert.notFalse(getSort().isSubSortOf(sortReal));
			return (SReal)dump();
		}
		
		public SRef dumpRef()
		{
			Assert.notFalse(getSort().isSubSortOf(sortRef));
			return (SRef)dump();
		}
	}
	
	class QuantVariableRef extends Term
	{
		final public QuantVariable qvar;
		
		public QuantVariableRef(QuantVariable q) { qvar = q; }		
		public Sort getSort() { return qvar.type; }
		public void infer() { }
		
		public void printTo(StringBuffer sb)
		{
			sb.append("?" + qvar.name + ":" + qvar.type);
		}
		
		public STerm dump()
		{
			return dumpBuilder.buildQVarRef(qvar.qvar); 
		}
	}
	
	class FnTerm extends Term
	{
		public FnSymbol fn;
		final public Term[] args;
		public int tag;
		final public Sort retType;
		public boolean isStringConst;
		public boolean isDistinctSymbol;
		
		public FnTerm(FnSymbol fn, Term[] args)
		{
			this.fn = fn;
			this.args = args;
			if (fn == symSelect || fn == symStore || fn == symAsField)
				retType = new SortVar();
			else
				retType = fn.retType;
		}
		
		public Sort getSort() { return retType; }
		
		void enforceArgType(int i, Sort r)
		{
			r = follow(r);
			Sort p = follow(args[i].getSort());
			
			if (isEarlySort (r, p))
				return;
			
			FnSymbol conv = null;
			
			int minpass = 2;
			if (p == sortValue)
				conv =
					r == sortInt ? symValueToInt : 
					r == sortRef ? symValueToRef : 
					r == sortBool ? symValueToBool : 
					r == sortPred ? symValueToPred : // TODO flag this with warning
					r == sortReal ? symValueToReal :
					null;
			else if (p == sortInt && r == sortReal) {
				conv = symIntToReal;
				minpass = 0;
			} else if (p == sortPred && (r == sortValue || r == sortBool)) {
				conv = symPredToBool;
				ErrorSet.caution("using pred -> bool conversion! in arg #" + (1+i) + " of " + fn + " / " + this);				
			} else if (p == sortBool && r == sortPred) {
				conv = symIsTrue;
				minpass = 1;
			}
			
			if (pass >= minpass && conv != null) {
				args[i] = new FnTerm(conv, new Term[] { args[i] });
			} else if (!require(p, r, args[i]))
				ErrorSet.error("which is arg #" + (1+i) + " of " + fn + " / " + this);
		}
		
		public void infer()
		{
			if (doTrace)
				trace("start infer " + pass + ": " + fn + " / " + this + " -> " + retType);
			
			if (args.length != fn.argumentTypes.length) {
				ErrorSet.error("wrong number of parameters to " + fn + " ---> " + this);
				return;
			}
			
			for (int i = 0; i < args.length; ++i)
				args[i].infer();			
			
			boolean skip = pass < lastPass;
			
			if (fn == symSelect)
			{
				Sort idx = new SortVar();
				Sort map = registerMapSort(idx, retType);
				
				enforceArgType(0, map);
				enforceArgType(1, idx);
			}
			else if (fn == symStore)
			{
				Sort idx = new SortVar();
				Sort val = new SortVar();
				Sort map = registerMapSort(idx, val);
				
				if (!isEarlySort(retType, map))
					unify(retType, map, this);				
				enforceArgType(0, map);
				enforceArgType(1, idx);
				enforceArgType(2, val);
			}
			else if (fn == symAnyEQ || fn == symAnyNE) {
				Sort common = new SortVar();
				
				enforceArgType(0, common);
				enforceArgType(1, common);
				
				if (follow(args[0].getSort()) == sortPred ||
					follow(args[1].getSort()) == sortPred)
				{
					if (fn == symAnyEQ)
						fn = symIff;
					else
						fn = symXor;
					skip = false;
				}
			}
			else if (fn == symAsField) {
				Sort field = registerMapSort(new SortVar(), new SortVar(), sortField);
				
				enforceArgType(0, field);
				enforceArgType(1, sortType);				
				unify(field, retType, this);				
			}
			else if (fn == symFClosedTime) {
				Sort field = registerMapSort(new SortVar(), new SortVar(), sortField);
				
				enforceArgType(0, field);				
			} else skip = false;
			
			if (!skip)
				for (int i = 0; i < args.length; ++i)
					enforceArgType(i, fn.argumentTypes[i]);					
			
			if (doTrace)
				trace("infer " + pass + ": " + fn + " / " + this + " -> " + retType);
		}
		
		public void printTo(StringBuffer sb)
		{
			sb.append(fn.name);
			
			if (args.length > 0) {
				sb.append("(");
				for (int i = 0; i < args.length; ++i) {
					args[i].printTo(sb);
					if (i != args.length - 1)
						sb.append(", ");
				}
				sb.append(")");
			}
			
			sb.append(":").append(retType);
		}
		
		public STerm dump()
		{
			boolean isPred = follow(fn.retType) == sortPred;
			FnSymbol tfn = mapFnSymbolTo(dumpBuilder, fn);
			if (tfn == null && fnTranslations.containsKey(fn))
				tfn = (FnSymbol)fnTranslations.get(fn);
				
			if (tfn != null)
				if (isPred)
					return dumpBuilder.buildPredCall((PredSymbol)tfn, dumpArray(args));
				else
					return dumpBuilder.buildFnCall(tfn, dumpArray(args));
			
			if (fn == symImplies)
				return dumpBuilder.buildImplies(args[0].dumpPred(), args[1].dumpPred());
			if (fn == symIff)
				return dumpBuilder.buildIff(args[0].dumpPred(), args[1].dumpPred());
			if (fn == symXor)
				return dumpBuilder.buildXor(args[0].dumpPred(), args[1].dumpPred());
			if (fn == symNot)
				return dumpBuilder.buildNot(args[0].dumpPred());
			if (fn.name.startsWith("%and."))
				return dumpBuilder.buildAnd(dumpPredArray(args));
			if (fn.name.startsWith("%or."))
				return dumpBuilder.buildOr(dumpPredArray(args));
			if (fn == symTermConditional)
				return dumpBuilder.buildITE(args[0].dumpPred(), args[1].dumpValue(), args[2].dumpValue());
			if (fn == symIntPred)
				return dumpBuilder.buildIntPred(tag, args[0].dumpInt(), args[1].dumpInt());
			if (fn == symIntFn)
				return dumpBuilder.buildIntFun(tag, args[0].dumpInt(), args[1].dumpInt());
			if (fn == symRealPred)
				return dumpBuilder.buildRealPred(tag, args[0].dumpReal(), args[1].dumpReal());
			if (fn == symRealFn)
				return dumpBuilder.buildRealFun(tag, args[0].dumpReal(), args[1].dumpReal());			
			if (fn == symIntegralNeg)
				return dumpBuilder.buildIntFun(funNEG, args[0].dumpInt());
			if (fn == symFloatingNeg)
				return dumpBuilder.buildRealFun(funNEG, args[0].dumpReal());
			if (fn == symSelect)
				return dumpBuilder.buildSelect((SMap)args[0].dump(), args[1].dumpValue());
			if (fn == symStore)
				return dumpBuilder.buildStore((SMap)args[0].dump(), args[1].dumpValue(), args[2].dumpValue());
			
			if (fn == symAnyEQ || fn == symAnyNE) {
				Sort t1 = args[0].getSort().theRealThing();
				Sort t2 = args[1].getSort().theRealThing();
				
				int tag = fn == symAnyEQ ? predEQ : predNE;
 
				if (t1.isSubSortOf(sortInt) && t2.isSubSortOf(sortInt))
					return dumpBuilder.buildIntPred(tag, args[0].dumpInt(), args[1].dumpInt());
				
				if (t1.isSubSortOf(sortReal) && t2.isSubSortOf(sortReal))
					return dumpBuilder.buildRealPred(tag, args[0].dumpReal(), args[1].dumpReal());
				
				if (fn == symAnyEQ)
					return dumpBuilder.buildAnyEQ(args[0].dumpAny(), args[1].dumpAny());
				else
					return dumpBuilder.buildAnyNE(args[0].dumpAny(), args[1].dumpAny());				
			}
			
			if (fn == symIsTrue)
				return dumpBuilder.buildIsTrue(args[0].dumpBool());
			
			if (fn == symValueToPred)
				return dumpBuilder.buildIsTrue(
						(SBool)dumpBuilder.buildValueConversion(
								dumpBuilder.sortValue, dumpBuilder.sortBool, 
								args[0].dumpBool()));
			
			if (fn == symPredToBool)
				return dumpBuilder.buildITE(args[0].dumpPred(),
						dumpBuilder.buildBool(true),
						dumpBuilder.buildBool(false));
			
			if (fn == symValueToBool || fn == symValueToInt || fn == symValueToReal ||
				fn == symValueToRef || fn == symIntToReal)
				return dumpBuilder.buildValueConversion(mapSortTo(dumpBuilder, fn.argumentTypes[0]),
								mapSortTo(dumpBuilder, fn.retType), args[0].dumpValue());
			
			Assert.notFalse(! fn.name.startsWith("%"));
			
			tfn = isPred ? dumpBuilder.registerPredSymbol(fn.name, mapSorts(fn.argumentTypes)) :
						   dumpBuilder.registerFnSymbol(fn.name, mapSorts(fn.argumentTypes), 
								   					mapSortTo(dumpBuilder, fn.retType));
			fnTranslations.put(fn, tfn);
			if (isStringConst) stringConstants.add(this);
			if (isDistinctSymbol) distinctSymbols.add(this);
			return dump();			
		}
	}
	
	Sort[] mapSorts(Sort[] s)
	{
		Sort[] res = new Sort[s.length];
		for (int i = 0; i < s.length; ++i)
			res[i] = mapSortTo(dumpBuilder, s[i]);
		return res;
	}
	
	SAny[] dumpArray(Term[] args)
	{
		SAny[] params = new SAny[args.length];
		for (int i = 0; i < args.length; ++i)
			params[i] = args[i].dumpAny();
		return params;
	}
	
	SPred[] dumpPredArray(Term[] args)
	{
		SPred[] params = new SPred[args.length];
		for (int i = 0; i < args.length; ++i)
			params[i] = args[i].dumpPred();
		return params;
	}
	
	Term toPred(Term body)
	{
		if (follow(body.getSort()) == sortBool)
			body = new FnTerm(symIsTrue, new Term[] { body });
		if (follow(body.getSort()) == sortValue)
			// TODO warning
			body = new FnTerm(symValueToPred, new Term[] { body });			
		unify(body.getSort(), sortPred, this);
		return body;		
	}
	
	class QuantTerm extends Term
	{
		public final boolean universal;
		public final QuantVariable[] vars;
		public final Term[][] pats;
		public final Term[] nopats;
		public Term body;
		
		public QuantTerm(boolean universal, QuantVariable[] vars, Term body, Term[][] pats, Term[] nopats)
		{
			this.universal = universal;
			this.vars = vars;
			this.body = body;
			this.pats = pats;
			this.nopats = nopats;		
		}
		
		public Sort getSort() { return sortPred; } 		
		public void infer()		
		{			
			if (doTrace)
				trace("infer start q " + pass + ": " + this);
			body.infer();
			body = toPred(body);
			if (doTrace)
				trace("infer q " + pass + ": " + this);
		}
		
		public void printTo(StringBuffer sb)
		{
			sb.append("forall [");
			for (int i = 0; i < vars.length; ++i)
				sb.append(vars[i].name + ":" + vars[i].type + /*"/" + vars[i].var.type +*/ ", ");
			sb.append("] ");
			body.printTo(sb);
		}
		
		public STerm dump()
		{
			QuantVar[] qvars = new QuantVar[vars.length];
			QuantVar[] prev = new QuantVar[vars.length];
			for (int i = 0; i < vars.length; ++i) {
				prev[i] = vars[i].qvar;
				vars[i].qvar = dumpBuilder.registerQuantifiedVariable(vars[i].name, 
											mapSortTo(dumpBuilder, vars[i].type));
				qvars[i] = vars[i].qvar;
			}
			SPred qbody = (SPred) body.dump();
			SAny[][] qpats = null;
			SAny[] qnopats = null;
			
			if (pats != null) {
				qpats = new SAny[pats.length][];
				for (int i = 0; i < pats.length; ++i)
					qpats[i] = dumpArray(pats[i]);				
			}
			
			if (nopats != null) qnopats = dumpArray(nopats);
			
			for (int i = 0; i < vars.length; ++i) {
				vars[i].qvar = prev[i];
			}
			
			if (universal)
				return dumpBuilder.buildForAll(qvars, qbody, qpats, qnopats);
			else if (qpats == null && qnopats == null)
				return dumpBuilder.buildExists(qvars, qbody);
			else
				return dumpBuilder.buildNot( 
						dumpBuilder.buildForAll(qvars,
								dumpBuilder.buildNot(qbody),
								qpats, qnopats));
		}
	}
	
	class QuantVariable
	{
		public final GenericVarDecl var;
		public final String name;
		public final Sort type;
		
		public QuantVar qvar;
		
		public QuantVariable(GenericVarDecl v, String n)
		{
			var = v;
			name = n;
			type = typeToSort(v.type);
		}
	}
	
	class LabeledTerm extends Term
	{
		public final boolean positive;
		public final String label;
		public Term body;
		
		public LabeledTerm(boolean pos, String l, Term b)
		{
			positive = pos;
			label = l;
			body = b;
		}
		
		public Sort getSort() { return body.getSort(); } 		
		public void infer() 
		{
			body.infer();
			body = toPred(body);
		}		
		
		public void printTo(StringBuffer sb)
		{
			if (!positive)
				sb.append("~");
			sb.append(label).append(": ");
			body.printTo(sb);
		}
		
		public STerm dump()
		{
			return dumpBuilder.buildLabel(positive, label, (SPred)body.dump());
		}
	}
	
	class IntLiteral extends Term
	{
		final public long value;
		public IntLiteral(long v) { value = v; }
		public Sort getSort() { return sortInt; }
		public void infer() { }
		public void printTo(StringBuffer sb) { sb.append(value); }
		public STerm dump() { return dumpBuilder.buildInt(value); }
	}
	
	class RealLiteral extends Term
	{
		final public double value;
		public RealLiteral(double v) { value = v; }
		public Sort getSort() { return sortReal; } 
		public void infer() { }
		public STerm dump() { return dumpBuilder.buildReal(value); }
	}
	
	class BoolLiteral extends Term
	{
		final public boolean value;
		public BoolLiteral(boolean v) {	value = v; }
		public Sort getSort() { return sortBool; }
		public void infer() { }
		public STerm dump() { return dumpBuilder.buildBool(value); }
	}
	
	class NullLiteral extends Term
	{
		public NullLiteral() { }
		public Sort getSort() { return sortRef; }
		public void infer() { }
		public STerm dump() { return dumpBuilder.buildNull(); }
	}
	
	public PredSymbol symImplies = registerPredSymbol("%implies", new Sort[] { sortPred, sortPred }, TagConstants.BOOLIMPLIES);
	public PredSymbol symAnd = registerPredSymbol("%and.2", new Sort[] { sortPred, sortPred });
	public PredSymbol symIff = registerPredSymbol("%iff", new Sort[] { sortPred, sortPred }, TagConstants.BOOLEQ);
	public PredSymbol symXor = registerPredSymbol("%xor", new Sort[] { sortPred, sortPred }, TagConstants.BOOLNE);
	public PredSymbol symNot = registerPredSymbol("%not", new Sort[] { sortPred }, TagConstants.BOOLNOT);
    public FnSymbol symTermConditional = registerFnSymbol("%ite", new Sort[] { sortPred, sortValue, sortValue }, sortValue, TagConstants.CONDITIONAL);
	public PredSymbol symIntPred = registerPredSymbol("%int-pred", new Sort[] { sortInt, sortInt });
	public PredSymbol symRealPred = registerPredSymbol("%real-pred", new Sort[] { sortReal, sortReal });
	public FnSymbol symIntFn = registerFnSymbol("%int-pred", new Sort[] { sortInt, sortInt }, sortInt);
	public FnSymbol symRealFn = registerFnSymbol("%real-pred", new Sort[] { sortReal, sortReal }, sortReal);
    public FnSymbol symIntegralNeg = registerFnSymbol("%integralNeg", new Sort[] { sortInt }, sortInt, TagConstants.INTEGRALNEG);
    public FnSymbol symFloatingNeg = registerFnSymbol("%floatingNeg", new Sort[] { sortReal }, sortReal, TagConstants.FLOATINGNEG);    
	public FnSymbol symSelect = registerFnSymbol("%select", new Sort[] { sortMap, sortValue }, sortValue, TagConstants.SELECT);
	public FnSymbol symStore = registerFnSymbol("%store", new Sort[] { sortMap, sortValue, sortValue }, sortMap, TagConstants.STORE);
	public PredSymbol symAnyEQ = registerPredSymbol("%anyEQ", new Sort[] { sortValue, sortValue }, TagConstants.ANYEQ);
	public PredSymbol symAnyNE = registerPredSymbol("%anyNE", new Sort[] { sortValue, sortValue }, TagConstants.ANYNE);
	public PredSymbol symIsTrue = registerPredSymbol("%isTrue", new Sort[] { sortBool });
	
    public FnSymbol symValueToRef = registerFnSymbol("%valueToRef", new Sort[] { sortValue }, sortRef);
    public FnSymbol symValueToInt = registerFnSymbol("%valueToInt", new Sort[] { sortValue }, sortInt);
    public FnSymbol symValueToBool = registerFnSymbol("%valueToBool", new Sort[] { sortValue }, sortBool);
    public FnSymbol symValueToReal = registerFnSymbol("%valueToReal", new Sort[] { sortValue }, sortReal);
    public FnSymbol symIntToReal = registerFnSymbol("%intToReal", new Sort[] { sortInt }, sortReal);
    
    public PredSymbol symValueToPred = registerPredSymbol("%valueToPred", new Sort[] { sortValue });
    public FnSymbol symPredToBool = registerFnSymbol("%predToBool", new Sort[] { sortPred }, sortBool);    
    
    /*
    public FnSymbol symIntShiftUR = registerFnSymbol("intShiftUR", new Sort[] { sortInt, sortInt }, sortInt, TagConstants.INTSHIFTRU);
    public FnSymbol symIntShiftL = registerFnSymbol("intShiftL", new Sort[] { sortInt, sortInt }, sortInt, TagConstants.INTSHIFTL);
    public FnSymbol symIntShiftAR = registerFnSymbol("intShiftAR", new Sort[] { sortInt, sortInt }, sortInt, TagConstants.INTSHIFTR);
    public FnSymbol symLongShiftAR = registerFnSymbol("longShiftAR", new Sort[] { sortInt, sortInt }, sortInt, TagConstants.LONGSHIFTR);
    public FnSymbol symLongShiftUR = registerFnSymbol("longShiftUR", new Sort[] { sortInt, sortInt }, sortInt, TagConstants.LONGSHIFTRU);
    public FnSymbol symLongShiftL = registerFnSymbol("longShiftL", new Sort[] { sortInt, sortInt }, sortInt, TagConstants.LONGSHIFTL);
    */
	
	// we just want Sort and the like, don't implement anything	
	static class Die extends RuntimeException { }
	public SAny buildFnCall(FnSymbol fn, SAny[] args) { throw new Die(); }
	public SAny buildConstantRef(FnSymbol c) { throw new Die(); }
	public SAny buildQVarRef(QuantVar v) { throw new Die(); }
	public SPred buildPredCall(PredSymbol fn, SAny[] args) { throw new Die(); }
	
	public SPred buildImplies(SPred arg1, SPred arg2) { throw new Die(); }
	public SPred buildIff(SPred arg1, SPred arg2) { throw new Die(); }
	public SPred buildXor(SPred arg1, SPred arg2) { throw new Die(); }
	public SPred buildAnd(SPred[] args) { throw new Die(); }
	public SPred buildOr(SPred[] args) { throw new Die(); }
	public SPred buildNot(SPred arg) { throw new Die(); }
	public SValue buildITE(SPred cond, SValue then_part, SValue else_part) { throw new Die(); }
	public SPred buildTrue() { throw new Die(); }
	public SPred buildDistinct(SAny[] terms) { throw new Die(); }
	public SPred buildLabel(boolean positive, String name, SPred pred) { throw new Die(); }
	public SPred buildForAll(QuantVar[] vars, SPred body, STerm[][] pats, STerm[] nopats) { throw new Die(); }
	public SPred buildExists(QuantVar[] vars, SPred body) { throw new Die(); }
	public SPred buildIntPred(int intPredTag, SInt arg1, SInt arg2) { throw new Die(); }
	public SInt buildIntFun(int intFunTag, SInt arg1, SInt arg2) { throw new Die(); }
	public SPred buildRealPred(int realPredTag, SReal arg1, SReal arg2) { throw new Die(); }
	public SReal buildRealFun(int realFunTag, SReal arg1, SReal arg2) { throw new Die(); }
	public SInt buildIntFun(int intFunTag, SInt arg1) { throw new Die(); }
	public SReal buildRealFun(int realFunTag, SReal arg1) { throw new Die(); }
	public SBool buildBool(boolean b) { throw new Die(); }
	public SInt buildInt(long n) { throw new Die(); }
	public SReal buildReal(double f) { throw new Die(); }
	public SRef buildNull() { throw new Die(); }
	public SValue buildSelect(SMap map, SValue idx) { throw new Die(); }
	public SMap buildStore(SMap map, SValue idx, SValue val) { throw new Die(); }
	public SPred buildAnyEQ(SAny arg1, SAny arg2) { throw new Die(); }
	public SPred buildAnyNE(SAny arg1, SAny arg2) { throw new Die(); }
	public SValue buildValueConversion(Sort from, Sort to, SValue val) { throw new Die(); }
	public SPred buildIsTrue(SBool val) { throw new Die(); }

	
	int pass;
	final int lastPass = 3;
	
	boolean isEarlySort(Sort s, Sort p)
	{
		return isEarlySort(s) || isEarlySort(p);
	}
	
	boolean isEarlySort(Sort s)
	{
		s = follow(s);
		
		if (pass == 0)
			return s == sortAny || s == sortPred || s == sortValue || s == sortRef;
		else if (pass == 1)
			return s == sortAny || s == sortPred || s == sortValue;
		else if (pass == 2)
			return s == sortAny || s == sortPred;
		else
			return false;
	}
	
	Term root;
	
	public Lifter(Expr e)
	{
		main = e;
	}
	
	public void run() 
	{		
		root = transform(main);
		
		pass = 0;
		while (pass <= lastPass) {
			root.infer();
			pass++;
		}
	}
	
	public SPred build(EscNodeBuilder builder)
	{
		if (root == null)
			run();
		
		dumpBuilder = builder;
		fnTranslations.clear();
		stringConstants.clear();
		distinctSymbols.clear();
		
		SPred res = root.dumpPred();
		
		SPred[] assumptions = new SPred[stringConstants.size() * 2 + 1];
		
		for (int i = 0; i < stringConstants.size(); ++i) {
			SRef str = ((Term)stringConstants.get(i)).dumpRef();
			assumptions[2*i] = dumpBuilder.buildAnyNE(str, dumpBuilder.buildNull());
			assumptions[2*i+1] = dumpBuilder.buildAnyEQ(
					dumpBuilder.buildFnCall(symTypeOf, new SAny[] { str }),
					symbolRef("T_java.lang.String", sortType).dumpAny());
		}
		
		SAny[] terms = new SAny[distinctSymbols.size()];
		for (int i = 0; i < terms.length; ++i)
			terms[i] = ((Term)distinctSymbols.get(i)).dumpAny();
		assumptions[stringConstants.size()*2] = 
			terms.length == 0 ? dumpBuilder.buildTrue() : dumpBuilder.buildDistinct(terms);
		
		res = dumpBuilder.buildImplies(dumpBuilder.buildAnd(assumptions), res); 
					
		dumpBuilder = null;
		return res;
	}
	
	final Stack quantifiedVars = new Stack();
	final Hashtable symbolTypes = new Hashtable();
	final Term[] emptyTerms = new Term[0];
	final Expr main;
	
	Sort follow(Sort s)
	{
		return s.theRealThing();
	}
	
	private boolean isFinalized(Sort s)
	{
		return s != null && !(s instanceof SortVar && !((SortVar)s).isFinalized());
	}
	
	// make sure s1<:s2
	private boolean require(Sort s1, Sort s2, Object where)
	{
		s1 = follow(s1);
		s2 = follow(s2);
		
		if (s1 == s2) return true;
		
		if (!isFinalized(s1))
			((SortVar)s1).assign(s2);
		else if (!isFinalized(s2))
			((SortVar)s2).assign(s1);
		else if (s1.isSubSortOf(s2))
		{}
		else if (s1.getMapFrom() != null && s2.getMapFrom() != null) {
			if (isFinalized(s2.getMapTo()) && s2.getMapTo().getMapFrom() != null) {
				return unify(sortRef, s1.getMapFrom(), where) &&
					   unify(sortRef, s2.getMapFrom(), where) &&
					   unify(sortArrayValue, s1.getMapTo(), where) &&
					   unify(sortArrayValue, s2.getMapTo(), where);
			}
			else {
				return unify(s1.getMapFrom(), s2.getMapFrom(), where) &&
					   unify(s1.getMapTo(), s2.getMapTo(), where);
			}
		}
		else {
			ErrorSet.error("the sort >" + s1 + "< is required to be subsort of >" + s2 + "< in " + where);
			return false;
		}
		
		return true;
	}
	
	private boolean unify(Sort s1, Sort s2, Object where)
	{
		return require(s1, s2, where) && require(s2, s1, where);
	}
	
	private FnTerm symbolRef(String name, Sort s)
	{
		FnSymbol fn = getFnSymbol(name, 0);
		if (s != null)
			if (!require(s, fn.retType, "symbol ref"))
				ErrorSet.error("symbol ref " + name);
		return new FnTerm(fn, emptyTerms);
	}
	
	private FnTerm symbolRef(String name)
	{
		return symbolRef(name, null);
	}
	
	private FnSymbol getFnSymbol(String name, int arity)
	{
		name = name + "." + arity;
		
		if (!symbolTypes.containsKey(name)) {			
			FnSymbol fn;
			if (arity == 0)
				fn = registerConstant(name, new SortVar());
			else {
				Sort[] args = new Sort[arity];
				for (int i = 0; i < arity; ++i)
					args[i] = new SortVar();
				fn = registerFnSymbol(name, args, new SortVar());
			}
			symbolTypes.put(name, fn);
			if (arity == 0 && name.startsWith("elems") && 
					(name.startsWith("elems<") || 
					 name.equals("elems") ||
					 name.startsWith("elems@") ||
					 name.startsWith("elems-") ||
					 name.startsWith("elems:")))
				unify(fn.retType, sortElems, "elems* hack");
			if (arity == 0 && name.startsWith("owner:") || name.equals("owner"))
				unify(fn.retType, sortOwner, "owner hack");
			return fn;
		}
		return (FnSymbol)symbolTypes.get(name);
	}
	
	private Term transform(/*@ non_null @*/ASTNode n)
	{
		//ErrorSet.caution("enter " + TagConstants.toString(n.getTag()) + " " + n);
		Term t = doTransform(n);
		//ErrorSet.caution("exit " + TagConstants.toString(n.getTag()));
		return t;
	}
	
	private Sort typeToSort(Type t)
	{
		switch (t.getTag()) {
		case TagConstants.ARRAYTYPE:
			return sortArray;
			
		case TagConstants.BOOLEANTYPE:
			return sortBool;
		
		case TagConstants.DOUBLETYPE:
		case TagConstants.FLOATTYPE:
			return sortReal;
			
		case TagConstants.BYTETYPE:
		case TagConstants.SHORTTYPE:
		case TagConstants.INTTYPE:
		case TagConstants.CHARTYPE:
		case TagConstants.LONGTYPE:
		case TagConstants.BIGINTTYPE:
			return sortInt;
			
		case TagConstants.TYPESIG:
		case TagConstants.TYPENAME:
			return sortRef;
			
		case TagConstants.ANY:
			return new SortVar();
			
		default:
			ErrorSet.caution("unknown type: " + TagConstants.toString(t.getTag()) + ":" + PrettyPrint.inst.toString(t));
			
			return new SortVar();
		}
	}
	
	private Pattern number = Pattern.compile("[0-9]+");
	
	private Term doTransform(/*@ non_null @*/ASTNode n)
	{		
		// declarations & instancations
		int nbChilds = n.childCount();
		Object o = null;
		
		// all types checked are in alphabetical order
		if (n instanceof ArrayType) {			
			ArrayType m = (ArrayType) n;			
			return new FnTerm(symArray, new Term[] { transform(m.elemType) });
			
		} else if (n instanceof LiteralExpr) {
			LiteralExpr m = (LiteralExpr) n;
			
			switch (m.getTag()) {
			case TagConstants.STRINGLIT:
				String s = "S_" + UniqName.locToSuffix(m.loc);
				FnTerm f = symbolRef(s, sortString);
				f.isStringConst = true;
				return f;
			case TagConstants.BOOLEANLIT: 
				return new BoolLiteral(((Boolean) m.value).booleanValue());
			case TagConstants.INTLIT:
			case TagConstants.CHARLIT:
				return new IntLiteral(((Integer) m.value).intValue());
			case TagConstants.LONGLIT:
				return new IntLiteral(((Long) m.value).longValue());
			case TagConstants.FLOATLIT:
				return new RealLiteral(((Float) m.value).floatValue());
			case TagConstants.DOUBLELIT:
				return new RealLiteral(((Double) m.value).doubleValue());
			case TagConstants.NULLLIT:
				return new NullLiteral();
			case TagConstants.SYMBOLLIT: {
				String v = (String)m.value;
				if (number.matcher(v).matches())
					return new IntLiteral(Long.parseLong(v));
				else {
					//ErrorSet.caution("symbol lit " + v);
					FnTerm a = symbolRef(v);
					a.isDistinctSymbol = true;
					return a;
				}
			}
			default:
				ErrorSet.fatal("Instanceof LiteralExpr, case missed :"
						+ TagConstants.toString(m.getTag()));
				return null;
			}
		}
		else if (n instanceof LabelExpr) {
			LabelExpr l = ((LabelExpr)n);			
			return new LabeledTerm(l.positive, l.label.toString(), transform(l.expr));
		}
		// name of a method
		else if (n instanceof NaryExpr) {
			NaryExpr m = (NaryExpr) n;
			
			FnSymbol fn = null;
			int tag = 0;
			int arity = m.childCount() - 1;
			
			switch (m.getTag()) {
			// hack: REFEQ is used to compare integers
			case TagConstants.REFEQ:
				fn = symAnyEQ; break;
			case TagConstants.REFNE:
				fn = symAnyNE; break;
				
			case TagConstants.BOOLAND:
			case TagConstants.BOOLANDX:
			case TagConstants.BOOLOR:
				fn = getFnSymbol(m.getTag() == TagConstants.BOOLOR ? "%or" : "%and",
								 arity);
				for (int i = 0; i < fn.argumentTypes.length; ++i)
					unify(fn.argumentTypes[i], sortPred, "and/or");
				unify(fn.retType, sortPred, "and/or");
				break;
			
			// integral comparisons
			case TagConstants.INTEGRALEQ:
				fn = symIntPred; tag = predEQ; break;
			case TagConstants.INTEGRALGE:
				fn = symIntPred; tag = predGE; break;
			case TagConstants.INTEGRALGT:
				fn = symIntPred; tag = predGT; break;
			case TagConstants.INTEGRALLE:
				fn = symIntPred; tag = predLE; break;
			case TagConstants.INTEGRALLT:
				fn = symIntPred; tag = predLT; break;
			case TagConstants.INTEGRALNE:
				fn = symIntPred; tag = predNE; break;
			
			// int functions
			case TagConstants.INTEGRALADD:
				fn = symIntFn; tag = funADD; break;
			case TagConstants.INTEGRALDIV:
				fn = symIntFn; tag = funDIV; break;
			case TagConstants.INTEGRALMOD:
				fn = symIntFn; tag = funMOD; break;
			case TagConstants.INTEGRALMUL:
				fn = symIntFn; tag = funMUL; break;
			case TagConstants.INTEGRALSUB:
				fn = symIntFn; tag = funSUB; break;
				
			case TagConstants.INTSHIFTRU:
				fn = symIntFn; tag = funUSR32; break;
			case TagConstants.INTSHIFTR:
				fn = symIntFn; tag = funASR32; break;
			case TagConstants.INTSHIFTL:
				fn = symIntFn; tag = funSL32; break;
				
			case TagConstants.LONGSHIFTRU:
				fn = symIntFn; tag = funUSR64; break;
			case TagConstants.LONGSHIFTR:
				fn = symIntFn; tag = funASR64; break;
			case TagConstants.LONGSHIFTL:
				fn = symIntFn; tag = funSL64; break;
				
			// real comparisons
			case TagConstants.FLOATINGEQ:
				fn = symRealPred; tag = predEQ; break;
			case TagConstants.FLOATINGGE:
				fn = symRealPred; tag = predGE; break;
			case TagConstants.FLOATINGGT:
				fn = symRealPred; tag = predGT; break;
			case TagConstants.FLOATINGLE:
				fn = symRealPred; tag = predLE; break;
			case TagConstants.FLOATINGLT:
				fn = symRealPred; tag = predLT; break;
			case TagConstants.FLOATINGNE:
				fn = symRealPred; tag = predNE; break;
			
			// real functions
			case TagConstants.FLOATINGADD:
				fn = symRealFn; tag = funADD; break;
			case TagConstants.FLOATINGDIV:
				fn = symRealFn; tag = funDIV; break;
			case TagConstants.FLOATINGMOD:
				fn = symRealFn; tag = funMOD; break;
			case TagConstants.FLOATINGMUL:
				fn = symRealFn; tag = funMUL; break;
			case TagConstants.FLOATINGSUB:
				fn = symRealFn; tag = funSUB; break;
			
			case TagConstants.DTTFSA: {
				LiteralExpr lit = (LiteralExpr)n.childAt(2);
				String op = (String)lit.value;
				if (arity == 1) {
					//ErrorSet.caution("Dttfsa " + op);
					return symbolRef(op);
				} else if (op.equals("identity")) {
					Assert.notFalse(arity == 3);
					Term body = transform((ASTNode)n.childAt(3));
					return body;
				} else {
					arity--;
					fn = getFnSymbol(op, arity);
					Term[] args = new Term[arity];
					for (int i = 0; i < arity; ++i)
						args[i] = transform((ASTNode)n.childAt(i+2));
					return new FnTerm(fn, args); 
				}
			}
			
			case TagConstants.SUM:
				Assert.fail("sum unhandled"); break;
				
			case TagConstants.METHODCALL:
				ASTNode sym = m.symbol;
				fn = getFnSymbol(m.methodName.toString(), arity); 
				if (sym == null) {
					ErrorSet.caution("no symbol stored in methodCall: " + m.methodName);
				} else if (sym instanceof FieldDecl) {
					Assert.notFalse(arity <= 2);
					Sort ft = typeToSort(((FieldDecl)sym).type);
					if (arity == 0) {
						Sort s = registerMapSort(sortRef, ft);
						unify(fn.retType, s, "mc");
					} else {
						unify(fn.retType, ft, "mc1");
						for (int i = 0; i < fn.argumentTypes.length; ++i)
							unify(fn.argumentTypes[i], sortRef, "mca");
					}
				} else if (sym instanceof GenericVarDecl) {
					ErrorSet.caution("gvd in methodCall: " + m.methodName);
				} else if (sym instanceof MethodDecl) {
					MethodDecl md = (MethodDecl)sym;
					int off = arity - md.args.size(); 
					Assert.notFalse(off <= 2);
					for (int i = 0; i < arity; ++i) {
						Sort s = sortRef;
						if (i >= off)
							s = typeToSort(md.args.elementAt(i - off).type);
						unify(fn.argumentTypes[i], s, "mda");
					}
					unify(fn.retType, typeToSort(md.returnType), "mdr");									
				} else if (sym instanceof ConstructorDecl) {
					ConstructorDecl md = (ConstructorDecl)sym;
					int off = arity - md.args.size(); 
					Assert.notFalse(off <= 2);
					for (int i = 0; i < arity; ++i) {
						Sort s = sortTime;
						if (i >= off)
							s = typeToSort(md.args.elementAt(i - off).type);
						unify(fn.argumentTypes[i], s, "cda");
					}
					unify(fn.retType, sortRef, "mdr");
				} else {
					ErrorSet.error("unknown symbol stored in methodcall: " + sym.getClass());
				}
				//ErrorSet.caution("MethodCall " + fn);
				break;
				
			default:
				Integer itag = new Integer(m.getTag());
				if (fnSymbolsByTag.containsKey(itag))
				{
					Object v = fnSymbolsByTag.get(itag); 
					if (v instanceof FnSymbol)
						fn = (FnSymbol)v;
					else if (v instanceof PredSymbol)
						fn = (PredSymbol)v;
					else
						Assert.fail("pff");					
				} else {
					ErrorSet.fatal("translating old gc tree, methodName not recognized "
						+ TagConstants.toString(m.getTag()) );
				}
			}
			
			Term[] args = new Term[arity];
			for (int i = 0; i < arity; ++i)
				args[i] = transform((ASTNode)n.childAt(i+1));
			FnTerm res = new FnTerm(fn, args);
			res.tag = tag;
			return res;
			
		} else if (n instanceof PrimitiveType) { // javafe/Type
			// this means this variable represent a primitive java type like
			// string, boolean or Java.lang.Object
			
			PrimitiveType m = (PrimitiveType) n;
			String s = javafe.ast.TagConstants.toString(m.getTag());
			return symbolRef(s, sortType);
			
		} else if (n instanceof QuantifiedExpr) {
			QuantifiedExpr m = (QuantifiedExpr) n;
			
			String s = TagConstants.toString(m.getTag());
			
			boolean universal = false;
			
			if (m.getTag() == TagConstants.FORALL)
				universal = true;
			else if (m.getTag() == TagConstants.EXISTS)
				universal = false;
			else
				Assert.fail("QuantifiedExpr, unhandled tag");
			
			QuantVariable[] vars = new QuantVariable[m.vars.size()];
			for (int i = 0; i < m.vars.size(); ++i) {
				GenericVarDecl v = m.vars.elementAt(i);
				vars[i] = new QuantVariable(v, UniqName.variable(v));
				quantifiedVars.push(vars[i]);
			}
			
			Term[][] pats;
			Term[] nopats;
			
			if (m.pats != null) {
				pats = new Term[1][];
				pats[0] = new Term[pats.length];
				for (int i = 0; i < pats.length; ++i)
					pats[0][i] = transform(m.pats.elementAt(i));
			} else { 
				pats = new Term[0][];
			}
			
			if (m.nopats != null) {
				nopats = new Term[m.nopats.size()];
				for (int i = 0; i < nopats.length; ++i)
					nopats[i] = transform(m.nopats.elementAt(i));
			}else {
				nopats = new Term[0];
			}
			
			Term body = transform(m.expr);
			
			if (m.rangeExpr != null) {
				Term range = transform(m.rangeExpr);
				if (range instanceof BoolLiteral && ((BoolLiteral)range).value == true)
				{}
				else {
					body = new FnTerm(universal ? symImplies : symAnd, new Term[] { range, body });
				}
			}
			
			for (int i = 0; i < m.vars.size(); ++i)
				quantifiedVars.pop();
			
			return new QuantTerm(universal, vars, body, pats, nopats);
			
		} else if (n instanceof SimpleName) {
			SimpleName m = (SimpleName) n;			
			
			// it seems that this node is under a TypeName node all the time
			// and that all the information is in the TypeName node.
			// that's why we don't create a new node here.
			
			ErrorSet.fatal("SimpleName: "+m);
			return null;
			
		} else if (n instanceof SubstExpr) {
			SubstExpr m = (SubstExpr) n;
			
			ErrorSet.fatal("SubstExpr viewed and not handled");
			return null;
			
		} else if (n instanceof TypeDecl) {						
			TypeDecl m = (TypeDecl) n;
			// this represents a type
			
			String s = new String(m.id.chars);
			
			ErrorSet.fatal("ignored TypeDecl " + s);
			return null;
			
		} else if (n instanceof TypeExpr) {
			TypeExpr m = (TypeExpr) n;
			return transform(m.type);
			
		} else if (n instanceof TypeName) { // javafe/Type
			// this represents a type			
			TypeName m = (TypeName) n;
			String s = m.name.printName();
			
			Assert.notFalse(s != null, 
					"case n instanceof TypeName, warning null reference not expected");
			
			return symbolRef(s, sortType);
			
		} else if (n instanceof TypeSig) {
			TypeSig m = (TypeSig) n;
			return symbolRef(m.getExternalName(), sortType);
			
		} else if (n instanceof VariableAccess) {
			VariableAccess m = (VariableAccess) n;
			for (int i = 0; i < quantifiedVars.size(); ++i) {
				QuantVariable q = (QuantVariable)quantifiedVars.elementAt(i);
				if (q.var == m.decl)
					return new QuantVariableRef(q);
			}
			
			Sort s = null;
			String name = UniqName.variable(m.decl);
			
			GenericVarDecl decl = m.decl;
			
			while (decl instanceof LocalVarDecl && ((LocalVarDecl)decl).source != null)
				decl = ((LocalVarDecl)decl).source;				
			
			if (decl instanceof FieldDecl) {
				FieldDecl d = (FieldDecl)decl;
				if (Modifiers.isStatic(d.getModifiers()))
					s = typeToSort(d.type);
				else
					s = registerMapSort(sortRef, typeToSort(d.type));
				//ErrorSet.caution("VariableAccess " + name + " -> " + s);
			} else if (decl instanceof LocalVarDecl || decl instanceof FormalParaDecl) {
				GenericVarDecl g = (GenericVarDecl)decl;
				s = typeToSort(g.type);
				//ErrorSet.caution("VariableAccess local " + name + " -> " + s);
			} else {
				ErrorSet.caution("unknown decl in VariableAccess " + m.decl.getClass());
			}
			
			return symbolRef (name, s);
			
		} else {
			ErrorSet.fatal("unhandled tag " + TagConstants.toString(n.getTag()));
			return null;
		}
	}	
}
