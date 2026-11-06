package photopaint.ui;

import photopaint.Theme;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Recessed XP track with a rounded thumb. */
public class XpSliderUI extends BasicSliderUI {

    public XpSliderUI(JSlider b) {
        super(b);
    }

    public static ComponentUI createUI(JComponent c) {
        return new XpSliderUI((JSlider) c);
    }

    @Override
    protected void installDefaults(JSlider slider) {
        super.installDefaults(slider);
        slider.setFont(Theme.ui());
        slider.setOpaque(false);
        slider.setFocusable(false);
    }

    @Override
    public Dimension getThumbSize() {
        return new Dimension(11, 19);
    }

    @Override
    public void paintTrack(Graphics gg) {
        Rectangle track = trackRect;
        Graphics2D g = (Graphics2D) gg.create();
        Theme.aa(g);
        g.setStroke(new BasicStroke(1f));
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int y = track.y + track.height / 2 - 2;
            g.setColor(new Color(0x8A8678));
            g.fillRoundRect(track.x, y, track.width, 4, 2, 2);
            g.setColor(Color.WHITE);
            g.fillRoundRect(track.x + 1, y + 1, track.width - 2, 2, 1, 1);
            int fillW = thumbRect.x + thumbRect.width / 2 - track.x;
            if (fillW > 0) {
                g.setColor(new Color(0x5F96DB));
                g.fillRoundRect(track.x + 1, y + 1, Math.min(fillW, track.width - 2), 2, 1, 1);
            }
        } else {
            int x = track.x + track.width / 2 - 2;
            g.setColor(new Color(0x8A8678));
            g.fillRoundRect(x, track.y, 4, track.height, 2, 2);
            g.setColor(Color.WHITE);
            g.fillRoundRect(x + 1, track.y + 1, 2, track.height - 2, 1, 1);
        }
        g.dispose();
    }

    @Override
    public void paintThumb(Graphics gg) {
        Rectangle t = thumbRect;
        if (t.width <= 0 || t.height <= 0) return;
        Graphics2D g = (Graphics2D) gg.create();
        Theme.aa(g);
        Color top = new Color(0xFEFEFC);
        Color bottom = new Color(0xD8D3C3);
        g.setColor(new Color(0x7F9DB9));
        g.drawRoundRect(t.x, t.y, t.width - 1, t.height - 1, 4, 4);
        g.setPaint(new GradientPaint(0, t.y + 1, top, 0, t.y + t.height - 2, bottom));
        g.fillRoundRect(t.x + 1, t.y + 1, t.width - 2, t.height - 2, 3, 3);
        g.setColor(new Color(255, 255, 255, 190));
        g.drawLine(t.x + 2, t.y + 2, t.x + t.width - 3, t.y + 2);
        g.setColor(new Color(0x9D998B));
        int cx = t.x + t.width / 2;
        for (int dy = -4; dy <= 4; dy += 4) {
            g.drawLine(cx - 2, t.y + t.height / 2 + dy, cx + 2, t.y + t.height / 2 + dy);
        }
        g.dispose();
    }

    @Override
    public void paintFocus(Graphics g) {
        // no focus ring
    }
}
