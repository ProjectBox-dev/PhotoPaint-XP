package photopaint.util;

import photopaint.i18n.I18n;
import photopaint.model.Document;
import photopaint.model.Layer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Native PhotoPaint XP project format: a ZIP archive with a small manifest and
 * one lossless PNG per layer, so layer names, order, visibility and opacity survive.
 */
public final class ProjectFiles {

    public static final String EXTENSION = "ppxp";
    private static final String MANIFEST = "manifest.properties";
    private static final int VERSION = 1;

    private ProjectFiles() {
    }

    public static boolean isProject(File f) {
        return f.getName().toLowerCase().endsWith("." + EXTENSION);
    }

    public static void write(Document doc, File f) throws IOException {
        Properties p = new Properties();
        p.setProperty("version", Integer.toString(VERSION));
        p.setProperty("width", Integer.toString(doc.width));
        p.setProperty("height", Integer.toString(doc.height));
        p.setProperty("active", Integer.toString(doc.active));
        p.setProperty("layer.count", Integer.toString(doc.layers.size()));
        for (int i = 0; i < doc.layers.size(); i++) {
            Layer l = doc.layers.get(i);
            p.setProperty("layer." + i + ".name", l.name == null ? "" : l.name);
            p.setProperty("layer." + i + ".visible", Boolean.toString(l.visible));
            p.setProperty("layer." + i + ".opacity", Float.toString(l.opacity));
        }

        try (ZipOutputStream zip = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(f)))) {
            zip.putNextEntry(new ZipEntry(MANIFEST));
            p.store(zip, "PhotoPaint XP project");
            zip.closeEntry();
            for (int i = 0; i < doc.layers.size(); i++) {
                zip.putNextEntry(new ZipEntry("layer" + i + ".png"));
                if (!ImageIO.write(doc.layers.get(i).image, "png", zip)) {
                    throw new IOException(I18n.t("Не удалось записать слой ") + i);
                }
                zip.closeEntry();
            }
        }
    }

    public static Document read(File f) throws IOException {
        Properties p = new Properties();
        int count;
        int width;
        int height;
        try (ZipFile zip = new ZipFile(f)) {
            ZipEntry manifest = zip.getEntry(MANIFEST);
            if (manifest == null) {
                throw new IOException(I18n.t("Это не файл проекта PhotoPaint XP"));
            }
            try (InputStream in = zip.getInputStream(manifest)) {
                p.load(in);
            }
            int version = intValue(p, "version", 0);
            if (version <= 0 || version > VERSION) {
                throw new IOException(I18n.t("Неподдерживаемая версия проекта: ") + version);
            }
            count = Math.max(1, intValue(p, "layer.count", 1));
            width = Math.max(1, intValue(p, "width", 1));
            height = Math.max(1, intValue(p, "height", 1));

            Document doc = new Document(width, height, I18n.t("Фон"), null);
            doc.layers.clear();
            for (int i = 0; i < count; i++) {
                ZipEntry entry = zip.getEntry("layer" + i + ".png");
                if (entry == null) continue;
                BufferedImage img;
                try (InputStream in = zip.getInputStream(entry)) {
                    img = ImageIO.read(in);
                }
                if (img == null) continue;
                img = Document.toArgb(img);
                if (img.getWidth() != width || img.getHeight() != height) {
                    BufferedImage fit = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g = fit.createGraphics();
                    g.drawImage(img, 0, 0, null);
                    g.dispose();
                    img = fit;
                }
                Layer l = new Layer(p.getProperty("layer." + i + ".name", I18n.t("Слой ") + (i + 1)), img);
                l.visible = Boolean.parseBoolean(p.getProperty("layer." + i + ".visible", "true"));
                l.opacity = clampOpacity(p.getProperty("layer." + i + ".opacity", "1"));
                doc.layers.add(l);
            }
            if (doc.layers.isEmpty()) {
                throw new IOException(I18n.t("В проекте нет слоёв"));
            }
            doc.active = Math.max(0, Math.min(intValue(p, "active", 0), doc.layers.size() - 1));
            doc.invalidate();
            return doc;
        }
    }

    private static int intValue(Properties p, String key, int def) {
        try {
            return Integer.parseInt(p.getProperty(key, Integer.toString(def)).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static float clampOpacity(String s) {
        try {
            return Math.max(0f, Math.min(1f, Float.parseFloat(s.trim())));
        } catch (NumberFormatException e) {
            return 1f;
        }
    }
}
