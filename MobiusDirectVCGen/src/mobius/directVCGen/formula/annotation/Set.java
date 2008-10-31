package mobius.directVCGen.formula.annotation;

import escjava.sortedProver.Lifter.QuantVariableRef;
import escjava.sortedProver.Lifter.Term;

//TODO: add comments
public class Set extends AAnnotation {

  /** FOL-Terms  containing variable declarations. (Each Term is just a Variable) */
  //TODO: Could maybe be Vector<SortVar> instead
  private final QuantVariableRef fDeclaration;

  /** FOL-Terms translation of JML's set statement. */
  private final Assignment fAssignment;


  /**
   * Build a new set from a variable and its assignment.
   * @param decl the variable to assign
   * @param assign the JML assignment associated to this construct
   */
  public Set(final QuantVariableRef decl, final Assignment assign) {
    fDeclaration = decl;
    fAssignment = assign;
  }
  
  /** {@inheritDoc} */
  @Override
  public int getID() {
    return annotSet;
  }


  /**
   * Inner class that represents a JML assignment (set statement).
   */
  public static class Assignment {
    /** the variable which is being assigned. */
    public final QuantVariableRef fVar;
    /** the expression which is assigned to the variable. */
    public final Term fExpr;
    


    // TODO: add comments
    public Assignment(final QuantVariableRef var, final Term expr) {
      this.fVar = var;
      this.fExpr = expr;
    }



    /** {@inheritDoc} */
    @Override
    public String toString() {
      return fVar + " := " + fExpr;
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Declare " + fDeclaration + ", Set " + fAssignment;
  }

  /**
   * Return the assignment of the set instruction.
   * @return an assignment structure
   */
  public  Assignment getAssignment() {
    return fAssignment;
  }

  /**
   * Returns the declaration, the variable name which is set.
   * @return a declaration
   */
  public QuantVariableRef getDeclaration() {
    return fDeclaration;
  }

}
