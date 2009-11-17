package java.io;

public abstract class Reader implements Readable, Closeable {
    protected Object lock;
    
    protected Reader() {
        
        this.lock = this;
    }
    
    protected Reader(Object lock) {
        
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }
    
    public int read(java.nio.CharBuffer target) throws IOException {
        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = read(cbuf, 0, len);
        if (n > 0) target.put(cbuf, 0, n);
        return n;
    }
    
    public int read() throws IOException {
        char[] cb = new char[1];
        if (read(cb, 0, 1) == -1) return -1; else return cb[0];
    }
    
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }
    
    public abstract int read(char[] cbuf, int off, int len) throws IOException;
    private static final int maxSkipBufferSize = 8192;
    private char[] skipBuffer = null;
    
    public long skip(long n) throws IOException {
        if (n < 0L) throw new IllegalArgumentException("skip value is negative");
        int nn = (int)Math.min(n, maxSkipBufferSize);
        synchronized (lock) {
            if ((skipBuffer == null) || (skipBuffer.length < nn)) skipBuffer = new char[nn];
            long r = n;
            while (r > 0) {
                int nc = read(skipBuffer, 0, (int)Math.min(r, nn));
                if (nc == -1) break;
                r -= nc;
            }
            return n - r;
        }
    }
    
    public boolean ready() throws IOException {
        return false;
    }
    
    public boolean markSupported() {
        return false;
    }
    
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }
    
    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }
    
    public abstract void close() throws IOException;
}
