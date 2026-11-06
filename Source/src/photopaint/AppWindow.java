package photopaint;

import photopaint.i18n.I18n;
import photopaint.filters.Filters;
import photopaint.model.Document;
import photopaint.model.Layer;
import photopaint.tools.Tool;
import photopaint.ui.CanvasPanel;
import photopaint.ui.ColorPanel;
import photopaint.ui.DockPanel;
import photopaint.ui.HistoryPanel;
import photopaint.ui.Icons;
import photopaint.ui.LayersPanel;
import photopaint.ui.OptionsBar;
import photopaint.ui.PaletteBar;
import photopaint.ui.StatusBar;
import photopaint.ui.ToolsPanel;
import photopaint.ui.XpFrame;
import photopaint.ui.XpLaf;
import photopaint.ui.XpButtonUI;
import photopaint.ui.dialogs.AboutDialog;
import photopaint.ui.dialogs.AdjustDialog;
import photopaint.ui.dialogs.ColorPickerDialog;
import photopaint.ui.dialogs.ConfirmDialog;
import photopaint.ui.dialogs.FilterDialog;
import photopaint.ui.dialogs.NewImageDialog;
import photopaint.ui.dialogs.ResizeDialog;
import photopaint.ui.dialogs.SettingsDialog;
import photopaint.ui.dialogs.XpFileDialog;
import photopaint.util.ImageFiles;
import photopaint.util.ProjectFiles;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Main PhotoPaint XP window. */
public class AppWindow extends XpFrame {

    private final Editor ed;
    private CanvasPanel canvas;
    private JScrollPane scroll;
    private ToolsPanel tools;
    private OptionsBar options;
    private LayersPanel layersPanel;
    private HistoryPanel historyPanel;
    private ColorPanel colorPanel;
    private StatusBar status;
    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private JMenuItem cropItem;
    private JCheckBoxMenuItem gridItem;
    private BufferedImage clipboardImage;

    public AppWindow() {
        super("PhotoPaint XP");
        ed = new Editor();
        ed.addListener(this::onEditorChanged);
        setIconImage(appImage());
        buildUi();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), "closeApp");
        getRootPane().getActionMap().put("closeApp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApp();
            }
        });

        setSize(1180, 790);
        setLocationRelativeTo(null);
        updateWindowState();
    }

    /**
     * Builds (or rebuilds) the whole window contents. Rebuilding is what makes a
     * live language switch possible without touching the document or its history.
     */
    private void buildUi() {
        java.awt.Point viewPos = scroll != null ? scroll.getViewport().getViewPosition() : null;
        content().removeAll();

        canvas = new CanvasPanel(ed);
        scroll = new JScrollPane(canvas);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.WORKSPACE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);

        tools = new ToolsPanel(ed);
        options = new OptionsBar(ed);
        layersPanel = new LayersPanel(ed);
        historyPanel = new HistoryPanel(ed);
        colorPanel = new ColorPanel(ed);
        status = new StatusBar();

        DockPanel dock = new DockPanel();
        dock.addTab(I18n.t("Слои"), layersPanel);
        dock.addTab(I18n.t("История"), historyPanel);
        dock.addTab(I18n.t("Цвет"), colorPanel);

        JPanel top = new JPanel(new BorderLayout());
        top.add(buildMenuBar(), BorderLayout.NORTH);
        top.add(options, BorderLayout.SOUTH);
        content().add(top, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(tools, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        main.add(dock, BorderLayout.EAST);
        content().add(main, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(new PaletteBar(ed), BorderLayout.NORTH);
        south.add(status, BorderLayout.SOUTH);
        content().add(south, BorderLayout.SOUTH);

        canvas.setCursorListener((x, y) -> status.setCursor(x, y));
        status.setDocSize(ed.doc.width, ed.doc.height);
        status.setZoom(ed.zoom);
        status.setSelection(ed.doc.selectionOrNull());
        status.setHint(I18n.t(ed.tool.name) + " — " + I18n.t(ed.tool.hint));

        bindToolKeys();
        content().revalidate();
        content().repaint();
        updateWindowState();
        if (viewPos != null) {
            SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(viewPos));
        }
    }

    public void afterShown() {
        canvas.requestFocusInWindow();
        SwingUtilities.invokeLater(this::fitInitialZoom);
    }

    private Image appImage() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Icons.appIcon(32).paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new XpLaf.XpMenuBar();

        JMenu file = menu(bar, I18n.t("Файл"));
        item(file, I18n.t("Новый..."), "control N", this::newDocument);
        item(file, I18n.t("Открыть..."), "control O", this::openDocument);
        file.addSeparator();
        item(file, I18n.t("Сохранить"), "control S", this::save);
        item(file, I18n.t("Сохранить как..."), "control shift S", this::saveAs);
        file.addSeparator();
        item(file, I18n.t("Выход"), "alt F4", this::exitApp);

        JMenu edit = menu(bar, I18n.t("Правка"));
        undoItem = item(edit, I18n.t("Отменить"), "control Z", ed::undo);
        redoItem = item(edit, I18n.t("Повторить"), "control Y", ed::redo);
        edit.addSeparator();
        item(edit, I18n.t("Вырезать"), "control X", this::cut);
        item(edit, I18n.t("Копировать"), "control C", this::copy);
        item(edit, I18n.t("Вставить"), "control V", this::paste);
        item(edit, I18n.t("Удалить"), "DELETE", this::delete);
        edit.addSeparator();
        item(edit, I18n.t("Выделить всё"), "control A", this::selectAll);
        item(edit, I18n.t("Снять выделение"), "control D", this::deselect);
        edit.addSeparator();
        item(edit, I18n.t("Настройки..."), null, this::openSettings);

        JMenu image = menu(bar, I18n.t("Изображение"));
        item(image, I18n.t("Размер изображения..."), null, this::resizeImage);
        image.addSeparator();
        item(image, I18n.t("Повернуть на 90° по часовой"), null, () -> rotate(true));
        item(image, I18n.t("Повернуть на 90° против часовой"), null, () -> rotate(false));
        item(image, I18n.t("Повернуть на 180°"), null, this::rotate180);
        item(image, I18n.t("Отразить по горизонтали"), null, () -> flip(true));
        item(image, I18n.t("Отразить по вертикали"), null, () -> flip(false));
        image.addSeparator();
        cropItem = item(image, I18n.t("Обрезать по выделению"), null, this::crop);
        item(image, I18n.t("Свести изображение"), null, this::flatten);

        JMenu layer = menu(bar, I18n.t("Слой"));
        item(layer, I18n.t("Новый слой"), null, () -> layerAction("add"));
        item(layer, I18n.t("Импортировать изображение..."), null, this::importLayer);
        item(layer, I18n.t("Дублировать слой"), null, () -> layerAction("duplicate"));
        item(layer, I18n.t("Удалить слой"), null, () -> layerAction("delete"));
        item(layer, I18n.t("Объединить с нижним"), null, () -> layerAction("merge"));
        layer.addSeparator();
        item(layer, I18n.t("Поднять слой"), null, () -> layerAction("up"));
        item(layer, I18n.t("Опустить слой"), null, () -> layerAction("down"));

        JMenu adjust = menu(bar, I18n.t("Коррекция"));
        item(adjust, I18n.t("Яркость / контраст..."), null, this::brightnessContrast);
        item(adjust, I18n.t("Оттенок / насыщенность..."), null, this::hueSaturation);
        adjust.addSeparator();
        item(adjust, I18n.t("Оттенки серого"), null, () -> directFilter(I18n.t("Оттенки серого"),
                (img, clip) -> Filters.desaturate(img, clip)));
        item(adjust, I18n.t("Инвертировать"), "control I", () -> directFilter(I18n.t("Инверсия"),
                (img, clip) -> Filters.invert(img, clip)));
        item(adjust, I18n.t("Сепия"), null, () -> directFilter(I18n.t("Сепия"),
                (img, clip) -> Filters.sepia(img, clip)));

        JMenu filter = menu(bar, I18n.t("Фильтр"));
        item(filter, I18n.t("Размытие..."), null, () -> filterDialog(I18n.t("Размытие"), I18n.t("Радиус:"),
                0, 20, 3, I18n.t("Размытие"), Filters::blur));
        item(filter, I18n.t("Резкость..."), null, () -> filterDialog(I18n.t("Резкость"), I18n.t("Сила:"),
                0, 200, 60, I18n.t("Резкость"), Filters::sharpen));
        item(filter, I18n.t("Пикселизация..."), null, () -> filterDialog(I18n.t("Пикселизация"), I18n.t("Блок:"),
                2, 60, 8, I18n.t("Пикселизация"), Filters::pixelate));
        item(filter, I18n.t("Шум..."), null, () -> filterDialog(I18n.t("Шум"), I18n.t("Сила:"),
                0, 100, 20, I18n.t("Шум"), Filters::noise));
        filter.addSeparator();
        item(filter, I18n.t("Тиснение"), null, () -> directFilter(I18n.t("Тиснение"),
                (img, clip) -> Filters.emboss(img, clip)));
        item(filter, I18n.t("Выделение краёв"), null, () -> directFilter(I18n.t("Выделение краёв"),
                (img, clip) -> Filters.edgeDetect(img, clip)));

        JMenu view = menu(bar, I18n.t("Вид"));
        item(view, I18n.t("Увеличить"), "control EQUALS", this::zoomIn);
        item(view, I18n.t("Уменьшить"), "control MINUS", this::zoomOut);
        item(view, I18n.t("Реальный размер"), "control 1", () -> canvas.setZoomPercent(100));
        item(view, I18n.t("По размеру окна"), "control 0", canvas::fitToWindow);
        view.addSeparator();
        gridItem = new JCheckBoxMenuItem(I18n.t("Показать сетку"));
        gridItem.setFont(Theme.ui());
        gridItem.setSelected(ed.grid);
        gridItem.addActionListener(e -> {
            ed.grid = gridItem.isSelected();
            canvas.repaint();
        });
        view.add(gridItem);

        JMenu help = menu(bar, I18n.t("Справка"));
        item(help, I18n.t("О программе PhotoPaint XP"), null, () ->
                new AboutDialog(this).showDialog());

        return bar;
    }

    private JMenu menu(JMenuBar bar, String text) {
        JMenu m = new JMenu(text);
        m.setFont(Theme.ui());
        m.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8));
        bar.add(m);
        return m;
    }

    private JMenuItem item(JMenu menu, String text, String accel, Runnable action) {
        JMenuItem it = new JMenuItem(text);
        it.setFont(Theme.ui());
        if (accel != null) {
            try {
                it.setAccelerator(KeyStroke.getKeyStroke(accel));
            } catch (Exception ignored) {
            }
        }
        it.addActionListener(e -> action.run());
        menu.add(it);
        return it;
    }

    private void bindToolKeys() {
        InputMap im = canvas.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = canvas.getActionMap();
        for (Tool t : ed.tools) {
            String key = "tool-" + t.id;
            char c = t.shortcut.toLowerCase().charAt(0);
            // Bind as KEY_PRESSED without modifiers: a KEY_TYPED stroke would also
            // match Ctrl+Z / Alt+... and switch tools instead of running the menu action.
            int code = KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(c));
            im.put(KeyStroke.getKeyStroke(code, 0), key);
            am.put(key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ed.setTool(t);
                }
            });
        }
    }

    private void onEditorChanged(String what) {
        switch (what) {
            case "newdoc" -> {
                ed.zoom = 100;
                canvas.revalidate();
                canvas.repaint();
                layersPanel.refresh();
                historyPanel.refresh();
                options.rebuild();
                status.setDocSize(ed.doc.width, ed.doc.height);
                status.setZoom(ed.zoom);
                status.setSelection(ed.doc.selectionOrNull());
                updateWindowState();
                SwingUtilities.invokeLater(() -> {
                    fitInitialZoom();
                    canvas.centerView();
                });
                if (ed.doc.file != null) {
                    status.setHint(I18n.t("Открыт: ") + ed.doc.file.getName());
                } else {
                    status.setHint(I18n.t("Новый документ ") + ed.doc.width + " × " + ed.doc.height);
                }
            }
            case "doc" -> {
                canvas.revalidate();
                canvas.repaint();
                layersPanel.refresh();
                historyPanel.refresh();
                status.setDocSize(ed.doc.width, ed.doc.height);
                status.setSelection(ed.doc.selectionOrNull());
                updateWindowState();
            }
            case "canvas" -> canvas.repaint();
            case "selection" -> {
                status.setSelection(ed.doc.selectionOrNull());
                if (cropItem != null) cropItem.setEnabled(ed.doc.selectionOrNull() != null);
                canvas.repaint();
            }
            case "tool" -> {
                tools.refreshTool();
                options.rebuild();
                canvas.toolChanged();
                status.setHint(I18n.t(ed.tool.name) + " — " + I18n.t(ed.tool.hint));
            }
            case "colors" -> {
                tools.refreshColors();
                colorPanel.refresh();
                options.refreshColors();
            }
            case "zoom" -> {
                status.setZoom(ed.zoom);
                canvas.repaint();
            }
            case "zoom100" -> canvas.setZoomPercent(100);
            case "fit" -> canvas.fitToWindow();
            default -> canvas.repaint();
        }
    }

    private void updateWindowState() {
        String name = ed.doc.file != null ? ed.doc.file.getName() : I18n.t("Безымянный");
        setTitle("PhotoPaint XP — " + name + (ed.doc.dirty ? " *" : ""));
        boolean canUndo = ed.history.canUndo();
        boolean canRedo = ed.history.canRedo();
        if (undoItem != null) {
            undoItem.setEnabled(canUndo);
            undoItem.setText(canUndo ? I18n.t("Отменить: ") + ed.history.undoName() : I18n.t("Отменить"));
        }
        if (redoItem != null) {
            redoItem.setEnabled(canRedo);
            redoItem.setText(canRedo ? I18n.t("Повторить: ") + ed.history.redoName() : I18n.t("Повторить"));
        }
        if (cropItem != null) cropItem.setEnabled(ed.doc.selectionOrNull() != null);
    }

    private void fitInitialZoom() {
        Dimension ext = scroll.getViewport().getExtentSize();
        if (ext.width <= 0 || ext.height <= 0) {
            canvas.setZoomPercent(100);
            return;
        }
        if (ed.doc.width > ext.width - CanvasPanel.MARGIN * 2
                || ed.doc.height > ext.height - CanvasPanel.MARGIN * 2) {
            canvas.fitToWindow();
        } else {
            canvas.setZoomPercent(100);
        }
    }

    // ---- settings ----------------------------------------------------------
    private void openSettings() {
        I18n.Lang chosen = new SettingsDialog(this, I18n.language()).showDialog();
        if (chosen == null || chosen == I18n.language()) return;
        Settings.writeLanguage(chosen);
        I18n.setLanguage(chosen);
        buildUi();
    }

    // ---- files -------------------------------------------------------------
    private void newDocument() {
        if (!confirmDiscard()) return;
        NewImageDialog.Result r = new NewImageDialog(this, 900, 650).showDialog();
        if (r == null) return;
        Document d = new Document(r.width(), r.height(), I18n.t("Фон"), r.background());
        ed.setDocument(d, I18n.t("Новый документ"));
    }

    private void openDocument() {
        if (!confirmDiscard()) return;
        XpFileDialog dlg = new XpFileDialog(this, XpFileDialog.Mode.OPEN, null);
        File f = dlg.showDialog();
        if (f == null) return;
        openDocumentFile(f);
    }

    /** Opens a file passed on the command line, e.g. by a double click in Explorer. */
    public void openFile(File f) {
        if (f == null || !f.isFile()) return;
        openDocumentFile(f);
    }

    private void openDocumentFile(File f) {
        try {
            Document d;
            if (ProjectFiles.isProject(f)) {
                d = ProjectFiles.read(f);
                d.file = f;
                ed.setDocument(d, I18n.t("Открыт проект: ") + f.getName());
            } else {
                BufferedImage img = ImageFiles.read(f);
                d = new Document(img, f.getName());
                d.file = f;
                ed.setDocument(d, I18n.t("Открыт: ") + f.getName());
            }
        } catch (IOException ex) {
            error(I18n.t("Открыть изображение"), ex.getMessage());
        }
    }

    private void save() {
        if (ed.doc.file == null) {
            saveAs();
            return;
        }
        writeFile(ed.doc.file);
    }

    private void saveAs() {
        String suggested = ed.doc.file != null ? ed.doc.file.getName()
                : I18n.t("Безымянный.") + ProjectFiles.EXTENSION;
        XpFileDialog dlg = new XpFileDialog(this, XpFileDialog.Mode.SAVE, suggested);
        File f = dlg.showDialog();
        if (f == null) return;
        writeFile(f);
    }

    private void writeFile(File f) {
        try {
            if (ProjectFiles.isProject(f)) {
                ProjectFiles.write(ed.doc, f);
            } else {
                BufferedImage img = ed.doc.compositeForSave();
                ImageFiles.write(img, f);
            }
            ed.doc.file = f;
            ed.doc.dirty = false;
            updateWindowState();
            status.setHint(I18n.t("Сохранено: ") + f.getName());
        } catch (IOException ex) {
            error(I18n.t("Сохранить изображение"), ex.getMessage());
        }
    }

    private boolean confirmDiscard() {
        if (!ed.doc.dirty) return true;
        String name = ed.doc.file != null ? ed.doc.file.getName() : I18n.t("Безымянный");
        ConfirmDialog d = new ConfirmDialog(this, "PhotoPaint XP",
                I18n.t("Документ «") + name + I18n.t("» содержит несохранённые изменения.<br>")
                        + I18n.t("Сохранить его перед продолжением?"));
        int r = d.showDialog();
        if (r == ConfirmDialog.CANCEL) return false;
        if (r == ConfirmDialog.SAVE) {
            save();
            return !ed.doc.dirty;
        }
        return true;
    }

    private void exitApp() {
        if (confirmDiscard()) {
            System.exit(0);
        }
    }

    private void error(String title, String message) {
        photopaint.ui.XpDialog d = new photopaint.ui.XpDialog(this, title);
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(new JLabel(Icons.icon("info", 32)), BorderLayout.WEST);
        JLabel l = new JLabel("<html><div style='width:320px'>" + message + "</div></html>");
        l.setFont(Theme.ui());
        p.add(l, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(350, 60));
        d.body().add(p, BorderLayout.CENTER);
        d.addButton(I18n.t("ОК"), true, d::dispose);
        d.showCentered();
    }

    // ---- edit --------------------------------------------------------------
    private void copy() {
        Rectangle sel = ed.doc.selectionOrNull();
        Shape outline = ed.doc.selectionShape();
        if (sel == null) sel = ed.doc.bounds();
        BufferedImage img = new BufferedImage(sel.width, sel.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(ed.doc.composite(), -sel.x, -sel.y, null);
        g.dispose();
        if (outline != null) {
            photopaint.tools.ToolPaint.keepOnly(img, outline, sel.x, sel.y);
        }
        clipboardImage = img;
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            cb.setContents(new ImageTransferable(img), null);
        } catch (Exception ignored) {
        }
        status.setHint(I18n.t("Скопировано: ") + sel.width + " × " + sel.height);
    }

    private void cut() {
        Rectangle sel = ed.doc.selectionOrNull();
        if (sel == null) return;
        copy();
        photopaint.tools.ToolPaint.clearShape(ed.doc.activeImage(), ed.doc.clipShape());
        ed.commit(I18n.t("Вырезать"));
    }

    private void paste() {
        BufferedImage img = clipboardImage;
        try {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
                Object o = cb.getData(DataFlavor.imageFlavor);
                if (o instanceof BufferedImage bi) img = ImageFiles.toArgb(bi);
            }
        } catch (Exception ignored) {
        }
        if (img == null) {
            status.setHint(I18n.t("Буфер обмена пуст"));
            return;
        }
        Layer layer = ed.doc.addLayer(I18n.t("Вставка"), true);
        Graphics2D g = layer.image.createGraphics();
        Rectangle sel = ed.doc.selectionOrNull();
        int x = sel != null ? sel.x : Math.max(0, (ed.doc.width - img.getWidth()) / 2);
        int y = sel != null ? sel.y : Math.max(0, (ed.doc.height - img.getHeight()) / 2);
        g.drawImage(img, x, y, null);
        g.dispose();
        ed.commit(I18n.t("Вставка"));
    }

    /** Loads a picture from disk into a new transparent layer, scaled down to fit. */
    private void importLayer() {
        XpFileDialog dlg = new XpFileDialog(this, XpFileDialog.Mode.OPEN, null);
        File f = dlg.showDialog();
        if (f == null) return;
        try {
            BufferedImage img = ProjectFiles.isProject(f)
                    ? ProjectFiles.read(f).composite()
                    : ImageFiles.read(f);

            int w = ed.doc.width;
            int h = ed.doc.height;
            BufferedImage placed = img;
            if (img.getWidth() > w || img.getHeight() > h) {
                double k = Math.min(w / (double) img.getWidth(), h / (double) img.getHeight());
                int nw = Math.max(1, (int) Math.round(img.getWidth() * k));
                int nh = Math.max(1, (int) Math.round(img.getHeight() * k));
                placed = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = placed.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(img, 0, 0, nw, nh, null);
                g.dispose();
            }

            String name = f.getName();
            int dot = name.lastIndexOf('.');
            if (dot > 0) name = name.substring(0, dot);

            Layer layer = ed.doc.addLayer(name.isEmpty() ? I18n.t("Импорт") : name, true);
            Graphics2D g = layer.image.createGraphics();
            g.drawImage(placed, (w - placed.getWidth()) / 2, (h - placed.getHeight()) / 2, null);
            g.dispose();
            ed.commit(I18n.t("Импорт изображения"));
            status.setHint(I18n.t("Импортировано в новый слой: ") + f.getName());
        } catch (IOException ex) {
            error(I18n.t("Импортировать изображение"), ex.getMessage());
        }
    }

    private void delete() {
        Rectangle sel = ed.doc.selectionOrNull();
        if (sel != null) {
            photopaint.tools.ToolPaint.clearShape(ed.doc.activeImage(), ed.doc.clipShape());
            ed.commit(I18n.t("Удаление"));
        } else {
            photopaint.tools.ToolPaint.clearRegion(ed.doc.activeImage(), ed.doc.bounds());
            ed.commit(I18n.t("Очистка слоя"));
        }
    }

    private void selectAll() {
        ed.selectAll();
    }

    private void deselect() {
        ed.deselect();
    }

    // ---- image ------------------------------------------------------------
    private void resizeImage() {
        Dimension d = new ResizeDialog(this, ed.doc.width, ed.doc.height).showDialog();
        if (d == null) return;
        ed.doc.resizeImage(d.width, d.height);
        ed.commit(I18n.t("Размер изображения"));
    }

    private void rotate(boolean clockwise) {
        ed.doc.rotate90(clockwise);
        ed.commit(I18n.t("Поворот"));
    }

    private void rotate180() {
        ed.doc.rotate180();
        ed.commit(I18n.t("Поворот 180°"));
    }

    private void flip(boolean horizontal) {
        ed.doc.flip(horizontal);
        ed.commit(I18n.t("Отражение"));
    }

    private void crop() {
        Rectangle sel = ed.doc.selectionOrNull();
        if (sel == null) return;
        ed.doc.cropTo(sel);
        ed.commit(I18n.t("Обрезка"));
    }

    private void flatten() {
        ed.doc.flatten();
        ed.commit(I18n.t("Сведение слоёв"));
    }

    private void layerAction(String action) {
        switch (action) {
            case "add" -> ed.doc.addLayer(I18n.t("Слой"), true);
            case "duplicate" -> ed.doc.duplicateActive();
            case "delete" -> ed.doc.deleteActive();
            case "merge" -> ed.doc.mergeDown();
            case "up" -> ed.doc.moveActive(-1);
            case "down" -> ed.doc.moveActive(1);
            default -> {
            }
        }
        ed.commit(switch (action) {
            case "add" -> I18n.t("Новый слой");
            case "duplicate" -> I18n.t("Дублировать слой");
            case "delete" -> I18n.t("Удалить слой");
            case "merge" -> I18n.t("Объединить слои");
            case "up" -> I18n.t("Поднять слой");
            default -> I18n.t("Опустить слой");
        });
    }

    // ---- adjustments and filters ------------------------------------------
    private void brightnessContrast() {
        new AdjustDialog(this, ed, false).showDialog();
    }

    private void hueSaturation() {
        new AdjustDialog(this, ed, true).showDialog();
    }

    private interface SimpleFilter {
        void apply(BufferedImage img, Shape clip);
    }

    private void directFilter(String name, SimpleFilter filter) {
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        filter.apply(img, ed.doc.clipShape());
        ed.commit(name);
    }

    private void filterDialog(String title, String label, int min, int max, int initial,
                              String actionName, FilterDialog.PixelFilter filter) {
        BufferedImage img = ed.doc.activeImage();
        if (img == null) return;
        new FilterDialog(this, ed, title, label, min, max, initial, actionName, filter)
                .showDialog();
    }

    private void zoomIn() {
        Rectangle r = canvas.getVisibleRect();
        canvas.zoomAt(r.x + r.width / 2, r.y + r.height / 2, 1);
    }

    private void zoomOut() {
        Rectangle r = canvas.getVisibleRect();
        canvas.zoomAt(r.x + r.width / 2, r.y + r.height / 2, -1);
    }

    private static class ImageTransferable implements Transferable {
        private final BufferedImage image;

        ImageTransferable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return image;
        }
    }
}
