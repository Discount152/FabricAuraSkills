package dev.granite.graniteskills.fabric.item;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IgnoreInteractClicksTest {

    @Test
    void ignoreInteractStopsTheRightClickHandlerAndAllowsAnOrdinaryClick() {
        CompoundTag values = new CompoundTag();
        values.put(FabricItemIgnoreTags.IGNORE_INTERACT_KEY, ByteTag.ONE);
        CompoundTag root = new CompoundTag();
        root.put("PublicBukkitValues", values);
        assertTrue(FabricItemIgnoreTags.ignores(root));

        boolean ignore = FabricItemIgnoreTags.ignores(root);
        int[] ran = {0};
        InteractionResult result = IgnoreInteractClicks.finishRightClick(ignore, () -> ran[0]++);
        assertEquals(0, ran[0]);
        assertEquals(InteractionResult.PASS, result);
        assertFalse(IgnoreInteractClicks.runRightClickHandler(ignore));
        assertTrue(IgnoreInteractClicks.allowOrdinaryClick(ignore));
    }

    @Test
    void itemWithoutTheFlagStillRunsTheRightClickHandler() {
        assertFalse(FabricItemIgnoreTags.ignores(new CompoundTag()));
        int[] ran = {0};
        InteractionResult result = IgnoreInteractClicks.finishRightClick(false, () -> ran[0]++);
        assertEquals(1, ran[0]);
        assertEquals(InteractionResult.PASS, result);
        assertTrue(IgnoreInteractClicks.allowOrdinaryClick(false));
    }
}
