package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PencilTool extends Tool {

    private boolean drawing;
    private double lastX;
    private double lastY;
    private boolean changed;

    public PencilTool() {
        super("pencil", "Карандаш", "N", "Жёсткий пиксельный карандаш.");
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
        ToolPaint.pencilLine(img, ed.doc.clipShape(), e.x, e.y, e.x, e.y, ed.pencilSize,
                ed.colors.getForeground(), ed.opacity / 100f);
        changed = true;
        ed.touch();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!drawing) return;
        ToolPaint.pencilLine(ed.doc.activeImage(), ed.doc.clipShape(), lastX, lastY, e.x, e.y,
                ed.pencilSize, ed.colors.getForeground(), ed.opacity / 100f);
        lastX = e.x;
        lastY = e.y;
        changed = true;
        ed.touch();
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (drawing && changed) ed.commit(I18n.t("Карандаш"));
        drawing = false;
    }

    @Override
    public boolean showsBrushOutline() {
        return true;
    }

    @Override
    public int brushOutlineSize(Editor ed) {
        return Math.max(2, ed.pencilSize);
    }

    static boolean inside(photopaint.model.Document d, double x, double y) {
        return x >= 0 && y >= 0 && x < d.width && y < d.height;
    }
}
