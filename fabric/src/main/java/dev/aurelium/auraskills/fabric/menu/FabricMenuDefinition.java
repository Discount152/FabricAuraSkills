package dev.aurelium.auraskills.fabric.menu;

/**
 * One registered menu: stable ID (matches reference menu file base name), title translation key, and
 * opener. Full Slate/YAML is still out of scope; definitions are code-built in {@link FabricMenuLoaders}.
 */
public record FabricMenuDefinition(String id, String titleTranslationKey, FabricMenuOpener opener) {
}
