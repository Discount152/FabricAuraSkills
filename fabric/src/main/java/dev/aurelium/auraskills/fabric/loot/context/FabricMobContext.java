package dev.aurelium.auraskills.fabric.loot.context;

import dev.aurelium.auraskills.api.loot.LootContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;

/**
 * Fabric counterpart to Bukkit {@code MobContext} — matches registry {@code path} (e.g. {@code "zombie"})
 * as used in Bukkit’s {@code EntityType#toString().toLowerCase()}.
 */
public record FabricMobContext(String path) implements LootContext {

    @Override
    public String getName() {
        return path;
    }

    public static FabricMobContext forEntity(LivingEntity entity) {
        return new FabricMobContext(Registries.ENTITY_TYPE.getId(entity.getType()).getPath());
    }
}
