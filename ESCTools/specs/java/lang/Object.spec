/* ESC/Java interface of java.lang.Object */

package java.lang;

public class Object {
    /** A data group for the state of this object.  This is used to
     * allow side effects on unknown variables in methods such as
     * equals, clone, and toString. It also provides a convenient way
     * to talk about "the state" of an object in assignable
     * clauses.
     */
    //@ public non_null model JMLDataGroup objectState;

    /**
     ** The Object that has a field pointing to this Object.
     ** Used to specify (among other things) injectivity (see
     ** the ESC/Java User's Manual.
     **/
    //@ ghost public Object owner;

    //@ modifies \nothing;
    public Object();

    //@ modifies \nothing;
    //@ ensures \result != null;
    public final native Class getClass();

    //@ modifies \nothing;
    public native int hashCode();

    //@ modifies \nothing;
    //@ ensures obj == null ==> !\result;
    public boolean equals(Object obj);

    //@ modifies \nothing;
    //@ ensures \result != null;
    //@ ensures \typeof(\result) == \typeof(this);
    //@ ensures \result.owner == null;
    //@ ensures (this instanceof Cloneable);
    //@ signals (CloneNotSupportedException) !(this instanceof Cloneable);
    protected native Object clone() throws CloneNotSupportedException;

    //@ modifies \nothing;
    //@ ensures \result != null;
    public String toString();

    public final native void notify();

    public final native void notifyAll();

    public final native void wait(long timeout) throws InterruptedException;

    //@ requires 0<=timeout;
    //@ requires 0<=nanos && nanos<=999999;
    public final void wait(long timeout, int nanos) throws InterruptedException;

    public final void wait() throws InterruptedException;

    protected void finalize() throws Throwable;
}
