package photopaint.ui.dialogs;

import photopaint.Theme;
import photopaint.i18n.I18n;
import photopaint.ui.XpDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

/** First-run language chooser. Both languages are always shown together. */
public class LanguageDialog extends XpDialog {

    private I18n.Lang result;

    private LanguageDialog() {
        super(null, "PhotoPaint XP — Язык / Language");

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(16, 22, 4, 22));

        JLabel title = new JLabel("<html><div style='width:360px;text-align:center'>"
                + "<b>Выберите язык интерфейса</b><br>Choose the interface language</div></html>");
        title.setFont(Theme.ui());
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel note = new JLabel("<html><div style='width:360px;text-align:center;color:#555555'>"
                + "Позже язык можно изменить в меню «Правка → Настройки...».<br>"
                + "You can change it later in Edit → Settings...</div></html>");
        note.setFont(Theme.uiSmall());
        note.setForeground(new Color(0x555555));
        note.setAlignmentX(CENTER_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(10));
        box.add(note);
        body().add(box, BorderLayout.CENTER);

        addButton("Русский", true, () -> choose(I18n.Lang.RU));
        addButton("English", false, () -> choose(I18n.Lang.EN));
    }

    private void choose(I18n.Lang lang) {
        result = lang;
        dispose();
    }

    /** Returns the chosen language, or null when the dialog was closed. */
    public static I18n.Lang ask() {
        LanguageDialog dialog = new LanguageDialog();
        dialog.showCentered();
        return dialog.result;
    }
}
