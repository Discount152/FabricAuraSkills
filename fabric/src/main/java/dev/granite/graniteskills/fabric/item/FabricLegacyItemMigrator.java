package dev.granite.graniteskills.fabric.item;

import dev.granite.graniteskills.common.migration.LegacyIdentity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites item custom data saved under AuraSkills or AureliumSkills names.
 */
public final class FabricLegacyItemMigrator {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final int MAX_DEPTH = 8;

    private FabricLegacyItemMigrator() {
    }

    public static boolean migrate(ItemStack stack) {
        return migrate(stack, 0);
    }

    private static boolean migrate(ItemStack stack, int depth) {
        if (stack == null || stack.isEmpty() || depth > MAX_DEPTH) {
            return false;
        }
        boolean changed = rewriteCustomData(stack);
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return changed;
        }
        List<ItemStack> copies = contents.itemCopies().toList();
        boolean inner = false;
        for (ItemStack copy : copies) {
            inner |= migrate(copy, depth + 1);
        }
        if (inner) {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(copies));
            changed = true;
        }
        return changed;
    }

    private static boolean rewriteCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        CompoundTag copy = data.copyTag();
        if (!rewrite(copy)) {
            return false;
        }
        CustomData.set(DataComponents.CUSTOM_DATA, stack, copy);
        return true;
    }

    static boolean rewrite(CompoundTag tag) {
        boolean changed = adoptRoots(tag);
        changed |= rewriteCompound(tag.getCompound(LegacyIdentity.NBT_ROOT).orElse(null));
        changed |= rewriteCompound(tag.getCompound(PUBLIC_BUKKIT_VALUES).orElse(null));
        return changed;
    }

    private static boolean adoptRoots(CompoundTag tag) {
        boolean changed = false;
        for (String oldRoot : LegacyIdentity.OLD_NBT_ROOTS) {
            CompoundTag legacy = tag.getCompound(oldRoot).orElse(null);
            if (legacy == null) {
                continue;
            }
            CompoundTag current = tag.getCompound(LegacyIdentity.NBT_ROOT).orElse(null);
            if (current == null) {
                tag.put(LegacyIdentity.NBT_ROOT, legacy.copy());
            } else {
                current.merge(legacy.copy());
            }
            tag.remove(oldRoot);
            changed = true;
        }
        return changed;
    }

    private static boolean rewriteCompound(CompoundTag tag) {
        if (tag == null) {
            return false;
        }
        boolean changed = false;
        for (String key : new ArrayList<>(tag.keySet())) {
            String rewrittenKey = LegacyIdentity.rewriteStoredString(key);
            Tag value = tag.get(key);
            if (!rewrittenKey.equals(key)) {
                tag.remove(key);
                tag.put(rewrittenKey, value);
                key = rewrittenKey;
                changed = true;
            }
            if (value instanceof StringTag stringTag) {
                String rewritten = LegacyIdentity.rewriteStoredString(stringTag.value());
                if (!rewritten.equals(stringTag.value())) {
                    tag.putString(key, rewritten);
                    changed = true;
                }
            } else if (value instanceof CompoundTag child) {
                changed |= rewriteCompound(child);
            } else if (value instanceof ListTag list) {
                changed |= rewriteList(list);
            }
        }
        return changed;
    }

    private static boolean rewriteList(ListTag list) {
        boolean changed = false;
        for (int i = 0; i < list.size(); i++) {
            Tag element = list.get(i);
            if (element instanceof StringTag stringTag) {
                String rewritten = LegacyIdentity.rewriteStoredString(stringTag.value());
                if (!rewritten.equals(stringTag.value())) {
                    list.set(i, StringTag.valueOf(rewritten));
                    changed = true;
                }
            } else if (element instanceof CompoundTag child) {
                changed |= rewriteCompound(child);
            } else if (element instanceof ListTag nested) {
                changed |= rewriteList(nested);
            }
        }
        return changed;
    }

}
