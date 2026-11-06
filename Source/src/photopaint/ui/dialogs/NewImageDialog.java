package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.ChooserButton;
import photopaint.ui.ColorSwatch;
import photopaint.ui.XpDialog;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/** New image size and background. */
public class NewImageDialog extends XpDialog {

    public record Result(int width, int height, Color background) {
    }

    private final JTextField widthField = new JTextField("900", 6);
    private final JTextField heightField = new JTextField("650", 6);
    private final JLabel error = new JLabel(" ");
    private final ChooserButton<String> background;
    private final ColorSwatch swatch = new ColorSwatch(22, 18);
    private Result result;

    public NewImageDialog(Window owner, int w, int h) {
        super(owner, I18n.t("Новое изображение"));
        widthField.setText(String.valueOf(w));
        heightField.setText(String.valueOf(h));
        widthField.setFont(Theme.ui());
        heightField.setFont(Theme.ui());

        background = new ChooserButton<>(java.util.Arrays.asList(I18n.t("Белый фон"), I18n.t("Прозрачный фон")),
                s -> s, I18n.t("Белый фон"));
        swatch.setColor(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel(I18n.t("Ширина:")), c);
        c.gridx = 1;
        form.add(widthField, c);
        c.gridx = 2;
        form.add(new JLabel("px"), c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel(I18n.t("Высота:")), c);
        c.gridx = 1;
        form.add(heightField, c);
        c.gridx = 2;
        form.add(new JLabel("px"), c);
        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel(I18n.t("Фон:")), c);
        c.gridx = 1;
        c.gridwidth = 2;
        JPanel bgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        bgRow.setOpaque(false);
        bgRow.add(background);
        bgRow.add(swatch);
        form.add(bgRow, c);

        background.onChange(v -> swatch.setColor(v.equals(I18n.t("Прозрачный фон"))
                ? new Color(0, 0, 0, 0) : Color.WHITE));
        swatch.setShowAlpha(true);

        error.setForeground(new Color(0xB00000));
        error.setFont(Theme.uiSmall());

        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.add(form, BorderLayout.CENTER);
        box.add(error, BorderLayout.SOUTH);
        body().add(box, BorderLayout.CENTER);

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, this::accept);
    }

    private void accept() {
        try {
            int w = Integer.parseInt(widthField.getText().trim());
            int h = Integer.parseInt(heightField.getText().trim());
            if (w < 1 || h < 1 || w > 8000 || h > 8000) {
                error.setText(I18n.t("Размер должен быть от 1 до 8000 пикселей."));
                return;
            }
            result = new Result(w, h, background.getValue().equals(I18n.t("Прозрачный фон"))
                    ? null : Color.WHITE);
            dispose();
        } catch (NumberFormatException e) {
            error.setText(I18n.t("Введите целые числа."));
        }
    }

    public Result showDialog() {
        showCentered();
        return result;
    }
}
