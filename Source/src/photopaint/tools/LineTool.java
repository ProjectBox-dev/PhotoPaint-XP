package photopaint.tools;

import photopaint.Editor;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class LineTool extends ShapeTool {

    public LineTool() {
        super("line", "Линия", "L", "Прямая линия. Shift — под углом 45°.");
    }

    @Override
    protected void drawShape(Graphics2D g, Editor ed, boolean preview) {
        double x2 = cx;
        double y2 = cy;
        if (shift) {
            double dx = x2 - sx;
            double dy = y2 - sy;
            double len = Math.hypot(dx, dy);
            double ang = Math.atan2(dy, dx);
            double snap = Math.round(ang / (Math.PI / 4)) * (Math.PI / 4);
            x2 = sx + Math.cos(snap) * len;
            y2 = sy + Math.sin(snap) * len;
        }
        g.setColor(ed.colors.getForeground());
        g.draw(new Line2D.Double(sx, sy, x2, y2));
    }
}
