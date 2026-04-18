package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.stat.ReloadableIdentifier;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.stat.StatManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.trait.FabricTraitHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricStatManager extends StatManager {

    public FabricStatManager(AuraSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public <T extends ReloadableIdentifier> void reload(User user, T type) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        if (type instanceof Stat stat) {
            reloadStat(user, player, stat);
        } else if (type instanceof Trait trait) {
            reloadTrait(user, player, trait);
        }
    }

    @Override
    public void reloadAllTraits(User user) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        FabricTraitManager tm = (FabricTraitManager) plugin.getTraitManager();
        for (FabricTraitHandler impl : tm.getAllTraitImpls()) {
            for (Trait trait : impl.getTraits()) {
                impl.onReload(player, user, trait);
            }
        }
    }

    private void reloadStat(User user, ServerPlayerEntity player, Stat stat) {
        if (!stat.isEnabled()) {
            return;
        }
        for (Trait trait : stat.getTraits()) {
            reloadTrait(user, player, trait);
        }
    }

    private void reloadTrait(User user, ServerPlayerEntity player, Trait trait) {
        FabricTraitHandler traitImpl = ((FabricTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
        if (traitImpl == null) {
            return;
        }
        traitImpl.onReload(player, user, trait);
    }
}
