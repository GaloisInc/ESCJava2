/* Copyright 2000, 2001, Compaq Computer Corporation */


package houdini.util;

import java.io.*;
import java.util.*;
 
/**
 * Random useful functions.
 */
public class Utility {

    /**
     * convert a list of strings into an array of strings.
     * Example:
     * <pre><code>String a[] = stringListToArray("moo;cow;dog", ";"); </code></pre>
     * @param s  the string to separate.
     * @param sep that separating character
     * @return an array
     */
    static public String[] stringListToArray(String s, String sep) {
        StringTokenizer st = new StringTokenizer(s, sep, true);
	Vector v = new Vector();
	while (st.hasMoreElements()) {
	    String token = st.nextToken();
	    if (token.equals(sep)) {
		v.addElement("");
	    } else {
		v.addElement(token);
		if (st.hasMoreElements()) {
		    token = st.nextToken();
		    Assert.notFalse(token.equals(sep), "unexpected " + token);
		    if (!st.hasMoreElements()) {
			v.addElement("");
		    }
		} 
	    }
	}
	String array[] = new String[v.size()];
	v.copyInto(array);
	return array;
    } 
    
    /**
     * Looks for a file with name fileName in every dir on the class path.     
     */
    static public File findFileOnClasspath(String fileName) {
        
        // absolute file???
        File f = new File(fileName);
        if (f.exists()) return f;        
        
        String cp = System.getProperty("java.class.path", "");
        String paths[] = stringListToArray(cp, ";");
        for (int i = 0; i < paths.length; i++) {
            f = new File(paths[i], fileName);
            if (f.exists()) return f;
        }
        return null;
    }
        
    /**
     * A general string substitution function.
     * @param s  this string to operate on
     * @param m  the text to match
     * @param t  the replacement text
     * @param pad determines whether or not spaces are put around the replaced text
     */
    static public String replaceString(String s, String m, String t, boolean pad) {
        if (s.indexOf(m) == -1) return s;
        String result = "";
        int prev = 0;
        int next;
        int start = 0;
        while (true) {
            next = s.indexOf(m, start);
            if (next == -1) {
                break;
            }
            result = result + s.substring(prev, next) + t + (pad ? " " : "");
            start = (prev = next + m.length());
        }
        result = result + (prev < s.length() ? s.substring(prev) : "");
        return result;
    }
    
    public boolean testFlag(int value, int flag) {
        return (value & flag) != 0;
    } 
    
    static public String longestCommonSubstring(String s[]) {
        if (s.length == 0) return "";
        String res = s[0];
        for (int i = 1; i < s.length; i++) {
            while (true) {
                if (s[i].startsWith(res)) break;
                if (res.length() == 0) return "";
                res = res.substring(0, res.length() - 1);
            }
        }
        return res;
    }    

    static public String longestCommonSubstring(Vector v) {
        if (v.size() == 0) return "";
        String res = (String)v.elementAt(0);
        for (int i = 1; i < v.size(); i++) {
            while (true) {
                String s = (String)v.elementAt(i);
                if (s.startsWith(res)) break;
                if (res.length() == 0) return "";
                res = res.substring(0, res.length() - 1);
            }
        }
        return res;
    }        
    
    static public void main(String s[]) {
        String t[] = { "cow", "cowabunga", "cop" };
        System.out.println(longestCommonSubstring(t));
    }
    

    /**
     * Return a buffered data input stream for file.
     */
    public static DataInputStream getInputStream(String file) throws IOException {
	return getInputStream(new File(file));
    }  

    /**
     * Return a buffered data input stream for f.
     */
    public static DataInputStream getInputStream(File f) throws IOException {
	return new DataInputStream(new BufferedInputStream(new FileInputStream(f), 32676));
    }  

    
    /**
     * Return a buffered print stream stream for file.
     */
    public static PrintStream getPrintStream(String file) throws IOException {
	return getPrintStream(new File(file));
    } 

   /**
     * Return a buffered print stream stream for f.
     */
    public static PrintStream getPrintStream(File f) throws IOException {
	return new PrintStream(new BufferedOutputStream(new FileOutputStream(f), 32676), true);
    }  

    /**
     * Return a buffered output stream stream for file.
     */
    public static DataOutputStream getOutputStream(String file) throws IOException {
	return getOutputStream(new File(file));
    } 

    /**
     * Return a buffered output stream stream for f.
     */
    public static DataOutputStream getOutputStream(File f) throws IOException {
	return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f), 32676));
    }  

    /**
     * There is an odd problem where srcjava sometimes cannot create a date's string
     * due to what I assume is a library bug.  So, this method tries to make a string
     * a few times and then gives up.
     */
    public static String getDateString() {
	try {
	    return new Date().toString();
	} catch (java.util.MissingResourceException e) {
	    try {
		houdini.util.Assert.notify(e);
		return new Date().toString();
	    } catch (java.util.MissingResourceException e2) {
		houdini.util.Assert.notify(e2);
		return "missing resource- what date is it?";
	    }
	}
    }

}

/*
  static public String[] stringListToArray(String s, String sep) {
        StringTokenizer st = new StringTokenizer(s, sep, false);
        int len = st.countTokens();
        String array[] = new String[len];
        for (int i = 0; i < len; i++) {
            array[i] = st.nextToken();
        }
        return array;
	} */
