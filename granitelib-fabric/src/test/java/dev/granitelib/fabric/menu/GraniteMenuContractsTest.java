package dev.granitelib.fabric.menu;

import net.minecraft.world.inventory.ContainerInput;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GraniteMenuContractsTest {

    @Test
    void clickKindsStayDistinct() {
        assertEquals(GraniteClickKind.PICKUP, GraniteClickKind.from(ContainerInput.PICKUP));
        assertEquals(GraniteClickKind.QUICK_MOVE, GraniteClickKind.from(ContainerInput.QUICK_MOVE));
        assertEquals(GraniteClickKind.SWAP, GraniteClickKind.from(ContainerInput.SWAP));
        assertEquals(GraniteClickKind.THROW, GraniteClickKind.from(ContainerInput.THROW));
        assertEquals(GraniteClickKind.OTHER, GraniteClickKind.from(null));
    }

    @Test
    void backReturnsThePreviousMenuAndDoesNotDropTheRoot() {
        UUID player = UUID.randomUUID();
        GraniteMenuHistory.clear(player);
        GraniteMenuHistory.push(player, "skills");
        GraniteMenuHistory.push(player, "level_progression");
        assertEquals("level_progression", GraniteMenuHistory.current(player));
        assertEquals("skills", GraniteMenuHistory.back(player));
        assertNull(GraniteMenuHistory.back(player));
        assertEquals("skills", GraniteMenuHistory.current(player));
        GraniteMenuHistory.clear(player);
    }
}
