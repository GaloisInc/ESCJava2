// @(#)$Id: Boolean.spec 2022 2006-08-13 02:36:42Z chalin $

// Copyright (C) 2002 Iowa State University

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

/** JML's specification of java.lang.Boolean.
 * @version $Revision: 2022 $
 * @author Brandon Shilling
 * @author Gary T. Leavens
 * @author David R. Cok
 */
//-@ immutable
public final /*@ pure @*/ class Boolean implements java.io.Serializable {

    //@ non_null
    public static final Boolean TRUE;

    //@ non_null
    public static final Boolean FALSE;

    //@ non_null
    public static final Class	TYPE;
    //@ initially TYPE == \type(boolean);
    //@ invariant_redundantly TYPE == boolean.class;

    //@ public model boolean theBoolean; in objectState;

    /*@ public normal_behavior
      @   ensures theBoolean == value;
      @*/
    //@ pure
    public Boolean(boolean value);

    /*@ public normal_behavior
      @   ensures \result <==> (s != null && s.equalsIgnoreCase("true"));
      @ public static pure model boolean decode(String s);
      @
      @ public normal_behavior
      @   ensures \result == ( b ? "true" : "false" );
      @ public static pure model String canonicalString(boolean b);
      @*/

    //@ axiom decode(canonicalString(true)) == true;
    //@ axiom decode(canonicalString(false)) == false;
    //@ axiom TRUE.theBoolean;
    //@ axiom !FALSE.theBoolean;
    //@ axiom decode("true");
    //@ axiom decode("True");
    //@ axiom decode("TRUE");
    //@ axiom !decode("false");
    //@ axiom !decode("False");
    //@ axiom !decode("FALSE");
    //@ axiom !decode("");

    /*@ public normal_behavior
      @   ensures theBoolean <==> decode(s);
      @*/
    public /*@ pure @*/ Boolean(String s);
    
    /*@ public normal_behavior
      @   ensures \result == theBoolean;
      @*/
    public /*@ pure @*/ boolean booleanValue();

    // NOTE: The result is not fresh
    /*@ public normal_behavior
      @   ensures \result != null && \result.booleanValue() == b;
      @   ensures \result == ( b ? TRUE : FALSE );
      @*/
    public static /*@ pure @*/ Boolean valueOf(boolean b);


    /*@ public normal_behavior
      @   ensures \result == ( decode(s) ? TRUE : FALSE );
      @*/
    public static /*@ pure @*/ Boolean valueOf(String s);

    // NOTE: The results are interned (not fresh) Strings.
    /*@ public normal_behavior
      @   ensures decode(\result) == b;
      @   ensures \result == (canonicalString(b));
      @   ensures String.isInterned(\result);
      @*/
    public static /*@ pure @*/ String toString(boolean b);

    /*@ also
      @ public normal_behavior
      @   ensures decode(\result) == theBoolean;
      @   ensures \result == canonicalString(theBoolean);
      @*/
    public String toString();

    //+@ public static model int TRUE_HC;
    //+@ public static model int FALSE_HC;
    //-@ public static final model int TRUE_HC;
    //-@ public static final model int FALSE_HC;
    //@ axiom TRUE_HC != FALSE_HC;

    // inherited specification
    //@ also public normal_behavior
    //@   ensures \result == ( theBoolean ? TRUE_HC : FALSE_HC);
    public int hashCode();

    /*@ 
      @ public normal_behavior
      @   ensures \result <==> ( t == obj || (t!= null && obj != null 
                         && (t.theBoolean == ((Boolean) obj).theBoolean)));
        //-@ function
      @ public pure static model boolean equalsBoolean(Boolean t, Boolean obj);
      @*/

    /*@ also
      @ public normal_behavior
      @   ensures \result <==> ( obj != null && (obj instanceof Boolean)
                         && equalsBoolean(this, (Boolean)obj));
      @*/
    public /*@ pure @*/ boolean equals(Object obj);
    
    /*@ public normal_behavior
      @   ensures \result <==> (name != null && 
                                decode(System.getProperty(name)));
      @*/
    public static /*@ pure @*/ boolean getBoolean(String name);
}
