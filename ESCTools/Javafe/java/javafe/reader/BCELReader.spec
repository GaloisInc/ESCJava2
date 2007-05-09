/* Copyright 2007, Systems Research Group, University College Dublin, Ireland */

/*
 * ========================================================================= 
 * BCELReader.spec
 * =========================================================================
 *
 * @note This is a partial specification for the conversion of a BCEL JavaClass
 * format into an abstract syntax tree of a compilatiom unit.
 *
 */

package javafe.reader;

import java.io.DataInput;
import java.io.IOException;
import java.util.Vector;

import javafe.ast.CompilationUnit;
import javafe.ast.Identifier;
import javafe.ast.TypeDecl;
import javafe.genericfile.GenericFile;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.tools.ant.Location;

import com.sun.tools.javac.util.Name;

import escjava.ast.Modifiers;
import escjava.ast.TagConstants;

/**
 * 
 * @design Parses the contents of a class file into an AST for the purpose of
 *         type checking. Ignores components of the class file that have no
 *         relevance to type checking (e.g. method bodies).
 */

class BCELReader extends Reader {
	/* -- package instance methods ------------------------------------------- */

	/**
	 * The package name of the class being parsed. Initialized by constructor
	 * (by way of set_this_class)
	 */
	public Name classPackage;

	/**
	 * The AST of the class parsed by this parser. Initialized by constructor
	 * (by way of parse_file).
	 */
	// @ invariant typeDecl != null;
	// @ invariant typeDecl.specOnly;
	public TypeDecl typeDecl;

	/**
	 * A dummy location representing the class being parsed. Initialized by
	 * constructor.
	 */
	// @ invariant classLocation != Location.NULL;
	public int classLocation;

	/**
	 * The BCEL representation of the binary classfile.
	 */
	protected JavaClass javaClass;

	/* -- protected instance methods ----------------------------------------- */

	/**
	 * Add only AST nodes that are not synthetic decls to v. nodes should be an
	 * array of TypeDeclElems. A synthetic decl is one that had the synthetic
	 * attribute, or is a static method decl for an interface.
	 */
	protected void addNonSyntheticDecls(/* @ non_null */TypeDeclElemVec v,
	/* @ non_null */TypeDeclElem[] elems);

	 
	protected boolean includeBodies;

	protected boolean omitPrivateFields = true;

	/**
	 * 
	 */
	public BCELReader() {
	}

	/**
	 * Binary inner class constructors have an extra initial argument to their
	 * constructors (the enclosing class). This is not present in the source
	 * file. To make the AST generated by reading the binary correspond to that
	 * obtained from a source file, we remove that extra argument for each inner
	 * (non-static) class. Since we do this at the end of parse_file, each
	 * nested class does this for its own direct inner classes. - DRCok
	 */
	protected void removeExtraArg();

	/**
	 * Convert the BCEL JavaClass format into an abstract syntax tree of a
	 * compilation unit suitable for extended static checking.
	 * 
	 * @requires javaClass != null;
	 * 
	 * @ensures \result != null;
	 * 
	 * 
	 * @author dermotcochran
	 * @return An abstract syntax tree of a compilation unit.
	 * 
	 * @throws ClassNotFoundException
	 * @throws CloneNotSupportedException
	 * @throws ClassFormatError
	 */

	protected CompilationUnit convertBCELtoAST() throws ClassNotFoundException;

	/**
	 * Read fields from BCEL into AST
	 * 
	 * @param fields
	 * @throws ClassFormatError
	 * 
	 * @requires fields != null;
	 */
	protected void readFields(Field[] fields);

	/**
	 * Read constants from BCEL into AST
	 * 
	 * @param constantPool
	 * @throws ClassFormatError
	 * 
	 * @requires constantPool != null
	 */
	protected void readConstants(ConstantPool constantPool)
			throws ClassFormatError;

	/**
	 * @param methods
	 * @throws ClassFormatError
	 */
	protected void readMethods(Method[] methods) throws ClassFormatError;

	/**
	 * 
	 */
	protected int extractLocation(JavaClass javaClass);

	/**
	 * 
	 */
	protected ImportDeclVec extractImportDeclVec(JavaClass javaClass);

	/**
	 * 
	 */
	protected TypeDeclVec extractTypeDeclVec(JavaClass javaClass);

	/**
	 * 
	 */
	protected TypeDeclElemVec extractTypeDeclElemVec(JavaClass javaClass);

	/* -- protected instance methods ----------------------------------------- */

	/**
	 * Add only AST nodes that are not synthetic decls to v. nodes should be an
	 * array of TypeDeclElems. A synthetic decl is one that had the synthetic
	 * attribute, or is a static method decl for an interface.
	 */
	protected void addNonSyntheticDecls(/* @ non_null */TypeDeclElemVec v,
	/* @ non_null */TypeDeclElem[] elems, JavaClass javaClass);

	/**
	 * 
	 */
	protected void set_num_constants(int cnum) throws ClassFormatError;
	
	/**
	 * @param i
	 * @param ctype
	 * @param value
	 * @throws ClassFormatError
	 */
	protected void set_const(int i, int ctype, Object value)
			throws ClassFormatError;

	/**
	 * Set the class attributes
	 * 
	 * @param aname
	 * @param n
	 * @param num_classes
	 * @throws IOException
	 * @throws ClassFormatError
	 */
	protected void set_class_attribute(/* @non_null */String aname,
	/* @non_null */int n, int num_classes) throws IOException,
			ClassFormatError;  

	/**
	 * Get outer class index
	 * 
	 * @param classIndex
	 * @requires classIndex >= 0;
	 * @return
	 */
	protected int getOuterClassIndex(int classIndex);

	/**
	 * Get inner class index
	 * 
	 * @param classIndex
	 * @requires classIndex >= 0;
	 * @return
	 */
	protected int getInnerClassIndex(int classIndex);

	/**
	 * Recursively read details of inner class
	 * 
	 * @param innerClassFile
	 * @param avoidSpec
	 * @return
	 */
	protected BCELReader getParser(GenericFile innerClassFile, boolean avoidSpec);

	/**
	 * Set the class type details
	 * 
	 * @param cindex
	 * 
	 * @requires cindex >= 0;
	 * 
	 * @throws ClassFormatError
	 */
	protected void set_this_class(int cindex) throws ClassFormatError;

	/**
	 * 
	 */
	protected void set_super_class(int cindex) throws ClassFormatError;

	/**
	 * 
	 */
	protected void set_num_interfaces(int n) throws ClassFormatError;

	/**
	 * 
	 */
	protected void set_interface(int index, int cindex) throws ClassFormatError;

	/**
	 * 
	 * @requires n > 0
	 */
	protected void set_num_fields(int n) throws ClassFormatError;

	/**
	 * @param i
	 * @param fname
	 * @param fieldType
	 * @param mod
	 * @throws ClassFormatError
	 */
	protected void set_field(int i, String fname, Type fieldType, int mod)
			throws ClassFormatError;

	/**
	 * @param fieldType
	 * @return
	 */
	protected javafe.ast.Type readType(Type fieldType);

	/**
	 * 
	 */
	protected void set_field_initializer(int i, Object value)
			throws ClassFormatError;

	/**
	 * @requires n > 0;
	 * @ensures routineDecl != null;
	 */
	protected void set_num_methods(int n) throws ClassFormatError;

	/**
	 * Parse a method
	 * 
	 * @param i
	 * @param mname
	 * @param sig
	 * @param mod
	 * @throws ClassFormatError
	 */
	protected void set_method(int i, String mname, String sig, int mod)
			throws ClassFormatError;

	/**
	 * 
	 */
	protected void set_method_attribute(int i, String aname, DataInput stream,
			int n) throws IOException, ClassFormatError, ClassNotFoundException;

	/* -- private instance variables ----------------------------------------- */

	/**
	 * The input file being parsed.
	 */
	/* @ non_null */ GenericFile inputFile;

	/**
	 * The class members of the class being parsed. Intialized by set_field,
	 * set_method, and set_class_attributes.
	 */
	// @ invariant classMembers != null;
	TypeDeclElemVec classMembers;

	/**
	 * The fields of the class being parsed. Initialized by set_num_fields.
	 * Elements initialized by set_field.
	 */
	// @ invariant fields != null;
	// @ invariant \typeof(fields) == \type(FieldDecl[]);
	// @ spec_public
	protected FieldDecl[] fieldDecl;

	/**
	 * The methods and constructors of the class being parsed. Initialized by
	 * set_num_methods. Elements initialized by set_method.
	 */
	// @ invariant routines != null;
	// @ invariant \typeof(routines) == \type(RoutineDecl[]);
	// @ spec_public
	protected RoutineDecl[] routineDecl;

	/**
	 * The identifier of the class being parsed. Initialized by set_this_class.
	 */
	// @ spec_public
	protected Identifier classIdentifier;

	/**
	 * 
	 */
	public CompilationUnit read(GenericFile target, boolean avoidSpec);

}
