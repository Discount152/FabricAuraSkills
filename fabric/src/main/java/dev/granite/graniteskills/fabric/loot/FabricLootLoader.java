package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.loot.CommandLootParser;
import dev.granite.graniteskills.common.loot.LootLoader;
import dev.granite.graniteskills.common.loot.LootType;

public final class FabricLootLoader extends LootLoader {

    private final FabricLootManager manager;

    public FabricLootLoader(GraniteSkillsPlugin plugin, FabricLootManager manager) {
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
