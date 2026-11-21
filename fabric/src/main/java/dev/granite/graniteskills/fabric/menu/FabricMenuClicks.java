package dev.granite.graniteskills.fabric.menu;

import dev.granitelib.fabric.menu.GraniteClickKind;
import net.minecraft.world.inventory.ContainerInput;

/**
 * Built-in menus act on a normal pickup. The other GraniteLib click kinds do not navigate.
 */
public final class FabricMenuClicks {

    private FabricMenuClicks() {
    }

    public static boolean activates(ContainerInput input) {
        return GraniteClickKind.from(input) == GraniteClickKind.PICKUP;
    }
}
