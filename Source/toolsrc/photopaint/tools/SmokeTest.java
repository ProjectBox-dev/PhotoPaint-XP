package photopaint.tools;

import photopaint.Editor;
import photopaint.filters.Filters;
import photopaint.model.Document;
import photopaint.model.Layer;
import photopaint.util.ImageFiles;
import photopaint.util.ProjectFiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JPanel;

/** Non-interactive smoke test of the model, filters, history and file I/O. */
public final class SmokeTest {

    private static int checks;

    public static void main(String[] args) throws Exception {
        Document doc = new Document(160, 120, "Фон", Color.WHITE);
        check(doc.width == 160 && doc.height == 120, "document size");

        Layer layer = doc.addLayer("Тест", true);
        Graphics2D g = layer.image.createGraphics();
        g.setColor(new Color(200, 30, 30));
        g.fillRect(20, 20, 60, 40);
        g.dispose();
        doc.invalidate();
        check(layer.image.getRGB(30, 30) == new Color(200, 30, 30).getRGB(), "layer draw");

        Rectangle clip = doc.clip();
        Filters.invert(layer.image, clip);
        check(layer.image.getRGB(30, 30) == new Color(55, 225, 225).getRGB(), "invert");
        Filters.blur(layer.image, clip, 3);
        Filters.sharpen(layer.image, clip, 50);
        Filters.pixelate(layer.image, clip, 4);
        Filters.noise(layer.image, clip, 10);
        Filters.emboss(layer.image, clip);
        Filters.edgeDetect(layer.image, clip);
        Filters.desaturate(layer.image, clip);
        Filters.sepia(layer.image, clip);
        Filters.brightnessContrast(layer.image, clip, 10, 5);
        Filters.hueSaturation(layer.image, clip, 15, 10);
        check(true, "filters run");

        doc.setSelection(new Rectangle(10, 10, 50, 40));
        check(doc.selectionOrNull() != null, "selection set");
        doc.resizeImage(200, 150);
        check(doc.width == 200 && doc.height == 150, "resize");
        doc.rotate90(true);
        check(doc.width == 150 && doc.height == 200, "rotate 90");
        doc.flip(true);
        doc.rotate180();
        check(true, "transforms run");

        Document histDoc = new Document(40, 30, "Фон", Color.WHITE);
        Editor ed = new Editor();
        ed.setDocument(histDoc, "Тест");
        ed.commit("Первый");
        int before = histDoc.composite().getRGB(10, 10);
        Filters.invert(histDoc.activeImage(), histDoc.clip());
        ed.commit("Инверсия");
        ed.undo();
        check(histDoc.composite().getRGB(10, 10) == before, "undo");
        ed.redo();
        check(histDoc.composite().getRGB(10, 10) != before, "redo");
        check(ed.history.canUndo(), "history can undo");
        while (ed.history.canUndo()) ed.undo();
        check(true, "undo chain");

        File out = new File("build/smoke.png");
        ImageFiles.write(doc.compositeForSave(), out);
        BufferedImage back = ImageFiles.read(out);
        check(back.getWidth() == doc.width && back.getHeight() == doc.height, "png round trip");

        File jpg = new File("build/smoke.jpg");
        ImageFiles.write(doc.compositeForSave(), jpg);
        check(ImageFiles.read(jpg).getWidth() == doc.width, "jpg round trip");
        check(out.delete() && jpg.delete(), "cleanup");

        Document proj = new Document(64, 48, "Фон", Color.WHITE);
        Layer top = proj.addLayer("Верхний", true);
        Graphics2D tg = top.image.createGraphics();
        tg.setColor(new Color(10, 200, 40, 255));
        tg.fillRect(4, 4, 20, 20);
        tg.dispose();
        top.opacity = 0.5f;
        top.visible = false;
        proj.active = 0;
        File ppxp = new File("build/smoke.ppxp");
        ProjectFiles.write(proj, ppxp);
        Document reopened = ProjectFiles.read(ppxp);
        check(reopened.width == 64 && reopened.height == 48, "project size");
        check(reopened.layers.size() == 2, "project layers");
        Layer rt = reopened.layers.get(0);
        check(rt.name.equals("Верхний") && !rt.visible
                && Math.abs(rt.opacity - 0.5f) < 0.001f, "project layer state");
        check(rt.image.getRGB(10, 10) == new Color(10, 200, 40, 255).getRGB(), "project pixels");
        check(ppxp.delete(), "project cleanup");

        // Move tool: a selection must stay under the cursor instead of jumping to the canvas corner.
        Editor moveEd = new Editor();
        Document moveDoc = new Document(100, 100, "Слой", null);
        moveEd.setDocument(moveDoc, "move");
        Graphics2D mg = moveDoc.activeImage().createGraphics();
        mg.setColor(new Color(255, 0, 0));
        mg.fillRect(40, 40, 20, 20);
        mg.dispose();
        moveDoc.setSelection(new Rectangle(40, 40, 20, 20));
        MoveTool move = (MoveTool) moveEd.toolById("move");
        move.press(moveEd, null, toolEvent(50, 50, MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1));
        move.drag(moveEd, null, toolEvent(60, 55, MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON));
        move.release(moveEd, null, toolEvent(60, 55, MouseEvent.MOUSE_RELEASED, MouseEvent.NOBUTTON));
        Rectangle movedSel = moveDoc.selectionOrNull();
        check(moveDoc.activeImage().getRGB(55, 50) == 0xFFFF0000, "move content follows cursor");
        check(movedSel != null && movedSel.equals(new Rectangle(50, 45, 20, 20)), "move selection rect");
        check((moveDoc.activeImage().getRGB(15, 10) >>> 24) == 0, "move does not jump to corner");

        // Lasso selection: a free-form outline clips painting, moving and deletion.
        Editor lassoEd = new Editor();
        Document lassoDoc = new Document(100, 100, "Слой", null);
        lassoEd.setDocument(lassoDoc, "lasso");
        Graphics2D lg = lassoDoc.activeImage().createGraphics();
        lg.setColor(new Color(0, 0, 255));
        lg.fillRect(0, 0, 100, 100);
        lg.dispose();

        Path2D.Double tri = new Path2D.Double();
        tri.moveTo(20, 20);
        tri.lineTo(80, 20);
        tri.lineTo(20, 80);
        tri.closePath();
        lassoDoc.setSelectionShape(tri);
        check(lassoDoc.selectionOrNull().equals(new Rectangle(20, 20, 60, 60)), "lasso bounds");
        check(lassoDoc.clipShape().contains(25, 25) && !lassoDoc.clipShape().contains(70, 70),
                "lasso outline");

        Graphics2D bg = ToolPaint.prepare(lassoDoc.activeImage(), lassoDoc.clipShape());
        bg.setColor(new Color(255, 0, 0));
        bg.fillRect(0, 0, 100, 100);
        bg.dispose();
        check(lassoDoc.activeImage().getRGB(25, 25) == 0xFFFF0000, "lasso paint inside");
        check(lassoDoc.activeImage().getRGB(70, 70) == 0xFF0000FF, "lasso paint outside");

        // Filters must also respect the free-form outline, not just its bounding box.
        Document filterDoc = new Document(100, 100, "Слой", Color.WHITE);
        filterDoc.setSelectionShape(tri);
        Filters.invert(filterDoc.activeImage(), filterDoc.clipShape());
        check(filterDoc.activeImage().getRGB(25, 25) == 0xFF000000, "lasso filter inside");
        check(filterDoc.activeImage().getRGB(70, 70) == 0xFFFFFFFF, "lasso filter outside");

        // Flood fill is bounded by the lasso outline as well.
        Document fillDoc = new Document(100, 100, "Слой", Color.WHITE);
        fillDoc.setSelectionShape(tri);
        ToolPaint.floodFill(fillDoc.activeImage(), 30, 30, Color.RED, 0, fillDoc.clipShape());
        check(fillDoc.activeImage().getRGB(30, 30) == 0xFFFF0000, "lasso fill inside");
        check(fillDoc.activeImage().getRGB(70, 70) == 0xFFFFFFFF, "lasso fill outside");

        MoveTool lassoMove = (MoveTool) lassoEd.toolById("move");
        lassoMove.press(lassoEd, null, toolEvent(25, 25, MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1));
        lassoMove.drag(lassoEd, null, toolEvent(35, 25, MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON));
        lassoMove.release(lassoEd, null, toolEvent(35, 25, MouseEvent.MOUSE_RELEASED, MouseEvent.NOBUTTON));
        check(lassoDoc.activeImage().getRGB(35, 25) == 0xFFFF0000, "lasso move content");
        check((lassoDoc.activeImage().getRGB(25, 25) >>> 24) == 0, "lasso move clears source");
        check(lassoDoc.activeImage().getRGB(70, 70) == 0xFF0000FF, "lasso move keeps outside");
        Rectangle lassoMoved = lassoDoc.selectionOrNull();
        check(lassoMoved != null && lassoMoved.equals(new Rectangle(30, 20, 60, 60)),
                "lasso move bounds");

        ToolPaint.clearShape(lassoDoc.activeImage(), lassoDoc.clipShape());
        check((lassoDoc.activeImage().getRGB(35, 25) >>> 24) == 0, "lasso delete inside");
        check(lassoDoc.activeImage().getRGB(70, 70) == 0xFF0000FF, "lasso delete outside");

        // The lasso tool itself turns a freehand drag into a closed outline.
        Editor freeEd = new Editor();
        Document freeDoc = new Document(100, 100, "Слой", null);
        freeEd.setDocument(freeDoc, "free");
        LassoTool lasso = (LassoTool) freeEd.toolById("lasso");
        lasso.press(freeEd, null, toolEvent(10, 10, MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1));
        lasso.drag(freeEd, null, toolEvent(80, 10, MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON));
        lasso.drag(freeEd, null, toolEvent(80, 80, MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON));
        lasso.drag(freeEd, null, toolEvent(10, 80, MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON));
        lasso.release(freeEd, null, toolEvent(10, 80, MouseEvent.MOUSE_RELEASED, MouseEvent.NOBUTTON));
        check(freeDoc.selectionOrNull() != null && freeDoc.clipShape().contains(50, 50),
                "lasso tool outline");

        System.out.println("SMOKE OK: " + checks + " checks passed");
        System.exit(0);
    }

    private static ToolEvent toolEvent(double x, double y, int id, int button) {
        MouseEvent me = new MouseEvent(new JPanel(), id, System.currentTimeMillis(), 0,
                0, 0, 1, false, button);
        return new ToolEvent(x, y, 0, 0, me);
    }

    private static void check(boolean ok, String name) {
        checks++;
        if (!ok) {
            System.out.println("FAILED: " + name);
            System.exit(1);
        }
    }
}
