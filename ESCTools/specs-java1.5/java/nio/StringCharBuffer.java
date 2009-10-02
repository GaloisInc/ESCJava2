package java.nio;

class StringCharBuffer extends CharBuffer {
    CharSequence str;
    
    StringCharBuffer(CharSequence s, int start, int end) {
        super(-1, start, end, s.length());
        int n = s.length();
        if ((start < 0) || (start > n) || (end < start) || (end > n)) throw new IndexOutOfBoundsException();
        str = s;
    }
    
    public CharBuffer slice() {
        return new StringCharBuffer(str, position(), limit());
    }
    
    private StringCharBuffer(CharSequence s, int mark, int pos, int limit, int cap) {
        super(mark, pos, limit, cap);
        str = s;
    }
    
    public CharBuffer duplicate() {
        return new StringCharBuffer(str, markValue(), position(), limit(), capacity());
    }
    
    public CharBuffer asReadOnlyBuffer() {
        return duplicate();
    }
    
    public final char get() {
        return str.charAt(nextGetIndex());
    }
    
    public final char get(int index) {
        return str.charAt(checkIndex(index));
    }
    
    public final CharBuffer put(char c) {
        throw new ReadOnlyBufferException();
    }
    
    public final CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }
    
    public final CharBuffer compact() {
        throw new ReadOnlyBufferException();
    }
    
    public final boolean isReadOnly() {
        return true;
    }
    
    final String toString(int start, int end) {
        return str.toString().substring(start, end);
    }
    
    public final CharSequence subSequence(int start, int end) {
        try {
            int pos = position();
            return new StringCharBuffer(str, pos + checkIndex(start, pos), pos + checkIndex(end, pos));
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    public boolean isDirect() {
        return false;
    }
    
    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }
}
