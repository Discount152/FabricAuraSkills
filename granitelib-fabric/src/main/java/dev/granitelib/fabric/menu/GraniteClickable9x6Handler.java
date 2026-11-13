package dev.granitelib.fabric.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 6×9 {@link MenuType#GENERIC_9x6} container. Top slots never allow item movement; when
 * {@code topClick != null}, it is notified for each container click (read-only + interaction).
 * When {@code topClick == null}, behavior matches the former read-only handler.
 */
public final class GraniteClickable9x6Handler extends ChestMenu {

    public static final int ROWS = 6;
    public static final int CONTAINER_SLOTS = ROWS * 9;

    private final @Nullable GraniteTopSlotClickHandler topClick;

    public GraniteClickable9x6Handler(
            int syncId,
            Inventory playerInventory,
            Container containerInventory,
            @Nullable GraniteTopSlotClickHandler topClick) {
        super(MenuType.GENERIC_9x6, syncId, playerInventory, containerInventory, ROWS);
        this.topClick = topClick;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        if (slotIndex >= 0 && slotIndex < CONTAINER_SLOTS) {
            if (topClick != null && player instanceof ServerPlayer sp) {
                topClick.onTopSlot(slotIndex, sp, button, actionType);
            }
            return;
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int fromIndex) {
        if (fromIndex >= 0 && fromIndex < CONTAINER_SLOTS) {
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(player, fromIndex);
    }
}
