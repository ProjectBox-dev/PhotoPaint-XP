package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Editor;
import photopaint.Theme;
import photopaint.ui.ChooserButton;
import photopaint.ui.ColorSwatch;
import photopaint.ui.XpButtonUI;
import photopaint.ui.XpDialog;
import photopaint.ui.XpSliderUI;
import photopaint.ui.XpToolButtonUI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/** Text tool options: content, font, size and style. */
public class TextDialog extends XpDialog {

    private final JTextArea area = new JTextArea(5, 36);
    private final JSlider sizeSlider;
    private final JLabel sizeLabel = new JLabel();
    private final JToggleButton bold;
    private final JToggleButton italic;
    private final ChooserButton<String> family;
    private boolean accepted;

    public TextDialog(Window owner, Editor ed) {
        super(owner, I18n.t("Текст"));
        List<String> families = Arrays.asList("Tahoma", "Arial", "Times New Roman",
                "Courier New", "Verdana", "Georgia", "Comic Sans MS", "Impact");
        String initialFamily = families.contains(ed.fontFamily) ? ed.fontFamily : "Tahoma";
        family = new ChooserButton<>(families, s -> s, initialFamily);

        sizeSlider = new JSlider(8, 200, Math.max(8, Math.min(200, ed.fontSize)));
        sizeSlider.setUI(new XpSliderUI(sizeSlider));
        sizeSlider.setPreferredSize(new Dimension(110, 20));
        sizeSlider.addChangeListener(e -> sizeLabel.setText(sizeSlider.getValue() + " px"));
        sizeLabel.setText(sizeSlider.getValue() + " px");
        sizeLabel.setPreferredSize(new Dimension(48, 18));

        bold = new JToggleButton(I18n.t("Ж"));
        bold.setUI(new XpToolButtonUI());
        bold.setFont(Theme.font(11, java.awt.Font.BOLD));
        bold.setSelected(ed.bold);
        bold.setToolTipText(I18n.t("Полужирный"));

        italic = new JToggleButton(I18n.t("К"));
        italic.setUI(new XpToolButtonUI());
        italic.setFont(Theme.font(11, java.awt.Font.ITALIC));
        italic.setSelected(ed.italic);
        italic.setToolTipText(I18n.t("Курсив"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        top.setOpaque(false);
        top.add(new JLabel(I18n.t("Шрифт:")));
        top.add(family);
        top.add(bold);
        top.add(italic);
        top.add(new JLabel(I18n.t("Размер:")));
        top.add(sizeSlider);
        top.add(sizeLabel);

        area.setFont(Theme.font(12, java.awt.Font.PLAIN));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(Theme.sunkenBorder());
        scroll.setPreferredSize(new Dimension(440, 120));

        ColorSwatch swatch = new ColorSwatch(18, 18);
        swatch.setColor(ed.colors.getForeground());
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        info.setOpaque(false);
        info.add(new JLabel(I18n.t("Цвет:")));
        info.add(swatch);
        info.add(new JLabel(I18n.t("(основной цвет)")));
        info.add(new JLabel(I18n.t("    Enter — новая строка")));

        JPanel center = new JPanel(new BorderLayout(0, 5));
        center.setOpaque(false);
        center.add(info, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        content.add(top, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        body().add(content, BorderLayout.CENTER);

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, () -> {
            accepted = true;
            ed.fontFamily = family.getValue();
            ed.fontSize = sizeSlider.getValue();
            ed.bold = bold.isSelected();
            ed.italic = italic.isSelected();
            dispose();
        });
    }

    public boolean showDialog() {
        showCentered();
        return accepted;
    }

    public String getText() {
        return area.getText();
    }

    public void setText(String t) {
        area.setText(t);
        area.requestFocusInWindow();
    }
}
