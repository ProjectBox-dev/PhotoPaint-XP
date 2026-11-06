package photopaint.ui;

import photopaint.Editor;
import photopaint.Theme;
import photopaint.model.Document;
import photopaint.tools.ToolEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

/** The scrollable drawing surface. */
public class CanvasPanel extends JComponent {

    public static final int MARGIN = 28;
    private static final int[] ZOOMS = {10, 25, 33, 50, 67, 75, 100, 125, 150, 200, 300, 400, 600, 800};

    private final Editor ed;
    private int antPhase;
    private boolean mouseInside;
    private double mouseDocX;
    private double mouseDocY;
    private int mouseScreenX;
    private int mouseScreenY;
    private BiConsumer<Integer, Integer> cursorListener;
    private boolean showBrushOutline = true;

    public CanvasPanel(Editor ed) {
        this.ed = ed;
        setOpaque(true);
        setBackground(Theme.WORKSPACE);
        setFocusable(true);
        setDoubleBuffered(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                updateMouse(e);
                if (ed.doc == null) return;
                ed.tool.press(ed, CanvasPanel.this, event(e));
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateMouse(e);
                if (ed.doc == null) return;
                ed.tool.drag(ed, CanvasPanel.this, event(e));
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                updateMouse(e);
                if (ed.doc == null) return;
                ed.tool.release(ed, CanvasPanel.this, event(e));
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouse(e);
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseInside = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseInside = false;
                if (cursorListener != null) cursorListener.accept(null, null);
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(this::wheel);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repaint();
            }
        });

        Timer ants = new Timer(150, e -> {
            if (ed.doc != null && ed.doc.selectionOrNull() != null) {
                antPhase = (antPhase + 1) % 8;
                repaint();
            }
        });
        ants.start();
        setCursor(ed.tool.cursor());
    }

    public void toolChanged() {
        setCursor(ed.tool.cursor());
        repaint();
    }

    public void setCursorListener(BiConsumer<Integer, Integer> l) {
        cursorListener = l;
    }

    public void setShowBrushOutline(boolean show) {
        showBrushOutline = show;
    }

    private void wheel(MouseWheelEvent e) {
        if (e.isControlDown()) {
            zoomAt(e.getX(), e.getY(), e.getWheelRotation() < 0 ? 1 : -1);
        } else {
            JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (sp != null) {
                JViewport vp = sp.getViewport();
                Point p = vp.getViewPosition();
                int dy = e.getWheelRotation() * 32;
                int maxY = Math.max(0, getHeight() - vp.getHeight());
                vp.setViewPosition(new Point(p.x, Math.max(0, Math.min(maxY, p.y + dy))));
            }
        }
    }

    private void updateMouse(MouseEvent e) {
        mouseScreenX = e.getX();
        mouseScreenY = e.getY();
        int ox = originX();
        int oy = originY();
        double z = ed.zoom / 100.0;
        mouseDocX = (e.getX() - ox) / z;
        mouseDocY = (e.getY() - oy) / z;
        mouseInside = ed.doc != null && mouseDocX >= 0 && mouseDocY >= 0
                && mouseDocX < ed.doc.width && mouseDocY < ed.doc.height;
        if (cursorListener != null) {
            if (mouseInside) {
                cursorListener.accept((int) Math.floor(mouseDocX), (int) Math.floor(mouseDocY));
            } else {
                cursorListener.accept(null, null);
            }
        }
    }

    private ToolEvent event(MouseEvent e) {
        int ox = originX();
        int oy = originY();
        double z = ed.zoom / 100.0;
        return new ToolEvent((e.getX() - ox) / z, (e.getY() - oy) / z,
                e.getX(), e.getY(), e);
    }

    public int originX() {
        int iw = imageWidth();
        return (getWidth() - iw) / 2;
    }

    public int originY() {
        int ih = imageHeight();
        return (getHeight() - ih) / 2;
    }

    private int imageWidth() {
        return Math.max(1, (int) Math.round(ed.doc.width * ed.zoom / 100.0));
    }

    private int imageHeight() {
        return Math.max(1, (int) Math.round(ed.doc.height * ed.zoom / 100.0));
    }

    @Override
    public Dimension getPreferredSize() {
        if (ed.doc == null) return new Dimension(400, 300);
        return new Dimension(imageWidth() + MARGIN * 2, imageHeight() + MARGIN * 2);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        int w = getWidth();
        int h = getHeight();
        g.setColor(Theme.WORKSPACE);
        g.fillRect(0, 0, w, h);
        GradientPaintBackdrop(g, w, h);

        if (ed.doc == null) {
            g.dispose();
            return;
        }
        int iw = imageWidth();
        int ih = imageHeight();
        int ox = originX();
        int oy = originY();

        g.setColor(new Color(0, 0, 0, 70));
        g.fillRect(ox + 4, oy + 4, iw, ih);

        g.setColor(new Color(0xF2F2F2));
        g.fillRect(ox, oy, iw, ih);
        checker(g, ox, oy, iw, ih);

        BufferedImage comp = ed.doc.composite();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                ed.zoom >= 100 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                        : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(comp, ox, oy, iw, ih, null);

        if (ed.grid && ed.zoom >= 40) drawGrid(g, ox, oy, iw, ih);

        g.setColor(new Color(0x303030));
        g.drawRect(ox - 1, oy - 1, iw + 1, ih + 1);
        g.setColor(new Color(255, 255, 255, 90));
        g.drawRect(ox - 2, oy - 2, iw + 3, ih + 3);

        drawSelection(g, ox, oy);

        Graphics2D oc = (Graphics2D) g.create();
        oc.translate(ox, oy);
        oc.scale(ed.zoom / 100.0, ed.zoom / 100.0);
        oc.clip(new Rectangle(0, 0, ed.doc.width, ed.doc.height));
        ed.tool.overlay(oc, ed);
        oc.dispose();

        if (showBrushOutline && mouseInside && ed.tool.showsBrushOutline()) {
            int size = (int) Math.max(2, Math.round(ed.tool.brushOutlineSize(ed) * ed.zoom / 100.0));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(mouseScreenX - size / 2, mouseScreenY - size / 2, size, size);
            g.setColor(new Color(0, 0, 0, 170));
            g.setStroke(new BasicStroke(1f));
            g.drawOval(mouseScreenX - size / 2, mouseScreenY - size / 2, size, size);
        }
        g.dispose();
    }

    private void GradientPaintBackdrop(Graphics2D g, int w, int h) {
        g.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x8C8C8C), 0, h,
                new Color(0x6E6E6E)));
        g.fillRect(0, 0, w, h);
    }

    private void checker(Graphics2D g, int ox, int oy, int iw, int ih) {
        Graphics2D c = (Graphics2D) g.create();
        c.clipRect(ox, oy, iw, ih);
        int sq = 8;
        for (int y = 0; y < ih; y += sq) {
            for (int x = 0; x < iw; x += sq) {
                boolean light = ((x / sq) + (y / sq)) % 2 == 0;
                c.setColor(light ? new Color(0xFFFFFF) : new Color(0xD4D0C8));
                c.fillRect(ox + x, oy + y, sq, sq);
            }
        }
        c.dispose();
    }

    private void drawGrid(Graphics2D g, int ox, int oy, int iw, int ih) {
        int step = (int) Math.round(16 * ed.zoom / 100.0);
        if (step < 4) return;
        g.setColor(new Color(255, 255, 255, 70));
        for (int x = step; x < iw; x += step) g.drawLine(ox + x, oy, ox + x, oy + ih);
        for (int y = step; y < ih; y += step) g.drawLine(ox, oy + y, ox + iw, oy + y);
        g.setColor(new Color(0, 0, 0, 50));
        for (int x = step; x < iw; x += step) g.drawLine(ox + x + 1, oy, ox + x + 1, oy + ih);
    }

    private void drawSelection(Graphics2D g, int ox, int oy) {
        Shape sel = ed.doc.selectionShape();
        if (sel == null) return;
        double z = ed.zoom / 100.0;
        AffineTransform at = AffineTransform.getTranslateInstance(ox, oy);
        at.scale(z, z);
        Shape screen = at.createTransformedShape(sel);
        float[] dash = {4f, 4f};
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, dash, antPhase));
        g.setColor(Color.BLACK);
        g.draw(screen);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, dash, antPhase + 4));
        g.setColor(Color.WHITE);
        g.draw(screen);
    }

    public void zoomAt(int screenX, int screenY, int dir) {
        int next = ed.zoom;
        if (dir > 0) {
            for (int z : ZOOMS) {
                if (z > ed.zoom) {
                    next = z;
                    break;
                }
            }
            if (next == ed.zoom) next = Math.min(800, ed.zoom + 100);
        } else {
            for (int i = ZOOMS.length - 1; i >= 0; i--) {
                if (ZOOMS[i] < ed.zoom) {
                    next = ZOOMS[i];
                    break;
                }
            }
            if (next == ed.zoom) next = Math.max(5, ed.zoom - 100);
        }
        zoomTo(next, screenX, screenY);
    }

    public void zoomTo(int percent, int screenX, int screenY) {
        if (percent == ed.zoom) return;
        int ox = originX();
        int oy = originY();
        double docX = (screenX - ox) * 100.0 / ed.zoom;
        double docY = (screenY - oy) * 100.0 / ed.zoom;
        ed.zoom = Math.max(5, Math.min(800, percent));
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            int nx = (int) Math.round(docX * ed.zoom / 100.0 + originX());
            int ny = (int) Math.round(docY * ed.zoom / 100.0 + originY());
            scrollRectToVisible(new Rectangle(nx - 2, ny - 2, 5, 5));
        });
        ed.fire("zoom");
    }

    public void setZoomPercent(int percent) {
        Rectangle vis = getVisibleRect();
        zoomTo(percent, vis.x + vis.width / 2, vis.y + vis.height / 2);
    }

    public void fitToWindow() {
        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (sp == null || ed.doc == null) return;
        Dimension d = sp.getViewport().getExtentSize();
        double zw = (d.width - MARGIN * 2.0) * 100.0 / ed.doc.width;
        double zh = (d.height - MARGIN * 2.0) * 100.0 / ed.doc.height;
        int pct = (int) Math.floor(Math.min(zw, zh));
        setZoomPercent(Math.max(5, Math.min(800, pct)));
    }

    public void panBy(int dx, int dy) {
        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (sp == null) return;
        JViewport vp = sp.getViewport();
        Point p = vp.getViewPosition();
        int maxX = Math.max(0, getWidth() - vp.getWidth());
        int maxY = Math.max(0, getHeight() - vp.getHeight());
        vp.setViewPosition(new Point(Math.max(0, Math.min(maxX, p.x + dx)),
                Math.max(0, Math.min(maxY, p.y + dy))));
    }

    public void centerView() {
        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (sp == null) return;
        JViewport vp = sp.getViewport();
        int x = Math.max(0, (getWidth() - vp.getWidth()) / 2);
        int y = Math.max(0, (getHeight() - vp.getHeight()) / 2);
        vp.setViewPosition(new Point(x, y));
    }
}
