package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;

import java.lang.reflect.Field;

public final class FabricAnvilDiscount {

    private static final Field COST = findCost();

    private FabricAnvilDiscount() {
    }

    public static void apply(ServerPlayer player, AnvilMenu menu) {
        if (!Traits.ANVIL_DISCOUNT.isEnabled() || COST == null) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player))) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        double traitValue = user.getEffectiveTraitLevel(Traits.ANVIL_DISCOUNT);
        if (traitValue <= 0.0) {
            return;
        }
        double discount = -1.0 * Math.pow(1.025, -1.0 * traitValue) + 1;
        discount = Math.max(0.0, Math.min(0.99, discount));
        int cost = menu.getCost();
        int next = (int) Math.round(cost * (1.0 - discount));
        if (next < 1) {
            next = 1;
        }
        try {
            if (COST.get(menu) instanceof DataSlot slot) {
                slot.set(next);
            }
        } catch (IllegalAccessException ignored) {
            // Leave the vanilla cost when the field cannot be written.
        }
    }

    private static Field findCost() {
        try {
            Field field = AnvilMenu.class.getDeclaredField("cost");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
