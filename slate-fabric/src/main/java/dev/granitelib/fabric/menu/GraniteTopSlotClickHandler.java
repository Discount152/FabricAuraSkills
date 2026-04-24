package dev.granitelib.fabric.menu;

import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
/**
 * Invoked (main thread) when the player uses a top (container) slot in
 * {@link GraniteClickable9x6Handler}. The handler should not move items; implementors that open
 * other screens may schedule work on the next server tick to avoid re-entrancy.
 */
@FunctionalInterface
public interface GraniteTopSlotClickHandler {

    /**
     * @param topSlot 0..53 for a 6×9 container
     */
    void onTopSlot(int topSlot, ServerPlayerEntity player, int button, SlotActionType actionType);
}
