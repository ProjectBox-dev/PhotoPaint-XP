package photopaint;

import photopaint.i18n.I18n;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Portable settings stored next to the application: {@code Settings/settings.properties}.
 * Only the interface language is remembered for now.
 */
public final class Settings {

    private static final String FOLDER = "Settings";
    private static final String FILE = "settings.properties";

    private Settings() {
    }

    /** Folder that contains PhotoPaint XP.exe (or the project folder during development). */
    public static Path appHome() {
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath != null && !appPath.isBlank()) {
            Path p = Path.of(appPath).toAbsolutePath();
            if (p.getParent() != null) return p.getParent();
        }
        try {
            var source = Settings.class.getProtectionDomain().getCodeSource();
            if (source != null && source.getLocation() != null) {
                Path p = Path.of(source.getLocation().toURI());
                if (Files.isRegularFile(p) && p.getParent() != null
                        && p.getParent().getParent() != null) {
                    return p.getParent().getParent();
                }
            }
        } catch (URISyntaxException | RuntimeException ignored) {
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath();
    }

    public static Path folder() {
        return appHome().resolve(FOLDER);
    }

    public static Path file() {
        return folder().resolve(FILE);
    }

    /** Reads the saved language, or returns null when there is no valid setting yet. */
    public static I18n.Lang readLanguage() {
        Path file = file();
        if (!Files.isRegularFile(file)) return null;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
        return I18n.Lang.fromCode(p.getProperty("language"));
    }

    /**
     * Saves the language. Returns false when the application folder is read-only;
     * the language still applies for the current session.
     */
    public static boolean writeLanguage(I18n.Lang lang) {
        if (lang == null) return false;
        Path dir = folder();
        Path target = dir.resolve(FILE);
        Path temp = dir.resolve(FILE + ".tmp");
        try {
            Files.createDirectories(dir);
            String text = "# PhotoPaint XP settings\nlanguage=" + lang.code() + "\n";
            try (Writer w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                w.write(text);
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
            return false;
        }
    }
}
