package dev.granitelib.fabric.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Server-side 6×9 menu entry points. Read-only = {@code topClick == null}.
 */
public final class GraniteContainerMenus {

    private GraniteContainerMenus() {
    }

    /**
     * Opens a 6×9 menu with no top-slot click handling (items are display-only in the top grid).
     */
    public static void openReadOnly9x6(ServerPlayer player, Component title, Consumer<SimpleContainer> populate) {
        openClickable9x6(player, title, populate, null);
    }

    /**
     * Opens a 6×9 menu; {@code topClick} is invoked for interactions in the top 54 slots (items are
     * never moved by the handler).
     */
    public static void openClickable9x6(
            ServerPlayer player,
            Component title,
            Consumer<SimpleContainer> populate,
            @Nullable GraniteTopSlotClickHandler topClick) {
        SimpleContainer inv = new SimpleContainer(GraniteClickable9x6Handler.CONTAINER_SLOTS);
        populate.accept(inv);
        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInventory, playerEntity) ->
                        new GraniteClickable9x6Handler(syncId, playerInventory, inv, topClick),
                title));
    }
}
