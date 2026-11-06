package photopaint.tools;

import photopaint.Editor;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class EllipseTool extends ShapeTool {

    public EllipseTool() {
        super("ellipse", "Эллипс", "O", "Эллипс. Shift — круг, режим заливки сверху.");
    }

    @Override
    protected void drawShape(Graphics2D g, Editor ed, boolean preview) {
        Rectangle2D r = rect();
        paintShape(g, ed, r, true);
    }
}
