/*
 * Copyright (C) 2000-2001 Iowa State University
 * Modified 2008 Systems Research Group, University College Dublin
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
 * $Id: TestFilesTestSuite.java 71450 2008-04-10 10:31:38Z dcochran $
 * Author: David R. Cok
 */
package junitutils;

import junit.framework.*;
import java.io.*;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * This is a JUnit TestSuite that is created from a number of tests as follows.
 * Each TestCase is an instance of the inner class Helper, instantiated with a
 * name of a file. The file names are read from the file named by the parameter
 * 'fileOfTestFilenames'. The argument to the constructor named 'args' provides
 * a set of command-line arguments; the filename for the TestCase is added on to
 * the end of the list of command-line arguments. Then the static compile method
 * of the given class is called on those command-line arguments.
 * <P>
 * The standard output and error output is captured from the execution of the
 * compile method. This is compared to the output in filename + "-expected". The
 * TestCase succeeds if these match; if they do not match, the test fails and
 * the actual output is saved in filename + "-ckd".
 * <P>
 * The test must be run from the directory in which it resides - because it
 * creates and opens files in the current directory.
 * 
 * @author David R. Cok
 */
public class AbstractDistributedTestSuite extends TestSuite {
  
  static final int ONE_MEGABYTE = 1024 * 1024;
  static final String NOT_ENOUGH_MEMORY = " *** not enough free memory to run this test ***";

  //@ ensures_redundantly !initialized;
  protected AbstractDistributedTestSuite() {
  }

  /*
   * Create derived classes with alternate tests by deriving a class from this
   * one. It should contain an inner Helper class derived from
   * TestFilesTestSuite.Helper. The method TestFilesTestSuite.makeHelper
   * should be overridden to return an instance of the derived Helper class.
   * The derived Helper class should override Helper.dotest to do the actual
   * test.
   */

  // -------------------------------------------------------------
  // DATA MEMBERS
  // -------------------------------------------------------------
  /** The name of this test suite. */
  protected String testName; //@ in initialized;

  //@ protected invariant_redundantly initialized ==> (testName != null);

  /** The method that is to be executed on the command-line arguments. */
  protected Method method; //@ in initialized;

  //@ protected invariant_redundantly initialized ==> (method != null);

  /*
   * "-expected" is the file for the expected result with minimal or no line options
   * e.g. testmode; if the expected result varies for option -era then there would be
   * a file with suffix "-expected-era";
   */
  final static String SAVED_SUFFIX = "-ckd";

  final static String ORACLE_SUFFIX = "-expected";

  // -------------------------------------------------------------
  // CONSTRUCTOR
  // -------------------------------------------------------------

  /**
   * A constructor for this test suite.
   * 
   * @param testName
   *            The name of the test suite
   * @param fileOfTestFilenames
   *            The file to be read for filenames of tests
   * @param listOfOptions
   *            The file containing command-line arguments that the static
   *            compile method will be applied to, with the test filename
   *            added on
   * @param listOfSecondOptions
   *            The file containing additional command-line arguments that the
   *            static compile method will be applied to, with the test
   *            filename added on
   * @param listOfProvers
   *            The file contain the list of provers and prover specific options
   * @param cls
   *            The class in which to find the static compile method
   * @param serverIndex
   *            The position of this server in the list
   * @param numberOfServers
   *            The total number of servers available for distributed testing
   */
  /*
   * @ public behavior @ requires serverIndex < numberOfServers; @ assignable
   * initialized, objectState; @ ensures_redundantly initialized; @
   * signals_only RuntimeException; @
   */
  public AbstractDistributedTestSuite(/*@ non_null */String testName,
  /*@ non_null */String fileOfTestFilenames,
  /*@ non_null */String listOfOptions,
  /*@ non_null */String listOfSecondOptions,
  /*@ non_null */String listOfProvers,
  /*@ non_null */Class cls, int serverIndex, int numberOfServers) {
    super(testName);
    this.testName = testName;
    
    try {
      method = cls.getMethod("compile", new Class[] { String[].class });
      Class rt = method.getReturnType();
      if (rt != Integer.TYPE && rt != Boolean.TYPE) {
        String m = (cls.getName() + ".compile() must have a return type of "
                    + "'int' or 'boolean', not " + rt);
        throw new RuntimeException(m);
      }
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e.toString());
    }
    
    // Divide tests between the available build processes
    int numberOfProcessors = Runtime.getRuntime().availableProcessors() + 1;
    long batch = System.currentTimeMillis() % numberOfProcessors;

    int position = 0; // Sequence number for each subset of tests
    if ((0 < numberOfServers) && (0 <= serverIndex)) {
      try {
        // Iterate over the list of test cases
        Iterator i = new LineIterator(fileOfTestFilenames);
        while (i.hasNext()) {
          String thisLine = (String) i.next();
          if (0 < thisLine.length()) {
            // Iterate over the list of command line options
            Iterator j = new LineIterator(listOfOptions);
            while (j.hasNext()) {
              String preArgs = (String) j.next();
              // Iterate over the list of provers
              Iterator p = new LineIterator(listOfProvers);
              while (p.hasNext()) {
                String proverArgs = (String) p.next();
              
                // Iterate over the list of additional command line options
                Iterator k = new LineIterator(listOfSecondOptions);
                while (k.hasNext()) {
                  // Add this subset of tests only if it is the turn of this server,
                  // and we are within the current batch of tests
                  if ((position++ % numberOfServers) == serverIndex) {
                    if ((position % numberOfProcessors) == batch) {
                      System.gc();
                      if (Runtime.getRuntime().freeMemory() > ONE_MEGABYTE) {
                        addTest(makeHelper(JUnitUtils.parseLine(preArgs
                                                                + " "
                                                                + proverArgs
                                                                + " "
                                                                + (String) k
                                                                    .next()
                                                                + " "
                                                                + thisLine)));
                      }
                    }
                }
              }
              }
            }
          }
        }
      } catch (java.io.IOException e) {
        throw new RuntimeException(e.toString());
      }
    }
  }

  //@ public model boolean initialized;
  /*
   *@ protected represents initialized <- testName != null 
   *@ && method !=null 
   *@ && (method.getReturnType() == int.class 
   *@ || method.getReturnType() == boolean.class); 
   *@
   */

  //@ ensures \result == initialized;
  private/*@ pure */boolean initialized() {
    return testName != null
           && method != null
           && (method.getReturnType() == int.class || method.getReturnType() == boolean.class);
  }

  /** Factory method for the helper class object. */
  //@ requires 0 < args.length;
  //@ assignable \nothing;
  protected Helper makeHelper(/*@ non_null */String[] args) {
    return new Helper(args[args.length - 1], args);
  }

  // FIXME - This test does not do the equivalent of FIXTILT or PATHTOFILES
  // that is performed in the Makefile to canonicalize the outputs. So far we
  // have not needed it.

  /**
   * This is a helper class that is actually a TestCase; it is run repeatedly
   * with different constructor arguments.
   */
  public class Helper extends TestCase {

    /**
     * The first argument is used as the name of the test as well as the
     * name of the file to be tested.
     */
    public Helper(/*@ non_null */String testname, /*@ non_null */
    String[] args) {
      super(testname);
      this.fileToTest = testname;
      this.args = args;
    }

    /** Filename of comparison files */
    protected/*@ non_null */String fileToTest;

    /** Result of test */
    protected/*@ nullable */Object returnedObject;

    /** Command-line arguments (including filename) for this test. */
    protected/*@ non_null */String[] args;

    /**
     * This is the framework around the test. It sets up the streams to
     * capture output, and catches all relevant exceptions.
     */

    //@ also
    //@ requires initialized;
    // Maybe we could move the above spec case into the superclass?
    public void runTest() throws java.io.IOException {
      // Due to behavioral subtyping this method might be called
      // when !initialized ... hence we will test for this condition
      if (!initialized()) {
        String msg = "TestFilesTestSuite.runTest() "
                     + "called before 'this' was properly initialized";
        fail(msg);
      }
      
      // Diagnostic for adding/changing command line options
      System.out.println("\nTest suite " + testName + ": "  + fileToTest);
      System.out.print("Options being tested: ");
      for (int kk=0; kk<args.length-1; ++kk) System.out.print(args[kk] + " ");
      System.out.println();

      ByteArrayOutputStream ba = JUnitUtils.setStreams();
      try {
        System.gc();
        if (Runtime.getRuntime().freeMemory() > ONE_MEGABYTE) {
          returnedObject = dotest(fileToTest, args);
        }
        else {
          fail (fileToTest + NOT_ENOUGH_MEMORY);
        }
      } catch (IllegalAccessException e) {
        JUnitUtils.restoreStreams(true);
        fail(e.toString());
      } catch (IllegalArgumentException e) {
        JUnitUtils.restoreStreams(true);
        fail(e.toString());
      } catch (java.lang.reflect.InvocationTargetException e) {
        JUnitUtils.restoreStreams(true);
        java.io.StringWriter sw = new StringWriter();
        sw.write(e.toString());
        e.printStackTrace(new PrintWriter(sw));
        fail(sw.toString());
      } catch (OutOfMemoryError notEnoughHeap) {
        returnedObject = null;
        System.gc();
        notEnoughHeap.printStackTrace();
        fail (args + fileToTest + notEnoughHeap.getLocalizedMessage());
      } finally {
        JUnitUtils.restoreStreams(true);
      }
      /*@ nullable */String err;
      
      if (returnedObject != null) {
        err = doOutputCheck(fileToTest, ba.toString(), returnedObject, args);
      
        if (err != null) {
          fail(err);
        }
      }
    }
  } // end of class Helper

  /**
   * This is the actual test; it compiles the given file and compares its
   * output to the expected result (in fileToTest+ORACLE_SUFFIX); the output
   * is expected to match and the result of the compile to be true or false,
   * depending on whether errors or warnings were reported. Override this
   * method in derived tests.
   * 
   */
  //@ requires initialized;
  protected/*@ non_null */Object dotest(String fileToTest, String[] args)
      throws IllegalAccessException, IllegalArgumentException,
      java.lang.reflect.InvocationTargetException {
    return method.invoke(null, new Object[] { args });
  }

  /**
   * Compare the expected result with the actual result
   * 
   * @param fileToTest
   * @param output
   * @param returnedValue
   * @param options The command line options being tested
   * @return
   */
  //@ requires initialized;
  protected/*@ nullable */String doOutputCheck(
  /*@ non_null */String fileToTest,
  /*@ non_null */String output,
  /*@ non_null */Object returnedValue,
  /*@ non_null */String[] options) {
    try {
      String expectedOutput = JUnitUtils.readFile(fileToTest + ORACLE_SUFFIX);
      Diff df = new Diff("expected", expectedOutput, "actual", output);

      if (!df.areDifferent()) {
        // If the two strings match, the test succeeds and we make sure
        // that there is no -ckd file to confuse anyone.
        (new File(fileToTest + SAVED_SUFFIX)).delete();
      } else {
        // If test fails then look for an alternative expected result based
        // on the set of command line options chosen.
        StringBuffer optionalSuffix = new StringBuffer();
        for (int kk=0; kk<options.length-1; ++kk) {
          optionalSuffix.append(options[kk].trim());
        }
        
        expectedOutput = JUnitUtils.readFile(fileToTest + ORACLE_SUFFIX 
                                               + optionalSuffix.toString());
        df = new Diff("expected", expectedOutput, "actual", output);

        if (!df.areDifferent()) {
          // If the two strings match, the test succeeds and we make sure
          // that there is no -ckd-* file to confuse anyone.
          (new File(fileToTest + SAVED_SUFFIX)).delete();
          (new File(fileToTest + SAVED_SUFFIX + optionalSuffix.toString())).delete();
        } else {
        // If the strings still do not match, we save the actual string and
        // fail the test.
        FileWriter f = null;
        try {
          f = new FileWriter(fileToTest + SAVED_SUFFIX + optionalSuffix.toString());
          f.write(output);
        } finally {
          if (f != null)
            f.close();
        }
        }

        return (df.result());
      }
      return checkReturnValue(fileToTest, expectedOutput, returnedValue);
    } catch (java.io.IOException e) {
      return (e.toString());
    }
  }

  //@ requires initialized;
  public/*@ nullable */String checkReturnValue(
  /*@ non_null */String fileToTest,
  /*@ non_null */String expectedOutput,
  /*@ non_null */Object returnedValue) {
    if (returnedValue instanceof Boolean) {
      return expectedStatusReport(fileToTest, ((Boolean) returnedValue)
          .booleanValue(), expectedOutput);
    } else if (returnedValue instanceof Integer) {
      return expectedStatusReport(fileToTest, ((Integer) returnedValue)
          .intValue(), expectedOutput);
    } else {
      return ("The return value is of type " + returnedValue.getClass() + " instead of int or boolean");
    }
  }

  /** Returns null if ok, otherwise returns failure message. */
  //@ requires initialized;
  public/*@ nullable */String expectedStatusReport(
  /*@ non_null */String fileToTest, int ecode,
  /*@ non_null */String expectedOutput) {
    int ret = expectedIntegerStatus(fileToTest, expectedOutput);
    if (ecode == ret)
      return null;
    return "The compile produced an invalid return value.  It should be " + ret
           + " but instead is " + ecode;
  }

  //@ requires initialized;
  public/*@ nullable */String expectedStatusReport(
  /*@ non_null */String fileToTest, boolean b,
  /*@ non_null */String expectedOutput) {
    boolean status = expectedBooleanStatus(fileToTest, expectedOutput);
    if (status == b)
      return null;
    return ("The compile produced an invalid return value.  It should be "
            + (!b) + " since there was " + (b ? "no " : "")
            + "error output but instead is " + b);
  }

  //@ requires initialized;
  public boolean expectedBooleanStatus(/*@ non_null */String fileToTest,
  /*@ non_null */String expectedOutput) {
    return expectedOutput.length() == 0;
  }

  //@ requires initialized;
  public int expectedIntegerStatus(/*@ non_null */String fileToTest,
  /*@ non_null */String expectedOutput) {
    return 0;
  }

}
