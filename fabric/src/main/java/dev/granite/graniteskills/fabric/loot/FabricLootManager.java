package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.loot.CustomItemParser;
import dev.granite.graniteskills.common.loot.LootManager;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public final class FabricLootManager extends LootManager {

    private final FabricGraniteSkills plugin;
    private final FabricLootLoader lootLoader;
    private final List<CustomItemParser> customItemParsers = new ArrayList<>();

    public FabricLootManager(FabricGraniteSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.lootLoader = new FabricLootLoader(plugin, this);
    }

    @Override
    public dev.granite.graniteskills.api.loot.LootRequirements getLootRequirements(ConfigurationNode config) {
        return new FabricPassthroughLootRequirements();
    }

    public void loadLootTables() {
        lootLoader.loadLootTables();
    }

    /** Mirrors Bukkit {@code BukkitLootManager#toInventory} (telekinesis NBT not checked on Fabric). */
    public boolean toInventory(ItemStack held) {
        return plugin.configBoolean(Option.LOOT_DIRECTLY_TO_INVENTORY);
    }

    public List<CustomItemParser> getCustomItemParsers() {
        return customItemParsers;
    }

    public void registerCustomItemParser(CustomItemParser customItemParser) {
        customItemParsers.add(customItemParser);
    }

    public FabricGraniteSkills getFabricPlugin() {
        return plugin;
    }
}
