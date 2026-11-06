package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class MoveTool extends Tool {

    private BufferedImage original;
    private BufferedImage base;
    private BufferedImage floating;
    private Rectangle selectionStart;
    private Shape selectionStartShape;
    private boolean active;
    private boolean moved;
    private double startX;
    private double startY;
    private int dx;
    private int dy;

    public MoveTool() {
        super("move", "Перемещение", "V", "Двигает содержимое слоя или выделенной области.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1 || ed.doc.activeImage() == null) return;
        original = ToolPaint.copy(ed.doc.activeImage());
        selectionStart = ed.doc.selectionOrNull();
        selectionStartShape = selectionStart == null ? null : ed.doc.selectionShape();
        if (selectionStart != null) {
            floating = ToolPaint.copyRegion(original, selectionStart);
            ToolPaint.keepOnly(floating, selectionStartShape, selectionStart.x, selectionStart.y);
            base = ToolPaint.copy(original);
            ToolPaint.clearShape(base, selectionStartShape);
        } else {
            floating = original;
            base = new BufferedImage(original.getWidth(), original.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }
        startX = e.x;
        startY = e.y;
        dx = 0;
        dy = 0;
        moved = false;
        active = true;
        ed.doc.activeLayer().image = ToolPaint.copy(base);
        ed.touch();
    }

    @Override
    public void drag(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        int ndx = (int) Math.round(e.x - startX);
        int ndy = (int) Math.round(e.y - startY);
        if (e.shift) {
            if (Math.abs(ndx) > Math.abs(ndy)) ndy = 0;
            else ndx = 0;
        }
        if (ndx == dx && ndy == dy) return;
        dx = ndx;
        dy = ndy;
        moved = true;
        compose(ed);
    }

    private void compose(Editor ed) {
        BufferedImage composed = ToolPaint.copy(base);
        // The floating region is pasted in document coordinates: its original
        // top-left corner plus the drag offset. Pasting at (dx, dy) alone moved
        // the selection to the top-left corner of the canvas.
        int pasteX = dx;
        int pasteY = dy;
        if (selectionStart != null) {
            pasteX += selectionStart.x;
            pasteY += selectionStart.y;
        }
        ToolPaint.paste(composed, floating, pasteX, pasteY, null);
        ed.doc.activeLayer().image = composed;
        if (selectionStart != null) applySelection(ed);
        ed.touch();
    }

    /** Places the moved outline at the current drag offset, clamped to the canvas. */
    private void applySelection(Editor ed) {
        if (selectionStartShape != null) {
            ed.doc.setSelectionShape(AffineTransform.getTranslateInstance(dx, dy)
                    .createTransformedShape(selectionStartShape));
        } else {
            ed.doc.setSelection(new Rectangle(selectionStart.x + dx, selectionStart.y + dy,
                    selectionStart.width, selectionStart.height));
        }
    }

    private void restoreSelection(Editor ed) {
        if (selectionStart == null) {
            ed.doc.clearSelection();
        } else if (selectionStartShape != null) {
            ed.doc.setSelectionShape(selectionStartShape);
        } else {
            ed.doc.setSelection(selectionStart);
        }
    }

    @Override
    public void release(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!active) return;
        active = false;
        if (!moved) {
            ed.doc.activeLayer().image = original;
            restoreSelection(ed);
            ed.touch();
            return;
        }
        compose(ed);
        ed.commit(I18n.t("Перемещение"));
    }

    @Override
    public java.awt.Cursor cursor() {
        return java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR);
    }
}
