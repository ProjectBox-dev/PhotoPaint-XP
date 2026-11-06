package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class EraserTool extends Tool {

    private boolean drawing;
    private double lastX;
    private double lastY;
    private boolean changed;

    public EraserTool() {
        super("eraser", "Ластик", "E", "Стирает пиксели активного слоя до прозрачности.");
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
        if (!BrushTool.inside(ed.doc, e.x, e.y)) return;
        Graphics2D g = ToolPaint.prepare(img, ed.doc.clipShape());
        ToolPaint.stamp(g, e.x, e.y, ed.eraserSize, ed.hardness, java.awt.Color.WHITE,
                ed.opacity / 100f, true);
        g.dispose();
        changed = true;
        ed.touch();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!drawing) return;
        Graphics2D g = ToolPaint.prepare(ed.doc.activeImage(), ed.doc.clipShape());
        ToolPaint.stroke(g, lastX, lastY, e.x, e.y, ed.eraserSize, ed.hardness,
                java.awt.Color.WHITE, ed.opacity / 100f, true);
        g.dispose();
        lastX = e.x;
        lastY = e.y;
        changed = true;
        ed.touch();
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (drawing && changed) ed.commit(I18n.t("Ластик"));
        drawing = false;
    }

    @Override
    public boolean showsBrushOutline() {
        return true;
    }

    @Override
    public int brushOutlineSize(Editor ed) {
        return ed.eraserSize;
    }
}
