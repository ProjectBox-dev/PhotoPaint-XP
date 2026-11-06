package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.ui.dialogs.ColorPickerDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Foreground colour editor with RGB sliders, hex field and palette. */
public class ColorPanel extends JPanel {

    private final Editor ed;
    private final JSlider r = new JSlider(0, 255);
    private final JSlider g = new JSlider(0, 255);
    private final JSlider b = new JSlider(0, 255);
    private final JLabel rv = new JLabel();
    private final JLabel gv = new JLabel();
    private final JLabel bv = new JLabel();
    private final JTextField hex = new JTextField(7);
    private final ColorSwatch fg = new ColorSwatch(46, 32);
    private final ColorSwatch bg = new ColorSwatch(46, 32);
    private boolean updating;

    public ColorPanel(Editor ed) {
        this.ed = ed;
        setLayout(new BorderLayout(0, 5));
        setBackground(Theme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        top.setOpaque(false);
        top.add(new JLabel(I18n.t("Основной")));
        top.add(fg);
        top.add(new JLabel(I18n.t("Фон")));
        top.add(bg);
        JButton swap = new JButton(Icons.small("swap"));
        swap.setUI(new XpButtonUI());
        swap.setPreferredSize(new Dimension(26, 23));
        swap.setMargin(new java.awt.Insets(0, 0, 0, 0));
        swap.setToolTipText(I18n.t("Поменять цвета"));
        swap.addActionListener(e -> {
            ed.colors.swap();
            ed.fire("colors");
        });
        top.add(swap);
        add(top, BorderLayout.NORTH);

        JPanel sliders = new JPanel(new GridLayout(3, 1, 0, 3));
        sliders.setOpaque(false);
        sliders.add(row("R", r, rv));
        sliders.add(row("G", g, gv));
        sliders.add(row("B", b, bv));

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(sliders, BorderLayout.NORTH);

        JPanel hexRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        hexRow.setOpaque(false);
        hexRow.add(new JLabel("Hex:"));
        hex.setFont(Theme.ui());
        hex.setPreferredSize(new Dimension(74, 21));
        hex.addActionListener(e -> applyHex());
        hexRow.add(hex);
        center.add(hexRow, BorderLayout.CENTER);

        PalettePanel palette = new PalettePanel(14, 14, (c, isBg) -> {
            if (isBg) ed.setBackground(c);
            else ed.setForeground(c);
        });
        JPanel palWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        palWrap.setOpaque(false);
        palWrap.add(palette);
        center.add(palWrap, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        javax.swing.event.ChangeListener cl = e -> {
            if (updating) return;
            ed.setForeground(new Color(r.getValue(), g.getValue(), b.getValue()));
        };
        r.addChangeListener(cl);
        g.addChangeListener(cl);
        b.addChangeListener(cl);

        fg.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Color c = new ColorPickerDialog(SwingUtilities.getWindowAncestor(ColorPanel.this),
                        ed.colors.getForeground()).showDialog();
                if (c != null) ed.setForeground(c);
            }
        });
        bg.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Color c = new ColorPickerDialog(SwingUtilities.getWindowAncestor(ColorPanel.this),
                        ed.colors.getBackground()).showDialog();
                if (c != null) ed.setBackground(c);
            }
        });

        refresh();
    }

    private JPanel row(String name, JSlider slider, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(3, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(name);
        l.setPreferredSize(new Dimension(14, 16));
        l.setFont(Theme.uiBold());
        p.add(l, BorderLayout.WEST);
        slider.setUI(new XpSliderUI(slider));
        slider.setPreferredSize(new Dimension(100, 19));
        p.add(slider, BorderLayout.CENTER);
        value.setFont(Theme.uiSmall());
        value.setPreferredSize(new Dimension(28, 15));
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private void applyHex() {
        String s = hex.getText().trim();
        if (s.startsWith("#")) s = s.substring(1);
        try {
            int v = Integer.parseInt(s, 16);
            ed.setForeground(new Color((v >> 16) & 255, (v >> 8) & 255, v & 255));
        } catch (NumberFormatException ignored) {
            refresh();
        }
    }

    public void refresh() {
        updating = true;
        Color c = ed.colors.getForeground();
        r.setValue(c.getRed());
        g.setValue(c.getGreen());
        b.setValue(c.getBlue());
        rv.setText(String.valueOf(c.getRed()));
        gv.setText(String.valueOf(c.getGreen()));
        bv.setText(String.valueOf(c.getBlue()));
        hex.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
        fg.setColor(c);
        bg.setColor(ed.colors.getBackground());
        updating = false;
    }
}
