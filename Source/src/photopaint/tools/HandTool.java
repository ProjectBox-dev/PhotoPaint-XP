package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Cursor;

public class HandTool extends Tool {

    private int lastX;
    private int lastY;
    private boolean active;

    public HandTool() {
        super("hand", "Рука", "H", "Перетаскивание для прокрутки холста.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1) return;
        active = true;
        lastX = e.source.getXOnScreen();
        lastY = e.source.getYOnScreen();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        int x = e.source.getXOnScreen();
        int y = e.source.getYOnScreen();
        // Screen coordinates: canvas-local coordinates change while the viewport scrolls.
        canvas.panBy(lastX - x, lastY - y);
        lastX = x;
        lastY = y;
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        active = false;
    }

    @Override
    public Cursor cursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }
}
