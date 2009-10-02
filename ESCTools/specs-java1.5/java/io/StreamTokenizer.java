package java.io;

public class StreamTokenizer {
    private Reader reader = null;
    private InputStream input = null;
    private char[] buf = new char[20];
    private int peekc = NEED_CHAR;
    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = Integer.MAX_VALUE - 1;
    private boolean pushedBack;
    private boolean forceLower;
    private int LINENO = 1;
    private boolean eolIsSignificantP = false;
    private boolean slashSlashCommentsP = false;
    private boolean slashStarCommentsP = false;
    private byte[] ctype = new byte[256];
    private static final byte CT_WHITESPACE = 1;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_ALPHA = 4;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_COMMENT = 16;
    public int ttype = TT_NOTHING;
    public static final int TT_EOF = -1;
    public static final int TT_EOL = '\n';
    public static final int TT_NUMBER = -2;
    public static final int TT_WORD = -3;
    private static final int TT_NOTHING = -4;
    public String sval;
    public double nval;
    
    private StreamTokenizer() {
        
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars(128 + 32, 255);
        whitespaceChars(0, ' ');
        commentChar('/');
        quoteChar('\"');
        quoteChar('\'');
        parseNumbers();
    }
    
    
    public StreamTokenizer(InputStream is) {
        this();
        if (is == null) {
            throw new NullPointerException();
        }
        input = is;
    }
    
    public StreamTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        reader = r;
    }
    
    public void resetSyntax() {
        for (int i = ctype.length; --i >= 0; ) ctype[i] = 0;
    }
    
    public void wordChars(int low, int hi) {
        if (low < 0) low = 0;
        if (hi >= ctype.length) hi = ctype.length - 1;
        while (low <= hi) ctype[low++] |= CT_ALPHA;
    }
    
    public void whitespaceChars(int low, int hi) {
        if (low < 0) low = 0;
        if (hi >= ctype.length) hi = ctype.length - 1;
        while (low <= hi) ctype[low++] = CT_WHITESPACE;
    }
    
    public void ordinaryChars(int low, int hi) {
        if (low < 0) low = 0;
        if (hi >= ctype.length) hi = ctype.length - 1;
        while (low <= hi) ctype[low++] = 0;
    }
    
    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < ctype.length) ctype[ch] = 0;
    }
    
    public void commentChar(int ch) {
        if (ch >= 0 && ch < ctype.length) ctype[ch] = CT_COMMENT;
    }
    
    public void quoteChar(int ch) {
        if (ch >= 0 && ch < ctype.length) ctype[ch] = CT_QUOTE;
    }
    
    public void parseNumbers() {
        for (int i = '0'; i <= '9'; i++) ctype[i] |= CT_DIGIT;
        ctype['.'] |= CT_DIGIT;
        ctype['-'] |= CT_DIGIT;
    }
    
    public void eolIsSignificant(boolean flag) {
        eolIsSignificantP = flag;
    }
    
    public void slashStarComments(boolean flag) {
        slashStarCommentsP = flag;
    }
    
    public void slashSlashComments(boolean flag) {
        slashSlashCommentsP = flag;
    }
    
    public void lowerCaseMode(boolean fl) {
        forceLower = fl;
    }
    
    private int read() throws IOException {
        if (reader != null) return reader.read(); else if (input != null) return input.read(); else throw new IllegalStateException();
    }
    
    public int nextToken() throws IOException {
        if (pushedBack) {
            pushedBack = false;
            return ttype;
        }
        byte[] ct = ctype;
        sval = null;
        int c = peekc;
        if (c < 0) c = NEED_CHAR;
        if (c == SKIP_LF) {
            c = read();
            if (c < 0) return ttype = TT_EOF;
            if (c == '\n') c = NEED_CHAR;
        }
        if (c == NEED_CHAR) {
            c = read();
            if (c < 0) return ttype = TT_EOF;
        }
        ttype = c;
        peekc = NEED_CHAR;
        int ctype = c < 256 ? ct[c] : CT_ALPHA;
        while ((ctype & CT_WHITESPACE) != 0) {
            if (c == '\r') {
                LINENO++;
                if (eolIsSignificantP) {
                    peekc = SKIP_LF;
                    return ttype = TT_EOL;
                }
                c = read();
                if (c == '\n') c = read();
            } else {
                if (c == '\n') {
                    LINENO++;
                    if (eolIsSignificantP) {
                        return ttype = TT_EOL;
                    }
                }
                c = read();
            }
            if (c < 0) return ttype = TT_EOF;
            ctype = c < 256 ? ct[c] : CT_ALPHA;
        }
        if ((ctype & CT_DIGIT) != 0) {
            boolean neg = false;
            if (c == '-') {
                c = read();
                if (c != '.' && (c < '0' || c > '9')) {
                    peekc = c;
                    return ttype = '-';
                }
                neg = true;
            }
            double v = 0;
            int decexp = 0;
            int seendot = 0;
            while (true) {
                if (c == '.' && seendot == 0) seendot = 1; else if ('0' <= c && c <= '9') {
                    v = v * 10 + (c - '0');
                    decexp += seendot;
                } else break;
                c = read();
            }
            peekc = c;
            if (decexp != 0) {
                double denom = 10;
                decexp--;
                while (decexp > 0) {
                    denom *= 10;
                    decexp--;
                }
                v = v / denom;
            }
            nval = neg ? -v : v;
            return ttype = TT_NUMBER;
        }
        if ((ctype & CT_ALPHA) != 0) {
            int i = 0;
            do {
                if (i >= buf.length) {
                    char[] nb = new char[buf.length * 2];
                    System.arraycopy(buf, 0, nb, 0, buf.length);
                    buf = nb;
                }
                buf[i++] = (char)c;
                c = read();
                ctype = c < 0 ? CT_WHITESPACE : c < 256 ? ct[c] : CT_ALPHA;
            }             while ((ctype & (CT_ALPHA | CT_DIGIT)) != 0);
            peekc = c;
            sval = String.copyValueOf(buf, 0, i);
            if (forceLower) sval = sval.toLowerCase();
            return ttype = TT_WORD;
        }
        if ((ctype & CT_QUOTE) != 0) {
            ttype = c;
            int i = 0;
            int d = read();
            while (d >= 0 && d != ttype && d != '\n' && d != '\r') {
                if (d == '\\') {
                    c = read();
                    int first = c;
                    if (c >= '0' && c <= '7') {
                        c = c - '0';
                        int c2 = read();
                        if ('0' <= c2 && c2 <= '7') {
                            c = (c << 3) + (c2 - '0');
                            c2 = read();
                            if ('0' <= c2 && c2 <= '7' && first <= '3') {
                                c = (c << 3) + (c2 - '0');
                                d = read();
                            } else d = c2;
                        } else d = c2;
                    } else {
                        switch (c) {
                        case 'a': 
                            c = 7;
                            break;
                        
                        case 'b': 
                            c = '\b';
                            break;
                        
                        case 'f': 
                            c = 12;
                            break;
                        
                        case 'n': 
                            c = '\n';
                            break;
                        
                        case 'r': 
                            c = '\r';
                            break;
                        
                        case 't': 
                            c = '\t';
                            break;
                        
                        case 'v': 
                            c = 11;
                            break;
                        
                        }
                        d = read();
                    }
                } else {
                    c = d;
                    d = read();
                }
                if (i >= buf.length) {
                    char[] nb = new char[buf.length * 2];
                    System.arraycopy(buf, 0, nb, 0, buf.length);
                    buf = nb;
                }
                buf[i++] = (char)c;
            }
            peekc = (d == ttype) ? NEED_CHAR : d;
            sval = String.copyValueOf(buf, 0, i);
            return ttype;
        }
        if (c == '/' && (slashSlashCommentsP || slashStarCommentsP)) {
            c = read();
            if (c == '*' && slashStarCommentsP) {
                int prevc = 0;
                while ((c = read()) != '/' || prevc != '*') {
                    if (c == '\r') {
                        LINENO++;
                        c = read();
                        if (c == '\n') {
                            c = read();
                        }
                    } else {
                        if (c == '\n') {
                            LINENO++;
                            c = read();
                        }
                    }
                    if (c < 0) return ttype = TT_EOF;
                    prevc = c;
                }
                return nextToken();
            } else if (c == '/' && slashSlashCommentsP) {
                while ((c = read()) != '\n' && c != '\r' && c >= 0) ;
                peekc = c;
                return nextToken();
            } else {
                if ((ct['/'] & CT_COMMENT) != 0) {
                    while ((c = read()) != '\n' && c != '\r' && c >= 0) ;
                    peekc = c;
                    return nextToken();
                } else {
                    peekc = c;
                    return ttype = '/';
                }
            }
        }
        if ((ctype & CT_COMMENT) != 0) {
            while ((c = read()) != '\n' && c != '\r' && c >= 0) ;
            peekc = c;
            return nextToken();
        }
        return ttype = c;
    }
    
    public void pushBack() {
        if (ttype != TT_NOTHING) pushedBack = true;
    }
    
    public int lineno() {
        return LINENO;
    }
    
    public String toString() {
        String ret;
        switch (ttype) {
        case TT_EOF: 
            ret = "EOF";
            break;
        
        case TT_EOL: 
            ret = "EOL";
            break;
        
        case TT_WORD: 
            ret = sval;
            break;
        
        case TT_NUMBER: 
            ret = "n=" + nval;
            break;
        
        case TT_NOTHING: 
            ret = "NOTHING";
            break;
        
        default: 
            {
                if (ttype < 256 && ((ctype[ttype] & CT_QUOTE) != 0)) {
                    ret = sval;
                    break;
                }
                char[] s = new char[3];
                s[0] = s[2] = '\'';
                s[1] = (char)ttype;
                ret = new String(s);
                break;
            }
        
        }
        return "Token[" + ret + "], line " + LINENO;
    }
}
