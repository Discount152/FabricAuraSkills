package dev.aurelium.auraskills.fabric.menu;

/**
 * One registered menu: stable ID (matches reference menu file base name), default title translation key
 * (static menus / fallback; contextual GUIs may ignore it in favor of YAML + context), and opener.
 * **Sprint 32:** for {@code skills} / {@code stats}, title and enablement come from
 * {@code menus/<id>.yml} → {@code granite}. **Sprint 35:** {@code level_progression} / {@code stat_info} use
 * {@link FabricMenuContext#ofSkill} / {@link FabricMenuContext#ofStat}; registry supplies an empty
 * {@link net.minecraft.text.Text} title and the GUI builds the final title.
 */
public record FabricMenuDefinition(String id, String titleTranslationKey, FabricMenuOpener opener) {
}
