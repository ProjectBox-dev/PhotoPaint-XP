package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Bottom status bar with coordinate, zoom and selection readouts. */
public class StatusBar extends JPanel {

    private final Cell hint = new Cell("");
    private final Cell coords = new Cell("X: —  Y: —");
    private final Cell zoom = new Cell("100%");
    private final Cell size = new Cell("");
    private final Cell selection = new Cell("");

    public StatusBar() {
        setOpaque(false);
        setLayout(new BorderLayout(4, 0));
        setPreferredSize(new Dimension(100, 25));
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        add(hint, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        right.setOpaque(false);
        coords.setPreferredSize(new Dimension(132, 20));
        zoom.setPreferredSize(new Dimension(58, 20));
        size.setPreferredSize(new Dimension(106, 20));
        selection.setPreferredSize(new Dimension(172, 20));
        right.add(coords);
        right.add(zoom);
        right.add(size);
        right.add(selection);
        add(right, BorderLayout.EAST);

        JComponent grip = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(13, 18);
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0x8A8678));
                for (int y = 4; y < getHeight(); y += 3) {
                    for (int x = getWidth() - (getHeight() - y); x < getWidth(); x += 3) {
                        g.drawLine(x, y, x, y);
                    }
                }
            }
        };
        add(grip, BorderLayout.WEST);
    }

    public void setHint(String text) {
        hint.setText(text);
    }

    public void setCursor(Integer x, Integer y) {
        coords.setText(x == null ? "X: —  Y: —" : "X: " + x + "  Y: " + y);
    }

    public void setZoom(int percent) {
        zoom.setText(percent + "%");
    }

    public void setDocSize(int w, int h) {
        size.setText(w + " × " + h + " px");
    }

    public void setSelection(Rectangle r) {
        selection.setText(r == null ? "" : I18n.t("Выделение: ") + r.width + " × " + r.height);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        Theme.vertical(g, 0, 0, getWidth(), getHeight(), new Color(0xF3F1E6), new Color(0xE4E0D2));
        g.setColor(Color.WHITE);
        g.drawLine(0, 0, getWidth(), 0);
        g.setColor(new Color(0xC0BCAD));
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g.dispose();
    }

    private static class Cell extends JComponent {
        private String text;

        Cell(String text) {
            this.text = text;
        }

        void setText(String t) {
            text = t;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg.create();
            g.setColor(new Color(0xF8F7F0));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(0xC8C4B4));
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(new Color(0xFFFFFF));
            g.drawLine(1, 1, getWidth() - 2, 1);
            g.setFont(Theme.uiSmall());
            g.setColor(new Color(0x3A3A3A));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, 4, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }
}
