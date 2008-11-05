package java.io;
 
public class NotActiveException extends ObjectStreamException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public NotActiveException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public NotActiveException(String s);
}
