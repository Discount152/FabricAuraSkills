package dev.granite.graniteskills.fabric.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/**
 * {@code ignore_interact} stops the mana right-click handler. The ordinary click still passes through.
 */
public final class IgnoreInteractClicks {

    private IgnoreInteractClicks() {
    }

    public static boolean runRightClickHandler(boolean ignoreInteract) {
        return !ignoreInteract;
    }

    public static boolean allowOrdinaryClick(boolean ignoreInteract) {
        return true;
    }

    public static InteractionResult finishRightClick(boolean ignoreInteract, Runnable handler) {
        if (runRightClickHandler(ignoreInteract)) {
            handler.run();
        }
        return allowOrdinaryClick(ignoreInteract) ? InteractionResult.PASS : InteractionResult.FAIL;
    }

    public static InteractionResult finishRightClick(ItemStack stack, Runnable handler) {
        boolean ignore = stack != null && FabricItemIgnoreTags.ignoresManaAbilityInteract(stack);
        return finishRightClick(ignore, handler);
    }
}
