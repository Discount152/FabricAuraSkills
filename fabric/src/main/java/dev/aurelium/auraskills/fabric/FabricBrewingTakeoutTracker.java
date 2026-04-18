package dev.aurelium.auraskills.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks per-stand bottle slots for brewing sources with {@code TAKEOUT} trigger (Paper {@code BrewingLeveler}).
 */
public final class FabricBrewingTakeoutTracker {

    public record BrewStandKey(RegistryKey<World> worldKey, BlockPos pos) {
        public BrewStandKey {
            pos = pos.toImmutable();
        }
    }

    private static final ConcurrentHashMap<BrewStandKey, BrewingStandData> STANDS = new ConcurrentHashMap<>();

    private FabricBrewingTakeoutTracker() {
    }

    public static void remove(ServerWorld world, BlockPos pos) {
        STANDS.remove(new BrewStandKey(world.getRegistryKey(), pos));
    }

    /**
     * After a brew step, mark slots whose bottle stacks changed while still non-empty (vanilla potion output).
     */
    public static void applyCraft(
            ServerWorld world,
            BlockPos pos,
            ItemStack ingredientUsed,
            ItemStack[] bottlesBefore012,
            ItemStack[] bottlesAfter012) {
        if (ingredientUsed.isEmpty()) {
            return;
        }
        BrewStandKey key = new BrewStandKey(world.getRegistryKey(), pos);
        BrewingStandData standData = STANDS.computeIfAbsent(key, k -> new BrewingStandData());
        for (int i = 0; i < 3; i++) {
            ItemStack beforeItem = bottlesBefore012[i];
            ItemStack afterItem = bottlesAfter012[i];
            if (!beforeItem.isEmpty() && !afterItem.isEmpty() && !ItemStack.areEqual(beforeItem, afterItem)) {
                BrewingSlot slot = standData.getSlot(i);
                slot.setBrewed(true);
                if (slot.getIngredients().size() < 5) {
                    slot.addIngredient(ingredientUsed.copy());
                }
            }
        }
    }

    public static @Nullable BrewingSlot getSlot(ServerWorld world, BlockPos pos, int slot0to2) {
        BrewingStandData data = STANDS.get(new BrewStandKey(world.getRegistryKey(), pos));
        if (data == null) {
            return null;
        }
        return data.getSlot(slot0to2);
    }

    public static final class BrewingStandData {

        private final Map<Integer, BrewingSlot> slots = new ConcurrentHashMap<>();

        public BrewingSlot getSlot(int slot) {
            return slots.computeIfAbsent(slot, s -> new BrewingSlot());
        }
    }

    public static final class BrewingSlot {

        private boolean brewed;
        private final List<ItemStack> ingredients = new ArrayList<>();

        public synchronized boolean isBrewed() {
            return brewed;
        }

        public synchronized void setBrewed(boolean brewed) {
            this.brewed = brewed;
        }

        /** Snapshot for XP attribution (do not mutate). */
        public synchronized List<ItemStack> getIngredients() {
            return List.copyOf(ingredients);
        }

        public synchronized void addIngredient(ItemStack item) {
            ingredients.add(item);
        }

        public synchronized void resetIngredients() {
            ingredients.clear();
        }
    }
}
