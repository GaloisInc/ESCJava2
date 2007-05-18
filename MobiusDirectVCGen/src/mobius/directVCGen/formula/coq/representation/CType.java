
package mobius.directVCGen.formula.coq.representation;

import escjava.sortedProver.NodeBuilder.SAny;
import escjava.sortedProver.NodeBuilder.STerm;

/**
 * A class to represent formulas of type Type.
 * These formulas are used to represent types in subtyping relations
 * for instance.
 * @author J. Charles
 */
public class CType extends CTerm implements SAny {
	/**
	 * Constructs a formula of type Type.
	 * @param pref if true the symbol attached to this formula is considered as prefix.
	 * @param rep the symbol attached to this node
	 * @param args the children attached to this node
	 */
	public CType(boolean pref, String rep, STerm [] args) {
		super(pref, rep, args);
	}
	
	/**
	 * Constructs a formula of type Type; where the symbol is
	 * considered as prefix.
	 * @param rep the symbol attached to this node
	 * @param args the children attached to this node
	 */
	public CType(String rep, STerm [] args) {
		super(true, rep, args);
	}
	
	/**
	 * Constructs a formula of type Type, with no child.
	 * @param rep the symbol attached to this node
	 */
	public CType(String rep) {
		super(false, rep, new STerm[0]);
	}
	
	/**
	 * Constructs a formula of type Type, with only 2 children.
	 * The symbol is considered as prefix.
	 * @param rep the symbol attached to this node
	 * @param h the first child
	 * @param loc the second child
	 */
	public CType(String rep, STerm h, STerm loc) {
		this(rep, new STerm[] {h, loc});
	}
}