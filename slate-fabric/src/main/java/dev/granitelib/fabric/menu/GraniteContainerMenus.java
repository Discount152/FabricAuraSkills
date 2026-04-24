package dev.granitelib.fabric.menu;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Minimal GraniteLib entry point for server-side block-entity style menus. Future Slate-like menus
 * can compose multiple builders on top; this is the first shared primitive.
 */
public final class GraniteContainerMenus {

    private GraniteContainerMenus() {
    }

    /**
     * Opens a 6×9 read-only {@link GraniteReadOnly9x6Handler} filled by {@code populate} on the
     * main thread before the screen opens.
     */
    public static void openReadOnly9x6(ServerPlayerEntity player, Text title, Consumer<SimpleInventory> populate) {
        SimpleInventory inv = new SimpleInventory(GraniteReadOnly9x6Handler.ROWS * 9);
        populate.accept(inv);
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) ->
                        new GraniteReadOnly9x6Handler(syncId, playerInventory, inv),
                title));
    }
}
