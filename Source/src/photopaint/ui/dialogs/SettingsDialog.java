package photopaint.ui.dialogs;

import photopaint.Theme;
import photopaint.i18n.I18n;
import photopaint.ui.ChooserButton;
import photopaint.ui.XpDialog;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

/** Program settings. Currently only the interface language. */
public class SettingsDialog extends XpDialog {

    private final ChooserButton<I18n.Lang> language;
    private I18n.Lang result;

    public SettingsDialog(Window owner, I18n.Lang current) {
        super(owner, I18n.t("Настройки"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 5, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;

        JLabel label = new JLabel(I18n.t("Язык интерфейса:"));
        label.setFont(Theme.ui());
        form.add(label, c);

        c.gridx = 1;
        language = new ChooserButton<>(List.of(I18n.Lang.RU, I18n.Lang.EN),
                I18n.Lang::displayName, current);
        form.add(language, c);

        JLabel note = new JLabel(I18n.t("Изменения применяются сразу."));
        note.setFont(Theme.uiSmall());
        note.setForeground(new Color(0x555555));
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        form.add(note, c);

        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        box.add(form, BorderLayout.CENTER);
        body().add(box, BorderLayout.CENTER);

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, this::accept);
    }

    private void accept() {
        result = language.getValue();
        dispose();
    }

    /** Returns the selected language, or null when Cancel was pressed. */
    public I18n.Lang showDialog() {
        showCentered();
        return result;
    }
}
