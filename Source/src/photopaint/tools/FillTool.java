package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.image.BufferedImage;

public class FillTool extends Tool {

    public FillTool() {
        super("fill", "Заливка", "F", "Заливает область основным цветом. Допуск настраивается сверху.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1) return;
        if (!BrushTool.inside(ed.doc, e.x, e.y)) return;
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        boolean changed = ToolPaint.floodFill(img, e.ix(), e.iy(), ed.colors.getForeground(),
                ed.tolerance, ed.doc.clipShape());
        if (changed) ed.commit(I18n.t("Заливка"));
    }
}
