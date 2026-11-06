package photopaint.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

/** All icons are drawn with Java2D, so the program ships without image files. */
public final class Icons {

    private Icons() {
    }

    private static final Map<String, Icon> CACHE = new HashMap<>();

    public static Icon icon(String name, int size) {
        String key = name + "@" + size;
        Icon ic = CACHE.get(key);
        if (ic == null) {
            ic = new VectorIcon(name, size);
            CACHE.put(key, ic);
        }
        return ic;
    }

    public static Icon tool(String id) {
        return icon(id, 22);
    }

    public static Icon small(String name) {
        return icon(name, 16);
    }

    public static Icon appIcon(int size) {
        return icon("app", size);
    }

    /** Checkbox / radio icon that asks the component about its current state. */
    public static Icon stateIcon(String kind) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 13;
            }

            @Override
            public int getIconHeight() {
                return 13;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean selected = c instanceof javax.swing.AbstractButton b && b.isSelected();
                boolean enabled = c == null || c.isEnabled();
                if ("radio".equals(kind)) {
                    g2.setColor(enabled ? Color.WHITE : new Color(0xF0EEE2));
                    g2.fillOval(0, 0, 12, 12);
                    g2.setColor(enabled ? new Color(0x7F9DB9) : new Color(0xB8B4A6));
                    g2.drawOval(0, 0, 12, 12);
                    if (selected) {
                        g2.setColor(enabled ? new Color(0x2A5FA8) : new Color(0x9A9A9A));
                        g2.fillOval(3, 3, 7, 7);
                    }
                } else {
                    g2.setColor(enabled ? Color.WHITE : new Color(0xF0EEE2));
                    g2.fillRect(0, 0, 12, 12);
                    g2.setColor(enabled ? new Color(0x7F9DB9) : new Color(0xB8B4A6));
                    g2.drawRect(0, 0, 11, 11);
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.drawLine(1, 1, 10, 1);
                    if (selected) {
                        g2.setColor(enabled ? new Color(0x1A4A9C) : new Color(0x9A9A9A));
                        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND));
                        g2.drawLine(3, 6, 5, 9);
                        g2.drawLine(5, 9, 10, 3);
                    }
                }
                g2.dispose();
            }
        };
    }

    private static class VectorIcon implements Icon {
        private final String name;
        private final int size;

        VectorIcon(String name, int size) {
            this.name = name;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            try {
                paint(g2, name);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final Color DARK = new Color(0x3A3A3A);
    private static final Color YELLOW = new Color(0xFFD75E);
    private static final Color YELLOW_DARK = new Color(0xE0AC22);
    private static final Color BLUE = new Color(0x3F7FD8);
    private static final Color BLUE_DARK = new Color(0x1E4E9C);
    private static final Color RED = new Color(0xE0604A);
    private static final Color GREEN = new Color(0x4C9E45);
    private static final Color GRAY = new Color(0xB8B8B8);
    private static final Color PINK = new Color(0xF3A8B8);
    private static final Color BROWN = new Color(0x8A5A2B);
    private static final Color WHITE = new Color(0xFFFFFF);

    /** Fills a shape and draws its outline in one go. */
    private static void ink(Graphics2D g, Shape s, Paint fill, Color line, float width) {
        g.setPaint(fill);
        g.fill(s);
        g.setColor(line);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    private static void paint(Graphics2D g, String name) {
        switch (name) {
            case "select" -> select(g);
            case "lasso" -> lasso(g);
            case "move" -> move(g);
            case "pencil" -> pencil(g);
            case "brush" -> brush(g);
            case "eraser" -> eraser(g);
            case "fill" -> fill(g);
            case "gradient" -> gradient(g);
            case "line" -> line(g);
            case "rect" -> rectangle(g);
            case "ellipse" -> ellipse(g);
            case "text" -> text(g);
            case "picker" -> picker(g);
            case "zoom" -> magnifier(g, true);
            case "zoomIn" -> magnifier(g, true);
            case "zoomOut" -> magnifier(g, false);
            case "hand" -> hand(g);
            case "new" -> newDoc(g);
            case "open" -> folder(g, true);
            case "save" -> save(g);
            case "undo" -> arrow(g, false);
            case "redo" -> arrow(g, true);
            case "layerAdd" -> layerAdd(g);
            case "layerDuplicate" -> layerDuplicate(g);
            case "layerDelete" -> trash(g);
            case "up" -> triangle(g, true);
            case "down" -> triangle(g, false);
            case "eye" -> eye(g, true);
            case "eyeOff" -> eye(g, false);
            case "swap" -> swap(g);
            case "resetColors" -> resetColors(g);
            case "grid" -> grid(g);
            case "fit" -> fit(g);
            case "rotate" -> rotate(g);
            case "folder" -> folder(g, false);
            case "file" -> file(g);
            case "drive" -> drive(g);
            case "upFolder" -> upFolder(g);
            case "home" -> home(g);
            case "info" -> info(g);
            case "app" -> app(g);
            case "check" -> check(g);
            case "radioDot" -> {
                g.setColor(BLUE_DARK);
                g.fillOval(4, 4, 16, 16);
                g.setColor(new Color(0xA9C2E8));
                g.fillOval(8, 8, 8, 8);
            }
            default -> dot(g);
        }
    }

    // ---- tools ------------------------------------------------------------
    private static void select(Graphics2D g) {
        // Translucent "marching ants" marquee with corner handles.
        g.setColor(new Color(74, 144, 226, 34));
        g.fillRoundRect(3, 4, 18, 16, 2, 2);

        g.setStroke(new BasicStroke(2f));
        g.setColor(WHITE);
        g.drawRoundRect(3, 4, 18, 16, 2, 2);

        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[]{2.2f, 2.2f}, 0f));
        g.setColor(new Color(0x1E1E1E));
        g.drawRoundRect(3, 4, 18, 16, 2, 2);

        int[] xs = {1, 19, 1, 19};
        int[] ys = {2, 2, 18, 18};
        for (int i = 0; i < 4; i++) {
            g.setColor(WHITE);
            g.fillRect(xs[i], ys[i], 4, 4);
            g.setColor(new Color(0x2A5FA8));
            g.setStroke(new BasicStroke(1f));
            g.drawRect(xs[i], ys[i], 3, 3);
        }
    }

    private static void lasso(Graphics2D g) {
        // Free-form loop with a small tail, drawn as a dashed marquee.
        Path2D.Double loop = new Path2D.Double();
        loop.moveTo(4, 14);
        loop.curveTo(3, 7, 8, 3, 13, 4);
        loop.curveTo(18, 2, 21, 7, 19, 12);
        loop.curveTo(18, 17, 12, 20, 8, 18);
        loop.lineTo(5, 22);
        loop.moveTo(8, 18);
        loop.curveTo(6, 17, 4, 16, 4, 14);

        g.setColor(new Color(74, 144, 226, 34));
        g.fill(loop);

        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(WHITE);
        g.draw(loop);

        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                1f, new float[]{2.4f, 2.4f}, 0f));
        g.setColor(new Color(0x1E1E1E));
        g.draw(loop);
    }

    private static void move(Graphics2D g) {
        // Solid four-way arrow silhouette with a white halo, like the Windows cursor.
        Area arrow = new Area(new java.awt.geom.Rectangle2D.Float(11, 4, 2, 16));
        arrow.add(new Area(new java.awt.geom.Rectangle2D.Float(4, 11, 16, 2)));
        arrow.add(new Area(new Polygon(new int[]{12, 9, 15}, new int[]{2, 7, 7}, 3)));
        arrow.add(new Area(new Polygon(new int[]{12, 9, 15}, new int[]{22, 17, 17}, 3)));
        arrow.add(new Area(new Polygon(new int[]{2, 7, 7}, new int[]{12, 9, 15}, 3)));
        arrow.add(new Area(new Polygon(new int[]{22, 17, 17}, new int[]{12, 9, 15}, 3)));

        g.setColor(new Color(255, 255, 255, 240));
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arrow);
        g.setColor(new Color(0x2C2C2C));
        g.fill(arrow);
        g.setColor(new Color(0x101010));
        g.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arrow);
    }

    private static void pencil(Graphics2D g) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(12, 12);
        gg.rotate(-0.62);

        // Sharpened wood and graphite tip.
        Shape wood = new Polygon(new int[]{-4, -9, -4}, new int[]{-3, 0, 3}, 3);
        ink(gg, wood, new GradientPaint(0, -3, new Color(0xF8E1B4), 0, 3, new Color(0xD8AB67)),
                new Color(0x9A7440), 0.8f);
        Path2D lead = new Path2D.Double();
        lead.moveTo(-6, -1.7);
        lead.lineTo(-10, 0);
        lead.lineTo(-6, 1.7);
        lead.closePath();
        ink(gg, lead, new Color(0x3A3A3A), new Color(0x1A1A1A), 0.6f);

        // Yellow lacquered body.
        Shape body = new RoundRectangle2D.Float(-4, -3, 12, 6, 2, 2);
        ink(gg, body, new GradientPaint(0, -3, new Color(0xFFE982), 0, 3, new Color(0xE2A116)),
                new Color(0x9C7414), 0.8f);
        gg.setColor(new Color(255, 255, 255, 150));
        gg.setStroke(new BasicStroke(0.9f));
        gg.drawLine(-3, -2, 7, -2);

        // Metal ferrule.
        Shape ferrule = new RoundRectangle2D.Float(8, -3, 3.4f, 6, 1.4f, 1.4f);
        ink(gg, ferrule, new GradientPaint(0, -3, new Color(0xF4F4F4), 0, 3, new Color(0x9EA6AE)),
                new Color(0x5F666D), 0.7f);
        gg.setColor(new Color(0x5F666D));
        gg.drawLine(9, -3, 9, 3);

        // Pink eraser.
        Shape eraser = new RoundRectangle2D.Float(11.2f, -3, 4.4f, 6, 2, 2);
        ink(gg, eraser, new GradientPaint(0, -3, new Color(0xFFC6D2), 0, 3, new Color(0xE7758F)),
                new Color(0xA84D63), 0.8f);
        gg.setColor(new Color(255, 255, 255, 130));
        gg.drawLine(12, -2, 14, -2);
        gg.dispose();
    }

    private static void brush(Graphics2D g) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(12, 12);
        gg.rotate(-0.75);

        // Wooden handle.
        Path2D handle = new Path2D.Double();
        handle.moveTo(2, -2);
        handle.lineTo(12, -1.4);
        handle.lineTo(12, 1.4);
        handle.lineTo(3, 2);
        handle.closePath();
        ink(gg, handle, new GradientPaint(0, -2, new Color(0xD99A58), 0, 2, new Color(0x7A4A20)),
                new Color(0x5C3614), 0.8f);
        gg.setColor(new Color(255, 235, 200, 120));
        gg.setStroke(new BasicStroke(0.8f));
        gg.draw(new Line2D.Float(3, -1, 11, -0.6f));

        // Silver ferrule.
        Shape ferrule = new RoundRectangle2D.Float(-1, -2.1f, 3.6f, 4.2f, 1.2f, 1.2f);
        ink(gg, ferrule, new GradientPaint(0, -2, new Color(0xF6F6F6), 0, 2, new Color(0x9AA2AA)),
                new Color(0x5F666D), 0.7f);
        gg.setColor(new Color(0x6B7278));
        gg.draw(new Line2D.Float(0.4f, -2, 0.4f, 2));

        // Bristles with blue paint on the tip.
        Path2D bristles = new Path2D.Double();
        bristles.moveTo(-1, -2.2);
        bristles.lineTo(-7, -1.4);
        bristles.lineTo(-8, 0);
        bristles.lineTo(-7, 1.4);
        bristles.lineTo(-1, 2.2);
        bristles.closePath();
        ink(gg, bristles, new GradientPaint(0, -2, new Color(0xE9BC6C), 0, 2, new Color(0xB0792E)),
                new Color(0x7A521E), 0.8f);
        Path2D paint = new Path2D.Double();
        paint.moveTo(-11, 0);
        paint.lineTo(-5, -1.7);
        paint.lineTo(-5, 1.7);
        paint.closePath();
        ink(gg, paint, new GradientPaint(-11, 0, new Color(0x8DC0FF), -5, 0, new Color(0x2A62C4)),
                new Color(0x17408A), 0.7f);

        // A paint droplet.
        Shape drop = new Ellipse2D.Float(4, 6, 3.4f, 4.2f);
        ink(gg, drop, new Color(0x2F6FD0), new Color(0x17408A), 0.7f);
        gg.dispose();
    }

    private static void eraser(Graphics2D g) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(11.5f, 12);
        gg.rotate(-0.65);

        gg.setColor(new Color(0, 0, 0, 38));
        gg.fill(new RoundRectangle2D.Float(-7.5f, -4, 18, 10, 3, 3));

        Shape body = new RoundRectangle2D.Float(-9, -5, 18, 10, 3, 3);
        ink(gg, body, new GradientPaint(0, -5, new Color(0xFFC9D4), 0, 5, new Color(0xE5738E)),
                new Color(0x9E4A60), 0.9f);

        Shape sleeve = new RoundRectangle2D.Float(-1, -5, 10, 10, 3, 3);
        ink(gg, sleeve, new GradientPaint(0, -5, Color.WHITE, 0, 5, new Color(0xD5DCE4)),
                new Color(0x8A93A0), 0.8f);
        gg.setColor(new Color(0x8A93A0));
        gg.drawLine(-1, -4, -1, 4);

        gg.setColor(new Color(255, 255, 255, 170));
        gg.setStroke(new BasicStroke(0.9f));
        gg.drawLine(-7, -3, -3, -3);
        gg.dispose();
    }

    private static void fill(Graphics2D g) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(10.5f, 11.5f);
        gg.rotate(0.32);

        // Handle.
        gg.setColor(new Color(0x6E7B8A));
        gg.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawArc(-6, -11, 12, 16, 15, 150);

        // Bucket.
        Shape body = new Polygon(new int[]{-7, 7, 5, -5}, new int[]{-4, -4, 8, 8}, 4);
        ink(gg, body, new GradientPaint(-7, 0, new Color(0xF4F7FA), 7, 0, new Color(0xA5B2C3)),
                new Color(0x4E5A68), 0.9f);
        Shape rim = new Ellipse2D.Float(-7.6f, -6, 15.2f, 4);
        ink(gg, rim, new GradientPaint(0, -6, new Color(0xDCE5EE), 0, -2, new Color(0x8C9AAB)),
                new Color(0x4E5A68), 0.9f);
        gg.setColor(new Color(0x3A78D8));
        gg.fill(new Ellipse2D.Float(-5.8f, -5.2f, 11.6f, 2.4f));
        gg.setColor(new Color(255, 255, 255, 150));
        gg.setStroke(new BasicStroke(1f));
        gg.draw(new Line2D.Float(-5, -1.6f, -4, 5));

        // Paint pouring out.
        gg.setColor(new Color(0x3A78D8));
        gg.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawArc(3, -3, 9, 12, 260, 80);
        Shape drop = new Ellipse2D.Float(9, 6, 3.6f, 4.6f);
        ink(gg, drop, new Color(0x3A78D8), new Color(0x17408A), 0.7f);
        gg.dispose();
    }

    private static void gradient(Graphics2D g) {
        Shape box = new RoundRectangle2D.Float(3, 5, 18, 14, 2.5f, 2.5f);
        g.setPaint(new GradientPaint(3, 0, new Color(0x1F5FC4), 21, 0, Color.WHITE));
        g.fill(box);
        g.setColor(new Color(0x274B7A));
        g.setStroke(new BasicStroke(1.1f));
        g.draw(box);
        g.setColor(new Color(255, 255, 255, 170));
        g.drawLine(4, 6, 19, 6);
        g.setColor(new Color(0x2A5FA8));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(4, 18, 20, 6);
        g.fillPolygon(new int[]{20, 20, 16}, new int[]{6, 10, 6}, 3);
    }

    private static void line(Graphics2D g) {
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0x274B7A));
        g.drawLine(5, 18, 18, 5);
        g.setColor(new Color(255, 255, 255, 90));
        g.setStroke(new BasicStroke(0.8f));
        g.drawLine(5, 17, 17, 5);

        Shape[] handles = {
                new RoundRectangle2D.Float(2.5f, 15.5f, 4.6f, 4.6f, 1, 1),
                new RoundRectangle2D.Float(16f, 3.5f, 4.6f, 4.6f, 1, 1),
        };
        for (Shape h : handles) {
            g.setColor(WHITE);
            g.fill(h);
            g.setColor(new Color(0x2A5FA8));
            g.setStroke(new BasicStroke(1f));
            g.draw(h);
        }
    }

    private static void rectangle(Graphics2D g) {
        Shape box = new RoundRectangle2D.Float(3.5f, 5.5f, 17, 13, 2.5f, 2.5f);
        g.setPaint(new GradientPaint(0, 5, WHITE, 0, 19, new Color(0xC3D9F5)));
        g.fill(box);
        g.setColor(new Color(0x2A5FA8));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(box);
        g.setColor(new Color(255, 255, 255, 200));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(5, 7, 19, 7);
    }

    private static void ellipse(Graphics2D g) {
        Shape box = new Ellipse2D.Float(3, 5, 18, 14);
        g.setPaint(new GradientPaint(0, 5, WHITE, 0, 19, new Color(0xC3D9F5)));
        g.fill(box);
        g.setColor(new Color(0x2A5FA8));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(box);
        g.setColor(new Color(255, 255, 255, 200));
        g.setStroke(new BasicStroke(1f));
        g.drawArc(5, 7, 13, 8, 30, 120);
    }

    private static void text(Graphics2D g) {
        g.setFont(new java.awt.Font("Georgia", java.awt.Font.BOLD, 21));
        g.setColor(new Color(0xA3ABB6));
        g.drawString("A", 5.5f, 20.5f);
        g.setPaint(new GradientPaint(0, 3, new Color(0x63A0EA), 0, 20, new Color(0x123A82)));
        g.drawString("A", 4, 19);
        g.setColor(new Color(0x2A5FA8));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(4, 21.5f, 20, 21.5f));
        g.setColor(new Color(0x1A4A9C));
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(19.2f, 5.5f, 19.2f, 16.5f));
    }

    private static void picker(Graphics2D g) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(12, 12);
        gg.rotate(-0.72);

        // Metal tube.
        Path2D tube = new Path2D.Double();
        tube.moveTo(-10, 0);
        tube.lineTo(-8, -1.3);
        tube.lineTo(-2, -1.9);
        tube.lineTo(-2, 1.9);
        tube.lineTo(-8, 1.3);
        tube.closePath();
        ink(gg, tube, new GradientPaint(0, -2, new Color(0xF2F2F2), 0, 2, new Color(0x9AA2AA)),
                new Color(0x5F666D), 0.7f);
        Path2D tip = new Path2D.Double();
        tip.moveTo(-11, 0);
        tip.lineTo(-7.5, -1.1);
        tip.lineTo(-7.5, 1.1);
        tip.closePath();
        ink(gg, tip, new Color(0x3A3A3A), new Color(0x1A1A1A), 0.6f);

        // Blue bulb.
        Shape bulb = new RoundRectangle2D.Float(-2.4f, -3.5f, 9.6f, 7, 3.4f, 3.4f);
        ink(gg, bulb, new GradientPaint(0, -3.5f, new Color(0x8CC0FF), 0, 3.5f, new Color(0x1E56B4)),
                new Color(0x143C80), 0.9f);
        gg.setColor(new Color(255, 255, 255, 160));
        gg.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.draw(new Line2D.Float(0, -2.2f, 4.5f, -2.2f));
        gg.setColor(new Color(0x143C80));
        gg.drawLine(-2, -3, -2, 3);
        gg.dispose();
    }

    private static void magnifier(Graphics2D g, boolean plus) {
        // Handle behind the lens.
        g.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0x5E3A1A));
        g.draw(new Line2D.Float(16.2f, 16.2f, 21, 21));
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0xC08A50));
        g.draw(new Line2D.Float(16.8f, 16.8f, 20.2f, 20.2f));

        g.setColor(new Color(0x333333));
        g.fillOval(2, 2, 16, 16);
        g.setPaint(new GradientPaint(4, 4, new Color(0xF2F9FF), 15, 16, new Color(0x74A8E4)));
        g.fillOval(4, 4, 12, 12);
        g.setColor(new Color(0x262626));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(3, 3, 14, 14);
        g.setColor(new Color(255, 255, 255, 190));
        g.setStroke(new BasicStroke(1f));
        g.drawArc(5, 5, 9, 9, 55, 75);

        g.setColor(new Color(0x1E4E9C));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(7, 10, 13, 10);
        if (plus) g.drawLine(10, 7, 10, 13);
    }

    private static void hand(Graphics2D g) {
        Area hand = new Area(new RoundRectangle2D.Float(6.4f, 8.6f, 11.2f, 11.4f, 5, 5));
        float[][] fingers = {
                {7.0f, 2.3f, 4.0f}, {9.7f, 2.3f, 2.6f},
                {12.4f, 2.3f, 3.2f}, {15.1f, 2.2f, 5.0f},
        };
        for (float[] f : fingers) {
            hand.add(new Area(new RoundRectangle2D.Float(f[0], f[2], f[1],
                    14f - f[2], f[1], f[1])));
        }
        Shape thumb = AffineTransform.getRotateInstance(Math.toRadians(-28), 6.6f, 13.5f)
                .createTransformedShape(new RoundRectangle2D.Float(4.2f, 10.4f, 3.1f, 7.4f, 3.1f, 3.1f));
        hand.add(new Area(thumb));

        g.setPaint(new GradientPaint(0, 3, new Color(0xFFDDB6), 0, 21, new Color(0xDFA36C)));
        g.fill(hand);
        g.setColor(new Color(0x9A6B42));
        g.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hand);
        g.setColor(new Color(255, 255, 255, 130));
        g.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(8, 15, 8, 18);
    }

    // ---- file / edit ------------------------------------------------------
    private static void newDoc(Graphics2D g) {
        g.setColor(WHITE);
        g.fillPolygon(new int[]{5, 15, 19, 19, 5}, new int[]{3, 3, 7, 21, 21}, 5);
        g.setColor(GRAY);
        g.fillPolygon(new int[]{15, 19, 15}, new int[]{3, 7, 7}, 3);
        g.setColor(DARK);
        g.drawPolygon(new int[]{5, 15, 19, 19, 5}, new int[]{3, 3, 7, 21, 21}, 5);
        g.drawLine(15, 3, 15, 7);
        g.drawLine(15, 7, 19, 7);
        g.setColor(GREEN);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(9, 14, 15, 14);
        g.drawLine(12, 11, 12, 17);
    }

    private static void folder(Graphics2D g, boolean open) {
        g.setColor(new Color(0xE9B94E));
        g.fillRoundRect(2, 7, 20, 13, 3, 3);
        g.setColor(new Color(0xC99A2E));
        g.fillRoundRect(2, 5, 9, 5, 2, 2);
        g.setColor(new Color(0x8A6A1E));
        g.drawRoundRect(2, 6, 20, 14, 3, 3);
        if (open) {
            g.setColor(new Color(0xFFE9A8));
            g.fillPolygon(new int[]{4, 21, 19, 2}, new int[]{11, 11, 19, 19}, 4);
            g.setColor(new Color(0x8A6A1E));
            g.drawPolygon(new int[]{4, 21, 19, 2}, new int[]{11, 11, 19, 19}, 4);
        }
    }

    private static void save(Graphics2D g) {
        g.setColor(new Color(0x2B63B8));
        g.fillRoundRect(3, 3, 18, 18, 3, 3);
        g.setColor(new Color(0x1A3F7A));
        g.drawRoundRect(3, 3, 18, 18, 3, 3);
        g.setColor(WHITE);
        g.fillRect(7, 5, 10, 6);
        g.setColor(GRAY);
        g.fillRect(6, 14, 12, 7);
        g.setColor(new Color(0x5A5A5A));
        g.fillRect(8, 15, 8, 1);
        g.fillRect(8, 17, 8, 1);
        g.setColor(BLUE_DARK);
        g.fillRect(14, 6, 2, 4);
    }

    private static void arrow(Graphics2D g, boolean right) {
        Graphics2D gg = (Graphics2D) g.create();
        if (right) {
            gg.translate(24, 0);
            gg.scale(-1, 1);
        }
        gg.setColor(BLUE);
        gg.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gg.drawArc(5, 6, 14, 12, 40, 220);
        gg.setColor(BLUE_DARK);
        gg.fillPolygon(new int[]{5, 4, 10}, new int[]{6, 13, 9}, 3);
        gg.dispose();
    }

    private static void layerAdd(Graphics2D g) {
        g.setColor(new Color(0xE8EEF9));
        g.fillRoundRect(4, 4, 12, 15, 2, 2);
        g.setColor(BLUE_DARK);
        g.drawRoundRect(4, 4, 12, 15, 2, 2);
        g.setColor(GREEN);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(16, 15, 22, 15);
        g.drawLine(19, 12, 19, 18);
    }

    private static void layerDuplicate(Graphics2D g) {
        g.setColor(new Color(0xE8EEF9));
        g.fillRoundRect(3, 6, 12, 14, 2, 2);
        g.setColor(BLUE_DARK);
        g.drawRoundRect(3, 6, 12, 14, 2, 2);
        g.setColor(new Color(0xF8FAFF));
        g.fillRoundRect(9, 3, 12, 14, 2, 2);
        g.setColor(BLUE_DARK);
        g.drawRoundRect(9, 3, 12, 14, 2, 2);
    }

    private static void trash(Graphics2D g) {
        g.setColor(new Color(0xB9C6D8));
        g.fillPolygon(new int[]{7, 17, 16, 8}, new int[]{8, 8, 21, 21}, 4);
        g.setColor(DARK);
        g.drawPolygon(new int[]{7, 17, 16, 8}, new int[]{8, 8, 21, 21}, 4);
        g.setColor(new Color(0x6C7A8C));
        g.fillRoundRect(5, 5, 14, 3, 2, 2);
        g.setColor(DARK);
        g.drawRoundRect(5, 5, 14, 3, 2, 2);
        g.drawLine(10, 3, 14, 3);
        g.setColor(WHITE);
        for (int x = 10; x <= 14; x += 2) g.drawLine(x, 10, x, 18);
    }

    private static void triangle(Graphics2D g, boolean up) {
        g.setColor(new Color(0x5C8CC9));
        if (up) g.fillPolygon(new int[]{6, 18, 12}, new int[]{15, 15, 7}, 3);
        else g.fillPolygon(new int[]{6, 18, 12}, new int[]{9, 9, 17}, 3);
        g.setColor(BLUE_DARK);
        if (up) g.drawPolygon(new int[]{6, 18, 12}, new int[]{15, 15, 7}, 3);
        else g.drawPolygon(new int[]{6, 18, 12}, new int[]{9, 9, 17}, 3);
    }

    private static void eye(Graphics2D g, boolean on) {
        g.setStroke(new BasicStroke(1.3f));
        g.setColor(on ? DARK : new Color(0x9A9A9A));
        g.drawArc(2, 6, 20, 12, 0, 360);
        g.drawArc(2, 6, 20, 12, 20, 140);
        if (on) {
            g.setColor(BLUE_DARK);
            g.fillOval(9, 9, 6, 6);
            g.setColor(WHITE);
            g.fillOval(10, 10, 2, 2);
        } else {
            g.setColor(new Color(0x9A9A9A));
            g.drawLine(4, 19, 20, 5);
        }
    }

    private static void swap(Graphics2D g) {
        g.setColor(BLUE_DARK);
        g.setStroke(new BasicStroke(1.6f));
        g.drawLine(5, 8, 17, 8);
        g.drawLine(17, 8, 17, 16);
        g.fillPolygon(new int[]{17, 14, 20}, new int[]{16, 12, 12}, 3);
        g.drawLine(7, 16, 19, 16);
        g.fillPolygon(new int[]{7, 4, 10}, new int[]{8, 12, 12}, 3);
    }

    private static void resetColors(Graphics2D g) {
        g.setColor(WHITE);
        g.fillRect(4, 4, 12, 12);
        g.setColor(DARK);
        g.drawRect(4, 4, 12, 12);
        g.setColor(DARK);
        g.fillRect(9, 9, 11, 11);
        g.setColor(new Color(0x666666));
        g.drawRect(9, 9, 11, 11);
    }

    private static void grid(Graphics2D g) {
        g.setColor(new Color(0x5F5F5F));
        for (int i = 4; i <= 20; i += 4) {
            g.drawLine(i, 3, i, 21);
            g.drawLine(3, i, 21, i);
        }
        g.setColor(new Color(0x3F7FD8));
        g.fillRect(7, 7, 4, 4);
    }

    private static void fit(Graphics2D g) {
        g.setColor(BLUE_DARK);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRect(5, 5, 14, 14);
        g.drawLine(5, 5, 10, 5);
        g.drawLine(5, 5, 5, 10);
        g.drawLine(19, 19, 14, 19);
        g.drawLine(19, 19, 19, 14);
        g.setColor(BLUE);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(8, 16, 16, 8);
    }

    private static void rotate(Graphics2D g) {
        g.setColor(BLUE);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(4, 5, 16, 15, 40, 270);
        g.setColor(BLUE_DARK);
        g.fillPolygon(new int[]{17, 22, 17}, new int[]{5, 8, 11}, 3);
    }

    private static void file(Graphics2D g) {
        g.setColor(WHITE);
        g.fillPolygon(new int[]{5, 15, 19, 19, 5}, new int[]{3, 3, 7, 21, 21}, 5);
        g.setColor(DARK);
        g.drawPolygon(new int[]{5, 15, 19, 19, 5}, new int[]{3, 3, 7, 21, 21}, 5);
        g.drawLine(15, 3, 15, 7);
        g.drawLine(15, 7, 19, 7);
        g.setColor(new Color(0x8FA8C8));
        for (int y = 10; y <= 18; y += 3) g.drawLine(8, y, 16, y);
    }

    private static void drive(Graphics2D g) {
        g.setColor(new Color(0xC9D3DF));
        g.fillRoundRect(3, 10, 18, 9, 4, 4);
        g.setColor(DARK);
        g.drawRoundRect(3, 10, 18, 9, 4, 4);
        g.setColor(GREEN);
        g.fillOval(17, 13, 2, 2);
        g.setColor(new Color(0x8FA0B5));
        g.fillRect(6, 13, 8, 2);
    }

    private static void upFolder(Graphics2D g) {
        g.setColor(YELLOW);
        g.fillRoundRect(3, 8, 18, 12, 3, 3);
        g.setColor(new Color(0xC99A2E));
        g.fillRoundRect(3, 5, 8, 5, 2, 2);
        g.setColor(DARK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(12, 18, 12, 11);
        g.fillPolygon(new int[]{12, 8, 16}, new int[]{9, 14, 14}, 3);
    }

    private static void home(Graphics2D g) {
        g.setColor(new Color(0xF2E3C0));
        g.fillRect(6, 11, 12, 10);
        g.setColor(DARK);
        g.drawRect(6, 11, 12, 10);
        g.setColor(new Color(0xC0503B));
        g.fillPolygon(new int[]{3, 12, 21}, new int[]{11, 3, 11}, 3);
        g.setColor(DARK);
        g.drawPolygon(new int[]{3, 12, 21}, new int[]{11, 3, 11}, 3);
        g.setColor(new Color(0x6B4A2A));
        g.fillRect(10, 14, 4, 7);
    }

    private static void info(Graphics2D g) {
        g.setColor(new Color(0x2E6BC8));
        g.fillOval(3, 3, 18, 18);
        g.setColor(WHITE);
        g.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 15));
        g.drawString("i", 10, 17);
    }

    private static void check(Graphics2D g) {
        g.setColor(BLUE_DARK);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(5, 12, 10, 17);
        g.drawLine(10, 17, 19, 7);
    }

    private static void dot(Graphics2D g) {
        g.setColor(BLUE);
        g.fillOval(8, 8, 8, 8);
    }

    private static void app(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x60A8FF), 24, 24, new Color(0x0B3FA8)));
        g.fill(new RoundRectangle2D.Float(1, 1, 22, 22, 7, 7));
        g.setColor(new Color(255, 255, 255, 90));
        g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, 21, 21, 7, 7));
        g.setColor(new Color(0xF4E7C8));
        g.fillOval(5, 6, 14, 12);
        g.setColor(new Color(0x9A6B32));
        g.drawOval(5, 6, 14, 12);
        g.setColor(RED);
        g.fillOval(7, 9, 3, 3);
        g.setColor(GREEN);
        g.fillOval(13, 8, 3, 3);
        g.setColor(BLUE);
        g.fillOval(11, 13, 4, 3);
        g.setColor(YELLOW);
        g.fillOval(7, 14, 3, 2);
        g.setColor(new Color(0xFFF6E0));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(16, 16, 21, 21);
        g.setColor(BROWN);
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(18, 18, 21, 21);
    }
}
