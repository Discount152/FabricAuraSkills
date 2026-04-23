package dev.aurelium.auraskills.fabric;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.screen.slot.SlotActionType;
import org.junit.jupiter.api.Test;

class FabricGrindstoneXpHandlerTest {

    @Test
    void pickupOrPickupAll_allowedWithEmptyCursorEvenIfStorageFull() {
        assertTrue(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.PICKUP, true, true));
        assertTrue(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.PICKUP_ALL, true, true));
    }

    @Test
    void nonEmptyCursor_disallowed() {
        assertFalse(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.PICKUP, false, false));
    }

    @Test
    void quickMoveWithFullStorage_disallowed() {
        assertFalse(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.QUICK_MOVE, true, true));
    }

    @Test
    void quickMoveWithSpace_allowed() {
        assertTrue(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.QUICK_MOVE, true, false));
    }

    @Test
    void cloneOrQuickCraftNotWhitelisted() {
        assertFalse(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.CLONE, true, false));
        assertFalse(FabricGrindstoneXpHandler.allowsGrindstoneClickForXp(SlotActionType.QUICK_CRAFT, true, false));
    }
}
