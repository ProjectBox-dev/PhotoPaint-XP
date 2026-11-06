package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.model.History;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Clickable undo history panel. */
public class HistoryPanel extends JPanel {

    private final Editor ed;
    private final DefaultListModel<History.Entry> model = new DefaultListModel<>();
    private final JList<History.Entry> list = new JList<>(model);
    private final JLabel footer = new JLabel();
    private boolean updating;

    public HistoryPanel(Editor ed) {
        this.ed = ed;
        setLayout(new BorderLayout(0, 3));
        setBackground(Theme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        list.setCellRenderer(new Renderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(24);
        list.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(Theme.sunkenBorder());
        add(scroll, BorderLayout.CENTER);

        footer.setFont(Theme.uiSmall());
        footer.setForeground(new Color(0x555555));
        add(footer, BorderLayout.SOUTH);

        list.addListSelectionListener(e -> {
            if (updating || e.getValueIsAdjusting()) return;
            int i = list.getSelectedIndex();
            if (i >= 0) {
                if (i != ed.history.getIndex()) ed.jumpHistory(i);
            }
        });
        refresh();
    }

    public void refresh() {
        updating = true;
        model.clear();
        for (History.Entry e : ed.history.getEntries()) model.addElement(e);
        list.setSelectedIndex(ed.history.getIndex());
        if (ed.history.getIndex() >= 0) {
            list.ensureIndexIsVisible(ed.history.getIndex());
        }
        footer.setText(I18n.t("Шагов: ") + ed.history.getEntries().size() + I18n.t("   Память: ")
                + (ed.history.getTotalBytes() / (1024 * 1024)) + I18n.t(" МБ"));
        updating = false;
    }

    private class Renderer extends JPanel implements ListCellRenderer<History.Entry> {
        private History.Entry entry;
        private boolean selected;
        private boolean current;
        private int index;

        @Override
        public Component getListCellRendererComponent(JList<? extends History.Entry> list,
                                                      History.Entry value, int i, boolean isSelected,
                                                      boolean cellHasFocus) {
            entry = value;
            selected = isSelected;
            index = i;
            current = i == ed.history.getIndex();
            setBackground(isSelected ? Theme.SELECTION : Color.WHITE);
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(190, 24);
        }

        @Override
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            if (entry == null) return;
            Graphics2D g = (Graphics2D) gg.create();
            g.setColor(current ? new Color(0x2E6BC8) : new Color(0xB0ACA0));
            g.fillOval(6, getHeight() / 2 - 3, 6, 6);
            g.setColor(selected ? Color.WHITE : new Color(0x9A9688));
            g.drawOval(6, getHeight() / 2 - 3, 6, 6);
            g.setFont(Theme.ui());
            g.setColor(selected ? Color.WHITE : Theme.TEXT);
            FontMetrics fm = g.getFontMetrics();
            String name = entry.name;
            int max = getWidth() - 30;
            while (name.length() > 1 && fm.stringWidth(name) > max) {
                name = name.substring(0, name.length() - 2) + "…";
            }
            g.drawString(name, 20, getHeight() / 2 + fm.getAscent() / 2 - 1);
            g.dispose();
        }
    }
}
