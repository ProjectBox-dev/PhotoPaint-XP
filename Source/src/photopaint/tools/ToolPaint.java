package photopaint.tools;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/** Shared raster helpers for the painting tools. */
public final class ToolPaint {

    private ToolPaint() {
    }

    public static Rectangle clamp(Rectangle r, int w, int h) {
        Rectangle b = new Rectangle(0, 0, w, h);
        if (r == null) return b;
        Rectangle c = r.intersection(b);
        return c.isEmpty() ? new Rectangle(0, 0, 0, 0) : c;
    }

    public static Graphics2D prepare(BufferedImage img, Shape clip) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (clip != null) g.clip(clip);
        return g;
    }

    public static void stamp(Graphics2D g, double cx, double cy, int size, int hardness,
                             Color color, float opacity, boolean erase) {
        double radius = Math.max(0.55, size / 2.0);
        Graphics2D gg = (Graphics2D) g.create();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float alpha = Math.max(0f, Math.min(1f, opacity));
        if (erase) {
            gg.setComposite(AlphaComposite.DstOut);
        }
        int a = (int) (255 * alpha);
        if (a <= 0) {
            gg.dispose();
            return;
        }
        Color in = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
        Color out = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        if (size <= 2) {
            gg.setColor(in);
            gg.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
        } else {
            float hard = hardness / 100f;
            hard = Math.max(0.02f, Math.min(0.995f, hard));
            RadialGradientPaint paint = new RadialGradientPaint(
                    new Point2D.Double(cx, cy), (float) radius,
                    new float[]{0f, hard, 1f},
                    new Color[]{in, in, out});
            gg.setPaint(paint);
            gg.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
        }
        gg.dispose();
    }

    public static void stroke(Graphics2D g, double x1, double y1, double x2, double y2,
                              int size, int hardness, Color color, float opacity, boolean erase) {
        double dist = Math.hypot(x2 - x1, y2 - y1);
        double spacing = Math.max(0.7, size * 0.12);
        int steps = Math.max(1, (int) Math.ceil(dist / spacing));
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            stamp(g, x1 + (x2 - x1) * t, y1 + (y2 - y1) * t, size, hardness, color, opacity, erase);
        }
    }

    public static void pencilLine(BufferedImage img, Shape clip, double x1, double y1,
                                  double x2, double y2, int size, Color color, float opacity) {
        Graphics2D g = prepare(img, clip);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(color);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.setStroke(new BasicStroke(Math.max(1, size), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        if (size <= 1) {
            g.fillRect((int) Math.floor(x1), (int) Math.floor(y1), 1, 1);
        }
        g.dispose();
    }

    /** Scanline flood fill; returns true if anything changed. */
    public static boolean floodFill(BufferedImage img, int sx, int sy, Color color,
                                    int tolerance, Shape clip) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (sx < 0 || sy < 0 || sx >= w || sy >= h) return false;
        Rectangle area = clamp(clip == null ? null : clip.getBounds(), w, h);
        if (!area.contains(sx, sy)) return false;
        if (clip != null && !clip.contains(sx + 0.5, sy + 0.5)) return false;
        int[] px = new int[w * h];
        img.getRGB(0, 0, w, h, px, 0, w);
        int target = px[sy * w + sx];
        int fill = 0xFF000000 | (color.getRGB() & 0xFFFFFF);
        int tol = Math.max(0, Math.min(255, tolerance * 255 / 100));
        if (target == fill) return false;
        boolean[] seen = new boolean[w * h];
        int[] stack = new int[w * h];
        int sp = 0;
        seen[sy * w + sx] = true;
        stack[sp++] = sy * w + sx;
        boolean changed = false;
        while (sp > 0) {
            int idx = stack[--sp];
            int x = idx % w;
            int y = idx / w;
            if (x < area.x || x >= area.x + area.width || y < area.y || y >= area.y + area.height) {
                continue;
            }
            if (clip != null && !clip.contains(x + 0.5, y + 0.5)) {
                continue;
            }
            if (!matches(px[idx], target, tol)) {
                continue;
            }
            px[idx] = fill;
            changed = true;
            if (x > 0 && !seen[idx - 1]) {
                seen[idx - 1] = true;
                stack[sp++] = idx - 1;
            }
            if (x + 1 < w && !seen[idx + 1]) {
                seen[idx + 1] = true;
                stack[sp++] = idx + 1;
            }
            if (y > 0 && !seen[idx - w]) {
                seen[idx - w] = true;
                stack[sp++] = idx - w;
            }
            if (y + 1 < h && !seen[idx + w]) {
                seen[idx + w] = true;
                stack[sp++] = idx + w;
            }
        }
        if (changed) img.setRGB(0, 0, w, h, px, 0, w);
        return changed;
    }

    private static boolean matches(int p, int q, int tol) {
        int a1 = p >>> 24;
        int a2 = q >>> 24;
        if (Math.abs(a1 - a2) > tol) return false;
        if (a1 == 0 && a2 == 0) return true;
        int r1 = (p >> 16) & 255;
        int g1 = (p >> 8) & 255;
        int b1 = p & 255;
        int r2 = (q >> 16) & 255;
        int g2 = (q >> 8) & 255;
        int b2 = q & 255;
        return Math.abs(r1 - r2) <= tol && Math.abs(g1 - g2) <= tol && Math.abs(b1 - b2) <= tol;
    }

    public static BufferedImage copy(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    public static BufferedImage copyRegion(BufferedImage src, Rectangle r) {
        r = clamp(r, src.getWidth(), src.getHeight());
        BufferedImage out = new BufferedImage(Math.max(1, r.width), Math.max(1, r.height),
                BufferedImage.TYPE_INT_ARGB);
        if (r.width > 0 && r.height > 0) {
            Graphics2D g = out.createGraphics();
            g.drawImage(src.getSubimage(r.x, r.y, r.width, r.height), 0, 0, null);
            g.dispose();
        }
        return out;
    }

    public static void clearRegion(BufferedImage img, Rectangle r) {
        r = clamp(r, img.getWidth(), img.getHeight());
        if (r.width <= 0 || r.height <= 0) return;
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.dispose();
    }

    /** Clears exactly the pixels inside an arbitrary selection shape. */
    public static void clearShape(BufferedImage img, Shape shape) {
        if (shape == null) return;
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fill(shape);
        g.dispose();
    }

    /** Makes all pixels of a region buffer transparent outside the given document-space shape. */
    public static void keepOnly(BufferedImage region, Shape docShape, int originX, int originY) {
        if (docShape == null) return;
        Area outside = new Area(new Rectangle(0, 0, region.getWidth(), region.getHeight()));
        Shape local = AffineTransform.getTranslateInstance(-originX, -originY)
                .createTransformedShape(docShape);
        outside.subtract(new Area(local));
        Graphics2D g = region.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fill(outside);
        g.dispose();
    }

    public static void paste(BufferedImage dest, BufferedImage src, int x, int y, Rectangle clip) {
        Graphics2D g = dest.createGraphics();
        if (clip != null) g.clip(clip);
        g.drawImage(src, x, y, null);
        g.dispose();
    }
}
