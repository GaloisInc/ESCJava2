//
// This tests reasoning with \min and \max quantifiers
import java.util.Collection;
import java.util.LinkedList;

public class Min {
	Min();

	//@ pure
	public int f(int i);

	//@ normal_behavior ensures \result==i; private model int id(int i);
	//@ normal_behavior ensures \result; private model boolean ex1(int i);
	//@ normal_behavior ensures \result; private model boolean ex2(int i);
	//@ normal_behavior ensures \result; private model boolean ex3(int i);

	public void x() {
	    //@ assert (\max int i; 1<=i && i<10 && ex1(i); f(i)) >= f(2); // SHOULD FAIL
	}
	public void x2() {
	    //@  assert (\max int i; 1<=i && i<10 && ex2(i); id(i)) >= id(2); 
	}
	public void x3() {
	    //@  assert (\max int i; 1<=i && i<10 && ex3(i); id(i)) == id(9);
	}
	public void x4() {
	    //@  assert (\max int i; 1<=i && i<0 && ex2(i); id(i)) >= id(2); // SHOULD FAIL
	}
	public void x5() {
	    //@  assert (\max int i; 1<=i && i<10 && ex2(i); id(i)) == id(2); // SHOULD FAIL
	}

	public void m() {
	    //@ assert (\min int i; 1<=i && i<10 && ex1(i); f(i)) <= f(2); // SHOULD FAIL
	}
	public void m2() {
	    //@  assert (\min int i; 1<=i && i<10 && ex2(i); id(i)) <= id(2);
	}
	public void m3() {
	    //@  assert (\min int i; 1<=i && i<10 && ex3(i); id(i)) == id(1);
	}
	public void m4() {
	    //@  assert (\min int i; 1<=i && i<0 && ex2(i); id(i)) <= id(2); // SHOULD FAIL
	}
	public void m5() {
	    //@  assert (\min int i; 1<=i && i<10 && ex2(i); id(i)) == id(2); // SHOULD FAIL
	}


	public void pp() {
	    /*@ assert (\forall int k; 0<=k && k < 5 && ex1(k);
		         (\min int i; 0<=i && i<=k && ex1(i) ; id(i)) <= id(k));
	    */
	}
	public void pp2() {
	    //@ ghost Collection c = new LinkedList();
	    //@ ghost Collection cc = new LinkedList();
	    /*@ assert (\forall Object o,oo; c.contains(o) && c.contains(oo); 
			    (\min Object ooo; cc.contains(ooo); cc.hashCode())
				< o.hashCode() );
	    */
	}
}


