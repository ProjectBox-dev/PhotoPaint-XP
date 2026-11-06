package photopaint.ui;

import photopaint.Theme;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

/** Windows XP Luna style push button. */
public class XpButtonUI extends BasicButtonUI {

    public static ComponentUI createUI(JComponent c) {
        return new XpButtonUI();
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setFont(Theme.ui());
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setRolloverEnabled(true);
        if (b.getMargin() == null || b.getMargin().equals(new Insets(2, 14, 2, 14))) {
            b.setMargin(new Insets(2, 12, 2, 12));
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        d.width += 10;
        d.height = Math.max(d.height, 23);
        return d;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }

    @Override
    public void paint(Graphics gg, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Graphics2D g = (Graphics2D) gg.create();
        Theme.aa(g);
        int w = c.getWidth();
        int h = c.getHeight();
        boolean pressed = b.getModel().isArmed() && b.getModel().isPressed();
        boolean hover = b.getModel().isRollover() && b.isEnabled();
        boolean def = b instanceof JButton && ((JButton) b).isDefaultButton();

        Color top;
        Color bottom;
        Color border;
        if (!b.isEnabled()) {
            top = new Color(0xF6F4EC);
            bottom = new Color(0xEAE7DB);
            border = new Color(0xC2BEAF);
        } else if (pressed) {
            top = new Color(0xD9D4C4);
            bottom = new Color(0xC9C3B0);
            border = new Color(0x7F9DB9);
        } else if (hover) {
            top = new Color(0xFFF9E8);
            bottom = new Color(0xFFD98A);
            border = new Color(0xE0A33E);
        } else {
            top = Theme.BUTTON_TOP;
            bottom = Theme.BUTTON_BOTTOM;
            border = def ? new Color(0x0A56A5) : new Color(0x8D8A7C);
        }

        g.setColor(new Color(255, 255, 255, 160));
        g.fillRoundRect(0, 0, w, h, 5, 5);
        g.setColor(border);
        g.drawRoundRect(0, 0, w - 1, h - 1, 5, 5);
        g.setPaint(new GradientPaint(0, 1, top, 0, h - 2, bottom));
        g.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);
        if (def && b.isEnabled()) {
            g.setColor(new Color(255, 255, 255, 110));
            g.drawRoundRect(1, 1, w - 3, h - 3, 4, 4);
        }
        g.dispose();

        super.paint(gg, c);

        if (b.isFocusOwner() && b.isEnabled()) {
            Graphics2D f = (Graphics2D) gg.create();
            f.setColor(new Color(0x2A4F7E));
            f.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{1f, 1f}, 0f));
            f.drawRoundRect(3, 3, w - 7, h - 7, 3, 3);
            f.dispose();
        }
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        if (!b.isEnabled()) {
            g.setColor(Theme.TEXT_DISABLED);
            FontMetrics fm = g.getFontMetrics();
            int x = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
            g.drawString(text, x, textRect.y + fm.getAscent());
        } else {
            super.paintText(g, b, textRect, text);
        }
    }
}
