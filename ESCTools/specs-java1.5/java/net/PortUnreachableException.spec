package java.net;
 
public class PortUnreachableException extends SocketException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public PortUnreachableException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public PortUnreachableException(String s);
}
