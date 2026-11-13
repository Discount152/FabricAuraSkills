package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.LootParser;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.loot.CommandLootParser;
import dev.aurelium.auraskills.common.loot.LootLoader;
import dev.aurelium.auraskills.common.loot.LootType;

public final class FabricLootLoader extends LootLoader {

    private final FabricLootManager manager;

    public FabricLootLoader(AuraSkillsPlugin plugin, FabricLootManager manager) {
        super(plugin, manager);
        this.manager = manager;
    }

    @Override
    public LootParser getParser(LootType type) {
        return switch (type) {
            case ITEM -> new FabricItemLootParser(manager);
            case COMMAND -> new CommandLootParser();
            case ENTITY -> new FabricEntityLootParser();
        };
    }
}
