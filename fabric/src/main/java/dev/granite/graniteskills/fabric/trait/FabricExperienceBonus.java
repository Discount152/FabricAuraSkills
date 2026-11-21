package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

public final class FabricExperienceBonus {

    private FabricExperienceBonus() {
    }

    public static int scale(ServerPlayer player, int amount) {
        if (amount <= 0 || !Traits.EXPERIENCE_BONUS.isEnabled()) {
            return amount;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return amount;
        }
        if (plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player))) {
            return amount;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return amount;
        }
        double bonus = user.getEffectiveTraitLevel(Traits.EXPERIENCE_BONUS) / 100.0;
        return (int) (amount * (1.0 + bonus));
    }
}
