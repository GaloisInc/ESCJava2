package mobius.directVCGen.translator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javafe.ast.BinaryExpr;
import javafe.ast.FieldAccess;
import javafe.ast.GenericVarDecl;
import javafe.ast.InstanceOfExpr;
import javafe.ast.LiteralExpr;
import javafe.ast.ThisExpr;
import javafe.ast.VariableAccess;
import mobius.directVCGen.formula.Bool;
import mobius.directVCGen.formula.Expression;
import mobius.directVCGen.formula.Heap;
import mobius.directVCGen.formula.Logic;
import mobius.directVCGen.formula.Num;
import mobius.directVCGen.formula.Ref;
import mobius.directVCGen.formula.Type;
import mobius.directVCGen.translator.struct.ContextProperties;
import escjava.ast.NaryExpr;
import escjava.ast.QuantifiedExpr;
import escjava.ast.ResExpr;
import escjava.ast.TagConstants;
import escjava.sortedProver.Lifter.QuantVariable;
import escjava.sortedProver.Lifter.QuantVariableRef;
import escjava.sortedProver.Lifter.Term;
import escjava.sortedProver.NodeBuilder.Sort;

public class JmlExprToFormula {

  private JmlVisitor fVisitor;

  public JmlExprToFormula(final JmlVisitor visitor) {
    fVisitor = visitor;
  }

  public Term and(final BinaryExpr expr, final Object o) {
    final boolean pred = ((ContextProperties) o).fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred && 
        (t1.getSort() != Logic.sort) &&
        (t2.getSort() != Logic.sort)) {
      return Bool.and(t1, t2);
    }
    else {
      return Logic.and(Logic.boolToPred(t1), Logic.boolToPred(t2));
    }
  }


  public Object or(final BinaryExpr expr, final Object o) {
    final boolean pred = ((ContextProperties) o).fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred && 
        (t1.getSort() != Logic.sort) && 
        (t2.getSort() != Logic.sort)) {
      return Bool.or(t1, t2);
    }
    else {
      return Logic.or(Logic.boolToPred(t1), Logic.boolToPred(t2));
    }
  }


  public Term add(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    return Num.add(t1, t2);
  }

  /**
   * Returns either a sortPred or sortBool equality term depending of the 
   * expection of the upper method.
   * @param expr binary expression with a left and a right expression
   * @param o the Properties object, contains information about the excepted 
   * sort of the return value
   * @return term in the excepted sort, if possible
   */
  public Object eq(final BinaryExpr expr, final Object o) {
    final boolean predOld = ((ContextProperties) o).fInspectingPred;
    //set Properties.prop:=false (equality operation wants sortBool)
    ((ContextProperties) o).fInspectingPred = false;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    ((ContextProperties) o).fInspectingPred = predOld;
    if (!predOld && 
        (t1.getSort() != Logic.sort) &&
        (t2.getSort() != Logic.sort)) {
      return Bool.equals(t1, t2);
    }
    else if (predOld && 
        (t1.getSort() != Logic.sort) && 
        (t2.getSort() != Logic.sort)) {
      return Logic.equals(t1, t2);
    }
    else {
      return Logic.fullImplies(Logic.boolToPred(t1), Logic.boolToPred(t2));
    }
  }


  /**
   * ne(t1,t2) <--> not(equal(t1,t2)).
   */
  public Object ne(final BinaryExpr expr, final Object o) {
    final boolean pred = ((ContextProperties) o).fInspectingPred;
    final Term t = (Term) eq(expr, o);

    if (!pred && (t.getSort() != Logic.sort)) {
      return Bool.not(t);
    }
    else {
      return Logic.not(Logic.boolToPred(t));
    }
  }



  public Object ge(final BinaryExpr expr, final Object o) {
    final Boolean pred = ((ContextProperties)o).fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred.booleanValue()) {
      return Bool.ge(t1, t2);
    }
    else {
      return Logic.ge(t1, t2);
    }
  }


  public Term gt(final BinaryExpr expr, final ContextProperties o) {
    final Boolean pred = o.fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred.booleanValue()) {
      return Bool.gt(t1, t2);
    }
    else {
      return Logic.gt(t1, t2);
    }
  }


  public Object le(final BinaryExpr expr, final Object o) {
    final Boolean pred = ((ContextProperties)o).fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred.booleanValue()) {
      return Bool.le(t1, t2);
    }
    else {
      return Logic.le(t1, t2);
    }
  }


  public Object lt(final BinaryExpr expr, final Object o) {
    final Boolean pred = ((ContextProperties)o).fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred.booleanValue()) {
      return Bool.lt(t1, t2);
    }
    else {
      return Logic.lt(t1, t2);
    }
  }

  public Object bitor(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Expression.bitor(t1, t2);
  }

  public Object bitxor(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Expression.bitxor(t1, t2);
  }

  public Object bitand(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Expression.bitand(t1, t2);
  }

  public Object lshift(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.lshift(t1, t2);
  }

  public Object rshift(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.rshift(t1, t2);
  }

  public Object urshift(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.urshift(t1, t2);
  }

  public Object sub(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.sub(t1, t2);
  }

  public Object div(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.div(t1, t2);
  }

  public Object mod(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.mod(t1, t2);
  }

  public Object star(final BinaryExpr expr, final Object o) {
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);
    return Num.mul(t1, t2);
  }

  public Object assign(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgmul(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgdiv(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgrem(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgadd(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgsub(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asglshift(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgrshift(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgurshif(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object asgbitand(final BinaryExpr expr, final Object o) {
    return null;
  }

  public Object implies(final BinaryExpr expr, final Object o) {
    final boolean pred = ((ContextProperties) o).fInspectingPred;
    final Term t1 = (Term)expr.left.accept(fVisitor, o);
    final Term t2 = (Term)expr.right.accept(fVisitor, o);

    if (!pred && 
        (t1.getSort() != Logic.sort) &&
        (t2.getSort() != Logic.sort)) {
      return Bool.implies(t1, t2);
    }
    else {
      return Logic.implies(Logic.boolToPred(t1), Logic.boolToPred(t2));
    }
  }

  public Object explies(final BinaryExpr expr, final Object o) {
    
    return null;
  }


  public Object iff(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object niff(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object subtype(final BinaryExpr expr, final Object o) {
    
    return null;
  }

  public Object dotdot(final BinaryExpr expr, final Object o) {
    
    return null;
  }


  public Object instanceOfExpr(final InstanceOfExpr x, final Object o) {
    final Term refTerm = (Term) x.expr.accept(this.fVisitor, o);

    final Term sortType = Type.translateToType(x.type);
    return Logic.assignCompat(Heap.var, refTerm, sortType);
  }



  
  public Term literal(final LiteralExpr x, final Object o) {

    switch (x.getTag()) {
      case javafe.ast.TagConstants.BOOLEANLIT: 
        return Bool.value((Boolean) x.value);
      case javafe.ast.TagConstants.INTLIT:
        return Num.value((Integer) x.value);
      case javafe.ast.TagConstants.LONGLIT:
        return Num.value((Long) x.value);
      case javafe.ast.TagConstants.CHARLIT:
        return Num.value((Character) x.value);
      case javafe.ast.TagConstants.FLOATLIT:
        return Num.value((Float) x.value);
      case javafe.ast.TagConstants.DOUBLELIT:
        return Num.value((Double) x.value);

      case javafe.ast.TagConstants.STRINGLIT:
        return null;
      
      case javafe.ast.TagConstants.NULLLIT:
        return Ref.nullValue();
      
      case javafe.ast.TagConstants.BYTELIT:
        return Num.value((Byte) x.value);
      
      case javafe.ast.TagConstants.SHORTLIT:
        return Num.value((Short) x.value);
      default: 
        throw new IllegalArgumentException("LiteralExpr " + x.toString() + 
                                           " has unknown type.");  
                                              // should be unreachable
    }
  }

  
  public QuantVariableRef resultLiteral(final ResExpr x, 
                                        final MethodProperties prop) {
    final QuantVariableRef qvr = prop.fResult;
    if (qvr == null) {
      throw new NullPointerException();
      // should not be called if the method returns void
    }
    return qvr;
  }

  public Object variableAccess(final VariableAccess x, final Object o) {
    final boolean oldProp = (Boolean) ((ContextProperties) o).get("old");
    final boolean predProp = ((ContextProperties)o).fInspectingPred;
    Term res = null;
    
    
    if (oldProp) { 
      res = Expression.old(x.decl); // x.decl cannot be null
    }
    else {
      res = Expression.rvar(x.decl);
    }
    if (predProp && (res.getSort() == Bool.sort)) {
      res = Logic.boolToPred(res);
    }
    return res;
  }

  public Object genericVarDecl(final GenericVarDecl x, final Object o) {
    final boolean oldProp = (Boolean) ((ContextProperties) o).get("old");
    final boolean predProp = ((ContextProperties)o).fInspectingPred;

    Term res;
    if (oldProp) {
      res = Expression.old(x);
    }
    else { 
      res = Expression.rvar(x);
    }
    if (predProp && (res.getSort() == Bool.sort)) {
      res = Logic.boolToPred(res);
    }
    return res;
  }

  @SuppressWarnings("unchecked")
  public Object fieldAccess(final FieldAccess fieldAccess, final Object o) {
    final ContextProperties prop = (ContextProperties) o;
    if (prop.fresh) { 
      final QuantVariableRef qref = Expression.rvar(fieldAccess.decl);
      final Set<Term> freshSet = (HashSet<Term>) ((ContextProperties)o).get("freshSet");
      freshSet.add(qref);
      ((ContextProperties)o).put("freshSet", freshSet);
    }
    else { 
      if (fVisitor.getDoSubsetChecking()) {
        final java.util.Set<FieldAccess> subSet = fVisitor.fGlobal.subsetCheckingSet;
        subSet.add(fieldAccess);
      }
      final boolean oldProp = (Boolean) ((ContextProperties) o).get("old");
      final Term obj = (Term) fieldAccess.od.accept(fVisitor, o);
      QuantVariable var = null;
      QuantVariableRef heap = Heap.var;
      if (oldProp) {
        heap = Heap.varPre;
      }
      var = Expression.var(fieldAccess.decl);
      final Sort type = Type.getSort(fieldAccess);
      return Heap.select(heap, obj, var, type);
    }
    return null;
  }
  
  
  /**
   * Builds up a FOL term as: \fresh(x,y) --> and(%fresh(x:ref),%(y:ref)):PRED.
   * @param x the NaryExpr with TagConstant.FRESH
   * @param o properties object holding whether fresh property is set and holds the freshSet
   * @return the generated FOL term
   */
  public Term freshExpr(final NaryExpr x, final ContextProperties o) { 
    o.fresh = true;
    fVisitor.visitGCExpr(x, o);
    o.fresh = false;
    final Set freshVars = (HashSet) o.get("freshSet");
    
    Term res = null;
    
    final Iterator iter = freshVars.iterator();
    while (iter.hasNext()) { 
      if (res == null) {
        res = doFreshTerm((QuantVariableRef)iter.next());
      }
      else { 
        res = Logic.and(res, doFreshTerm((QuantVariableRef) iter.next()));
      }
      
    }
    return res;
  }

  
  
  
  /**
   * Generates a Term of an fresh variable: (x != null) and 
   * !isAlive(Pre_Heap, x) and isAlive(Heap, x).
   * @param qref the Term to translate into a fresh FOL term
   * @return the generated FOL term
   */
  public Term doFreshTerm(final QuantVariableRef qref) {
    final Term notEqualNull = Logic.not(Logic.equalsNull(qref));
    final Term isNotAliveInPreHeap = Logic.not(Logic.isAlive(Heap.varPre, qref));
    final Term isAliveInHeap = Logic.isAlive(Heap.var, qref);
    return Logic.and(Logic.and(notEqualNull, isNotAliveInPreHeap), isAliveInHeap);
  }
  
  public Term oldExpr(final NaryExpr x, final ContextProperties o) {
    final boolean old = (Boolean) o.get("old"); 
    o.put("old", Boolean.TRUE);
    final Term res = (Term) fVisitor.visitGCExpr(x, o);
    o.put("old", old);
    return res;
  }

  public Object thisLiteral(final ThisExpr x, final Object o) {
    return Ref.varThis;
  }

  
  @SuppressWarnings("unchecked")
  public Term quantifier(final QuantifiedExpr x, final Object o) {
  
    ((MethodProperties) o).put("quantifier", Boolean.TRUE); 
    fVisitor.visitGCExpr(x, o);  
    ((MethodProperties) o).put("quantifier", Boolean.FALSE); 
    
    final java.util.Set<QuantVariable> qVarsSet = 
      (HashSet) ((MethodProperties) o).get("quantVars");
    final QuantVariable[] qVarArray = new QuantVariable[qVarsSet.size()];
    final Iterator iter = qVarsSet.iterator();
    int i = 0;
    while (iter.hasNext()) {
      qVarArray[i] = (QuantVariable) iter.next();
      i++;
    }  
    ((MethodProperties) o).put("quantVars", new HashSet<QuantVariable>()); 
    final Term exprTerm = (Term) x.expr.accept(fVisitor, o);
    
    if (x.quantifier == TagConstants.FORALL) {
      return Logic.forall(qVarArray, exprTerm);
    }
    else {
      return Logic.exists(qVarArray, exprTerm);
    }
  }

}
 
