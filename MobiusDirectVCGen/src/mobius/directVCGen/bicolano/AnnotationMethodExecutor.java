package mobius.directVCGen.bicolano;

import java.util.List;
import java.util.Vector;

import javafe.ast.FormalParaDecl;
import javafe.ast.FormalParaDeclVec;
import javafe.ast.RoutineDecl;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import escjava.sortedProver.Lifter.QuantVariableRef;

import mobius.bico.ABasicExecutor;
import mobius.bico.MethodHandler;
import mobius.bico.Util.Stream;
import mobius.directVCGen.formula.Expression;
import mobius.directVCGen.formula.Formula;
import mobius.directVCGen.formula.Heap;
import mobius.directVCGen.formula.Lookup;
import mobius.directVCGen.formula.Ref;
import mobius.directVCGen.vcgen.struct.Post;

public class AnnotationMethodExecutor extends ABasicExecutor {
  /** the current routine (method) that is treated - esc java style. */
  private final RoutineDecl fRout;
  /** the current method (routine) that is treated - bcel style. */
  private final Method fMeth;

  public AnnotationMethodExecutor(ABasicExecutor be, final Method met, final RoutineDecl rout) {
    super(be);
    fRout = rout;
    fMeth = met;
  }

  @Override
  public void start() {
    doMethodPreAndPostDefinition();
  }
  
  private void doMethodPreAndPostDefinition() {
    final MethodHandler hdl = getMethodHandler();
    final String name = hdl.getName(fMeth);
    final String nameModule = name + "_annotations";
    final String namePre = "pre";
    final String namePost = "post";
    final String nameAssertion = "assertion";
    final String nameAssumption = "assumption";
    final String nameSpec = "spec";
    final String defaultSpecs = "(0%nat,,(" + namePre + ",," + namePost + "))";

    final Stream out = getOut();
//    out.println(tab, "Definition " + namePost + " (s0:InitState) (t:ReturnState) := " +
//                        " @nil Prop.\n");
    out.println("Module " + nameModule + ".");
    out.incTab();
    
    doMethodPre(namePre);
    doMethodPost(namePost);
    out.println("Definition " + nameAssertion + " := " +
                AnnotationVisitor.getAssertion(out, fRout, fMeth) + ".");
    out.println("Definition " + nameAssumption + " :=" +
                  " (@PCM.empty Assumption).");
    out.println("Definition " + nameSpec + " :=");
    out.incTab();
    out.println("(" + defaultSpecs + ",,");
    out.incTab();
    out.println("(assertion,,assumption)). ");
    out.decTab();
    out.decTab();
    out.decTab();
    out.println("End " + nameModule + ".\n\n");
    
  }
  
  private void doMethodPre(final String namePre) {
    final Stream out = getOut();
    out.println("Definition mk_" + namePre + " := ");
    final List<QuantVariableRef> list = mkArguments(fRout, fMeth);
    
    String varsAndType = "";
    final String hname = Formula.generateFormulas(Heap.var).toString();
    varsAndType += "(" + hname + ": " + Formula.generateType(Heap.var.getSort()) +  ")";
        
    for (QuantVariableRef qvr: list) {
      final String vname = Formula.generateFormulas(qvr).toString();
      varsAndType += " (" + vname + ": " + Formula.generateType(qvr.getSort()) +  ")";
      
    }
    out.incTab();
    out.println("fun " + varsAndType + " => ");
    out.incTab();
    out.println(Formula.generateFormulas(Lookup.precondition(fRout)) + ".");
    out.decTab();
    out.decTab();
    out.println("Definition " + namePre + " (s0:InitState) := ");
    out.incTab();
    final String vars = doLetPre();
    out.incTab();
    //out.println(tab, "   let " + Ref.varThis + " := (do_lvget (fst s0) 0%N)" + " in " +
    out.println("mk_" + namePre +  " " + vars + ".");
    out.decTab();
    out.decTab();
  }
  
  
  private void doMethodPost(final String namePost) {
    final Stream out = getOut();
    // definition of the mk method
    out.println("Definition mk_" + namePost + " := ");
    final List<QuantVariableRef> list = mkOldArguments(fRout, fMeth);
    
    String varsAndType = "";
    
    
    final String olhname = Formula.generateFormulas(Heap.varPre).toString();
    varsAndType += "(" + olhname + ": " + Formula.generateType(Heap.varPre.getSort()) +  ")";
    
    final String hname = Formula.generateFormulas(Heap.var).toString();
    varsAndType += "(" + hname + ": " + Formula.generateType(Heap.var.getSort()) +  ")";
    
    
    for (QuantVariableRef qvr: list) {
      final String vname = Formula.generateFormulas(qvr).toString();

      varsAndType += " (" + vname + ": " + Formula.generateType(qvr.getSort()) +  ")";
      
    }
        
    Post normalPost = Lookup.normalPostcondition(fRout);
    Post excpPost = Lookup.exceptionalPostcondition(fRout);
    out.incTab();
    out.println("fun " + "(t: ReturnVal) " + varsAndType + " => ");
    out.incTab();
    out.println("match t with");
    final boolean hasRet = !(fMeth.getReturnType().equals(Type.VOID));
    
    if (hasRet) {
      out.println("| Normal (Some " + 
                                       Formula.generateFormulas(normalPost.getRVar()) + 
                                       ") =>");
    }
    else {
      out.println("| Normal None =>");
    }
    
    // momentary fix
    for (QuantVariableRef qvr: list) {
      System.out.println();
      System.out.println(qvr + " " + Expression.old(qvr));
      
      normalPost = new Post(normalPost.getRVar(),
                            normalPost.subst(Expression.old(qvr), qvr));
      excpPost = new Post(excpPost.getRVar(),
                            excpPost.subst(Expression.old(qvr), qvr));
      System.out.println(normalPost);
    }
    // end momentary fix 
    out.incTab();
    out.println("" + Formula.generateFormulas(normalPost.getPost()));
    out.decTab();
    if (hasRet) {
      out.println("| Normal None => True");
    }
    else {
      out.println("| Normal (Some _) => True");
    }
    out.println("| Exception " + 
                              Formula.generateFormulas(excpPost.getRVar()) + 
                                       " =>");
    out.incTab();
    out.println("" + Formula.generateFormulas(
                                       excpPost.substWith(
                                              Ref.fromLoc(excpPost.getRVar()))));
    out.decTab();
    out.println("end" + ".");
    out.decTab();
    out.decTab();
    
    // definition of the usable version
    out.println("Definition " + namePost + " (s0:InitState) (t:ReturnState) := ");
    out.incTab();
    final String vars = doLetPost();
    out.incTab();
    //out.println(tab, "   let " + Ref.varThis + " := (do_lvget (fst s0) 0%N)" + " in " +
    out.println( "mk_" + namePost +  " (snd t) " + vars + ".");
    out.decTab();
    out.decTab();
    
  }
  
  private String doLetPre() {
    final Stream out = getOut();
    String vars = "";
    final String hname = Formula.generateFormulas(Heap.var).toString();
    out.println("let " + hname + " := (snd s0) " + " in");
    vars += hname;
    int count = 0;
    for (QuantVariableRef qvr: mkArguments(fRout, fMeth)) {
      final String vname = Formula.generateFormulas(qvr).toString();
      out.println("let " + vname + " := " +
                           "(do_lvget (fst s0) " + count++ + "%N)" + " in ");
      vars += " " + vname;
    }
    return vars;
  }
  private String doLetPost() {
    final Stream out = getOut();
    String vars = "";
    final String olhname = Formula.generateFormulas(Heap.varPre).toString();
    out.println("let " + olhname + " := (snd s0) " + " in");
    vars += olhname;
    
    final String hname = Formula.generateFormulas(Heap.var).toString();
    out.println("let " + hname + " := (fst t) " + " in");
    vars += " " + hname;
    
    int count = 0;
    for (QuantVariableRef qvr: mkOldArguments(fRout, fMeth)) {
      final String vname = Formula.generateFormulas(qvr).toString();
      out.println("let " + vname + " := " +
                           "(do_lvget (fst s0) " + count++ + "%N)" + " in ");
      vars += " " + vname;
    }
    
    return vars;
  }
  
  public static List<QuantVariableRef> mkOldArguments(final RoutineDecl rd, Method met) {
    final List<QuantVariableRef> v = new Vector<QuantVariableRef>();
    final FormalParaDeclVec fpdvec = rd.args;
    if (!met.isStatic()) {
      v.add(Ref.varThis);
      
    }
    final FormalParaDecl[] args = fpdvec.toArray();
    for (FormalParaDecl fpd: args) {
      v.add(Expression.rvar(fpd));
    }
    return v;
  }
  
  public static List<QuantVariableRef> mkArguments(final RoutineDecl rd, Method met) {
    final List<QuantVariableRef> v = new Vector<QuantVariableRef>();
    final FormalParaDeclVec fpdvec = rd.args;
    if (!met.isStatic()) {
      v.add(Ref.varThis);
    }
    final FormalParaDecl[] args = fpdvec.toArray();
    for (FormalParaDecl fpd: args) {
      v.add(Expression.rvar(fpd));
    }
    return v;
  }

}