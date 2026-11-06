package photopaint.ui;

import photopaint.Theme;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Luna title bar with working minimise / maximise / close buttons. */
public class XpTitleBar extends JComponent {

    private final Window window;
    private final boolean fullControls;
    private String text;
    private final Rectangle minRect = new Rectangle();
    private final Rectangle maxRect = new Rectangle();
    private final Rectangle closeRect = new Rectangle();
    private int hover = -1;
    private int pressed = -1;
    private Point dragStart;
    private Point windowStart;

    public XpTitleBar(Window window, boolean fullControls) {
        this.window = window;
        this.fullControls = fullControls;
        setOpaque(false);
        setPreferredSize(new Dimension(100, 27));
        setMinimumSize(new Dimension(60, 27));
        setCursor(Cursor.getDefaultCursor());
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int hit = buttonAt(e.getPoint());
                if (hit >= 0) {
                    pressed = hit;
                    repaint();
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = e.getLocationOnScreen();
                    windowStart = window.getLocation();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressed >= 0) return;
                if (dragStart != null && SwingUtilities.isLeftMouseButton(e)) {
                    Point now = e.getLocationOnScreen();
                    window.setLocation(windowStart.x + now.x - dragStart.x,
                            windowStart.y + now.y - dragStart.y);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pressed >= 0) {
                    int hit = buttonAt(e.getPoint());
                    int was = pressed;
                    pressed = -1;
                    repaint();
                    if (hit == was) {
                        switch (was) {
                            case 0 -> {
                                if (window instanceof XpDialog d) d.dispose();
                                else if (window instanceof XpFrame f) {
                                    f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
                                } else {
                                    window.setVisible(false);
                                }
                            }
                            case 1 -> {
                                if (window instanceof XpFrame f) {
                                    f.setExtendedState(f.getExtendedState() | Frame.ICONIFIED);
                                }
                            }
                            case 2 -> {
                                if (window instanceof XpFrame f) f.setMaximized(true);
                            }
                            case 3 -> {
                                if (window instanceof XpFrame f) f.setMaximized(!f.isMaximized());
                            }
                            default -> {
                            }
                        }
                    }
                }
                dragStart = null;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int hit = buttonAt(e.getPoint());
                if (hover != hit) {
                    hover = hit;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hover != -1) {
                    hover = -1;
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && buttonAt(e.getPoint()) < 0
                        && fullControls && window instanceof XpFrame f && e.getButton() == 1) {
                    f.setMaximized(!f.isMaximized());
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        window.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                repaint();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                repaint();
            }
        });
    }

    public void setTitleText(String text) {
        this.text = text;
        repaint();
    }

    private int buttonAt(Point p) {
        if (fullControls) {
            if (closeRect.contains(p)) return 0;
            if (window instanceof XpFrame f) {
                if (f.isMaximized()) {
                    if (maxRect.contains(p)) return 3;
                } else if (maxRect.contains(p)) {
                    return 2;
                }
            }
            if (minRect.contains(p)) return 1;
        } else if (closeRect.contains(p)) {
            return 0;
        }
        return -1;
    }

    private void layoutButtons() {
        int w = getWidth();
        closeRect.setBounds(w - 25, 4, 21, 19);
        if (fullControls) {
            maxRect.setBounds(w - 48, 4, 21, 19);
            minRect.setBounds(w - 71, 4, 21, 19);
        } else {
            maxRect.setBounds(0, 0, 0, 0);
            minRect.setBounds(0, 0, 0, 0);
        }
    }

    @Override
    public void doLayout() {
        layoutButtons();
    }

    @Override
    protected void paintComponent(Graphics gg) {
        layoutButtons();
        Graphics2D g = (Graphics2D) gg.create();
        Theme.aa(g);
        boolean active = window.isActive();
        Color top = active ? Theme.TITLE_TOP : Theme.TITLE_INACTIVE_TOP;
        Color bottom = active ? Theme.TITLE_BOTTOM : Theme.TITLE_INACTIVE_BOTTOM;
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 55));
        g.drawLine(1, 1, w - 2, 1);
        g.setColor(new Color(0, 40, 130, 60));
        g.drawLine(0, h - 1, w, h - 1);

        Icons.appIcon(16).paintIcon(this, g, 6, 5);

        g.setFont(Theme.title());
        FontMetrics fm = g.getFontMetrics();
        String t = text == null ? "" : text;
        int maxText = w - 84 - (fullControls ? 3 * 0 : 0);
        if (fullControls) maxText = w - 84;
        else maxText = w - 34;
        while (t.length() > 3 && fm.stringWidth(t) > maxText) {
            t = t.substring(0, t.length() - 2) + "…";
        }
        g.setColor(active ? Color.WHITE : new Color(0xE8EEF8));
        g.drawString(t, 27, (h + fm.getAscent() - fm.getDescent()) / 2);

        if (fullControls) {
            boolean maxed = window instanceof XpFrame f && f.isMaximized();
            paintControl(g, minRect, 1, false);
            paintControl(g, maxRect, maxed ? 3 : 2, false);
        }
        paintControl(g, closeRect, 0, true);
        g.dispose();
    }

    private void paintControl(Graphics2D g, Rectangle r, int kind, boolean close) {
        boolean hot = hover == kind && pressed < 0;
        boolean down = pressed == kind;
        Color top;
        Color bottom;
        Color border;
        if (close) {
            top = hot ? Theme.CLOSE_HOVER_TOP : Theme.CLOSE_TOP;
            bottom = hot ? Theme.CLOSE_HOVER_BOTTOM : Theme.CLOSE_BOTTOM;
            border = new Color(0x8C2A18);
        } else {
            top = down ? new Color(0x2D6BCD) : hot ? new Color(0x8FBDF7) : new Color(0x6DA4F0);
            bottom = down ? new Color(0x1748A8) : hot ? new Color(0x3A7BE6) : new Color(0x1E5CC8);
            border = new Color(0x0C3D96);
        }
        if (down) {
            Color t = top;
            top = bottom;
            bottom = t;
        }
        g.setColor(new Color(255, 255, 255, 90));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 4, 4);
        g.setPaint(new GradientPaint(0, r.y, top, 0, r.y + r.height, bottom));
        g.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 3, 3);
        g.setColor(border);
        g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 4, 4);
        g.setColor(new Color(255, 255, 255, 150));
        g.drawLine(r.x + 2, r.y + 2, r.x + r.width - 3, r.y + 2);

        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case 0 -> {
                g.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                g.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
            }
            case 1 -> g.drawLine(cx - 4, cy + 3, cx + 4, cy + 3);
            case 2 -> {
                g.drawRect(cx - 5, cy - 4, 9, 8);
                g.drawLine(cx - 5, cy - 4, cx - 5, cy + 3);
            }
            default -> {
                g.drawRect(cx - 5, cy - 5, 8, 7);
                g.drawRect(cx - 2, cy - 2, 8, 7);
            }
        }
    }
}
