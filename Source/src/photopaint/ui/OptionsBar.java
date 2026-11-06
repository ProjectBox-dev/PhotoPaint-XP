package photopaint.ui;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

/** Context bar with options of the active tool. */
public class OptionsBar extends JPanel {

    private final Editor ed;
    private final JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
    private final JLabel toolIcon = new JLabel();
    private final JLabel toolName = new JLabel();
    private final ColorSwatch fg = new ColorSwatch(19, 19);
    private final ColorSwatch bg = new ColorSwatch(19, 19);

    public OptionsBar(Editor ed) {
        this.ed = ed;
        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 34));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xACA899)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        left.setOpaque(false);
        toolIcon.setPreferredSize(new Dimension(24, 24));
        left.add(toolIcon);
        toolName.setFont(Theme.font(11, java.awt.Font.BOLD));
        toolName.setPreferredSize(new Dimension(100, 20));
        left.add(toolName);
        left.add(separator());
        add(left, BorderLayout.WEST);

        content.setOpaque(false);
        add(content, BorderLayout.CENTER);

        JPanel colors = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 6));
        colors.setOpaque(false);
        colors.add(new JLabel(I18n.t("Цвет:")));
        colors.add(fg);
        colors.add(bg);
        add(colors, BorderLayout.EAST);

        rebuild();
    }

    private JComponent separator() {
        JComponent c = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(2, 22);
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0xB8B4A6));
                g.drawLine(0, 2, 0, getHeight() - 3);
                g.setColor(Color.WHITE);
                g.drawLine(1, 2, 1, getHeight() - 3);
            }
        };
        return c;
    }

    public void rebuild() {
        content.removeAll();
        toolIcon.setIcon(Icons.icon(ed.tool.id, 22));
        toolName.setText(I18n.t(ed.tool.name));
        switch (ed.tool.id) {
            case "brush" -> {
                content.add(slider(I18n.t("Размер:"), 1, 200, ed.brushSize, v -> ed.brushSize = v));
                content.add(slider(I18n.t("Жёсткость:"), 0, 100, ed.hardness, v -> ed.hardness = v));
                content.add(slider(I18n.t("Непрозр.:"), 1, 100, ed.opacity, v -> ed.opacity = v));
            }
            case "pencil" -> {
                content.add(slider(I18n.t("Размер:"), 1, 24, ed.pencilSize, v -> ed.pencilSize = v));
                content.add(slider(I18n.t("Непрозр.:"), 1, 100, ed.opacity, v -> ed.opacity = v));
            }
            case "eraser" -> {
                content.add(slider(I18n.t("Размер:"), 1, 300, ed.eraserSize, v -> ed.eraserSize = v));
                content.add(slider(I18n.t("Жёсткость:"), 0, 100, ed.hardness, v -> ed.hardness = v));
                content.add(slider(I18n.t("Сила:"), 1, 100, ed.opacity, v -> ed.opacity = v));
            }
            case "fill" -> content.add(slider(I18n.t("Допуск:"), 0, 100, ed.tolerance, v -> ed.tolerance = v));
            case "gradient" -> {
                ChooserButton<String> type = new ChooserButton<>(
                        Arrays.asList(I18n.t("Линейный"), I18n.t("Радиальный")), s -> s,
                        ed.gradientType == 0 ? I18n.t("Линейный") : I18n.t("Радиальный"));
                type.onChange(v -> {
                    ed.gradientType = v.equals(I18n.t("Радиальный")) ? 1 : 0;
                    ed.fire("canvas");
                });
                content.add(smallLabel(I18n.t("Тип:")));
                content.add(type);
                JCheckBox transparent = check(I18n.t("Прозрачный фон"), ed.gradientTransparent,
                        v -> ed.gradientTransparent = v);
                content.add(transparent);
            }
            case "line" -> content.add(slider(I18n.t("Толщина:"), 1, 50, ed.lineWidth, v -> ed.lineWidth = v));
            case "rect", "ellipse" -> {
                ChooserButton<String> fill = new ChooserButton<>(
                        Arrays.asList(I18n.t("Контур"), I18n.t("Заливка"), I18n.t("Контур + заливка")), s -> s,
                        switch (ed.fillMode) {
                            case 1 -> I18n.t("Заливка");
                            case 2 -> I18n.t("Контур + заливка");
                            default -> I18n.t("Контур");
                        });
                fill.onChange(v -> {
                    ed.fillMode = v.equals(I18n.t("Заливка")) ? 1 : v.equals(I18n.t("Контур + заливка")) ? 2 : 0;
                    ed.fire("canvas");
                });
                content.add(smallLabel(I18n.t("Режим:")));
                content.add(fill);
                content.add(slider(I18n.t("Толщина:"), 1, 50, ed.lineWidth, v -> ed.lineWidth = v));
                content.add(check(I18n.t("Сглаживание"), ed.antialias, v -> ed.antialias = v));
            }
            case "select" -> content.add(smallLabel(I18n.t("Shift — квадрат. Клик без перетаскивания снимает выделение.")));
            case "lasso" -> content.add(smallLabel(I18n.t("Обведите мышью область произвольной формы. Отпустите кнопку — контур замкнётся.")));
            case "move" -> content.add(smallLabel(I18n.t("Перетаскивайте слой или выделенную область. Shift — по одной оси.")));
            case "text" -> content.add(smallLabel(I18n.t("Щёлкните по холсту и введите текст. Шрифт, размер и стиль — в окне текста.")));
            case "zoom" -> {
                JButton actual = smallButton("100%");
                actual.addActionListener(e -> ed.fire("zoom100"));
                JButton fit = smallButton(I18n.t("По окну"));
                fit.addActionListener(e -> ed.fire("fit"));
                content.add(actual);
                content.add(fit);
                content.add(smallLabel(I18n.t("Alt — уменьшить")));
            }
            case "hand" -> content.add(smallLabel(I18n.t("Перетаскивайте для прокрутки. Ctrl + колесо — масштаб.")));
            case "picker" -> content.add(smallLabel(I18n.t("Щелчок — основной цвет, Alt или правая кнопка — фоновый.")));
            default -> content.add(smallLabel(I18n.t(ed.tool.hint)));
        }
        fg.setColor(ed.colors.getForeground());
        bg.setColor(ed.colors.getBackground());
        content.revalidate();
        content.repaint();
        revalidate();
        repaint();
    }

    public void refreshColors() {
        fg.setColor(ed.colors.getForeground());
        bg.setColor(ed.colors.getBackground());
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.uiSmall());
        l.setForeground(new Color(0x3A3A3A));
        return l;
    }

    private JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setUI(new XpButtonUI());
        b.setFont(Theme.uiSmall());
        b.setPreferredSize(new Dimension(72, 23));
        return b;
    }

    private JCheckBox check(String text, boolean value, java.util.function.Consumer<Boolean> setter) {
        JCheckBox cb = new JCheckBox(text, value);
        cb.setOpaque(false);
        cb.setFont(Theme.uiSmall());
        cb.addActionListener(e -> {
            setter.accept(cb.isSelected());
            ed.fire("canvas");
        });
        return cb;
    }

    private JComponent slider(String label, int min, int max, int value, IntConsumer setter) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.uiSmall());
        p.add(l);
        JSlider s = new JSlider(min, max, Math.max(min, Math.min(max, value)));
        s.setUI(new XpSliderUI(s));
        s.setPreferredSize(new Dimension(76, 20));
        JLabel v = new JLabel(String.valueOf(value));
        v.setFont(Theme.uiSmall());
        v.setPreferredSize(new Dimension(28, 14));
        s.addChangeListener(e -> {
            v.setText(String.valueOf(s.getValue()));
            setter.accept(s.getValue());
            ed.fire("canvas");
        });
        p.add(s);
        p.add(v);
        return p;
    }

    @Override
    protected void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg.create();
        g.setPaint(new GradientPaint(0, 0, new Color(0xFBFAF3), 0, getHeight(),
                new Color(0xE7E3D4)));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(0xFFFFFF));
        g.drawLine(0, 0, getWidth(), 0);
        g.dispose();
    }
}
