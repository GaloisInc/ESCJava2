package java.lang.reflect;

public class InvocationTargetException extends Exception {
    private static final long serialVersionUID = 4085088731926701167L;
    private Throwable target;
    
    protected InvocationTargetException() {
        super((Throwable)null);
    }
    
    public InvocationTargetException(Throwable target) {
        super((Throwable)null);
        this.target = target;
    }
    
    public InvocationTargetException(Throwable target, String s) {
        super(s, null);
        this.target = target;
    }
    
    public Throwable getTargetException() {
        return target;
    }
    
    public Throwable getCause() {
        return target;
    }
}
