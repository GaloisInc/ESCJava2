/*
 * Copyright (C) 2000-2001 Iowa State University
 *
 * This file is part of mjc, the MultiJava Compiler.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 * Author: David R. Cok
 */
package junitutils;

import java.io.*;
import java.util.Iterator;

/** This is a utility class that is an iterator over the lines produced
    by reading a file.  It is used in particular by various
    JUnit tests.

    @author David R. Cok
*/
public class LineIterator implements Iterator {

    /** A reader that reads lines from the file. */
    //@ non_null
    private BufferedReader r;
    
    /** The next value to be returned by the iterator.  We read ahead one so
	that we know the value of hasNext() when asked.
    */
    private String nextLine;

    /** Starts an iterator reading from the given external process. 
	@param filename The name of the file to be read
     */
    //@ requires filename != null;
    //@ ensures (* file is readable *);
    //@ signals (java.io.IOException) (* file is not readable *);
    public LineIterator(String filename) throws java.io.IOException  {
	r = new BufferedReader(new FileReader(filename)); 
	nextLine = r.readLine();
	//System.out.println("READ " + nextLine);
    }


    /** Per a standard iterator, returns true if there is another value waiting. */
    public boolean hasNext() throws RuntimeException {
	try {
	    if (nextLine == null) {
		r.close();
	    }
	    return nextLine != null;
	} catch (java.io.IOException e) {
	    throw new RuntimeException("EXCEPTION in hasNext - " + e); 
	}
    }

    /** Per a standard iterator, returns the next value - and throws 
	java.util.NoSuchElementException if the list has been exhausted 
	(hasNext() returns false).
    */
    public Object next() throws java.util.NoSuchElementException, RuntimeException {
	if (nextLine == null) 
	    throw new java.util.NoSuchElementException();
	try {
	    String n = nextLine;
	    nextLine = r.readLine();
	    //System.out.println("READ " + nextLine);
	    return n;
	} catch (java.io.IOException e) {
	    throw new RuntimeException("EXCEPTION in next - " + e); 
	}
    }

    /** This operation will throw an exception, as there is no need for 
	remove in this context. */
    //@ also
    //@ requires \typeof(this)==\type(LineIterator);
    //@ ensures false;
    //@ signals (UnsupportedOperationException) true;
    public void remove() throws UnsupportedOperationException {
	throw new java.lang.UnsupportedOperationException();
    }

}



