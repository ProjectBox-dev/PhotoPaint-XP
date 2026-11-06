package photopaint.model;

import java.awt.image.BufferedImage;

/** One raster layer. Index 0 of a document is the topmost layer. */
public class Layer {

    public String name;
    public BufferedImage image;
    public boolean visible = true;
    public float opacity = 1f;

    public Layer(String name, int width, int height) {
        this.name = name;
        this.image = new BufferedImage(Math.max(1, width), Math.max(1, height),
                BufferedImage.TYPE_INT_ARGB);
    }

    public Layer(String name, BufferedImage image) {
        this.name = name;
        this.image = image;
    }

    public Layer copy() {
        BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        Layer l = new Layer(name, img);
        l.visible = visible;
        l.opacity = opacity;
        return l;
    }
}
