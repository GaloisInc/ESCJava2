package javax.swing;

import java.awt.*;
import java.util.*;

class DebugGraphicsInfo {
    
    DebugGraphicsInfo() {
        
    }
    Color flashColor = Color.red;
    int flashTime = 100;
    int flashCount = 2;
    Hashtable componentToDebug;
    JFrame debugFrame = null;
    java.io.PrintStream stream = System.out;
    
    void setDebugOptions(JComponent component, int debug) {
        if (debug == 0) {
            return;
        }
        if (componentToDebug == null) {
            componentToDebug = new Hashtable();
        }
        if (debug > 0) {
            componentToDebug.put(component, new Integer(debug));
        } else {
            componentToDebug.remove(component);
        }
    }
    
    int getDebugOptions(JComponent component) {
        if (componentToDebug == null) {
            return 0;
        } else {
            Integer integer = (Integer)(Integer)componentToDebug.get(component);
            return integer == null ? 0 : integer.intValue();
        }
    }
    
    void log(String string) {
        stream.println(string);
    }
}
