package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SourceType;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Shared gates for Fabric XP handlers (approximates Paper {@code LevelerContext} without Bukkit location API).
 */
public final class FabricXpHandlerUtil {

    private FabricXpHandlerUtil() {
    }

    /**
     * @return true if XP must not be awarded
     */
    public static boolean shouldBlockXp(FabricAuraSkills plugin, ServerPlayerEntity player, Skill skill, SourceType sourceType) {
        if (!plugin.getSkillManager().isSourceEnabled(sourceType)) {
            logBlocked(plugin, player, skill, sourceType, "source_type_disabled");
            return true;
        }
        if (!skill.isEnabled()) {
            logBlocked(plugin, player, skill, sourceType, "skill_disabled");
            return true;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            logBlocked(plugin, player, skill, sourceType, "creative_mode_disabled");
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user == null && plugin.getUserManager() instanceof FabricUserManager fum) {
            user = fum.getUser(player);
        }
        if (!Permissions.check(player, "auraskills.skill." + skill.name().toLowerCase(java.util.Locale.ROOT), true)) {
            logBlocked(plugin, player, skill, sourceType, "missing_skill_permission");
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
            logBlocked(plugin, player, skill, sourceType, "world_blocked_or_disabled");
            return true;
        }
        FabricXpDiagnostics.logDecision(
                plugin,
                sourceType.getId().toString(),
                player,
                skill,
                sourceType,
                null,
                0.0,
                false,
                "shared_gate_pass");
        return false;
    }

    private static void logBlocked(
            FabricAuraSkills plugin, ServerPlayerEntity player, Skill skill, SourceType sourceType, String reason) {
        FabricXpDiagnostics.logDecision(
                plugin,
                sourceType.getId().toString(),
                player,
                skill,
                sourceType,
                null,
                0.0,
                false,
                reason);
    }

    public static @Nullable FabricAuraSkills pluginOrNull() {
        return FabricAuraSkills.getInstance();
    }
}
