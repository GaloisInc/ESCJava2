package javax.swing.plaf.basic;

import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Toolkit;
import java.io.*;
import java.awt.Dimension;
import java.security.AccessController;
import java.util.*;
import java.lang.reflect.*;
import javax.sound.sampled.*;
import sun.awt.AppContext;
import sun.swing.SwingLazyValue;
import com.sun.java.swing.SwingUtilities2;
import javax.swing.LookAndFeel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.text.DefaultEditorKit;
import java.beans.PropertyChangeListener;

public abstract class BasicLookAndFeel extends LookAndFeel implements Serializable {
    
    /*synthetic*/ static Clip access$202(BasicLookAndFeel x0, Clip x1) {
        return x0.clipPlaying = x1;
    }
    
    /*synthetic*/ static Clip access$200(BasicLookAndFeel x0) {
        return x0.clipPlaying;
    }
    
    /*synthetic*/ static Object access$100(BasicLookAndFeel x0) {
        return x0.audioLock;
    }
    
    /*synthetic*/ static byte[] access$000(BasicLookAndFeel x0, String x1) {
        return x0.loadAudioData(x1);
    }
    
    public BasicLookAndFeel() {
        
    }
    static boolean hasPopups;
    private Object audioLock = new Object();
    private Clip clipPlaying;
    BasicLookAndFeel$PopupInvocationHelper invocator = null;
    private PropertyChangeListener disposer = null;
    
    public UIDefaults getDefaults() {
        UIDefaults table = new UIDefaults();
        initClassDefaults(table);
        initSystemColorDefaults(table);
        initComponentDefaults(table);
        return table;
    }
    
    public void initialize() {
        if (hasPopups) {
            createdPopup();
        }
    }
    
    public void uninitialize() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (invocator != null) {
            AccessController.doPrivileged(invocator);
            invocator = null;
        }
        if (disposer != null) {
            AppContext.getAppContext().removePropertyChangeListener(AppContext.GUI_DISPOSED, disposer);
            disposer = null;
        }
    }
    
    protected void initClassDefaults(UIDefaults table) {
        final String basicPackageName = "javax.swing.plaf.basic.";
        Object[] uiDefaults = {"ButtonUI", basicPackageName + "BasicButtonUI", "CheckBoxUI", basicPackageName + "BasicCheckBoxUI", "ColorChooserUI", basicPackageName + "BasicColorChooserUI", "FormattedTextFieldUI", basicPackageName + "BasicFormattedTextFieldUI", "MenuBarUI", basicPackageName + "BasicMenuBarUI", "MenuUI", basicPackageName + "BasicMenuUI", "MenuItemUI", basicPackageName + "BasicMenuItemUI", "CheckBoxMenuItemUI", basicPackageName + "BasicCheckBoxMenuItemUI", "RadioButtonMenuItemUI", basicPackageName + "BasicRadioButtonMenuItemUI", "RadioButtonUI", basicPackageName + "BasicRadioButtonUI", "ToggleButtonUI", basicPackageName + "BasicToggleButtonUI", "PopupMenuUI", basicPackageName + "BasicPopupMenuUI", "ProgressBarUI", basicPackageName + "BasicProgressBarUI", "ScrollBarUI", basicPackageName + "BasicScrollBarUI", "ScrollPaneUI", basicPackageName + "BasicScrollPaneUI", "SplitPaneUI", basicPackageName + "BasicSplitPaneUI", "SliderUI", basicPackageName + "BasicSliderUI", "SeparatorUI", basicPackageName + "BasicSeparatorUI", "SpinnerUI", basicPackageName + "BasicSpinnerUI", "ToolBarSeparatorUI", basicPackageName + "BasicToolBarSeparatorUI", "PopupMenuSeparatorUI", basicPackageName + "BasicPopupMenuSeparatorUI", "TabbedPaneUI", basicPackageName + "BasicTabbedPaneUI", "TextAreaUI", basicPackageName + "BasicTextAreaUI", "TextFieldUI", basicPackageName + "BasicTextFieldUI", "PasswordFieldUI", basicPackageName + "BasicPasswordFieldUI", "TextPaneUI", basicPackageName + "BasicTextPaneUI", "EditorPaneUI", basicPackageName + "BasicEditorPaneUI", "TreeUI", basicPackageName + "BasicTreeUI", "LabelUI", basicPackageName + "BasicLabelUI", "ListUI", basicPackageName + "BasicListUI", "ToolBarUI", basicPackageName + "BasicToolBarUI", "ToolTipUI", basicPackageName + "BasicToolTipUI", "ComboBoxUI", basicPackageName + "BasicComboBoxUI", "TableUI", basicPackageName + "BasicTableUI", "TableHeaderUI", basicPackageName + "BasicTableHeaderUI", "InternalFrameUI", basicPackageName + "BasicInternalFrameUI", "DesktopPaneUI", basicPackageName + "BasicDesktopPaneUI", "DesktopIconUI", basicPackageName + "BasicDesktopIconUI", "OptionPaneUI", basicPackageName + "BasicOptionPaneUI", "PanelUI", basicPackageName + "BasicPanelUI", "ViewportUI", basicPackageName + "BasicViewportUI", "RootPaneUI", basicPackageName + "BasicRootPaneUI"};
        table.putDefaults(uiDefaults);
    }
    
    protected void initSystemColorDefaults(UIDefaults table) {
        String[] defaultSystemColors = {"desktop", "#005C5C", "activeCaption", "#000080", "activeCaptionText", "#FFFFFF", "activeCaptionBorder", "#C0C0C0", "inactiveCaption", "#808080", "inactiveCaptionText", "#C0C0C0", "inactiveCaptionBorder", "#C0C0C0", "window", "#FFFFFF", "windowBorder", "#000000", "windowText", "#000000", "menu", "#C0C0C0", "menuText", "#000000", "text", "#C0C0C0", "textText", "#000000", "textHighlight", "#000080", "textHighlightText", "#FFFFFF", "textInactiveText", "#808080", "control", "#C0C0C0", "controlText", "#000000", "controlHighlight", "#C0C0C0", "controlLtHighlight", "#FFFFFF", "controlShadow", "#808080", "controlDkShadow", "#000000", "scrollbar", "#E0E0E0", "info", "#FFFFE1", "infoText", "#000000"};
        loadSystemColors(table, defaultSystemColors, isNativeLookAndFeel());
    }
    
    protected void loadSystemColors(UIDefaults table, String[] systemColors, boolean useNative) {
        if (useNative) {
            for (int i = 0; i < systemColors.length; i += 2) {
                Color color = Color.black;
                try {
                    String name = systemColors[i];
                    color = (Color)((Color)SystemColor.class.getField(name).get(null));
                } catch (Exception e) {
                }
                table.put(systemColors[i], new ColorUIResource(color));
            }
        } else {
            for (int i = 0; i < systemColors.length; i += 2) {
                Color color = Color.black;
                try {
                    color = Color.decode(systemColors[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                table.put(systemColors[i], new ColorUIResource(color));
            }
        }
    }
    
    private void initResourceBundle(UIDefaults table) {
        table.setDefaultLocale(Locale.getDefault());
        table.addResourceBundle("com.sun.swing.internal.plaf.basic.resources.basic");
    }
    
    protected void initComponentDefaults(UIDefaults table) {
        initResourceBundle(table);
        Integer fiveHundred = new Integer(500);
        Long oneThousand = new Long(1000);
        Integer twelve = new Integer(12);
        Integer fontPlain = new Integer(Font.PLAIN);
        Integer fontBold = new Integer(Font.BOLD);
        Object dialogPlain12 = new SwingLazyValue("javax.swing.plaf.FontUIResource", null, new Object[]{"Dialog", fontPlain, twelve});
        Object serifPlain12 = new SwingLazyValue("javax.swing.plaf.FontUIResource", null, new Object[]{"Serif", fontPlain, twelve});
        Object sansSerifPlain12 = new SwingLazyValue("javax.swing.plaf.FontUIResource", null, new Object[]{"SansSerif", fontPlain, twelve});
        Object monospacedPlain12 = new SwingLazyValue("javax.swing.plaf.FontUIResource", null, new Object[]{"MonoSpaced", fontPlain, twelve});
        Object dialogBold12 = new SwingLazyValue("javax.swing.plaf.FontUIResource", null, new Object[]{"Dialog", fontBold, twelve});
        ColorUIResource red = new ColorUIResource(Color.red);
        ColorUIResource black = new ColorUIResource(Color.black);
        ColorUIResource white = new ColorUIResource(Color.white);
        ColorUIResource yellow = new ColorUIResource(Color.yellow);
        ColorUIResource gray = new ColorUIResource(Color.gray);
        ColorUIResource lightGray = new ColorUIResource(Color.lightGray);
        ColorUIResource darkGray = new ColorUIResource(Color.darkGray);
        ColorUIResource scrollBarTrack = new ColorUIResource(224, 224, 224);
        Color control = table.getColor("control");
        Color controlDkShadow = table.getColor("controlDkShadow");
        Color controlHighlight = table.getColor("controlHighlight");
        Color controlLtHighlight = table.getColor("controlLtHighlight");
        Color controlShadow = table.getColor("controlShadow");
        Color controlText = table.getColor("controlText");
        Color menu = table.getColor("menu");
        Color menuText = table.getColor("menuText");
        Color textHighlight = table.getColor("textHighlight");
        Color textHighlightText = table.getColor("textHighlightText");
        Color textInactiveText = table.getColor("textInactiveText");
        Color textText = table.getColor("textText");
        Color window = table.getColor("window");
        InsetsUIResource zeroInsets = new InsetsUIResource(0, 0, 0, 0);
        InsetsUIResource twoInsets = new InsetsUIResource(2, 2, 2, 2);
        Object marginBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders$MarginBorder");
        Object etchedBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource", "getEtchedBorderUIResource");
        Object loweredBevelBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource", "getLoweredBevelBorderUIResource");
        Object popupMenuBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getInternalFrameBorder");
        Object blackLineBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource", "getBlackLineBorderUIResource");
        Object focusCellHighlightBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource$LineBorderUIResource", null, new Object[]{yellow});
        Object tableHeaderBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource$BevelBorderUIResource", null, new Object[]{new Integer(BevelBorder.RAISED), controlLtHighlight, control, controlDkShadow, controlShadow});
        Object buttonBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getButtonBorder");
        Object buttonToggleBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getToggleButtonBorder");
        Object radioButtonBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getRadioButtonBorder");
        Object newFolderIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/NewFolder.gif");
        Object upFolderIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/UpFolder.gif");
        Object homeFolderIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/HomeFolder.gif");
        Object detailsViewIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/DetailsView.gif");
        Object listViewIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/ListView.gif");
        Object directoryIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/Directory.gif");
        Object fileIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/File.gif");
        Object computerIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/Computer.gif");
        Object hardDriveIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/HardDrive.gif");
        Object floppyDriveIcon = SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/FloppyDrive.gif");
        Object internalFrameBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getInternalFrameBorder");
        Object listCellRendererActiveValue = new BasicLookAndFeel$1(this);
        Object menuBarBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getMenuBarBorder");
        Object menuItemCheckIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getMenuItemCheckIcon");
        Object menuItemArrowIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getMenuItemArrowIcon");
        Object menuArrowIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getMenuArrowIcon");
        Object checkBoxIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getCheckBoxIcon");
        Object radioButtonIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getRadioButtonIcon");
        Object checkBoxMenuItemIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getCheckBoxMenuItemIcon");
        Object radioButtonMenuItemIcon = new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "getRadioButtonMenuItemIcon");
        Object menuItemAcceleratorDelimiter = new String("+");
        Object optionPaneMinimumSize = new DimensionUIResource(262, 90);
        Integer zero = new Integer(0);
        Object zeroBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource$EmptyBorderUIResource", new Object[]{zero, zero, zero, zero});
        Integer ten = new Integer(10);
        Object optionPaneBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource$EmptyBorderUIResource", new Object[]{ten, ten, twelve, ten});
        Object optionPaneButtonAreaBorder = new SwingLazyValue("javax.swing.plaf.BorderUIResource$EmptyBorderUIResource", new Object[]{new Integer(6), zero, zero, zero});
        Object progressBarBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getProgressBarBorder");
        Object minimumThumbSize = new DimensionUIResource(8, 8);
        Object maximumThumbSize = new DimensionUIResource(4096, 4096);
        Object sliderFocusInsets = twoInsets;
        Object toolBarSeparatorSize = new DimensionUIResource(10, 10);
        Object splitPaneBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getSplitPaneBorder");
        Object splitPaneDividerBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getSplitPaneDividerBorder");
        Object tabbedPaneTabInsets = new InsetsUIResource(0, 4, 1, 4);
        Object tabbedPaneTabPadInsets = new InsetsUIResource(2, 2, 2, 1);
        Object tabbedPaneTabAreaInsets = new InsetsUIResource(3, 2, 0, 2);
        Object tabbedPaneContentBorderInsets = new InsetsUIResource(2, 2, 3, 3);
        Object textFieldBorder = new SwingLazyValue("javax.swing.plaf.basic.BasicBorders", "getTextFieldBorder");
        Object editorMargin = new InsetsUIResource(3, 3, 3, 3);
        Object caretBlinkRate = fiveHundred;
        Integer four = new Integer(4);
        Object[] allAuditoryCues = new Object[]{"CheckBoxMenuItem.commandSound", "InternalFrame.closeSound", "InternalFrame.maximizeSound", "InternalFrame.minimizeSound", "InternalFrame.restoreDownSound", "InternalFrame.restoreUpSound", "MenuItem.commandSound", "OptionPane.errorSound", "OptionPane.informationSound", "OptionPane.questionSound", "OptionPane.warningSound", "PopupMenu.popupSound", "RadioButtonMenuItem.commandSound"};
        Object[] noAuditoryCues = new Object[]{"mute"};
        Object[] defaults = {"AuditoryCues.cueList", allAuditoryCues, "AuditoryCues.allAuditoryCues", allAuditoryCues, "AuditoryCues.noAuditoryCues", noAuditoryCues, "AuditoryCues.playList", null, "Button.defaultButtonFollowsFocus", Boolean.TRUE, "Button.font", dialogPlain12, "Button.background", control, "Button.foreground", controlText, "Button.shadow", controlShadow, "Button.darkShadow", controlDkShadow, "Button.light", controlHighlight, "Button.highlight", controlLtHighlight, "Button.border", buttonBorder, "Button.margin", new InsetsUIResource(2, 14, 2, 14), "Button.textIconGap", four, "Button.textShiftOffset", zero, "Button.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released", "ENTER", "pressed", "released ENTER", "released"}), "ToggleButton.font", dialogPlain12, "ToggleButton.background", control, "ToggleButton.foreground", controlText, "ToggleButton.shadow", controlShadow, "ToggleButton.darkShadow", controlDkShadow, "ToggleButton.light", controlHighlight, "ToggleButton.highlight", controlLtHighlight, "ToggleButton.border", buttonToggleBorder, "ToggleButton.margin", new InsetsUIResource(2, 14, 2, 14), "ToggleButton.textIconGap", four, "ToggleButton.textShiftOffset", zero, "ToggleButton.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "RadioButton.font", dialogPlain12, "RadioButton.background", control, "RadioButton.foreground", controlText, "RadioButton.shadow", controlShadow, "RadioButton.darkShadow", controlDkShadow, "RadioButton.light", controlHighlight, "RadioButton.highlight", controlLtHighlight, "RadioButton.border", radioButtonBorder, "RadioButton.margin", twoInsets, "RadioButton.textIconGap", four, "RadioButton.textShiftOffset", zero, "RadioButton.icon", radioButtonIcon, "RadioButton.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released", "RETURN", "pressed"}), "CheckBox.font", dialogPlain12, "CheckBox.background", control, "CheckBox.foreground", controlText, "CheckBox.border", radioButtonBorder, "CheckBox.margin", twoInsets, "CheckBox.textIconGap", four, "CheckBox.textShiftOffset", zero, "CheckBox.icon", checkBoxIcon, "CheckBox.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"SPACE", "pressed", "released SPACE", "released"}), "FileChooser.useSystemExtensionHiding", Boolean.FALSE, "ColorChooser.font", dialogPlain12, "ColorChooser.background", control, "ColorChooser.foreground", controlText, "ColorChooser.swatchesSwatchSize", new Dimension(10, 10), "ColorChooser.swatchesRecentSwatchSize", new Dimension(10, 10), "ColorChooser.swatchesDefaultRecentColor", control, "ComboBox.font", sansSerifPlain12, "ComboBox.background", window, "ComboBox.foreground", textText, "ComboBox.buttonBackground", control, "ComboBox.buttonShadow", controlShadow, "ComboBox.buttonDarkShadow", controlDkShadow, "ComboBox.buttonHighlight", controlLtHighlight, "ComboBox.selectionBackground", textHighlight, "ComboBox.selectionForeground", textHighlightText, "ComboBox.disabledBackground", control, "ComboBox.disabledForeground", textInactiveText, "ComboBox.timeFactor", oneThousand, "ComboBox.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"ESCAPE", "hidePopup", "PAGE_UP", "pageUpPassThrough", "PAGE_DOWN", "pageDownPassThrough", "HOME", "homePassThrough", "END", "endPassThrough", "ENTER", "enterPressed"}), "FileChooser.newFolderIcon", newFolderIcon, "FileChooser.upFolderIcon", upFolderIcon, "FileChooser.homeFolderIcon", homeFolderIcon, "FileChooser.detailsViewIcon", detailsViewIcon, "FileChooser.listViewIcon", listViewIcon, "FileChooser.readOnly", Boolean.FALSE, "FileChooser.usesSingleFilePane", Boolean.FALSE, "FileChooser.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"ESCAPE", "cancelSelection", "F5", "refresh"}), "FileView.directoryIcon", directoryIcon, "FileView.fileIcon", fileIcon, "FileView.computerIcon", computerIcon, "FileView.hardDriveIcon", hardDriveIcon, "FileView.floppyDriveIcon", floppyDriveIcon, "InternalFrame.titleFont", dialogBold12, "InternalFrame.borderColor", control, "InternalFrame.borderShadow", controlShadow, "InternalFrame.borderDarkShadow", controlDkShadow, "InternalFrame.borderHighlight", controlLtHighlight, "InternalFrame.borderLight", controlHighlight, "InternalFrame.border", internalFrameBorder, "InternalFrame.icon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/JavaCup16.png"), "InternalFrame.maximizeIcon", new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "createEmptyFrameIcon"), "InternalFrame.minimizeIcon", new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "createEmptyFrameIcon"), "InternalFrame.iconifyIcon", new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "createEmptyFrameIcon"), "InternalFrame.closeIcon", new SwingLazyValue("javax.swing.plaf.basic.BasicIconFactory", "createEmptyFrameIcon"), "InternalFrame.closeSound", null, "InternalFrame.maximizeSound", null, "InternalFrame.minimizeSound", null, "InternalFrame.restoreDownSound", null, "InternalFrame.restoreUpSound", null, "InternalFrame.activeTitleBackground", table.get("activeCaption"), "InternalFrame.activeTitleForeground", table.get("activeCaptionText"), "InternalFrame.inactiveTitleBackground", table.get("inactiveCaption"), "InternalFrame.inactiveTitleForeground", table.get("inactiveCaptionText"), "InternalFrame.windowBindings", new Object[]{"shift ESCAPE", "showSystemMenu", "ctrl SPACE", "showSystemMenu", "ESCAPE", "hideSystemMenu"}, "DesktopIcon.border", internalFrameBorder, "Desktop.background", table.get("desktop"), "Desktop.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"ctrl F5", "restore", "ctrl F4", "close", "ctrl F7", "move", "ctrl F8", "resize", "RIGHT", "right", "KP_RIGHT", "right", "shift RIGHT", "shrinkRight", "shift KP_RIGHT", "shrinkRight", "LEFT", "left", "KP_LEFT", "left", "shift LEFT", "shrinkLeft", "shift KP_LEFT", "shrinkLeft", "UP", "up", "KP_UP", "up", "shift UP", "shrinkUp", "shift KP_UP", "shrinkUp", "DOWN", "down", "KP_DOWN", "down", "shift DOWN", "shrinkDown", "shift KP_DOWN", "shrinkDown", "ESCAPE", "escape", "ctrl F9", "minimize", "ctrl F10", "maximize", "ctrl F6", "selectNextFrame", "ctrl TAB", "selectNextFrame", "ctrl alt F6", "selectNextFrame", "shift ctrl alt F6", "selectPreviousFrame", "ctrl F12", "navigateNext", "shift ctrl F12", "navigatePrevious"}), "Label.font", dialogPlain12, "Label.background", control, "Label.foreground", controlText, "Label.disabledForeground", white, "Label.disabledShadow", controlShadow, "Label.border", null, "List.font", dialogPlain12, "List.background", window, "List.foreground", textText, "List.selectionBackground", textHighlight, "List.selectionForeground", textHighlightText, "List.focusCellHighlightBorder", focusCellHighlightBorder, "List.border", null, "List.cellRenderer", listCellRendererActiveValue, "List.timeFactor", oneThousand, "List.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "UP", "selectPreviousRow", "KP_UP", "selectPreviousRow", "shift UP", "selectPreviousRowExtendSelection", "shift KP_UP", "selectPreviousRowExtendSelection", "ctrl shift UP", "selectPreviousRowExtendSelection", "ctrl shift KP_UP", "selectPreviousRowExtendSelection", "ctrl UP", "selectPreviousRowChangeLead", "ctrl KP_UP", "selectPreviousRowChangeLead", "DOWN", "selectNextRow", "KP_DOWN", "selectNextRow", "shift DOWN", "selectNextRowExtendSelection", "shift KP_DOWN", "selectNextRowExtendSelection", "ctrl shift DOWN", "selectNextRowExtendSelection", "ctrl shift KP_DOWN", "selectNextRowExtendSelection", "ctrl DOWN", "selectNextRowChangeLead", "ctrl KP_DOWN", "selectNextRowChangeLead", "LEFT", "selectPreviousColumn", "KP_LEFT", "selectPreviousColumn", "shift LEFT", "selectPreviousColumnExtendSelection", "shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl shift LEFT", "selectPreviousColumnExtendSelection", "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl LEFT", "selectPreviousColumnChangeLead", "ctrl KP_LEFT", "selectPreviousColumnChangeLead", "RIGHT", "selectNextColumn", "KP_RIGHT", "selectNextColumn", "shift RIGHT", "selectNextColumnExtendSelection", "shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl shift RIGHT", "selectNextColumnExtendSelection", "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl RIGHT", "selectNextColumnChangeLead", "ctrl KP_RIGHT", "selectNextColumnChangeLead", "HOME", "selectFirstRow", "shift HOME", "selectFirstRowExtendSelection", "ctrl shift HOME", "selectFirstRowExtendSelection", "ctrl HOME", "selectFirstRowChangeLead", "END", "selectLastRow", "shift END", "selectLastRowExtendSelection", "ctrl shift END", "selectLastRowExtendSelection", "ctrl END", "selectLastRowChangeLead", "PAGE_UP", "scrollUp", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollUpExtendSelection", "ctrl PAGE_UP", "scrollUpChangeLead", "PAGE_DOWN", "scrollDown", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl PAGE_DOWN", "scrollDownChangeLead", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "List.focusInputMap.RightToLeft", new UIDefaults$LazyInputMap(new Object[]{"LEFT", "selectNextColumn", "KP_LEFT", "selectNextColumn", "shift LEFT", "selectNextColumnExtendSelection", "shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl shift LEFT", "selectNextColumnExtendSelection", "ctrl shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl LEFT", "selectNextColumnChangeLead", "ctrl KP_LEFT", "selectNextColumnChangeLead", "RIGHT", "selectPreviousColumn", "KP_RIGHT", "selectPreviousColumn", "shift RIGHT", "selectPreviousColumnExtendSelection", "shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl RIGHT", "selectPreviousColumnChangeLead", "ctrl KP_RIGHT", "selectPreviousColumnChangeLead"}), "MenuBar.font", dialogPlain12, "MenuBar.background", menu, "MenuBar.foreground", menuText, "MenuBar.shadow", controlShadow, "MenuBar.highlight", controlLtHighlight, "MenuBar.border", menuBarBorder, "MenuBar.windowBindings", new Object[]{"F10", "takeFocus"}, "MenuItem.font", dialogPlain12, "MenuItem.acceleratorFont", dialogPlain12, "MenuItem.background", menu, "MenuItem.foreground", menuText, "MenuItem.selectionForeground", textHighlightText, "MenuItem.selectionBackground", textHighlight, "MenuItem.disabledForeground", null, "MenuItem.acceleratorForeground", menuText, "MenuItem.acceleratorSelectionForeground", textHighlightText, "MenuItem.acceleratorDelimiter", menuItemAcceleratorDelimiter, "MenuItem.border", marginBorder, "MenuItem.borderPainted", Boolean.FALSE, "MenuItem.margin", twoInsets, "MenuItem.checkIcon", menuItemCheckIcon, "MenuItem.arrowIcon", menuItemArrowIcon, "MenuItem.commandSound", null, "RadioButtonMenuItem.font", dialogPlain12, "RadioButtonMenuItem.acceleratorFont", dialogPlain12, "RadioButtonMenuItem.background", menu, "RadioButtonMenuItem.foreground", menuText, "RadioButtonMenuItem.selectionForeground", textHighlightText, "RadioButtonMenuItem.selectionBackground", textHighlight, "RadioButtonMenuItem.disabledForeground", null, "RadioButtonMenuItem.acceleratorForeground", menuText, "RadioButtonMenuItem.acceleratorSelectionForeground", textHighlightText, "RadioButtonMenuItem.border", marginBorder, "RadioButtonMenuItem.borderPainted", Boolean.FALSE, "RadioButtonMenuItem.margin", twoInsets, "RadioButtonMenuItem.checkIcon", radioButtonMenuItemIcon, "RadioButtonMenuItem.arrowIcon", menuItemArrowIcon, "RadioButtonMenuItem.commandSound", null, "CheckBoxMenuItem.font", dialogPlain12, "CheckBoxMenuItem.acceleratorFont", dialogPlain12, "CheckBoxMenuItem.background", menu, "CheckBoxMenuItem.foreground", menuText, "CheckBoxMenuItem.selectionForeground", textHighlightText, "CheckBoxMenuItem.selectionBackground", textHighlight, "CheckBoxMenuItem.disabledForeground", null, "CheckBoxMenuItem.acceleratorForeground", menuText, "CheckBoxMenuItem.acceleratorSelectionForeground", textHighlightText, "CheckBoxMenuItem.border", marginBorder, "CheckBoxMenuItem.borderPainted", Boolean.FALSE, "CheckBoxMenuItem.margin", twoInsets, "CheckBoxMenuItem.checkIcon", checkBoxMenuItemIcon, "CheckBoxMenuItem.arrowIcon", menuItemArrowIcon, "CheckBoxMenuItem.commandSound", null, "Menu.font", dialogPlain12, "Menu.acceleratorFont", dialogPlain12, "Menu.background", menu, "Menu.foreground", menuText, "Menu.selectionForeground", textHighlightText, "Menu.selectionBackground", textHighlight, "Menu.disabledForeground", null, "Menu.acceleratorForeground", menuText, "Menu.acceleratorSelectionForeground", textHighlightText, "Menu.border", marginBorder, "Menu.borderPainted", Boolean.FALSE, "Menu.margin", twoInsets, "Menu.checkIcon", menuItemCheckIcon, "Menu.arrowIcon", menuArrowIcon, "Menu.menuPopupOffsetX", new Integer(0), "Menu.menuPopupOffsetY", new Integer(0), "Menu.submenuPopupOffsetX", new Integer(0), "Menu.submenuPopupOffsetY", new Integer(0), "Menu.shortcutKeys", new int[]{KeyEvent.ALT_MASK}, "Menu.crossMenuMnemonic", Boolean.TRUE, "PopupMenu.font", dialogPlain12, "PopupMenu.background", menu, "PopupMenu.foreground", menuText, "PopupMenu.border", popupMenuBorder, "PopupMenu.popupSound", null, "PopupMenu.selectedWindowInputMapBindings", new Object[]{"ESCAPE", "cancel", "DOWN", "selectNext", "KP_DOWN", "selectNext", "UP", "selectPrevious", "KP_UP", "selectPrevious", "LEFT", "selectParent", "KP_LEFT", "selectParent", "RIGHT", "selectChild", "KP_RIGHT", "selectChild", "ENTER", "return", "SPACE", "return"}, "PopupMenu.selectedWindowInputMapBindings.RightToLeft", new Object[]{"LEFT", "selectChild", "KP_LEFT", "selectChild", "RIGHT", "selectParent", "KP_RIGHT", "selectParent"}, "PopupMenu.consumeEventOnClose", Boolean.FALSE, "OptionPane.font", dialogPlain12, "OptionPane.background", control, "OptionPane.foreground", controlText, "OptionPane.messageForeground", controlText, "OptionPane.border", optionPaneBorder, "OptionPane.messageAreaBorder", zeroBorder, "OptionPane.buttonAreaBorder", optionPaneButtonAreaBorder, "OptionPane.minimumSize", optionPaneMinimumSize, "OptionPane.errorIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/Error.gif"), "OptionPane.informationIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/Inform.gif"), "OptionPane.warningIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/Warn.gif"), "OptionPane.questionIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/Question.gif"), "OptionPane.windowBindings", new Object[]{"ESCAPE", "close"}, "OptionPane.errorSound", null, "OptionPane.informationSound", null, "OptionPane.questionSound", null, "OptionPane.warningSound", null, "OptionPane.buttonClickThreshhold", fiveHundred, "Panel.font", dialogPlain12, "Panel.background", control, "Panel.foreground", textText, "ProgressBar.font", dialogPlain12, "ProgressBar.foreground", textHighlight, "ProgressBar.background", control, "ProgressBar.selectionForeground", control, "ProgressBar.selectionBackground", textHighlight, "ProgressBar.border", progressBarBorder, "ProgressBar.cellLength", new Integer(1), "ProgressBar.cellSpacing", zero, "ProgressBar.repaintInterval", new Integer(50), "ProgressBar.cycleTime", new Integer(3000), "ProgressBar.horizontalSize", new DimensionUIResource(146, 12), "ProgressBar.verticalSize", new DimensionUIResource(12, 146), "Separator.shadow", controlShadow, "Separator.highlight", controlLtHighlight, "Separator.background", controlLtHighlight, "Separator.foreground", controlShadow, "ScrollBar.background", scrollBarTrack, "ScrollBar.foreground", control, "ScrollBar.track", table.get("scrollbar"), "ScrollBar.trackHighlight", controlDkShadow, "ScrollBar.thumb", control, "ScrollBar.thumbHighlight", controlLtHighlight, "ScrollBar.thumbDarkShadow", controlDkShadow, "ScrollBar.thumbShadow", controlShadow, "ScrollBar.border", null, "ScrollBar.minimumThumbSize", minimumThumbSize, "ScrollBar.maximumThumbSize", maximumThumbSize, "ScrollBar.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "positiveUnitIncrement", "KP_RIGHT", "positiveUnitIncrement", "DOWN", "positiveUnitIncrement", "KP_DOWN", "positiveUnitIncrement", "PAGE_DOWN", "positiveBlockIncrement", "LEFT", "negativeUnitIncrement", "KP_LEFT", "negativeUnitIncrement", "UP", "negativeUnitIncrement", "KP_UP", "negativeUnitIncrement", "PAGE_UP", "negativeBlockIncrement", "HOME", "minScroll", "END", "maxScroll"}), "ScrollBar.ancestorInputMap.RightToLeft", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "negativeUnitIncrement", "KP_RIGHT", "negativeUnitIncrement", "LEFT", "positiveUnitIncrement", "KP_LEFT", "positiveUnitIncrement"}), "ScrollBar.width", new Integer(16), "ScrollPane.font", dialogPlain12, "ScrollPane.background", control, "ScrollPane.foreground", controlText, "ScrollPane.border", textFieldBorder, "ScrollPane.viewportBorder", null, "ScrollPane.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "unitScrollRight", "KP_RIGHT", "unitScrollRight", "DOWN", "unitScrollDown", "KP_DOWN", "unitScrollDown", "LEFT", "unitScrollLeft", "KP_LEFT", "unitScrollLeft", "UP", "unitScrollUp", "KP_UP", "unitScrollUp", "PAGE_UP", "scrollUp", "PAGE_DOWN", "scrollDown", "ctrl PAGE_UP", "scrollLeft", "ctrl PAGE_DOWN", "scrollRight", "ctrl HOME", "scrollHome", "ctrl END", "scrollEnd"}), "ScrollPane.ancestorInputMap.RightToLeft", new UIDefaults$LazyInputMap(new Object[]{"ctrl PAGE_UP", "scrollRight", "ctrl PAGE_DOWN", "scrollLeft"}), "Viewport.font", dialogPlain12, "Viewport.background", control, "Viewport.foreground", textText, "Slider.foreground", control, "Slider.background", control, "Slider.highlight", controlLtHighlight, "Slider.tickColor", Color.black, "Slider.shadow", controlShadow, "Slider.focus", controlDkShadow, "Slider.border", null, "Slider.horizontalSize", new Dimension(200, 21), "Slider.verticalSize", new Dimension(21, 200), "Slider.minimumHorizontalSize", new Dimension(36, 21), "Slider.minimumVerticalSize", new Dimension(21, 36), "Slider.focusInsets", sliderFocusInsets, "Slider.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "positiveUnitIncrement", "KP_RIGHT", "positiveUnitIncrement", "DOWN", "negativeUnitIncrement", "KP_DOWN", "negativeUnitIncrement", "PAGE_DOWN", "negativeBlockIncrement", "LEFT", "negativeUnitIncrement", "KP_LEFT", "negativeUnitIncrement", "UP", "positiveUnitIncrement", "KP_UP", "positiveUnitIncrement", "PAGE_UP", "positiveBlockIncrement", "HOME", "minScroll", "END", "maxScroll"}), "Slider.focusInputMap.RightToLeft", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "negativeUnitIncrement", "KP_RIGHT", "negativeUnitIncrement", "LEFT", "positiveUnitIncrement", "KP_LEFT", "positiveUnitIncrement"}), "Spinner.font", monospacedPlain12, "Spinner.background", control, "Spinner.foreground", control, "Spinner.border", textFieldBorder, "Spinner.arrowButtonBorder", null, "Spinner.arrowButtonInsets", null, "Spinner.arrowButtonSize", new Dimension(16, 5), "Spinner.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"UP", "increment", "KP_UP", "increment", "DOWN", "decrement", "KP_DOWN", "decrement"}), "Spinner.editorBorderPainted", Boolean.FALSE, "SplitPane.background", control, "SplitPane.highlight", controlLtHighlight, "SplitPane.shadow", controlShadow, "SplitPane.darkShadow", controlDkShadow, "SplitPane.border", splitPaneBorder, "SplitPane.dividerSize", new Integer(7), "SplitPaneDivider.border", splitPaneDividerBorder, "SplitPaneDivider.draggingColor", darkGray, "SplitPane.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"UP", "negativeIncrement", "DOWN", "positiveIncrement", "LEFT", "negativeIncrement", "RIGHT", "positiveIncrement", "KP_UP", "negativeIncrement", "KP_DOWN", "positiveIncrement", "KP_LEFT", "negativeIncrement", "KP_RIGHT", "positiveIncrement", "HOME", "selectMin", "END", "selectMax", "F8", "startResize", "F6", "toggleFocus", "ctrl TAB", "focusOutForward", "ctrl shift TAB", "focusOutBackward"}), "TabbedPane.font", dialogPlain12, "TabbedPane.background", control, "TabbedPane.foreground", controlText, "TabbedPane.highlight", controlLtHighlight, "TabbedPane.light", controlHighlight, "TabbedPane.shadow", controlShadow, "TabbedPane.darkShadow", controlDkShadow, "TabbedPane.selected", null, "TabbedPane.focus", controlText, "TabbedPane.textIconGap", four, "TabbedPane.tabsOverlapBorder", Boolean.FALSE, "TabbedPane.tabInsets", tabbedPaneTabInsets, "TabbedPane.selectedTabPadInsets", tabbedPaneTabPadInsets, "TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets, "TabbedPane.contentBorderInsets", tabbedPaneContentBorderInsets, "TabbedPane.tabRunOverlay", new Integer(2), "TabbedPane.tabsOpaque", Boolean.TRUE, "TabbedPane.contentOpaque", Boolean.TRUE, "TabbedPane.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "navigateRight", "KP_RIGHT", "navigateRight", "LEFT", "navigateLeft", "KP_LEFT", "navigateLeft", "UP", "navigateUp", "KP_UP", "navigateUp", "DOWN", "navigateDown", "KP_DOWN", "navigateDown", "ctrl DOWN", "requestFocusForVisibleComponent", "ctrl KP_DOWN", "requestFocusForVisibleComponent"}), "TabbedPane.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"ctrl PAGE_DOWN", "navigatePageDown", "ctrl PAGE_UP", "navigatePageUp", "ctrl UP", "requestFocus", "ctrl KP_UP", "requestFocus"}), "Table.font", dialogPlain12, "Table.foreground", controlText, "Table.background", window, "Table.selectionForeground", textHighlightText, "Table.selectionBackground", textHighlight, "Table.gridColor", gray, "Table.focusCellBackground", window, "Table.focusCellForeground", controlText, "Table.focusCellHighlightBorder", focusCellHighlightBorder, "Table.scrollPaneBorder", loweredBevelBorder, "Table.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "RIGHT", "selectNextColumn", "KP_RIGHT", "selectNextColumn", "shift RIGHT", "selectNextColumnExtendSelection", "shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl shift RIGHT", "selectNextColumnExtendSelection", "ctrl shift KP_RIGHT", "selectNextColumnExtendSelection", "ctrl RIGHT", "selectNextColumnChangeLead", "ctrl KP_RIGHT", "selectNextColumnChangeLead", "LEFT", "selectPreviousColumn", "KP_LEFT", "selectPreviousColumn", "shift LEFT", "selectPreviousColumnExtendSelection", "shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl shift LEFT", "selectPreviousColumnExtendSelection", "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection", "ctrl LEFT", "selectPreviousColumnChangeLead", "ctrl KP_LEFT", "selectPreviousColumnChangeLead", "DOWN", "selectNextRow", "KP_DOWN", "selectNextRow", "shift DOWN", "selectNextRowExtendSelection", "shift KP_DOWN", "selectNextRowExtendSelection", "ctrl shift DOWN", "selectNextRowExtendSelection", "ctrl shift KP_DOWN", "selectNextRowExtendSelection", "ctrl DOWN", "selectNextRowChangeLead", "ctrl KP_DOWN", "selectNextRowChangeLead", "UP", "selectPreviousRow", "KP_UP", "selectPreviousRow", "shift UP", "selectPreviousRowExtendSelection", "shift KP_UP", "selectPreviousRowExtendSelection", "ctrl shift UP", "selectPreviousRowExtendSelection", "ctrl shift KP_UP", "selectPreviousRowExtendSelection", "ctrl UP", "selectPreviousRowChangeLead", "ctrl KP_UP", "selectPreviousRowChangeLead", "HOME", "selectFirstColumn", "shift HOME", "selectFirstColumnExtendSelection", "ctrl shift HOME", "selectFirstRowExtendSelection", "ctrl HOME", "selectFirstRow", "END", "selectLastColumn", "shift END", "selectLastColumnExtendSelection", "ctrl shift END", "selectLastRowExtendSelection", "ctrl END", "selectLastRow", "PAGE_UP", "scrollUpChangeSelection", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollLeftExtendSelection", "ctrl PAGE_UP", "scrollLeftChangeSelection", "PAGE_DOWN", "scrollDownChangeSelection", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollRightExtendSelection", "ctrl PAGE_DOWN", "scrollRightChangeSelection", "TAB", "selectNextColumnCell", "shift TAB", "selectPreviousColumnCell", "ENTER", "selectNextRowCell", "shift ENTER", "selectPreviousRowCell", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "ESCAPE", "cancel", "F2", "startEditing", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "Table.ancestorInputMap.RightToLeft", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "selectPreviousColumn", "KP_RIGHT", "selectPreviousColumn", "shift RIGHT", "selectPreviousColumnExtendSelection", "shift KP_RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift RIGHT", "selectPreviousColumnExtendSelection", "ctrl shift KP_RIGHT", "selectPreviousColumnExtendSelection", "shift RIGHT", "selectPreviousColumnChangeLead", "shift KP_RIGHT", "selectPreviousColumnChangeLead", "LEFT", "selectNextColumn", "KP_LEFT", "selectNextColumn", "shift LEFT", "selectNextColumnExtendSelection", "shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl shift LEFT", "selectNextColumnExtendSelection", "ctrl shift KP_LEFT", "selectNextColumnExtendSelection", "ctrl LEFT", "selectNextColumnChangeLead", "ctrl KP_LEFT", "selectNextColumnChangeLead", "ctrl PAGE_UP", "scrollRightChangeSelection", "ctrl PAGE_DOWN", "scrollLeftChangeSelection", "ctrl shift PAGE_UP", "scrollRightExtendSelection", "ctrl shift PAGE_DOWN", "scrollLeftExtendSelection"}), "TableHeader.font", dialogPlain12, "TableHeader.foreground", controlText, "TableHeader.background", control, "TableHeader.cellBorder", tableHeaderBorder, "TextField.font", sansSerifPlain12, "TextField.background", window, "TextField.foreground", textText, "TextField.shadow", controlShadow, "TextField.darkShadow", controlDkShadow, "TextField.light", controlHighlight, "TextField.highlight", controlLtHighlight, "TextField.inactiveForeground", textInactiveText, "TextField.inactiveBackground", control, "TextField.selectionBackground", textHighlight, "TextField.selectionForeground", textHighlightText, "TextField.caretForeground", textText, "TextField.caretBlinkRate", caretBlinkRate, "TextField.border", textFieldBorder, "TextField.margin", zeroInsets, "FormattedTextField.font", sansSerifPlain12, "FormattedTextField.background", window, "FormattedTextField.foreground", textText, "FormattedTextField.inactiveForeground", textInactiveText, "FormattedTextField.inactiveBackground", control, "FormattedTextField.selectionBackground", textHighlight, "FormattedTextField.selectionForeground", textHighlightText, "FormattedTextField.caretForeground", textText, "FormattedTextField.caretBlinkRate", caretBlinkRate, "FormattedTextField.border", textFieldBorder, "FormattedTextField.margin", zeroInsets, "FormattedTextField.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"ctrl C", DefaultEditorKit.copyAction, "ctrl V", DefaultEditorKit.pasteAction, "ctrl X", DefaultEditorKit.cutAction, "COPY", DefaultEditorKit.copyAction, "PASTE", DefaultEditorKit.pasteAction, "CUT", DefaultEditorKit.cutAction, "shift LEFT", DefaultEditorKit.selectionBackwardAction, "shift KP_LEFT", DefaultEditorKit.selectionBackwardAction, "shift RIGHT", DefaultEditorKit.selectionForwardAction, "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction, "ctrl LEFT", DefaultEditorKit.previousWordAction, "ctrl KP_LEFT", DefaultEditorKit.previousWordAction, "ctrl RIGHT", DefaultEditorKit.nextWordAction, "ctrl KP_RIGHT", DefaultEditorKit.nextWordAction, "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction, "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction, "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction, "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction, "ctrl A", DefaultEditorKit.selectAllAction, "HOME", DefaultEditorKit.beginLineAction, "END", DefaultEditorKit.endLineAction, "shift HOME", DefaultEditorKit.selectionBeginLineAction, "shift END", DefaultEditorKit.selectionEndLineAction, "BACK_SPACE", DefaultEditorKit.deletePrevCharAction, "ctrl H", DefaultEditorKit.deletePrevCharAction, "DELETE", DefaultEditorKit.deleteNextCharAction, "RIGHT", DefaultEditorKit.forwardAction, "LEFT", DefaultEditorKit.backwardAction, "KP_RIGHT", DefaultEditorKit.forwardAction, "KP_LEFT", DefaultEditorKit.backwardAction, "ENTER", JTextField.notifyAction, "ctrl BACK_SLASH", "unselect", "control shift O", "toggle-componentOrientation", "ESCAPE", "reset-field-edit", "UP", "increment", "KP_UP", "increment", "DOWN", "decrement", "KP_DOWN", "decrement"}), "PasswordField.font", monospacedPlain12, "PasswordField.background", window, "PasswordField.foreground", textText, "PasswordField.inactiveForeground", textInactiveText, "PasswordField.inactiveBackground", control, "PasswordField.selectionBackground", textHighlight, "PasswordField.selectionForeground", textHighlightText, "PasswordField.caretForeground", textText, "PasswordField.caretBlinkRate", caretBlinkRate, "PasswordField.border", textFieldBorder, "PasswordField.margin", zeroInsets, "TextArea.font", monospacedPlain12, "TextArea.background", window, "TextArea.foreground", textText, "TextArea.inactiveForeground", textInactiveText, "TextArea.selectionBackground", textHighlight, "TextArea.selectionForeground", textHighlightText, "TextArea.caretForeground", textText, "TextArea.caretBlinkRate", caretBlinkRate, "TextArea.border", marginBorder, "TextArea.margin", zeroInsets, "TextPane.font", serifPlain12, "TextPane.background", white, "TextPane.foreground", textText, "TextPane.selectionBackground", textHighlight, "TextPane.selectionForeground", textHighlightText, "TextPane.caretForeground", textText, "TextPane.caretBlinkRate", caretBlinkRate, "TextPane.inactiveForeground", textInactiveText, "TextPane.border", marginBorder, "TextPane.margin", editorMargin, "EditorPane.font", serifPlain12, "EditorPane.background", white, "EditorPane.foreground", textText, "EditorPane.selectionBackground", textHighlight, "EditorPane.selectionForeground", textHighlightText, "EditorPane.caretForeground", textText, "EditorPane.caretBlinkRate", caretBlinkRate, "EditorPane.inactiveForeground", textInactiveText, "EditorPane.border", marginBorder, "EditorPane.margin", editorMargin, "TitledBorder.font", dialogPlain12, "TitledBorder.titleColor", controlText, "TitledBorder.border", etchedBorder, "ToolBar.font", dialogPlain12, "ToolBar.background", control, "ToolBar.foreground", controlText, "ToolBar.shadow", controlShadow, "ToolBar.darkShadow", controlDkShadow, "ToolBar.light", controlHighlight, "ToolBar.highlight", controlLtHighlight, "ToolBar.dockingBackground", control, "ToolBar.dockingForeground", red, "ToolBar.floatingBackground", control, "ToolBar.floatingForeground", darkGray, "ToolBar.border", etchedBorder, "ToolBar.separatorSize", toolBarSeparatorSize, "ToolBar.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"UP", "navigateUp", "KP_UP", "navigateUp", "DOWN", "navigateDown", "KP_DOWN", "navigateDown", "LEFT", "navigateLeft", "KP_LEFT", "navigateLeft", "RIGHT", "navigateRight", "KP_RIGHT", "navigateRight"}), "ToolTip.font", sansSerifPlain12, "ToolTip.background", table.get("info"), "ToolTip.foreground", table.get("infoText"), "ToolTip.border", blackLineBorder, "Tree.paintLines", Boolean.TRUE, "Tree.lineTypeDashed", Boolean.FALSE, "Tree.font", dialogPlain12, "Tree.background", window, "Tree.foreground", textText, "Tree.hash", gray, "Tree.textForeground", textText, "Tree.textBackground", table.get("text"), "Tree.selectionForeground", textHighlightText, "Tree.selectionBackground", textHighlight, "Tree.selectionBorderColor", black, "Tree.editorBorder", blackLineBorder, "Tree.leftChildIndent", new Integer(7), "Tree.rightChildIndent", new Integer(13), "Tree.rowHeight", new Integer(16), "Tree.scrollsOnExpand", Boolean.TRUE, "Tree.openIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/TreeOpen.gif"), "Tree.closedIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/TreeClosed.gif"), "Tree.leafIcon", SwingUtilities2.makeIcon(getClass(), BasicLookAndFeel.class, "icons/TreeLeaf.gif"), "Tree.expandedIcon", null, "Tree.collapsedIcon", null, "Tree.changeSelectionWithFocus", Boolean.TRUE, "Tree.drawsFocusBorderAroundIcon", Boolean.FALSE, "Tree.timeFactor", oneThousand, "Tree.focusInputMap", new UIDefaults$LazyInputMap(new Object[]{"ctrl C", "copy", "ctrl V", "paste", "ctrl X", "cut", "COPY", "copy", "PASTE", "paste", "CUT", "cut", "UP", "selectPrevious", "KP_UP", "selectPrevious", "shift UP", "selectPreviousExtendSelection", "shift KP_UP", "selectPreviousExtendSelection", "ctrl shift UP", "selectPreviousExtendSelection", "ctrl shift KP_UP", "selectPreviousExtendSelection", "ctrl UP", "selectPreviousChangeLead", "ctrl KP_UP", "selectPreviousChangeLead", "DOWN", "selectNext", "KP_DOWN", "selectNext", "shift DOWN", "selectNextExtendSelection", "shift KP_DOWN", "selectNextExtendSelection", "ctrl shift DOWN", "selectNextExtendSelection", "ctrl shift KP_DOWN", "selectNextExtendSelection", "ctrl DOWN", "selectNextChangeLead", "ctrl KP_DOWN", "selectNextChangeLead", "RIGHT", "selectChild", "KP_RIGHT", "selectChild", "LEFT", "selectParent", "KP_LEFT", "selectParent", "PAGE_UP", "scrollUpChangeSelection", "shift PAGE_UP", "scrollUpExtendSelection", "ctrl shift PAGE_UP", "scrollUpExtendSelection", "ctrl PAGE_UP", "scrollUpChangeLead", "PAGE_DOWN", "scrollDownChangeSelection", "shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl shift PAGE_DOWN", "scrollDownExtendSelection", "ctrl PAGE_DOWN", "scrollDownChangeLead", "HOME", "selectFirst", "shift HOME", "selectFirstExtendSelection", "ctrl shift HOME", "selectFirstExtendSelection", "ctrl HOME", "selectFirstChangeLead", "END", "selectLast", "shift END", "selectLastExtendSelection", "ctrl shift END", "selectLastExtendSelection", "ctrl END", "selectLastChangeLead", "F2", "startEditing", "ctrl A", "selectAll", "ctrl SLASH", "selectAll", "ctrl BACK_SLASH", "clearSelection", "ctrl LEFT", "scrollLeft", "ctrl KP_LEFT", "scrollLeft", "ctrl RIGHT", "scrollRight", "ctrl KP_RIGHT", "scrollRight", "SPACE", "addToSelection", "ctrl SPACE", "toggleAndAnchor", "shift SPACE", "extendTo", "ctrl shift SPACE", "moveSelectionTo"}), "Tree.focusInputMap.RightToLeft", new UIDefaults$LazyInputMap(new Object[]{"RIGHT", "selectParent", "KP_RIGHT", "selectParent", "LEFT", "selectChild", "KP_LEFT", "selectChild"}), "Tree.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"ESCAPE", "cancel"}), "RootPane.ancestorInputMap", new UIDefaults$LazyInputMap(new Object[]{"shift F10", "postPopup"}), "RootPane.defaultButtonWindowKeyBindings", new Object[]{"ENTER", "press", "released ENTER", "release", "ctrl ENTER", "press", "ctrl released ENTER", "release"}};
        table.putDefaults(defaults);
    }
    
    static Object getUIOfType(ComponentUI ui, Class klass) {
        if (klass.isInstance(ui)) {
            return ui;
        }
        return null;
    }
    
    protected ActionMap getAudioActionMap() {
        ActionMap audioActionMap = (ActionMap)(ActionMap)UIManager.get("AuditoryCues.actionMap");
        if (audioActionMap == null) {
            Object[] acList = (Object[])(Object[])UIManager.get("AuditoryCues.cueList");
            if (acList != null) {
                audioActionMap = new ActionMapUIResource();
                for (int counter = acList.length - 1; counter >= 0; counter--) {
                    audioActionMap.put(acList[counter], createAudioAction(acList[counter]));
                }
            }
            UIManager.getLookAndFeelDefaults().put("AuditoryCues.actionMap", audioActionMap);
        }
        return audioActionMap;
    }
    
    protected Action createAudioAction(Object key) {
        if (key != null) {
            String audioKey = (String)(String)key;
            String audioValue = (String)(String)UIManager.get(key);
            return new BasicLookAndFeel$AudioAction(this, audioKey, audioValue);
        } else {
            return null;
        }
    }
    {
    }
    
    private byte[] loadAudioData(final String soundFile) {
        if (soundFile == null) {
            return null;
        }
        byte[] buffer = (byte[])(byte[])AccessController.doPrivileged(new BasicLookAndFeel$2(this, soundFile));
        if (buffer == null) {
            System.err.println(getClass().getName() + "/" + soundFile + " not found.");
            return null;
        }
        if (buffer.length == 0) {
            System.err.println("warning: " + soundFile + " is zero-length");
            return null;
        }
        return buffer;
    }
    
    protected void playSound(Action audioAction) {
        if (audioAction != null) {
            Object[] audioStrings = (Object[])(Object[])UIManager.get("AuditoryCues.playList");
            if (audioStrings != null) {
                HashSet audioCues = new HashSet();
                for (int i = 0; i < audioStrings.length; i++) {
                    audioCues.add(audioStrings[i]);
                }
                String actionName = (String)(String)audioAction.getValue(Action.NAME);
                if (audioCues.contains(actionName)) {
                    audioAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionName));
                }
            }
        }
    }
    
    static void installAudioActionMap(ActionMap map) {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof BasicLookAndFeel) {
            map.setParent(((BasicLookAndFeel)(BasicLookAndFeel)laf).getAudioActionMap());
        }
    }
    
    static void playSound(JComponent c, Object actionKey) {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof BasicLookAndFeel) {
            ActionMap map = c.getActionMap();
            if (map != null) {
                Action audioAction = map.get(actionKey);
                if (audioAction != null) {
                    ((BasicLookAndFeel)(BasicLookAndFeel)laf).playSound(audioAction);
                }
            }
        }
    }
    
    static Component compositeRequestFocus(Component component) {
        if (component instanceof Container) {
            Container container = (Container)(Container)component;
            if (container.isFocusCycleRoot()) {
                FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
                Component comp = policy.getDefaultComponent(container);
                if (comp != null) {
                    comp.requestFocus();
                    return comp;
                }
            }
            Container rootAncestor = container.getFocusCycleRootAncestor();
            if (rootAncestor != null) {
                FocusTraversalPolicy policy = rootAncestor.getFocusTraversalPolicy();
                Component comp = policy.getComponentAfter(rootAncestor, container);
                if (comp != null && SwingUtilities.isDescendingFrom(comp, container)) {
                    comp.requestFocus();
                    return comp;
                }
            }
        }
        if (component.isFocusable()) {
            component.requestFocus();
            return component;
        }
        return null;
    }
    
    void createdPopup() {
        if (invocator == null) {
            invocator = new BasicLookAndFeel$PopupInvocationHelper(this);
            hasPopups = true;
            disposer = new BasicLookAndFeel$3(this);
            AppContext.getAppContext().addPropertyChangeListener(AppContext.GUI_DISPOSED, disposer);
        }
    }
    {
    }
}
