package mobius.directVCGen.formula;

import escjava.sortedProver.Lifter.Term;
import escjava.sortedProver.NodeBuilder.Sort;

public class Ref {
	public static Sort sort = Formula.lf.sortRef;
	
	public static Term Null() {
		return Formula.lf.mkNullLiteral();
	}

	public static Term strValue(String string) {
		return Formula.lf.symbolRef(string);
	}

}