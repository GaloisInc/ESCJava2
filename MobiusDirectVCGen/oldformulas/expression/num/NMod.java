package mobius.directVCGen.formula.expression.num;

import java.util.Vector;

import mobius.directVCGen.formula.FormulaException;
import mobius.directVCGen.formula.IFormula;
import mobius.directVCGen.formula.IFormulaVisitor;
import mobius.directVCGen.formula.expression.APrefixExpr;
import mobius.directVCGen.formula.type.FunType;
import mobius.directVCGen.formula.type.Type;


public class NMod extends APrefixExpr{

	public final static FunType fullType =
			new FunType(Type.num, new FunType(Type.num, new FunType(Type.num)));
	
	public NMod(Vector<IFormula> args) {
		super("mod", args, fullType);
	}

	@Override
	public IFormula clone(Vector<IFormula> args) {
		return new NMod(args);
	}

	public void accept(IFormulaVisitor v) throws FormulaException {
		v.visitNMod(this);
		
	}

	public int getID() {
		return nMod;
	}

}