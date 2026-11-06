package photopaint.ui;

import photopaint.Theme;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** A combo box replacement built from a button and an XP popup menu. */
public class ChooserButton<T> extends JButton {

    private final List<T> values;
    private final Function<T, String> labels;
    private T value;
    private Consumer<T> onChange;
    private int preferredWidth;

    public ChooserButton(List<T> values, Function<T, String> labels, T initial) {
        this.values = values;
        this.labels = labels;
        this.value = initial != null ? initial : values.get(0);
        setUI(new XpButtonUI());
        setFont(Theme.ui());
        setHorizontalAlignment(LEFT);
        setText(textFor(value));
        addActionListener(e -> showPopup());
    }

    private String textFor(T v) {
        return labels.apply(v);
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg.create();
        int x = getWidth() - 14;
        int y = getHeight() / 2 - 2;
        g.setColor(new Color(0x333333));
        g.fillPolygon(new int[]{x, x + 8, x + 4}, new int[]{y, y, y + 4}, 3);
        g.dispose();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T v) {
        if (v == null) return;
        value = v;
        setText(textFor(v));
        if (onChange != null) onChange.accept(v);
    }

    public void setValueSilently(T v) {
        value = v;
        setText(textFor(v));
    }

    public void onChange(Consumer<T> c) {
        onChange = c;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        // The width is computed once for the widest option and then kept fixed.
        // Otherwise picking a longer value (e.g. "Прозрачный фон") grows the button
        // after the dialog is packed and GridBagLayout squeezes and ellipsises the
        // neighbouring labels and text fields.
        d.width = Math.max(d.width, preferredWidth());
        return d;
    }

    private int preferredWidth() {
        if (preferredWidth <= 0) {
            java.awt.FontMetrics fm = getFontMetrics(getFont());
            int widest = 0;
            for (T v : values) {
                widest = Math.max(widest, fm.stringWidth(labels.apply(v)));
            }
            preferredWidth = Math.max(86, widest + 40);
        }
        return preferredWidth;
    }

    private void showPopup() {
        JPopupMenu popup = new JPopupMenu();
        for (T v : values) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(labels.apply(v), v.equals(value));
            item.setFont(Theme.ui());
            item.addActionListener(e -> {
                value = v;
                setText(textFor(v));
                if (onChange != null) onChange.accept(v);
            });
            popup.add(item);
        }
        popup.setInvoker(this);
        Dimension pref = popup.getPreferredSize();
        popup.setPopupSize(Math.max(getWidth(), pref.width + 10), pref.height);
        popup.show(this, 0, getHeight());
    }
}
