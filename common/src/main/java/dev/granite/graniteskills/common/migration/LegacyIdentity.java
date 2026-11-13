package dev.granite.graniteskills.common.migration;

import java.util.Locale;

/**
 * Old AuraSkills and AureliumSkills names, used only when reading or moving saved data.
 */
public final class LegacyIdentity {

    public static final String NAMESPACE = "graniteskills";
    public static final String NBT_ROOT = "GraniteSkills";
    public static final String MARKER_FILE = ".granite-identity-migrated";
    /** Oldest first, so a later folder wins when both are present. */
    public static final String[] OLD_FOLDER_NAMES = {"AureliumSkills", "aureliumskills", "AuraSkills", "auraskills"};
    public static final String[] OLD_NAMESPACES = {"aureliumskills", "auraskills"};
    public static final String[] OLD_NBT_ROOTS = {"AureliumSkills", "AuraSkills"};

    private LegacyIdentity() {
    }

    public static String canonicalNamespace(String namespace) {
        String lower = namespace.toLowerCase(Locale.ROOT);
        for (String old : OLD_NAMESPACES) {
            if (old.equals(lower)) {
                return NAMESPACE;
            }
        }
        return lower;
    }

    /**
     * Rewrites a stored id or modifier name. Ordinary text is left as it was.
     */
    public static String rewriteStoredString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (String old : OLD_NAMESPACES) {
            String slash = old + "/";
            if (lower.startsWith(slash)) {
                return NAMESPACE + "/" + value.substring(slash.length());
            }
            String colon = old + ":";
            if (lower.startsWith(colon)) {
                return NAMESPACE + ":" + value.substring(colon.length());
            }
        }
        if (value.startsWith("AureliumSkills.")) {
            return NBT_ROOT + "." + value.substring("AureliumSkills.".length());
        }
        if (value.startsWith("AuraSkills.")) {
            return NBT_ROOT + "." + value.substring("AuraSkills.".length());
        }
        return value;
    }

}
