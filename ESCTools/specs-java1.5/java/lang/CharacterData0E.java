package java.lang;

class CharacterData0E {
    /*synthetic*/ static final boolean $assertionsDisabled = !CharacterData0E.class.desiredAssertionStatus();
    
    CharacterData0E() {
        
    }
    
    static int getProperties(int ch) {
        char offset = (char)ch;
        int props = A[Y[X[offset >> 5] | ((offset >> 1) & 15)] | (offset & 1)];
        return props;
    }
    
    static int getType(int ch) {
        int props = getProperties(ch);
        return (props & 31);
    }
    
    static boolean isLowerCase(int ch) {
        int type = getType(ch);
        return (type == Character.LOWERCASE_LETTER);
    }
    
    static boolean isUpperCase(int ch) {
        int type = getType(ch);
        return (type == Character.UPPERCASE_LETTER);
    }
    
    static boolean isTitleCase(int ch) {
        int type = getType(ch);
        return (type == Character.TITLECASE_LETTER);
    }
    
    static boolean isDigit(int ch) {
        int type = getType(ch);
        return (type == Character.DECIMAL_DIGIT_NUMBER);
    }
    
    static boolean isDefined(int ch) {
        int type = getType(ch);
        return (type != Character.UNASSIGNED);
    }
    
    static boolean isLetter(int ch) {
        int type = getType(ch);
        return (((((1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER) | (1 << Character.TITLECASE_LETTER) | (1 << Character.MODIFIER_LETTER) | (1 << Character.OTHER_LETTER)) >> type) & 1) != 0);
    }
    
    static boolean isLetterOrDigit(int ch) {
        int type = getType(ch);
        return (((((1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER) | (1 << Character.TITLECASE_LETTER) | (1 << Character.MODIFIER_LETTER) | (1 << Character.OTHER_LETTER) | (1 << Character.DECIMAL_DIGIT_NUMBER)) >> type) & 1) != 0);
    }
    
    static boolean isSpaceChar(int ch) {
        int type = getType(ch);
        return (((((1 << Character.SPACE_SEPARATOR) | (1 << Character.LINE_SEPARATOR) | (1 << Character.PARAGRAPH_SEPARATOR)) >> type) & 1) != 0);
    }
    
    static boolean isJavaIdentifierStart(int ch) {
        int props = getProperties(ch);
        return ((props & 28672) >= 20480);
    }
    
    static boolean isJavaIdentifierPart(int ch) {
        int props = getProperties(ch);
        return ((props & 12288) != 0);
    }
    
    static boolean isUnicodeIdentifierStart(int ch) {
        int props = getProperties(ch);
        return ((props & 28672) == 28672);
    }
    
    static boolean isUnicodeIdentifierPart(int ch) {
        int props = getProperties(ch);
        return ((props & 4096) != 0);
    }
    
    static boolean isIdentifierIgnorable(int ch) {
        int props = getProperties(ch);
        return ((props & 28672) == 4096);
    }
    
    static int toLowerCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);
        if ((val & 131072) != 0) {
            int offset = val << 5 >> (5 + 18);
            mapChar = ch + offset;
        }
        return mapChar;
    }
    
    static int toUpperCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);
        if ((val & 65536) != 0) {
            int offset = val << 5 >> (5 + 18);
            mapChar = ch - offset;
        }
        return mapChar;
    }
    
    static int toTitleCase(int ch) {
        int mapChar = ch;
        int val = getProperties(ch);
        if ((val & 32768) != 0) {
            if ((val & 65536) == 0) {
                mapChar = ch + 1;
            } else if ((val & 131072) == 0) {
                mapChar = ch - 1;
            }
        } else if ((val & 65536) != 0) {
            mapChar = toUpperCase(ch);
        }
        return mapChar;
    }
    
    static int digit(int ch, int radix) {
        int value = -1;
        if (radix >= Character.MIN_RADIX && radix <= Character.MAX_RADIX) {
            int val = getProperties(ch);
            int kind = val & 31;
            if (kind == Character.DECIMAL_DIGIT_NUMBER) {
                value = ch + ((val & 992) >> 5) & 31;
            } else if ((val & 3072) == 3072) {
                value = (ch + ((val & 992) >> 5) & 31) + 10;
            }
        }
        return (value < radix) ? value : -1;
    }
    
    static int getNumericValue(int ch) {
        int val = getProperties(ch);
        int retval = -1;
        switch (val & 3072) {
        default: 
        
        case (0): 
            retval = -1;
            break;
        
        case (1024): 
            retval = ch + ((val & 992) >> 5) & 31;
            break;
        
        case (2048): 
            retval = -2;
            break;
        
        case (3072): 
            retval = (ch + ((val & 992) >> 5) & 31) + 10;
            break;
        
        }
        return retval;
    }
    
    static boolean isWhitespace(int ch) {
        int props = getProperties(ch);
        return ((props & 28672) == 16384);
    }
    
    static byte getDirectionality(int ch) {
        int val = getProperties(ch);
        byte directionality = (byte)((val & 2013265920) >> 27);
        if (directionality == 15) {
            directionality = Character.DIRECTIONALITY_UNDEFINED;
        }
        return directionality;
    }
    
    static boolean isMirrored(int ch) {
        int props = getProperties(ch);
        return ((props & -2147483648) != 0);
    }
    static final char[] X = ("\000\020\020\020    0000000@                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                ").toCharArray();
    static final char[] Y = ("\000\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\002\002\002\002\002\002\002\002").toCharArray();
    static final int[] A = new int[8];
    static final String A_DATA = "\u7800\000\u4800\u1010\u7800\000\u7800\000\u4800\u1010\u4800\u1010\u4000\u3006\u4000\u3006";
    static {
        {
            char[] data = A_DATA.toCharArray();
            if (!$assertionsDisabled && !(data.length == (8 * 2))) throw new AssertionError();
            int i = 0;
            int j = 0;
            while (i < (8 * 2)) {
                int entry = data[i++] << 16;
                A[j++] = entry | data[i++];
            }
        }
    }
}
