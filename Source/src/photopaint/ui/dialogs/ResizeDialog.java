package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.XpDialog;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/** Image size dialog with optional proportional scaling. */
public class ResizeDialog extends XpDialog {

    private final JTextField widthField;
    private final JTextField heightField;
    private final JCheckBox keep = new JCheckBox(I18n.t("Сохранять пропорции"), true);
    private final JLabel error = new JLabel(" ");
    private final int ow;
    private final int oh;
    private Dimension result;
    private boolean updating;

    public ResizeDialog(Window owner, int w, int h) {
        super(owner, I18n.t("Размер изображения"));
        ow = w;
        oh = h;
        widthField = new JTextField(String.valueOf(w), 6);
        heightField = new JTextField(String.valueOf(h), 6);
        widthField.setFont(Theme.ui());
        heightField.setFont(Theme.ui());
        keep.setOpaque(false);
        keep.setFont(Theme.ui());

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
        c.gridwidth = 3;
        form.add(keep, c);

        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        note.setOpaque(false);
        JLabel n = new JLabel(I18n.t("Текущий размер: ") + w + " × " + h + " px");
        n.setFont(Theme.uiSmall());
        n.setForeground(new java.awt.Color(0x555555));
        note.add(n);

        error.setForeground(new java.awt.Color(0xB00000));
        error.setFont(Theme.uiSmall());

        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.add(form, BorderLayout.CENTER);
        box.add(note, BorderLayout.NORTH);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(error, BorderLayout.NORTH);
        box.add(south, BorderLayout.SOUTH);
        body().add(box, BorderLayout.CENTER);

        widthField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                syncHeight();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                syncHeight();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                syncHeight();
            }
        });
        heightField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                syncWidth();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                syncWidth();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                syncWidth();
            }
        });

        addButton(I18n.t("Отмена"), false, this::dispose);
        addButton(I18n.t("ОК"), true, this::accept);
    }

    private void syncHeight() {
        if (updating || !keep.isSelected()) return;
        try {
            int w = Integer.parseInt(widthField.getText().trim());
            int h = Math.max(1, (int) Math.round(w * (double) oh / ow));
            updating = true;
            heightField.setText(String.valueOf(h));
            updating = false;
        } catch (NumberFormatException ignored) {
        }
    }

    private void syncWidth() {
        if (updating || !keep.isSelected()) return;
        try {
            int h = Integer.parseInt(heightField.getText().trim());
            int w = Math.max(1, (int) Math.round(h * (double) ow / oh));
            updating = true;
            widthField.setText(String.valueOf(w));
            updating = false;
        } catch (NumberFormatException ignored) {
        }
    }

    private void accept() {
        try {
            int w = Integer.parseInt(widthField.getText().trim());
            int h = Integer.parseInt(heightField.getText().trim());
            if (w < 1 || h < 1 || w > 8000 || h > 8000) {
                error.setText(I18n.t("Допустимый размер: 1–8000 px."));
                return;
            }
            result = new Dimension(w, h);
            dispose();
        } catch (NumberFormatException e) {
            error.setText(I18n.t("Введите целые числа."));
        }
    }

    public Dimension showDialog() {
        showCentered();
        return result;
    }
}
