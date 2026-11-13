package dev.granite.graniteskills.bukkit.loot;

import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.bukkit.loot.parser.EntityLootParser;
import dev.granite.graniteskills.bukkit.loot.parser.ItemLootParser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.loot.CommandLootParser;
import dev.granite.graniteskills.common.loot.LootLoader;
import dev.granite.graniteskills.common.loot.LootType;

public class BukkitLootLoader extends LootLoader {

    private final BukkitLootManager manager;

    public BukkitLootLoader(GraniteSkillsPlugin plugin, BukkitLootManager manager) {
        super(plugin, manager);
        this.manager = manager;
    }

    @Override
    public LootParser getParser(LootType type) {
        return switch (type) {
            case ITEM -> new ItemLootParser(manager);
            case COMMAND -> new CommandLootParser();
            case ENTITY -> new EntityLootParser(manager);
        };
    }

}
