package java.util;

public abstract class Dictionary {
    
    public Dictionary() {
        
    }
    
    public abstract int size();
    
    public abstract boolean isEmpty();
    
    public abstract Enumeration keys();
    
    public abstract Enumeration elements();
    
    public abstract Object get(Object key);
    
    public abstract Object put(Object key, Object value);
    
    public abstract Object remove(Object key);
}