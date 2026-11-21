package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class FabricPlayerDeathReset {

    private FabricPlayerDeathReset() {
    }

    public static void register(FabricGraniteSkills plugin) {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }
            if (!plugin.configBoolean(Option.ON_DEATH_RESET_SKILLS) && !plugin.configBoolean(Option.ON_DEATH_RESET_XP)) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return;
            }
            for (Skill skill : plugin.getSkillRegistry().getValues()) {
                if (plugin.configBoolean(Option.ON_DEATH_RESET_SKILLS)) {
                    user.resetSkill(skill);
                } else {
                    double ratio = plugin.configDouble(Option.ON_DEATH_RESET_XP_RATIO);
                    if (ratio > 0 && ratio < 1) {
                        user.setSkillXp(skill, user.getSkillXp(skill) * ratio);
                    } else {
                        user.setSkillXp(skill, 0);
                    }
                }
            }
        });
    }
}
