package escjava.vcGeneration;

// TBoolOp = return a boolean and sons are boolean : list(boolean) -> boolean
public class TFloatGE extends TFloatOp {

    public void accept(/*@ non_null @*/ TVisitor v){
	v.visitTFloatGE(this);
    }

}
