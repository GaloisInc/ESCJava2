package java.lang;

import java.io.*;

public class StringCoding {

    public static char[] decode(byte[] b, int offset, int len);

    public static byte[] encode(char[] c, int offset, int len);

    public static char[] decode(String encoding, byte[] b, int offset, int len)
          throws UnsupportedEncodingException;

    public static byte[] encode(String encoding, char[] c, int offset, int len)
          throws UnsupportedEncodingException;

}
