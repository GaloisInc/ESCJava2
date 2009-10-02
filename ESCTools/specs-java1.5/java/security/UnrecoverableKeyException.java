package java.security;

public class UnrecoverableKeyException extends GeneralSecurityException {
    private static final long serialVersionUID = 7275063078190151277L;
    
    public UnrecoverableKeyException() {
        
    }
    
    public UnrecoverableKeyException(String msg) {
        super(msg);
    }
}
