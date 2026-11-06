package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.model.Layer;
import photopaint.ui.dialogs.InputDialog;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/** Layer list with an opacity slider. */
public class LayersPanel extends JPanel {

    private final Editor ed;
    private final DefaultListModel<Layer> model = new DefaultListModel<>();
    private final JList<Layer> list = new JList<>(model);
    private final JSlider opacity = new JSlider(0, 100, 100);
    private final JLabel opacityValue = new JLabel("100%");
    private boolean updating;
    private boolean opacityChanged;

    public LayersPanel(Editor ed) {
        this.ed = ed;
        setLayout(new BorderLayout(0, 4));
        setBackground(Theme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel top = new JPanel(new BorderLayout(4, 0));
        top.setOpaque(false);
        JLabel label = new JLabel(I18n.t("Непрозр.:"));
        label.setFont(Theme.ui());
        top.add(label, BorderLayout.WEST);
        opacity.setUI(new XpSliderUI(opacity));
        opacity.setPreferredSize(new Dimension(100, 20));
        top.add(opacity, BorderLayout.CENTER);
        opacityValue.setFont(Theme.uiSmall());
        opacityValue.setPreferredSize(new Dimension(38, 16));
        top.add(opacityValue, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        list.setCellRenderer(new Renderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(54);
        list.setBackground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(Theme.sunkenBorder());
        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        actions.setOpaque(false);
        actions.add(button("layerAdd", I18n.t("Новый слой")));
        actions.add(button("layerDuplicate", I18n.t("Дублировать слой")));
        actions.add(button("layerDelete", I18n.t("Удалить слой")));
        actions.add(button("up", I18n.t("Поднять слой")));
        actions.add(button("down", I18n.t("Опустить слой")));
        add(actions, BorderLayout.SOUTH);

        opacity.addChangeListener(e -> {
            if (updating) return;
            Layer l = ed.doc.activeLayer();
            if (l == null) return;
            l.opacity = opacity.getValue() / 100f;
            opacityValue.setText(opacity.getValue() + "%");
            ed.doc.invalidate();
            opacityChanged = true;
            ed.fire("canvas");
        });
        opacity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (opacityChanged) {
                    opacityChanged = false;
                    ed.commit(I18n.t("Непрозрачность слоя"));
                }
            }
        });

        list.addListSelectionListener(e -> {
            if (updating || e.getValueIsAdjusting()) return;
            int i = list.getSelectedIndex();
            if (i >= 0 && i != ed.doc.active) {
                ed.doc.active = i;
                syncOpacity();
                ed.fire("canvas");
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = list.locationToIndex(e.getPoint());
                if (row < 0) return;
                java.awt.Rectangle cell = list.getCellBounds(row, row);
                if (cell == null) return;
                if (e.getX() - cell.x < 22) {
                    Layer l = model.get(row);
                    l.visible = !l.visible;
                    ed.doc.invalidate();
                    ed.commit(I18n.t("Видимость слоя"));
                    return;
                }
                if (e.getClickCount() == 2) {
                    Layer l = ed.doc.activeLayer();
                    if (l == null) return;
                    String name = InputDialog.show(SwingUtilities.getWindowAncestor(LayersPanel.this),
                            I18n.t("Переименовать слой"), I18n.t("Имя слоя:"), l.name);
                    if (name != null && !name.isBlank()) {
                        l.name = name.trim();
                        ed.commit(I18n.t("Переименование слоя"));
                    }
                }
            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refresh();
            }
        });
        refresh();
    }

    private JButton button(String icon, String tip) {
        JButton b = new JButton(Icons.small(icon));
        b.setUI(new XpButtonUI());
        b.setPreferredSize(new Dimension(28, 23));
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        b.setToolTipText(tip);
        b.addActionListener(e -> {
            switch (icon) {
                case "layerAdd" -> ed.doc.addLayer(I18n.t("Слой"), true);
                case "layerDuplicate" -> ed.doc.duplicateActive();
                case "layerDelete" -> ed.doc.deleteActive();
                case "up" -> ed.doc.moveActive(-1);
                case "down" -> ed.doc.moveActive(1);
                default -> {
                }
            }
            ed.commit(switch (icon) {
                case "layerAdd" -> I18n.t("Новый слой");
                case "layerDuplicate" -> I18n.t("Дублировать слой");
                case "layerDelete" -> I18n.t("Удалить слой");
                case "up" -> I18n.t("Поднять слой");
                default -> I18n.t("Опустить слой");
            });
        });
        return b;
    }

    private void syncOpacity() {
        Layer l = ed.doc.activeLayer();
        if (l == null) return;
        updating = true;
        opacity.setValue(Math.round(l.opacity * 100));
        opacityValue.setText(Math.round(l.opacity * 100) + "%");
        updating = false;
    }

    public void refresh() {
        updating = true;
        model.clear();
        for (Layer l : ed.doc.layers) model.addElement(l);
        list.setSelectedIndex(ed.doc.active);
        if (ed.doc.active >= 0 && ed.doc.active < model.size()) {
            list.ensureIndexIsVisible(ed.doc.active);
        }
        Layer l = ed.doc.activeLayer();
        if (l != null) {
            opacity.setValue(Math.round(l.opacity * 100));
            opacityValue.setText(Math.round(l.opacity * 100) + "%");
        }
        updating = false;
        list.repaint();
    }

    private static class Renderer extends JPanel implements ListCellRenderer<Layer> {
        private Layer layer;
        private boolean selected;

        @Override
        public Component getListCellRendererComponent(JList<? extends Layer> list, Layer value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            layer = value;
            selected = isSelected;
            setBackground(isSelected ? Theme.SELECTION : Color.WHITE);
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(190, 54);
        }

        @Override
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            if (layer == null) return;
            Graphics2D g = (Graphics2D) gg.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (!selected) {
                g.setColor(new Color(0xF4F2E9));
                g.fillRect(0, h - 1, w, 1);
            }
            g.setColor(selected ? Color.WHITE : new Color(0x666666));
            Icons.small(layer.visible ? "eye" : "eyeOff").paintIcon(this, g, 3, h / 2 - 8);

            int tx = 22;
            int ty = 9;
            int tw = 42;
            int th = 34;
            for (int y = 0; y < th; y += 6) {
                for (int x = 0; x < tw; x += 6) {
                    g.setColor(((x / 6 + y / 6) & 1) == 0 ? Color.WHITE : new Color(0xD4D0C8));
                    g.fillRect(tx + x, ty + y, 6, 6);
                }
            }
            BufferedImage img = layer.image;
            if (img != null) {
                double sc = Math.min(tw / (double) img.getWidth(), th / (double) img.getHeight());
                int iw = Math.max(1, (int) Math.round(img.getWidth() * sc));
                int ih = Math.max(1, (int) Math.round(img.getHeight() * sc));
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(img, tx + (tw - iw) / 2, ty + (th - ih) / 2, iw, ih, null);
            }
            g.setColor(new Color(0x8A8678));
            g.drawRect(tx - 1, ty - 1, tw + 1, th + 1);

            g.setFont(Theme.ui());
            g.setColor(selected ? Color.WHITE : Theme.TEXT);
            String name = layer.name;
            FontMetrics fm = g.getFontMetrics();
            int max = w - tx - tw - 12;
            while (name.length() > 1 && fm.stringWidth(name) > max) {
                name = name.substring(0, name.length() - 2) + "…";
            }
            g.drawString(name, tx + tw + 6, h / 2 + fm.getAscent() / 2 - 1);
            g.setFont(Theme.uiSmall());
            g.setColor(selected ? new Color(0xE0E8F8) : new Color(0x7A7A7A));
            g.drawString(Math.round(layer.opacity * 100) + "%", tx + tw + 6,
                    h / 2 + fm.getAscent() / 2 + 12);
            g.dispose();
        }
    }
}
