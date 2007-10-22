package java.io;
 
public class InterruptedIOException extends IOException {

    public int bytesTransferred;
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public InterruptedIOException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public InterruptedIOException(String s);
}
