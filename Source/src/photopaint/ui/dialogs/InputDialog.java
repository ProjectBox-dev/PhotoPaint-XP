package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.XpDialog;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

/** Small text input box. */
public class InputDialog extends XpDialog {

    private final JTextField field;
    private String result;

    private InputDialog(Window owner, String title, String label, String initial) {
        super(owner, title);
        field = new JTextField(initial == null ? "" : initial, 24);
        field.setFont(Theme.ui());

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setPreferredSize(new Dimension(360, 24));
        body().add(row, BorderLayout.CENTER);

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, () -> {
            result = field.getText();
            dispose();
        });
        SwingUtilities.invokeLater(() -> {
            field.requestFocusInWindow();
            field.selectAll();
        });
    }

    public static String show(Window owner, String title, String label, String initial) {
        InputDialog d = new InputDialog(owner, title, label, initial);
        d.showCentered();
        return d.result;
    }
}
