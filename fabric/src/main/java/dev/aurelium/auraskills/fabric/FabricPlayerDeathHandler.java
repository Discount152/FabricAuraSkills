package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Mirrors Bukkit {@code PlayerDeath} — {@code on_death.reset_skills}, {@code on_death.reset_xp},
 * and {@code on_death.reset_xp_ratio} from {@link Option} (see {@code config.yml}).
 * <p>
 * Geyser/Floodgate clients are still {@link ServerPlayerEntity} on the dedicated server; no
 * client-specific branch is required.
 */
public final class FabricPlayerDeathHandler {

    private FabricPlayerDeathHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }
            if (plugin == null || !plugin.isRuntimeReady()) {
                return;
            }
            if (!plugin.configBoolean(Option.ON_DEATH_RESET_SKILLS) && !plugin.configBoolean(Option.ON_DEATH_RESET_XP)) {
                return;
            }
            User user = ((FabricUserManager) plugin.getUserManager()).getUser(player);
            boolean resetSkills = plugin.configBoolean(Option.ON_DEATH_RESET_SKILLS);
            boolean resetXp = plugin.configBoolean(Option.ON_DEATH_RESET_XP);
            for (Skill skill : plugin.getSkillRegistry().getValues()) {
                if (resetSkills) {
                    user.resetSkill(skill);
                } else if (resetXp) {
                    double ratio = plugin.configDouble(Option.ON_DEATH_RESET_XP_RATIO);
                    if (ratio > 0.0 && ratio < 1.0) {
                        user.setSkillXp(skill, user.getSkillXp(skill) * ratio);
                    } else {
                        user.setSkillXp(skill, 0.0);
                    }
                }
            }
            plugin.getModifierManager().applyModifiers(user, true);
        });
    }
}
