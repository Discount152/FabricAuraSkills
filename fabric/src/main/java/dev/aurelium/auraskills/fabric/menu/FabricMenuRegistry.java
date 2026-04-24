package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central registry for Granite slice menus. Lookup by ID and dispatch to the registered
 * {@link FabricMenuOpener}. Future: merge YAML-backed definitions; reload hook.
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
        FabricMenuDefinition def = byId.get(id);
        if (def == null) {
            plugin.logger().warn("Unknown menu id '" + id + "'; registered: " + byId.keySet());
            return;
        }
        Text title = Text.translatable(def.titleTranslationKey());
        def.opener().open(player, plugin, title);
    }
}
