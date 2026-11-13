package dev.granite.graniteskills.bukkit.loot.item;

import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.loot.item.enchant.BukkitLootEnchantments;
import dev.granite.graniteskills.common.loot.ItemSupplier;
import dev.granite.graniteskills.common.ref.ItemRef;
import org.jetbrains.annotations.Nullable;

import static dev.granite.graniteskills.bukkit.ref.BukkitItemRef.wrap;

public class BukkitItemSupplier {

    private final ItemSupplier supplier;

    public BukkitItemSupplier(ItemSupplier supplier) {
        this.supplier = supplier;
    }

    @Nullable
    public ItemRef supplyItem(GraniteSkills plugin, LootTable table) {
        ItemRef baseItem = supplier.baseItem();
        ItemRef item = null;
        if (baseItem != null) {
            item = supplier.baseItem().clone();
        }
        if (supplier.baseItemKey() != null) {
            item = wrap(plugin.getItemRegistry().getItem(supplier.baseItemKey()));
        }
        // Select and apply enchantments
        if (supplier.enchantments() != null) {
            new BukkitLootEnchantments(supplier.enchantments()).applyEnchantments(item, plugin, table);
        }
        return item;
    }

}
