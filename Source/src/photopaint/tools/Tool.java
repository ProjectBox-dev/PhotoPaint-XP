package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Cursor;
import java.awt.Graphics2D;

/** Base class for canvas tools. Only the active tool gets events. */
public abstract class Tool {

    public final String id;
    public final String name;
    public final String shortcut;
    public final String hint;

    protected Tool(String id, String name, String shortcut, String hint) {
        this.id = id;
        this.name = name;
        this.shortcut = shortcut;
        this.hint = hint;
    }

    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
    }

    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
    }

    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
    }

    /** Overlay is drawn already transformed into document coordinates. */
    public void overlay(Graphics2D g, Editor ed) {
    }

    public Cursor cursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    public boolean showsBrushOutline() {
        return false;
    }

    public int brushOutlineSize(Editor ed) {
        return 0;
    }
}
