// @(#)$Id: List.spec 1398 2005-05-09 07:22:32Z chalin $

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

package java.util;

/** JML's specification of java.util.List.
 * @version $Revision: 1398 $
 * @author Brandon Shilling
 * @author Gary T. Leavens
 * @author Erik Poll
 */
public interface List extends Collection {

    /*@ public normal_behavior
      @   ensures \result;
      @ public pure model boolean initialList();
      @*/

    //-@ pure public static model Object get(Content c, \bigint i);

    /*-@ public normal_behavior
      @   ensures \result <==> ( c.theSize == cc.theSize &&
      @     (\forall \bigint i; 0<=i && i<c.theSize; get(c,i) == get(cc,i)));
      @*/
    //-@ function
    //-@ public pure static model boolean equals(Content c, Content cc);

    // specification inherited
    //@ pure
    int size();

    // specification inherited
    //@ pure
    boolean isEmpty();

    //@ also public behavior
    //@   ensures \result <==> (\exists int i; 0<=i && i<size(); nullequals(o,get(i)));
    //@ pure
    boolean contains(Object o);

    /*@ also
      @ public behavior
      @   ensures \result != null;
      @*/
    /* FIXME
      @   ensures size() < Integer.MAX_VALUE
      @       ==> (\forall int i; 0 <= i && i < size();
      @                \result.hasNext(i) && \result.nthNextElement(i)
      @                 == toArray()[i]);
      @*/
    /*@ pure @*/ Iterator iterator();

    /*@ also
      @  public normal_behavior
      @   requires size() < Integer.MAX_VALUE;
      @   ensures \result != null;
      @   ensures \result.length == size();
      @   //-@ ensures (\forall int i; 0<=i && i < size(); \result[i] == get(content,i));
      @*/
    //@ pure
    Object[] toArray();

    /*@ also
      @ public normal_behavior
      @   old int arrSize = a.length;
      @   old int colSize = size();
      @   requires a!= null; 
      @   requires elementType <: \elemtype(\typeof(a));
      @   {|
      @     requires colSize <= arrSize;
      @     assignable a[*];
      @     ensures \result == a;
      @     ensures (\forall int k; 0 <= k && k < colSize;
      @                 get(k) == \result[k]);
      @     ensures (\forall int i; colSize <= i && i < arrSize;
      @                            \result[i] == null);
      @   also
      @     requires colSize > arrSize;
      @     assignable \nothing;
      @     ensures \fresh(\result) && \result.length == colSize;
      @     ensures (\forall int k; 0 <= k && k < colSize;
      @                            get(k) == \result[k]);
      @   |}
      @ also public exceptional_behavior
      @   requires a == null;
      @   assignable \not_specified;
      @   signals_only NullPointerException;

              // FIXME - spec the exceptions
      @*/
    Object[] toArray(Object[] a) throws NullPointerException;

    /*@ also public normal_behavior
      @   requires \typeof(o) <: elementType;
      @   requires !containsNull ==> o != null;
      @   assignable objectState;
      @   ensures content.theSize == \old(content.theSize+1); */
    /*-@  ensures get(content,content.theSize-1) == o;
      @   ensures (\forall \bigint i; 0<=i && i < content.theSize-1; 
      @                    get(content,i) == \old(get(content,i))); */
    /*@   ensures \result;
      @*/
    boolean add(Object o);


    /*@
      @ also public normal_behavior
      @   requires contains(o);
      @   assignable objectState;
      @   ensures content.theSize == \old(content.theSize-1);
      @   ensures \result != (size() == \old(size()) ); */
    /*-@ ensures (\exists \bigint j; 0<=j && j<\old(size()) && nullequals(o,get(content,j));
              (\forall \bigint k; 0<=k && k<j; get(content,k) == \old(get(content,k)))
	      &&  (\forall \bigint k; j<k && k<content.theSize; get(content,k-1)==\old(get(content,k+1)))); */
    /*@ also public exceptional_behavior
      @   requires !contains(o);
      @   assignable \nothing;
      @   signals_only NoSuchElementException;
      @*/
    // FIXME - watch out for optional exceptions
    boolean remove(Object o);

    // specification inherited
    //@ pure
    boolean containsAll(Collection c);

    // FIXME
    boolean addAll(Collection c);

    // FIXME - spec the contents of the new list
    /*@   public behavior
      @     requires c != null;
      @     requires 0 <= index && index <= size();
      @     requires c.elementType <: elementType;
      @     requires !containsNull ==> !c.containsNull;
      @     assignable objectState;
      @   signals_only UnsupportedOperationException, ClassCastException,
                         IllegalArgumentException;
      @     signals (UnsupportedOperationException)
      @              (* if this operation is not supported *);
      @     signals (IllegalArgumentException)
      @              (* if some aspect of c prevents
      @                 its elements from being added to the list *);
      @ also
      @   public exceptional_behavior
      @     requires c == null || !(0 <= index && index <= size())
      @             || !(c.elementType <: elementType)
      @             || (!containsNull && c.containsNull);
      @     assignable \nothing;
      @     signals_only ClassCastException, NullPointerException, IndexOutOfBoundsException;
      @     signals (ClassCastException)
      @             c != null && !(c.elementType <: elementType);
      @     signals (NullPointerException) c == null;
      @     signals (IndexOutOfBoundsException)
      @             !(0 <= index && index <= size());
      @*/
    boolean addAll(int index, Collection c);

    // inherited spec
    // FIXME - retains order
    boolean removeAll(Collection c) throws NullPointerException;

    // inherited spec
    // FIXME - retains order
    boolean retainAll(Collection c) throws NullPointerException;

    // specification inherited
    void clear();

    /*@ also
      @ public normal_behavior
      @   requires o instanceof List && size() == ((List) o).size(); */
    /*-@  ensures \result <==> (\forall \bigint i; 0<=i && i < content.theSize; 
      @       nullequals(get(content,i),get(((List)o).content,i))); */
    /*@ also public normal_behavior
      @   requires !(o instanceof List && size() == ((List) o).size());
      @   ensures !\result;
      @*/
    /*@ pure @*/ boolean equals(Object o);

    // specification inherited
    int hashCode();

    /*@ public normal_behavior
      @   requires 0 <= index && index < size(); */
    /*-@  ensures \result == get(content,index); */
    /*@   ensures (\result == null) || \typeof(\result) <: elementType;
      @   ensures !containsNull ==> \result != null;
      @ also
      @ public exceptional_behavior
      @   requires !(0 <= index && index < size());
      @   signals_only IndexOutOfBoundsException;
      @
            // FIXME - other exceptions
      @*/
    /*@ pure @*/ Object get(int index);

    /*@
      @ public behavior
      @ requires !containsNull ==> element != null;
      @ requires (element == null) || \typeof(element) <: elementType;
      @ {|
      @   requires 0 <= index && index < size();
      @   assignable objectState;
      @   ensures \not_modified(containsNull,elementType,content.theSize);
      @   ensures \result == (\old(get(index)));
      @   ensures get(index) == element; */
    /*@   ensures (\forall \bigint i; 0<=i && i<content.theSize && i != index;
      @               get(content,i) == \old(get(content,i))); */
    /*@   signals_only UnsupportedOperationException, ClassCastException,
                         NullPointerException, IllegalArgumentException;
      @   signals (UnsupportedOperationException)
      @           (* set method not supported by list *);
      @   signals (ClassCastException)
      @           (* class of specified element prevents it
      @              from being added to this *);
      @   signals (NullPointerException)
      @            (* element is null and null elements
      @               not supported by this *);
      @   signals (IllegalArgumentException)
      @            (* some aspect of element prevents it
      @               from being added to this *);
      @ also
      @   requires !(0 <= index && index < size());
      @   assignable \nothing;
      @   ensures false;
      @   signals_only IndexOutOfBoundsException;
      @ |}
      @*/
    Object set(int index, Object element);
    
    /*@ public behavior
      @   requires !containsNull ==> element != null;
      @   requires (element == null) || \typeof(element) <: elementType;
      @   {|
      @     requires  0 <= index && index <= size();
      @     assignable objectState;
      @     ensures \not_modified(containsNull,elementType);
      @     ensures content.theSize == \old(content.theSize)+1;
      @     ensures get(index) == element;
      @     ensures (\forall int i; 0 <= i && i < index;
      @                     get(i) == \old(get(i)));
      @     ensures (\forall int i; index <= i && i < \old(size());
      @                     \old(get(i)) == get(i+1));
      @     signals_only UnsupportedOperationException, ClassCastException,
                         NullPointerException, IllegalArgumentException;
      @     signals (UnsupportedOperationException)
      @             (* add method not supported by list *);
      @     signals (ClassCastException)
      @             (* class of specified element prevents it
      @                from being added to this *);
      @     signals (NullPointerException)
      @             (* element is null and null elements are not
      @                supported by this *);
      @     signals (IllegalArgumentException)
      @             (* some aspect of element
      @                prevents it from being added to this *);
      @ also
      @   requires !(0 <= index && index <= size());
      @   assignable \nothing;
      @   ensures false;
      @   signals_only IndexOutOfBoundsException;
      @ |}
      @*/
    void add(int index, Object element);

    /*@ public behavior
      @   requires  0 <= index && index < size();
      @   assignable objectState;
      @   ensures \not_modified(containsNull,elementType);
      @   ensures (\result == null) || \typeof(\result) <: elementType;
      @   ensures !containsNull ==> \result != null;
      @   ensures content.theSize == \old(content.theSize)-1;
      @   ensures \result == (\old(get(index))) ;
      @   ensures (\forall int i; 0 <= i && i < index;
      @                get(i) == (\old(this.get(i))))
      @        && (\forall \bigint i; index <= i && i < content.theSize;
      @                 get((int)i) == (\old(this.get((int)(i+1)))));
      @   signals_only UnsupportedOperationException;
      @   signals (UnsupportedOperationException)
      @            (* remove method not supported by list *);
          // FIXME - other exceptions?
      @ also public exceptional_behavior
      @   requires !(0 <= index && index < size());
      @   assignable \nothing;
      @   signals_only IndexOutOfBoundsException;
      @*/
    Object remove(int index);

    /*@ public behavior
      @   ensures \result >= -1 && \result < size();
      @   ensures \result != -1 ==> nullequals(o,get(\result));
      @   // ensures (\forall int i; (0<=i && i < \result) ==> !nullequals(o,get(i)));
      @   //ensures \result == -1 <==>
          //   (\forall int i; (0<=i && i < content.theSize) ==> !nullequals(o,get(i)));
      @   //ensures \result == -1 <==> !contains(o);
      @   signals_only ClassCastException, NullPointerException;
      @   signals (ClassCastException)
      @           (* class of specified element is incompatible with this *);
      @   signals (NullPointerException)
      @            (* element is null and null elements are not
      @               supported by this *);
      @*/
    /*@ pure @*/ int indexOf(Object o);

    /*@ public behavior
      @   ensures \result >= -1 && \result < size();
      @   ensures \result != -1 ==> nullequals(o,get(\result));
      @   ensures (\forall int i; \result<i && i < size(); !nullequals(o,get(i)));
      @   ensures \result == -1 <==> !contains(o);
      @   signals_only ClassCastException, NullPointerException;
      @   signals (ClassCastException)
      @           (* class of specified element is incompatible with this *);
      @   signals (NullPointerException)
      @            (* element is null and null elements are not
      @               supported by this *);
      @*/
    /*@ pure @*/ int lastIndexOf(Object o);

    /* FIXME @ 
      @ public normal_behavior
      @   ensures \result != null && size() < Integer.MAX_VALUE
      @       ==> (\forall int i; 0 <= i && i < size();
      @                \result.hasNext(i) && \result.nthNextElement(i)
      @                 == toArray()[i]);
      @   ensures \result.elementType == elementType;
      @   ensures containsNull == \result.returnsNull;
      @*/
    /*@ pure @*/ /*@ non_null @*/ ListIterator listIterator();

    /* FIXME @ 
      @ public normal_behavior
      @   requires 0 <= index && index < size();
      @   ensures \result != null && size() < Integer.MAX_VALUE
      @       ==> (\forall int i; index <= i && i < size();
      @                \result.hasNext(i) && \result.nthNextElement(i)
      @                 == toArray()[i]);
      @ also
      @ public exceptional_behavior
      @   requires index < 0 && size() <= index;
      @   signals_only IndexOutOfBoundsException;
      @
      @  implies_that
      @     ensures \result.elementType == elementType;
      @     ensures containsNull == \result.returnsNull;
      @*/
    /*@ pure @*/ /*@ non_null @*/ ListIterator listIterator(int index);

    // FIXME - changes to a sublist affect the list it is a sublist of!!!
    /*@ public normal_behavior
      @   requires 0 <= fromIndex && fromIndex <= size() 
      @         && 0 <= toIndex && toIndex <= size()
      @         && fromIndex <= toIndex;
      @   ensures \result.size() == toIndex - fromIndex;
      @   ensures (\forall int i; fromIndex <=i && i < toIndex;
                         get(i) == \result.get(i-fromIndex));
      @ also
      @ public exceptional_behavior
      @   requires !(0 <= fromIndex && fromIndex <= size())
      @         || !( 0 <= toIndex && toIndex <= size())
      @         || !(fromIndex <= toIndex);
      @   signals_only IndexOutOfBoundsException;
      @
      @*/
    /*@ pure @*/ List subList(int fromIndex, int toIndex);
}
