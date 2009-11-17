// @(#)$Id: String.spec 3299 2007-12-04 17:41:28Z mikolas $

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
 * @version $Revision: 3299 $
 * @author Gary T. Leavens
 * @author David R. Cok
 * @author Yoosik Cheon
 * @author Clyde Ruby
 * @author Patrice Chalin
 * @author Joseph R. Kiniry
 * @author Mikolas Janota
 */
//-@ immutable 
public final class String
    implements java.io.Serializable, Comparable, CharSequence
{
    // NOTE: The value of the String is modeled by the charArray in CharSequence

    public static final Comparator CASE_INSENSITIVE_ORDER;

    //---------------------------------------------------------------------
    // Constructors (and their helpers)
    //---------------------------------------------------------------------

    //@ initially !isInterned(this);

    /*@  public normal_behavior
      @      ensures initialCharSequence();
      @      ensures charArray.length == 0;
      @      ensures String.equals(this,"");
      @*/
    public /*@ pure @*/ String();

    /*@  public normal_behavior
      @   requires original != null;
      @   ensures initialCharSequence();
      @   ensures equals(this,original);
      @
      @ also public exceptional_behavior
      @   requires original == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ String(String original) throws NullPointerException;

    /*@  public normal_behavior
      @   requires value != null;
      @   ensures initialCharSequence();
      @   ensures equal(charArray,value);
      @
      @ also public exceptional_behavior
      @   requires value == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ String(char[] value);

    /*@
      @  public exceptional_behavior
      @   requires value == null
      @             || (offset < 0
      @                 || (offset + count) > value.length
      @                 || count < 0);
      @   assignable \nothing;
      @   signals_only StringIndexOutOfBoundsException, NullPointerException;
      @   signals (NullPointerException) value == null;
      @   signals (StringIndexOutOfBoundsException) offset < 0 || count < 0 ||
			     (value != null && (offset+count)>value.length);
      @
      @ also
      @  public normal_behavior
      @   requires value != null
      @             && 0 <= offset 
      @             && (offset + count) <= value.length
      @             && 0 <= count;
      @   ensures charArray != null;
      @   ensures equal(charArray,0,value,offset,count);
      @   ensures charArray.length == count;
      @   ensures (offset == 0 && count == value.length) ==> equal(charArray,value);
      @*/
    public /*@ pure @*/ String(char[] value, int offset, int count)
        throws StringIndexOutOfBoundsException, NullPointerException;

    /*@  public normal_behavior
      @   ensures \result == (char) (((hibyte & 0xff) << 8) | (ascii & 0xff));
    public pure model char byteToChar(int hibyte, byte ascii); @*/

    /** @deprecated as of 1.1 */
    /*@  public normal_behavior
      @   requires ascii != null
      @             && 0 <= offset 
      @             && (offset + count) < ascii.length
      @             && 0 <= count;
      @   ensures charArray != null;
      @   ensures charArray.length == count;
      @   ensures (\forall int i; 0 <= i && i < count;
      @               (charArray[i]
      @               == byteToChar(hibyte, ascii[i+offset])));
      @ also
      @  public exceptional_behavior
      @   requires ascii != null
      @             && (offset < 0
      @                 || (offset + count) >= ascii.length
      @                 || count < 0);
      @   signals_only StringIndexOutOfBoundsException;
      @
      @ also public exceptional_behavior
      @   requires ascii == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ String(byte[] ascii, int hibyte,
                  int offset, int count)
        throws StringIndexOutOfBoundsException;
  
    /** @deprecated as of 1.1 */
    /*@  public normal_behavior
      @   requires ascii != null;
      @   ensures charArray != null;
      @   ensures charArray.length == ascii.length;
      @   ensures (\forall int i;  0 <= i && i < ascii.length;
      @             charArray[i] == byteToChar(hibyte, ascii[i]));
      @
      @ also public exceptional_behavior
      @   requires ascii == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ String(byte[] ascii, int hibyte);

    // Define the following to be true (e.g. with an axiom) to declare
    // a given encoding as supported.  FIXME - define this in terms of
    // functionality in java.nio.charset.CharSet.
    //@ public static pure model boolean supportedEncoding(String charsetName);

    /*@ public behavior
      @  requires bytes != null && charsetName != null
      @             && 0 <= offset 
      @             && (offset + length) <= bytes.length
      @             && 0 <= length;
      @     requires supportedEncoding(charsetName);
      @     ensures charArray != null;
      @     ensures charArray.length <= length;
      @     ensures 
      @            (\forall int i; 0 <= i && i < length;
      @                  (* charArray[i] == 
      @                     char at position i of the conversion of subarray
      @                     of bytes using the encoding charsetName *));
      @
      @ also public exceptional_behavior
      @   requires bytes == null || charsetName == null ||
                   !supportedEncoding(charsetName) ||
                   (offset < 0 || length < 0 || offset+length>bytes.length);
      @   assignable \nothing;
      @   signals_only NullPointerException, StringIndexOutOfBoundsException,
                                  UnsupportedEncodingException;
      @   signals (StringIndexOutOfBoundsException e) 
                           offset<0 || length<0 || (offset+length)>bytes.length;
      @   signals (NullPointerException) bytes==null || charsetName == null;
      @   signals (UnsupportedEncodingException) charsetName != null &&
					     !supportedEncoding(charsetName);
      @*/
    public /*@ pure @*/ String(byte[] bytes,
                                 int offset, int length,
                                 String charsetName)
        throws UnsupportedEncodingException;

    /*@
      @  public exceptional_behavior
      @   requires bytes == null || charsetName == null
                   || !supportedEncoding(charsetName);
      @   signals_only NullPointerException, UnsupportedEncodingException;
      @   signals (NullPointerException) bytes==null || charsetName == null;
      @   signals (UnsupportedEncodingException) charsetName != null &&
      @                                     !supportedEncoding(charsetName);
      @
      @
      @ also
      @ public behavior
      @   requires bytes != null && charsetName != null;
      @   requires supportedEncoding(charsetName);
      @   ensures charArray != null;
      @   ensures charArray.length <= bytes.length;
      @   // ensures charArray is the translation of bytes
      @*/
    public /*@ pure @*/ String(byte[] bytes, String charsetName)
        throws UnsupportedEncodingException;

    /*@  public behavior
      @   requires bytes != null
      @             && 0 <= offset 
      @             && (offset + length) <= bytes.length
      @             && 0 <= length;
      @   ensures charArray != null;
      @   ensures charArray.length <= length;
      @   // ensures charArray is the translation of bytes with the 
      @   // default file encoding System.getProperty("file.encoding")
      @
      @ also public exceptional_behavior
      @   requires bytes == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @
      @ also public exceptional_behavior
      @   requires bytes != null && 
                 (offset < 0 || length < 0 || offset+length > bytes.length);
      @   assignable \nothing;
      @   signals_only StringIndexOutOfBoundsException;
      @
      @*/
    public /*@ pure @*/ String(byte[] bytes, int offset, int length);

    /*@  public normal_behavior
      @   requires bytes != null;
      @   ensures charArray != null;
      @   ensures charArray.length <= bytes.length;
      @   // ensures charArray is the translation of bytes with the 
      @   // default file encoding System.getProperty("file.encoding")
      @
      @ also public exceptional_behavior
      @   requires bytes == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ String(byte[] bytes);

    /*@ public normal_behavior
      @   requires buffer != null;
      @   ensures equals(this,buffer.accumulatedString);
      @
      @ also public exceptional_behavior
      @   requires buffer == null;
      @   assignable \nothing;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ String (StringBuffer buffer);


    //---------------------------------------------------------------------
    // Methods
    //---------------------------------------------------------------------

    /*@ public normal_behavior
      @   ensures \result == s.charArray.length;
      @
      @ //-@ function
      @ public static model pure int length(String s);
      @*/

    // inherits specs from CharSequence - \result == charArray.length
    /*@ also public normal_behavior
      @   ensures \result == length(this);
      @*/
    public /*@ pure @*/ int length();

    // This should be derivable, but the relevant formulae are not always triggered
    //@ axiom (\forall String s,ss; s.equals(ss) ==>s.length() == ss.length());

    /*@ also public normal_behavior
      @   requires 0 <= index && index < charArray.length;
      @   ensures \result == charArray[index];
      @ also
      @  public exceptional_behavior
      @   requires index < 0 || index >= charArray.length;
      @   signals_only StringIndexOutOfBoundsException;
      @*/
    public /*@ pure @*/ char charAt(int index)
        throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   requires srcBegin >= 0
      @             && srcEnd <= charArray.length
      @             && srcBegin <= srcEnd
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin);
      @   modifies dst[dstBegin .. dstBegin+srcEnd-srcBegin-1];
      @   ensures equal(charArray,srcBegin,dst,dstBegin,srcEnd-srcBegin);
      @ also
      @  public exceptional_behavior
      @   requires (srcBegin < 0
      @              || srcEnd > charArray.length
      @              || srcBegin > srcEnd)
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin);
      @   modifies \nothing;
      @   signals_only StringIndexOutOfBoundsException;
      @ also
      @  public exceptional_behavior
      @   requires dst == null;
      @   modifies \nothing;
      @   signals_only NullPointerException;
      @*/
    public void getChars(int srcBegin, int srcEnd,
                                        char[] dst, int dstBegin)
      throws StringIndexOutOfBoundsException;

    /** @deprecated as of 1.1, use getBytes() */
    /*@  public normal_behavior
      @   requires srcBegin >= 0
      @             && srcEnd <= length()
      @             && srcBegin <= srcEnd
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin) + 1;
      @   modifies dst[dstBegin .. dstBegin+srcEnd-srcBegin-1];
      @   ensures (\forall int i; srcBegin <= i && i < srcEnd;
      @                            dst[(int)(dstBegin + i - srcBegin)]
      @                            == (byte) (charAt(i) & 0xff));
      @ also
      @  public exceptional_behavior
      @   requires (srcBegin <= 0
      @              || srcEnd > length()
      @              || srcBegin > srcEnd)
      @             && dst != null
      @             && dst.length >= dstBegin + (srcEnd - srcBegin) + 1;
      @   modifies \nothing;
      @   signals (StringIndexOutOfBoundsException);
      @ also
      @  public exceptional_behavior
      @   requires dst == null;
      @   modifies \nothing;
      @   signals_only NullPointerException;
      @*/
    public void getBytes(int srcBegin, int srcEnd,
                                        byte[] dst, int dstBegin)
      throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   ensures \result == (a==b || ((a.length == b.length)
      @                       && (\forall int i; 0 <= i && i < a.length;
                                                 a[i] == b[i])));
    public static pure model boolean byteArraysEqual(byte[] a, byte[] b);   @*/

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
      @             && !(* (charsetName is the name of a supporting encoding) *);
      @   signals_only UnsupportedEncodingException;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ byte[] getBytes(String charsetName)
      throws UnsupportedEncodingException;

    /*@  public normal_behavior
      @  ensures \result != null 
      @           && byteArraysEqual(\result, 
      @                       getBytes(System.getProperty("file.encoding")));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ byte[] getBytes();

    /*@ public normal_behavior
      @   requires s2 != null && s1 != null;
      @   ensures \result == equal(s1.charArray,s2.charArray);
      @ also public normal_behavior
      @   requires s1 == s2;
      @   ensures \result;
      @ also public normal_behavior
      @   requires s1 == null || s2 == null ||
                     (isInterned(s1) && isInterned(s2));
      @   ensures \result <==> (s1 == s2);
      @
      @ pure //-@ function
      @ public static model boolean equals(String s1, String s2);
      @*/

    //@ axiom (\forall String s,ss; equals(s,ss) ==> s.length() == ss.length());

    //@ axiom (\forall String s,ss; equals(s,ss) ==> equals(ss,s));

    //@ axiom (\forall String s1,s2,s3; equals(s1,s2) ==> (equals(s1,s3) <==> equals(s2,s3)));

    /*@ also public normal_behavior
      @   requires anObject != null && (anObject instanceof String);
      @   ensures \result == equals(this,(String)anObject);
      @ also public normal_behavior
      @   requires this == anObject;
      @   ensures \result;
      @ also public normal_behavior
      @   requires anObject == null || !(anObject instanceof String);
      @   ensures !\result;
      @
      @*/
    public /*@ pure @*/ boolean equals(Object anObject);

    /*@ public normal_behavior
      @   requires sb != null;
      @   ensures \result <==> String.equals(this,sb.accumulatedString);
      @
      @ also public exceptional_behavior
      @   requires sb == null;
      @   signals_only NullPointerException;
      @*/
    public /*@ pure @*/ boolean contentEquals(StringBuffer sb);

    /*@  public normal_behavior
      @    ensures \result <==> (c1 == c2)
      @                        || (Character.toUpperCase(c1) 
      @                            == Character.toUpperCase(c2))
      @                        || (Character.toLowerCase(c1) 
      @                            == Character.toLowerCase(c2));
      public static pure model
          boolean charEqualsIgnoreCase(char c1, char c2); @*/

    /*@  public normal_behavior
      @    ensures \result <==> 
      @                         (Character.toLowerCase(c1) 
      @                            < Character.toLowerCase(c2));
      public static pure model
          boolean charLessIgnoreCase(char c1, char c2); @*/

    /*@  public normal_behavior
      @   requires anotherString != null;
      @   ensures equals(this,anotherString) ==> \result;
      @   ensures \result <==> (length() == anotherString.length())
      @                        && (\forall int i;
      @                             0 <= i && i < this.length();
      @                              charEqualsIgnoreCase(
      @                                   charAt(i),
      @                                   anotherString.charAt(i)));
      @ also
      @   requires anotherString == null;
      @   ensures !\result;
      @*/
    //-@ function
    public /*@ pure @*/ boolean equalsIgnoreCase(String anotherString);

    /*@  public normal_behavior
      @   requires s1 != null && s2 != null;
      @   {|
      @     requires s2.length == 0 && s1.length == 0;
      @     ensures !\result;
      @   also
      @     requires s1.length == 0 && s2.length != 0;
      @     ensures \result;
      @   also
      @     requires s1.length != 0 && s2.length == 0;
      @     ensures !\result;
      @   also
      @     requires s1.length > 0 && s2.length > 0 && s1[0] != s2[0];
      @     ensures \result == (s1[0] < s2[0]);
      @   also
      @     ensures \result == (
      @               (\exists int i; 0 <= i && i < s1.length && i < s2.length;
      @                        s1[i] < s2[i] && equal(s1,0,s2,0,i))
      @               || 
      @                 (s1.length < s2.length && equal(s1,0,s2,0,s1.length))
      @               );
      @   |}
    public static pure model boolean lessThan(char[] s1,
                                              char[] s2);  @*/

    /*@  public normal_behavior
      @   requires s1 != null && s2 != null;
      @   {|
      @     requires s2.length == 0 && s1.length == 0;
      @     ensures !\result;
      @   also
      @     requires s1.length == 0 && s2.length != 0;
      @     ensures \result;
      @   also
      @     requires s1.length != 0 && s2.length == 0;
      @     ensures !\result;
      @   also
      @     requires s1.length > 0 && s2.length > 0 
                                 && !charEqualsIgnoreCase(s1[0],s2[0]);
      @     ensures \result == charLessIgnoreCase(s1[0],s2[0]);
      @   also
      @     ensures \result == (
      @               (\exists int i; 0 <= i && i < s1.length && i < s2.length;
      @                        charLessIgnoreCase(s1[i],s2[i]) && 
                               (\forall int j; 0<=j && j<i ;
                                       charEqualsIgnoreCase(s1[j],s2[j])))
      @               || 
      @                 (s1.length < s2.length && 
                               (\forall int j; 0<=j && j<s1.length ;
                                       charEqualsIgnoreCase(s1[j],s2[j])))
      @               );
      @   |}
    public static pure model boolean lessThanIgnoreCase(char[] s1,
                                              char[] s2);  @*/

    /*@ axiom (\forall char[] s1, s2; s1 != null && s2 != null;
                   !equal(s1,s2) ==> (lessThan(s1,s2) <=!=> lessThan(s2,s1)));
     */

    /*@  public normal_behavior
      @   requires anotherString != null;
      @   {|
      @       requires  equal(charArray,anotherString.charArray);
      @       ensures \result == 0;
      @     also
      @       requires  lessThan(charArray,anotherString.charArray);
      @       ensures \result < 0;
      @     also
      @       requires  !lessThan(charArray,anotherString.charArray);
      @       requires  !equal(charArray,anotherString.charArray);
      @       ensures \result > 0;
      @   |}
      @*/
    public /*@ pure @*/ int compareTo(String anotherString);

    /*@ also
      @   public normal_behavior
      @     requires o != null && (o instanceof String);
      @     ensures \result == compareTo((String) o);
      @ also
      @   public exceptional_behavior
      @     requires o == null || !(o instanceof String);
      @     signals_only ClassCastException;
      @*/
    public /*@ pure @*/ int compareTo(Object o);


    /*@  public normal_behavior
      @   requires str != null;
      @   {|
      @       requires equalsIgnoreCase(str);
      @       ensures \result == 0;
      @     also
      @       requires lessThanIgnoreCase(charArray, str.charArray);
      @       ensures \result < 0;
      @     also
      @       requires !lessThanIgnoreCase(charArray, str.charArray)
      @                 && !equalsIgnoreCase(str);
      @       ensures \result > 0;
      @   |}
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
      @             && (0 <= ooffset && (ooffset + len) <= other.length())
      @             && ignoreCase;
      @   ensures \result == substring(toffset, (int)(toffset + len)).equalsIgnoreCase(
      @                        other.substring(ooffset, (int)(ooffset + len)));
      @  also
      @   requires (0 <= toffset && (toffset + len) <= length())
      @             && (0 <= ooffset && (ooffset + len) <= other.length())
      @             && !ignoreCase;
      @   ensures \result == equals(
                            substring(toffset, (int)(toffset + len)),
      @                     other.substring(ooffset, (int)(ooffset + len)));
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
      @   requires prefix != null && 0<=toffset && toffset < length();
      @   ensures \result ==  equal(charArray,toffset,prefix.charArray,0,prefix.charArray.length);
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
      @   ensures \result == equals(
                                substring((int)(length() - suffix.length())),
      @                         suffix);
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
      @   also
      @     ensures \result == -1 <==> (\forall int i; fromIndex<=i && i < charArray.length; charArray[i] != ch);
      @     ensures \result != -1 <==> (fromIndex <= \result && \result < charArray.length);
      @     ensures \result != -1 <==> (
      @                charArray[\result] == ch &&
      @                (\forall int i; fromIndex <= i && i < \result;
      @                     charArray[i] != ch));
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
      @   also
      @     ensures \result == -1 <==> (\forall int i; 0<=i && i <= fromIndex; charArray[i] != ch);
      @     ensures \result != -1 <==> (0 <= \result && \result < fromIndex);
      @     ensures \result != -1 <==> (
      @                charArray[\result] == ch &&
      @                (\forall int i; \result < i && i <= fromIndex;
      @                     charArray[i] != ch));
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
      @     ensures \result == -1 <==>
      @               (\forall int i; 
      @                      fromIndex <= i &&  i + str.length() < length();
      @                      ! equal(charArray,i,str.charArray,0,str.length()));
      @     ensures \result != -1 <==> 
      @            (fromIndex <= \result && \result + str.length() <= length());
      @     ensures \result != -1 <==>
      @              (equal(charArray,\result,str.charArray,0,str.length()) &&
      @               (\forall int i; 
      @                      fromIndex <= i &&  i + str.length() < length();
      @                      !equal(charArray,i,str.charArray,0,str.length())));
      @   |}
      @*/
    public /*@ pure @*/ int indexOf(String str, int fromIndex);

    // a package-visible method - not specified
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
      @     ensures \result == -1 <==>
      @               (\forall int i; 
      @                      0 <= i &&  i <= fromIndex;
      @                      ! equal(charArray,i,str.charArray,0,str.length()));
      @     ensures \result != -1 <==> (0 <= \result && \result <= fromIndex);
      @     ensures \result != -1 <==>
      @              (equal(charArray,\result,str.charArray,0,str.length()) &&
      @               (\forall int i; 
      @                      0 <= i &&  i <= fromIndex;
      @                      !equal(charArray,i,str.charArray,0,str.length())));
      @   |}
      @*/
    public /*@ pure @*/ int lastIndexOf(String str, int fromIndex);
      
    // a package-visible method not specified
    static /*@ pure @*/
        int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                        char[] target, int targetOffset, int targetCount,
                        int fromIndex);


    /*@  public normal_behavior
      @   requires 0 <= beginIndex;
      @   requires beginIndex <= length();
      @   ensures equals(\result, substring(beginIndex, length()));
      @   ensures \fresh(\result);
      @   ensures_redundantly (beginIndex == 0) ==> equals(\result, this);
      @ also
      @  public exceptional_behavior
      @   requires beginIndex < 0 || beginIndex > length();
      @   signals_only StringIndexOutOfBoundsException;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String substring(int beginIndex)
      throws StringIndexOutOfBoundsException;

    /*@  public normal_behavior
      @   requires 0 <= beginIndex
      @             && beginIndex <= endIndex
      @             && (endIndex <= length());
      @   ensures \result != null
      @            && \fresh(\result)
      @            && \result.length() == endIndex - beginIndex
      @            && equal(\result.charArray,0,charArray,beginIndex,
                                                    endIndex-beginIndex);
      @   ensures beginIndex ==0 && endIndex == length() ==>
                                        equals(this,\result);
      @ also
      @  public exceptional_behavior
      @   requires 0 > beginIndex
      @             || beginIndex > endIndex
      @             || (endIndex > length());
      @   signals_only StringIndexOutOfBoundsException;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String substring(int beginIndex,
                                                            int endIndex)
      throws StringIndexOutOfBoundsException;

    /*@ 
      @   // inherits normal behavior
      @ also
      @  public exceptional_behavior
      @   requires 0 > beginIndex
      @             || beginIndex > endIndex
      @             || (endIndex > length());
      @   signals_only StringIndexOutOfBoundsException;
      @*/
    public /*@ pure @*/ CharSequence subSequence(int beginIndex,
                                                   int endIndex);

    /*@ public normal_behavior
      @   requires s != null && ss != null;
      @   ensures equal(\result.charArray,0,s.charArray,0,s.length());
      @   ensures equal(\result.charArray,s.length(),ss.charArray,0,ss.length());
      @
      @ //-@ function
      @ public static pure model non_null
      @                                 String concat(String s, String ss);
      @
      @
      @ public normal_behavior
      @   ensures \result == (s == null ? "null" : s) ;
      @
      @ //-@  function
      @ model static public pure non_null String nonnull(String s);
      @
      @
      @ public normal_behavior
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \fresh(\result);
      @   ensures !isInterned(\result);
      @   ensures \result.equals( concat(nonnull(s),nonnull(ss)));
      @
      @ //-@ function
      @ model static public non_null pure String 
      @                                  _infixConcat_(String s, String ss);
      @*/
 

    /*@ public normal_behavior
      @   requires str != null;
      @   {|
      @   requires str.length() != 0;
      @   assignable \nothing;
      @   ensures \result != null
      @            && \fresh(\result)
      @            && !isInterned(\result)
      @            && \result.equals(concat(this,str));
      @   also
      @   requires str.length() == 0;
      @   assignable \nothing;
      @   ensures \result != null
      @            && \result == this
      @            && \result.length() == length();
      @   |}
      @ also public exceptional_behavior
      @    requires str == null;
      @   assignable \nothing;
      @    signals_only NullPointerException;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String concat(String str);

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.length() == length()
      @            && \fresh(\result)
      @            && (\forall int i; 0 <= i && i < length();
      @                  \result.charAt(i) 
      @                     == ((charAt(i) == oldChar) ? newChar : charAt(i)));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String replace(char oldChar,
                                                          char newChar);

    /* FIXME +@ public normal_behavior
      @     requires regex != null;
      @     assignable \nothing;
      @     ensures \result <==> Pattern.matches(regex, this);
      @*/
    public /*@ pure @*/ boolean matches(/*@ non_null @*/ String regex);

    /* FIXME +@ public normal_behavior
      @     requires regex != null && replacement != null;
      @     assignable \nothing;
      @     ensures equals(\result,
      @               Pattern.compile(regex).matcher(this)
      @                      .replaceFirst(replacement));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String replaceFirst(/*@ non_null @*/ String regex,
                            /*@ non_null @*/ String replacement);

    /* FIXME +@ public normal_behavior
      @     requires regex != null && replacement != null;
      @     assignable \nothing;
      @     ensures equals(\result,
      @               Pattern.compile(regex).matcher(this)
      @                      .replaceAll(replacement));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String replaceAll(/*@ non_null @*/ String regex,
                          /*@ non_null @*/ String replacement);

    /* FIXME +@ public normal_behavior
      @     requires regex != null;
      @     assignable \nothing;
      @     ensures equals(\result,
                            Pattern.compile(regex).split(this, limit));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String[] split(/*@ non_null @*/ String regex, int limit);

    /* FIXME +@ public normal_behavior
      @     requires regex != null;
      @     assignable \nothing;
      @     ensures equals(\result,split(regex,0));
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String[] split(/*@ non_null @*/ String regex);

    /*@  public normal_behavior
      @   requires locale != null;
      @   ensures \fresh(\result) && \result.length() == length();
      @   ensures (* \result == a lower case conversion of this using the 
      @                          rules of the given locale *);
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String toLowerCase(/*@ non_null @*/ Locale locale);

    /*@  public normal_behavior
      @   ensures \fresh(\result) && \result.length() == length();
      @   ensures \result != null
      @            && equals(\result,toLowerCase(Locale.getDefault()));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String toLowerCase();

    /*@  public normal_behavior
      @   requires locale != null;
      @   ensures \fresh(\result) && \result.length() == length();
      @   ensures (* \result == an upper case conversion of this using the 
      @                          rules of the given locale *);
      @*/
    public /*@ pure @*/ /*@ non_null @*/
        String toUpperCase(/*@ non_null @*/ Locale locale);

    /*@  public normal_behavior
      @   ensures \fresh(\result) && \result.length() == length();
      @   ensures \result != null
      @            && equals(\result,toUpperCase(Locale.getDefault()));
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String toUpperCase();

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.length() <= length()
      @            && \result.charAt(0) > ' '
      @            && \result.charAt((int)(\result.length() - 1)) > ' ';
      @*/
	// FIXME - be more precise about what is omitted; also avoid recursion
    public /*@ pure @*/ /*@ non_null @*/ String trim();

    /*@ also
      @  public normal_behavior
      @    ensures \result != null && \result == this;
      @*/
    public /*@ pure @*/ /*@ non_null @*/ String toString();
    
    /*@  public normal_behavior
      @    assignable \nothing;
      @    ensures \result.length == length();
      @    ensures \fresh(\result);
      @    ensures equal(\result,charArray);
      @*/
    public /*@ pure @*/ /*@ non_null @*/ char[] toCharArray();

    /*@  public normal_behavior
      @    ensures \result != null;
      @    // FIXME ? \result is fresh?
      @  also public normal_behavior
      @    requires obj == null;
      @    ensures \result != null && equals(\result,"null");
      @  also public normal_behavior
      @    requires obj instanceof String;
      @    ensures \result.equals(obj);   // \result == obj ??? FIXME
      @  also public behavior
      @    requires obj != null;
      @    ensures equals(\result,obj.theString); 
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(Object obj);

    /*@ public normal_behavior
      @   requires data != null;
      @   ensures \result != null;
      @   ensures equal(\result.charArray,data);
      @   ensures \fresh(\result);
      @ also public exceptional_behavior
      @   requires data == null;
      @   signals_only NullPointerException;
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String valueOf(char[] data);

    /*@ public normal_behavior
      @   requires data != null && offset >= 0 && count >= 0
      @       && offset + count < data.length;
      @   ensures \fresh(\result);
      @   ensures \result != null; 
      @   ensures equal(\result.charArray,0,data,offset,count);
// FIXME - exceptions
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String valueOf(char[] data,
                                        int offset, int count);

    /*@ public normal_behavior
      @   requires data != null;
      @   requires 0<=offset && 0<=count && offset+count <= data.length;
      @   ensures \result != null;
      @   ensures \fresh(\result);
      @   ensures equal(\result.charArray,0,data,offset,count);
      @   ensures \result.charArray.length == count;
      @   ensures equal(\result.charArray,0,data,offset,count);
      @ also public exceptional_behavior
      @   requires data == null || offset < 0 || count < 0 ||
      @                            (offset+count)>data.length;
      @   signals_only NullPointerException, StringIndexOutOfBoundsException;
      @   signals (NullPointerException) data == null;
      @   signals (StringIndexOutOfBoundsException) offset<0 || count < 0
                                  || (offset+count)> data.length;
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String copyValueOf(char[] data,
                                            int offset, int count);

    /*@ public normal_behavior
      @   requires data != null;
      @   ensures \result != null && equals(\result,new String(data));
      @   ensures \fresh(\result);
      @   ensures \result.charArray.length == data.length;
      @   ensures equal(\result.charArray,data);
      @ also public exceptional_behavior
      @   requires data == null;
      @   signals_only NullPointerException;
      @*/
    public static /*@ pure @*/
        /*@ non_null @*/ String copyValueOf(char[] data);
        
    /*@  public normal_behavior
      @   ensures \result != null
      @            && equals(\result, Boolean.toString(b));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(boolean b);

    /*@  public normal_behavior
      @   ensures \result != null
      @            && \result.length() == 1
      @            && \result.charAt(0) == c;
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(char c);

    /*@  public normal_behavior
      @   ensures \result != null && equals(\result,Integer.toString(i));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(int i);
    
    /*@  public normal_behavior
      @   ensures \result != null && equals(\result,Long.toString(l));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(long l);
    
    /*@  public normal_behavior
      @   ensures \result != null && equals(\result,Float.toString(f));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(float f);
    
    /*@  public normal_behavior
      @   ensures \result != null && equals(\result,Double.toString(d));
      @*/
    public static /*@ pure @*/ /*@ non_null @*/ String valueOf(double d);
    
    /*@  public normal_behavior
      @   ensures isInterned(\result);
      @   ensures equals(\result,this);
      @   ensures isInterned(this) ==> (\result == this);
      @*/
    //@ pure // -@ function
    public native /*@ non_null @*/ String intern();

    //@ public normal_behavior
    //+@    ensures \result <==> (s != null && (* \dttfsa(boolean,"|interned:|",s) *));
    //-@    ensures \result <==> (s != null && \dttfsa(boolean,"|interned:|",s));
    //-@ function
    //@ public pure static model boolean isInterned(String s);

    //-@ axiom (\forall int i,k; length(\dttfsa(java.lang.String,"|intern:|",i,k)) == k);
    
}
