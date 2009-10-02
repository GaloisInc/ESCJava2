package javax.swing.text;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.*;
import javax.swing.UIManager;

class DefaultEditorKit$DeleteNextCharAction extends TextAction {
    
    DefaultEditorKit$DeleteNextCharAction() {
        super("delete-next");
    }
    
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        boolean beep = true;
        if ((target != null) && (target.isEditable())) {
            try {
                Document doc = target.getDocument();
                Caret caret = target.getCaret();
                int dot = caret.getDot();
                int mark = caret.getMark();
                if (dot != mark) {
                    doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                    beep = false;
                } else if (dot < doc.getLength()) {
                    int delChars = 1;
                    if (dot < doc.getLength() - 1) {
                        String dotChars = doc.getText(dot, 2);
                        char c0 = dotChars.charAt(0);
                        char c1 = dotChars.charAt(1);
                        if (c0 >= '\ud800' && c0 <= '\udbff' && c1 >= '\udc00' && c1 <= '\udfff') {
                            delChars = 2;
                        }
                    }
                    doc.remove(dot, delChars);
                    beep = false;
                }
            } catch (BadLocationException bl) {
            }
        }
        if (beep) {
            UIManager.getLookAndFeel().provideErrorFeedback(target);
        }
    }
}
