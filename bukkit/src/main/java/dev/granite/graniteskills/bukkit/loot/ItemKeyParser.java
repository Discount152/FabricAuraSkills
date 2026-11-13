package dev.granite.graniteskills.bukkit.loot;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.loot.CustomItemParser;
import dev.granite.graniteskills.common.loot.ItemSupplier;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import static dev.granite.graniteskills.bukkit.ref.BukkitItemRef.wrap;

public class ItemKeyParser implements CustomItemParser {

    private final GraniteSkills plugin;

    public ItemKeyParser(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean shouldUseParser(ConfigurationNode config) {
        return !config.node("key").virtual();
    }

    @Override
    public ItemSupplier parseCustomItem(ConfigurationNode config) {
        NamespacedId itemKey = NamespacedId.fromDefaultWithColon(config.node("key").getString(""));
        ItemStack item = plugin.getItemRegistry().getItem(itemKey);
        if (item != null) {
            return new ItemSupplier(wrap(item), null, null);
        } else {
            return new ItemSupplier(null, itemKey, null);
        }
    }

}
