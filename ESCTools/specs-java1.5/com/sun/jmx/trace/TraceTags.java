package com.sun.jmx.trace;

public interface TraceTags {
    public static final int LEVEL_ERROR = 0;
    public static final int LEVEL_TRACE = 1;
    public static final int LEVEL_DEBUG = 2;
    public static final int INFO_MBEANSERVER = 1;
    public static final int INFO_MLET = 2;
    public static final int INFO_MONITOR = 4;
    public static final int INFO_TIMER = 8;
    public static final int INFO_MISC = 16;
    public static final int INFO_NOTIFICATION = 32;
    public static final int INFO_RELATION = 64;
    public static final int INFO_MODELMBEAN = 128;
    public static final int INFO_ADAPTOR_SNMP = 256;
    public static final int INFO_SNMP = 512;
}
