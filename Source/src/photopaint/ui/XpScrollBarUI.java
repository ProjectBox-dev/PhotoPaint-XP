package photopaint.ui;

import photopaint.Theme;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Thin Luna-like scrollbar. */
public class XpScrollBarUI extends BasicScrollBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new XpScrollBarUI();
    }

    @Override
    protected void configureScrollBarColors() {
        trackColor = new Color(0xF0EEE2);
        thumbColor = new Color(0xFFFFFF);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(16, 16);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return new ArrowButton(orientation, false);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return new ArrowButton(orientation, true);
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(10, 10);
    }

    @Override
    protected void paintTrack(Graphics gg, JComponent c, Rectangle track) {
        Graphics2D g = (Graphics2D) gg.create();
        g.setColor(new Color(0xF4F2E7));
        g.fillRect(track.x, track.y, track.width, track.height);
        g.setColor(new Color(0xD8D4C6));
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            g.drawLine(track.x + track.width - 1, track.y, track.x + track.width - 1,
                    track.y + track.height);
            g.setColor(new Color(0xFFFFFF));
            g.drawLine(track.x, track.y, track.x, track.y + track.height);
        } else {
            g.drawLine(track.x, track.y + track.height - 1, track.x + track.width,
                    track.y + track.height - 1);
            g.setColor(new Color(0xFFFFFF));
            g.drawLine(track.x, track.y, track.x + track.width, track.y);
        }
        g.dispose();
    }

    @Override
    protected void paintThumb(Graphics gg, JComponent c, Rectangle t) {
        if (t.width <= 0 || t.height <= 0) return;
        Graphics2D g = (Graphics2D) gg.create();
        Theme.aa(g);
        boolean hot = isThumbRollover() || isDragging;
        Color top = hot ? new Color(0xF3F8FF) : new Color(0xFFFFFF);
        Color bottom = hot ? new Color(0xC3D6F4) : new Color(0xD9D5C7);
        g.setColor(new Color(0x8D8A7C));
        g.drawRoundRect(t.x, t.y, t.width - 1, t.height - 1, 4, 4);
        g.setPaint(new GradientPaint(0, t.y, top, 0, t.y + t.height, bottom));
        g.fillRoundRect(t.x + 1, t.y + 1, t.width - 2, t.height - 2, 3, 3);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawLine(t.x + 2, t.y + 2, t.x + t.width - 3, t.y + 2);
        g.setColor(new Color(0x9D998B));
        if (t.width > t.height) {
            int cx = t.x + t.width / 2;
            int cy = t.y + t.height / 2;
            g.drawLine(cx - 8, cy - 2, cx + 8, cy - 2);
            g.drawLine(cx - 8, cy + 1, cx + 8, cy + 1);
        } else {
            int cx = t.x + t.width / 2;
            int cy = t.y + t.height / 2;
            g.drawLine(cx - 2, cy - 7, cx - 2, cy + 7);
            g.drawLine(cx + 1, cy - 7, cx + 1, cy + 7);
        }
        g.dispose();
    }

    private class ArrowButton extends JButton {
        private final int direction;
        private final boolean increase;

        ArrowButton(int direction, boolean increase) {
            super();
            this.direction = direction;
            this.increase = increase;
            setFocusable(false);
            setOpaque(false);
            setBorder(null);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setPreferredSize(new Dimension(16, 16));
            setRolloverEnabled(true);
            setFont(Theme.ui());
        }

        @Override
        protected void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg.create();
            Theme.aa(g);
            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isArmed() && getModel().isPressed();
            boolean hot = getModel().isRollover();
            Color top = pressed ? new Color(0xD9D4C4)
                    : hot ? new Color(0xFFF9E8) : new Color(0xFDFDFB);
            Color bottom = pressed ? new Color(0xC4BEAB)
                    : hot ? new Color(0xFFE3A4) : new Color(0xE3DFD0);
            g.setColor(new Color(0xC8C4B6));
            g.drawRect(0, 0, w - 1, h - 1);
            g.setPaint(new GradientPaint(0, 1, top, 0, h - 2, bottom));
            g.fillRect(1, 1, w - 2, h - 2);
            g.setColor(new Color(255, 255, 255, 170));
            g.drawLine(1, 1, w - 2, 1);

            int cx = w / 2;
            int cy = h / 2;
            g.setColor(new Color(0x1D3A66));
            int s = 3;
            switch (direction) {
                case SwingConstants.NORTH -> g.fillPolygon(
                        new int[]{cx, cx - s, cx + s}, new int[]{cy - s, cy + s, cy + s}, 3);
                case SwingConstants.SOUTH -> g.fillPolygon(
                        new int[]{cx, cx - s, cx + s}, new int[]{cy + s, cy - s, cy - s}, 3);
                case SwingConstants.WEST -> g.fillPolygon(
                        new int[]{cx - s, cx + s, cx + s}, new int[]{cy, cy - s, cy + s}, 3);
                default -> g.fillPolygon(
                        new int[]{cx + s, cx - s, cx - s}, new int[]{cy, cy - s, cy + s}, 3);
            }
            g.dispose();
        }
    }
}
