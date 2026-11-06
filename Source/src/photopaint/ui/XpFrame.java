package photopaint.ui;

import photopaint.Theme;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Undecorated frame with the XP window frame, custom title bar and edge resizing. */
public class XpFrame extends JFrame {

    private static final int BORDER = 4;

    private final JPanel root;
    private final JPanel content;
    private final XpTitleBar titleBar;
    private Rectangle restoreBounds;
    private boolean maximized;
    private int resizeDir;
    private Point pressScreen;
    private Rectangle pressBounds;

    public XpFrame(String title) {
        super(title);
        setUndecorated(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(780, 560));

        root = new JPanel(new BorderLayout());
        root.setBackground(Theme.FRAME_EDGE);

        titleBar = new XpTitleBar(this, true);
        titleBar.setTitleText(title);

        content = new JPanel(new BorderLayout());
        content.setBackground(Theme.WINDOW);

        root.add(titleBar, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        applyBorder(false);
        installResize();
    }

    private void applyBorder(boolean maximized) {
        if (maximized) {
            root.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        } else {
            root.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x0046AD)),
                    BorderFactory.createEmptyBorder(BORDER - 1, BORDER - 1, BORDER - 1, BORDER - 1)));
        }
        content.setBorder(BorderFactory.createLineBorder(new Color(0x9DB5D8)));
    }

    public JPanel content() {
        return content;
    }

    public XpTitleBar titleBar() {
        return titleBar;
    }

    public boolean isMaximized() {
        return maximized;
    }

    @Override
    public void setTitle(String t) {
        super.setTitle(t);
        if (titleBar != null) titleBar.setTitleText(t);
    }

    public void setMaximized(boolean max) {
        if (max == maximized) return;
        if (max) {
            restoreBounds = getBounds();
            GraphicsConfiguration gc = getGraphicsConfiguration();
            Rectangle screen = gc.getBounds();
            Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            setBounds(screen.x + ins.left, screen.y + ins.top,
                    screen.width - ins.left - ins.right,
                    screen.height - ins.top - ins.bottom);
            maximized = true;
        } else {
            setBounds(restoreBounds != null ? restoreBounds : new Rectangle(100, 60, 1100, 760));
            maximized = false;
        }
        applyBorder(maximized);
        titleBar.repaint();
        revalidate();
        repaint();
    }

    private int dirAt(Point p) {
        int d = 0;
        int w = root.getWidth();
        int h = root.getHeight();
        if (p.x <= BORDER) d |= 4;
        if (p.x >= w - BORDER) d |= 8;
        if (p.y <= BORDER) d |= 1;
        if (p.y >= h - BORDER) d |= 2;
        return d;
    }

    private Cursor cursorFor(int dir) {
        return switch (dir) {
            case 1, 2 -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            case 4, 8 -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            case 1 | 4, 2 | 8 -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            case 1 | 8, 2 | 4 -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            default -> Cursor.getDefaultCursor();
        };
    }

    private void installResize() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !maximized) {
                    resizeDir = dirAt(e.getPoint());
                    if (resizeDir != 0) {
                        pressScreen = e.getLocationOnScreen();
                        pressBounds = getBounds();
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizeDir == 0 || pressScreen == null || pressBounds == null) return;
                Point now = e.getLocationOnScreen();
                int dx = now.x - pressScreen.x;
                int dy = now.y - pressScreen.y;
                int nx = pressBounds.x;
                int ny = pressBounds.y;
                int nw = pressBounds.width;
                int nh = pressBounds.height;
                if ((resizeDir & 4) != 0) {
                    int nl = pressBounds.x + dx;
                    if (nl <= pressBounds.x + pressBounds.width - getMinimumSize().width) {
                        nx = nl;
                        nw = pressBounds.width - dx;
                    }
                }
                if ((resizeDir & 8) != 0) {
                    nw = Math.max(getMinimumSize().width, pressBounds.width + dx);
                }
                if ((resizeDir & 1) != 0) {
                    int nt = pressBounds.y + dy;
                    if (nt <= pressBounds.y + pressBounds.height - getMinimumSize().height) {
                        ny = nt;
                        nh = pressBounds.height - dy;
                    }
                }
                if ((resizeDir & 2) != 0) {
                    nh = Math.max(getMinimumSize().height, pressBounds.height + dy);
                }
                setBounds(nx, ny, nw, nh);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resizeDir = 0;
                pressScreen = null;
                pressBounds = null;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!maximized) {
                    root.setCursor(cursorFor(dirAt(e.getPoint())));
                } else {
                    root.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        root.addMouseListener(mouse);
        root.addMouseMotionListener(mouse);
    }
}
