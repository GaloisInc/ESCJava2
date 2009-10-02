package java.lang;
 
public class IllegalAccessException extends Exception {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public IllegalAccessException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public IllegalAccessException(String s);
}
