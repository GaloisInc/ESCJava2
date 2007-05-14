/* Copyright 2007, Systems Research Group, University College Dublin, Ireland */

/*
 * ========================================================================= BCELReader.java
 * =========================================================================
 */

package javafe.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javafe.ast.BlockStmt;
import javafe.ast.ClassDecl;
import javafe.ast.CompilationUnit;
import javafe.ast.ConstructorDecl;
import javafe.ast.FieldDecl;
import javafe.ast.FormalParaDecl;
import javafe.ast.FormalParaDeclVec;
import javafe.ast.Identifier;
import javafe.ast.ImportDeclVec;
import javafe.ast.InterfaceDecl;
import javafe.ast.JavafePrimitiveType;
import javafe.ast.MethodDecl;
import javafe.ast.Modifiers;
import javafe.ast.Name;
import javafe.ast.PrettyPrint;
import javafe.ast.RoutineDecl;
import javafe.ast.StmtVec;
import javafe.ast.TagConstants;
import javafe.ast.TypeDecl;
import javafe.ast.TypeDeclElem;
import javafe.ast.TypeDeclElemVec;
import javafe.ast.TypeDeclVec;
import javafe.ast.TypeName;
import javafe.ast.TypeNameVec;
import javafe.genericfile.GenericFile;
import javafe.genericfile.NormalGenericFile;
import javafe.util.Location;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

/*
 * ------------------------------------------------------------------------- BCELReader
 * -------------------------------------------------------------------------
 */

// @refines "BCELReader.spec";
/**
 * Parses the contents of a class file into an AST for the purpose of type checking. Ignores
 * components of the class file that have no relevance to type checking (e.g. method bodies).
 */

class BCELReader extends Reader {
   /* -- package instance methods ------------------------------------------- */

   /**
    * The package name of the class being parsed. Initialized by constructor (by way of
    * set_this_class)
    */
   public Name classPackage;

   /**
    * A dummy location representing the class being parsed. Initialized by constructor.
    */
   // @ invariant classLocation != Location.NULL;
   public int classLocation;

   /**
    * The BCEL representation of the binary classfile.
    */
   protected JavaClass javaClass;

   /* -- protected instance methods ----------------------------------------- */

   /**
    * Add only AST nodes that are not synthetic decls to v. nodes should be an array of
    * TypeDeclElems. A synthetic decl is one that had the synthetic attribute, or is a static
    * method decl for an interface.
    */
   protected void addNonSyntheticDecls(/* @ non_null */TypeDeclElemVec v,
   /* @ non_null */TypeDeclElem[] elems) {
      for (int i = 0; i < elems.length; i++) {
         if (synthetics.contains(elems[i])) { // @ nowarn;
            continue;
         }
         if ((javaClass.isInterface()) && elems[i] instanceof RoutineDecl) {
            RoutineDecl rd = (RoutineDecl) elems[i];

            if (Modifiers.isStatic(rd.modifiers)) {
               continue;
            }
         }
         if (omitPrivateFields && elems[i] instanceof FieldDecl) {
            if (Modifiers.isPrivate(((FieldDecl) elems[i]).modifiers)) {
               continue;
            }
         }
         v.addElement(elems[i]); // @ nowarn Pre;
      }
   }

   /**
    * Vector of methods and fields with Synthetic attributes. Use this to weed out synthetic
    * while constructing TypeDecl.
    */
   private/* @ non_null */Vector<RoutineDecl> synthetics = new Vector<RoutineDecl>();

   /**
    * Flag indicating whether the class being parsed has the synthetic attribute.
    */
   private boolean syntheticClass = false;

   protected boolean includeBodies;

   protected boolean omitPrivateFields = true;

   /**
    * Default constructor - does nothing
    */
   public BCELReader() {
   }

   /**
    * Binary inner class constructors have an extra initial argument to their constructors (the
    * enclosing class). This is not present in the source file. To make the AST generated by
    * reading the binary correspond to that obtained from a source file, we remove that extra
    * argument for each inner (non-static) class. Since we do this at the end of parse_file,
    * each nested class does this for its own direct inner classes. - DRCok
    * 
    * @param typeDecl
    */
   protected void removeExtraArg(TypeDecl typeDecl) {
      TypeDeclElemVec vv = typeDecl.elems;
      for (int k = 0; k < vv.size(); ++k) {
         if (!(vv.elementAt(k) instanceof ClassDecl))
            continue;
         ClassDecl cd = (ClassDecl) vv.elementAt(k);
         if (Modifiers.isStatic(cd.modifiers))
            continue;
         TypeDeclElemVec v = cd.elems;
         for (int j = 0; j < v.size(); ++j) {
            if (!(v.elementAt(j) instanceof ConstructorDecl))
               continue;
            ConstructorDecl cdl = (ConstructorDecl) v.elementAt(j);
            cdl.args.removeElementAt(0);
         }
      }
   }

   /**
    * Convert the BCEL JavaClass format into an abstract syntax tree of a compilation unit
    * suitable for extended static checking.
    * 
    * @return An abstract syntax tree of a compilation unit.
    * @throws ClassNotFoundException
    * @throws IOException
    * @throws ClassFormatError
    * @throws CloneNotSupportedException
    * @throws ClassFormatError
    */

   protected CompilationUnit getCompilationUnit() throws ClassNotFoundException,
         ClassFormatError, IOException {

      constantPool = javaClass.getConstantPool();
      readConstants(constantPool);

      classNameIndex = javaClass.getClassNameIndex();
      set_this_class();
      readClassAttributes();
      int superclassNameIndex = javaClass.getSuperclassNameIndex();
      set_super_class(superclassNameIndex);

      int classLocationIndex = classNameIndex;
      Name pkgName = Name.make(javaClass.getPackageName(), classLocationIndex);
      TypeDecl typeDecl = getTypeDecl();

      // Return
      TypeDeclVec types = TypeDeclVec.make(new TypeDecl[] { typeDecl });
      TypeDeclElemVec emptyTypeDeclElemVec = TypeDeclElemVec.make();
      ImportDeclVec emptyImportDeclVec = ImportDeclVec.make();
      CompilationUnit result = CompilationUnit.make(pkgName, null, emptyImportDeclVec, types,
            classLocationIndex, emptyTypeDeclElemVec);
      return result;
   }

   /**
    * @return
    * @throws ClassNotFoundException
    * @throws ClassFormatError
    */
   protected TypeDecl getTypeDecl() throws ClassNotFoundException, ClassFormatError {
      TypeDeclElemVec otherPragmas = extractTypeDeclElemVec(javaClass);
      ImportDeclVec imports = extractImportDeclVec(javaClass);
      TypeDeclVec elems = extractTypeDeclVec(javaClass);

      JavaClass[] interfaces = javaClass.getInterfaces();
      TypeNameVec interfaceVec = TypeNameVec.make(interfaces.length); // @
      // nowarn
      // Pre
      set_num_interfaces(interfaces.length);

      Method[] methods = javaClass.getMethods();
      readMethods(methods);

      Field[] fields = javaClass.getFields();
      FieldDecl[] fieldDecl = getFieldDecl(fields);

      int predict = classMembers.size() + methods.length + fields.length;
      TypeDeclElemVec elementVec = TypeDeclElemVec.make(predict);

      elementVec.append(classMembers);

      // only add routines and fields that are not synthetic.
      this.addNonSyntheticDecls(elementVec, routineDecl);
      this.addNonSyntheticDecls(elementVec, fieldDecl);

      // The synchronized bit for classes is used for other purposes
      int modifiers = javaClass.getModifiers() & ~Constants.ACC_SYNCHRONIZED;

      TypeDecl typeDecl;
      // @ assume classIdentifier != null;
      if ((javaClass.isInterface())) {
         typeDecl = (TypeDecl) InterfaceDecl.make(modifiers, null, classIdentifier,
               interfaceVec, null, elementVec, classLocation, classLocation, classLocation,
               classLocation);
      } else {
         typeDecl = (TypeDecl) ClassDecl.make(modifiers, null, classIdentifier, interfaceVec,
               null, elementVec, classLocation, classLocation, classLocation, classLocation,
               super_class);
      }
      typeDecl.specOnly = true;

      removeExtraArg(typeDecl);

      return typeDecl;
   }

   /**
    * Read fields from BCEL into AST
    * 
    * @param fields
    * @return
    * @throws ClassFormatError
    */
   protected FieldDecl[] getFieldDecl(Field[] fields) throws ClassFormatError {
      int numberOfFieldsInClass = fields.length;
      FieldDecl[] fieldDecl = new FieldDecl[numberOfFieldsInClass];

      for (int loopIndex = 0; loopIndex < numberOfFieldsInClass; loopIndex++) {
         Field field = fields[loopIndex];
         String fieldName = field.getName();
         javafe.ast.Type fieldType = readType(field);
         int fieldModifiers = field.getModifiers();

         fieldDecl[loopIndex] = // @ nowarn IndexTooBig;
         FieldDecl.make(fieldModifiers, null, Identifier.intern(fieldName), fieldType,
               classLocation, null, classLocation);

      }
      return fieldDecl;
   }

   /**
    * Read constants from BCEL into AST
    * 
    * @param constantPool
    * @throws ClassFormatError
    * @requires constantPool != null
    */
   protected void readConstants(ConstantPool constantPool) throws ClassFormatError {
      int numberOfConstants = constantPool.getLength();
      set_num_constants(numberOfConstants);

      for (int loopIndex = 1; loopIndex < numberOfConstants; loopIndex++) {
         Constant constant = constantPool.getConstant(loopIndex);
         byte constantTypeTag = constant.getTag();
         String constantValue = constant.toString();
         set_const(loopIndex, constantTypeTag, constantValue);
      }
   }

   /**
    * Add class methods to abstract syntax tree
    * 
    * @param methods
    * @throws ClassFormatError
    * @throws ClassNotFoundException
    */
   protected void readMethods(Method[] methods) throws ClassFormatError,
         ClassNotFoundException {
      int numberOfMethodsInClass = methods.length;
      routineDecl = new RoutineDecl[numberOfMethodsInClass];
      for (int loopVar = 0; loopVar < numberOfMethodsInClass; loopVar++) {

         Method method = methods[loopVar];
         String nameOfMethod = method.getName();
         String signature = method.getSignature();
         int methodModifiers = method.getModifiers();
         set_method(loopVar, nameOfMethod, signature, methodModifiers);

         // put in a dummy body
         if (includeBodies) {
            routineDecl[loopVar].body = // @ nowarn Null, IndexTooBig;
            BlockStmt.make(StmtVec.make(), classLocation, classLocation);
            routineDecl[loopVar].locOpenBrace = classLocation;
         }

         // Set method attributes
         Attribute[] attributes = method.getAttributes();
         for (int attributeLoopVar = 0; attributeLoopVar < attributes.length; attributeLoopVar++) {

            // Get attribute name tag
            byte attributeTag = getAttributeTag(attributes, attributeLoopVar);

            switch (attributeTag) {

            case Constants.ATTR_EXCEPTIONS:
               ExceptionTable exceptionTable = (ExceptionTable) attributes[attributeLoopVar];

               routineDecl[loopVar].raises = TypeNameVec
                     .make(readExceptionTypeNames(exceptionTable.getExceptionNames()));
               break;

            case Constants.ATTR_SYNTHETIC:
               synthetics.addElement(routineDecl[loopVar]);
               break;
            }
         }
      }

   }

   /**
    * Get the tag of an attribute
    * 
    * @param attributes
    * @param attributeLoopVar
    * @return
    */
   protected byte getAttributeTag(Attribute[] attributes, int attributeLoopVar) {
      Attribute attribute = attributes[attributeLoopVar];
      return attribute.getTag();
   }

   protected int extractLocation(JavaClass javaClass) {
      int loc = javaClass.getClassNameIndex();
      return loc;
   }

   protected ImportDeclVec extractImportDeclVec(JavaClass javaClass) {
      ImportDeclVec importDeclVec = ImportDeclVec.make();
      return importDeclVec;
   }

   protected TypeDeclVec extractTypeDeclVec(JavaClass javaClass) {
      TypeDeclVec typeDeclVec = TypeDeclVec.make();

      return typeDeclVec;
   }

   protected TypeDeclElemVec extractTypeDeclElemVec(JavaClass javaClass) {
      TypeDeclElemVec typeDeclElemVec = TypeDeclElemVec.make();

      return typeDeclElemVec;
   }

   /* -- protected instance methods ----------------------------------------- */

   /**
    * Add only AST nodes that are not synthetic decls to v. nodes should be an array of
    * TypeDeclElems. A synthetic decl is one that had the synthetic attribute, or is a static
    * method decl for an interface.
    */
   protected void addNonSyntheticDecls(/* @ non_null */TypeDeclElemVec v,
   /* @ non_null */TypeDeclElem[] elems, JavaClass javaClass) {
      for (int i = 0; i < elems.length; i++) {
         if (synthetics.contains(elems[i])) { // @ nowarn;
            continue;
         }
         if ((javaClass.isInterface()) && elems[i] instanceof RoutineDecl) {
            RoutineDecl rd = (RoutineDecl) elems[i];
            if (Modifiers.isStatic(rd.modifiers)) {
               continue;
            }
         }
         if (omitPrivateFields && elems[i] instanceof FieldDecl) {
            if (Modifiers.isPrivate(((FieldDecl) elems[i]).modifiers)) {
               continue;
            }
         }
         v.addElement(elems[i]); // @ nowarn Pre;
      }
   }

   protected void set_num_constants(int cnum) throws ClassFormatError {
      constants = new Object[cnum];
      rawConstants = new Object[cnum];
   }

   /**
    * @param i
    * @param ctype
    * @param value
    * @throws ClassFormatError
    */
   protected void set_const(int i, int ctype, Object value) throws ClassFormatError {
      constants[i] = ctype == Constants.CONSTANT_Class ? // @ nowarn
      // IndexTooBig;
      DescriptorParser.parseClass((String) value)
            : value;
      rawConstants[i] = value;
   }

   /**
    * Read the class attributes
    * 
    * @throws IOException
    * @throws ClassFormatError
    * @throws ClassNotFoundException
    */
   protected void readClassAttributes() throws IOException, ClassFormatError,
         ClassNotFoundException {

      Attribute[] classAttributes = javaClass.getAttributes();

      for (int loopVar = 0; loopVar < classAttributes.length; loopVar++) {

         // Get attribute Name
         byte attributeTag = getAttributeTag(classAttributes, loopVar);

         if (attributeTag == Constants.ATTR_SYNTHETIC) {
            syntheticClass = true;
         } else if (attributeTag == Constants.ATTR_INNER_CLASSES) {

            InnerClasses innerClassesAttribute = (InnerClasses) classAttributes[loopVar];
            InnerClass[] innerClasses = innerClassesAttribute.getInnerClasses();

            for (int j = 0; j < innerClasses.length; j++) {
               InnerClass innerClass = innerClasses[j];
               
               // Get inner class name
               int innerNameIndex = innerClass.getInnerNameIndex();
               Constant innerClassNameConstant = constantPool.getConstant(innerNameIndex);
               String innerClassName = innerClassNameConstant.toString();
               int index = innerClassName.lastIndexOf("/");
               String innerClassFileName;
               
               if (index > 0) {
               innerClassFileName = innerClassName.substring(index + 1) + ".class";
                
               } else {
                  innerClassFileName = innerClassName + ".class";
               }
               
               // Parse the inner class
               addInnerClass(innerClassFileName);

            }
         }
      }
   }

   /**
    * Add the inner class to the abstract syntax tree, unless synthetic
    * 
    * @param icfn
    * @throws ClassNotFoundException
    * @throws ClassFormatError
    */
   protected void addInnerClass(String icfn) throws ClassNotFoundException, ClassFormatError {

      BCELReader innerClassReader = readInnerClass(icfn, true);
      TypeDecl innerClassTypeDecl = innerClassReader.getTypeDecl();
      boolean innerClassIsNotSynthetic = !innerClassReader.isSyntheticClass();

      // Add non-synthetic classes
      if (innerClassIsNotSynthetic) {
         classMembers.addElement(innerClassTypeDecl);
      }
   }

   /**
    * Recursively read details of inner class
    * 
    * @param icfn
    * @param avoidSpec
    * @return
    */
   protected BCELReader readInnerClass(String icfn, boolean avoidSpec) {
      BCELReader parser = new BCELReader();
      GenericFile target = new NormalGenericFile(icfn);
      CompilationUnit cu = parser.read(target, avoidSpec);
      return parser;
   }

   /**
    * Set class name
    * 
    * @requires cindex >= 0;
    * @throws ClassFormatError
    */
   protected void set_this_class() throws ClassFormatError {
      // record the class type and synthesize a location for the class binary

      Name className = Name.make(javaClass.getClassName(), 1);

      Name qualifier = getNameQualifier(className);
      Identifier terminal = getNameTerminal(className);
      classPackage = qualifier;
      classIdentifier = terminal;

      DescriptorParser.classLocation = classLocation;
   }

   /**
    * Set super class name
    * 
    * @param cindex
    * @throws ClassFormatError
    */
   protected void set_super_class(int cindex) throws ClassFormatError {

      String superClassName = javaClass.getSuperclassName();

      Name name = Name.make(superClassName, 1);
      super_class = TypeName.make(name);
   }

   /**
    * Call back from ClassFileParser.
    */
   protected void set_num_interfaces(int n) throws ClassFormatError {
      typeNames = new TypeName[n];
   }

   /**
    * Call back from ClassFileParser.
    */
   protected void set_interface(int index, int cindex) throws ClassFormatError {
      typeNames[index] = (TypeName) constants[cindex]; // @ nowarn
      // Cast,IndexTooBig;
   }

   /**
    * Convert BCEL field type to AST field type
    * 
    * @param fieldType
    * @param field
    * @return astType
    */
   protected javafe.ast.Type readType(Field field) {

      int typeTag;
      javafe.ast.Type astType;
      Type fieldType = field.getType();

      switch (fieldType.getType()) {
      case Constants.T_BOOLEAN:
         typeTag = TagConstants.BOOLEANTYPE;
         break;

      case Constants.T_BYTE:
         typeTag = TagConstants.BYTETYPE;
         break;

      case Constants.T_INT:
         typeTag = TagConstants.INTTYPE;
         break;

      case Constants.T_LONG:
         typeTag = TagConstants.LONGTYPE;
         break;

      case Constants.T_VOID:
         typeTag = TagConstants.VOIDTYPE;
         break;

      case Constants.T_DOUBLE:
         typeTag = TagConstants.DOUBLETYPE;
         break;

      case Constants.T_ARRAY:
         typeTag = TagConstants.ARRAYTYPE;
         break;

      case Constants.T_FLOAT:
         typeTag = TagConstants.FLOATTYPE;
         break;

      case Constants.T_SHORT:
         typeTag = TagConstants.SHORTTYPE;
         break;

      case Constants.T_OBJECT:
      default:
         typeTag = TagConstants.NULLTYPE;
      }

      // Non primitive types need to be parsed using full type name
      switch (typeTag) {

      case TagConstants.ARRAYTYPE:
      case TagConstants.NULLTYPE:
         String typeSignature = field.getType().getSignature();
         astType = DescriptorParser.parseField(typeSignature);
         break;

      // Primitive types
      default:
         astType = JavafePrimitiveType.make(typeTag, classLocation);
      }

      return astType;

   }

   /**
    * Parse the type name of a field which is an object
    * 
    * @param field
    * @return
    */
   protected javafe.ast.Type readObjectType(Field field) {

      String fieldClassName = field.getClass().getName();
      TypeName typeName = DescriptorParser.parseClass(fieldClassName);
      return typeName;
   }

   protected void set_num_methods(int n) throws ClassFormatError {
      routineDecl = new RoutineDecl[n];
   }

   /**
    * Parse a method
    * 
    * @param i
    * @param mname
    * @param sig
    * @param mod
    * @throws ClassFormatError
    */
   protected void set_method(int i, String mname, String sig, int mod) throws ClassFormatError {
      MethodSignature signature = DescriptorParser.parseMethod(sig);
      FormalParaDeclVec formalVec = FormalParaDeclVec.make(makeFormals(signature));
      BlockStmt body = null;

      if (mname.equals("<init>")) {
         RoutineDecl constructor = (RoutineDecl) ConstructorDecl.make(mod, null, null,
               formalVec, emptyTypeNameVec, body, Location.NULL, classLocation, classLocation,
               classLocation);
         routineDecl[i] = constructor;
      } else {

         RoutineDecl otherMethod = (RoutineDecl) MethodDecl.make(mod, null, null, formalVec,
               emptyTypeNameVec, body, Location.NULL, classLocation, classLocation,
               classLocation, Identifier.intern(mname), signature.getReturn(), classLocation);
         routineDecl[i] = otherMethod;
      }
   }

   /* -- private instance variables ----------------------------------------- */

   /**
    * The input file being parsed.
    */
   /* @ non_null */GenericFile inputFile;

   /**
    * The constant pool of the class being parsed. Initialized by set_num_constants. Elements
    * initialized by set_const and set_const_ref. Dynamic element types according to constant
    * tag: UTF8 String String String Class TypeName Integer Integer Float Float Long Long
    * Double Double FieldRef null MethodRef null InterfaceMethodRef null
    */
   // @ private invariant constants != null;
   // @ private invariant \typeof(constants) == \type(Object[]);
   private Object[] constants;

   /**
    * The constant pool of the class being parsed. This array contains the constants as they
    * came out of the parser (versus translated by DescriptorParser). Initialized by set_const
    * and set_num_constants.
    */
   // @ private invariant rawConstants != null;
   // @ private invariant \typeof(rawConstants) == \type(Object[]);
   // @ private invariant constants.length == rawConstants.length;
   private Object[] rawConstants;

   /**
    * The type name of the superclass of the class being parsed. Initialized by
    * set_super_class.
    */
   private TypeName super_class;

   /**
    * The type names of the interfaces implemented by the class being parsed. Initialized by
    * set_num_interfaces. Elements initialized by set_interface.
    */
   // @ private invariant interfaces != null;
   // @ private invariant \typeof(interfaces) == \type(TypeName[]);
   private TypeName[] typeNames;

   /**
    * The class members of the class being parsed. Intialized by set_field, set_method, and
    * set_class_attributes.
    */
   // @ invariant classMembers != null;
   TypeDeclElemVec classMembers = TypeDeclElemVec.make(0);

   /**
    * The methods and constructors of the class being parsed. Initialized by set_num_methods.
    * Elements initialized by set_method.
    */
   // @ invariant routines != null;
   // @ invariant \typeof(routines) == \type(RoutineDecl[]);
   // @ spec_public
   private RoutineDecl[] routineDecl;

   /**
    * The identifier of the class being parsed. Initialized by set_this_class.
    */
   // @ spec_public
   private Identifier classIdentifier;

   /* -- private instance methods ------------------------------------------- */

   /**
    * Parse a sequence of exception type names
    * 
    * @param exceptionNames
    * @return an array of type names
    * @throws ClassNotFoundException
    */
   protected TypeName[] readExceptionTypeNames(String[] exceptionNames)
         throws ClassFormatError, ClassNotFoundException {

      int numberOfExceptionsThrown = exceptionNames.length;
      TypeName[] exceptionTypeNames = new TypeName[numberOfExceptionsThrown];

      for (int loopVar = 0; loopVar < numberOfExceptionsThrown; loopVar++) {

         String exceptionClassName = exceptionNames[loopVar];
         exceptionTypeNames[loopVar] = DescriptorParser.parseClass(exceptionClassName);
      }

      return exceptionTypeNames;
   }

   /**
    * Construct a vector of formal parameters from a method signature.
    * 
    * @param signature
    *           the method signature to make the formal parameters from
    * @return the formal parameters
    */
   // @ requires signature != null;
   // @ ensures \nonnullelements(\result);
   // @ ensures \typeof(\result) == \type(FormalParaDecl[]);
   private FormalParaDecl[] makeFormals(MethodSignature signature) {
      int length = signature.countParameters();
      FormalParaDecl[] formals = new FormalParaDecl[length];

      for (int i = 0; i < length; i++) {
         Identifier id = Identifier.intern("arg" + i);
         formals[i] = FormalParaDecl
               .make(0, null, id, signature.parameterAt(i), classLocation);
      }

      return formals;
   }

   /* -- private class methods ---------------------------------------------- */

   /**
    * Return the package qualifier of a given name.
    * 
    * @param name
    *           the name to return the package qualifier of
    * @return the package qualifier of name
    */
   // @ requires name != null;
   private static Name getNameQualifier(Name name) {
      int size = name.size();

      return size > 1 ? name.prefix(size - 1) : null;
      // using null for the unnamed package ???
   }

   /**
    * Return the terminal identifier of a given name.
    * 
    * @param name
    *           the name to return the terminal identifier of
    * @return the terminal identifier of name
    */
   // @ requires name != null;
   private static Identifier getNameTerminal(Name name) {
      return name.identifierAt(name.size() - 1);
   }

   /* -- private class variables -------------------------------------------- */

   /**
    * An empty type name vector.
    */
   // @ invariant emptyTypeNameVec != null;
   // @ spec_public
   private static final TypeNameVec emptyTypeNameVec = TypeNameVec.make();

   protected ConstantPool constantPool;

   protected int classNameIndex;

   /*******************************************************************************************
    * * Test methods: * *
    ******************************************************************************************/

   // @ requires \nonnullelements(args);
   public static void main(String[] args) {
      if (args.length != 1) {
         System.err.println("BCELReader: <source filename>");
         System.exit(1);
      }

      GenericFile target = new NormalGenericFile(args[0]);
      BCELReader reader;
      try {
         reader = new BCELReader();
         CompilationUnit cu = reader.read(target, false);
         if (cu != null)
            PrettyPrint.inst.print(System.out, cu);

      } catch (ClassFormatError e) {
         e.printStackTrace();
      }

   }

   /**
    * Read and parse a binary class file
    */
   public CompilationUnit read(GenericFile target, boolean avoidSpec) {

      this.includeBodies = avoidSpec;
      this.classLocation = Location.createWholeFileLoc(target);

      // Parse the binary class file using the BCEL parser
      ClassParser classParser;
      try {
         readJavaClass(target);

         CompilationUnit compilationUnit = getCompilationUnit();

         return compilationUnit;

      } catch (ClassFormatError e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }
      return null;

   }

   /**
    * Use BCEL to read a binary classfile
    * 
    * @param target
    * @throws IOException
    * @throws ClassFormatException
    */
   protected void readJavaClass(GenericFile target) throws IOException, ClassFormatException {
      InputStream inputStream = target.getInputStream();
      String localName = target.getLocalName();
      ClassParser classParser = new ClassParser(inputStream, localName);
      this.javaClass = classParser.parse();
   }

   /*
    * Begin test
    */

   class InnerTestClass {

      int star() {
         return classLocation;

      }
   }

   {
      int i;
      byte bear;
   }

   static {
   }

   /*
    * End of test
    */

   public boolean isSyntheticClass() {
      return syntheticClass;
   }

}
