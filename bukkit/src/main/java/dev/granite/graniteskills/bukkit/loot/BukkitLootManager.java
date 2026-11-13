package dev.granite.graniteskills.bukkit.loot;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.loot.context.MobContextProvider;
import dev.granite.graniteskills.bukkit.loot.entity.VanillaEntityParser;
import dev.granite.graniteskills.bukkit.loot.parser.CustomEntityParser;
import dev.granite.graniteskills.bukkit.requirement.BukkitLootRequirements;
import dev.granite.graniteskills.bukkit.util.ItemUtils;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.loot.CustomItemParser;
import dev.granite.graniteskills.common.loot.LootLoader;
import dev.granite.graniteskills.common.loot.LootManager;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class BukkitLootManager extends LootManager {

    private final GraniteSkills plugin;
    private final LootLoader lootLoader;
    private final List<CustomItemParser> customItemParsers = new ArrayList<>();
    private final List<CustomEntityParser> customEntityParsers = new ArrayList<>();

    public BukkitLootManager(GraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.lootLoader = new BukkitLootLoader(plugin, this);

        registerContextProvider(new MobContextProvider());
        registerCustomItemParser(new ItemKeyParser(plugin));
        registerCustomEntityParser(new VanillaEntityParser(plugin));
    }

    @Override
    public GraniteSkills getPlugin() {
        return plugin;
    }

    public void loadLootTables() {
        lootLoader.loadLootTables();
    }

    public BukkitLootRequirements getLootRequirements(ConfigurationNode config) {
        return plugin.getRequirementManager().getLootRequirements(config);
    }

    public boolean toInventory(ItemStack held) {
        if (ItemUtils.hasTelekinesis(held)) {
            return true;
        }
        return plugin.configBoolean(Option.LOOT_DIRECTLY_TO_INVENTORY);
    }

    public List<CustomItemParser> getCustomItemParsers() {
        return customItemParsers;
    }

    public List<CustomEntityParser> getCustomEntityParsers() {
        return customEntityParsers;
    }

    public void registerCustomItemParser(CustomItemParser customItemParser) {
        customItemParsers.add(customItemParser);
    }

    public void registerCustomEntityParser(CustomEntityParser customEntityParser) {
        customEntityParsers.add(customEntityParser);
    }

}
