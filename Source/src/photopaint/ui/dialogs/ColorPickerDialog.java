package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.ColorSwatch;
import photopaint.ui.PalettePanel;
import photopaint.ui.XpDialog;
import photopaint.ui.XpSliderUI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

/** RGB colour picker with a preset palette. */
public class ColorPickerDialog extends XpDialog {

    private final JSlider r = new JSlider(0, 255);
    private final JSlider g = new JSlider(0, 255);
    private final JSlider b = new JSlider(0, 255);
    private final JLabel rv = new JLabel();
    private final JLabel gv = new JLabel();
    private final JLabel bv = new JLabel();
    private final JTextField hex = new JTextField(8);
    private final ColorSwatch preview = new ColorSwatch(88, 38);
    private boolean updating;
    private Color result;

    public ColorPickerDialog(Window owner, Color initial) {
        super(owner, I18n.t("Выбор цвета"));
        Color start = initial == null ? Color.BLACK : initial;
        r.setValue(start.getRed());
        g.setValue(start.getGreen());
        b.setValue(start.getBlue());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setOpaque(false);
        preview.setPreferredSize(new java.awt.Dimension(88, 38));
        top.add(preview);
        JPanel hexBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        hexBox.setOpaque(false);
        hexBox.add(new JLabel("Hex:"));
        hex.setFont(Theme.ui());
        hex.setPreferredSize(new java.awt.Dimension(76, 22));
        hex.addActionListener(e -> applyHex());
        hexBox.add(hex);
        top.add(hexBox);

        JPanel sliders = new JPanel(new GridLayout(3, 1, 0, 4));
        sliders.setOpaque(false);
        sliders.add(row("R", r, rv));
        sliders.add(row("G", g, gv));
        sliders.add(row("B", b, bv));

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(sliders, BorderLayout.CENTER);

        PalettePanel palette = new PalettePanel(18, 14, (c, bg) -> setColor(c));
        JPanel pal = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        pal.setOpaque(false);
        pal.add(palette);
        center.add(pal, BorderLayout.SOUTH);

        body().add(center, BorderLayout.CENTER);

        javax.swing.event.ChangeListener cl = e -> {
            if (updating) return;
            setColor(new Color(r.getValue(), g.getValue(), b.getValue()), false);
        };
        r.addChangeListener(cl);
        g.addChangeListener(cl);
        b.addChangeListener(cl);

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, () -> {
            result = new Color(r.getValue(), g.getValue(), b.getValue());
            dispose();
        });
        refresh();
    }

    private JPanel row(String name, JSlider slider, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(name);
        l.setFont(Theme.uiBold());
        l.setPreferredSize(new java.awt.Dimension(15, 16));
        p.add(l, BorderLayout.WEST);
        slider.setUI(new XpSliderUI(slider));
        slider.setPreferredSize(new java.awt.Dimension(180, 20));
        p.add(slider, BorderLayout.CENTER);
        value.setFont(Theme.uiSmall());
        value.setPreferredSize(new java.awt.Dimension(30, 15));
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private void setColor(Color c) {
        setColor(c, true);
    }

    private void setColor(Color c, boolean fromSliders) {
        updating = true;
        if (fromSliders) {
            r.setValue(c.getRed());
            g.setValue(c.getGreen());
            b.setValue(c.getBlue());
        }
        rv.setText(String.valueOf(r.getValue()));
        gv.setText(String.valueOf(g.getValue()));
        bv.setText(String.valueOf(b.getValue()));
        Color cur = new Color(r.getValue(), g.getValue(), b.getValue());
        preview.setColor(cur);
        hex.setText(String.format("#%02X%02X%02X", cur.getRed(), cur.getGreen(), cur.getBlue()));
        updating = false;
    }

    private void refresh() {
        setColor(new Color(r.getValue(), g.getValue(), b.getValue()), false);
    }

    private void applyHex() {
        String s = hex.getText().trim();
        if (s.startsWith("#")) s = s.substring(1);
        try {
            int v = Integer.parseInt(s, 16);
            setColor(new Color((v >> 16) & 255, (v >> 8) & 255, v & 255));
        } catch (NumberFormatException e) {
            refresh();
        }
    }

    public Color showDialog() {
        showCentered();
        return result;
    }
}
