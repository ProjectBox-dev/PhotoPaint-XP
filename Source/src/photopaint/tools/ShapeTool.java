package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class ShapeTool extends Tool {

    protected double sx;
    protected double sy;
    protected double cx;
    protected double cy;
    protected boolean active;
    protected boolean shift;

    protected ShapeTool(String id, String name, String shortcut, String hint) {
        super(id, name, shortcut, hint);
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1) return;
        sx = cx = e.x;
        sy = cy = e.y;
        shift = e.shift;
        active = true;
        canvas.repaint();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        cx = e.x;
        cy = e.y;
        shift = e.shift;
        canvas.repaint();
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        cx = e.x;
        cy = e.y;
        shift = e.shift;
        active = false;
        if (Math.abs(cx - sx) < 1 && Math.abs(cy - sy) < 1) {
            canvas.repaint();
            return;
        }
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        Graphics2D g = ToolPaint.prepare(img, ed.doc.clipShape());
        configure(g, ed);
        drawShape(g, ed, false);
        g.dispose();
        ed.commit(name);
        canvas.repaint();
    }

    @Override
    public void overlay(Graphics2D g, Editor ed) {
        if (!active) return;
        configure(g, ed);
        drawShape(g, ed, true);
    }

    protected void configure(Graphics2D g, Editor ed) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                ed.antialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setStroke(new BasicStroke(Math.max(1, ed.lineWidth), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
    }

    protected Rectangle2D rect() {
        double x1 = sx;
        double y1 = sy;
        double x2 = cx;
        double y2 = cy;
        if (shift) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double s = Math.max(Math.abs(dx), Math.abs(dy));
            x2 = x1 + (dx < 0 ? -s : s);
            y2 = y1 + (dy < 0 ? -s : s);
        }
        return new Rectangle2D.Double(Math.min(x1, x2), Math.min(y1, y2),
                Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    protected void paintShape(Graphics2D g, Editor ed, Rectangle2D r, boolean ellipse) {
        Color fg = ed.colors.getForeground();
        Color bg = ed.colors.getBackground();
        if (ed.fillMode == 1 || ed.fillMode == 2) {
            g.setColor(bg);
            if (ellipse) g.fill(new Ellipse2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()));
            else g.fill(r);
        }
        if (ed.fillMode == 0 || ed.fillMode == 2) {
            g.setColor(fg);
            if (ellipse) g.draw(new Ellipse2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()));
            else g.draw(r);
        }
    }

    protected abstract void drawShape(Graphics2D g, Editor ed, boolean preview);
}
