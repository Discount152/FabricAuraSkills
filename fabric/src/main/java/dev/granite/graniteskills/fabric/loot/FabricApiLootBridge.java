package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.api.loot.LootManager;
import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.api.registry.NamespacedId;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes {@link FabricLootManager} through the public API {@link LootManager} interface (same pattern as Bukkit
 * {@link dev.granite.graniteskills.bukkit.api.implementation.ApiLootManager}).
 */
public final class FabricApiLootBridge implements LootManager {

    private final FabricLootManager delegate;

    public FabricApiLootBridge(FabricLootManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable LootTable getLootTable(NamespacedId id) {
        return delegate.getLootTable(id);
    }

    @Override
    public void registerLootType(String name, LootParser parser) {
        delegate.registerCustomLootParser(name, parser);
    }
}
