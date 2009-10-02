package java.io;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

class UnixFileSystem extends FileSystem {
    /*synthetic*/ static final boolean $assertionsDisabled = !UnixFileSystem.class.desiredAssertionStatus();
    private final char slash;
    private final char colon;
    private final String javaHome;
    
    public UnixFileSystem() {
        
        slash = ((String)(String)AccessController.doPrivileged(new GetPropertyAction("file.separator"))).charAt(0);
        colon = ((String)(String)AccessController.doPrivileged(new GetPropertyAction("path.separator"))).charAt(0);
        javaHome = (String)(String)AccessController.doPrivileged(new GetPropertyAction("java.home"));
    }
    
    public char getSeparator() {
        return slash;
    }
    
    public char getPathSeparator() {
        return colon;
    }
    
    private String normalize(String pathname, int len, int off) {
        if (len == 0) return pathname;
        int n = len;
        while ((n > 0) && (pathname.charAt(n - 1) == '/')) n--;
        if (n == 0) return "/";
        StringBuffer sb = new StringBuffer(pathname.length());
        if (off > 0) sb.append(pathname.substring(0, off));
        char prevChar = 0;
        for (int i = off; i < n; i++) {
            char c = pathname.charAt(i);
            if ((prevChar == '/') && (c == '/')) continue;
            sb.append(c);
            prevChar = c;
        }
        return sb.toString();
    }
    
    public String normalize(String pathname) {
        int n = pathname.length();
        char prevChar = 0;
        for (int i = 0; i < n; i++) {
            char c = pathname.charAt(i);
            if ((prevChar == '/') && (c == '/')) return normalize(pathname, n, i - 1);
            prevChar = c;
        }
        if (prevChar == '/') return normalize(pathname, n, n - 1);
        return pathname;
    }
    
    public int prefixLength(String pathname) {
        if (pathname.length() == 0) return 0;
        return (pathname.charAt(0) == '/') ? 1 : 0;
    }
    
    public String resolve(String parent, String child) {
        if (child.equals("")) return parent;
        if (child.charAt(0) == '/') {
            if (parent.equals("/")) return child;
            return parent + child;
        }
        if (parent.equals("/")) return parent + child;
        return parent + '/' + child;
    }
    
    public String getDefaultParent() {
        return "/";
    }
    
    public String fromURIPath(String path) {
        String p = path;
        if (p.endsWith("/") && (p.length() > 1)) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }
    
    public boolean isAbsolute(File f) {
        return (f.getPrefixLength() != 0);
    }
    
    public String resolve(File f) {
        if (isAbsolute(f)) return f.getPath();
        return resolve(System.getProperty("user.dir"), f.getPath());
    }
    private ExpiringCache cache = new ExpiringCache();
    private ExpiringCache javaHomePrefixCache = new ExpiringCache();
    
    public String canonicalize(String path) throws IOException {
        if (!useCanonCaches) {
            return canonicalize0(path);
        } else {
            String res = cache.get(path);
            if (res == null) {
                String dir = null;
                String resDir = null;
                if (useCanonPrefixCache) {
                    dir = parentOrNull(path);
                    if (dir != null) {
                        resDir = javaHomePrefixCache.get(dir);
                        if (resDir != null) {
                            String filename = path.substring(1 + dir.length());
                            res = resDir + slash + filename;
                            cache.put(dir + slash + filename, res);
                        }
                    }
                }
                if (res == null) {
                    res = canonicalize0(path);
                    cache.put(path, res);
                    if (useCanonPrefixCache && dir != null && dir.startsWith(javaHome)) {
                        resDir = parentOrNull(res);
                        if (resDir != null && resDir.equals(dir)) {
                            File f = new File(res);
                            if (f.exists() && !f.isDirectory()) {
                                javaHomePrefixCache.put(dir, resDir);
                            }
                        }
                    }
                }
            }
            if (!$assertionsDisabled && !(canonicalize0(path).equals(res) || path.startsWith(javaHome))) throw new AssertionError();
            return res;
        }
    }
    
    private native String canonicalize0(String path) throws IOException;
    
    static String parentOrNull(String path) {
        if (path == null) return null;
        char sep = File.separatorChar;
        int last = path.length() - 1;
        int idx = last;
        int adjacentDots = 0;
        int nonDotCount = 0;
        while (idx > 0) {
            char c = path.charAt(idx);
            if (c == '.') {
                if (++adjacentDots >= 2) {
                    return null;
                }
            } else if (c == sep) {
                if (adjacentDots == 1 && nonDotCount == 0) {
                    return null;
                }
                if (idx == 0 || idx >= last - 1 || path.charAt(idx - 1) == sep) {
                    return null;
                }
                return path.substring(0, idx);
            } else {
                ++nonDotCount;
                adjacentDots = 0;
            }
            --idx;
        }
        return null;
    }
    
    public native int getBooleanAttributes0(File f);
    
    public int getBooleanAttributes(File f) {
        int rv = getBooleanAttributes0(f);
        String name = f.getName();
        boolean hidden = (name.length() > 0) && (name.charAt(0) == '.');
        return rv | (hidden ? BA_HIDDEN : 0);
    }
    
    public native boolean checkAccess(File f, boolean write);
    
    public native long getLastModifiedTime(File f);
    
    public native long getLength(File f);
    
    public native boolean createFileExclusively(String path) throws IOException;
    
    public boolean delete(File f) {
        cache.clear();
        javaHomePrefixCache.clear();
        return delete0(f);
    }
    
    private native boolean delete0(File f);
    
    public synchronized native boolean deleteOnExit(File f);
    
    public native String[] list(File f);
    
    public native boolean createDirectory(File f);
    
    public boolean rename(File f1, File f2) {
        cache.clear();
        javaHomePrefixCache.clear();
        return rename0(f1, f2);
    }
    
    private native boolean rename0(File f1, File f2);
    
    public native boolean setLastModifiedTime(File f, long time);
    
    public native boolean setReadOnly(File f);
    
    public File[] listRoots() {
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkRead("/");
            }
            return new File[]{new File("/")};
        } catch (SecurityException x) {
            return new File[0];
        }
    }
    
    public int compare(File f1, File f2) {
        return f1.getPath().compareTo(f2.getPath());
    }
    
    public int hashCode(File f) {
        return f.getPath().hashCode() ^ 1234321;
    }
    
    private static native void initIDs();
    static {
        initIDs();
    }
}
