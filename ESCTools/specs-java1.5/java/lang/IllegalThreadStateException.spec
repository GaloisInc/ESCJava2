package java.lang;
 
public class IllegalThreadStateException extends IllegalArgumentException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public IllegalThreadStateException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public IllegalThreadStateException(String s);
}
