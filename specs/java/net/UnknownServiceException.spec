package java.net;
 
public class UnknownServiceException extends java.io.IOException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public UnknownServiceException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public UnknownServiceException(String s);
}
