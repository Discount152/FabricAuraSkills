package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.item.ItemCategory;
import dev.granite.graniteskills.api.item.ItemFilterMeta;
import dev.granite.graniteskills.api.item.LootItemFilter;
import dev.granite.graniteskills.api.item.PotionData;
import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootPool;
import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.fabric.loot.FabricItemLoot;
import dev.granite.graniteskills.fabric.loot.FabricLootTemplateSupplier;
import dev.granite.graniteskills.common.util.text.TextUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

final class FabricLootItemMatcher {

    private FabricLootItemMatcher() {
    }

    /**
     * Matches {@link dev.granite.graniteskills.api.item.ItemFilter} for non-loot sources (consume, anvil side, enchanting, potion held stack).
     */
    static boolean passesApiItemFilter(FabricGraniteSkills plugin, ItemStack stack, dev.granite.graniteskills.api.item.ItemFilter filter, Skill skill) {
        if (filter instanceof LootItemFilter lootFilter) {
            return passesLootFilter(plugin, stack, lootFilter, skill);
        }
        return passesItemFilter(stack, filter.materials(), filter.excludedMaterials(), filter.category(), filter.meta());
    }

    static boolean passesLootFilter(FabricGraniteSkills plugin, ItemStack stack, LootItemFilter filter, Skill skill) {
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
                if (ItemStack.isSameItem(stack, template) || ItemStack.matches(stack, template)) {
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
        String typeKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!BrewingIngredientXp.matches(typeKey, materials, excludedMaterials)) {
            return false;
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
        if (filterMeta.potionData() != null && !matchesPotion(stack, filterMeta.potionData())) {
            return false;
        }
        if (!filterMeta.ignoreCustomModelData()) {
            var cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (filterMeta.hasCustomModelData()) {
                if (cmd == null || cmd.equals(CustomModelData.EMPTY)) {
                    return false;
                }
                if (filterMeta.customModelData() != Integer.MIN_VALUE) {
                    Integer c = cmd.getColor(0);
                    if (c == null || c != filterMeta.customModelData()) {
                        return false;
                    }
                }
            } else {
                if (cmd != null && !cmd.equals(CustomModelData.EMPTY)) {
                    return false;
                }
            }
        }
        if (filterMeta.displayName() != null) {
            Component custom = stack.get(DataComponents.CUSTOM_NAME);
            if (custom == null) {
                return false;
            }
            if (!Objects.equals(custom.getString(), filterMeta.displayName())) {
                return false;
            }
        }
        List<String> filterLore = filterMeta.lore();
        if (filterLore != null && !filterLore.isEmpty()) {
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore == null) {
                return false;
            }
            List<Component> lines = lore.lines();
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

    private static boolean matchesPotion(ItemStack stack, PotionData data) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return false;
        }
        String path = contents.potion().get().unwrapKey().map(key -> key.identifier().getPath()).orElse("");
        if (path.isEmpty()) {
            return false;
        }
        boolean extended = false;
        boolean upgraded = false;
        String type = path;
        if (path.startsWith("long_")) {
            extended = true;
            type = path.substring("long_".length());
        } else if (path.startsWith("strong_")) {
            upgraded = true;
            type = path.substring("strong_".length());
        }
        if (data.types() != null && !TextUtil.contains(data.types(), type) && !TextUtil.contains(data.types(), path)) {
            return false;
        }
        if (data.excludedTypes() != null
                && (TextUtil.contains(data.excludedTypes(), type) || TextUtil.contains(data.excludedTypes(), path))) {
            return false;
        }
        if (data.extended() != extended || data.upgraded() != upgraded) {
            return false;
        }
        if (data.excludeNegative() && isNegativePotion(type)) {
            return false;
        }
        return true;
    }

    private static boolean isNegativePotion(String type) {
        return "poison".equals(type) || "slowness".equals(type) || "weakness".equals(type) || "harming".equals(type);
    }
}
