package dev.granite.graniteskills.bukkit.stat;

import dev.granite.graniteskills.api.bukkit.BukkitTraitHandler;
import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.bukkit.trait.BukkitTraitManager;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.stat.StatManager;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;

public class BukkitStatManager extends StatManager {

    public BukkitStatManager(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public <T extends ReloadableIdentifier> void reload(User user, T type) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player == null) return;

        if (type instanceof Stat stat) {
            reloadStat(user, stat);
        } else if (type instanceof Trait trait) {
            reloadTrait(user, player, trait);
        }
    }

    @Override
    public void reloadAllTraits(User user) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player == null) return;

        for (BukkitTraitHandler impl : ((BukkitTraitManager) plugin.getTraitManager()).getAllTraitImpls()) {
            for (Trait trait : impl.getTraits()) {
                impl.onReload(player, user.toApi(), trait);
            }
        }
    }

    private void reloadStat(User user, Stat stat) {
        if (!stat.isEnabled()) return;
        Player player = ((BukkitUser) user).getPlayer();
        if (player == null) return;
        // Reload traits
        for (Trait trait : stat.getTraits()) {
            reloadTrait(user, player, trait);
        }
    }

    private void reloadTrait(User user, Player player, Trait trait) {
        BukkitTraitHandler traitImpl = ((BukkitTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
        if (traitImpl == null) return;

        traitImpl.onReload(player, user.toApi(), trait);
    }

}
