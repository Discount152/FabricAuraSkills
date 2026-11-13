package dev.granite.graniteskills.fabric.loot;

import dev.granite.graniteskills.common.ref.ItemRef;
import net.minecraft.world.item.ItemStack;

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
