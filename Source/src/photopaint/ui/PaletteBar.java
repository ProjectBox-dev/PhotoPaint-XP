package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Bottom colour palette strip. */
public class PaletteBar extends JPanel {

    public PaletteBar(Editor ed) {
        super(new FlowLayout(FlowLayout.LEFT, 6, 4));
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xFFFFFF)),
                javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        JLabel label = new JLabel(I18n.t("Палитра:"));
        label.setFont(Theme.uiBold());
        add(label);
        PalettePanel palette = new PalettePanel(15, 14, (c, bg) -> {
            if (bg) ed.setBackground(c);
            else ed.setForeground(c);
        });
        add(palette);
        JLabel hint = new JLabel(I18n.t("  ЛКМ — основной цвет, ПКМ — фоновый"));
        hint.setFont(Theme.uiSmall());
        hint.setForeground(new Color(0x555555));
        add(hint);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        Theme.vertical(g, 0, 0, getWidth(), getHeight(), new Color(0xF6F4EA), new Color(0xE4E0D2));
        g.dispose();
    }
}
