class C {
    
    /*@ non_null*/ Integer i;
    /*@ non_null*/ Integer j;
    
    //@ modifies i,j,this.*;
    C() {
	int k = init(4);
    }
    //@ modifies i,j;
    //@ ensures \result == 4;
    /*@ helper */ private int init(int k) {
	i = new Integer(13);
	j = new Integer (55);
	return k;
    }
    //@ modifies i,j;
    void m(int x) {
        int k = init(x);
    }  // postcondition violation
}
