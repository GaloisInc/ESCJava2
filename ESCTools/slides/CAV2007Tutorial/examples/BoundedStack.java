public class BoundedStack {

  private /*@ spec_public @*/ Object[] elems;
  private /*@ spec_public @*/ int size = 0;

  //@ public invariant 0 <= size;
  /*@ public invariant
    @     (\forall int i; 
    @           0 <= size && i < elems.length;
    @           elems[i] == null);
    @*/

  /*@ requires 0 < n;
    @ assignable elems;
    @ ensures elems.length == n;
    @*/
  public BoundedStack(int n) {
    elems = new Object[n];
  }

  /*@ requires size < elems.length-1;   
    @ assignable elems[size], size;
    @ ensures size == \old(size+1);
    @ ensures elems[size-1] == x;
    @ ensures_redundantly
    @     (\forall int i; 0 <= i && i < size-1;
    @               elems[i] == \old(elems[i]));
    @*/
  public void push(Object x) {
    size++;
    elems[size-1] = x;
  }

  /*@ requires 0 < size;
    @ assignable size, elems[size-1];
    @ ensures size == \old(size-1);
    @ ensures_redundantly
    @      elems[size-1] == null
    @   && (\forall int i; 0 <= i && i < size-2;
    @               elems[i] == \old(elems[i]));
    @*/
  public void pop() {
    elems[size-1] = null;
    size--;
  }

  /*@ requires 0 < size;
    @ assignable \nothing;
    @ ensures \result == elems[size-1];
    @*/
  public /*@ pure @*/ Object top() {
    return elems[size-1];
  }
}