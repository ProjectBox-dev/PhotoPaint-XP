package photopaint.filters;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.BiConsumer;

/** Pixel filters. Every method changes only pixels inside {@code clip}. */
public final class Filters {

    private Filters() {
    }

    private static Rectangle safeClip(BufferedImage img, Rectangle clip) {
        if (clip == null) return new Rectangle(0, 0, img.getWidth(), img.getHeight());
        Rectangle r = clip.intersection(new Rectangle(0, 0, img.getWidth(), img.getHeight()));
        return r.isEmpty() ? new Rectangle(0, 0, img.getWidth(), img.getHeight()) : r;
    }

    // ---- color adjustments -------------------------------------------------
    public static void brightnessContrast(BufferedImage img, Rectangle clip, int brightness, int contrast) {
        Rectangle r = safeClip(img, clip);
        int[] px = pixels(img);
        double c = Math.max(-100, Math.min(100, contrast)) * 2.54;
        double factor = (259.0 * (c + 255.0)) / (255.0 * (259.0 - c));
        int b = (int) Math.round(brightness * 2.55);
        for (int y = r.y; y < r.y + r.height; y++) {
            int row = y * img.getWidth();
            for (int x = r.x; x < r.x + r.width; x++) {
                int p = px[row + x];
                int a = p >>> 24;
                int rr = clamp(factor * (((p >> 16) & 255) - 128) + 128 + b);
                int gg = clamp(factor * (((p >> 8) & 255) - 128) + 128 + b);
                int bb = clamp(factor * ((p & 255) - 128) + 128 + b);
                px[row + x] = (a << 24) | (rr << 16) | (gg << 8) | bb;
            }
        }
        write(img, px);
    }

    public static void hueSaturation(BufferedImage img, Rectangle clip, int hueShift, int saturation) {
        Rectangle r = safeClip(img, clip);
        int[] px = pixels(img);
        float[] hsb = new float[3];
        float satMul = Math.max(0f, 1f + saturation / 100f);
        for (int y = r.y; y < r.y + r.height; y++) {
            int row = y * img.getWidth();
            for (int x = r.x; x < r.x + r.width; x++) {
                int p = px[row + x];
                int a = p >>> 24;
                int rr = (p >> 16) & 255;
                int gg = (p >> 8) & 255;
                int bb = p & 255;
                java.awt.Color.RGBtoHSB(rr, gg, bb, hsb);
                float h = (hsb[0] + hueShift / 360f) % 1f;
                if (h < 0) h += 1f;
                float s = Math.min(1f, hsb[1] * satMul);
                int rgb = java.awt.Color.HSBtoRGB(h, s, hsb[2]);
                px[row + x] = (a << 24) | (rgb & 0xFFFFFF);
            }
        }
        write(img, px);
    }

    public static void desaturate(BufferedImage img, Rectangle clip) {
        Rectangle r = safeClip(img, clip);
        int[] px = pixels(img);
        for (int y = r.y; y < r.y + r.height; y++) {
            int row = y * img.getWidth();
            for (int x = r.x; x < r.x + r.width; x++) {
                int p = px[row + x];
                int a = p >>> 24;
                int rr = (p >> 16) & 255;
                int gg = (p >> 8) & 255;
                int bb = p & 255;
                int lum = clamp(0.299 * rr + 0.587 * gg + 0.114 * bb);
                px[row + x] = (a << 24) | (lum << 16) | (lum << 8) | lum;
            }
        }
        write(img, px);
    }

    public static void invert(BufferedImage img, Rectangle clip) {
        Rectangle r = safeClip(img, clip);
        int[] px = pixels(img);
        for (int y = r.y; y < r.y + r.height; y++) {
            int row = y * img.getWidth();
            for (int x = r.x; x < r.x + r.width; x++) {
                int p = px[row + x];
                int a = p >>> 24;
                px[row + x] = (a << 24) | ((255 - ((p >> 16) & 255)) << 16)
                        | ((255 - ((p >> 8) & 255)) << 8) | (255 - (p & 255));
            }
        }
        write(img, px);
    }

    public static void sepia(BufferedImage img, Rectangle clip) {
        Rectangle r = safeClip(img, clip);
        int[] px = pixels(img);
        for (int y = r.y; y < r.y + r.height; y++) {
            int row = y * img.getWidth();
            for (int x = r.x; x < r.x + r.width; x++) {
                int p = px[row + x];
                int a = p >>> 24;
                int rr = (p >> 16) & 255;
                int gg = (p >> 8) & 255;
                int bb = p & 255;
                int nr = clamp(0.393 * rr + 0.769 * gg + 0.189 * bb);
                int ng = clamp(0.349 * rr + 0.686 * gg + 0.168 * bb);
                int nb = clamp(0.272 * rr + 0.534 * gg + 0.131 * bb);
                px[row + x] = (a << 24) | (nr << 16) | (ng << 8) | nb;
            }
        }
        write(img, px);
    }

    // ---- blur / sharpen ----------------------------------------------------
    public static void blur(BufferedImage img, Rectangle clip, int radius) {
        if (radius <= 0) return;
        Rectangle r = safeClip(img, clip);
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = pixels(img);
        int n = w * h;
        int[] pr = new int[n];
        int[] pg = new int[n];
        int[] pb = new int[n];
        int[] pa = new int[n];
        for (int i = 0; i < n; i++) {
            int p = px[i];
            int a = p >>> 24;
            pa[i] = a;
            pr[i] = ((p >> 16) & 255) * a / 255;
            pg[i] = ((p >> 8) & 255) * a / 255;
            pb[i] = (p & 255) * a / 255;
        }
        pr = box(pr, w, h, radius);
        pg = box(pg, w, h, radius);
        pb = box(pb, w, h, radius);
        pa = box(pa, w, h, radius);
        for (int y = r.y; y < r.y + r.height; y++) {
            int row = y * w;
            for (int x = r.x; x < r.x + r.width; x++) {
                int i = row + x;
                int a = pa[i];
                if (a <= 0) {
                    px[i] = 0;
                } else {
                    int rr = clamp(pr[i] * 255.0 / a);
                    int gg = clamp(pg[i] * 255.0 / a);
                    int bb = clamp(pb[i] * 255.0 / a);
                    px[i] = (a << 24) | (rr << 16) | (gg << 8) | bb;
                }
            }
        }
        write(img, px);
    }

    private static int[] box(int[] src, int w, int h, int radius) {
        int[] tmp = new int[w * h];
        int[] out = new int[w * h];
        int window = radius * 2 + 1;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            int sum = 0;
            for (int k = -radius; k <= radius; k++) sum += src[row + clampI(k, w)];
            for (int x = 0; x < w; x++) {
                tmp[row + x] = sum / window;
                sum -= src[row + clampI(x - radius, w)];
                sum += src[row + clampI(x + radius + 1, w)];
            }
        }
        for (int x = 0; x < w; x++) {
            int sum = 0;
            for (int k = -radius; k <= radius; k++) sum += tmp[clampI(k, h) * w + x];
            for (int y = 0; y < h; y++) {
                out[y * w + x] = sum / window;
                sum -= tmp[clampI(y - radius, h) * w + x];
                sum += tmp[clampI(y + radius + 1, h) * w + x];
            }
        }
        return out;
    }

    public static void sharpen(BufferedImage img, Rectangle clip, int amount) {
        if (amount <= 0) return;
        Rectangle r = safeClip(img, clip);
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = pixels(img);
        int[] copy = px.clone();
        double k = amount / 100.0;
        int[] kernel = {0, -1, 0, -1, 5, -1, 0, -1, 0};
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                int a = copy[y * w + x] >>> 24;
                int rr = 0, gg = 0, bb = 0;
                int ki = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int p = copy[clampI(y + dy, h) * w + clampI(x + dx, w)];
                        int kv = kernel[ki++];
                        rr += ((p >> 16) & 255) * kv;
                        gg += ((p >> 8) & 255) * kv;
                        bb += (p & 255) * kv;
                    }
                }
                int or = (copy[y * w + x] >> 16) & 255;
                int og = (copy[y * w + x] >> 8) & 255;
                int ob = copy[y * w + x] & 255;
                rr = clamp(or + (rr - or) * k);
                gg = clamp(og + (gg - og) * k);
                bb = clamp(ob + (bb - ob) * k);
                px[y * w + x] = (a << 24) | (rr << 16) | (gg << 8) | bb;
            }
        }
        write(img, px);
    }

    public static void pixelate(BufferedImage img, Rectangle clip, int block) {
        if (block <= 1) return;
        Rectangle r = safeClip(img, clip);
        int w = img.getWidth();
        int[] px = pixels(img);
        for (int by = r.y; by < r.y + r.height; by += block) {
            int bh = Math.min(block, r.y + r.height - by);
            for (int bx = r.x; bx < r.x + r.width; bx += block) {
                int bw = Math.min(block, r.x + r.width - bx);
                long sa = 0, sr = 0, sg = 0, sb = 0;
                int count = 0;
                for (int y = by; y < by + bh; y++) {
                    for (int x = bx; x < bx + bw; x++) {
                        int p = px[y * w + x];
                        int a = p >>> 24;
                        sa += a;
                        sr += ((p >> 16) & 255) * a;
                        sg += ((p >> 8) & 255) * a;
                        sb += (p & 255) * a;
                        count++;
                    }
                }
                int a = (int) (sa / count);
                int rr = sa == 0 ? 0 : clamp((double) sr / sa);
                int gg = sa == 0 ? 0 : clamp((double) sg / sa);
                int bb = sa == 0 ? 0 : clamp((double) sb / sa);
                int val = (a << 24) | (rr << 16) | (gg << 8) | bb;
                for (int y = by; y < by + bh; y++) {
                    for (int x = bx; x < bx + bw; x++) px[y * w + x] = val;
                }
            }
        }
        write(img, px);
    }

    public static void noise(BufferedImage img, Rectangle clip, int amount) {
        if (amount <= 0) return;
        Rectangle r = safeClip(img, clip);
        int w = img.getWidth();
        int[] px = pixels(img);
        Random rnd = new Random(7);
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                int p = px[y * w + x];
                int a = p >>> 24;
                int d = rnd.nextInt(amount * 2 + 1) - amount;
                int rr = clamp(((p >> 16) & 255) + d);
                int gg = clamp(((p >> 8) & 255) + d);
                int bb = clamp((p & 255) + d);
                px[y * w + x] = (a << 24) | (rr << 16) | (gg << 8) | bb;
            }
        }
        write(img, px);
    }

    public static void emboss(BufferedImage img, Rectangle clip) {
        Rectangle r = safeClip(img, clip);
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = pixels(img);
        int[] copy = px.clone();
        int[] kernel = {-2, -1, 0, -1, 1, 1, 0, 1, 2};
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                int a = copy[y * w + x] >>> 24;
                int sum = 0;
                int ki = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int p = copy[clampI(y + dy, h) * w + clampI(x + dx, w)];
                        int lum = (((p >> 16) & 255) * 299 + ((p >> 8) & 255) * 587
                                + (p & 255) * 114) / 1000;
                        sum += lum * kernel[ki++];
                    }
                }
                int v = clamp(sum + 128);
                px[y * w + x] = (a << 24) | (v << 16) | (v << 8) | v;
            }
        }
        write(img, px);
    }

    public static void edgeDetect(BufferedImage img, Rectangle clip) {
        Rectangle r = safeClip(img, clip);
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = pixels(img);
        int[] copy = px.clone();
        int[] gx = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        int[] gy = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                int a = copy[y * w + x] >>> 24;
                int sx = 0, sy = 0;
                int ki = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int p = copy[clampI(y + dy, h) * w + clampI(x + dx, w)];
                        int lum = (((p >> 16) & 255) * 299 + ((p >> 8) & 255) * 587
                                + (p & 255) * 114) / 1000;
                        sx += lum * gx[ki];
                        sy += lum * gy[ki];
                        ki++;
                    }
                }
                int v = clamp(Math.sqrt((double) sx * sx + (double) sy * sy));
                px[y * w + x] = (a << 24) | (v << 16) | (v << 8) | v;
            }
        }
        write(img, px);
    }

    // ---- lasso-aware entry points -----------------------------------------
    /**
     * Runs a rectangle filter on a scratch copy and writes the result back only
     * inside the selection shape, so a lasso restricts the filter exactly.
     */
    private static void masked(BufferedImage img, Shape shape,
                               BiConsumer<BufferedImage, Rectangle> op) {
        Rectangle full = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        Rectangle r = shape == null ? full : shape.getBounds().intersection(full);
        if (r.isEmpty()) return;
        if (shape == null || (shape instanceof Rectangle && r.equals(full))) {
            op.accept(img, r);
            return;
        }
        BufferedImage work = new BufferedImage(img.getWidth(), img.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = work.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        op.accept(work, r);
        g = img.createGraphics();
        g.setClip(shape);
        g.setComposite(AlphaComposite.Src);
        g.drawImage(work, 0, 0, null);
        g.dispose();
    }

    public static void brightnessContrast(BufferedImage img, Shape clip,
                                          int brightness, int contrast) {
        masked(img, clip, (i, r) -> brightnessContrast(i, r, brightness, contrast));
    }

    public static void hueSaturation(BufferedImage img, Shape clip, int hueShift, int saturation) {
        masked(img, clip, (i, r) -> hueSaturation(i, r, hueShift, saturation));
    }

    public static void desaturate(BufferedImage img, Shape clip) {
        masked(img, clip, (i, r) -> desaturate(i, r));
    }

    public static void invert(BufferedImage img, Shape clip) {
        masked(img, clip, (i, r) -> invert(i, r));
    }

    public static void sepia(BufferedImage img, Shape clip) {
        masked(img, clip, (i, r) -> sepia(i, r));
    }

    public static void blur(BufferedImage img, Shape clip, int radius) {
        masked(img, clip, (i, r) -> blur(i, r, radius));
    }

    public static void sharpen(BufferedImage img, Shape clip, int amount) {
        masked(img, clip, (i, r) -> sharpen(i, r, amount));
    }

    public static void pixelate(BufferedImage img, Shape clip, int block) {
        masked(img, clip, (i, r) -> pixelate(i, r, block));
    }

    public static void noise(BufferedImage img, Shape clip, int amount) {
        masked(img, clip, (i, r) -> noise(i, r, amount));
    }

    public static void emboss(BufferedImage img, Shape clip) {
        masked(img, clip, (i, r) -> emboss(i, r));
    }

    public static void edgeDetect(BufferedImage img, Shape clip) {
        masked(img, clip, (i, r) -> edgeDetect(i, r));
    }

    // ---- helpers ----------------------------------------------------------
    private static int[] pixels(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = new int[w * h];
        img.getRGB(0, 0, w, h, px, 0, w);
        return px;
    }

    private static void write(BufferedImage img, int[] px) {
        int w = img.getWidth();
        int h = img.getHeight();
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    private static int clamp(double v) {
        int i = (int) Math.round(v);
        return i < 0 ? 0 : Math.min(i, 255);
    }

    private static int clampI(int i, int size) {
        if (i < 0) return 0;
        if (i >= size) return size - 1;
        return i;
    }
}
