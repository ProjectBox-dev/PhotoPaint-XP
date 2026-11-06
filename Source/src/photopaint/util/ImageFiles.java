package photopaint.util;

import photopaint.i18n.I18n;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Reading and writing pictures without external libraries. */
public final class ImageFiles {

    private ImageFiles() {
    }

    public static final String[] EXTENSIONS = {"png", "jpg", "jpeg", "bmp", "gif"};

    public static boolean isSupported(File f) {
        String n = f.getName().toLowerCase();
        for (String e : EXTENSIONS) {
            if (n.endsWith("." + e)) return true;
        }
        return false;
    }

    public static BufferedImage read(File f) throws IOException {
        BufferedImage img = ImageIO.read(f);
        if (img == null) throw new IOException(I18n.t("Формат файла не поддерживается"));
        return toArgb(img);
    }

    public static BufferedImage toArgb(BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }

    public static void write(BufferedImage img, File f) throws IOException {
        String n = f.getName().toLowerCase();
        String format;
        boolean keepAlpha;
        if (n.endsWith(".png")) {
            format = "png";
            keepAlpha = true;
        } else if (n.endsWith(".jpg") || n.endsWith(".jpeg")) {
            format = "jpg";
            keepAlpha = false;
        } else if (n.endsWith(".bmp")) {
            format = "bmp";
            keepAlpha = false;
        } else if (n.endsWith(".gif")) {
            format = "gif";
            keepAlpha = false;
        } else {
            throw new IOException(I18n.t("Неизвестный формат: ") + f.getName());
        }

        BufferedImage out;
        if (keepAlpha) {
            out = img;
        } else {
            out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, out.getWidth(), out.getHeight());
            g.drawImage(img, 0, 0, null);
            g.dispose();
        }
        if (!ImageIO.write(out, format, f)) {
            throw new IOException(I18n.t("Не удалось записать файл в формате ") + format.toUpperCase());
        }
    }

    public static String extension(File f) {
        String n = f.getName();
        int i = n.lastIndexOf('.');
        return i >= 0 ? n.substring(i + 1).toLowerCase() : "";
    }

    public static File withExtension(File f, String ext) {
        String n = f.getName();
        if (n.toLowerCase().endsWith("." + ext)) return f;
        return new File(f.getParentFile(), n + "." + ext);
    }
}
