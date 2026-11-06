package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.model.Document;
import photopaint.ui.CanvasPanel;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class BrushTool extends Tool {

    private boolean drawing;
    private double lastX;
    private double lastY;
    private boolean changed;

    public BrushTool() {
        super("brush", "Кисть", "B", "Мягкая кисть. Размер и жёсткость настраиваются сверху.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1) return;
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        drawing = true;
        changed = false;
        lastX = e.x;
        lastY = e.y;
        if (!inside(ed.doc, e.x, e.y)) return;
        Graphics2D g = ToolPaint.prepare(img, ed.doc.clipShape());
        ToolPaint.stamp(g, e.x, e.y, ed.brushSize, ed.hardness, ed.colors.getForeground(),
                ed.opacity / 100f, false);
        g.dispose();
        changed = true;
        ed.touch();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!drawing) return;
        if (!inside(ed.doc, e.x, e.y) && !inside(ed.doc, lastX, lastY)) {
            lastX = e.x;
            lastY = e.y;
            return;
        }
        Graphics2D g = ToolPaint.prepare(ed.doc.activeImage(), ed.doc.clipShape());
        ToolPaint.stroke(g, lastX, lastY, e.x, e.y, ed.brushSize, ed.hardness,
                ed.colors.getForeground(), ed.opacity / 100f, false);
        g.dispose();
        lastX = e.x;
        lastY = e.y;
        changed = true;
        ed.touch();
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (drawing && changed) ed.commit(I18n.t("Кисть"));
        drawing = false;
    }

    @Override
    public boolean showsBrushOutline() {
        return true;
    }

    @Override
    public int brushOutlineSize(Editor ed) {
        return ed.brushSize;
    }

    static boolean inside(Document d, double x, double y) {
        return x >= 0 && y >= 0 && x < d.width && y < d.height;
    }
}
