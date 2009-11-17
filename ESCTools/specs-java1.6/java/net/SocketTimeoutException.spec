package java.net;
 
public class SocketTimeoutException extends java.io.InterruptedIOException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public SocketTimeoutException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public SocketTimeoutException(String s);
}
