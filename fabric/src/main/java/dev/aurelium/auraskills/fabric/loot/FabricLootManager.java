package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.loot.context.FabricMobContextProvider;
import dev.aurelium.auraskills.common.loot.CustomItemParser;
import dev.aurelium.auraskills.common.loot.LootManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public final class FabricLootManager extends LootManager {

    private final FabricAuraSkills plugin;
    private final FabricLootLoader lootLoader;
    private final List<CustomItemParser> customItemParsers = new ArrayList<>();

    public FabricLootManager(FabricAuraSkills plugin) {
        super(plugin);
        this.plugin = plugin;
        this.lootLoader = new FabricLootLoader(plugin, this);
        registerContextProvider(new FabricMobContextProvider());
    }

    @Override
    public dev.aurelium.auraskills.api.loot.LootRequirements getLootRequirements(ConfigurationNode config) {
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

    public FabricAuraSkills getFabricPlugin() {
        return plugin;
    }
}
