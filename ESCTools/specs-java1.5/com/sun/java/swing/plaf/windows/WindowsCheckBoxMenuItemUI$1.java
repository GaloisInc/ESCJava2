package com.sun.java.swing.plaf.windows;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.TMSchema.Part;
import com.sun.java.swing.plaf.windows.TMSchema.State;

class WindowsCheckBoxMenuItemUI$1 implements WindowsMenuItemUIAccessor {
    /*synthetic*/ final WindowsCheckBoxMenuItemUI this$0;
    
    WindowsCheckBoxMenuItemUI$1(/*synthetic*/ final WindowsCheckBoxMenuItemUI this$0) {
        this.this$0 = this$0;
        
    }
    
    public JMenuItem getMenuItem() {
        return WindowsCheckBoxMenuItemUI.access$000(this$0);
    }
    
    public TMSchema$State getState(JMenuItem menuItem) {
        return WindowsMenuItemUI.getState(this, menuItem);
    }
    
    public TMSchema$Part getPart(JMenuItem menuItem) {
        return WindowsMenuItemUI.getPart(this, menuItem);
    }
}
