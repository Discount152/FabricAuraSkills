package dev.aurelium.auraskills.fabric;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Bukkit {@code AnvilLeveler} reads the left/right stacks and anvil view repair cost in the
 * {@code InventoryClickEvent} (effectively <strong>before</strong> input consumption). Vanilla applies input changes
 * before {@link net.minecraft.screen.ForgingScreenHandler#onTakeOutput}; we record a one-shot snapshot at
 * {@code ForgingScreenHandler#onTake} HEAD for the <strong>output</strong> slot so {@link FabricAnvilXpHandler} can
 * match {@link dev.aurelium.auraskills.api.source.type.AnvilXpSource} filters and substitute {@code {repair_cost}} with
 * the same cost the player was charged, without relying on post-take slot state.
 */
public final class FabricAnvilTakeContext {

    public record Pending(ItemStack left, ItemStack right, int levelCost) {
        public Pending {
            left = left.copy();
            right = right.copy();
        }
    }

    private static final ThreadLocal<@Nullable Pending> PENDING = new ThreadLocal<>();

    private FabricAnvilTakeContext() {
    }

    /**
     * Called from {@code AnvilScreenHandler} mixin: {@code onTake} HEAD for the result slot, before the handler body
     * mutates input stacks.
     */
    public static void setPendingForOutputTake(Pending pending) {
        PENDING.set(pending);
    }

    /**
     * @return and clears the pending snapshot (one use per take).
     */
    public static @Nullable Pending takePending() {
        Pending p = PENDING.get();
        PENDING.remove();
        return p;
    }

    public static void clear() {
        PENDING.remove();
    }
}
