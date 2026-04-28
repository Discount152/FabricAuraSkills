package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central registry for Granite slice menus. Lookup by ID and dispatch to the registered
 * {@link FabricMenuOpener}. **Sprint 32:** Skills/Stats titles and enablement read from
 * {@link FabricBuiltInMenuConfig} when present. **Sprint 35:** {@code level_progression} and
 * {@code stat_info} require {@link FabricMenuContext#ofSkill} / {@link FabricMenuContext#ofStat}.
 */
public final class FabricMenuRegistry {

    private final Map<String, FabricMenuDefinition> byId = new LinkedHashMap<>();

    public void register(FabricMenuDefinition definition) {
        byId.put(definition.id(), definition);
    }

    public Optional<FabricMenuDefinition> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Set<String> menuIds() {
        return Collections.unmodifiableSet(byId.keySet());
    }

    public void open(String id, ServerPlayerEntity player, FabricAuraSkills plugin) {
        open(id, player, plugin, FabricMenuContext.none());
    }

    public void open(
            String id, ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context) {
        FabricMenuDefinition def = byId.get(id);
        if (def == null) {
            if (plugin != null) {
                plugin.logger().warn("Unknown menu id '" + id + "'; registered: " + byId.keySet());
            }
            return;
        }
        if (FabricMenuKeys.LEVEL_PROGRESSION.equals(id)) {
            if (plugin == null) {
                return;
            }
            if (context.skill() == null) {
                plugin.logger()
                        .warn(
                                "Menu '"
                                        + id
                                        + "' requires FabricMenuContext.ofSkill(Skill); use FabricAuraSkillsMenus#openLevelProgression or the registry with context.");
                return;
            }
            def.opener().open(player, plugin, Text.empty(), context);
            return;
        }
        if (FabricMenuKeys.STAT_INFO.equals(id)) {
            if (plugin == null) {
                return;
            }
            if (context.stat() == null) {
                plugin.logger()
                        .warn(
                                "Menu '"
                                        + id
                                        + "' requires FabricMenuContext.ofStat(Stat); use FabricAuraSkillsMenus#openStatInfo or the registry with context.");
                return;
            }
            def.opener().open(player, plugin, Text.empty(), context);
            return;
        }
        Text title;
        if (plugin != null && FabricMenuKeys.SKILLS.equals(id)) {
            FabricMenuYamlSlice slice =
                    plugin.getBuiltInMenuConfig().getSlice(id, FabricMenuYamlSlice.defaultSkills());
            if (!slice.enabled()) {
                if (player != null) {
                    player.sendMessage(
                            FabricGraniteText.key("auraskills.granite.menu.built_in_disabled", id), false);
                }
                return;
            }
            title = FabricMenuTitles.granitePrimaryTitleFromKey(slice.titleTranslationKey());
        } else if (plugin != null && FabricMenuKeys.STATS.equals(id)) {
            FabricMenuYamlSlice slice =
                    plugin.getBuiltInMenuConfig().getSlice(id, FabricMenuYamlSlice.defaultStats());
            if (!slice.enabled()) {
                if (player != null) {
                    player.sendMessage(
                            FabricGraniteText.key("auraskills.granite.menu.built_in_disabled", id), false);
                }
                return;
            }
            title = FabricMenuTitles.granitePrimaryTitleFromKey(slice.titleTranslationKey());
        } else {
            title = FabricMenuTitles.granitePrimaryTitleFromKey(def.titleTranslationKey());
        }
        def.opener().open(player, plugin, title, context);
    }
}
