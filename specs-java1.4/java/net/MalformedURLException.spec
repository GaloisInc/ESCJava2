package java.net;
 
public class MalformedURLException extends java.io.IOException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public MalformedURLException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public MalformedURLException(String s);
}
