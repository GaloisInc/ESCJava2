package decsrc.util;

/** Class file parsing constants.  See the JVM specification for
    more details. */
public interface ClassFileConstants {
    // Class file properties
    public static final int MAGIC = 0xCAFEBABE;
    public static final int MAJOR = 45;
    public static final int MINOR = 3;

    // Constant pool types
    public static final int CONSTANT_Unused	= 0;
    public static final int CONSTANT_Utf8	= 1;
    public static final int CONSTANT_Integer	= 3;
    public static final int CONSTANT_Float	= 4;
    public static final int CONSTANT_Long	= 5;
    public static final int CONSTANT_Double	= 6;
    public static final int CONSTANT_Class	= 7;
    public static final int CONSTANT_String	= 8;
    public static final int CONSTANT_Fref	= 9;
    public static final int CONSTANT_Mref	= 10;
    public static final int CONSTANT_IMref	= 11;
    public static final int CONSTANT_NameType	= 12;

    // Constant pool flags
    public static final int ACC_PUBLIC		= 0x0001;
    public static final int ACC_PRIVATE		= 0x0002;
    public static final int ACC_PROTECTED	= 0x0004;
    public static final int ACC_STATIC		= 0x0008;
    public static final int ACC_FINAL		= 0x0010;
    public static final int ACC_SYNCHRONIZED	= 0x0020;
    public static final int ACC_SUPER		= 0x0020; // Same as sync
    public static final int ACC_VOLATILE	= 0x0040;
    public static final int ACC_TRANSIENT	= 0x0080;
    public static final int ACC_NATIVE		= 0x0100;
    public static final int ACC_INTERFACE	= 0x0200;
    public static final int ACC_ABSTRACT	= 0x0400;
}
