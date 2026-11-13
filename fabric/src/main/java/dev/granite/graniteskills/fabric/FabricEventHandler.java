package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.common.event.EventHandler;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.data.Pair;
import dev.granite.graniteskills.fabric.ability.FabricEnchantingAbilities;
import dev.granite.graniteskills.fabric.ability.FabricForgingAbilities;
import dev.granite.graniteskills.fabric.trait.FabricGatheringLuckTraits;
import org.jetbrains.annotations.Nullable;

/**
 * Fires the Fabric listener bus used by Granitelib, then the built-in ability side effects.
 */
public final class FabricEventHandler implements EventHandler {

    @Override
    public void callUserLoadEvent(User user) {
        for (FabricSkillsListeners.Listener listener : FabricSkillsListeners.listeners()) {
            listener.onUserLoad(user);
        }
    }

    @Override
    public void callSkillLevelUpEvent(User user, Skill skill, int level) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin != null) {
            var luckTrait = FabricGatheringLuckTraits.traitForSkill(skill);
            if (luckTrait != null) {
                plugin.getStatManager().reload(user, luckTrait);
            }
        }
        for (FabricSkillsListeners.Listener listener : FabricSkillsListeners.listeners()) {
            listener.onSkillLevelUp(user, skill, level);
        }
    }

    @Override
    public Pair<Boolean, Double> callXpGainEvent(User user, Skill skill, @Nullable XpSource source, double amount) {
        boolean cancelled = false;
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin != null && plugin.getAntiAfkManager() instanceof FabricAntiAfkManager antiAfk
                && antiAfk.shouldCancel(user, source)) {
            cancelled = true;
        }
        for (FabricSkillsListeners.Listener listener : FabricSkillsListeners.listeners()) {
            Pair<Boolean, Double> next = listener.onXpGain(user, skill, source, amount);
            if (next != null) {
                cancelled = next.first();
                amount = next.second();
            }
        }
        if (!cancelled && plugin != null) {
            FabricEnchantingAbilities.onSkillXp(plugin, user, amount);
            FabricForgingAbilities.onSkillXp(plugin, user, amount);
        }
        return new Pair<>(cancelled, amount);
    }
}
