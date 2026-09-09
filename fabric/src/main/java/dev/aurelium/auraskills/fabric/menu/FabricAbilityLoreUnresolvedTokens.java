package dev.aurelium.auraskills.fabric.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects raw AuraSkills template leaks in final menu strings. Intended for tests and diagnostics.
 */
public final class FabricAbilityLoreUnresolvedTokens {

    private static final Pattern BRACE = Pattern.compile("\\{[a-zA-Z0-9_./:-]+}");
    private static final Pattern NUMBERED = Pattern.compile("<[0-9]+>");
    private static final String REMOVE_SENTINEL = "!!REMOVE!!";

    private FabricAbilityLoreUnresolvedTokens() {}

    public static boolean hasAnyUnresolved(String s) {
        return !findUnresolved(s).isEmpty();
    }

    public static List<String> findUnresolved(String s) {
        List<String> out = new ArrayList<>();
        if (s == null || s.isEmpty()) {
            return out;
        }
        if (s.contains(REMOVE_SENTINEL)) {
            out.add(REMOVE_SENTINEL);
        }
        Matcher m = BRACE.matcher(s);
        while (m.find()) {
            out.add(m.group());
        }
        Matcher n = NUMBERED.matcher(s);
        while (n.find()) {
            out.add(n.group());
        }
        return out;
    }
}