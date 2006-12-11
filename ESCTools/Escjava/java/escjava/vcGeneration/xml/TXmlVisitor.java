package escjava.vcGeneration.xml;

import escjava.vcGeneration.*;

import java.io.*;

import org.w3c.dom.*;

/**
 * Visitor implementation that generates an XML string conforming to the 
 * DTD <i>escjava/vcGeneration/xml/xmlprover.dtd</i>.
 * 
 * @author Carl Pulley
 */
class TXmlVisitor extends TVisitor {
    
    private Document dom;
    private Element node;
    
    /**
     * In this visitor class, the <i>out</i> stream is <b>not</b> used.
     * 
     * <p>Instead, the <i>node</i> and <i>dom</i> object references are used to pass 
     * back the generated term to calling code.
     * 
     * @param out this parameter is <b>ignored</b> by this class implementation
     * @throws IOException
     */
    public TXmlVisitor(Writer out) throws IOException {
        super(out);
    }
    
    public void setDocumentNode(Document dom, Element node) {
        this.dom = dom;
        this.node = node;
    }
    
    private void prop(String tagName, TFunction n) throws IOException {
        Element propNode = dom.createElement("PROP");
        Attr nameAttr = dom.createAttribute("name");
        nameAttr.setValue(tagName);
        propNode.setAttributeNode(nameAttr);
        node.appendChild(propNode);
        Element savedNode = node;
        node = propNode;
        for (int index = 0; index < n.sons.size(); index++) {
            n.getChildAt(index).accept(this);
        }
        node = savedNode;
    }
    private void quant(String tagName, TFunction n) throws IOException {
        Element quantNode = dom.createElement("QUANT");
        Attr nameAttr = dom.createAttribute("name");
        nameAttr.setValue(tagName);
        quantNode.setAttributeNode(nameAttr);
        node.appendChild(quantNode);
        Element savedNode = node;
        Element patternNode = dom.createElement("PAT");
        quantNode.appendChild(patternNode);
        Element bodyNode = dom.createElement("BODY");
        quantNode.appendChild(bodyNode);
        node = quantNode;
        n.getChildAt(0).accept(this);
        node = patternNode;
        n.getChildAt(1).accept(this);
        node = bodyNode;
        n.getChildAt(2).accept(this);
        node = savedNode;
    }

    private void pred(String tagType, String tagName, TFunction n) throws IOException {
        Element tagNmNode = dom.createElement("PRED");
        Attr nameAttr = dom.createAttribute("name");
        nameAttr.setValue(tagName);
        tagNmNode.setAttributeNode(nameAttr);
        if (tagType != null) {
            Attr typeAttr = dom.createAttribute("type");
            typeAttr.setValue(tagType);
            tagNmNode.setAttributeNode(typeAttr);
        }
        node.appendChild(tagNmNode);
        Element savedNode = node;
        node = tagNmNode;
        for(int index = 0; index < n.sons.size(); index++) {
            n.getChildAt(index).accept(this);
        }
        node = savedNode;
    }
    
    private void term(String tagType, String tagName, TFunction n) throws IOException {
        Element tagNmNode = dom.createElement("TERM");
        Attr nameAttr = dom.createAttribute("name");
        nameAttr.setValue(tagName);
        tagNmNode.setAttributeNode(nameAttr);
        if (tagType != null) {
            Attr typeAttr = dom.createAttribute("type");
            typeAttr.setValue(tagType);
            tagNmNode.setAttributeNode(typeAttr);
        }
        node.appendChild(tagNmNode);
        Element savedNode = node;
        node = tagNmNode;
        for(int index = 0; index < n.sons.size(); index++) {
            n.getChildAt(index).accept(this);
        }
        node = savedNode;
    }

    private void xmlValue(String tagType, Object value) throws IOException {
        Element valueNode = dom.createElement("CONST");
        Attr typeAttr = dom.createAttribute("type");
        typeAttr.setValue(tagType);
        valueNode.setAttributeNode(typeAttr);
        Attr valueAttr = dom.createAttribute("value");
        if (value != null) {
            valueAttr.setValue(value.toString());
        } else {
            valueAttr.setValue("null");
        }
        valueNode.setAttributeNode(valueAttr);
        node.appendChild(valueNode);
    }
    
    public void visitTName(/*@ non_null @*/TName n) throws IOException {
        VariableInfo vi = TNode.getVariableInfo(n.name);
        Element nameNode = dom.createElement("VAR");
        Attr valueAttr = dom.createAttribute("name");
        valueAttr.setValue(vi.getVariableInfo());
        nameNode.setAttributeNode(valueAttr);
        node.appendChild(nameNode);
    }

    public void visitTRoot(/*@ non_null @*/TRoot n) throws IOException {
        for(int index = 0; index < n.sons.size(); index++) {
            n.getChildAt(index).accept(this);
        }
    }

    public void visitTBoolImplies(/*@ non_null @*/TBoolImplies n) throws IOException {
        prop("IMPLIES", n);
    }

    public void visitTBoolAnd(/*@ non_null @*/TBoolAnd n) throws IOException {
        prop("AND", n);
    }

    public void visitTBoolOr(/*@ non_null @*/TBoolOr n) throws IOException {
        prop("OR", n);
    }

    public void visitTBoolNot(/*@ non_null @*/TBoolNot n) throws IOException {
        prop("NOT", n);
    }

    public void visitTBoolEQ(/*@ non_null @*/TBoolEQ n) throws IOException {
        prop("EQ", n);
    }

    public void visitTBoolNE(/*@ non_null @*/TBoolNE n) throws IOException {
        prop("NEQ", n);
    }

    public void visitTAllocLT(/*@ non_null @*/TAllocLT n) throws IOException {
        pred("alloc", "LT", n);
    }

    public void visitTAllocLE(/*@ non_null @*/TAllocLE n) throws IOException {
        pred("alloc", "LE", n);
    }

    public void visitTAnyEQ(/*@ non_null @*/TAnyEQ n) throws IOException {
        pred("any", "EQ", n);
    }

    public void visitTAnyNE(/*@ non_null @*/TAnyNE n) throws IOException {
        pred("any", "NEQ", n);
    }

    public void visitTIntegralEQ(/*@ non_null @*/TIntegralEQ n) throws IOException {
        pred("int", "EQ", n);
    }

    public void visitTIntegralGE(/*@ non_null @*/TIntegralGE n) throws IOException {
        pred("int", "GE", n);
    }

    public void visitTIntegralGT(/*@ non_null @*/TIntegralGT n) throws IOException {
        pred("int", "GT", n);
    }

    public void visitTIntegralLE(/*@ non_null @*/TIntegralLE n) throws IOException {
        pred("int", "LE", n);
    }

    public void visitTIntegralLT(/*@ non_null @*/TIntegralLT n) throws IOException {
        pred("int", "LT", n);
    }

    public void visitTIntegralNE(/*@ non_null @*/TIntegralNE n) throws IOException {
        pred("int", "NEQ", n);
    }

    public void visitTIntegralAdd(/*@ non_null @*/TIntegralAdd n) throws IOException {
        term("int", "ADD", n);
    }

    public void visitTIntegralDiv(/*@ non_null @*/TIntegralDiv n) throws IOException {
        term("int", "DIV", n);
    }

    public void visitTIntegralMod(/*@ non_null @*/TIntegralMod n) throws IOException {
        term("int", "MOD", n);
    }

    public void visitTIntegralMul(/*@ non_null @*/TIntegralMul n) throws IOException {
        term("int", "TIMES", n);
    }

    public void visitTFloatEQ(/*@ non_null @*/TFloatEQ n) throws IOException {
        pred("float", "EQ", n);
    }

    public void visitTFloatGE(/*@ non_null @*/TFloatGE n) throws IOException {
        pred("float", "GE", n);
    }

    public void visitTFloatGT(/*@ non_null @*/TFloatGT n) throws IOException {
        pred("float", "GT", n);
    }

    public void visitTFloatLE(/*@ non_null @*/TFloatLE n) throws IOException {
        pred("float", "LE", n);
    }

    public void visitTFloatLT(/*@ non_null @*/TFloatLT n) throws IOException {
        pred("float", "GE", n);
    }

    public void visitTFloatNE(/*@ non_null @*/TFloatNE n) throws IOException {
        pred("float", "NEQ", n);
    }

    public void visitTFloatAdd(/*@ non_null @*/TFloatAdd n) throws IOException {
        term("float", "ADD", n);
    }

    public void visitTFloatDiv(/*@ non_null @*/TFloatDiv n) throws IOException {
        term("float", "DIV", n);
    }

    public void visitTFloatMod(/*@ non_null @*/TFloatMod n) throws IOException {
        term("float", "MOD", n);
    }

    public void visitTFloatMul(/*@ non_null @*/TFloatMul n) throws IOException {
        term("float", "TIMES", n);
    }

    public void visitTLockLE(/*@ non_null @*/TLockLE n) throws IOException {
        pred("lock", "LE", n);
    }

    public void visitTLockLT(/*@ non_null @*/TLockLT n) throws IOException {
        pred("lock", "LT", n);
    }

    public void visitTRefEQ(/*@ non_null @*/TRefEQ n) throws IOException {
        pred("ref", "EQ", n);
    }

    public void visitTRefNE(/*@ non_null @*/TRefNE n) throws IOException {
        pred("ref", "NEQ", n);
    }

    public void visitTTypeEQ(/*@ non_null @*/TTypeEQ n) throws IOException {
        pred("type", "EQ", n);
    }

    public void visitTTypeNE(/*@ non_null @*/TTypeNE n) throws IOException {
        pred("type", "NEQ", n);
    }

    public void visitTTypeLE(/*@ non_null @*/TTypeLE n) throws IOException {
        pred("type", "LE", n);
    }

    public void visitTCast(/*@ non_null @*/TCast n) throws IOException {
        term(null, "cast", n);
    }

    public void visitTIs(/*@ non_null @*/TIs n) throws IOException {
        pred(null, "is", n);
    }

    public void visitTSelect(/*@ non_null @*/TSelect n) throws IOException {
        term(null, "select", n);
    }

    public void visitTStore(/*@ non_null @*/TStore n) throws IOException {
        term(null, "store", n);
    }

    public void visitTTypeOf(/*@ non_null @*/TTypeOf n) throws IOException {
        term(null, "typeof", n);
    }

    public void visitTForAll(/*@ non_null @*/TForAll n) throws IOException {
        quant("FORALL", n);
    }

    public void visitTExist(/*@ non_null @*/TExist n) throws IOException {
        quant("EXISTS", n);
    }

    public void visitTIsAllocated(/*@ non_null @*/TIsAllocated n) throws IOException {
        pred(null, "isAllocated", n);
    }

    public void visitTEClosedTime(/*@ non_null @*/TEClosedTime n) throws IOException {
        term(null, "eClosedTime", n);
    }

    public void visitTFClosedTime(/*@ non_null @*/TFClosedTime n) throws IOException {
        term(null, "fClosedTime", n);
    }

    public void visitTAsElems(/*@ non_null @*/TAsElems n) throws IOException {
        term(null, "asElems", n);
    }

    public void visitTAsField(/*@ non_null @*/TAsField n) throws IOException {
        term(null, "asField", n);
    }

    public void visitTAsLockSet(/*@ non_null @*/TAsLockSet n) throws IOException {
        term(null, "asLockSet", n);
    }

    public void visitTArrayLength(/*@ non_null @*/TArrayLength n) throws IOException {
        term("array", "Length", n);
    }

    public void visitTArrayFresh(/*@ non_null @*/TArrayFresh n) throws IOException {
        term("array", "Fresh", n);
    }

    public void visitTArrayShapeOne(/*@ non_null @*/TArrayShapeOne n) throws IOException {
        term("array", "ShapeOne", n);
    }

    public void visitTArrayShapeMore(/*@ non_null @*/TArrayShapeMore n) throws IOException {
        term("array", "ShapeMore", n);
    }

    public void visitTIsNewArray(/*@ non_null @*/TIsNewArray n) throws IOException {
        term(null, "IsNewArray", n);
    }

    public void visitTString(/*@ non_null @*/TString n) throws IOException {
        xmlValue("String", n.value);
    }

    public void visitTBoolean(/*@ non_null @*/TBoolean n) throws IOException {
        xmlValue("Boolean", new Boolean(n.value));
    }

    public void visitTChar(/*@ non_null @*/TChar n) throws IOException {
        xmlValue("Character", new Character(n.value));
    }

    public void visitTInt(/*@ non_null @*/TInt n) throws IOException {
        xmlValue("Integer", new Integer(n.value));
    }

    public void visitTFloat(/*@ non_null @*/TFloat n) throws IOException {
        xmlValue("Float", new Float(n.value));
    }

    public void visitTDouble(/*@ non_null @*/TDouble n) throws IOException {
        xmlValue("Double", new Double(n.value));
    }

    public void visitTNull(/*@ non_null @*/TNull n) throws IOException {
        xmlValue("Object", null);
    }

	public void visitTUnset(/*@non_null*/TUnset n) throws IOException {
		term(null, "unset", n);
	}

	public void visitTMethodCall(/*@non_null*/TMethodCall call) throws IOException {
		term("Method", TNode.prover.getVariableInfo(call.getName()), call);
	}

	public void visitTIntegralSub(/*@non_null*/TIntegralSub sub) throws IOException {
		term("int", "SUB", sub);
	}

	public void visitTSum(/*@non_null@*/ TSum s) {
		// TODO Auto-generated method stub
		
	}

}
