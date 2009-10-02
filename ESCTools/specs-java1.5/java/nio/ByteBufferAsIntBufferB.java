package java.nio;

class ByteBufferAsIntBufferB extends IntBuffer {
    /*synthetic*/ static final boolean $assertionsDisabled = !ByteBufferAsIntBufferB.class.desiredAssertionStatus();
    protected final ByteBuffer bb;
    protected final int offset;
    
    ByteBufferAsIntBufferB(ByteBuffer bb) {
        super(-1, 0, bb.remaining() >> 2, bb.remaining() >> 2);
        this.bb = bb;
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        if (!$assertionsDisabled && !(pos <= cap)) throw new AssertionError();
        offset = pos;
    }
    
    ByteBufferAsIntBufferB(ByteBuffer bb, int mark, int pos, int lim, int cap, int off) {
        super(mark, pos, lim, cap);
        this.bb = bb;
        offset = off;
    }
    
    public IntBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        if (!$assertionsDisabled && !(pos <= lim)) throw new AssertionError();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 2) + offset;
        if (!$assertionsDisabled && !(off >= 0)) throw new AssertionError();
        return new ByteBufferAsIntBufferB(bb, -1, 0, rem, rem, off);
    }
    
    public IntBuffer duplicate() {
        return new ByteBufferAsIntBufferB(bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
    }
    
    public IntBuffer asReadOnlyBuffer() {
        return new ByteBufferAsIntBufferRB(bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
    }
    
    protected int ix(int i) {
        return (i << 2) + offset;
    }
    
    public int get() {
        return Bits.getIntB(bb, ix(nextGetIndex()));
    }
    
    public int get(int i) {
        return Bits.getIntB(bb, ix(checkIndex(i)));
    }
    
    public IntBuffer put(int x) {
        Bits.putIntB(bb, ix(nextPutIndex()), x);
        return this;
    }
    
    public IntBuffer put(int i, int x) {
        Bits.putIntB(bb, ix(checkIndex(i)), x);
        return this;
    }
    
    public IntBuffer compact() {
        int pos = position();
        int lim = limit();
        if (!$assertionsDisabled && !(pos <= lim)) throw new AssertionError();
        int rem = (pos <= lim ? lim - pos : 0);
        ByteBuffer db = bb.duplicate();
        db.limit(ix(lim));
        db.position(ix(0));
        ByteBuffer sb = db.slice();
        sb.position(pos << 2);
        sb.compact();
        position(rem);
        limit(capacity());
        return this;
    }
    
    public boolean isDirect() {
        return bb.isDirect();
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }
}
