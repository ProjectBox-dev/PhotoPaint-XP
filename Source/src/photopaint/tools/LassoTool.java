package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/** Free-form (lasso) selection: the outline is traced with the mouse. */
public class LassoTool extends Tool {

    private final List<Point2D.Double> points = new ArrayList<>();
    private boolean active;

    public LassoTool() {
        super("lasso", "Лассо", "A",
                "Выделение произвольной формы. Обведите область мышью, отпустите кнопку — контур замкнётся.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1 || ed.doc.activeImage() == null) return;
        points.clear();
        points.add(new Point2D.Double(e.x, e.y));
        active = true;
        ed.doc.clearSelection();
        ed.fire("selection");
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        if (!addPoint(e.x, e.y)) return;
        ed.doc.setSelectionShape(outline());
        ed.fire("selection");
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        active = false;
        addPoint(e.x, e.y);
        Rectangle sel = null;
        if (points.size() >= 3) {
            ed.doc.setSelectionShape(outline());
            sel = ed.doc.selectionOrNull();
        }
        if (sel == null || sel.width < 3 || sel.height < 3) {
            ed.doc.clearSelection();
        }
        ed.fire("selection");
    }

    /** Adds a point unless it is almost on top of the previous one. */
    private boolean addPoint(double x, double y) {
        if (!points.isEmpty()) {
            Point2D.Double last = points.get(points.size() - 1);
            if (Math.hypot(x - last.x, y - last.y) < 1.0) return false;
        }
        points.add(new Point2D.Double(x, y));
        return true;
    }

    /** Closed outline through all sampled points. */
    private Path2D outline() {
        Path2D.Double p = new Path2D.Double(Path2D.WIND_NON_ZERO);
        Point2D.Double first = points.get(0);
        p.moveTo(first.x, first.y);
        for (int i = 1; i < points.size(); i++) {
            Point2D.Double pt = points.get(i);
            p.lineTo(pt.x, pt.y);
        }
        p.closePath();
        return p;
    }
}
