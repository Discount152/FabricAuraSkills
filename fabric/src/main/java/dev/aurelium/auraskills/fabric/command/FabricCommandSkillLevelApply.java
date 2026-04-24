package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Bukkit {@code SkillCommand#onSkillSetlevel} side effects: level + XP clear, stats, reward
 * commands, modifiers (**Sprint 42**). Call only after validating {@code newLevel} &gt;=
 * {@code plugin.config().getStartLevel()}.
 */
public final class FabricCommandSkillLevelApply {

    private FabricCommandSkillLevelApply() {}

    /**
     * Sets the skill to {@code newLevel} (absolute), zeroes XP, recalculates stats and rewards.
     * Mirrors the reference Bukkit order: revert commands when lowering, level-up when raising.
     */
    public static void setSkillLevel(
            FabricAuraSkills plugin, User user, ServerPlayerEntity player, Skill skill, int newLevel) {
        int oldLevel = user.getSkillLevel(skill);
        user.setSkillLevel(skill, newLevel);
        user.setSkillXp(skill, 0.0);
        plugin.getStatManager().recalculateStats(user);
        plugin.getRewardManager().updatePermissions(user);
        plugin.getRewardManager().applyRevertCommands(user, skill, oldLevel, newLevel);
        plugin.getRewardManager().applyLevelUpCommands(user, skill, oldLevel, newLevel);
        plugin.getModifierManager().applyModifiers(user, true);
    }

    @VisibleForTesting
    static int clampRemoveTarget(int startLevel, int currentLevel, int toRemove) {
        int t = currentLevel - toRemove;
        return Math.max(t, startLevel);
    }
}
