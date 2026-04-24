package dev.aurelium.auraskills.fabric.menu;

/**
 * One registered menu: stable ID (matches reference menu file base name), default title translation key
 * (used if {@link FabricMenuRegistry} has no YAML slice for this id), and opener. **Sprint 32:** for
 * {@code skills} / {@code stats}, title and enablement come from {@code menus/<id>.yml} → {@code granite}.
 */
public record FabricMenuDefinition(String id, String titleTranslationKey, FabricMenuOpener opener) {
}
