package photopaint.ui.dialogs;

import photopaint.i18n.I18n;
import photopaint.Theme;
import photopaint.ui.Icons;
import photopaint.ui.XpDialog;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/** About box. */
public class AboutDialog extends XpDialog {

    private static final String PROFILE = "https://github.com/ProjectBox-dev";

    public AboutDialog(Window owner) {
        super(owner, I18n.t("О программе PhotoPaint XP"));
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        JLabel icon = new JLabel(Icons.appIcon(48));
        row.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel name = new JLabel("PhotoPaint XP");
        name.setFont(Theme.font(18, java.awt.Font.BOLD));
        name.setForeground(new Color(0x0A3C8C));
        JLabel version = new JLabel(I18n.t("Версия 1.0  ·  Windows XP Edition"));
        version.setFont(Theme.ui());
        JLabel desc = new JLabel(I18n.t("Растровый графический редактор для Windows XP."));
        desc.setFont(Theme.uiSmall());
        JLabel author = new JLabel(I18n.t("Создатель: ProjectBox"));
        author.setFont(Theme.uiSmall());
        JLabel link = new JLabel("<html><a href=''>" + PROFILE + "</a></html>");
        link.setFont(Theme.uiSmall());
        link.setForeground(new Color(0x0645AD));
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setToolTipText(I18n.t("Открыть профиль ProjectBox на GitHub"));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(PROFILE));
                } catch (Exception ignored) {
                }
            }
        });
        JLabel java = new JLabel("Java " + System.getProperty("java.version") + "  ·  Java2D");
        java.setFont(Theme.uiSmall());
        java.setForeground(new Color(0x555555));
        JLabel copyright = new JLabel("© 2026  PhotoPaint XP");
        copyright.setFont(Theme.uiSmall());
        copyright.setForeground(new Color(0x555555));
        text.add(name);
        text.add(javax.swing.Box.createVerticalStrut(3));
        text.add(version);
        text.add(javax.swing.Box.createVerticalStrut(10));
        text.add(desc);
        text.add(javax.swing.Box.createVerticalStrut(8));
        text.add(author);
        text.add(javax.swing.Box.createVerticalStrut(2));
        text.add(link);
        text.add(javax.swing.Box.createVerticalStrut(8));
        text.add(java);
        text.add(javax.swing.Box.createVerticalStrut(10));
        text.add(copyright);
        row.add(text, BorderLayout.CENTER);

        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.add(row, BorderLayout.CENTER);
        box.setPreferredSize(new java.awt.Dimension(410, 170));
        body().add(box, BorderLayout.CENTER);
        addButton(I18n.t("ОК"), true, this::dispose);
    }

    public void showDialog() {
        showCentered();
    }
}
