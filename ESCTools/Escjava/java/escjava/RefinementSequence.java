/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava;

import javafe.ast.CompilationUnit;
import javafe.ast.LexicalPragmaVec;
import javafe.ast.Modifiers;
import javafe.ast.Identifier;
import javafe.ast.Name;
import javafe.ast.*;
import javafe.ast.TypeDecl;
import javafe.ast.TypeDeclVec;
import javafe.ast.PrettyPrint;			// Debugging methods only
import javafe.ast.StandardPrettyPrint;		// Debugging methods only
import javafe.ast.DelegatingPrettyPrint;	// Debugging methods only
import escjava.ast.EscPrettyPrint;		// Debugging methods only
import javafe.util.Location;
import escjava.ast.RefinePragma;
import escjava.ast.*;
import escjava.ast.TagConstants; // Resolves ambiguity
//import escjava.reader.EscTypeReader;

import escjava.AnnotationHandler;
import javafe.genericfile.*;
import javafe.parser.PragmaParser;
import javafe.filespace.Tree;
import javafe.filespace.Query;

import javafe.util.Assert;
import javafe.util.ErrorSet;

import javafe.reader.*;
import javafe.tc.OutsideEnv;

import java.util.ArrayList;
import java.util.Enumeration;

public class RefinementSequence extends CompilationUnit {

    protected AnnotationHandler annotationHandler;
    protected CompilationUnit javacu;
    protected ArrayList refinements; // list of CompilationUnits
    protected boolean hasJavaDef;
    protected boolean javaIsBinary = false;


    public RefinementSequence(
		ArrayList refinements, // list of CompilationUnit
		CompilationUnit javacu,
		AnnotationHandler ah) {
	annotationHandler = ah; // FIXME - not sure this is needed
	this.refinements = refinements;
	this.javacu = javacu;
	hasJavaDef = javacu != null;
	if (hasJavaDef) javaIsBinary = javacu.isBinary();

	// Put everything together
	CompilationUnit newcu = consolidateRefinements(refinements,javacu);

	// Copy results into self
	pkgName = newcu.pkgName;
	lexicalPragmas = newcu.lexicalPragmas;
	imports = newcu.imports;
	elems = newcu.elems;
	loc = newcu.loc;
    }

    //@ requires refinements.size() > 0;
    CompilationUnit consolidateRefinements(ArrayList refinements,
	    CompilationUnit javacu) {

	// There are two tasks.  First we have to create a consolidated
	// signature, consisting of both java and jml information.  For
	// the java information, we use the java or class file, if they
	// are available, and do not allow anything to be added to that.
	// If they are not available, we create the java signature by
	// combining all disjoint type elements from the various 
	// refinement files.  The jml signature is created by combining
	// the various refinements.  We have to do this now, prior to any
	// type checking, so that the type signature registered for this
	// type is accurate for other types to check against. [It would
	// be better to establish and register all signatures, and then
	// do any checking, but escjava is not organized that way.]

	// Secondly, we have to combine all specifications.  We are doing
	// that here, before the typechecking.  It is convenient to 
	// syntactically combine the specs, but the type names in the text
	// are not resolved, causing difficulties in matching elements of
	// type declarations.
	// However, if we delay merging material until after all 
	// typechecking is performed, then it is complicated to register
	// a type signature.  Perhaps this can be worked around and is 
	// probably the better way in the long run, but for the moment
	// the work is done here and the type comparisons are not real
	// robust.  FIXME -- DRCok

	CompilationUnit lastcu = (CompilationUnit)refinements.get(refinements.size()-1);
		    
	if (javacu != null && refinements.size() == 0) return javacu;
	if (javacu == null && refinements.size() == 1) return lastcu;
	if (javacu == lastcu && refinements.size() == 1) return javacu;

	//System.out.println("CONSOLIDATION NEEDED " + lastcu.sourceFile().getHumanName() + " " + (javacu==null?"":javacu.sourceFile().getHumanName()));

	// Variables into which to accumulate all the refinements
	Name pkgName = (javacu==null?lastcu:javacu).pkgName;
	LexicalPragmaVec lexicalPragmaVec = LexicalPragmaVec.make();
	ImportDeclVec imports = ImportDeclVec.make();
	TypeDeclVec types = TypeDeclVec.make();
	if (javacu != null) {
	    imports = javacu.imports.copy();
	    types = cleancopy(javacu.elems);
	}

	int loc = ((CompilationUnit)refinements.get(0)).loc;

	for (int k=refinements.size()-1; k>=0; --k) {
	    CompilationUnit cu = (CompilationUnit)refinements.get(k);
	    //System.out.println("INSERTING " + cu.sourceFile().getHumanName());

	    // Check that the package name is always consistent
	    String p = pkgName==null ? "" : pkgName.printName();
	    String cp = cu.pkgName==null ? "" : cu.pkgName.printName();
	    if (!cp.equals(p)) {
		ErrorSet.error(cu.loc,"Package name does not match the package name in " + Location.toFile(loc).getHumanName() + ": " +
		    cp + " vs. " + p);
	    }

	    // Combine all the NoWarn and Import pragmas 
	    // (leave out RefinePragmas)
	    LexicalPragmaVec lexvec = cu.lexicalPragmas;
	    for (int i=0; i<lexvec.size(); ++i) {
		LexicalPragma lexp = lexvec.elementAt(i);
		if (!(lexp instanceof RefinePragma)) {
		    lexicalPragmaVec.addElement(lexp);
		}
	    }

	    // Combine imports
	    imports.append(cu.imports);

	    // Combine all of the top-level type declarations
	    TypeDeclVec typevec = cu.elems;
	    for (int i=0; i<typevec.size(); ++i) {
		TypeDecl td = typevec.elementAt(i);
		boolean foundMatch = false;
		for (int j=0; j<types.size(); ++j) {
		    if (types.elementAt(j).id.equals(td.id)) {
			foundMatch = true;
			combineType(td,types.elementAt(j),javacu==null);
		    }
		}
		if (!foundMatch) {
		    if (javacu == null) {
			types.addElement(td);
		    } else if (!javaIsBinary) {
// FIXME - need to find the right class file for each type
			ErrorSet.error(td.getStartLoc(),
			    "Type declaration is not in the java file");
		    }
		}
	    }
	}
	return CompilationUnit.make(pkgName,lexicalPragmaVec,imports,types,loc);
    }
 
    void combineFields(FieldDecl newfd, FieldDecl fd) {
	// Check modifiers, pragmas -- FIXME
	// DOes it matter if we duplicate pragmas ? -- FIXME
	fd.modifiers |= newfd.modifiers;
	if (newfd.pmodifiers != null) {
	    if (fd.pmodifiers == null)
		fd.pmodifiers = newfd.pmodifiers.copy();
	    fd.pmodifiers.append(newfd.pmodifiers); 
	}
	if (fd.init == null) fd.init = newfd.init;
	else if (newfd.init != null) {
	    ErrorSet.error(newfd.locAssignOp,
		"A field may be initialized only once in a refinement sequence");
	}
	if (!equalTypes(fd.type,newfd.type)) {
	    ErrorSet.error(newfd.type.getStartLoc(),
		"The field has been redeclared with a new type (see " +
		Location.toString(fd.type.getStartLoc()) + ")");
	}
    }

    boolean match(RoutineDecl newrd, RoutineDecl rd) {
	if ((newrd instanceof MethodDecl) != 
	    (rd instanceof MethodDecl)) return false;
	if ((newrd instanceof ConstructorDecl) != 
	    (rd instanceof ConstructorDecl)) return false;
	if (newrd instanceof MethodDecl) {
	    MethodDecl newmd = (MethodDecl)newrd;
	    MethodDecl md = (MethodDecl)rd;
	    if ( !newmd.id.equals( md.id ) ) return false;
	    // FIXME - check reutrn type
	}
	if (newrd.args.size() != rd.args.size()) return false;
	for (int i=0; i<newrd.args.size(); ++i) {
	    FormalParaDecl newarg = newrd.args.elementAt(i);
	    FormalParaDecl arg = rd.args.elementAt(i);
	    // Mismatched id - an error or a non-match???
//System.out.println("IDS " + newarg.id + " " + arg.id);
// This comparison does not work for binary specs
	    //if (!(newarg.id.equals(arg.id))) return false;
	    // FIXME - check id
	    // FIXME - chech type
	    Type newtype = newarg.type;
	    Type type = arg.type;
	    if (!equalTypes(type,newtype)) return false;
	    
	}
	return true;
    }

    public boolean equalTypes(Type t, Type tt) {
	if (t instanceof PrimitiveType) {
	    if (!(tt instanceof PrimitiveType)) return false;
	    return ((PrimitiveType)t).tag == ((PrimitiveType)tt).tag;
	} else if (t instanceof ArrayType) {
	    if (!(tt instanceof ArrayType)) return false;
	    return equalTypes( ((ArrayType)t).elemType, ((ArrayType)tt).elemType );
	} else if (t instanceof TypeName) {
	    if (!(tt instanceof TypeName)) return false;
	    // This is not a robust way to check for equality of types
	    // An unqualified name may be resolved differently depending
	    // on the imports present; also thsi may not work for 
	    // nested types.  But this is the best we can do if we are
	    // testing equality before type resolution.
	    String s = ((TypeName)t).name.printName();
	    String ss = ((TypeName)tt).name.printName();
	    //System.out.println("COMP NAMES " + s + " " + ss);
	    return s.endsWith(ss) || ss.endsWith(s);
	} else {
	    ErrorSet.error("Implementation error: Unknown type in RefinementSequence.equalType " + t.getClass());
	    return t.getClass() == tt.getClass();
	}
    }

    void combineRoutine(RoutineDecl newrd, RoutineDecl rd) {
	// FIXME - check exceptions
	for (int i=0; i<newrd.args.size(); ++i) {
	    FormalParaDecl newarg = newrd.args.elementAt(i);
	    FormalParaDecl arg = rd.args.elementAt(i);
	    // FIXME - check modifiers
	    // FIXME - check pragmas; does it matter if we duplicate pragmas?
	    arg.modifiers |= newarg.modifiers;
	    if (newarg.pmodifiers != null) {
		if (arg.pmodifiers == null)
		    arg.pmodifiers = ModifierPragmaVec.make();
		arg.pmodifiers.append(newarg.pmodifiers);
	    }
	    // If rd is from a binary file, the argument names will
	    // be non-existent, so we need to fix them.
	    if (rd.binaryArgNames) arg.id = newarg.id;
	    else if (!arg.id.toString().equals(newarg.id.toString())) {
		ErrorSet.error(newarg.locId,
		    "Refinements may not change the names of formal parameters (" +
		    Location.toString(arg.locId) + ")");
	    }
	}
	rd.binaryArgNames = false;
	// FIXME - check modifiers ???
	// combine modifiers
	rd.modifiers |= newrd.modifiers;

	// Body: 
	//  Java routines:
	//     if we have a java/class file, the body will already
	//    be present.  The specs should not have a body, and we
	//    don't add it if they do, even if it is not present in 
	//    the Java file.
	//  JML routines:  Add the body if we do not have one already.
	// (We don't check the case of no Java body but a spec body
	//   for a Java routine.)
	if (rd.body == null) rd.body = newrd.body;
	else if (newrd.body != null) {
	    // If the bodies are the same object then we are just adding
	    // back the java method that was part of the starting CU.
	    // If 'implicit' is true, then the method is added by the 
	    // compiler, and is the same method (e.g. default constructor).
	    if (newrd.body != rd.body && !newrd.implicit && !rd.implicit) {
		ErrorSet.error(newrd.body.locOpenBrace,
		    "Body is specified more than once (" +
		    Location.toString(rd.body.locOpenBrace));
	    }
	}

	// combine pragmas
	if (newrd.pmodifiers != null) {
	    if (rd.pmodifiers == null) {
		rd.pmodifiers = ModifierPragmaVec.make();
	    }
	    if (rd.pmodifiers.size() != 0) 
		rd.pmodifiers.addElement(SimpleModifierPragma.make(TagConstants.ALSO_REFINE,rd.pmodifiers.elementAt(rd.pmodifiers.size()-1).getStartLoc() ));
		// FIXME - should not need this check anymore
	    if (rd.pmodifiers != newrd.pmodifiers)
		rd.pmodifiers.append(newrd.pmodifiers);
	}
	if (newrd.tmodifiers != null) {
	    if (rd.tmodifiers == null) {
		rd.tmodifiers = TypeModifierPragmaVec.make();
	    }
		// FIXME - should not need this check anymore
	    if (rd.tmodifiers != newrd.tmodifiers)
		rd.tmodifiers.append(newrd.tmodifiers);
	}

    }

    void combineType(TypeDecl newtd, TypeDecl td, boolean addNewItems) {
	// Compare modifiers -- FIXME
	td.modifiers |= newtd.modifiers;
	td.specOnly = td.specOnly && newtd.specOnly;
	td.loc = newtd.loc; // Just to avoid having loc in a class file
			// Need to understand and make more robust - FIXME

	// Add to the type's annotations
	if (newtd.pmodifiers != null) {
	    if (td.pmodifiers == null) {
		td.pmodifiers = ModifierPragmaVec.make();
	    }
	    td.pmodifiers.append(newtd.pmodifiers);
	}
	if (newtd.tmodifiers != null) {
	    if (td.tmodifiers == null) {
		td.tmodifiers = TypeModifierPragmaVec.make();
	    }
	    td.tmodifiers.append(newtd.tmodifiers);
	}

	// Verify that both are classes or both are interfaces --- FIXME
	// Verify that superInterfaces are identical -- FIXME
	// Verify that superclass is identical -- FIXME

	// Check and combine the fields etc. of the type declarations
	for (int i=0; i<newtd.elems.size(); ++i) {
	    TypeDeclElem tde = newtd.elems.elementAt(i);
	    boolean found = false;
	    if (tde instanceof FieldDecl) {
		for (int k=0; !found && k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof FieldDecl)) continue;
		    if (!( ((FieldDecl)tde).id.equals( ((FieldDecl)tdee).id ))) continue;
		    combineFields( (FieldDecl)tde, (FieldDecl)tdee );
		    found = true;
		}
		if (!found) {
		    if (addNewItems) {
			td.elems.addElement(tde);
			tde.setParent(td);
		    } else {
			ErrorSet.error(tde.getStartLoc(),
			    "Field is not declared in the java/class file");
		    }
		}
	    } else if (tde instanceof RoutineDecl) {
		for (int k=0; !found && k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof RoutineDecl)) continue;
		    if (!match( (RoutineDecl)tde, (RoutineDecl)tdee )) continue;
		    combineRoutine( (RoutineDecl)tde, (RoutineDecl)tdee );
		    found = true;
		}
		if (!found) {
		    if (addNewItems) {
			td.elems.addElement(tde);
			tde.setParent(td);
		    } else {
		      if (((RoutineDecl)tde).parent instanceof InterfaceDecl) {
			    // An interface may specify some methods that
			    // it does not declare, but 'knows' that its
			    // classes implement.  For example CharSequence
			    // specifies some methods that are in Object
			    // even though Object is not a superinterface
			    // of CharSequence. Perhaps this is only
			    // relevant to methods of Object, but for the
			    // moment we will make this a caution, though
			    // eventually we should detect that the method
			    // is a method of Object and either say nothing
			    // or give an error.  FIXME
			ErrorSet.caution(((RoutineDecl)tde).locId,
			    "Method is not declared in the java/class file");
		      } else {
			ErrorSet.error(((RoutineDecl)tde).locId,
			    "Method is not declared in the java/class file");
		      }
		    }
		}
	    } else if (tde instanceof TypeDecl) {
		for (int k=0; k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof TypeDecl)) continue;
		    if ( ((TypeDecl)tde).id.equals( ((TypeDecl)tdee).id)) {
			combineType( (TypeDecl)tde, (TypeDecl)tdee, addNewItems );
			found = true;
		    }
		}
		if (!found) {
		    if (addNewItems) {
			td.elems.addElement(tde);
			tde.setParent(td);
		    } else if (!javaIsBinary) {
// Can't do this error for binary files - additional types in a file are put in their own class file, including nested classes
// Do need to check these against the real class file.  FIXME
			ErrorSet.error(tde.getStartLoc(),
			    "Type is not declared in the java file");
		    }
		}
	    } else if (tde instanceof InitBlock) {
		// FIXME - combine modifiers ???
		// FIXME - combine pmodifiers ???
		if (addNewItems) {
		    td.elems.addElement(tde);
		    tde.setParent(td);
		} else {
		    ErrorSet.error(tde.getStartLoc(),
			"InitBlock should not be present in a spec file");
		}
	    } else if (tde instanceof GhostDeclPragma) {
		GhostDeclPragma g = (GhostDeclPragma)tde;
		for (int k=0; !found && k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof GhostDeclPragma)) continue;
		    if ( ((GhostDeclPragma)tde).decl.id.equals( ((GhostDeclPragma)tdee).decl.id)) {
			tdee.setModifiers(tdee.getModifiers() | tde.getModifiers()); // trim & check
			    // FIXME - check types and modifiers
			    // FIXME - what about initializer ???
			// FIXME - what combining to do???
			found = true;
		    }
		}
		if (!found) {
		    // Can always add specification stuff
		    td.elems.addElement(tde);
		    tde.setParent(td);
		}
		
	    } else if (tde instanceof ModelDeclPragma) {
		ModelDeclPragma g = (ModelDeclPragma)tde;
		for (int k=0; !found && k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof ModelDeclPragma)) continue;
		    if ( ((ModelDeclPragma)tde).decl.id.equals( ((ModelDeclPragma)tdee).decl.id)) {
			tdee.setModifiers(tdee.getModifiers() | tde.getModifiers()); // trim & check
			    // FIXME - check types and modifiers
			// FIXME - what combining to do???
			found = true;
		    }
		}
		if (!found) {
		    // Can always add specification stuff
		    td.elems.addElement(tde);
		    tde.setParent(td);
		}
	    } else if (tde instanceof ModelMethodDeclPragma) {
		for (int k=0; !found && k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof ModelMethodDeclPragma)) continue;
		    if (match( ((ModelMethodDeclPragma)tde).decl,
			       ((ModelMethodDeclPragma)tdee).decl)) {
			tdee.setModifiers(tdee.getModifiers() | tde.getModifiers()); // trim & check
			    // FIXME - check types and modifiers
			// FIXME - what combining to do???
			found = true;
		    }
		}
		if (!found) {
		    // Can always add specification stuff
		    td.elems.addElement(tde);
		    tde.setParent(td);
		}
		
	    } else if (tde instanceof ModelConstructorDeclPragma) {
		ModelConstructorDeclPragma g = (ModelConstructorDeclPragma)tde;
		for (int k=0; !found && k<td.elems.size(); ++k) {
		    TypeDeclElem tdee = td.elems.elementAt(k);
		    if (!(tdee instanceof ModelConstructorDeclPragma)) continue;
		    if (match( ((ModelConstructorDeclPragma)tde).decl,
			       ((ModelConstructorDeclPragma)tdee).decl)) {
			tdee.setModifiers(tdee.getModifiers() | tde.getModifiers()); // trim & check
			    // FIXME - check types and modifiers
			// FIXME - what combining to do???
			found = true;
		    }
		}
		if (!found) {
		    // Can always add specification stuff
		    td.elems.addElement(tde);
		    tde.setParent(td);
		}
		
	    } else if (tde instanceof TypeDeclElemPragma) {
		// Can always add specification stuff
		td.elems.addElement(tde);
		tde.setParent(td);
	    } else {
		ErrorSet.error(tde.getStartLoc(),"This type of type declaration element is not implemented - please report the problem: " + tde.getClass());
	    }
	}
    }

    /* These cleancopy routines produce a fresh, somewhat deep copy of a set
	of TypeDecl objects.  The purpose is to provide a copy that can be 
	modified by adding in the refinements, without modifying the original
	compilation unit obtained from java or class files.  Any part that
	needs to be changed via refinement is cloned.  In addition all 
	pragma stuff is removed (to be added in via the refinement sequence).
	Even prgam stuff in the java file is removed, so that it is added in
	in the correct sequence and is not added in twice.  In the case of
	binary files, the ids of formal arguments are set to null since they
	have arbitrary names rather than the declared ones.
    */
    TypeDeclVec cleancopy(TypeDeclVec types) {
	TypeDeclVec v = TypeDeclVec.make();
	for (int i = 0; i<types.size(); ++i) {
	    v.addElement(cleancopy(types.elementAt(i)));
	}
	return v;
    }

    TypeDecl cleancopy(TypeDecl td) {
	TypeDeclElemVec newelems = TypeDeclElemVec.make();
	for (int i=0; i<td.elems.size(); ++i) {
	    TypeDeclElem tde = cleancopy(td.elems.elementAt(i));
	    if (tde != null) newelems.addElement(tde);
	}
	TypeDecl newtd = null;
	if (td instanceof ClassDecl) {
	    ClassDecl cd = (ClassDecl)td;
	    newtd = ClassDecl.make(
			cd.modifiers, // shrink to Java only
			null,
			cd.id,
			cd.superInterfaces,
			null,
			newelems,
			cd.loc,
			cd.locId,
			cd.locOpenBrace,
			cd.locCloseBrace,
			cd.superClass);
	} else if (td instanceof InterfaceDecl) {
	    InterfaceDecl cd = (InterfaceDecl)td;
	    newtd = InterfaceDecl.make(
			cd.modifiers,
			null,
			cd.id,
			cd.superInterfaces,
			null,
			newelems,
			cd.loc,
			cd.locId,
			cd.locOpenBrace,
			cd.locCloseBrace);
	} else {
	    ErrorSet.fatal(td.getStartLoc(),
		"Not implemented: Unable to process this type in Refinement.cleancopy: " + td.getClass());
	}
	return newtd;
    }

    TypeDeclElem cleancopy(TypeDeclElem tde) {
	TypeDeclElem newtde = null;
	if (tde instanceof FieldDecl) {
	    FieldDecl d = (FieldDecl)tde;
	    newtde = FieldDecl.make(
		d.modifiers, // FIXME trim
		null,
		d.id,
		d.type,
		d.locId,
		d.init,
		d.locAssignOp);
	} else if (tde instanceof MethodDecl) {
	    MethodDecl d = (MethodDecl)tde;
	    newtde = MethodDecl.make(
		d.modifiers,
		null,
		null,
		d.args,
		d.raises,
		javaIsBinary? null: d.body,
		d.locOpenBrace,
		d.loc,
		d.locId,
		d.locThrowsKeyword,
		d.id,
		d.returnType,
		d.locType);
	    ((RoutineDecl)newtde).implicit = d.implicit;
	} else if (tde instanceof ConstructorDecl) {
	    ConstructorDecl d = (ConstructorDecl)tde;
	    newtde = ConstructorDecl.make(
		d.modifiers,
		null,
		null,
		d.args,
		d.raises,
		javaIsBinary? null: d.body,
		d.locOpenBrace,
		d.loc,
		d.locId,
		d.locThrowsKeyword);
	    ((RoutineDecl)newtde).implicit = d.implicit;
	} else if (tde instanceof TypeDecl) {
	    newtde = cleancopy((TypeDecl)tde);
	} else if (tde instanceof InitBlock) {
	    InitBlock d = (InitBlock)tde;
	    newtde = InitBlock.make(
		d.modifiers, // FIXME trim 
		null,
		javaIsBinary? null: d.block);
	} else if (tde instanceof TypeDeclElemPragma) {
	    newtde = null;
	} else {
	    ErrorSet.fatal(tde.getStartLoc(),
		"Not implemented: Unable to process this type in Refinement.cleancopy: " + tde.getClass());
	}
	if (javaIsBinary && newtde instanceof RoutineDecl) {
	    ((RoutineDecl)newtde).binaryArgNames = true;
	}
	return newtde;
    }
}
