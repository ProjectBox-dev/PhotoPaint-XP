package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.ChooserButton;
import photopaint.ui.Icons;
import photopaint.ui.XpButtonUI;
import photopaint.ui.XpDialog;
import photopaint.util.ImageFiles;
import photopaint.util.ProjectFiles;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** XP style open / save file dialog. */
public class XpFileDialog extends XpDialog {

    public enum Mode {OPEN, SAVE}

    private static File lastDir = defaultDir();

    private final Mode mode;
    private File currentDir = lastDir;
    private final DefaultListModel<File> model = new DefaultListModel<>();
    private final JList<File> list = new JList<>(model);
    private final JTextField nameField = new JTextField(26);
    private final JTextField pathField = new JTextField();
    private final ChooserButton<String> typeChooser;
    private final JLabel error = new JLabel(" ");
    private File result;

    private static File defaultDir() {
        File pictures = new File(System.getProperty("user.home"), "Pictures");
        if (pictures.isDirectory()) return pictures;
        return new File(System.getProperty("user.home"));
    }

    public XpFileDialog(Window owner, Mode mode, String fileName) {
        super(owner, mode == Mode.OPEN ? I18n.t("Открыть") : I18n.t("Сохранить как"));
        this.mode = mode;

        JPanel top = new JPanel(new BorderLayout(5, 0));
        top.setOpaque(false);
        JLabel look = new JLabel(I18n.t("Папка:"));
        top.add(look, BorderLayout.WEST);
        pathField.setEditable(false);
        pathField.setFont(Theme.ui());
        pathField.setBackground(Color.WHITE);
        pathField.setBorder(Theme.sunkenBorder());
        top.add(pathField, BorderLayout.CENTER);
        JButton up = new JButton(Icons.small("upFolder"));
        up.setUI(new XpButtonUI());
        up.setToolTipText(I18n.t("На уровень вверх"));
        up.setPreferredSize(new Dimension(28, 23));
        up.addActionListener(e -> goUp());
        top.add(up, BorderLayout.EAST);
        body().add(top, BorderLayout.NORTH);

        JPanel places = new JPanel();
        places.setOpaque(false);
        places.setLayout(new javax.swing.BoxLayout(places, javax.swing.BoxLayout.Y_AXIS));
        places.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 6));
        places.setPreferredSize(new Dimension(132, 300));
        places.add(place(I18n.t("Мои рисунки"), "folder",
                new File(System.getProperty("user.home"), "Pictures")));
        places.add(place(I18n.t("Рабочий стол"), "home",
                new File(System.getProperty("user.home"), "Desktop")));
        places.add(place(I18n.t("Документы"), "file",
                new File(System.getProperty("user.home"), "Documents")));
        places.add(place(I18n.t("Мой компьютер"), "drive", null));

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(places, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(21);
        list.setBackground(Color.WHITE);
        list.setCellRenderer(new Renderer());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) accept();
            }
        });
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            File f = list.getSelectedValue();
            if (f != null && !f.isDirectory()) {
                nameField.setText(f.getName());
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(Theme.sunkenBorder());

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(left, BorderLayout.WEST);
        center.add(scroll, BorderLayout.CENTER);
        body().add(center, BorderLayout.CENTER);

        List<String> types = mode == Mode.OPEN
                ? Arrays.asList(I18n.t("Все поддерживаемые"), "PhotoPaint XP (*.ppxp)", "PNG (*.png)",
                "JPEG (*.jpg)", "BMP (*.bmp)", "GIF (*.gif)", I18n.t("Все файлы"))
                : Arrays.asList("PhotoPaint XP (*.ppxp)", "PNG (*.png)", "JPEG (*.jpg)", "BMP (*.bmp)");
        typeChooser = new ChooserButton<>(types, s -> s, types.get(0));
        typeChooser.onChange(v -> refreshList());
        nameField.setFont(Theme.ui());
        if (fileName != null) nameField.setText(fileName);
        nameField.addActionListener(e -> accept());

        JPanel fileNameRow = new JPanel(new BorderLayout(6, 0));
        fileNameRow.setOpaque(false);
        fileNameRow.add(new JLabel(I18n.t("Имя файла:")), BorderLayout.WEST);
        fileNameRow.add(nameField, BorderLayout.CENTER);

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        typeRow.setOpaque(false);
        typeRow.add(new JLabel(I18n.t("Тип файлов:")));
        typeRow.add(typeChooser);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        JPanel nameWrap = new JPanel(new BorderLayout(0, 4));
        nameWrap.setOpaque(false);
        nameWrap.add(fileNameRow, BorderLayout.NORTH);
        nameWrap.add(typeRow, BorderLayout.CENTER);
        south.add(nameWrap, BorderLayout.NORTH);
        error.setForeground(new Color(0xB00000));
        error.setFont(Theme.uiSmall());
        south.add(error, BorderLayout.SOUTH);
        body().add(south, BorderLayout.SOUTH);

        body().setPreferredSize(new Dimension(670, 430));
        setPreferredSize(new Dimension(700, 520));

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(mode == Mode.OPEN ? I18n.t("Открыть") : I18n.t("Сохранить"), true, this::accept);
        refreshList();
    }

    private JButton place(String text, String icon, File dir) {
        JButton b = new JButton(text, Icons.small(icon));
        b.setUI(new XpButtonUI());
        b.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        b.setFont(Theme.ui());
        b.setMaximumSize(new Dimension(126, 26));
        b.setPreferredSize(new Dimension(126, 26));
        b.setMargin(new java.awt.Insets(2, 6, 2, 2));
        b.addActionListener(e -> navigate(dir));
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 1));
        wrap.setOpaque(false);
        wrap.add(b);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(wrap, BorderLayout.WEST);
        JButton proxy = b;
        // return a button; layout adds it directly
        return proxy;
    }

    private void navigate(File dir) {
        if (dir != null && !dir.isDirectory()) return;
        currentDir = dir;
        refreshList();
    }

    private void goUp() {
        if (currentDir == null) return;
        File parent = currentDir.getParentFile();
        currentDir = parent;
        refreshList();
    }

    private void refreshList() {
        model.clear();
        if (currentDir == null) {
            pathField.setText(I18n.t("Мой компьютер"));
            File[] roots = File.listRoots();
            if (roots != null) {
                for (File r : roots) model.addElement(r);
            }
            return;
        }
        if (!currentDir.isDirectory()) currentDir = defaultDir();
        pathField.setText(currentDir.getAbsolutePath());
        File[] files = currentDir.listFiles();
        if (files == null) files = new File[0];
        List<File> dirs = new ArrayList<>();
        List<File> plain = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) dirs.add(f);
            else if (matches(f)) plain.add(f);
        }
        dirs.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
        plain.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
        for (File f : dirs) model.addElement(f);
        for (File f : plain) model.addElement(f);
    }

    private boolean matches(File f) {
        String type = typeChooser.getValue();
        if (type.equals(I18n.t("Все файлы"))) return true;
        String n = f.getName().toLowerCase();
        if (type.equals(I18n.t("Все поддерживаемые"))) {
            return ImageFiles.isSupported(f) || ProjectFiles.isProject(f);
        }
        if (type.contains("PhotoPaint XP")) return n.endsWith(".ppxp");
        if (type.contains("PNG")) return n.endsWith(".png");
        if (type.contains("JPEG")) return n.endsWith(".jpg") || n.endsWith(".jpeg");
        if (type.contains("BMP")) return n.endsWith(".bmp");
        if (type.contains("GIF")) return n.endsWith(".gif");
        return true;
    }

    private void accept() {
        File selected = list.getSelectedValue();
        File baseDir = currentDir;
        if (selected != null && selected.isDirectory()) {
            navigate(selected);
            return;
        }
        String typed = nameField.getText().trim();
        if (typed.isEmpty() && selected != null) typed = selected.getName();
        if (typed.isEmpty()) {
            error.setText(I18n.t("Укажите имя файла."));
            return;
        }
        if (baseDir == null) {
            error.setText(I18n.t("Выберите диск и папку."));
            return;
        }
        if (mode == Mode.OPEN) {
            File f = new File(typed);
            if (!f.isAbsolute()) f = new File(baseDir, typed);
            if (!f.isFile()) {
                error.setText(I18n.t("Файл не найден."));
                return;
            }
            result = f;
            lastDir = f.getParentFile();
            dispose();
        } else {
            String ext = "";
            String type = typeChooser.getValue();
            if (type.contains("PhotoPaint XP")) ext = ".ppxp";
            else if (type.contains("PNG")) ext = ".png";
            else if (type.contains("JPEG")) ext = ".jpg";
            else if (type.contains("BMP")) ext = ".bmp";
            if (!ext.isEmpty()) typed = ensureExt(typed, ext);
            File f = new File(baseDir, typed);
            if (f.exists()) {
                if (!confirmOverwrite(f)) return;
            }
            result = f;
            lastDir = f.getParentFile();
            dispose();
        }
    }

    private String ensureExt(String name, String ext) {
        String lower = name.toLowerCase();
        for (String e : new String[]{".ppxp", ".png", ".jpg", ".jpeg", ".bmp", ".gif"}) {
            if (lower.endsWith(e)) {
                return name.substring(0, name.length() - e.length()) + ext;
            }
        }
        return name + ext;
    }

    private boolean confirmOverwrite(File f) {
        XpDialog d = new XpDialog(SwingUtilities.getWindowAncestor(this), I18n.t("Подтверждение"));
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(new JLabel(Icons.icon("info", 32)), BorderLayout.WEST);
        JLabel l = new JLabel(I18n.t("<html>Файл <b>") + f.getName()
                + I18n.t("</b> уже существует.<br>Заменить его?</html>"));
        l.setFont(Theme.ui());
        p.add(l, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(330, 54));
        d.body().add(p, BorderLayout.CENTER);
        final boolean[] ok = {false};
        d.addButton(I18n.t("Отмена"), false, d::dispose);
        d.addButton(I18n.t("Заменить"), true, () -> {
            ok[0] = true;
            d.dispose();
        });
        d.showCentered();
        return ok[0];
    }

    public File showDialog() {
        showCentered();
        return result;
    }

    private class Renderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                      boolean selected, boolean focus) {
            super.getListCellRendererComponent(l, value, index, selected, focus);
            File f = (File) value;
            if (currentDir == null) {
                setText(f.getAbsolutePath());
                setIcon(Icons.small("drive"));
            } else if (f.isDirectory()) {
                setText(f.getName());
                setIcon(Icons.small("folder"));
            } else {
                setText(f.getName());
                setIcon(Icons.small("file"));
            }
            setFont(Theme.ui());
            setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
            if (selected) {
                setBackground(Theme.SELECTION);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(Theme.TEXT);
            }
            return this;
        }
    }
}
