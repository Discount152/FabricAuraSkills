package dev.granite.graniteskills.fabric.menu;

import net.minecraft.world.inventory.ContainerInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMenuClicksTest {

    @Test
    void onlyAPickupActivatesABuiltInMenu() {
        assertTrue(FabricMenuClicks.activates(ContainerInput.PICKUP));
        assertFalse(FabricMenuClicks.activates(ContainerInput.QUICK_MOVE));
        assertFalse(FabricMenuClicks.activates(ContainerInput.SWAP));
        assertFalse(FabricMenuClicks.activates(ContainerInput.CLONE));
        assertFalse(FabricMenuClicks.activates(ContainerInput.THROW));
        assertFalse(FabricMenuClicks.activates(ContainerInput.QUICK_CRAFT));
        assertFalse(FabricMenuClicks.activates(ContainerInput.PICKUP_ALL));
        assertFalse(FabricMenuClicks.activates(null));
    }
}
