package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.item.ItemCategory;
import dev.aurelium.auraskills.api.item.ItemFilterMeta;
import dev.aurelium.auraskills.api.item.LootItemFilter;
import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootPool;
import dev.aurelium.auraskills.api.loot.LootTable;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.loot.FabricItemLoot;
import dev.aurelium.auraskills.fabric.loot.FabricLootTemplateSupplier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

final class FabricLootItemMatcher {

    private FabricLootItemMatcher() {
    }

    /**
     * Matches {@link dev.aurelium.auraskills.api.item.ItemFilter} for non-loot sources (consume, anvil side, enchanting, potion held stack).
     */
    static boolean passesApiItemFilter(FabricAuraSkills plugin, ItemStack stack, dev.aurelium.auraskills.api.item.ItemFilter filter, Skill skill) {
        if (filter instanceof LootItemFilter lootFilter) {
            return passesLootFilter(plugin, stack, lootFilter, skill);
        }
        return passesItemFilter(stack, filter.materials(), filter.excludedMaterials(), filter.category(), filter.meta());
    }

    static boolean passesLootFilter(FabricAuraSkills plugin, ItemStack stack, LootItemFilter filter, Skill skill) {
        if (filter.lootPool() != null) {
            LootTable table = plugin.getApiProvider().getLootManager().getLootTable(skill.getId());
            if (table == null) {
                return false;
            }
            LootPool pool = table.getPool(filter.lootPool());
            if (pool == null) {
                return false;
            }
            for (Loot loot : pool.getLoot()) {
                if (!(loot instanceof FabricItemLoot itemLoot)) {
                    continue;
                }
                ItemStack template = FabricLootTemplateSupplier.supplyTemplate(itemLoot.getItem());
                if (template == null || template.isEmpty()) {
                    continue;
                }
                if (ItemStack.areItemsEqual(stack, template) || ItemStack.areEqual(stack, template)) {
                    return true;
                }
            }
            return false;
        }
        return passesItemFilter(stack, filter.materials(), filter.excludedMaterials(), filter.category(), filter.meta());
    }

    private static boolean passesItemFilter(ItemStack stack, @Nullable String[] materials, @Nullable String[] excludedMaterials,
            @Nullable ItemCategory category, ItemFilterMeta meta) {
        if (stack.isEmpty()) {
            return false;
        }
        String typeKey = Registries.ITEM.getId(stack.getItem()).getPath();
        if (materials != null && !TextUtil.contains(materials, typeKey)) {
            return false;
        }
        if (excludedMaterials != null) {
            for (String excluded : excludedMaterials) {
                if (excluded != null && excluded.equalsIgnoreCase(typeKey)) {
                    return false;
                }
            }
        }
        if (category != null) {
            Set<ItemCategory> categories = FabricItemCategories.categoriesFor(stack);
            if (!categories.contains(category)) {
                return false;
            }
        }
        return passesItemMeta(stack, meta);
    }

    private static boolean passesItemMeta(ItemStack stack, ItemFilterMeta filterMeta) {
        if (filterMeta == null) {
            return true;
        }
        if (filterMeta.potionData() != null) {
            return false;
        }
        if (!filterMeta.ignoreCustomModelData()) {
            var cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (filterMeta.hasCustomModelData()) {
                if (cmd == null || cmd.equals(CustomModelDataComponent.DEFAULT)) {
                    return false;
                }
                if (filterMeta.customModelData() != Integer.MIN_VALUE) {
                    Integer c = cmd.getColor(0);
                    if (c == null || c != filterMeta.customModelData()) {
                        return false;
                    }
                }
            } else {
                if (cmd != null && !cmd.equals(CustomModelDataComponent.DEFAULT)) {
                    return false;
                }
            }
        }
        if (filterMeta.displayName() != null) {
            Text custom = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (custom == null) {
                return false;
            }
            if (!Objects.equals(custom.getString(), filterMeta.displayName())) {
                return false;
            }
        }
        List<String> filterLore = filterMeta.lore();
        if (filterLore != null && !filterLore.isEmpty()) {
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore == null) {
                return false;
            }
            List<Text> lines = lore.lines();
            if (lines.size() != filterLore.size()) {
                return false;
            }
            for (int i = 0; i < lines.size(); i++) {
                if (!Objects.equals(lines.get(i).getString(), filterLore.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
