package java.net;
 
public class ProtocolException extends java.io.IOException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public ProtocolException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public ProtocolException(String s);
}
