package photopaint.model;

import photopaint.i18n.I18n;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The edited image: a stack of layers plus the current selection (rectangle or lasso). */
public class Document {

    public int width;
    public int height;
    /** Index 0 is the topmost layer. */
    public final List<Layer> layers = new ArrayList<>();
    public int active = 0;
    /** Bounding box of the selection. */
    public Rectangle selection;
    /** Free-form outline from the lasso, or null for a plain rectangular selection. */
    private Shape selectionShape;
    public File file;
    public boolean dirty;

    private BufferedImage compositeCache;
    private BufferedImage previewImage;
    private int previewIndex = -1;

    public Document(int width, int height) {
        this(width, height, I18n.t("Фон"), Color.WHITE);
    }

    public Document(int width, int height, String layerName, Color fill) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        Layer l = new Layer(layerName, this.width, this.height);
        if (fill != null) {
            Graphics2D g = l.image.createGraphics();
            g.setColor(fill);
            g.fillRect(0, 0, this.width, this.height);
            g.dispose();
        }
        layers.add(l);
        active = 0;
    }

    public Document(BufferedImage image, String name) {
        width = image.getWidth();
        height = image.getHeight();
        Layer l = new Layer(name, toArgb(image));
        layers.add(l);
        active = 0;
    }

    public static BufferedImage toArgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    public Layer activeLayer() {
        if (layers.isEmpty()) return null;
        if (active < 0) active = 0;
        if (active >= layers.size()) active = layers.size() - 1;
        return layers.get(active);
    }

    public BufferedImage activeImage() {
        Layer l = activeLayer();
        return l == null ? null : l.image;
    }

    public Rectangle bounds() {
        return new Rectangle(0, 0, width, height);
    }

    /** Selection clipped to the document, or the whole document when nothing is selected. */
    public Rectangle clip() {
        if (selection == null) return bounds();
        Rectangle r = selection.intersection(bounds());
        return r.isEmpty() ? bounds() : r;
    }

    public Rectangle selectionOrNull() {
        if (selection == null) return null;
        Rectangle r = selection.intersection(bounds());
        return r.isEmpty() ? null : r;
    }

    public void setSelection(Rectangle r) {
        if (r == null) {
            selection = null;
            selectionShape = null;
        } else {
            r = r.intersection(bounds());
            selection = r.isEmpty() ? null : r;
            selectionShape = null;
        }
        invalidate();
    }

    /** Selects an arbitrary free-form shape (the lasso). */
    public void setSelectionShape(Shape shape) {
        if (shape == null) {
            clearSelection();
            return;
        }
        Area area = new Area(shape);
        area.intersect(new Area(bounds()));
        Rectangle b = area.getBounds();
        if (b.isEmpty()) {
            selection = null;
            selectionShape = null;
        } else {
            selection = new Rectangle(b);
            selectionShape = area;
        }
        invalidate();
    }

    public void clearSelection() {
        selection = null;
        selectionShape = null;
        invalidate();
    }

    /** The selection outline, or null when nothing is selected. */
    public Shape selectionShape() {
        if (selection == null) return null;
        return selectionShape != null ? selectionShape : new Rectangle(selection);
    }

    /** Clipping shape: the selection outline, or the whole document when nothing is selected. */
    public Shape clipShape() {
        if (selection == null) return bounds();
        return selectionShape != null ? selectionShape : new Rectangle(selection);
    }

    public void invalidate() {
        compositeCache = null;
    }

    // ---- live preview (used by adjustment dialogs) ------------------------
    public void setPreview(int layerIndex, BufferedImage image) {
        previewIndex = layerIndex;
        previewImage = image;
        invalidate();
    }

    public void clearPreview() {
        previewIndex = -1;
        previewImage = null;
        invalidate();
    }

    public boolean hasPreview() {
        return previewImage != null;
    }

    // ---- composition ------------------------------------------------------
    public BufferedImage composite() {
        if (compositeCache != null) return compositeCache;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer l = layers.get(i);
            if (!l.visible) continue;
            BufferedImage src = (i == previewIndex && previewImage != null) ? previewImage : l.image;
            if (src == null) continue;
            float op = Math.max(0f, Math.min(1f, l.opacity));
            if (op <= 0f) continue;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, op));
            g.drawImage(src, 0, 0, null);
        }
        g.dispose();
        compositeCache = img;
        return img;
    }

    public BufferedImage compositeForSave() {
        BufferedImage saved = previewImage;
        int savedIndex = previewIndex;
        if (saved != null) {
            clearPreview();
        }
        BufferedImage img = composite();
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        if (saved != null) {
            setPreview(savedIndex, saved);
        }
        return copy;
    }

    // ---- layer operations -------------------------------------------------
    public Layer addLayer(String name, boolean transparent) {
        Layer l = new Layer(uniqueName(name), width, height);
        if (!transparent) {
            Graphics2D g = l.image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();
        }
        layers.add(Math.max(0, Math.min(active, layers.size())), l);
        active = layers.indexOf(l);
        invalidate();
        return l;
    }

    public void duplicateActive() {
        Layer l = activeLayer();
        if (l == null) return;
        Layer c = l.copy();
        c.name = uniqueName(l.name + I18n.t(" копия"));
        layers.add(active, c);
        invalidate();
    }

    public void deleteActive() {
        if (layers.size() <= 1) return;
        layers.remove(active);
        if (active >= layers.size()) active = layers.size() - 1;
        invalidate();
    }

    public void moveActive(int delta) {
        int ni = active + delta;
        if (ni < 0 || ni >= layers.size()) return;
        Layer l = layers.remove(active);
        layers.add(ni, l);
        active = ni;
        invalidate();
    }

    public void mergeDown() {
        if (active + 1 >= layers.size()) return;
        Layer top = layers.get(active);
        Layer bottom = layers.get(active + 1);
        BufferedImage merged = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = merged.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, bottom.opacity))));
        if (bottom.visible) g.drawImage(bottom.image, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, top.opacity))));
        if (top.visible) g.drawImage(top.image, 0, 0, null);
        g.dispose();
        Layer l = new Layer(bottom.name, merged);
        layers.remove(active + 1);
        layers.set(active, l);
        invalidate();
    }

    public void flatten() {
        BufferedImage img = composite();
        Layer l = new Layer(I18n.t("Фон"), img);
        layers.clear();
        layers.add(l);
        active = 0;
        invalidate();
    }

    public String uniqueName(String base) {
        String candidate = base;
        int n = 2;
        boolean clash;
        do {
            clash = false;
            for (Layer l : layers) {
                if (l.name.equals(candidate)) {
                    clash = true;
                    candidate = base + " " + n++;
                    break;
                }
            }
        } while (clash);
        return candidate;
    }

    // ---- geometry ---------------------------------------------------------
    public void resizeImage(int w, int h) {
        w = Math.max(1, w);
        h = Math.max(1, h);
        for (Layer l : layers) {
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(l.image, 0, 0, w, h, null);
            g.dispose();
            l.image = img;
        }
        width = w;
        height = h;
        selection = null;
        selectionShape = null;
        invalidate();
    }

    public void cropTo(Rectangle r) {
        r = r.intersection(bounds());
        if (r.isEmpty()) return;
        for (Layer l : layers) {
            BufferedImage img = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.drawImage(l.image, -r.x, -r.y, null);
            g.dispose();
            l.image = img;
        }
        width = r.width;
        height = r.height;
        selection = null;
        selectionShape = null;
        invalidate();
    }

    public void rotate90(boolean clockwise) {
        for (Layer l : layers) {
            BufferedImage img = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            if (clockwise) {
                g.translate(height, 0);
                g.rotate(Math.PI / 2);
            } else {
                g.translate(0, width);
                g.rotate(-Math.PI / 2);
            }
            g.drawImage(l.image, 0, 0, null);
            g.dispose();
            l.image = img;
        }
        int t = width;
        width = height;
        height = t;
        selection = null;
        selectionShape = null;
        invalidate();
    }

    public void rotate180() {
        for (Layer l : layers) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.translate(width, height);
            g.rotate(Math.PI);
            g.drawImage(l.image, 0, 0, null);
            g.dispose();
            l.image = img;
        }
        invalidate();
    }

    public void flip(boolean horizontal) {
        for (Layer l : layers) {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            if (horizontal) {
                g.translate(width, 0);
                g.scale(-1, 1);
            } else {
                g.translate(0, height);
                g.scale(1, -1);
            }
            g.drawImage(l.image, 0, 0, null);
            g.dispose();
            l.image = img;
        }
        invalidate();
    }

    // ---- snapshots --------------------------------------------------------
    public static class Snapshot {
        final int width;
        final int height;
        final List<Layer> layers;
        final int active;

        private Snapshot(int width, int height, List<Layer> layers, int active) {
            this.width = width;
            this.height = height;
            this.layers = layers;
            this.active = active;
        }

        public static Snapshot of(Document d) {
            List<Layer> copies = new ArrayList<>();
            for (Layer l : d.layers) copies.add(l.copy());
            return new Snapshot(d.width, d.height, copies, d.active);
        }

        public long bytes() {
            long b = 64;
            for (Layer l : layers) {
                b += (long) l.image.getWidth() * l.image.getHeight() * 4L + 128;
            }
            return b;
        }

        public void restoreTo(Document d) {
            d.width = width;
            d.height = height;
            d.layers.clear();
            for (Layer l : layers) d.layers.add(l.copy());
            d.active = Math.max(0, Math.min(active, d.layers.size() - 1));
            if (d.selectionShape != null) d.setSelectionShape(d.selectionShape);
            else if (d.selection != null) d.setSelection(d.selection);
            d.invalidate();
        }
    }

    public List<Layer> layersTopFirst() {
        return Collections.unmodifiableList(layers);
    }
}
