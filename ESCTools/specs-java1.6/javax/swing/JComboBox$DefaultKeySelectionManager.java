package javax.swing;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.border.*;
import javax.accessibility.*;

class JComboBox$DefaultKeySelectionManager implements JComboBox$KeySelectionManager, Serializable {
    /*synthetic*/ final JComboBox this$0;
    
    JComboBox$DefaultKeySelectionManager(/*synthetic*/ final JComboBox this$0) {
        this.this$0 = this$0;
        
    }
    
    public int selectionForKey(char aKey, ComboBoxModel aModel) {
        int i;
        int c;
        int currentSelection = -1;
        Object selectedItem = aModel.getSelectedItem();
        String v;
        String pattern;
        if (selectedItem != null) {
            for (i = 0, c = aModel.getSize(); i < c; i++) {
                if (selectedItem == aModel.getElementAt(i)) {
                    currentSelection = i;
                    break;
                }
            }
        }
        pattern = ("" + aKey).toLowerCase();
        aKey = pattern.charAt(0);
        for (i = ++currentSelection, c = aModel.getSize(); i < c; i++) {
            Object elem = aModel.getElementAt(i);
            if (elem != null && elem.toString() != null) {
                v = elem.toString().toLowerCase();
                if (v.length() > 0 && v.charAt(0) == aKey) return i;
            }
        }
        for (i = 0; i < currentSelection; i++) {
            Object elem = aModel.getElementAt(i);
            if (elem != null && elem.toString() != null) {
                v = elem.toString().toLowerCase();
                if (v.length() > 0 && v.charAt(0) == aKey) return i;
            }
        }
        return -1;
    }
}
