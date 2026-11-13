package dev.granite.graniteskills.api.bukkit;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.TraitHandler;
import dev.granite.graniteskills.api.user.SkillsUser;
import org.bukkit.entity.Player;

public interface BukkitTraitHandler extends TraitHandler {

    double getBaseLevel(Player player, Trait trait);

    void onReload(Player player, SkillsUser user, Trait trait);

}
