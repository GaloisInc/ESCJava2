/* Copyright 2000, 2001, Compaq Computer Corporation */

package javafe;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;

import javafe.ast.*;
import javafe.tc.*;
import javafe.util.*;
import javafe.genericfile.GenericFile;
import javafe.genericfile.NormalGenericFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * <code>SrcTool</code> is an abstract class for tools that use
 * our Java front end to process the <code>CompilationUnit</code>s
 * found in source files. <p>
 *
 * It adds to <code>FrontEndTool</code> code for processing a series of
 * source files specified on the command line.  
 * 
 * If processRecursively is set, then files are processed
 * recursively.  (I.e., files loaded in the course of processing one
 * file are also processed.)<p>
 *
 * The remaining processing, if any, is front-end-tool specific.<p>
 */

public abstract class SrcTool extends FrontEndTool implements Listener
{

    /***************************************************
     *                                                 *
     * Keeping track of loaded CompilationUnits:       *
     *                                                 *
     **************************************************/

    /**
     * A list of all the <code>CompilationUnit</code>s we have loaded
     * so far.  This list is extended automatically at the end as new
     * <code>CompilationUnit</code>s are loaded using notification from
     * <code>OutsideEnv</code>.
     */
    //@ invariant loaded != null 
    //@ invariant loaded.elementType == \type(CompilationUnit)
    //@ invariant !loaded.containsNull
    //@ invariant loaded.owner == this
    public Vector loaded = new Vector();

    public SrcTool() {
	super();

	//@ set loaded.elementType = \type(CompilationUnit)
	//@ set loaded.containsNull = false
	//@ set loaded.owner = this
    }


    /**
     * Add a <code>CompilationUnit</code> to <code>loaded</code>. <p>
     *
     * This should only be called by <code>OutsideEnv</code> using the
     * <code>Listener</code> interface.<p>
     */
    public void notify(CompilationUnit justLoaded) {
	if (!justLoaded.duplicate) loaded.addElement(justLoaded);
    }


    /***************************************************
     *                                                 *
     * Main processing code:			       *
     *                                                 *
     **************************************************/

    public Options makeOptions() {
    	return new SrcToolOptions();
    }

    private static SrcToolOptions options() { 
    	return (SrcToolOptions)options;
    }

    /**
     * Main processing loop for <code>SrcTool</code>. <p>
     *
     * The remaining arguments are <code>args[offset]</code>,
     * <code>args[offset+1]</code>, ...<p>
     *
     * This method calls preload, loadAllFiles, postload, preprocess, handleAllCU, postprocess.
     */
    public void frontEndToolProcessing(ArrayList args) {
	long startTime = currentTime();
	/*
	 * At this point, all options have already been processed and
	 * the front end has been initialized.
	 */

        preload();
	
        loadAllFiles(args);
	
	postload();

	// Do any tool-specific pre-processing:
	preprocess();

	if (!options.quiet)
		System.out.println("    [" + timeUsed(startTime) + "]");
	
        handleAllCUs();
	
	// Do any tool-specific post-processing:
	postprocess();
    }


    /***************************************************
     *                                                 *
     * SrcTool-instance specific processing:	       *
     *                                                 *
     **************************************************/

    public void loadAllFiles(ArrayList args) {
	ArrayList accumulatedResults = new ArrayList(args.size());
	Iterator i = args.iterator();
	while (i.hasNext()) {
	    InputEntry ie = (InputEntry)i.next();
	    ArrayList a = resolveInputEntry(ie);
	    OutsideEnv.addSources(a);
	}
    }

    //@ ensures \result.elementType <: \type(GenericFile);
    public ArrayList resolveInputEntry(InputEntry iee) {
	InputEntry ie = iee;
	if (ie.contents == null) {
	    ie = ie.resolve();
	    if (!ie.auto) {
		String s = ie.verify();
		if (s != null) {
		    ErrorSet.error(s);
		    ie.contents = new ArrayList(0);
		    return ie.contents;
		}
	    }
	    if (ie instanceof InputEntry.File) {
		ie.contents = new ArrayList(1);
		ie.contents.add(new NormalGenericFile(ie.name));
	    } else if (ie instanceof InputEntry.Dir) {
		ie.contents = OutsideEnv.resolveDirSources(ie.name);
	    } else if (ie instanceof InputEntry.Package) {
		String[] pa = javafe.filespace.StringUtil.parseList(ie.name,'.');
		ie.contents = OutsideEnv.resolveSources(pa);
	    } else if (ie instanceof InputEntry.Class) {
		ie.contents = new ArrayList(1);
		int p = ie.name.lastIndexOf('.');
		if (p == -1) {
		    GenericFile gf = OutsideEnv.reader.findType(
				new String[0],ie.name);
		    ie.contents.add(gf);
		} else if (p==0 || p == ie.name.length()-1) {
		    ErrorSet.error("Invalid type name: " + ie.name);
		    ie.contents = new ArrayList(0);
		} else {
		    String[] pa = javafe.filespace.StringUtil.parseList(
				    ie.name.substring(0,p),'.');
		    GenericFile gf = OutsideEnv.reader.findType(pa,
				    ie.name.substring(p+1));
		    ie.contents.add(gf);
	        }
	    } else if (ie instanceof InputEntry.List) {
		ie.contents = resolveList(ie.name);
	    } else {
		ErrorSet.caution("Skipping unknown (or not found) input item: "
			+ ie.name);
		ie.contents = new ArrayList(0);
	    }
	}
	return ie.contents;
    }

    public void loadInputEntry(InputEntry ie) {
	OutsideEnv.addSources(resolveInputEntry(ie));
    }

    //@ requires argumentFileName != null;
    //@ ensures \result.elementType <: \type(GenericFile);
    public ArrayList resolveList(String argumentFileName) {
	/* load in source files from supplied file name */
	ArrayList list = new ArrayList();
	try {
	    BufferedReader in = new BufferedReader(
				new FileReader(argumentFileName));
	    String s;
	    while ((s = in.readLine()) != null) {
		// allow blank lines in files list
		if (!s.equals("")) {
		    ArrayList a = (resolveInputEntry(InputEntry.make(s)));
		    if (a != null) list.addAll(a);
		}
	    }
	} catch (IOException e) {
	    ErrorSet.error("I/O failure while reading argument list file "
		    + argumentFileName + ": " + e.getMessage());
	}
	return list;
    }

/*
    public void resolvePackages(ArrayList packagesToProcess) {
	Iterator i = packagesToProcess.iterator();
	while (i.hasNext()) {
	    String p = (String)i.next();
	}
    }
*/
    /** Iterates, calling handleCU for each loaded CU.
     */	
    public void handleAllCUs() {
	/*
	 * Call handleCU on the resulting loaded CompilationUnits.
	 *
	 * If processRecursively is true, then continue calling handleCU
	 * on loaded CompilationUnits that have not had handleCU called
	 * on them in the order they were loaded until no such
	 * CompilationUnits remain.  (handleCU may load CompilationUnits
	 * indirectly.)
	 */
	int i=0;
	for (int end=loaded.size(); i<end; i++) {
	    handleCU((CompilationUnit)loaded.elementAt(i));
	    if (options().processRecursively) {
			Assert.notFalse(OutsideEnv.avoidSpec == true);
			end = loaded.size();
	    }
	}
    }
	 
    /**
     * Hook for any work needed before any files are loaded.
     */
    public void preload() {
	// Set up to receive CompilationUnit-loading notification events:
	OutsideEnv.setListener(this);
    }
    
    /**
     * Called for any work after loading files
     */
// FIXME - can this be done at preload time?
    public void postload() {
	OutsideEnv.avoidSpec = options().avoidSpec;
	if (options().processRecursively)
	    OutsideEnv.avoidSpec = true;
    }

    /**
     * Hook for any work needed after files are loaded
     * but before <code>handleCU</code> is called
     * on each <code>CompilationUnit</code> to process them.
     */
    public void preprocess() {}

    /**
     * Hook for any work needed after <code>handleCU</code> has been called
     * on each <code>CompilationUnit</code> to process them.
     */
    public void postprocess() {}


    /**
     * This method is called on each <code>CompilationUnit</code>
     * that this tool processes. <p>
     *
     * The default implementation is simply to call
     * <code>handleTD</code> on each <code>TypeDecl</code> present in
     * cu.  It is intended that subclassers override this method.<p>
     */
    //@ requires cu != null;
    public void handleCU(CompilationUnit cu) {
		// Iterate over all the TypeDecls representing outside types in cu:
		TypeDeclVec elems = cu.elems;
		for (int i=0; i<elems.size(); i++) {
		    TypeDecl d = elems.elementAt(i);

		    handleTD(d);
		}
    }


    /**
     * This method is called on the TypeDecl of each
     * outside type that SrcTool is to process. <p>
     */
    //@ requires td != null;
    public void handleTD(TypeDecl td) {}

}
