package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class FabricMaxManaTrait extends FabricTraitImpl {

    public FabricMaxManaTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.MAX_MANA);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return Traits.MAX_MANA.optionDouble("base", 20);
    }

    @Override
    protected void reload(ServerPlayerEntity player, Trait trait) {
        if (!trait.optionBoolean("allow_overflow", false)) {
            User user = plugin.getUserManager().getUser(player.getUuid());
            double maxMana = user.getMaxMana();
            if (user.getMana() > maxMana) {
                user.setMana(maxMana);
            }
        }
    }

    @Override
    protected void onWorldChanged(ServerPlayerEntity player, ServerWorld from, ServerWorld to, Trait trait) {
        if (!trait.equals(Traits.MAX_MANA)) {
            return;
        }
        reload(player, trait);
    }
}
