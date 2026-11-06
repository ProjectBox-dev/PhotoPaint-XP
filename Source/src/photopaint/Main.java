package photopaint;

import photopaint.i18n.I18n;
import photopaint.ui.XpLaf;
import photopaint.ui.dialogs.LanguageDialog;

import javax.swing.SwingUtilities;
import java.io.File;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        File startFile = null;
        for (String a : args) {
            if (a != null && !a.isBlank() && !a.startsWith("-")) {
                startFile = new File(a);
                break;
            }
        }
        final File start = startFile;

        SwingUtilities.invokeLater(() -> {
            XpLaf.install();
            I18n.Lang language = Settings.readLanguage();
            if (language == null) {
                language = LanguageDialog.ask();
                if (language == null) language = I18n.Lang.RU;
                Settings.writeLanguage(language);
            }
            I18n.setLanguage(language);
            AppWindow window = new AppWindow();
            window.setVisible(true);
            window.afterShown();
            if (start != null) window.openFile(start);
        });
    }
}
