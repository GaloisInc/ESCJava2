package java.lang;
 
public class NegativeArraySizeException extends RuntimeException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public NegativeArraySizeException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public NegativeArraySizeException(String s);
}
