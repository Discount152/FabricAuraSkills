package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class FabricMaxManaTrait extends FabricTraitImpl {

    public FabricMaxManaTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.MAX_MANA);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        return Traits.MAX_MANA.optionDouble("base", 20);
    }

    @Override
    protected void reload(ServerPlayer player, Trait trait) {
        if (!trait.optionBoolean("allow_overflow", false)) {
            User user = plugin.getUserManager().getUser(player.getUUID());
            double maxMana = user.getMaxMana();
            if (user.getMana() > maxMana) {
                user.setMana(maxMana);
            }
        }
    }

    @Override
    protected void onWorldChanged(ServerPlayer player, ServerLevel from, ServerLevel to, Trait trait) {
        if (!trait.equals(Traits.MAX_MANA)) {
            return;
        }
        reload(player, trait);
    }
}
