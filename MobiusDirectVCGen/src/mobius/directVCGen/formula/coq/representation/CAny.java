package mobius.directVCGen.formula.coq.representation;

import escjava.sortedProver.NodeBuilder.SAny;
import escjava.sortedProver.NodeBuilder.STerm;

/**
 * This class is used to represent terms of type any.
 * Should not be used, outside subclassing.
 * 
 * @author J. Charles
 */
class CAny extends CTerm implements SAny {
	/**
	 * Creates an object of type SAny
	 * @param pref whether or not the symbol should be shown as a prefix
	 * @param rep the symbol associated to this node
	 * @param args the children of the node
	 */
	protected CAny(boolean pref, String rep, STerm [] args) {
		super(pref, rep, args);
	}
	
	/**
	 * Creates an object of type SAny, with its symbol
	 * as a prefix.
	 * @param rep the symbol
	 * @param args the argurments of the symbol
	 */
	protected CAny(String rep, STerm [] args) {
		this(true, rep, args);
	}
	
	/**
	 * Creates an object containing only the specfied symbol.
	 * @param rep the symbol to attach to the node
	 */
	protected CAny(String rep) {
		this(false, rep, new STerm[0]);
	}
}