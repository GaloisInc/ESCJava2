package java.lang;

import java.io.*;
import java.util.StringTokenizer;

public class Runtime {
    private static Runtime currentRuntime = new Runtime();
    
    public static Runtime getRuntime() {
        return currentRuntime;
    }
    
    private Runtime() {
        
    }
    
    public void exit(int status) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkExit(status);
        }
        Shutdown.exit(status);
    }
    
    public void addShutdownHook(Thread hook) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        Shutdown.add(hook);
    }
    
    public boolean removeShutdownHook(Thread hook) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
        }
        return Shutdown.remove(hook);
    }
    
    public void halt(int status) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkExit(status);
        }
        Shutdown.halt(status);
    }
    
    
    public static void runFinalizersOnExit(boolean value) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkExit(0);
            } catch (SecurityException e) {
                throw new SecurityException("runFinalizersOnExit");
            }
        }
        Shutdown.setRunFinalizersOnExit(value);
    }
    
    public Process exec(String command) throws IOException {
        return exec(command, null, null);
    }
    
    public Process exec(String command, String[] envp) throws IOException {
        return exec(command, envp, null);
    }
    
    public Process exec(String command, String[] envp, File dir) throws IOException {
        if (command.length() == 0) throw new IllegalArgumentException("Empty command");
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) cmdarray[i] = st.nextToken();
        return exec(cmdarray, envp, dir);
    }
    
    public Process exec(String[] cmdarray) throws IOException {
        return exec(cmdarray, null, null);
    }
    
    public Process exec(String[] cmdarray, String[] envp) throws IOException {
        return exec(cmdarray, envp, null);
    }
    
    public Process exec(String[] cmdarray, String[] envp, File dir) throws IOException {
        return new ProcessBuilder(cmdarray).environment(envp).directory(dir).start();
    }
    
    public native int availableProcessors();
    
    public native long freeMemory();
    
    public native long totalMemory();
    
    public native long maxMemory();
    
    public native void gc();
    
    private static native void runFinalization0();
    
    public void runFinalization() {
        runFinalization0();
    }
    
    public native void traceInstructions(boolean on);
    
    public native void traceMethodCalls(boolean on);
    
    public void load(String filename) {
        load0(System.getCallerClass(), filename);
    }
    
    synchronized void load0(Class fromClass, String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(filename);
        }
        if (!(new File(filename).isAbsolute())) {
            throw new UnsatisfiedLinkError("Expecting an absolute path of the library: " + filename);
        }
        ClassLoader.loadLibrary(fromClass, filename, true);
    }
    
    public void loadLibrary(String libname) {
        loadLibrary0(System.getCallerClass(), libname);
    }
    
    synchronized void loadLibrary0(Class fromClass, String libname) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkLink(libname);
        }
        if (libname.indexOf((int)File.separatorChar) != -1) {
            throw new UnsatisfiedLinkError("Directory separator should not appear in library name: " + libname);
        }
        ClassLoader.loadLibrary(fromClass, libname, false);
    }
    
    
    public InputStream getLocalizedInputStream(InputStream in) {
        return in;
    }
    
    
    public OutputStream getLocalizedOutputStream(OutputStream out) {
        return out;
    }
}
