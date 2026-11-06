package photopaint.tools;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.ui.CanvasPanel;
import photopaint.ui.dialogs.TextDialog;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class TextTool extends Tool {

    public TextTool() {
        super("text", "Текст", "T", "Добавляет текст на активный слой. Шрифт и размер задаются в окне.");
    }

    @Override
    public void press(Editor ed, CanvasPanel canvas, ToolEvent e) {
        if (e.button != 1 || !BrushTool.inside(ed.doc, e.x, e.y)) return;
        TextDialog dialog = new TextDialog(javax.swing.SwingUtilities.getWindowAncestor(canvas), ed);
        if (!dialog.showDialog()) return;
        String text = dialog.getText();
        if (text == null || text.isEmpty()) return;

        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        int style = Font.PLAIN;
        if (ed.bold) style |= Font.BOLD;
        if (ed.italic) style |= Font.ITALIC;
        Font font = new Font(ed.fontFamily, style, ed.fontSize);
        Graphics2D g = ToolPaint.prepare(img, ed.doc.clipShape());
        g.setFont(font);
        g.setColor(ed.colors.getForeground());
        FontMetrics fm = g.getFontMetrics();
        String[] lines = text.split("\n", -1);
        int lineHeight = fm.getHeight();
        int y = (int) Math.round(e.y) + fm.getAscent();
        for (String line : lines) {
            g.drawString(line, (float) e.x, y);
            y += lineHeight;
        }
        g.dispose();
        ed.commit(I18n.t("Текст"));
        canvas.repaint();
    }

    @Override
    public Cursor cursor() {
        return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    }
}
