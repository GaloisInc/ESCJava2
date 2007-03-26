package escjava.sortedProver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javafe.util.Assert;
import javafe.util.Set;
import escjava.prover.Atom;

public class SimplifyNodeBuilder extends EscNodeBuilder
{
	class Sx implements SMap, SBool, SInt, SReal, SPred
	{
		private static final int LIMIT = 100;
		public final String head;
		public final Sx[] args;
		
		public Sx(String name, Sx[] args)
		{
			this.head = name;
			this.args = args;
		}

		public boolean isSubSortOf(Sort s)
		{
			return true;
		}
		
		public int length(int limit)
		{
			if (limit < 0) return 1000;
			
			int sz = head.length() + 2;
			for (int i = 0; i < args.length; ++i)
				sz += 1 + args[i].length(limit - sz);
			
			return sz;
		}
		
		public void dump(int ind, StringBuffer sb)
		{
			String head = this.head;
			
			if (head != "") head = Atom.printableVersion(head);
			
			for (int i = 0; i < ind; ++i)
				sb.append(" ");
			
			if (args.length == 0) {
				sb.append(head);
			} else {
				sb.append("(").append(head);
				int limit = LIMIT - ind;
				if (ind < 0 || length(limit) <= limit) {
					for (int i = 0; i < args.length; ++i) {
						sb.append(" ");
						args[i].dump(-1, sb);
					}
				} else {
					sb.append("\n");
					for (int i = 0; i < args.length; ++i) {						
						args[i].dump(ind + 1, sb);
					}					
					for (int i = 0; i < ind; ++i)
						sb.append(" ");
				}
				sb.append(")");
			}
			if (ind >= 0) sb.append("\n");
		}
	}
	
	Sx sx(String name, STerm[] args)
	{
		if (args instanceof Sx[])
			return new Sx(name, (Sx[])args);
		
		Sx[] temp = new Sx[args.length];
		for (int i = 0; i < args.length; ++i)
			temp[i] = (Sx)args[i];
		return new Sx(name, temp);		
	}	
	Sx sx(String name) { return new Sx(name, new Sx[0]); }	
	Sx sx(String name, STerm a1) { return new Sx(name, new Sx[] { (Sx)a1 }); }	
	Sx sx(String name, STerm a1, STerm a2) { return new Sx(name, new Sx[] { (Sx)a1, (Sx)a2 }); }
	
	Sx trueConst = sx("|@true|");
	
	final HashSet defpreds = new HashSet();
	
	public String dump(SPred p)
	{
		StringBuffer sb = new StringBuffer();
		
		// not really used		
		Iterator iter = defpreds.iterator();
		while (iter.hasNext()) {
			PredSymbol fn = (PredSymbol)iter.next();
			Sx[] args = new Sx[fn.argumentTypes.length];
			for (int i = 0; i < args.length; ++i)
				args[i] = sx("a" + i);
			Sx dp = sx("DEFPRED", sx(fn.name, args));
			dp.dump(0,sb);
		}
			
		((Sx)p).dump(0, sb);
		return sb.toString();
	}
	
	public SAny buildFnCall(FnSymbol fn, SAny[] args)
	{
		return sx(fn.name, args);
	}

	public SAny buildConstantRef(FnSymbol c) 
	{
		return sx(c.name);
	}

	public SAny buildQVarRef(QuantVar v)
	{
		return sx(v.name);
	}

	public SPred buildPredCall(PredSymbol fn, SAny[] args)
	{
		if (fn == symRefNE || fn == symTypeNE) 
			return sx("NEQ", args);
		
		else if (fn == symRefEQ || fn == symTypeEQ) 
			return sx("EQ", args);
		
		if (fn == symAllocLT || fn == symAllocLE || fn == symTypeLE || fn == symArrayFresh ||
				fn == symIsAllocated)
			return sx(fn.name, args);
			
		// defpreds.add(fn);
		return sx("EQ", sx(fn.name, args), trueConst);
	}

	public SPred buildImplies(SPred arg1, SPred arg2) { return sx("IMPLIES", arg1, arg2); }
	public SPred buildIff(SPred arg1, SPred arg2) { return sx("IFF", arg1, arg2); }
	public SPred buildXor(SPred arg1, SPred arg2) { return sx("NOT", sx("IFF", arg1, arg2)); }
	public SPred buildAnd(SPred[] args) { return sx("AND", args); }
	public SPred buildOr(SPred[] args) { return sx("OR", args); }
	public SPred buildNot(SPred arg) { return sx("NOT", arg); }
	
	public SPred buildLabel(boolean positive, String name, SPred pred)
	{
		return sx(positive ? "LBLPOS" : "LBLNEG", sx(name), pred);
	}

	public SValue buildITE(SPred cond, SValue then_part, SValue else_part)
	{
		// TODO: this is probably wrong
		return sx("termConditional", new STerm[] { cond, then_part, else_part });
	}

	public SPred buildForAll(QuantVar[] vars, SPred body, STerm[][] pats,
			STerm[] nopats) 
	{
		int size = 2;
		if (pats != null && pats.length == 0) pats = null;		
		if (nopats != null && nopats.length == 0) nopats = null;
		if (pats != null) size++;
		if (nopats != null) size++;
		
		STerm[] args = new Sx[size];
		int p = 0;
		args[p++] = sx("", getVars(vars));
		if (nopats != null) args[p++] = sx("NOPATS", nopats);
		if (pats != null) {
			STerm[] pp = new Sx[pats.length];
			for (int i = 0; i < pats.length; ++i)
				pp[i] = pats[i].length == 1 ? pats[i][0] : sx("MPAT", pats[i]);
			args[p++] = sx("PATS", pp);
		}
		args[p++] = body;			 
		return sx("FORALL", args);
	}

	public SPred buildExists(QuantVar[] vars, SPred body)
	{
		return sx("EXISTS", sx("", getVars(vars)), body);
	}
	
	private Sx[] getVars(QuantVar[] vars)
	{
		Sx[] vs = new Sx[vars.length];
		for (int i = 0; i < vars.length; ++i)
			vs[i] = sx(vars[i].name);
		return vs;
	}

	public SPred buildIntPred(int intPredTag, SInt arg1, SInt arg2) {
		String sym;
		
		switch (intPredTag) {
		case predGE: sym = ">="; break;
		case predLE: sym = "<="; break;
		case predGT: sym = ">"; break;
		case predLT: sym = "<"; break;
		case predEQ: sym = "EQ"; break;
		case predNE: sym = "NEQ"; break;
		default: Assert.fail(""); sym = null;
		}
		
		return sx(sym, arg1, arg2);
	}

	public SInt buildIntFun(int intFunTag, SInt arg1, SInt arg2)
	{
		String sym;
		
		switch (intFunTag) {
		case funADD: sym = "+"; break;
		case funSUB: sym = "-"; break;
		case funMUL: sym = "*"; break;
		case funDIV: sym = "integralDiv"; break;
		case funMOD: sym = "integralMod"; break;
		case funASR32: sym = "intShiftAR"; break;
		case funSL32: sym = "intShiftL"; break;
		case funUSR32: sym = "intShiftUR"; break;
		case funASR64: sym = "longShiftAR"; break;
		case funSL64: sym = "longShiftL"; break;
		case funUSR64: sym = "longShiftUR"; break;
		default: Assert.fail(""); sym = null;
		}
		
		return sx(sym, arg1, arg2);
	}

	public SPred buildRealPred(int realPredTag, SReal arg1, SReal arg2) 
	{
		String sym;
		
		switch (realPredTag) {
		case predGE: sym = "floatingGE"; break;
		case predLE: sym = "floatingLE"; break;
		case predGT: sym = "floatingGT"; break;
		case predLT: sym = "floatingLT"; break;
		case predEQ: sym = "floatingEQ"; break;
		case predNE: sym = "floatingNE"; break;
		default: Assert.fail(""); sym = null;
		}
		
		return sx(sym, arg1, arg2);
	}

	public SReal buildRealFun(int realFunTag, SReal arg1, SReal arg2) 
	{
		String sym;
		
		switch (realFunTag) {
		case funADD: sym = "floatingADD"; break;
		case funSUB: sym = "floatingSUB"; break;
		case funMUL: sym = "floatingMUL"; break;
		case funDIV: sym = "floatingDiv"; break;
		case funMOD: sym = "floatingMod"; break;
		default: Assert.fail(""); sym = null;
		}
		
		return sx(sym, arg1, arg2);
	}

	public SInt buildIntFun(int intFunTag, SInt arg1)
	{		
		switch (intFunTag) {
		case funNEG: return sx("-", sx("0"), arg1); 
		default: Assert.fail(""); return null;
		}
	}

	public SReal buildRealFun(int realFunTag, SReal arg1)
	{
		switch (realFunTag) {
		case funNEG: return sx("floatingNEG", arg1); 
		default: Assert.fail(""); return null;
		}
	}

	public SBool buildBool(boolean b)
	{
		if (b) return trueConst;
		else return sx("bool$false");
	}

	public SInt buildInt(long n)
	{
		return sx(integralPrintName(n));
	}

	public SReal buildReal(double f) 	
	{
		return sx("F_" + f);
	}


	public SRef buildNull() 
	{
		return sx("null");
	}

	public SValue buildSelect(SMap map, SValue idx) 
	{
		return sx("select", map, idx);
	}

	public SMap buildStore(SMap map, SValue idx, SValue val)
	{
		return sx("store", new SAny[] { map, idx, val });
	}

	public SPred buildAnyEQ(SAny arg1, SAny arg2)
	{
		return sx("EQ", arg1, arg2);
	}

	public SPred buildAnyNE(SAny arg1, SAny arg2) 
	{
		return sx("NEQ", arg1, arg2);
	}

	public SValue buildValueConversion(Sort from, Sort to, SValue val) 
	{
		return val;
	}

	public SPred buildIsTrue(SBool val) 
	{
		return sx("EQ", trueConst, val);
	}
	
	public SPred buildDistinct(SAny[] terms)
	{
		return sx("DISTINCT", terms);
	}
	
	public SPred buildTrue()
	{
		return sx("TRUE");
	}

	// from VcToString
	protected static final long MaxIntegral = 1000000;
	protected static final/*@ non_null */Long minThreshold = new Long(-MaxIntegral);
	protected static final/*@ non_null */Long maxThreshold = new Long(MaxIntegral);
	//+@ invariant integralPrintNames.keyType == \type(Long);
	//+@ invariant integralPrintNames.elementType == \type(String);
	//@ spec_public
	protected static/*@  non_null */Hashtable integralPrintNames;
	
	/**
	 * * Convert an integral # into its printname according to the rules * of ESCJ
	 * 23b, part 9.
	 */
	
	protected/*@ non_null */String integralPrintName(long n) {
		if (-MaxIntegral <= n && n <= MaxIntegral) {
			return String.valueOf(n);
		}
		
		Long l = new Long(n);
		String name = (String)integralPrintNames.get(l);
		if (name != null) {
			return name;
		}
		
		if (n == -n) {
			// Need to handle minlong specially...
			name = "neg" + String.valueOf(n).substring(1);
		} else if (n < 0) {
			name = "neg" + String.valueOf(-n);
		} else {
			name = "pos" + String.valueOf(n);
		}
		
		integralPrintNames.put(l, name);
		return name;
	}
}