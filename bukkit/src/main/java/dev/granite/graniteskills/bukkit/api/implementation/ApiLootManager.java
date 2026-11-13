package dev.granite.graniteskills.bukkit.api.implementation;

import dev.granite.graniteskills.api.loot.LootManager;
import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.jetbrains.annotations.Nullable;

public class ApiLootManager implements LootManager {

    private final GraniteSkills plugin;

    public ApiLootManager(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable LootTable getLootTable(NamespacedId id) {
        return plugin.getLootManager().getLootTable(id);
    }

    @Override
    public void registerLootType(String name, LootParser parser) {
        plugin.getLootManager().registerCustomLootParser(name, parser);
    }

}
