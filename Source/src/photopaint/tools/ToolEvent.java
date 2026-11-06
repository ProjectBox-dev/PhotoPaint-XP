package photopaint.tools;

import java.awt.event.MouseEvent;

/** Mouse event translated to document coordinates. */
public class ToolEvent {

    public final double x;
    public final double y;
    public final int screenX;
    public final int screenY;
    public final boolean shift;
    public final boolean alt;
    public final boolean ctrl;
    public final int button;
    public final int clicks;
    public final MouseEvent source;

    public ToolEvent(double x, double y, int screenX, int screenY, MouseEvent source) {
        this.x = x;
        this.y = y;
        this.screenX = screenX;
        this.screenY = screenY;
        this.shift = source.isShiftDown();
        this.alt = source.isAltDown();
        this.ctrl = source.isControlDown();
        this.button = source.getButton();
        this.clicks = source.getClickCount();
        this.source = source;
    }

    public int ix() {
        return (int) Math.floor(x);
    }

    public int iy() {
        return (int) Math.floor(y);
    }
}
