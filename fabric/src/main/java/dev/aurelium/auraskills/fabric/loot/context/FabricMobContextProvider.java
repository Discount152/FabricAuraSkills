package dev.aurelium.auraskills.fabric.loot.context;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.loot.LootContext;
import dev.aurelium.auraskills.common.loot.ContextProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Bukkit {@code MobContextProvider} on Fabric: {@code mobs:} list entries map to
 * {@link FabricMobContext} using the entity type registry (see {@code loot/mob.yml}).
 */
public final class FabricMobContextProvider extends ContextProvider {

    public FabricMobContextProvider() {
        super("mobs");
    }

    @Override
    public @Nullable Set<LootContext> parseContext(ConfigurationNode config) throws SerializationException {
        Set<LootContext> contexts = Sets.newConcurrentHashSet();
        if (config.node("mobs").virtual()) {
            return contexts;
        }
        List<String> mobList = config.node("mobs").getList(String.class, new ArrayList<>());
        for (String name : mobList) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String path = toRegisteredPath(name.trim());
            if (path == null) {
                continue;
            }
            contexts.add(new FabricMobContext(path));
        }
        return contexts;
    }

    private @Nullable String toRegisteredPath(String raw) {
        // Mirror Bukkit: list entries are enum names or vanilla ids; accept minecraft:zombie or zombie.
        String s = raw.replace("minecraft:", "").trim();
        if (s.isEmpty()) {
            return null;
        }
        s = s.toLowerCase(Locale.ROOT);
        if (!s.contains(":")) {
            s = "minecraft:" + s;
        }
        Identifier id = Identifier.tryParse(s);
        if (id == null) {
            return null;
        }
        if (Registries.ENTITY_TYPE.get(id) == null) {
            return null;
        }
        return id.getPath();
    }
}
