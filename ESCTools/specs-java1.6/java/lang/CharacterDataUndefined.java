package java.lang;

class CharacterDataUndefined {
    
    CharacterDataUndefined() {
        
    }
    
    static int getProperties(int ch) {
        return 0;
    }
    
    static int getType(int ch) {
        return Character.UNASSIGNED;
    }
    
    static boolean isLowerCase(int ch) {
        return false;
    }
    
    static boolean isUpperCase(int ch) {
        return false;
    }
    
    static boolean isTitleCase(int ch) {
        return false;
    }
    
    static boolean isDigit(int ch) {
        return false;
    }
    
    static boolean isDefined(int ch) {
        return false;
    }
    
    static boolean isLetter(int ch) {
        return false;
    }
    
    static boolean isLetterOrDigit(int ch) {
        return false;
    }
    
    static boolean isSpaceChar(int ch) {
        return false;
    }
    
    static boolean isJavaIdentifierStart(int ch) {
        return false;
    }
    
    static boolean isJavaIdentifierPart(int ch) {
        return false;
    }
    
    static boolean isUnicodeIdentifierStart(int ch) {
        return false;
    }
    
    static boolean isUnicodeIdentifierPart(int ch) {
        return false;
    }
    
    static boolean isIdentifierIgnorable(int ch) {
        return false;
    }
    
    static int toLowerCase(int ch) {
        return ch;
    }
    
    static int toUpperCase(int ch) {
        return ch;
    }
    
    static int toTitleCase(int ch) {
        return ch;
    }
    
    static int digit(int ch, int radix) {
        return -1;
    }
    
    static int getNumericValue(int ch) {
        return -1;
    }
    
    static boolean isWhitespace(int ch) {
        return false;
    }
    
    static byte getDirectionality(int ch) {
        return Character.DIRECTIONALITY_UNDEFINED;
    }
    
    static boolean isMirrored(int ch) {
        return false;
    }
}
