// COpyright 2004, David Cok

package escjava.gui;

import java.lang.reflect.Field;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import escjava.translate.NoWarn;
import escjava.ast.TagConstants;

public class EscOptions extends JPanel implements ActionListener {

    /** A reference to the Options structure in the ESC tool,
	which serves as the document for this GUI. 
     */
    protected escjava.Options doc;

    /** A reference to the Class object of escjava.Options.
     */
    static final public Class escoptions = escjava.Options.class;

    /** These are the options that are actually portrayed in the
	GUI.  The array contains the text string, field name and the tooltip.
     */
    static String[][] optionsShown = {
	{"Control of Input:", null, null},
	{"source 1.4", "assertIsKeyword",	
		"When enabled, Java 1.4 source (including assert statements)\n"+
		"is parsed; when disabled 'assert' is treated as a nomal identifier" },

	{"enableassertions", "assertionsEnabled",
		"When enabled, Java assert statements are enabled;\n"+
		"when disabled, Java assert statements are ignored." },
	//{"jmlAssertions", 	
	//	"When enabled, Java assert statements are\n"+
	//	"treated like JML assert statements." },
	{"parsePlus", "parsePlus",
		"When enabled, JML annotations in '//@' and\n"+
		"/*@' comments are parsed by the ESC/Java2 tool" },
	{"Control of Output:",null,null},
	{"noCautions", 	"noCautions",
		"When enabled, no Caution meesages are output" },
	{"noSemicolonWarnings", "noSemicolonWarnings",
		"When enabled, no warnings about missing semicolons\n"+
		"are issued (JML requires terminating semicolons)" },
	{"noNotCheckedWarnings", "noNotCheckedWarnings",
		"When enabled, no warnings about JML features that\n"+
		"are not implemented in ESC/Java2 are issued" },

	{"Debugging:", null, null},
	{"showErrorLocation", "showErrorLocation",	"TBD description" },
	{"showDesugaredSpecs", "desugaredSpecs",	"TBD description" },
	//{"pxLog log", 		"TBD description" },
	{"pgc", "pgc", 		"TBD description" },
	{"pdsa", "pdsa",		"TBD description" },
	{"pvc", "pvc", 		"TBD description" },
	{"testMode", "testMode",		"TBD description" },
	};

    public EscOptions(escjava.Options doc) {
	init(doc);
    }

    public void init(escjava.Options doc) {
	this.doc = doc;
	removeAll();
	JButton jb;
	setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));

	JPanel misc = new JPanel();
	misc.setLayout(new BoxLayout(misc,BoxLayout.PAGE_AXIS));
	add(misc);
	JPanel warningsB = new JPanel();
	warningsB.setLayout(new BoxLayout(warningsB,BoxLayout.PAGE_AXIS));
	add(warningsB);

	JPanel warningsHeader = new JPanel();
	warningsHeader.setLayout(new BoxLayout(warningsHeader,BoxLayout.LINE_AXIS));
	warningsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
	warningsB.add(warningsHeader);

	JPanel warnings = new JPanel(new GridLayout(0,2));
	warnings.setAlignmentX(Component.LEFT_ALIGNMENT);
	warningsB.add(warnings);

	warningsB.add(Box.createVerticalGlue());
	
	warningsHeader.add(new JLabel(" Warning Types:  "));
	//warningsHeader.add(Box.createHorizontalGlue());
	warningsHeader.add(jb = new JButton("Disable All"));
	jb.addActionListener( new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		NoWarn.setAllChkStatus(TagConstants.CHK_AS_ASSUME);
		init(GUI.gui.options());
	    }
	});
	warningsHeader.add(jb = new JButton("Enable All"));
	jb.addActionListener( new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		NoWarn.setAllChkStatus(TagConstants.CHK_AS_ASSERT);
		init(GUI.gui.options());
	    }
	});

	final JTextField simplify = new JTextField(System.getProperty("simplify"));
	JPanel simplifyHeader = new JPanel();
	simplifyHeader.setLayout(new BoxLayout(simplifyHeader,BoxLayout.LINE_AXIS));
	simplifyHeader.add(new JLabel("Path to SIMPLIFY executable:  "));
	//simplifyHeader.add(Box.createHorizontalGlue());
	simplifyHeader.add(jb = new JButton("Browse"));
	jb.addActionListener( new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	        JFileChooser fc = new JFileChooser();
		fc.setApproveButtonText("Select");
		int returnVal = fc.showOpenDialog(EscOptions.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    java.io.File file = fc.getSelectedFile();
		    simplify.setText(file.getAbsolutePath());
		}
	    }
	});
	simplifyHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

	misc.add(simplifyHeader);

	// For some reason the maximum height of a JTextField is very large,
	// so it does not behave well in a Boxlayout.
	simplify.setColumns(30);
	simplify.setMaximumSize(simplify.getPreferredSize());
	//simplify.setMaximumSize(new Dimension(
	 // simplify.getMaximumSize().width, simplify.getPreferredSize().height));
	misc.add(simplify);
	simplify.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.setProperty("simplify",simplify.getText());
	        }
	    });
	simplify.addFocusListener(
	    new FocusAdapter() {
		public void focusLost(FocusEvent e) {
		    System.setProperty("simplify",simplify.getText());
	        }
	    });
	simplify.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	JCheckBox cb;
	for (int i = 0; i<optionsShown.length; ++i) {
	    String[] opttext = optionsShown[i];
	    if (opttext[1] == null) {
		misc.add(new JLabel(opttext[0]));
	    } else {
		try {
		    Field f = escoptions.getField(opttext[1]);
		    boolean b = f.getBoolean(escjava.Main.options());
		    cb = new JCheckBox(opttext[0],b);
		    cb.setAlignmentX(Component.LEFT_ALIGNMENT);
		    cb.setToolTipText(opttext[2]);
		    cb.addActionListener(this);
		    misc.add(cb);
		} catch (Exception e) {
		    System.out.println("FAILED TO RECOGNIZE OPTION " + i + ": " + opttext[0] + " " + e);
		}
	    }
	}
	misc.add(Box.createVerticalGlue());

	ActionListener a = new MListener();
	String[] wnames = escjava.ast.TagConstants.escchecks;
	int n = wnames.length-4;
	int nn = n/2;
	int np =n-nn;
	for (int i=0; i<nn; ++i) {
	    makeWarningOpt(wnames[i],warnings,a);
	    makeWarningOpt(wnames[i+np],warnings,a);
	}
	if (n%2 == 1) {
	    makeWarningOpt(wnames[nn],warnings,a);
	}
    }

    public void makeWarningOpt(String name, JPanel warnings, ActionListener a) {
	int tag = NoWarn.toNoWarnTag(name);
	boolean b = NoWarn.getChkStatus(tag) != TagConstants.CHK_AS_ASSUME;
	JCheckBox cb = new JCheckBox(name,b);
	warnings.add(cb);
	cb.addActionListener(a);
	//cb.setToolTipText(opttext[2]);
    }

    public void actionPerformed(ActionEvent e) {
	// write back out to the Options structure

	Object source = e.getSource();
	String name = null;
	if (source instanceof JCheckBox) {
	    String fname = null;
	    name = ((JCheckBox)source).getText();
	    for (int i=0; i<optionsShown.length; ++i) {
		if (name.equals(optionsShown[i][0])) {
		    fname = optionsShown[i][1];
		    break;
		}
	    }
	    boolean value = ((JCheckBox)source).isSelected();
	    try {
		Field f = escoptions.getField(fname);
		f.setBoolean(escjava.Main.options(),value);
	    } catch (Exception ee) {
		System.out.println("FAILED TO RECOGNIZE OPTION: " + name + " " + ee);
	    }
	} else {
	    System.out.println("UNKNOWN GUI OPTION " + name);
	}
    }

    static public class MListener implements ActionListener {
	static private String[] temp = new String[1];
	public void actionPerformed(ActionEvent e) {
	    Object o = e.getSource();
	    if (o instanceof JCheckBox) {
		JCheckBox cb = (JCheckBox)o;
		boolean b = cb.isSelected();
		temp[0] = cb.getText();
		try {
		    GUI.gui.options.processOption(
			b ? "-warn" : "-nowarn", temp, 0);
		} catch (javafe.util.UsageError ee) {
		    // FIXME - internal error if this happens
		}
	    }
	}
    }

}
