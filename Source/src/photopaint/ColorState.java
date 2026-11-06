package photopaint;

import java.awt.Color;

/** Foreground / background color pair shared by the tools and panels. */
public class ColorState {

    private Color foreground = Color.BLACK;
    private Color background = Color.WHITE;

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color c) {
        if (c != null) {
            foreground = new Color(c.getRed(), c.getGreen(), c.getBlue());
        }
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color c) {
        if (c != null) {
            background = new Color(c.getRed(), c.getGreen(), c.getBlue());
        }
    }

    public void swap() {
        Color t = foreground;
        foreground = background;
        background = t;
    }

    public void reset() {
        foreground = Color.BLACK;
        background = Color.WHITE;
    }
}
