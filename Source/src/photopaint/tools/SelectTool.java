package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Rectangle;

public class SelectTool extends Tool {

    private double sx;
    private double sy;
    private boolean active;

    public SelectTool() {
        super("select", "Выделение", "M", "Прямоугольное выделение. Shift — квадрат, клик без перетаскивания снимает выделение.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1) return;
        sx = e.x;
        sy = e.y;
        active = true;
        ed.doc.setSelection(new Rectangle(e.ix(), e.iy(), 0, 0));
        ed.fire("selection");
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        double x2 = e.x;
        double y2 = e.y;
        if (e.shift) {
            double dx = x2 - sx;
            double dy = y2 - sy;
            double s = Math.max(Math.abs(dx), Math.abs(dy));
            x2 = sx + (dx < 0 ? -s : s);
            y2 = sy + (dy < 0 ? -s : s);
        }
        int x = (int) Math.round(Math.min(sx, x2));
        int y = (int) Math.round(Math.min(sy, y2));
        int w = (int) Math.round(Math.abs(x2 - sx));
        int h = (int) Math.round(Math.abs(y2 - sy));
        ed.doc.setSelection(new Rectangle(x, y, w, h));
        ed.fire("selection");
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        active = false;
        Rectangle sel = ed.doc.selectionOrNull();
        if (sel == null || sel.width < 3 || sel.height < 3) {
            ed.doc.clearSelection();
        }
        ed.fire("selection");
    }
}
