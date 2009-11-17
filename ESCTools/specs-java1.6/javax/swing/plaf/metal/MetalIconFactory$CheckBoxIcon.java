package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.io.Serializable;

class MetalIconFactory$CheckBoxIcon implements Icon, UIResource, Serializable {
    
    /*synthetic*/ MetalIconFactory$CheckBoxIcon(javax.swing.plaf.metal.MetalIconFactory$1 x0) {
        this();
    }
    
    private MetalIconFactory$CheckBoxIcon() {
        
    }
    
    protected int getControlSize() {
        return 13;
    }
    
    private void paintOceanIcon(Component c, Graphics g, int x, int y) {
        ButtonModel model = ((JCheckBox)(JCheckBox)c).getModel();
        g.translate(x, y);
        int w = getIconWidth();
        int h = getIconHeight();
        if (model.isEnabled()) {
            if (model.isPressed() && model.isArmed()) {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.fillRect(0, 0, w, h);
                g.setColor(MetalLookAndFeel.getControlDarkShadow());
                g.fillRect(0, 0, w, 2);
                g.fillRect(0, 2, 2, h - 2);
                g.fillRect(w - 1, 1, 1, h - 1);
                g.fillRect(1, h - 1, w - 2, 1);
            } else if (model.isRollover()) {
                MetalUtils.drawGradient(c, g, "CheckBox.gradient", 0, 0, w, h, true);
                g.setColor(MetalLookAndFeel.getControlDarkShadow());
                g.drawRect(0, 0, w - 1, h - 1);
                g.setColor(MetalLookAndFeel.getPrimaryControl());
                g.drawRect(1, 1, w - 3, h - 3);
                g.drawRect(2, 2, w - 5, h - 5);
            } else {
                MetalUtils.drawGradient(c, g, "CheckBox.gradient", 0, 0, w, h, true);
                g.setColor(MetalLookAndFeel.getControlDarkShadow());
                g.drawRect(0, 0, w - 1, h - 1);
            }
            g.setColor(MetalLookAndFeel.getControlInfo());
        } else {
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w - 1, h - 1);
        }
        g.translate(-x, -y);
        if (model.isSelected()) {
            drawCheck(c, g, x, y);
        }
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (MetalLookAndFeel.usingOcean()) {
            paintOceanIcon(c, g, x, y);
            return;
        }
        ButtonModel model = ((JCheckBox)(JCheckBox)c).getModel();
        int controlSize = getControlSize();
        if (model.isEnabled()) {
            if (model.isPressed() && model.isArmed()) {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.fillRect(x, y, controlSize - 1, controlSize - 1);
                MetalUtils.drawPressed3DBorder(g, x, y, controlSize, controlSize);
            } else {
                MetalUtils.drawFlush3DBorder(g, x, y, controlSize, controlSize);
            }
            g.setColor(MetalLookAndFeel.getControlInfo());
        } else {
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawRect(x, y, controlSize - 2, controlSize - 2);
        }
        if (model.isSelected()) {
            drawCheck(c, g, x, y);
        }
    }
    
    protected void drawCheck(Component c, Graphics g, int x, int y) {
        int controlSize = getControlSize();
        g.fillRect(x + 3, y + 5, 2, controlSize - 8);
        g.drawLine(x + (controlSize - 4), y + 3, x + 5, y + (controlSize - 6));
        g.drawLine(x + (controlSize - 4), y + 4, x + 5, y + (controlSize - 5));
    }
    
    public int getIconWidth() {
        return getControlSize();
    }
    
    public int getIconHeight() {
        return getControlSize();
    }
}
