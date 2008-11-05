package java.lang;
 
public class ArrayStoreException extends RuntimeException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public ArrayStoreException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public ArrayStoreException(String s);
}
