package escjava.parser;

import javafe.ast.*;

public class OldVarDecl extends javafe.ast.LocalVarDecl {

  protected OldVarDecl(int modifiers, ModifierPragmaVec pmodifiers, /*@ non_null @*/ Identifier id, /*@ non_null @*/ Type type, int locId, VarInit init, int locAssignOp) {
        super(modifiers, pmodifiers, id, type, locId, init, locAssignOp);
    }

    public static OldVarDecl make(/*@ non_null */ Identifier id,
			/*@ non_null */ Type type,
			int locId,
			/*@ non_null */ VarInit init,
			int locAssignOp
			) {
	OldVarDecl result = new OldVarDecl(
                                Modifiers.NONE,
                                null,
                                id,
                                type,
                                locId,
                                init,
                                locAssignOp);
	return result;
    }

}
