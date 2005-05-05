package java.io;
 
public class StreamCorruptedException extends ObjectStreamException {
  
    /*@ public normal_behavior
      @   ensures standardThrowable(null);
      @*/
    //@ pure
    public StreamCorruptedException();
  
    /*@ public normal_behavior
      @   ensures standardThrowable(s);
      @*/
    //@ pure
    public StreamCorruptedException(String s);
}
