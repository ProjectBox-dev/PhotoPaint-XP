package photopaint.tools;

import photopaint.ui.Icons;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/** Builds the .ico file used by jpackage and by the Windows taskbar. */
public final class IconGen {

    private IconGen() {
    }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "PhotoPaintXP.ico");
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};
        List<int[]> headers = new ArrayList<>();
        List<byte[]> payloads = new ArrayList<>();
        for (int size : sizes) {
            BufferedImage img = render(size);
            byte[] data = size >= 128 ? png(img) : dib(img);
            headers.add(new int[]{size, data.length});
            payloads.add(data);
        }
        int offset = 6 + 16 * sizes.length;
        ByteArrayOutputStream all = new ByteArrayOutputStream();
        ByteBuffer head = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN);
        head.putShort((short) 0).putShort((short) 1).putShort((short) sizes.length);
        all.write(head.array());
        for (int i = 0; i < sizes.length; i++) {
            int size = headers.get(i)[0];
            int len = headers.get(i)[1];
            ByteBuffer e = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
            e.put((byte) (size >= 256 ? 0 : size));
            e.put((byte) (size >= 256 ? 0 : size));
            e.put((byte) 0).put((byte) 0);
            e.putShort((short) 1).putShort((short) 32);
            e.putInt(len).putInt(offset);
            all.write(e.array());
            offset += len;
        }
        for (byte[] p : payloads) all.write(p);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            all.writeTo(fos);
        }
        System.out.println("Icon written: " + out.getAbsolutePath() + " (" + all.size() + " bytes)");
    }

    private static BufferedImage render(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Icons.appIcon(size).paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    private static byte[] png(BufferedImage img) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    private static byte[] dib(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int xorSize = w * h * 4;
        int maskRow = ((w + 31) / 32) * 4;
        int andSize = maskRow * h;
        ByteBuffer bb = ByteBuffer.allocate(40 + xorSize + andSize).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(40);
        bb.putInt(w);
        bb.putInt(h * 2);
        bb.putShort((short) 1);
        bb.putShort((short) 32);
        bb.putInt(0);
        bb.putInt(xorSize + andSize);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                bb.put((byte) (argb & 255));
                bb.put((byte) ((argb >> 8) & 255));
                bb.put((byte) ((argb >> 16) & 255));
                bb.put((byte) ((argb >>> 24) & 255));
            }
        }
        for (int i = 0; i < andSize; i++) bb.put((byte) 0);
        return bb.array();
    }
}
