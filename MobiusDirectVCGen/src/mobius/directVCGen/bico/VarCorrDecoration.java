package mobius.directVCGen.bico;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafe.ast.RoutineDecl;
import mobius.directVCGen.formula.Decoration;
import mobius.directVCGen.formula.PositionHint;
import mobius.directVCGen.translator.LookupJavaFe;

import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;

import escjava.sortedProver.Lifter.QuantVariableRef;

/**
 * Give the correspondence between the variables of a method
 * and their bytecode equivalence.
 * @author J. Charles (julien.charles@inria.fr)
 */
public class VarCorrDecoration extends Decoration {
  
  /** the current instance initialized of the annotation decorations. */
  public static final VarCorrDecoration inst = new VarCorrDecoration();

  /**
   * Creates an instance.
   */
  public VarCorrDecoration() {
    super("variables-bytecode-map");
  }

  
  /**
   * Retrieve the variables which were previously registered 
   * using the set method.
   * 
   * @param n the routine to get the variables from
   * @return a list of variables, or null
   */
  public List<QuantVariableRef> get(final MethodGen n) {
    final List<QuantVariableRef> v = 
      (List<QuantVariableRef>) super.get(new PositionHint(n));
    return v;
  }
  
  /**
   * Retrieve the variables which were previously registered 
   * using the set method.
   * 
   * @param n the routine to get the variables from
   * @return a list of variables, or null
   */
  public List<QuantVariableRef> get(final RoutineDecl n) {
    return get(LookupJavaFe.getInst().translate(n));
  }
  
  /**
   * Adds the methods variable list to a method declaration.
   * 
   * @param n the routine to annotate
   * @param vars the variables
   * @param old the variables that can turn old.
   */
  public void set(final MethodGen n,
                  final Map<QuantVariableRef, LocalVariableGen> vars,
                  final List<QuantVariableRef> old) {
    final List<QuantVariableRef> bcvars = 
      new ArrayList<QuantVariableRef>();
    bcvars.addAll(old);
    
    for (Entry<QuantVariableRef, LocalVariableGen> entry: vars.entrySet()) {
      bcvars.add(entry.getKey());
      
    }
    
    final LinkedList<QuantVariableRef> rev = new LinkedList<QuantVariableRef>();
    for (QuantVariableRef q: bcvars) {
      rev.addFirst(q);
    }
    super.set(new PositionHint(n), rev);
  }

}
