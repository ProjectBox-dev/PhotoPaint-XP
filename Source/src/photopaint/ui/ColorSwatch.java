package photopaint.ui;

import photopaint.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Small colour rectangle with a sunken border and optional checkerboard. */
public class ColorSwatch extends JComponent {

    private Color color = Color.BLACK;
    private boolean showAlpha;

    public ColorSwatch(int width, int height) {
        setPreferredSize(new java.awt.Dimension(width, height));
        setMinimumSize(new java.awt.Dimension(width, height));
        setOpaque(false);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
        repaint();
    }

    public void setShowAlpha(boolean showAlpha) {
        this.showAlpha = showAlpha;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        int w = getWidth();
        int h = getHeight();
        if (showAlpha) {
            for (int y = 0; y < h; y += 6) {
                for (int x = 0; x < w; x += 6) {
                    g.setColor(((x / 6 + y / 6) & 1) == 0 ? Color.WHITE : new Color(0xCCCCCC));
                    g.fillRect(x, y, 6, 6);
                }
            }
        }
        if (color != null) {
            g.setColor(color);
            g.fillRect(0, 0, w, h);
        }
        g.setColor(Theme.BORDER_DARK);
        g.drawRect(0, 0, w - 1, h - 1);
        g.setColor(Color.WHITE);
        g.drawLine(1, 1, w - 2, 1);
        g.dispose();
    }
}
