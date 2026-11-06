package photopaint.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiny translation helper. Russian text is used directly as the key, which keeps
 * the source readable; English strings are loaded from a UTF-8 TSV resource.
 */
public final class I18n {

    public enum Lang {
        RU("ru", "Русский"),
        EN("en", "English");

        private final String code;
        private final String displayName;

        Lang(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String code() {
            return code;
        }

        public String displayName() {
            return displayName;
        }

        public static Lang fromCode(String code) {
            if (code != null) {
                for (Lang l : values()) {
                    if (l.code.equalsIgnoreCase(code.trim())) return l;
                }
            }
            return null;
        }
    }

    private static final String RESOURCE = "/photopaint/i18n/messages_en.tsv";
    private static final Map<String, String> ENGLISH = loadEnglish();
    private static Lang current = Lang.RU;

    private I18n() {
    }

    public static Lang language() {
        return current;
    }

    public static void setLanguage(Lang lang) {
        current = lang != null ? lang : Lang.RU;
    }

    /** Returns the text for the current language, or the Russian source text as a fallback. */
    public static String t(String russian) {
        if (russian == null || current != Lang.EN) return russian;
        String translated = ENGLISH.get(russian);
        return translated != null ? translated : russian;
    }

    public static String t(String russian, Object... args) {
        return String.format(t(russian), args);
    }

    private static Map<String, String> loadEnglish() {
        Map<String, String> map = new LinkedHashMap<>();
        try (InputStream in = I18n.class.getResourceAsStream(RESOURCE)) {
            if (in == null) return map;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) {
                        first = false;
                        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                            line = line.substring(1);
                        }
                    }
                    if (line.isBlank() || line.startsWith("#")) continue;
                    int tab = line.indexOf('\t');
                    if (tab < 0) continue;
                    map.put(line.substring(0, tab), line.substring(tab + 1));
                }
            }
        } catch (IOException ignored) {
        }
        return map;
    }
}
