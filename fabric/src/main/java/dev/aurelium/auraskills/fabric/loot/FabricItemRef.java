package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.common.ref.ItemRef;
import net.minecraft.item.ItemStack;

public final class FabricItemRef implements ItemRef {

    private final ItemStack stack;

    public FabricItemRef(ItemStack stack) {
        this.stack = stack;
    }

    public static FabricItemRef wrap(ItemStack stack) {
        return new FabricItemRef(stack.copy());
    }

    @Override
    public Object get() {
        return stack;
    }

    @Override
    public ItemRef clone() {
        return new FabricItemRef(stack.copy());
    }
}
