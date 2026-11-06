package photopaint.ui;

import photopaint.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

/** The 28-colour palette. */
public class PalettePanel extends JComponent {

    public static final Color[] COLORS = {
            new Color(0x000000), new Color(0x808080), new Color(0x800000), new Color(0x808000),
            new Color(0x008000), new Color(0x008080), new Color(0x000080), new Color(0x800080),
            new Color(0x808040), new Color(0x004040), new Color(0x0080FF), new Color(0x004080),
            new Color(0x8000FF), new Color(0x804000),
            new Color(0xFFFFFF), new Color(0xC0C0C0), new Color(0xFF0000), new Color(0xFFFF00),
            new Color(0x00FF00), new Color(0x00FFFF), new Color(0x0000FF), new Color(0xFF00FF),
            new Color(0xFFFF80), new Color(0x00FF80), new Color(0x80FFFF), new Color(0x8080FF),
            new Color(0xFF0080), new Color(0xFF8040)
    };

    private final int cell;
    private final int cols;
    private final BiConsumer<Color, Boolean> pick;
    private int hover = -1;

    public PalettePanel(int cell, int cols, BiConsumer<Color, Boolean> pick) {
        this.cell = cell;
        this.cols = cols;
        this.pick = pick;
        setOpaque(false);
        setPreferredSize(new Dimension(cols * (cell + 1) + 1,
                (COLORS.length / cols) * (cell + 1) + 1));
        setToolTipText("");
        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int i = indexAt(e.getX(), e.getY());
                if (i >= 0 && pick != null) {
                    pick.accept(COLORS[i], e.getButton() == MouseEvent.BUTTON3 || e.isAltDown());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int i = indexAt(e.getX(), e.getY());
                if (i != hover) {
                    hover = i;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = -1;
                repaint();
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    private int indexAt(int x, int y) {
        int cw = cell + 1;
        int col = x / cw;
        int row = y / cw;
        if (col < 0 || col >= cols) return -1;
        int i = row * cols + col;
        if (i < 0 || i >= COLORS.length) return -1;
        int ox = col * cw;
        int oy = row * cw;
        if (x - ox > cell || y - oy > cell) return -1;
        return i;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int i = indexAt(e.getX(), e.getY());
        if (i < 0) return null;
        Color c = COLORS[i];
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        for (int i = 0; i < COLORS.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = col * (cell + 1);
            int y = row * (cell + 1);
            g.setColor(i == hover ? Color.WHITE : Theme.BORDER_DARK);
            g.fillRect(x, y, cell + 1, cell + 1);
            g.setColor(COLORS[i]);
            g.fillRect(x + 1, y + 1, cell - 1, cell - 1);
        }
        g.dispose();
    }
}
