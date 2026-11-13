package dev.granite.graniteskills.common.loot;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.common.loot.enchant.LootEnchantments;
import dev.granite.graniteskills.common.ref.ItemRef;
import org.jetbrains.annotations.Nullable;

public record ItemSupplier(
        @Nullable ItemRef baseItem,
        @Nullable NamespacedId baseItemKey,
        @Nullable LootEnchantments enchantments
) {}
