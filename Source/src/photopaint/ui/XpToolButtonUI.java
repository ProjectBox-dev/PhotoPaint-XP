package photopaint.ui;

import photopaint.Theme;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Square tool button for the toolbox. */
public class XpToolButtonUI extends BasicToggleButtonUI {

    public static ComponentUI createUI(JComponent c) {
        return new XpToolButtonUI();
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
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(28, 28);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
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
        boolean selected = b.isSelected();
        boolean pressed = b.getModel().isArmed() && b.getModel().isPressed();
        boolean hover = b.getModel().isRollover() && b.isEnabled();

        if (selected || pressed) {
            g.setColor(new Color(0xA9C2E8));
            g.fillRoundRect(1, 1, w - 2, h - 2, 3, 3);
            g.setColor(new Color(0x7F9DB9));
            g.drawRoundRect(1, 1, w - 3, h - 3, 3, 3);
            g.setColor(new Color(255, 255, 255, 120));
            g.drawLine(3, 2, w - 4, 2);
        } else if (hover) {
            g.setPaint(new GradientPaint(0, 0, new Color(0xFFFBEA), 0, h,
                    new Color(0xFFE9AE)));
            g.fillRoundRect(1, 1, w - 2, h - 2, 3, 3);
            g.setColor(new Color(0xE0B85A));
            g.drawRoundRect(1, 1, w - 3, h - 3, 3, 3);
        }
        if (selected) {
            g.setColor(new Color(255, 255, 255, 110));
            g.setStroke(new BasicStroke(1f));
            g.drawLine(3, h - 3, w - 4, h - 3);
        }
        g.dispose();
        super.paint(gg, c);
    }
}
