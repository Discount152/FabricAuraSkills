package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.stat.StatManager;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.trait.FabricTraitHandler;
import net.minecraft.server.level.ServerPlayer;

public final class FabricStatManager extends StatManager {

    public FabricStatManager(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public <T extends ReloadableIdentifier> void reload(User user, T type) {
        ServerPlayer player = FabricUser.getPlayer(user);
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
        ServerPlayer player = FabricUser.getPlayer(user);
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

    private void reloadStat(User user, ServerPlayer player, Stat stat) {
        if (!stat.isEnabled()) {
            return;
        }
        for (Trait trait : stat.getTraits()) {
            reloadTrait(user, player, trait);
        }
    }

    private void reloadTrait(User user, ServerPlayer player, Trait trait) {
        FabricTraitHandler traitImpl = ((FabricTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
        if (traitImpl == null) {
            return;
        }
        traitImpl.onReload(player, user, trait);
    }
}
