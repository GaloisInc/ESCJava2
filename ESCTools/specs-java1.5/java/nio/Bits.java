package java.nio;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.misc.Unsafe;
import sun.misc.VM;

class Bits {
    /*synthetic*/ static final boolean $assertionsDisabled = !Bits.class.desiredAssertionStatus();
    
    private Bits() {
        
    }
    
    static short swap(short x) {
        return (short)((x << 8) | ((x >> 8) & 255));
    }
    
    static char swap(char x) {
        return (char)((x << 8) | ((x >> 8) & 255));
    }
    
    static int swap(int x) {
        return (int)((swap((short)x) << 16) | (swap((short)(x >> 16)) & 65535));
    }
    
    static long swap(long x) {
        return (long)(((long)swap((int)(x)) << 32) | ((long)swap((int)(x >> 32)) & 4294967295L));
    }
    
    private static char makeChar(byte b1, byte b0) {
        return (char)((b1 << 8) | (b0 & 255));
    }
    
    static char getCharL(ByteBuffer bb, int bi) {
        return makeChar(bb._get(bi + 1), bb._get(bi + 0));
    }
    
    static char getCharL(long a) {
        return makeChar(_get(a + 1), _get(a + 0));
    }
    
    static char getCharB(ByteBuffer bb, int bi) {
        return makeChar(bb._get(bi + 0), bb._get(bi + 1));
    }
    
    static char getCharB(long a) {
        return makeChar(_get(a + 0), _get(a + 1));
    }
    
    static char getChar(ByteBuffer bb, int bi, boolean bigEndian) {
        return (bigEndian ? getCharB(bb, bi) : getCharL(bb, bi));
    }
    
    static char getChar(long a, boolean bigEndian) {
        return (bigEndian ? getCharB(a) : getCharL(a));
    }
    
    private static byte char1(char x) {
        return (byte)(x >> 8);
    }
    
    private static byte char0(char x) {
        return (byte)(x >> 0);
    }
    
    static void putCharL(ByteBuffer bb, int bi, char x) {
        bb._put(bi + 0, char0(x));
        bb._put(bi + 1, char1(x));
    }
    
    static void putCharL(long a, char x) {
        _put(a + 0, char0(x));
        _put(a + 1, char1(x));
    }
    
    static void putCharB(ByteBuffer bb, int bi, char x) {
        bb._put(bi + 0, char1(x));
        bb._put(bi + 1, char0(x));
    }
    
    static void putCharB(long a, char x) {
        _put(a + 0, char1(x));
        _put(a + 1, char0(x));
    }
    
    static void putChar(ByteBuffer bb, int bi, char x, boolean bigEndian) {
        if (bigEndian) putCharB(bb, bi, x); else putCharL(bb, bi, x);
    }
    
    static void putChar(long a, char x, boolean bigEndian) {
        if (bigEndian) putCharB(a, x); else putCharL(a, x);
    }
    
    private static short makeShort(byte b1, byte b0) {
        return (short)((b1 << 8) | (b0 & 255));
    }
    
    static short getShortL(ByteBuffer bb, int bi) {
        return makeShort(bb._get(bi + 1), bb._get(bi + 0));
    }
    
    static short getShortL(long a) {
        return makeShort(_get(a + 1), _get(a));
    }
    
    static short getShortB(ByteBuffer bb, int bi) {
        return makeShort(bb._get(bi + 0), bb._get(bi + 1));
    }
    
    static short getShortB(long a) {
        return makeShort(_get(a), _get(a + 1));
    }
    
    static short getShort(ByteBuffer bb, int bi, boolean bigEndian) {
        return (bigEndian ? getShortB(bb, bi) : getShortL(bb, bi));
    }
    
    static short getShort(long a, boolean bigEndian) {
        return (bigEndian ? getShortB(a) : getShortL(a));
    }
    
    private static byte short1(short x) {
        return (byte)(x >> 8);
    }
    
    private static byte short0(short x) {
        return (byte)(x >> 0);
    }
    
    static void putShortL(ByteBuffer bb, int bi, short x) {
        bb._put(bi + 0, short0(x));
        bb._put(bi + 1, short1(x));
    }
    
    static void putShortL(long a, short x) {
        _put(a, short0(x));
        _put(a + 1, short1(x));
    }
    
    static void putShortB(ByteBuffer bb, int bi, short x) {
        bb._put(bi + 0, short1(x));
        bb._put(bi + 1, short0(x));
    }
    
    static void putShortB(long a, short x) {
        _put(a, short1(x));
        _put(a + 1, short0(x));
    }
    
    static void putShort(ByteBuffer bb, int bi, short x, boolean bigEndian) {
        if (bigEndian) putShortB(bb, bi, x); else putShortL(bb, bi, x);
    }
    
    static void putShort(long a, short x, boolean bigEndian) {
        if (bigEndian) putShortB(a, x); else putShortL(a, x);
    }
    
    private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (int)((((b3 & 255) << 24) | ((b2 & 255) << 16) | ((b1 & 255) << 8) | ((b0 & 255) << 0)));
    }
    
    static int getIntL(ByteBuffer bb, int bi) {
        return makeInt(bb._get(bi + 3), bb._get(bi + 2), bb._get(bi + 1), bb._get(bi + 0));
    }
    
    static int getIntL(long a) {
        return makeInt(_get(a + 3), _get(a + 2), _get(a + 1), _get(a + 0));
    }
    
    static int getIntB(ByteBuffer bb, int bi) {
        return makeInt(bb._get(bi + 0), bb._get(bi + 1), bb._get(bi + 2), bb._get(bi + 3));
    }
    
    static int getIntB(long a) {
        return makeInt(_get(a + 0), _get(a + 1), _get(a + 2), _get(a + 3));
    }
    
    static int getInt(ByteBuffer bb, int bi, boolean bigEndian) {
        return (bigEndian ? getIntB(bb, bi) : getIntL(bb, bi));
    }
    
    static int getInt(long a, boolean bigEndian) {
        return (bigEndian ? getIntB(a) : getIntL(a));
    }
    
    private static byte int3(int x) {
        return (byte)(x >> 24);
    }
    
    private static byte int2(int x) {
        return (byte)(x >> 16);
    }
    
    private static byte int1(int x) {
        return (byte)(x >> 8);
    }
    
    private static byte int0(int x) {
        return (byte)(x >> 0);
    }
    
    static void putIntL(ByteBuffer bb, int bi, int x) {
        bb._put(bi + 3, int3(x));
        bb._put(bi + 2, int2(x));
        bb._put(bi + 1, int1(x));
        bb._put(bi + 0, int0(x));
    }
    
    static void putIntL(long a, int x) {
        _put(a + 3, int3(x));
        _put(a + 2, int2(x));
        _put(a + 1, int1(x));
        _put(a + 0, int0(x));
    }
    
    static void putIntB(ByteBuffer bb, int bi, int x) {
        bb._put(bi + 0, int3(x));
        bb._put(bi + 1, int2(x));
        bb._put(bi + 2, int1(x));
        bb._put(bi + 3, int0(x));
    }
    
    static void putIntB(long a, int x) {
        _put(a + 0, int3(x));
        _put(a + 1, int2(x));
        _put(a + 2, int1(x));
        _put(a + 3, int0(x));
    }
    
    static void putInt(ByteBuffer bb, int bi, int x, boolean bigEndian) {
        if (bigEndian) putIntB(bb, bi, x); else putIntL(bb, bi, x);
    }
    
    static void putInt(long a, int x, boolean bigEndian) {
        if (bigEndian) putIntB(a, x); else putIntL(a, x);
    }
    
    private static long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
        return ((((long)b7 & 255) << 56) | (((long)b6 & 255) << 48) | (((long)b5 & 255) << 40) | (((long)b4 & 255) << 32) | (((long)b3 & 255) << 24) | (((long)b2 & 255) << 16) | (((long)b1 & 255) << 8) | (((long)b0 & 255) << 0));
    }
    
    static long getLongL(ByteBuffer bb, int bi) {
        return makeLong(bb._get(bi + 7), bb._get(bi + 6), bb._get(bi + 5), bb._get(bi + 4), bb._get(bi + 3), bb._get(bi + 2), bb._get(bi + 1), bb._get(bi + 0));
    }
    
    static long getLongL(long a) {
        return makeLong(_get(a + 7), _get(a + 6), _get(a + 5), _get(a + 4), _get(a + 3), _get(a + 2), _get(a + 1), _get(a + 0));
    }
    
    static long getLongB(ByteBuffer bb, int bi) {
        return makeLong(bb._get(bi + 0), bb._get(bi + 1), bb._get(bi + 2), bb._get(bi + 3), bb._get(bi + 4), bb._get(bi + 5), bb._get(bi + 6), bb._get(bi + 7));
    }
    
    static long getLongB(long a) {
        return makeLong(_get(a + 0), _get(a + 1), _get(a + 2), _get(a + 3), _get(a + 4), _get(a + 5), _get(a + 6), _get(a + 7));
    }
    
    static long getLong(ByteBuffer bb, int bi, boolean bigEndian) {
        return (bigEndian ? getLongB(bb, bi) : getLongL(bb, bi));
    }
    
    static long getLong(long a, boolean bigEndian) {
        return (bigEndian ? getLongB(a) : getLongL(a));
    }
    
    private static byte long7(long x) {
        return (byte)(x >> 56);
    }
    
    private static byte long6(long x) {
        return (byte)(x >> 48);
    }
    
    private static byte long5(long x) {
        return (byte)(x >> 40);
    }
    
    private static byte long4(long x) {
        return (byte)(x >> 32);
    }
    
    private static byte long3(long x) {
        return (byte)(x >> 24);
    }
    
    private static byte long2(long x) {
        return (byte)(x >> 16);
    }
    
    private static byte long1(long x) {
        return (byte)(x >> 8);
    }
    
    private static byte long0(long x) {
        return (byte)(x >> 0);
    }
    
    static void putLongL(ByteBuffer bb, int bi, long x) {
        bb._put(bi + 7, long7(x));
        bb._put(bi + 6, long6(x));
        bb._put(bi + 5, long5(x));
        bb._put(bi + 4, long4(x));
        bb._put(bi + 3, long3(x));
        bb._put(bi + 2, long2(x));
        bb._put(bi + 1, long1(x));
        bb._put(bi + 0, long0(x));
    }
    
    static void putLongL(long a, long x) {
        _put(a + 7, long7(x));
        _put(a + 6, long6(x));
        _put(a + 5, long5(x));
        _put(a + 4, long4(x));
        _put(a + 3, long3(x));
        _put(a + 2, long2(x));
        _put(a + 1, long1(x));
        _put(a + 0, long0(x));
    }
    
    static void putLongB(ByteBuffer bb, int bi, long x) {
        bb._put(bi + 0, long7(x));
        bb._put(bi + 1, long6(x));
        bb._put(bi + 2, long5(x));
        bb._put(bi + 3, long4(x));
        bb._put(bi + 4, long3(x));
        bb._put(bi + 5, long2(x));
        bb._put(bi + 6, long1(x));
        bb._put(bi + 7, long0(x));
    }
    
    static void putLongB(long a, long x) {
        _put(a + 0, long7(x));
        _put(a + 1, long6(x));
        _put(a + 2, long5(x));
        _put(a + 3, long4(x));
        _put(a + 4, long3(x));
        _put(a + 5, long2(x));
        _put(a + 6, long1(x));
        _put(a + 7, long0(x));
    }
    
    static void putLong(ByteBuffer bb, int bi, long x, boolean bigEndian) {
        if (bigEndian) putLongB(bb, bi, x); else putLongL(bb, bi, x);
    }
    
    static void putLong(long a, long x, boolean bigEndian) {
        if (bigEndian) putLongB(a, x); else putLongL(a, x);
    }
    
    static float getFloatL(ByteBuffer bb, int bi) {
        return Float.intBitsToFloat(getIntL(bb, bi));
    }
    
    static float getFloatL(long a) {
        return Float.intBitsToFloat(getIntL(a));
    }
    
    static float getFloatB(ByteBuffer bb, int bi) {
        return Float.intBitsToFloat(getIntB(bb, bi));
    }
    
    static float getFloatB(long a) {
        return Float.intBitsToFloat(getIntB(a));
    }
    
    static float getFloat(ByteBuffer bb, int bi, boolean bigEndian) {
        return (bigEndian ? getFloatB(bb, bi) : getFloatL(bb, bi));
    }
    
    static float getFloat(long a, boolean bigEndian) {
        return (bigEndian ? getFloatB(a) : getFloatL(a));
    }
    
    static void putFloatL(ByteBuffer bb, int bi, float x) {
        putIntL(bb, bi, Float.floatToRawIntBits(x));
    }
    
    static void putFloatL(long a, float x) {
        putIntL(a, Float.floatToRawIntBits(x));
    }
    
    static void putFloatB(ByteBuffer bb, int bi, float x) {
        putIntB(bb, bi, Float.floatToRawIntBits(x));
    }
    
    static void putFloatB(long a, float x) {
        putIntB(a, Float.floatToRawIntBits(x));
    }
    
    static void putFloat(ByteBuffer bb, int bi, float x, boolean bigEndian) {
        if (bigEndian) putFloatB(bb, bi, x); else putFloatL(bb, bi, x);
    }
    
    static void putFloat(long a, float x, boolean bigEndian) {
        if (bigEndian) putFloatB(a, x); else putFloatL(a, x);
    }
    
    static double getDoubleL(ByteBuffer bb, int bi) {
        return Double.longBitsToDouble(getLongL(bb, bi));
    }
    
    static double getDoubleL(long a) {
        return Double.longBitsToDouble(getLongL(a));
    }
    
    static double getDoubleB(ByteBuffer bb, int bi) {
        return Double.longBitsToDouble(getLongB(bb, bi));
    }
    
    static double getDoubleB(long a) {
        return Double.longBitsToDouble(getLongB(a));
    }
    
    static double getDouble(ByteBuffer bb, int bi, boolean bigEndian) {
        return (bigEndian ? getDoubleB(bb, bi) : getDoubleL(bb, bi));
    }
    
    static double getDouble(long a, boolean bigEndian) {
        return (bigEndian ? getDoubleB(a) : getDoubleL(a));
    }
    
    static void putDoubleL(ByteBuffer bb, int bi, double x) {
        putLongL(bb, bi, Double.doubleToRawLongBits(x));
    }
    
    static void putDoubleL(long a, double x) {
        putLongL(a, Double.doubleToRawLongBits(x));
    }
    
    static void putDoubleB(ByteBuffer bb, int bi, double x) {
        putLongB(bb, bi, Double.doubleToRawLongBits(x));
    }
    
    static void putDoubleB(long a, double x) {
        putLongB(a, Double.doubleToRawLongBits(x));
    }
    
    static void putDouble(ByteBuffer bb, int bi, double x, boolean bigEndian) {
        if (bigEndian) putDoubleB(bb, bi, x); else putDoubleL(bb, bi, x);
    }
    
    static void putDouble(long a, double x, boolean bigEndian) {
        if (bigEndian) putDoubleB(a, x); else putDoubleL(a, x);
    }
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    
    private static byte _get(long a) {
        return unsafe.getByte(a);
    }
    
    private static void _put(long a, byte b) {
        unsafe.putByte(a, b);
    }
    
    static Unsafe unsafe() {
        return unsafe;
    }
    private static ByteOrder byteOrder = null;
    
    static ByteOrder byteOrder() {
        if (byteOrder != null) return byteOrder;
        long a = unsafe.allocateMemory(8);
        try {
            unsafe.putLong(a, 72623859790382856L);
            byte b = unsafe.getByte(a);
            switch (b) {
            case 1: 
                byteOrder = ByteOrder.BIG_ENDIAN;
                break;
            
            case 8: 
                byteOrder = ByteOrder.LITTLE_ENDIAN;
                break;
            
            default: 
                throw new Error("Unknown byte order");
            
            }
        } finally {
            unsafe.freeMemory(a);
        }
        return byteOrder;
    }
    private static int pageSize = -1;
    
    static int pageSize() {
        if (pageSize == -1) pageSize = unsafe().pageSize();
        return pageSize;
    }
    private static boolean unaligned;
    private static boolean unalignedKnown = false;
    
    static boolean unaligned() {
        if (unalignedKnown) return unaligned;
        PrivilegedAction pa = new sun.security.action.GetPropertyAction("os.arch");
        String arch = (String)(String)AccessController.doPrivileged(pa);
        unaligned = arch.equals("i386") || arch.equals("x86");
        unalignedKnown = true;
        return unaligned;
    }
    private static volatile long maxMemory = VM.maxDirectMemory();
    private static volatile long reservedMemory = 0;
    private static boolean memoryLimitSet = false;
    
    static void reserveMemory(long size) {
        synchronized (Bits.class) {
            if (!memoryLimitSet && VM.isBooted()) {
                maxMemory = VM.maxDirectMemory();
                memoryLimitSet = true;
            }
            if (size <= maxMemory - reservedMemory) {
                reservedMemory += size;
                return;
            }
        }
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
        }
        synchronized (Bits.class) {
            if (reservedMemory + size > maxMemory) throw new OutOfMemoryError("Direct buffer memory");
            reservedMemory += size;
        }
    }
    
    static synchronized void unreserveMemory(long size) {
        if (reservedMemory > 0) {
            reservedMemory -= size;
            if (!$assertionsDisabled && !(reservedMemory > -1)) throw new AssertionError();
        }
    }
    static final int JNI_COPY_TO_ARRAY_THRESHOLD = 6;
    static final int JNI_COPY_FROM_ARRAY_THRESHOLD = 6;
    
    static native void copyFromByteArray(Object src, long srcPos, long dstAddr, long length);
    
    static native void copyToByteArray(long srcAddr, Object dst, long dstPos, long length);
    
    static void copyFromCharArray(Object src, long srcPos, long dstAddr, long length) {
        copyFromShortArray(src, srcPos, dstAddr, length);
    }
    
    static void copyToCharArray(long srcAddr, Object dst, long dstPos, long length) {
        copyToShortArray(srcAddr, dst, dstPos, length);
    }
    
    static native void copyFromShortArray(Object src, long srcPos, long dstAddr, long length);
    
    static native void copyToShortArray(long srcAddr, Object dst, long dstPos, long length);
    
    static native void copyFromIntArray(Object src, long srcPos, long dstAddr, long length);
    
    static native void copyToIntArray(long srcAddr, Object dst, long dstPos, long length);
    
    static native void copyFromLongArray(Object src, long srcPos, long dstAddr, long length);
    
    static native void copyToLongArray(long srcAddr, Object dst, long dstPos, long length);
}
