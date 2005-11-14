package escjava.vcGeneration.coq.visitor;

import escjava.vcGeneration.*;
import escjava.vcGeneration.coq.CoqProver;
import escjava.vcGeneration.coq.CoqStringBuffer;

public abstract class AArrayOpsVisitor extends AFloatVisitor {

	protected AArrayOpsVisitor(CoqProver prover, CoqStringBuffer out) {
		super(prover, out);
	}
	
    public void visitTAsLockSet(/*@ non_null @*/ TAsLockSet n){
    	genericFun("asLockSet", n);
    	
    }
				  
    public void visitTArrayLength(/*@ non_null @*/ TArrayLength n){
    	genericFun("arrayLength", n);
    }
    public void visitTArrayFresh(/*@ non_null @*/ TArrayFresh n){
    	if(TNode.$boolean.equals(n.getChildAt(6).type)) {
    		String s= "arrayFreshBool";
    	
	    	out.appendI("("+ s+" ");
	    	
	    	int i =0;
	    	for(; i < n.sons.size(); i++) {
	    		
	    	    n.getChildAt(i).accept(tcbv);
	
	    	    /*
	    	     * If not last, output a space
	    	     */
	    	    if(i != n.sons.size() - 1)
	    		out.appendN(" ");
	    	}
	    	out.appendN(")");
	
	    	if((n.getChildAt(--i)) instanceof TName || (n.getChildAt(--i)) instanceof TLiteral)
	    	    out.reduceIwNl();
	    	else
	    	    out.reduceI();
	    }
    	else
    		genericFun("arrayFresh", n);     	
    	
    }
    public void visitTArrayShapeOne(/*@ non_null @*/ TArrayShapeOne n){
    	genericFun("arrayShapeOne", n);
    }
    public void visitTArrayShapeMore(/*@ non_null @*/ TArrayShapeMore n){
    	genericFun("arrayShapeMore", n);
    }
    
	  
    public void visitTSelect(/*@ non_null @*/ TSelect n){
    	String pre = "";
    	if(TNode.$integer.equals(((TNode)n.sons.get(1)).type))
    		pre = "arr";
    	if((TNode.$integer.equals(n.type))||
    			(TNode.$INTTYPE.equals(n.type)) ||
    			n.parent instanceof TIntegralEQ) {
    		genericFun("IntHeap." + pre +"select ", n);
    		
    	} else if(TNode.$boolean.equals(n.type))
    		genericFun("BoolHeap." + pre +"select ", n);
    	else
    		genericFun("RefHeap." +pre +"select ", n);
    }
    public void visitTStore(/*@ non_null @*/ TStore n){
    	String pre = "";
    	TNode index =(TNode)n.sons.get(1);
    	TNode val =(TNode)n.sons.get(2);
    	TypeInfo tval = val.type;
    	if(val instanceof TName) {
    		tval = TNode.getVariableInfo(((TName)val).name).type;
    	}
    	if(TNode.$integer.equals(index.type))
    		pre = "arr";
    	
    	if((TNode.$integer.equals(tval)) ||
    			(TNode.$INTTYPE.equals(tval))) {
    		
    		genericFun("IntHeap." + pre + "store ", n);
    	}
    	else if(TNode.$boolean.equals(tval))
    		genericFun("BoolHeap." + pre +"store ", n);
    	else {
    		if(val instanceof TName) {
    			String na = ((TName)val).name;
    			if(na.indexOf("resu") != -1)
    				System.out.println(na + " " + val.type);	
    		}
    		genericFun("RefHeap." + pre + "store ", n);
    	}
    }
	  

    public void visitTIsNewArray(/*@ non_null @*/ TIsNewArray n){
    	genericFun("isNewArray", n);
    }
}
