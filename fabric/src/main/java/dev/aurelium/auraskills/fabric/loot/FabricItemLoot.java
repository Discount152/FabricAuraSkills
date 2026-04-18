package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootValues;
import dev.aurelium.auraskills.common.loot.ItemSupplier;

/**
 * Fabric counterpart to Bukkit {@link dev.aurelium.auraskills.bukkit.loot.type.ItemLoot} for YAML-loaded loot tables.
 */
public final class FabricItemLoot extends Loot {

    private final ItemSupplier item;
    private final int minAmount;
    private final int maxAmount;
    private final double minDamage;
    private final double maxDamage;

    public FabricItemLoot(LootValues values, ItemSupplier item, int minAmount, int maxAmount, double minDamage, double maxDamage) {
        super(values);
        this.item = item;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
    }

    public ItemSupplier getItem() {
        return item;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public double getMinDamage() {
        return minDamage;
    }

    public double getMaxDamage() {
        return maxDamage;
    }
}
