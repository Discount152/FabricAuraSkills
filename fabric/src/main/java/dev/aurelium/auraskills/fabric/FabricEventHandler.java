package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.common.event.EventHandler;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.Pair;
import dev.aurelium.auraskills.fabric.trait.FabricGatheringLuckTraits;
import org.jetbrains.annotations.Nullable;

/**
 * MVP: does not fire Bukkit-style API events. Always allows XP with the computed amount.
 */
public final class FabricEventHandler implements EventHandler {

    @Override
    public void callUserLoadEvent(User user) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin != null) {
            plugin.getUiProvider().getActionBarManager().onIdleUserLoaded(user);
        }
    }

    @Override
    public void callSkillLevelUpEvent(User user, Skill skill, int level) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        Trait luckTrait = FabricGatheringLuckTraits.traitForSkill(skill);
        if (luckTrait != null) {
            plugin.getStatManager().reload(user, luckTrait);
        }
    }

    @Override
    public Pair<Boolean, Double> callXpGainEvent(User user, Skill skill, @Nullable XpSource source, double amount) {
        return new Pair<>(false, amount);
    }
}
