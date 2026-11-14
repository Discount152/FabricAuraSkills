package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Bukkit {@code SkillCommand} side effects: single-skill set (**Sprint 42**), **setall** batch
 * (**Sprint 43**). Callers validate {@code newLevel} &gt;=
 * {@code plugin.config().getStartLevel()} where the reference does.
 */
public final class FabricCommandSkillLevelApply {

    private FabricCommandSkillLevelApply() {}

    /**
     * Sets the skill to {@code newLevel} (absolute), zeroes XP, recalculates stats and rewards.
     * Mirrors the reference Bukkit {@code onSkillSetlevel} order: revert/level-up when
     * changing level, then global updates.
     */
    public static void setSkillLevel(
            FabricGraniteSkills plugin, User user, ServerPlayer player, Skill skill, int newLevel) {
        int oldLevel = user.getSkillLevel(skill);
        user.setSkillLevel(skill, newLevel);
        user.setSkillXp(skill, 0.0);
        plugin.getStatManager().recalculateStats(user);
        plugin.getRewardManager().updatePermissions(user);
        plugin.getRewardManager().applyRevertCommands(user, skill, oldLevel, newLevel);
        plugin.getRewardManager().applyLevelUpCommands(user, skill, oldLevel, newLevel);
        plugin.getModifierManager().applyModifiers(user, true);
    }

    /**
     * Bukkit {@code onSkillSetall}: for each <strong>enabled</strong> skill, set level + clear XP
     * + reward revert/level-up; <strong>then</strong> one {@code recalculateStats}, {@code
     * applyModifiers}, {@code updatePermissions} (reference order; do not call {@link
     * #setSkillLevel} in a loop).
     */
    public static void setAllEnabledSkillsToLevel(FabricGraniteSkills plugin, User user, int level) {
        for (Skill skill : plugin.getSkillRegistry().getValues()) {
            if (skill.isEnabled()) {
                int oldLevel = user.getSkillLevel(skill);
                user.setSkillLevel(skill, level);
                user.setSkillXp(skill, 0.0);
                plugin.getRewardManager().applyRevertCommands(user, skill, oldLevel, level);
                plugin.getRewardManager().applyLevelUpCommands(user, skill, oldLevel, level);
            }
        }
        plugin.getStatManager().recalculateStats(user);
        plugin.getModifierManager().applyModifiers(user, true);
        plugin.getRewardManager().updatePermissions(user);
    }

    /** Bukkit: after {@link User#resetSkill(Skill)}, re-apply modifiers. */
    public static void afterSkillResetApplyModifiers(FabricGraniteSkills plugin, User user) {
        plugin.getModifierManager().applyModifiers(user, true);
    }

    @VisibleForTesting
    static int clampRemoveTarget(int startLevel, int currentLevel, int toRemove) {
        int t = currentLevel - toRemove;
        return Math.max(t, startLevel);
    }
}
