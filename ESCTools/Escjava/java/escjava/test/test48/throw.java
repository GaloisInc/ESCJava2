class C {
    
    C() {}

    static int m(boolean b)
    //@ ensures \result == 0
    {
	try {
	    if (b)
		return 0;
	    else
		throw new Throwable();
	}
	catch (Throwable t) {
	    return 1;
	}
    }
}
