/* Copyright 2000, 2001, Compaq Computer Corporation */

package escjava.prover;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class is a {@link FilterOutputStream} class that forwards its
 * given output to two given {@link OutputStream}s.
 *
 * @author Rustan Leino
 * @version 11 Aug 2000
 */

public class TeeOutputStream extends FilterOutputStream
{
    /**
     * The other underlying output stream to be filtered (the first
     * being <code>out</code> in the superclass).
     */
    //@ spec_public
    protected /*@non_null*/ OutputStream out2;

    /**
     * Creates an output stream filter built on top of two specified 
     * underlying output streams.
     *
     * @param   out0  the first of the underlying output streams;
     *                will be assigned to <code>this.out</code>;
     *                should not be <code>null</code>
     * @param   out1  the second of the underlying output streams;
     *                will be assigned to <code>this.out2</code>;
     *                should not be <code>null</code>
     */
    //@ public normal_behavior
    //@   modifies this.out2;
    //@   ensures this.out2 == out1;
    public TeeOutputStream(/*@ non_null @*/ OutputStream out0,
			   /*@ non_null @*/ OutputStream out1) {
        super(out0);
	this.out2 = out1;
    }

    /**
     * Writes the specified <code>byte</code> to this output stream. 
     * 
     * <p> The <code>write</code> method of
     * <code>TeeOutputStream</code> calls the <code>write</code>
     * method of its two underlying output streams, that is, it
     * performs <tt>out.write(b)</tt> and then
     * <tt>out2.write(b)</tt>. </p>
     *
     * <p> Implements the abstract <tt>write</tt> method of
     * <tt>OutputStream</tt>. </p>
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException {
	out.write(b);
	out2.write(b);
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream. 
     * 
     * <p>The <code>write</code> method of
     * <code>TeeOutputStream</code> calls the <code>write</code>
     * method of its two underlying output streams, that is, it
     * performs <tt>out.write(b)</tt> and then
     * <tt>out2.write(b)</tt>. </p>
     *
     * @param      b   the data to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#write(byte[], int, int)
     */
    //@ also
    //@ public normal_behavior
    //@ requires b != null;
    //@   modifies \everything;
    public void write(byte b[]) throws IOException {
        out.write(b);
        out2.write(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified
     * <code>byte</code> array starting at offset <code>off</code> to
     * this output stream.
     * 
     * <p>The <code>write</code> method of
     * <code>TeeOutputStream</code> calls the <code>write</code>
     * method of its two underlying output streams, that is, it
     * performs <tt>out.write(b)</tt> and then
     * <tt>out2.write(b)</tt>. </p>
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#write(int)
     */
    //@ also
    //@ public normal_behavior
    //@   requires b != null;
    //@   requires b.length >= off + len;
    //@   modifies \everything;
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        out2.write(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes 
     * to be written out to the stream. 
     * 
     * <p> The <code>flush</code> method of
     * <code>FilterOutputStream</code> calls the <code>flush</code>
     * method of its two underlying output streams. </p>
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#flush
     */
    public void flush() throws IOException {
	out.flush();
	out2.flush();
    }

    /**
     * Closes this output stream and releases any system resources 
     * associated with the stream. 
     * 
     * <p> The <code>close</code> method of
     * <code>FilterOutputStream</code> calls its <code>flush</code>
     * method, and then calls the <code>close</code> method of its two
     * underlying output streams. </p>
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#flush()
     * @see        java.io.FilterOutputStream#close
     */
    public void close() throws IOException {
	try {
            flush();
	} catch (IOException ignored) {
            // ignored
	}
	out.close();
	out2.close();
    }
}
