package photopaint;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

/**
 * Colors, fonts and small painting helpers for the Windows XP (Luna) look.
 */
public final class Theme {

    private Theme() {
    }

    // ---- Luna palette -----------------------------------------------------
    public static final Color WINDOW = new Color(0xECE9D8);
    public static final Color WINDOW_LIGHT = new Color(0xF7F5EC);
    public static final Color WINDOW_DARK = new Color(0xD6D2C2);
    public static final Color FIELD = Color.WHITE;

    public static final Color BORDER = new Color(0xACA899);
    public static final Color BORDER_DARK = new Color(0x8A8678);
    public static final Color BORDER_BLUE = new Color(0x7F9DB9);
    public static final Color BORDER_LIGHT = Color.WHITE;

    public static final Color TEXT = new Color(0x000000);
    public static final Color TEXT_DISABLED = new Color(0xA0A0A0);

    public static final Color SELECTION = new Color(0x316AC5);
    public static final Color SELECTION_TEXT = Color.WHITE;
    public static final Color POPUP = new Color(0xF7F6EF);

    public static final Color TOOL_HOVER = new Color(0xFFF2C8);
    public static final Color TOOL_PRESSED = new Color(0xC1D2EE);
    public static final Color BUTTON_TOP = new Color(0xFDFDFB);
    public static final Color BUTTON_BOTTOM = new Color(0xE3DFD0);

    public static final Color TITLE_TOP = new Color(0x0058EE);
    public static final Color TITLE_BOTTOM = new Color(0x3D8BFF);
    public static final Color TITLE_INACTIVE_TOP = new Color(0x7BA2E7);
    public static final Color TITLE_INACTIVE_BOTTOM = new Color(0xA9C3F0);
    public static final Color FRAME_EDGE = new Color(0x0A4BC4);

    public static final Color WORKSPACE = new Color(0x808080);
    public static final Color GRID = new Color(0x9A9A9A);

    public static final Color CLOSE_TOP = new Color(0xF7A58F);
    public static final Color CLOSE_BOTTOM = new Color(0xD8442A);
    public static final Color CLOSE_HOVER_TOP = new Color(0xFFC6B2);
    public static final Color CLOSE_HOVER_BOTTOM = new Color(0xEC5B3C);

    // ---- Fonts ------------------------------------------------------------
    private static String family;
    private static final Map<String, Font> FONTS = new HashMap<>();

    public static String fontFamily() {
        if (family == null) {
            family = "Dialog";
            String[] wanted = {"Tahoma", "Segoe UI", "Verdana"};
            String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();
            outer:
            for (String w : wanted) {
                for (String a : available) {
                    if (a.equalsIgnoreCase(w)) {
                        family = a;
                        break outer;
                    }
                }
            }
        }
        return family;
    }

    public static Font font(int size, int style) {
        String key = size + "/" + style;
        Font f = FONTS.get(key);
        if (f == null) {
            f = new Font(fontFamily(), style, size);
            FONTS.put(key, f);
        }
        return f;
    }

    public static Font ui() {
        return font(11, Font.PLAIN);
    }

    public static Font uiBold() {
        return font(11, Font.BOLD);
    }

    public static Font uiSmall() {
        return font(10, Font.PLAIN);
    }

    public static Font title() {
        return font(12, Font.BOLD);
    }

    // ---- Borders ----------------------------------------------------------
    public static Border sunkenBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE),
                BorderFactory.createEmptyBorder(1, 2, 1, 2));
    }

    public static Border sunkenBox() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x808080)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE),
                        BorderFactory.createEmptyBorder(1, 1, 1, 1)));
    }

    public static Border popupBorder() {
        return BorderFactory.createLineBorder(BORDER_DARK);
    }

    // ---- Painting helpers -------------------------------------------------
    public static void vertical(Graphics2D g, int x, int y, int w, int h, Color top, Color bottom) {
        if (w <= 0 || h <= 0) return;
        g.setPaint(new GradientPaint(x, y, top, x, y + h, bottom));
        g.fillRect(x, y, w, h);
    }

    public static void horizontal(Graphics2D g, int x, int y, int w, int h, Color left, Color right) {
        if (w <= 0 || h <= 0) return;
        g.setPaint(new GradientPaint(x, y, left, x + w, y, right));
        g.fillRect(x, y, w, h);
    }

    public static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Raised XP-ish beige surface used for bars. */
    public static void raisedBar(Graphics2D g, int x, int y, int w, int h) {
        vertical(g, x, y, w, h, WINDOW_LIGHT, new Color(0xE9E6D8));
        g.setColor(new Color(0xFFFFFF));
        g.drawLine(x, y, x + w - 1, y);
        g.setColor(BORDER);
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    }
}
