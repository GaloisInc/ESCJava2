package escjava.vcGeneration;

// TBoolOp = return a boolean and sons are boolean : list(boolean) -> boolean
class TStore extends TFunction {

        public void typeTree(){
	
	if(sons.size()!=3)
	    System.err.println("TStore node with "+sons.size()+" instead of 3, that's strange...");
	else {
	    TNode n1 = getChildAt(0);
	    TNode n2 = getChildAt(1);
	    TNode n3 = getChildAt(2);

	    n1.setType($Field, true);
	    n2.setType($Reference,true);

	    n1.typeTree();
	    n2.typeTree();
	    n3.typeTree();
	}

    }

} 

// %Reference -> %Type
