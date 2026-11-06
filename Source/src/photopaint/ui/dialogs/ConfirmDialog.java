package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.Icons;
import photopaint.ui.XpDialog;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Window;

/** Save / discard / cancel prompt. */
public class ConfirmDialog extends XpDialog {

    public static final int SAVE = 0;
    public static final int DISCARD = 1;
    public static final int CANCEL = 2;

    private int result = CANCEL;

    public ConfirmDialog(Window owner, String title, String message) {
        super(owner, title);
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(new JLabel(Icons.icon("info", 32)), BorderLayout.WEST);
        JLabel label = new JLabel("<html><div style='width:320px'>" + message + "</div></html>");
        label.setFont(Theme.ui());
        row.add(label, BorderLayout.CENTER);
        row.setPreferredSize(new java.awt.Dimension(360, 60));
        body().add(row, BorderLayout.CENTER);

        addButton(I18n.t("Сохранить"), true, () -> {
            result = SAVE;
            dispose();
        });
        addButton(I18n.t("Не сохранять"), false, () -> {
            result = DISCARD;
            dispose();
        });
        addButton(I18n.t("Отмена"), false, () -> {
            result = CANCEL;
            dispose();
        });
    }

    public int showDialog() {
        showCentered();
        return result;
    }
}
