package java.lang;
 
public class NoSuchMethodException extends Exception {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public NoSuchMethodException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public NoSuchMethodException(String s);
}
