package com.sun.java.swing.plaf.windows;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public enum TMSchema$Prop extends Enum<TMSchema$Prop> {
    /*public static final*/ COLOR /* = new TMSchema$Prop("COLOR", 0, Color.class, 204) */,
    /*public static final*/ SIZE /* = new TMSchema$Prop("SIZE", 1, Dimension.class, 207) */,
    /*public static final*/ FLATMENUS /* = new TMSchema$Prop("FLATMENUS", 2, Boolean.class, 1001) */,
    /*public static final*/ BORDERONLY /* = new TMSchema$Prop("BORDERONLY", 3, Boolean.class, 2203) */,
    /*public static final*/ IMAGECOUNT /* = new TMSchema$Prop("IMAGECOUNT", 4, Integer.class, 2401) */,
    /*public static final*/ BORDERSIZE /* = new TMSchema$Prop("BORDERSIZE", 5, Integer.class, 2403) */,
    /*public static final*/ PROGRESSCHUNKSIZE /* = new TMSchema$Prop("PROGRESSCHUNKSIZE", 6, Integer.class, 2411) */,
    /*public static final*/ PROGRESSSPACESIZE /* = new TMSchema$Prop("PROGRESSSPACESIZE", 7, Integer.class, 2412) */,
    /*public static final*/ TEXTSHADOWOFFSET /* = new TMSchema$Prop("TEXTSHADOWOFFSET", 8, Point.class, 3402) */,
    /*public static final*/ NORMALSIZE /* = new TMSchema$Prop("NORMALSIZE", 9, Dimension.class, 3409) */,
    /*public static final*/ SIZINGMARGINS /* = new TMSchema$Prop("SIZINGMARGINS", 10, Insets.class, 3601) */,
    /*public static final*/ CONTENTMARGINS /* = new TMSchema$Prop("CONTENTMARGINS", 11, Insets.class, 3602) */,
    /*public static final*/ CAPTIONMARGINS /* = new TMSchema$Prop("CAPTIONMARGINS", 12, Insets.class, 3603) */,
    /*public static final*/ BORDERCOLOR /* = new TMSchema$Prop("BORDERCOLOR", 13, Color.class, 3801) */,
    /*public static final*/ FILLCOLOR /* = new TMSchema$Prop("FILLCOLOR", 14, Color.class, 3802) */,
    /*public static final*/ TEXTCOLOR /* = new TMSchema$Prop("TEXTCOLOR", 15, Color.class, 3803) */,
    /*public static final*/ TEXTSHADOWCOLOR /* = new TMSchema$Prop("TEXTSHADOWCOLOR", 16, Color.class, 3818) */,
    /*public static final*/ BGTYPE /* = new TMSchema$Prop("BGTYPE", 17, Integer.class, 4001) */,
    /*public static final*/ TEXTSHADOWTYPE /* = new TMSchema$Prop("TEXTSHADOWTYPE", 18, Integer.class, 4010) */;
    /*synthetic*/ private static final TMSchema$Prop[] $VALUES = new TMSchema$Prop[]{TMSchema$Prop.COLOR, TMSchema$Prop.SIZE, TMSchema$Prop.FLATMENUS, TMSchema$Prop.BORDERONLY, TMSchema$Prop.IMAGECOUNT, TMSchema$Prop.BORDERSIZE, TMSchema$Prop.PROGRESSCHUNKSIZE, TMSchema$Prop.PROGRESSSPACESIZE, TMSchema$Prop.TEXTSHADOWOFFSET, TMSchema$Prop.NORMALSIZE, TMSchema$Prop.SIZINGMARGINS, TMSchema$Prop.CONTENTMARGINS, TMSchema$Prop.CAPTIONMARGINS, TMSchema$Prop.BORDERCOLOR, TMSchema$Prop.FILLCOLOR, TMSchema$Prop.TEXTCOLOR, TMSchema$Prop.TEXTSHADOWCOLOR, TMSchema$Prop.BGTYPE, TMSchema$Prop.TEXTSHADOWTYPE};
    
    public static final TMSchema$Prop[] values() {
        return (TMSchema$Prop[])$VALUES.clone();
    }
    
    public static TMSchema$Prop valueOf(String name) {
        return (TMSchema$Prop)Enum.valueOf(TMSchema.Prop.class, name);
    }
    private final Class type;
    private final int value;
    
    private TMSchema$Prop(/*synthetic*/ String $enum$name, /*synthetic*/ int $enum$ordinal, Class type, int value) {
        super($enum$name, $enum$ordinal);
        this.type = type;
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public String toString() {
        return name() + "[" + type.getName() + "] = " + value;
    }
}
