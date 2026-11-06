package photopaint;

import photopaint.i18n.I18n;
import photopaint.model.Document;
import photopaint.model.History;
import photopaint.tools.BrushTool;
import photopaint.tools.EllipseTool;
import photopaint.tools.EraserTool;
import photopaint.tools.FillTool;
import photopaint.tools.GradientTool;
import photopaint.tools.HandTool;
import photopaint.tools.LassoTool;
import photopaint.tools.LineTool;
import photopaint.tools.MoveTool;
import photopaint.tools.PencilTool;
import photopaint.tools.PickerTool;
import photopaint.tools.RectTool;
import photopaint.tools.SelectTool;
import photopaint.tools.TextTool;
import photopaint.tools.Tool;
import photopaint.tools.ZoomTool;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Central editor state: document, history, colors, tool and tool options. */
public class Editor {

    public interface Listener {
        void editorChanged(String what);
    }

    public Document doc;
    public final History history = new History();
    public final ColorState colors = new ColorState();
    public final List<Tool> tools = new ArrayList<>();
    public Tool tool;
    public final List<Listener> listeners = new ArrayList<>();

    // tool options
    public int brushSize = 18;
    public int pencilSize = 2;
    public int eraserSize = 28;
    public int hardness = 70;
    public int opacity = 100;
    public int tolerance = 35;
    public int lineWidth = 3;
    public int fillMode = 0; // 0 outline, 1 fill, 2 both
    public boolean antialias = true;
    public int gradientType = 0; // 0 linear, 1 radial
    public boolean gradientTransparent = true;
    public String fontFamily = "Tahoma";
    public int fontSize = 48;
    public boolean bold = false;
    public boolean italic = false;
    public boolean grid = false;
    public int zoom = 100;

    public Editor() {
        doc = new Document(900, 650);
        history.reset(I18n.t("Новый документ"), doc);
        tools.add(new SelectTool());
        tools.add(new LassoTool());
        tools.add(new MoveTool());
        tools.add(new PencilTool());
        tools.add(new BrushTool());
        tools.add(new EraserTool());
        tools.add(new FillTool());
        tools.add(new GradientTool());
        tools.add(new LineTool());
        tools.add(new RectTool());
        tools.add(new EllipseTool());
        tools.add(new TextTool());
        tools.add(new PickerTool());
        tools.add(new ZoomTool());
        tools.add(new HandTool());
        tool = toolById("pencil");
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void fire(String what) {
        for (Listener l : new ArrayList<>(listeners)) l.editorChanged(what);
    }

    public void setDocument(Document d, String historyName) {
        doc = d;
        history.reset(historyName, d);
        fire("newdoc");
    }

    public void setTool(Tool t) {
        if (t != tool) {
            tool = t;
            fire("tool");
        }
    }

    public Tool toolById(String id) {
        for (Tool t : tools) if (t.id.equals(id)) return t;
        return null;
    }

    public void setForeground(Color c) {
        colors.setForeground(c);
        fire("colors");
    }

    public void setBackground(Color c) {
        colors.setBackground(c);
        fire("colors");
    }

    /** Records a finished action in the history and marks the document dirty. */
    public void commit(String name) {
        doc.dirty = true;
        doc.invalidate();
        history.push(name, doc);
        fire("doc");
    }

    public void touch() {
        doc.invalidate();
        fire("canvas");
    }

    public void undo() {
        if (!history.canUndo()) return;
        history.undo(doc);
        doc.dirty = true;
        doc.clearPreview();
        fire("doc");
    }

    public void redo() {
        if (!history.canRedo()) return;
        history.redo(doc);
        doc.dirty = true;
        doc.clearPreview();
        fire("doc");
    }

    public void jumpHistory(int index) {
        if (index < 0 || index == history.getIndex()) return;
        history.jump(index, doc);
        doc.dirty = true;
        doc.clearPreview();
        fire("doc");
    }

    public Rectangle selection() {
        return doc.selectionOrNull();
    }

    public void deleteSelection() {
        Rectangle sel = doc.selectionOrNull();
        if (sel == null) return;
        photopaint.tools.ToolPaint.clearShape(doc.activeImage(), doc.clipShape());
        commit(I18n.t("Удаление"));
    }

    public void selectAll() {
        doc.setSelection(doc.bounds());
        fire("selection");
    }

    public void deselect() {
        doc.clearSelection();
        fire("selection");
    }
}
