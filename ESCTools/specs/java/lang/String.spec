// @(#)$Id$

// Copyright (C) 2000 Iowa State University

// This file is part of JML

// JML is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// JML is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with JML; see the file COPYING.  If not, write to
// the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.

package java.lang;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Comparator;

/** JML's specification of java.lang.String.
 * @version $Revision$
 * @author Gary T. Leavens
 * @author Erik Poll 
 */
public final class String
    implements java.io.Serializable, Comparable, CharSequence
{

    public static final Comparator CASE_INSENSITIVE_ORDER;

    //---------------------------------------------------------------------
    // Constructors (and their helpers)
    //---------------------------------------------------------------------

    /*@  public normal_behavior
      @   assignable objectState;
      @*/
    public /*@ pure @*/ String();

    /*@  public normal_behavior
      @   requires original != null;
      @   assignable objectState;
      @*/
    public /*@ pure @*/ String(String original);

    /*@  public normal_behavior
      @   requires value != null;
      @   assignable objectState;
      @   ensures (\forall int i; 0 <= i && i < value.length; charAt(i) == value[i]);
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ char value[]);

    /*@  public normal_behavior
      @   requires value != null
      @             && 0 <= offset 
      @             && (offset + count) < value.length
      @             && 0 <= count;
      @   assignable objectState;
      @   ensures (\forall int i; offset <= i && i < offset + count; charAt((int)(i - offset)) == value[i]);
      @  also
      @   public exceptional_behavior
      @    requires value != null
      @              && (offset < 0
      @                  || (offset + count) < value.length
      @                  || count < 0);
      @    signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ char value[],
                                 int offset, int count)
        throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   ensures \result == (char) (((hibyte & 0xff) << 8) | (ascii & 0xff));
    public pure model char byteToChar(int hibyte, byte ascii); @*/

    /** @deprecated as of 1.1 */
    /*@  public normal_behavior
      @   requires ascii != null
      @             && 0 <= offset 
      @             && (offset + count) < ascii.length
      @             && 0 <= count;
      @   assignable objectState;
      @  ensures (\forall int i; offset <= i && i < offset + count; charAt((int)(i - offset)) == byteToChar(hibyte, ascii[i]));
      @ also
      @  public exceptional_behavior
      @   requires ascii != null
      @             && (offset < 0
      @                 || (offset + count) < ascii.length
      @                 || count < 0);
      @   signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ byte ascii[], int hibyte,
                  int offset, int count)
        throws StringIndexOutOfBoundsException;
  
    /** @deprecated as of 1.1 */
    /*@  public normal_behavior
      @   requires ascii != null;
      @   assignable objectState;
      @  ensures (\forall int i;  0 <= i && i < ascii.length;
      @                 charAt(i)
      @                 == byteToChar(hibyte, ascii[i]));
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ byte ascii[], int hibyte);

    /*@ public behavior
      @  requires bytes != null && charsetName != null
      @             && 0 <= offset 
      @             && (offset + length) < bytes.length
      @             && 0 <= length;
      @  {|
      @     requires (* charsetName is the name of a supporting encoding *);
      @     assignable objectState;
      @     signals (Exception) false;
      @   also
      @     requires (* !(charsetName is the name of a supporting encoding) *);
      @     ensures false;
      @     signals (UnsupportedEncodingException);
      @  |}
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ byte bytes[],
                                 int offset, int length,
                                 /*@ non_null @*/ String charsetName)
        throws UnsupportedEncodingException;

    /*@  public normal_behavior
      @   requires bytes != null && charsetName != null
      @            && (* charsetName is the name of a supporting encoding *);
      @   assignable objectState;
      @ also
      @  public exceptional_behavior
      @   requires bytes != null && charsetName != null
      @            && (* !(charsetName is the name of a supporting encoding) *);
      @   signals (UnsupportedEncodingException);
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ byte bytes[],
                                 /*@ non_null @*/ String charsetName)
        throws UnsupportedEncodingException;

    /*@  public normal_behavior
      @   requires bytes != null
      @             && 0 <= offset 
      @             && (offset + length) < bytes.length
      @             && 0 <= length;
      @   assignable objectState;
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ byte bytes[],
                                 int offset, int length);

    /*@  public normal_behavior
      @   requires bytes != null;
      @   assignable objectState;
      @*/
    public /*@ pure @*/ String(/*@ non_null @*/ byte bytes[]);

    /*@  public normal_behavior
      @  requires buffer != null;
      @  assignable objectState;
      @*/
    public /*@ pure @*/ String (/*@ non_null @*/ StringBuffer buffer);

    String(int offset, int count, char[] value);

    //---------------------------------------------------------------------
    // Methods
    //---------------------------------------------------------------------

    //@ also
    /*@ public normal_behavior
      @    ensures \result >= 0;
      @*/
    public /*@ pure @*/ int length();
/*@
      @ also
      @  public normal_behavior
      @   requires 0 <= index && index < length();
      @ also
      @  public exceptional_behavior
      @   requires index < 0 || index >= length();
      @   signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ char charAt(int index)
        throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   requires srcBegin >= 0
      @             && srcEnd < length()
      @             && srcBegin < srcEnd
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin) + 1;
      @   ensures (\forall int i; srcBegin <= i && i < srcEnd;
      @                           dst[(int)(dstBegin + i - srcBegin)] == charAt(i));
      @ also
      @  public exceptional_behavior
      @   requires (srcBegin <= 0
      @              || srcEnd > length()
      @              || srcBegin >= srcEnd)
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin) + 1;
      @   signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ void getChars(int srcBegin, int srcEnd,
                                        char dst[], int dstBegin)
      throws StringIndexOutOfBoundsException;

    /** @deprecated as of 1.1, use getBytes() */
    /*@  public normal_behavior
      @   requires srcBegin >= 0
      @             && srcEnd < length()
      @             && srcBegin < srcEnd
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin) + 1;
      @   ensures (\forall int i; i >= srcBegin && i < srcEnd;
      @                            dst[(int)(dstBegin + i - srcBegin)]
      @                            == (byte) (charAt(i) & 0xff));
      @ also
      @  public exceptional_behavior
      @   requires (srcBegin <= 0
      @              || srcEnd > length()
      @              || srcBegin >= srcEnd)
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin) + 1;
      @   signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ void getBytes(int srcBegin, int srcEnd,
                                        byte dst[], int dstBegin)
      throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   ensures \result == (a.length == b.length)
      @                       && (\forall int i; 0 <= i && i < a.length;
                                                 a[i] == b[i]);
    public pure model boolean byteArraysEqual(byte[] a, byte[] b);   @*/

    /*@  public normal_behavior
      @   requires charsetName != null 
      @             && (* charsetName is the name of a supporting encoding *);
      @   ensures \result != null 
      @            && (\forall int i; 0 <= i && i < \result.length;
      @                    (* \result[i] is the byte at position i of the 
      @                       conversion of this string's chars using charsetName *));
      @ also
      @  public exceptional_behavior
      @   requires charsetName != null 
      @             && (* !(charsetName is the name of a supporting encoding) *);
      @   signals (UnsupportedEncodingException);
      @*/
    public /*@ pure @*/ /*@ non_null @*/ byte[] getBytes(String charsetName)
      throws UnsupportedEncodingException;

    /*@  public normal_behavior
      @  ensures \result != null;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ byte[] getBytes();

    /*@ also  public normal_behavior
      @   requires anObject != null && (anObject instanceof String);
      @ also
      @   requires anObject == null || !(anObject instanceof String);
      @   ensures !\result;
      @*/
    public /*@ pure @*/ boolean equals(Object anObject);

    /*@ public normal_behavior
      @   requires sb != null;
      @   ensures \result <==> length() == sb.length()
      @        && (\forall int i; 0 <= i && i < length();
      @               charAt(i) == sb.charAt(i));
      @*/
    public /*@ pure @*/ boolean contentEquals(StringBuffer sb);

    /*@  public normal_behavior
      @   requires anotherString != null;
      @   ensures this.equals(anotherString) ==> \result;
      @ also
      @   requires anotherString == null;
      @   ensures !\result;
      @*/
    public /*@ pure @*/ boolean equalsIgnoreCase(String anotherString);

    /*@  public normal_behavior
      @   requires anotherString != null;
      @   ensures \result == 0 <==> this.equals(anotherString);
      @*/
    public /*@ pure @*/ int compareTo(String anotherString);

    /*@ also
      @   public normal_behavior
      @     requires o != null && (o instanceof String);
      @     ensures \result == compareTo((String) o);
      @ also
      @   public exceptional_behavior
      @     requires o == null && !(o instanceof String);
      @     signals (Exception e) e instanceof ClassCastException;
      @*/
    public /*@ pure @*/ int compareTo(Object o);

    /*@  public normal_behavior
      @   requires str != null;
      @   ensures this.equals(str) ==> \result==0;
      @*/
    public /*@ pure @*/ int compareToIgnoreCase(String str);

    /*@  public normal_behavior
      @   requires other != null;
      @   ensures \result == regionMatches(false,toffset,other,ooffset,len);
      @*/
    public /*@ pure @*/ boolean regionMatches(int toffset, String other, 
                                 int ooffset, int len);

    /*@ public normal_behavior
      @  requires other != null;
      @  {|
      @   requires (0 <= toffset && (toffset + len) <= length())
      @             && (0 <= ooffset && (ooffset + len) < other.length())
      @             && ignoreCase;
      @   ensures \result == substring(toffset, (int)(toffset + len)).equalsIgnoreCase(
      @                        other.substring(ooffset, (int)(ooffset + len)));
      @  also
      @   requires (0 <= toffset && (toffset + len) <= length())
      @             && (0 <= ooffset && (ooffset + len) < other.length())
      @             && !ignoreCase;
      @   ensures \result == substring(toffset, (int)(toffset + len))
      @                        .equals(other.substring(ooffset, (int)(ooffset + len)));
      @  also
      @   requires (toffset < 0 || (toffset + len) > length())
      @             || (toffset < 0 || (ooffset + len) > other.length());
      @   ensures !\result;
      @  |}
      @*/
    public /*@ pure @*/ boolean regionMatches(boolean ignoreCase,
                                                int toffset, String other, 
                                                int ooffset, int len);

    /*@  public normal_behavior
      @   requires prefix != null && toffset < length();
      @  also
      @   requires prefix != null && toffset >= length();
      @   ensures !\result;
      @*/
    public /*@ pure @*/ boolean startsWith(String prefix, int toffset);

    /*@  public normal_behavior
      @   requires prefix != null;
      @   ensures \result == startsWith(prefix, 0);
      @*/
    public /*@ pure @*/ boolean startsWith(String prefix);

    /*@  public normal_behavior
      @   requires suffix != null && suffix.length() <= length();
      @   ensures \result == substring((int)(length() - suffix.length()))
      @                        .equals(suffix);
      @  also
      @   requires suffix != null && suffix.length() > length();
      @   ensures !\result;
      @*/
    public /*@ pure @*/ boolean endsWith(String suffix);

    // specification is inherited, this method does have side effects!
    public int hashCode();

    /*@  public normal_behavior
      @   ensures \result == indexOf(ch, 0);
      @*/
    public /*@ pure @*/ int indexOf(int ch);

    // behavior is not described if fromIndex >= length() but this
    // specification reflects the implementation
    /*@  public normal_behavior
      @   requires fromIndex >= length();
      @   ensures \result == -1;
      @  also
      @   requires fromIndex < 0;
      @   ensures \result == indexOf(ch, 0);
      @  also
      @   requires 0 <= fromIndex && fromIndex < length();
      @   {|
      @     requires charAt(fromIndex) == ch;
      @     ensures \result == fromIndex;
      @   also
      @     requires charAt(fromIndex) != ch;
      @     ensures \result == indexOf(ch, (int)(fromIndex + 1));
      @   |}
      @*/
    public /*@ pure @*/ int indexOf(int ch, int fromIndex);

    /*@  public normal_behavior
      @   ensures \result == lastIndexOf(ch, (int)(length() - 1));
      @*/
    public /*@ pure @*/ int lastIndexOf(int ch);

    // behavior is not described if fromIndex >= length() but this
    // specification reflects the implementation
    /*@ public normal_behavior
      @ {|
      @   requires fromIndex >= length();
      @   ensures \result == lastIndexOf(ch, (int)(length() - 1));
      @ also
      @   requires fromIndex < 0;
      @   ensures \result == -1;
      @ also
      @   requires 0 <= fromIndex && fromIndex < length();
      @   {|
      @     requires charAt(fromIndex) == ch;
      @     ensures \result == fromIndex;
      @   also
      @     requires charAt(fromIndex) != ch;
      @     ensures \result == lastIndexOf(ch, (int)(fromIndex - 1));
      @   |}
      @ |}
      @*/
    public /*@ pure @*/ int lastIndexOf(int ch, int fromIndex);

    /*@ public normal_behavior
      @  requires str != null;
      @  ensures \result == indexOf(str, 0);
      @*/
    public /*@ pure @*/ int indexOf(String str);

    // behavior is not described if fromIndex >= length() but this
    // specification reflects the implementation
    /*@  public normal_behavior
      @   requires str != null;
      @   {|
      @     requires fromIndex >= length();
      @     ensures \result == -1;
      @   also
      @     requires fromIndex < 0;
      @     ensures \result == indexOf(str, 0);
      @   also
      @     requires 0 <= fromIndex && fromIndex < length();
      @      ensures \result != -1 ==>
      @               ((\result >= fromIndex || \result>= str.length()) 
      @                 && this.startsWith(str, \result));
      @   |}
      @*/
    public /*@ pure @*/ int indexOf(String str, int fromIndex);

    static /*@ pure @*/
        int indexOf(char[] source, int sourceOffset, int sourceCount,
                    char[] target, int targetOffset, int targetCount,
                    int fromIndex);
      
    /*@  public normal_behavior
      @   requires str != null;
      @   ensures \result == lastIndexOf(str, (int)(length() - 1));
      @*/
    public /*@ pure @*/ int lastIndexOf(String str);

    // behavior is not described if fromIndex >= length() but this
    // specification reflects the implementation
    /*@  public normal_behavior
      @   requires str != null;
      @   {|
      @     requires fromIndex >= length();
      @     ensures \result == lastIndexOf(str, (int)(length() - 1));
      @   also
      @     requires fromIndex < 0;
      @     ensures \result == -1;
      @   also
      @     requires 0 <= fromIndex && fromIndex < length();
      @      ensures \result != -1 ==>
      @               ((\result >= fromIndex || \result>= str.length()) 
      @                 && this.startsWith(str, \result));
      @   |}
      @*/
    public /*@ pure @*/ int lastIndexOf(String str, int fromIndex);
      
    static /*@ pure @*/
        int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                        char[] target, int targetOffset, int targetCount,
                        int fromIndex);

    /*@  public normal_behavior
      @   requires beginIndex < length();
      @   ensures \result == substring(beginIndex, length());
      @ also
      @  public exceptional_behavior
      @   requires beginIndex >= length();
      @   signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String substring(int beginIndex)
      throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   requires 0 <= beginIndex
      @             && beginIndex < endIndex
      @             && (endIndex <= length());
      @   ensures \result != null;
      @ also
      @  public exceptional_behavior
      @   requires 0 > beginIndex
      @             || beginIndex >= endIndex
      @             || (endIndex > length());
      @   signals (StringIndexOutOfBoundsException);
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String substring(int beginIndex,
                                                            int endIndex)
      throws StringIndexOutOfBoundsException;

    public /*@ pure @*/ CharSequence subSequence(int beginIndex,
                                                   int endIndex);

    /*@  public normal_behavior
      @   requires str != null;
      @   ensures \result != null;
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String concat(/*@ non_null @*/ String str);

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.length() == length()
      @            && (\forall int i; 0 <= i && i < length();
      @                  \result.charAt(i) 
      @                     == ((charAt(i) == oldChar) ? newChar : charAt(i)));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String replace(char oldChar,
                                                          char newChar);

    /*@ public normal_behavior
      @     requires regex != null;
      @     ensures \result <==> Pattern.matches(regex, this);
      @*/
    public /*@ pure @*/ boolean matches(/*@ non_null @*/ String regex);

    /*@ public normal_behavior
      @     requires regex != null && replacement != null;
      @     ensures \result.equals(
      @               Pattern.compile(regex).matcher(this)
      @                      .replaceFirst(replacement));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String replaceFirst(/*@ non_null @*/ String regex,
                            /*@ non_null @*/ String replacement);

    /*@ public normal_behavior
      @     requires regex != null && replacement != null;
      @     ensures \result.equals(
      @               Pattern.compile(regex).matcher(this)
      @                      .replaceAll(replacement));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String replaceAll(/*@ non_null @*/ String regex,
                          /*@ non_null @*/ String replacement);

    /*@ public normal_behavior
      @     requires regex != null;
      @     ensures \result.equals(Pattern.compile(regex).split(this, limit));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String[] split(/*@ non_null @*/ String regex, int limit);

    /*@ public normal_behavior
      @     requires regex != null;
      @     ensures \result.equals(split(regex,0));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String[] split(/*@ non_null @*/ String regex);

    /*@  public normal_behavior
      @   requires locale != null;
      @   ensures (* \result == a lower case conversion of this using the 
      @                          rules of the given locale *);
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String toLowerCase(/*@ non_null @*/ Locale locale);

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.equals(toLowerCase(Locale.getDefault()));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String toLowerCase();

    /*@  public normal_behavior
      @   requires locale != null;
      @   ensures (* \result == an upper case conversion of this using the 
      @                          rules of the given locale *);
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String toUpperCase(/*@ non_null @*/ Locale locale);

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.equals(toUpperCase(Locale.getDefault()));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String toUpperCase();

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.length() <= length()
      @            && \result.charAt(0) > ' '
      @            && \result.charAt((int)(\result.length() - 1)) > ' ';
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String trim();

    /*@ also
      @  public normal_behavior
      @    ensures \result != null && \result == this;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String toString();
    
    /*@  public normal_behavior
      @    ensures \result != null
      @          && \result.length == length()
      @          && (\forall int i; 0 <= i && i < length();
      @                             \result[i] == charAt(i));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ char[] toCharArray();

    /*@  public normal_behavior
      @  {|
      @    requires obj == null;
      @    ensures \result != null && \result.equals("null");
      @  also
      @    requires obj != null;
      @    ensures \result != null;
      @  |}
      @  also
      @    public model_program {
      @       assume obj != null;
      @       return obj.toString();
      @    }
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(Object obj);

    /*@  public normal_behavior
      @  requires data != null;
      @   ensures \result != null && \result.equals(new String(data));
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String valueOf(/*@ non_null @*/ char data[]);

    /*@  public normal_behavior
      @  requires data != null && offset >= 0 && count >= 0
      @       && offset + count < data.length;
      @   ensures \result != null 
      @            && \result.equals(new String(data, offset, count));
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String valueOf(/*@ non_null @*/ char data[],
                                        int offset, int count);

    /*@  public normal_behavior
      @  requires data != null;
      @   ensures \result != null 
      @            && \result.equals(new String(data, offset, count));
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String copyValueOf(/*@ non_null @*/ char data[],
                                            int offset, int count);

    /*@  public normal_behavior
      @  requires data != null;
      @  ensures \result != null && \result.equals(new String(data));
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String copyValueOf(/*@ non_null @*/ char data[]);
        
    /*@  public normal_behavior
      @   ensures \result != null
      @            && (b ==> \result.equals("true")
      @                || !b ==> \result.equals("false"));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(boolean b);

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.length() == 1
      @            && \result.charAt(0) == c;
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(char c);

    /*@  public normal_behavior
      @   ensures \result != null && \result.equals(Integer.toString(i));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(int i);
    
    /*@  public normal_behavior
      @   ensures \result != null && \result.equals(Long.toString(l));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(long l);
    
    /*@  public normal_behavior
      @   ensures \result != null && \result.equals(Float.toString(f));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(float f);
    
    /*@  public normal_behavior
      @   ensures \result != null && \result.equals(Double.toString(d));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(double d);
    
    //@ public model non_null JMLDataGroup stringPool;

    /*@  public normal_behavior
      @   assignable stringPool;
      @   ensures_redundantly (* \result is a canonical representation 
      @                           for this *);
      @*/
    public native /*@ non_null @*/ String intern();

}
