package java.util;

public abstract class AbstractCollection implements Collection {
    
    protected AbstractCollection() {
        
    }
    
    public abstract Iterator iterator();
    
    public abstract int size();
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public boolean contains(Object o) {
        Iterator e = iterator();
        if (o == null) {
            while (e.hasNext()) if (e.next() == null) return true;
        } else {
            while (e.hasNext()) if (o.equals(e.next())) return true;
        }
        return false;
    }
    
    public Object[] toArray() {
        Object[] result = new Object[size()];
        Iterator e = iterator();
        for (int i = 0; e.hasNext(); i++) result[i] = e.next();
        return result;
    }
    
    public Object[] toArray(Object[] a) {
        int size = size();
        if (a.length < size) a = (Object[])(Object[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        Iterator it = iterator();
        Object[] result = a;
        for (int i = 0; i < size; i++) result[i] = it.next();
        if (a.length > size) a[size] = null;
        return a;
    }
    
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }
    
    public boolean remove(Object o) {
        Iterator e = iterator();
        if (o == null) {
            while (e.hasNext()) {
                if (e.next() == null) {
                    e.remove();
                    return true;
                }
            }
        } else {
            while (e.hasNext()) {
                if (o.equals(e.next())) {
                    e.remove();
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean containsAll(Collection c) {
        Iterator e = c.iterator();
        while (e.hasNext()) if (!contains(e.next())) return false;
        return true;
    }
    
    public boolean addAll(Collection c) {
        boolean modified = false;
        Iterator e = c.iterator();
        while (e.hasNext()) {
            if (add(e.next())) modified = true;
        }
        return modified;
    }
    
    public boolean removeAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        while (e.hasNext()) {
            if (c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    public void clear() {
        Iterator e = iterator();
        while (e.hasNext()) {
            e.next();
            e.remove();
        }
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        Iterator i = iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Object o = i.next();
            buf.append(o == this ? "(this Collection)" : String.valueOf(o));
            hasNext = i.hasNext();
            if (hasNext) buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }
}
