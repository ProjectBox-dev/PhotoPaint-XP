package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Cursor;

public class ZoomTool extends Tool {

    public ZoomTool() {
        super("zoom", "Масштаб", "Z", "Щелчок — увеличить, Alt или правая кнопка — уменьшить.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        boolean out = e.alt || e.button == 3;
        canvas.zoomAt(e.screenX, e.screenY, out ? -1 : 1);
    }

    @Override
    public Cursor cursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }
}
