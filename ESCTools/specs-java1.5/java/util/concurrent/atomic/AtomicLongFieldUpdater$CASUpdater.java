package java.util.concurrent.atomic;

import sun.misc.Unsafe;
import java.lang.reflect.*;

class AtomicLongFieldUpdater$CASUpdater extends AtomicLongFieldUpdater {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long offset;
    private final Class tclass;
    private final Class cclass;
    
    AtomicLongFieldUpdater$CASUpdater(Class tclass, String fieldName) {
        
        Field field = null;
        Class caller = null;
        int modifiers = 0;
        try {
            field = tclass.getDeclaredField(fieldName);
            caller = sun.reflect.Reflection.getCallerClass(3);
            modifiers = field.getModifiers();
            sun.reflect.misc.ReflectUtil.ensureMemberAccess(caller, tclass, null, modifiers);
            sun.reflect.misc.ReflectUtil.checkPackageAccess(tclass);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Class fieldt = field.getType();
        if (fieldt != Long.TYPE) throw new IllegalArgumentException("Must be long type");
        if (!Modifier.isVolatile(modifiers)) throw new IllegalArgumentException("Must be volatile type");
        this.cclass = (Modifier.isProtected(modifiers) && caller != tclass) ? caller : null;
        this.tclass = tclass;
        offset = unsafe.objectFieldOffset(field);
    }
    
    public boolean compareAndSet(Object obj, long expect, long update) {
        if (!tclass.isInstance(obj)) throw new ClassCastException();
        if (cclass != null) ensureProtectedAccess(obj);
        return unsafe.compareAndSwapLong(obj, offset, expect, update);
    }
    
    public boolean weakCompareAndSet(Object obj, long expect, long update) {
        if (!tclass.isInstance(obj)) throw new ClassCastException();
        if (cclass != null) ensureProtectedAccess(obj);
        return unsafe.compareAndSwapLong(obj, offset, expect, update);
    }
    
    public void set(Object obj, long newValue) {
        if (!tclass.isInstance(obj)) throw new ClassCastException();
        if (cclass != null) ensureProtectedAccess(obj);
        unsafe.putLongVolatile(obj, offset, newValue);
    }
    
    public long get(Object obj) {
        if (!tclass.isInstance(obj)) throw new ClassCastException();
        if (cclass != null) ensureProtectedAccess(obj);
        return unsafe.getLongVolatile(obj, offset);
    }
    
    private void ensureProtectedAccess(Object obj) {
        if (cclass.isInstance(obj)) {
            return;
        }
        throw new RuntimeException(new IllegalAccessException("Class " + cclass.getName() + " can not access a protected member of class " + tclass.getName() + " using an instance of " + obj.getClass().getName()));
    }
}
