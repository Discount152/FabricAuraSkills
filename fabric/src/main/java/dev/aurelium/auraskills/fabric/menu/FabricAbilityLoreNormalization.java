package dev.aurelium.auraskills.fabric.menu;

/**
 * Plain-text cleanup for ability lore after curly-brace placeholder expansion and before line wrapping.
 * Order: message load, placeholder expansion, this step, wrap, tooltip.
 */
public final class FabricAbilityLoreNormalization {

    private FabricAbilityLoreNormalization() {}

    /**
     * Stock AuraSkills separates activation hints using numbered segment markers; on Bukkit these are often
     * replaced with MiniMessage color resets. For Fabric menu plain lore, turn each marker into a newline so
     * hints become separate readable lines after wrapping.
     */
    public static String normalizeExpandedDescription(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String s = raw.replace("!!REMOVE!!", "");
        s = s.replaceAll("<[0-9]+>", "\n");
        s = s.replaceAll("\n{3,}", "\n\n");
        return s.stripLeading();
    }
}