package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SourceType;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
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
            return true;
        }
        if (!skill.isEnabled()) {
            return true;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (!user.hasSkillPermission(skill)) {
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
            return true;
        }
        return false;
    }

    public static @Nullable FabricAuraSkills pluginOrNull() {
        return FabricAuraSkills.getInstance();
    }
}
