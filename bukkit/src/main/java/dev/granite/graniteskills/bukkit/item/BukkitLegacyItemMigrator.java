package dev.granite.graniteskills.bukkit.item;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import dev.granite.graniteskills.common.migration.LegacyIdentity;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Rewrites item NBT saved under AuraSkills or AureliumSkills names.
 */
public final class BukkitLegacyItemMigrator {

    private static final String PUBLIC_BUKKIT_VALUES = "PublicBukkitValues";
    private static final int MAX_DEPTH = 8;

    private BukkitLegacyItemMigrator() {
    }

    public static boolean migrate(ItemStack stack) {
        return migrate(stack, 0);
    }

    private static boolean migrate(ItemStack stack, int depth) {
        if (stack == null || stack.getType().isAir() || depth > MAX_DEPTH) {
            return false;
        }
        boolean changed = migrateContents(stack, depth);
        boolean[] nbtChanged = {false};
        NBT.modify(stack, nbt -> {
            nbtChanged[0] = adopt(nbt);
        });
        return changed || nbtChanged[0];
    }

    private static boolean migrateContents(ItemStack stack, int depth) {
        ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockStateMeta)) {
            return false;
        }
        if (!(blockStateMeta.getBlockState() instanceof InventoryHolder holder)) {
            return false;
        }
        ItemStack[] contents = holder.getInventory().getContents();
        boolean changed = false;
        for (ItemStack inner : contents) {
            changed |= migrate(inner, depth + 1);
        }
        if (!changed) {
            return false;
        }
        holder.getInventory().setContents(contents);
        blockStateMeta.setBlockState((BlockState) holder);
        stack.setItemMeta(blockStateMeta);
        return true;
    }

    public static boolean adopt(ReadWriteNBT item) {
        if (item == null) {
            return false;
        }
        boolean changed = adoptRoots(item);
        changed |= rewriteCompound(item.getCompound(LegacyIdentity.NBT_ROOT));
        changed |= rewriteCompound(item.getCompound(PUBLIC_BUKKIT_VALUES));
        return changed;
    }

    private static boolean adoptRoots(ReadWriteNBT item) {
        boolean changed = false;
        for (String oldRoot : LegacyIdentity.OLD_NBT_ROOTS) {
            ReadWriteNBT legacy = item.getCompound(oldRoot);
            if (legacy == null) {
                continue;
            }
            ReadWriteNBT current = item.getCompound(LegacyIdentity.NBT_ROOT);
            if (current == null) {
                item.getOrCreateCompound(LegacyIdentity.NBT_ROOT).mergeCompound(legacy);
            } else {
                current.mergeCompound(legacy);
            }
            item.removeKey(oldRoot);
            changed = true;
        }
        return changed;
    }

    private static boolean rewriteCompound(ReadWriteNBT compound) {
        if (compound == null) {
            return false;
        }
        boolean changed = false;
        for (String key : new ArrayList<>(compound.getKeys())) {
            String rewrittenKey = LegacyIdentity.rewriteStoredString(key);
            if (!rewrittenKey.equals(key) && copyKey(compound, key, rewrittenKey)) {
                compound.removeKey(key);
                key = rewrittenKey;
                changed = true;
            }
            NBTType type = compound.getType(key);
            if (type == NBTType.NBTTagString) {
                String value = compound.getString(key);
                String rewritten = LegacyIdentity.rewriteStoredString(value);
                if (value != null && !rewritten.equals(value)) {
                    compound.setString(key, rewritten);
                    changed = true;
                }
            } else if (type == NBTType.NBTTagCompound) {
                changed |= rewriteCompound(compound.getCompound(key));
            } else if (type == NBTType.NBTTagList) {
                changed |= rewriteList(compound, key);
            }
        }
        return changed;
    }

    private static boolean rewriteList(ReadWriteNBT compound, String key) {
        try {
            ReadWriteNBTList<String> strings = compound.getStringList(key);
            boolean changed = false;
            for (int i = 0; i < strings.size(); i++) {
                String value = strings.get(i);
                String rewritten = LegacyIdentity.rewriteStoredString(value);
                if (value != null && !rewritten.equals(value)) {
                    strings.set(i, rewritten);
                    changed = true;
                }
            }
            if (!strings.isEmpty() || changed) {
                return changed;
            }
        } catch (RuntimeException ignored) {
            // The list holds another tag type.
        }
        try {
            boolean changed = false;
            ReadWriteNBTCompoundList children = compound.getCompoundList(key);
            for (ReadWriteNBT child : children) {
                changed |= rewriteCompound(child);
            }
            return changed;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean copyKey(ReadWriteNBT compound, String from, String to) {
        NBTType type = compound.getType(from);
        if (type == null) {
            return false;
        }
        if (compound.hasTag(to)) {
            return true;
        }
        switch (type) {
            case NBTTagCompound -> compound.getOrCreateCompound(to).mergeCompound(compound.getCompound(from));
            case NBTTagString -> compound.setString(to, compound.getString(from));
            case NBTTagDouble -> compound.setDouble(to, compound.getDouble(from));
            case NBTTagFloat -> compound.setFloat(to, compound.getFloat(from));
            case NBTTagInt -> compound.setInteger(to, compound.getInteger(from));
            case NBTTagLong -> compound.setLong(to, compound.getLong(from));
            case NBTTagShort -> compound.setShort(to, compound.getShort(from));
            case NBTTagByte -> compound.setByte(to, compound.getByte(from));
            case NBTTagByteArray -> compound.setByteArray(to, compound.getByteArray(from));
            case NBTTagIntArray -> compound.setIntArray(to, compound.getIntArray(from));
            case NBTTagLongArray -> compound.setLongArray(to, compound.getLongArray(from));
            case NBTTagList -> copyList(compound, from, to);
            default -> {
                return false;
            }
        }
        return compound.hasTag(to);
    }

    private static void copyList(ReadWriteNBT compound, String from, String to) {
        try {
            ReadWriteNBTCompoundList compounds = compound.getCompoundList(from);
            if (!compounds.isEmpty()) {
                ReadWriteNBTCompoundList destination = compound.getCompoundList(to);
                for (ReadWriteNBT entry : compounds) {
                    destination.addCompound(entry);
                }
                return;
            }
        } catch (RuntimeException ignored) {
            // Not a compound list.
        }
        try {
            ReadWriteNBTList<String> strings = compound.getStringList(from);
            if (!strings.isEmpty()) {
                ReadWriteNBTList<String> destination = compound.getStringList(to);
                for (int i = 0; i < strings.size(); i++) {
                    destination.add(strings.get(i));
                }
                return;
            }
        } catch (RuntimeException ignored) {
            // Not a string list.
        }
        try {
            ReadWriteNBTList<Double> doubles = compound.getDoubleList(from);
            if (!doubles.isEmpty()) {
                ReadWriteNBTList<Double> destination = compound.getDoubleList(to);
                for (int i = 0; i < doubles.size(); i++) {
                    destination.add(doubles.get(i));
                }
                return;
            }
        } catch (RuntimeException ignored) {
            // Not a double list.
        }
        try {
            ReadWriteNBTList<Integer> integers = compound.getIntegerList(from);
            if (!integers.isEmpty()) {
                ReadWriteNBTList<Integer> destination = compound.getIntegerList(to);
                for (int i = 0; i < integers.size(); i++) {
                    destination.add(integers.get(i));
                }
            }
        } catch (RuntimeException ignored) {
            // Leave numeric lists of other types on the old key.
        }
    }

}
