package photopaint.ui;

import photopaint.Theme;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Undecorated XP styled modal dialog with a title bar and a button row. */
public class XpDialog extends JDialog {

    private final JPanel body = new JPanel(new BorderLayout());
    private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 7));
    private JButton defaultButton;

    public XpDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.FRAME_EDGE);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x0046AD)),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        XpTitleBar titleBar = new XpTitleBar(this, false);
        titleBar.setTitleText(title);
        titleBar.setPreferredSize(new Dimension(100, 25));

        body.setBackground(Theme.WINDOW);
        body.setBorder(BorderFactory.createEmptyBorder(12, 14, 6, 14));
        buttons.setBackground(Theme.WINDOW);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD6D2C2)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        root.add(titleBar, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public JPanel body() {
        return body;
    }

    public JPanel buttonBar() {
        return buttons;
    }

    public JButton addButton(String text, boolean def, Runnable action) {
        JButton b = new JButton(text);
        b.setUI(new XpButtonUI());
        b.addActionListener(e -> action.run());
        buttons.add(b);
        if (def) {
            defaultButton = b;
            getRootPane().setDefaultButton(b);
        }
        return b;
    }

    public JButton defaultButton() {
        return defaultButton;
    }

    public void showCentered() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    public void close() {
        dispose();
    }
}
