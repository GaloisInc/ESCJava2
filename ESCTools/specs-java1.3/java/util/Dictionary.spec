/*
 * @(#)Dictionary.java	1.13 98/09/27
 *
 * Copyright 1995-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.util;

/**
 * The <code>Dictionary</code> class is the abstract parent of any 
 * class, such as <code>Hashtable</code>, which maps keys to values. 
 * Every key and every value is an object. In any one <tt>Dictionary</tt> 
 * object, every key is associated with at most one value. Given a 
 * <tt>Dictionary</tt> and a key, the associated element can be looked up. 
 * Any non-<code>null</code> object can be used as a key and as a value.
 * <p>
 * As a rule, the <code>equals</code> method should be used by 
 * implementations of this class to decide if two keys are the same. 
 * <p>
 * <strong>NOTE: This class is obsolete.  New implementations should
 * implement the Map interface, rather than extendidng this class.</strong>
 *
 * @author  unascribed
 * @version 1.13, 09/27/98
 * @see	    java.util.Map
 * @see     java.lang.Object#equals(java.lang.Object)
 * @see     java.lang.Object#hashCode()
 * @see     java.util.Hashtable
 * @since   JDK1.0
 */
/* Note, the "implements EscjavaKeyValue" is a feature of this .spec file,
 * so that classes (like Hashtable) that inherit from both Map and
 * Dictionary can be given a suitable ESC/Java specification.  This
 * "implements" clause is not present in the corresponding .java file,
 */ // Removed since not allowed by JML - FIXME
public abstract
class Dictionary { // implements EscjavaKeyValue {
    //@ ghost public boolean permitsNullKey = true;
    //@ ghost public boolean permitsNullValue = true;
    //@ ghost public \TYPE elementType;
    //@ ghost public \TYPE keyType;

    //@ invariant !permitsNullKey && !permitsNullValue;

    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    public Dictionary() ;

    /**
     * Returns the number of entries (dinstint keys) in this dictionary.
     *
     * @return  the number of keys in this dictionary.
     */
    //@ ensures 0 <= \result;
    abstract public int size();

    /**
     * Tests if this dictionary maps no keys to value. The general contract 
     * for the <tt>isEmpty</tt> method is that the result is true if and only 
     * if this dictionary contains no entries. 
     *
     * @return  <code>true</code> if this dictionary maps no keys to values;
     *          <code>false</code> otherwise.
     */
    abstract public boolean isEmpty();

    /**
     * Returns an enumeration of the keys in this dictionary. The general 
     * contract for the keys method is that an <tt>Enumeration</tt> object 
     * is returned that will generate all the keys for which this dictionary 
     * contains entries. 
     *
     * @return  an enumeration of the keys in this dictionary.
     * @see     java.util.Dictionary#elements()
     * @see     java.util.Enumeration
     */
    //@ ensures \result != null;
    //@ ensures !\result.returnsNull;
    //@ ensures \result.elementType == keyType;
    abstract public Enumeration keys();

    /**
     * Returns an enumeration of the values in this dictionary. The general 
     * contract for the <tt>elements</tt> method is that an 
     * <tt>Enumeration</tt> is returned that will generate all the elements 
     * contained in entries in this dictionary.
     *
     * @return  an enumeration of the values in this dictionary.
     * @see     java.util.Dictionary#keys()
     * @see     java.util.Enumeration
     */
    //@ ensures \result != null;
    //@ ensures !\result.returnsNull;
    //@ ensures \result.elementType == elementType;
    abstract public Enumeration elements();

    /**
     * Returns the value to which the key is mapped in this dictionary. 
     * The general contract for the <tt>isEmpty</tt> method is that if this 
     * dictionary contains an entry for the specified key, the associated 
     * value is returned; otherwise, <tt>null</tt> is returned. 
     *
     * @return  the value to which the key is mapped in this dictionary;
     * @param   key   a key in this dictionary.
     *          <code>null</code> if the key is not mapped to any value in
     *          this dictionary.
     * @exception NullPointerException if the <tt>key</tt> is <tt>null</tt>.
     * @see     java.util.Dictionary#put(java.lang.Object, java.lang.Object)
     */
    /* Note, the precondition in the next line does not follow from the
     * documentation above.  However, it seems to be a useful ESC/Java pragma
     * for finding potential bugs in the client code. */
    // @ requires \typeof(key) <: keyType;
    //@ ensures \result==null || \typeof(\result) <: elementType;
    abstract public Object get(/*@ non_null */ Object key);

    /**
     * Maps the specified <code>key</code> to the specified 
     * <code>value</code> in this dictionary. Neither the key nor the 
     * value can be <code>null</code>.
     * <p>
     * If this dictionary already contains an entry for the specified 
     * <tt>key</tt>, the value already in this dictionary for that 
     * <tt>key</tt> is returned, after modifying the entry to contain the
     *  new element. <p>If this dictionary does not already have an entry 
     *  for the specified <tt>key</tt>, an entry is created for the 
     *  specified <tt>key</tt> and <tt>value</tt>, and <tt>null</tt> is 
     *  returned.
     * <p>
     * The <code>value</code> can be retrieved by calling the 
     * <code>get</code> method with a <code>key</code> that is equal to 
     * the original <code>key</code>. 
     *
     * @param      key     the hashtable key.
     * @param      value   the value.
     * @return     the previous value to which the <code>key</code> was mapped
     *             in this dictionary, or <code>null</code> if the key did not
     *             have a previous mapping.
     * @exception  NullPointerException  if the <code>key</code> or
     *               <code>value</code> is <code>null</code>.
     * @see        java.lang.Object#equals(java.lang.Object)
     * @see        java.util.Dictionary#get(java.lang.Object)
     */
    // @ requires \typeof(key) <: keyType;
    // @ requires \typeof(value) <: elementType;
    //@ ensures \result==null || \typeof(\result) <: elementType;
    abstract public Object put(/*@ non_null */ Object key,
			       /*@ non_null */ Object value);

    /**
     * Removes the <code>key</code> (and its corresponding 
     * <code>value</code>) from this dictionary. This method does nothing 
     * if the <code>key</code> is not in this dictionary. 
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the <code>key</code> had been mapped in this
     *          dictionary, or <code>null</code> if the key did not have a
     *          mapping.
     * @exception NullPointerException if <tt>key</tt> is <tt>null</tt>.
     */
    // @ requires \typeof(key) <: keyType;
    //@ ensures \result==null || \typeof(\result) <: elementType;
    abstract public Object remove(/*@ non_null */ Object key);
}
