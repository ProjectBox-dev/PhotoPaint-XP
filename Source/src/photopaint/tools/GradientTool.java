package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class GradientTool extends Tool {

    private double sx;
    private double sy;
    private double ex;
    private double ey;
    private boolean active;

    public GradientTool() {
        super("gradient", "Градиент", "G", "Линейный или радиальный градиент от основного цвета.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1) return;
        sx = ex = e.x;
        sy = ey = e.y;
        active = true;
        canvas.repaint();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        ex = e.x;
        ey = e.y;
        if (e.shift) {
            double dx = ex - sx;
            double dy = ey - sy;
            double len = Math.hypot(dx, dy);
            double ang = Math.atan2(dy, dx);
            double snap = Math.round(ang / (Math.PI / 4)) * (Math.PI / 4);
            ex = sx + Math.cos(snap) * len;
            ey = sy + Math.sin(snap) * len;
        }
        canvas.repaint();
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        active = false;
        double dx = ex - sx;
        double dy = ey - sy;
        if (Math.hypot(dx, dy) < 2) {
            ex = sx + Math.max(40, ed.doc.width / 2.0);
            ey = sy;
        }
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        Graphics2D g = ToolPaint.prepare(img, ed.doc.clipShape());
        applyGradient(g, ed, false);
        g.dispose();
        ed.commit(I18n.t("Градиент"));
        canvas.repaint();
    }

    @Override
    public void overlay(Graphics2D g, Editor ed) {
        if (!active) return;
        java.awt.Composite old = g.getComposite();
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.75f));
        applyGradient(g, ed, true);
        g.setComposite(old);
    }

    private void applyGradient(Graphics2D g, Editor ed, boolean preview) {
        Color c1 = ed.colors.getForeground();
        Color c2 = ed.gradientTransparent
                ? new Color(ed.colors.getBackground().getRed(), ed.colors.getBackground().getGreen(),
                ed.colors.getBackground().getBlue(), 0)
                : ed.colors.getBackground();
        int w = ed.doc.width;
        int h = ed.doc.height;
        if (ed.gradientType == 1) {
            double radius = Math.max(1, Math.hypot(ex - sx, ey - sy));
            RadialGradientPaint p = new RadialGradientPaint(
                    new Point2D.Double(sx, sy), (float) radius,
                    new float[]{0f, 1f}, new Color[]{c1, c2});
            g.setPaint(p);
        } else {
            g.setPaint(new GradientPaint((float) sx, (float) sy, c1, (float) ex, (float) ey, c2));
        }
        g.fillRect(0, 0, w, h);
    }
}
