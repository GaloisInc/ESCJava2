package escjava.vcGeneration.simplify;

import java.util.HashMap;
import java.io.*;

import escjava.vcGeneration.*;
import escjava.translate.*;
import javafe.ast.*;

public class SimplifyProver extends ProverType {
    
    public String labelRename(String label) {
        return label;
    }
    
    public TVisitor visitor(Writer out) throws IOException {
        return new TSimplifyVisitor(out);
    }

    public void getProof(Writer out, String proofName, TNode term) throws IOException {
        generateTerm(out, term);
    }

    public String getVariableInfo(VariableInfo caller) {

        /*
         * This method is special since <explanation>
         * FIXME
         */

        if (caller.type != TNode.$Type)
            return caller.old;

        else {
            if (caller.old.equals("boolean"))
                return "T_bool"; // fixme not sure
            else if (caller.old.equals("char"))
                return "T_char";
            else if (caller.old.equals("DOUBLETYPE"))
                return "T_double";
            else if (caller.old.equals("double"))
                return "T_double";
            else if (caller.old.equals("JMLDataGroup"))
                return "|T_org.jmlspecs.lang.JMLDataGroup|";
            else if (caller.old.equals("INTTYPE"))
                return "T_int";
            else if (caller.old.equals("integer"))
                return "T_int";
            else if (caller.old.equals("Unknown tag <242>"))
                return "T_anytype";
            else {
                if (caller.old.startsWith("java.")) //check if in the form java.x.y 
                    return "T_" + caller.old;
                else {
                    TDisplay.warn(this, "getVariableInfo()", "Type not handled : "
                            + caller.old);
                    TDisplay.warn(this, "getVariableInfo()",
                            "Considering it as a user defined type");
                    return "T_" + caller.old;
                }
            }
        }

    }

    public String getTypeInfo(TypeInfo caller) {
        return null;
    }

    public void init() {
        // Predefined types

        TNode.$Reference = TNode.addType("%Reference", "%Reference");
        TNode.$Time = TNode.addType("%Time", "%Time");
        TNode.$Type = TNode.addType("%Type", "%Type");
        TNode.$boolean = TNode.addType("boolean", "boolean");
        TNode.$char = TNode.addType("char", "char");
        TNode.$DOUBLETYPE = TNode.addType("DOUBLETYPE", "DOUBLETYPE");
        TNode.$double = TNode.addType("double", "double");
        TNode.$Field = TNode.addType("%Field", "%Field");
        TNode.$INTTYPE = TNode.addType("INTTYPE", "INTTYPE");
        TNode.$integer = TNode.addType("integer", "integer");
        TNode.$float = TNode.addType("float", "float");
        TNode.$Path = TNode.addType("%Path", "%Path"); // used to modelize different ways
        // of terminating a function
        //$String = addType("String", "String"); fixme, does this type appears in original proof ?

        // Predefined variables name
        // variables used by the old proof system and that we still need
        TNode.addName("ecReturn", "%Path", "ecReturn");
        TNode.addName("ecThrow", "%Path", "ecThrow");
        TNode.addName("XRES", "%Reference", "XRES");        
    }
    
    public Expr addTypeInfo(InitialState initState, Expr tree) {
        tree = GC.implies(initState.getInitialState(), tree);
        return tree;
    }
    
    public TNode rewrite(TNode tree) {
        // Intentionally does nothing
        return tree;
    }

	public void generateDeclarations(Writer s, HashMap variablesName) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
