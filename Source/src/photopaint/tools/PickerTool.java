package photopaint.tools;

import photopaint.Editor;
import photopaint.ui.CanvasPanel;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PickerTool extends Tool {

    public PickerTool() {
        super("picker", "Пипетка", "I", "Берёт цвет с изображения. Alt или правая кнопка — фоновый цвет.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (!BrushTool.inside(ed.doc, e.x, e.y)) return;
        BufferedImage img = ed.doc.composite();
        int p = img.getRGB(e.ix(), e.iy());
        int a = p >>> 24;
        int r = (p >> 16) & 255;
        int g = (p >> 8) & 255;
        int b = p & 255;
        if (a < 255) {
            r = (r * a + 255 * (255 - a)) / 255;
            g = (g * a + 255 * (255 - a)) / 255;
            b = (b * a + 255 * (255 - a)) / 255;
        }
        Color c = new Color(r, g, b);
        if (e.alt || e.button == 3) ed.setBackground(c);
        else ed.setForeground(c);
    }
}
