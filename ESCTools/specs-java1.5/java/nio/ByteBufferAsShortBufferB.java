package java.nio;

class ByteBufferAsShortBufferB extends ShortBuffer {
    /*synthetic*/ static final boolean $assertionsDisabled = !ByteBufferAsShortBufferB.class.desiredAssertionStatus();
    protected final ByteBuffer bb;
    protected final int offset;
    
    ByteBufferAsShortBufferB(ByteBuffer bb) {
        super(-1, 0, bb.remaining() >> 1, bb.remaining() >> 1);
        this.bb = bb;
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        if (!$assertionsDisabled && !(pos <= cap)) throw new AssertionError();
        offset = pos;
    }
    
    ByteBufferAsShortBufferB(ByteBuffer bb, int mark, int pos, int lim, int cap, int off) {
        super(mark, pos, lim, cap);
        this.bb = bb;
        offset = off;
    }
    
    public ShortBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        if (!$assertionsDisabled && !(pos <= lim)) throw new AssertionError();
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 1) + offset;
        if (!$assertionsDisabled && !(off >= 0)) throw new AssertionError();
        return new ByteBufferAsShortBufferB(bb, -1, 0, rem, rem, off);
    }
    
    public ShortBuffer duplicate() {
        return new ByteBufferAsShortBufferB(bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
    }
    
    public ShortBuffer asReadOnlyBuffer() {
        return new ByteBufferAsShortBufferRB(bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
    }
    
    protected int ix(int i) {
        return (i << 1) + offset;
    }
    
    public short get() {
        return Bits.getShortB(bb, ix(nextGetIndex()));
    }
    
    public short get(int i) {
        return Bits.getShortB(bb, ix(checkIndex(i)));
    }
    
    public ShortBuffer put(short x) {
        Bits.putShortB(bb, ix(nextPutIndex()), x);
        return this;
    }
    
    public ShortBuffer put(int i, short x) {
        Bits.putShortB(bb, ix(checkIndex(i)), x);
        return this;
    }
    
    public ShortBuffer compact() {
        int pos = position();
        int lim = limit();
        if (!$assertionsDisabled && !(pos <= lim)) throw new AssertionError();
        int rem = (pos <= lim ? lim - pos : 0);
        ByteBuffer db = bb.duplicate();
        db.limit(ix(lim));
        db.position(ix(0));
        ByteBuffer sb = db.slice();
        sb.position(pos << 1);
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