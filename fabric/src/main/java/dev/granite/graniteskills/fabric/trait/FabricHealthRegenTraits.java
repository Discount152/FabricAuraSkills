package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public final class FabricHealthRegenTraits implements FabricTraitHandler {

    private final FabricGraniteSkills plugin;

    public FabricHealthRegenTraits(FabricGraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public Trait[] getTraits() {
        return new Trait[] {Traits.SATURATION_REGEN, Traits.HUNGER_REGEN};
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return 0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return "+" + NumberUtil.format2(value);
    }

    @Override
    public void onReload(ServerPlayer player, User user, Trait trait) {
    }

    public void tickPlayer(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null || !(player.level() instanceof ServerLevel)) {
            return;
        }
        if (plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player))) {
            return;
        }
        if (player.getFoodData().getSaturationLevel() > 0f && Traits.SATURATION_REGEN.isEnabled()) {
            heal(player, user.getBonusTraitLevel(Traits.SATURATION_REGEN));
        } else if (player.getFoodData().getFoodLevel() >= 14 && Traits.HUNGER_REGEN.isEnabled()) {
            heal(player, user.getBonusTraitLevel(Traits.HUNGER_REGEN));
        }
    }

    private static void heal(ServerPlayer player, double bonus) {
        if (bonus <= 0.0 || player.getHealth() >= player.getMaxHealth()) {
            return;
        }
        player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + bonus));
    }
}
