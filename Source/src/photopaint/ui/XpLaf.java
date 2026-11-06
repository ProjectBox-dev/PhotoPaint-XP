package photopaint.ui;

import photopaint.Theme;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Registers the XP look on top of the cross-platform look and feel. */
public final class XpLaf {

    private XpLaf() {
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception ignored) {
        }
        UIManager.put("ButtonUI", XpButtonUI.class.getName());
        UIManager.put("ToggleButtonUI", XpButtonUI.class.getName());
        UIManager.put("ScrollBarUI", XpScrollBarUI.class.getName());
        UIManager.put("SliderUI", XpSliderUI.class.getName());

        UIManager.put("PanelUI", "javax.swing.plaf.basic.BasicPanelUI");
        UIManager.put("LabelUI", "javax.swing.plaf.basic.BasicLabelUI");
        UIManager.put("MenuBarUI", "javax.swing.plaf.basic.BasicMenuBarUI");
        UIManager.put("MenuUI", "javax.swing.plaf.basic.BasicMenuUI");
        UIManager.put("MenuItemUI", "javax.swing.plaf.basic.BasicMenuItemUI");
        UIManager.put("CheckBoxMenuItemUI", "javax.swing.plaf.basic.BasicCheckBoxMenuItemUI");
        UIManager.put("RadioButtonMenuItemUI", "javax.swing.plaf.basic.BasicRadioButtonMenuItemUI");
        UIManager.put("PopupMenuUI", "javax.swing.plaf.basic.BasicPopupMenuUI");
        UIManager.put("ListUI", "javax.swing.plaf.basic.BasicListUI");
        UIManager.put("ToolTipUI", "javax.swing.plaf.basic.BasicToolTipUI");
        UIManager.put("TextFieldUI", "javax.swing.plaf.basic.BasicTextFieldUI");
        UIManager.put("TextAreaUI", "javax.swing.plaf.basic.BasicTextAreaUI");
        UIManager.put("TextPaneUI", "javax.swing.plaf.basic.BasicTextPaneUI");
        UIManager.put("ScrollPaneUI", "javax.swing.plaf.basic.BasicScrollPaneUI");
        UIManager.put("ViewportUI", "javax.swing.plaf.basic.BasicViewportUI");
        UIManager.put("CheckBoxUI", "javax.swing.plaf.basic.BasicCheckBoxUI");
        UIManager.put("RadioButtonUI", "javax.swing.plaf.basic.BasicRadioButtonUI");
        UIManager.put("FileChooserUI", "javax.swing.plaf.basic.BasicFileChooserUI");

        Font ui = Theme.ui();
        UIManager.put("Label.font", ui);
        UIManager.put("Button.font", ui);
        UIManager.put("ToggleButton.font", ui);
        UIManager.put("Menu.font", ui);
        UIManager.put("MenuItem.font", ui);
        UIManager.put("CheckBoxMenuItem.font", ui);
        UIManager.put("RadioButtonMenuItem.font", ui);
        UIManager.put("MenuBar.font", ui);
        UIManager.put("List.font", ui);
        UIManager.put("TextField.font", ui);
        UIManager.put("TextArea.font", ui);
        UIManager.put("TextPane.font", ui);
        UIManager.put("ToolTip.font", ui);
        UIManager.put("CheckBox.font", ui);
        UIManager.put("RadioButton.font", ui);
        UIManager.put("Slider.font", ui);

        UIManager.put("Panel.background", Theme.WINDOW);
        UIManager.put("OptionPane.background", Theme.WINDOW);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        UIManager.put("Label.foreground", Theme.TEXT);
        UIManager.put("Label.disabledForeground", Theme.TEXT_DISABLED);
        UIManager.put("Button.foreground", Theme.TEXT);
        UIManager.put("Button.disabledText", Theme.TEXT_DISABLED);
        UIManager.put("MenuBar.background", Theme.WINDOW);
        UIManager.put("MenuBar.foreground", Theme.TEXT);
        UIManager.put("Menu.background", Theme.WINDOW);
        UIManager.put("Menu.foreground", Theme.TEXT);
        UIManager.put("Menu.selectionBackground", Theme.SELECTION);
        UIManager.put("Menu.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("Menu.acceleratorForeground", Theme.TEXT);
        UIManager.put("Menu.acceleratorSelectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("Menu.disabledForeground", Theme.TEXT_DISABLED);
        javax.swing.border.Border menuItemBorder =
                new javax.swing.plaf.BorderUIResource.EmptyBorderUIResource(5, 10, 5, 10);
        UIManager.put("MenuItem.border", menuItemBorder);
        UIManager.put("CheckBoxMenuItem.border", menuItemBorder);
        UIManager.put("RadioButtonMenuItem.border", menuItemBorder);
        UIManager.put("MenuItem.background", Theme.POPUP);
        UIManager.put("MenuItem.foreground", Theme.TEXT);
        UIManager.put("MenuItem.selectionBackground", Theme.SELECTION);
        UIManager.put("MenuItem.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("MenuItem.acceleratorForeground", new Color(0x6A6A6A));
        UIManager.put("MenuItem.acceleratorSelectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("MenuItem.disabledForeground", Theme.TEXT_DISABLED);
        UIManager.put("CheckBoxMenuItem.background", Theme.POPUP);
        UIManager.put("CheckBoxMenuItem.foreground", Theme.TEXT);
        UIManager.put("CheckBoxMenuItem.selectionBackground", Theme.SELECTION);
        UIManager.put("CheckBoxMenuItem.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("CheckBoxMenuItem.acceleratorForeground", new Color(0x6A6A6A));
        UIManager.put("CheckBoxMenuItem.acceleratorSelectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("CheckBoxMenuItem.disabledForeground", Theme.TEXT_DISABLED);
        UIManager.put("RadioButtonMenuItem.background", Theme.POPUP);
        UIManager.put("RadioButtonMenuItem.foreground", Theme.TEXT);
        UIManager.put("RadioButtonMenuItem.selectionBackground", Theme.SELECTION);
        UIManager.put("RadioButtonMenuItem.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("RadioButtonMenuItem.acceleratorForeground", new Color(0x6A6A6A));
        UIManager.put("RadioButtonMenuItem.acceleratorSelectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("RadioButtonMenuItem.disabledForeground", Theme.TEXT_DISABLED);
        UIManager.put("PopupMenu.background", Theme.POPUP);
        UIManager.put("PopupMenu.border", Theme.popupBorder());
        UIManager.put("Separator.foreground", new Color(0xD5D2C4));
        UIManager.put("Separator.background", Color.WHITE);

        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.foreground", Theme.TEXT);
        UIManager.put("List.selectionBackground", Theme.SELECTION);
        UIManager.put("List.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("List.focusCellHighlightBorder", javax.swing.BorderFactory.createLineBorder(Theme.SELECTION));
        UIManager.put("List.selectionInactiveBackground", new Color(0xD8DFEA));
        UIManager.put("List.selectionInactiveForeground", Theme.TEXT);

        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", Theme.TEXT);
        UIManager.put("TextField.caretForeground", Theme.TEXT);
        UIManager.put("TextField.selectionBackground", Theme.SELECTION);
        UIManager.put("TextField.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("TextField.border", Theme.sunkenBorder());
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", Theme.TEXT);
        UIManager.put("TextArea.border", Theme.sunkenBorder());
        UIManager.put("TextArea.selectionBackground", Theme.SELECTION);
        UIManager.put("TextArea.selectionForeground", Theme.SELECTION_TEXT);
        UIManager.put("TextPane.background", Color.WHITE);
        UIManager.put("TextPane.border", Theme.sunkenBorder());

        UIManager.put("ScrollPane.border", javax.swing.BorderFactory.createEmptyBorder());
        UIManager.put("ScrollPane.background", Theme.WINDOW);

        UIManager.put("ToolTip.background", new Color(0xFFFFE1));
        UIManager.put("ToolTip.foreground", Theme.TEXT);
        UIManager.put("ToolTip.border", javax.swing.BorderFactory.createLineBorder(new Color(0x6E6E5E)));

        UIManager.put("CheckBox.background", Theme.WINDOW);
        UIManager.put("CheckBox.foreground", Theme.TEXT);
        UIManager.put("CheckBox.font", ui);
        UIManager.put("CheckBox.icon", Icons.stateIcon("checkbox"));
        UIManager.put("RadioButton.background", Theme.WINDOW);
        UIManager.put("RadioButton.foreground", Theme.TEXT);
        UIManager.put("RadioButton.font", ui);
        UIManager.put("RadioButton.icon", Icons.stateIcon("radio"));
        UIManager.put("CheckBoxMenuItem.checkIcon", Icons.icon("check", 13));
        UIManager.put("RadioButtonMenuItem.checkIcon", Icons.icon("radioDot", 13));
    }

    /** Compact menu bar with a one pixel bottom rule. */
    public static class XpMenuBar extends javax.swing.JMenuBar {
        public XpMenuBar() {
            setOpaque(false);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 2));
        }

        @Override
        protected void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg.create();
            Theme.vertical(g, 0, 0, getWidth(), getHeight(), new Color(0xFDFCF6), Theme.WINDOW);
            g.setColor(Theme.BORDER);
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g.setColor(Color.WHITE);
            g.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
            g.dispose();
        }
    }
}
