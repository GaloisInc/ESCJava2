import java.util.*;



public class SpecTest1_5 {
	//@ requires args[0] == null ;
	//@ ensures args[0] == null;
	public static void main(String [] args) {
		int i = 0;
		i++;
		StringBuffer s = new StringBuffer();
		String a = new String();
		Date d = new Date();
		
	}
	
	java.awt.Color x000 ;
	java.awt.color.ColorSpace x001 ;
	java.awt.event.ActionListener x002 ;
	java.awt.event.FocusAdapter x003 ;
	java.awt.event.FocusListener x004 ;
	java.awt.event.MouseListener x005 ;
	java.io.BufferedInputStream x006 ;
	java.io.ByteArrayInputStream x007 ;
	java.io.ByteArrayOutputStream x008 ;
	java.io.CharConversionException x009 ;
	java.io.EOFException x010 ;
	java.io.File x011 ;
	java.io.FileFilter x012 ;
	java.io.FileInputStream x013 ;
	java.io.FilenameFilter x014 ;
	java.io.FileNotFoundException x015 ;
	java.io.FileOutputStream x016 ;
	java.io.FilterInputStream x017 ;
	java.io.FilterOutputStream x018 ;
	java.io.InputStream x019;
	java.io.InputStreamReader x020 ;
	java.io.InterruptedIOException x021 ;
	java.io.InvalidClassException x022 ;
	java.io.IOException x023 ;
	java.io.NotActiveException x024 ;
	java.io.NotSerializableException x025 ;
	java.io.ObjectStreamException x026 ;
	java.io.OptionalDataException x027 ;
	java.io.OutputStream x028 ;
	java.io.OutputStreamWriter x029 ;
	java.io.PrintStream x030 ;
	java.io.PrintWriter x031 ;
	java.io.Reader x032 ;
	java.io.Serializable x033 ;
	java.io.StreamCorruptedException x034 ;
	java.io.StringWriter x035 ;
	java.io.SyncFailedException x036 ;
	java.io.UnsupportedEncodingException x037 ;
	java.io.UTFDataFormatException x038 ;
	java.io.WriteAbortedException x039 ;
	java.lang.AbstractMethodError x040 ;
	java.lang.ArithmeticException x041 ;
	java.lang.ArrayIndexOutOfBoundsException x042;
	java.lang.ArrayStoreException x043;
	java.lang.AssertionError x044;
	java.lang.Boolean x045;
	java.lang.Byte x046;
	java.lang.Character x047;
	java.lang.CharSequence x048;
	java.lang.Class x049;
	java.lang.ClassCastException x050;
	java.lang.ClassNotFoundException x051;
	java.lang.Cloneable x052;
	java.lang.CloneNotSupportedException x053;
	java.lang.Comparable x054;
	java.lang.Double x055;
	java.lang.Error x056;
	java.lang.Exception x057;
	java.lang.Float x058 ;
	java.lang.IllegalAccessException x059 ;
	java.lang.IllegalArgumentException x060 ;
	java.lang.IllegalMonitorStateException x061 ;
	java.lang.IllegalStateException x062 ;
	java.lang.IllegalThreadStateException x063 ;
	java.lang.IndexOutOfBoundsException x064 ;
	java.lang.InstantiationException x065 ;
	java.lang.Integer x066 ;
	java.lang.InternalError x067 ;
	java.lang.InterruptedException x068 ;
	java.lang.Long x069 ;
	java.lang.Math x070 ;
	java.lang.NegativeArraySizeException x071 ;
	java.lang.NoSuchFieldException x072 ;
	java.lang.NoSuchMethodException x073 ;
	java.lang.NullPointerException x074 ;
	java.lang.Number x075 ;
	java.lang.NumberFormatException x076 ;
	java.lang.Object x077 ;
	java.lang.Package x078 ;
	java.lang.reflect.Array x079 ;
	java.lang.reflect.Constructor x080 ;
	java.lang.reflect.Field x081 ;
	java.lang.reflect.InvocationTargetException x082 ;
	java.lang.reflect.Method x083 ;
	java.lang.reflect.UndeclaredThrowableException x084 ;
	java.lang.Runnable x085 ;
	java.lang.RuntimeException x086 ;
	java.lang.SecurityException x087 ;
	java.lang.SecurityManager x088 ;
	java.lang.Short x089 ;
	java.lang.StackTraceElement x090 ;
	java.lang.StrictMath x091 ;
	java.lang.String x092 ;
	java.lang.StringBuffer x093 ;
	java.lang.StringIndexOutOfBoundsException x095 ;
	java.lang.System x096 ;
	java.lang.Throwable x097 ;
	java.lang.UnsupportedOperationException x098 ;
	java.lang.VirtualMachineError x099 ;
	java.lang.Void x100 ;
	
	java.math.BigInteger x101 ;
	java.net.BindException x102 ;
	java.net.ConnectException x103 ;
	java.net.MalformedURLException x104;
	java.net.NoRouteToHostException x105;
	java.net.PortUnreachableException x106;
	java.net.ProtocolException x107;
	java.net.SocketException x108;
	java.net.SocketTimeoutException x109;
	java.net.UnknownHostException x110 ;
	java.net.UnknownServiceException x111 ;
	java.net.URI x112 ;
	java.net.URISyntaxException x113 ;
	java.util.AbstractCollection x114 ;
	java.util.AbstractList x115 ;
	java.util.AbstractMap x116 ;
	java.util.AbstractSequentialList x117 ;
	java.util.AbstractSet x118 ;
	java.util.ArrayList x119 ;
	java.util.Arrays x120 ;
	java.util.BitSet x121 ;
	java.util.Calendar x122 ;
	java.util.Collection x123 ;
	java.util.Comparator x124 ;
	java.util.ConcurrentModificationException x125 ;
	java.util.Date x126 ;
	java.util.Dictionary x127 ;
	java.util.EmptyStackException x128;
	java.util.Enumeration x129;
	java.util.GregorianCalendar x130;
	java.util.HashMap x131;
	java.util.Hashtable x132;
	java.util.Iterator x133;
	java.util.LinkedList x134;
	java.util.List x135;
	java.util.ListIterator x136;
	java.util.Locale x137;
	java.util.Map x138 ;
	java.util.MissingResourceException x139 ;
	java.util.NoSuchElementException x140 ;
	java.util.Observable x141 ;
	java.util.Observer x142 ;
	java.util.Properties x143 ;
	java.util.Random x144 ;
	java.util.RandomAccess x145 ;
	java.util.regex.Matcher x146 ;
	java.util.regex.Pattern x147 ;
	java.util.ResourceBundle x148 ;
	java.util.Set x149 ;
	java.util.TooManyListenersException x150 ;
	java.util.Vector x151 ;
	javax.swing.event.MouseInputAdapter x152 ;
	javax.swing.filechooser.FileFilter x153 ;
	//javax.swing.tree.DefaultTreeCellRenderer x154 ;
	javax.swing.tree.TreeCellRenderer x155 ;
}
