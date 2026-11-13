package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SourceType;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import net.minecraft.server.level.ServerPlayer;
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
    public static boolean shouldBlockXp(FabricGraniteSkills plugin, ServerPlayer player, Skill skill, SourceType sourceType) {
        if (!plugin.getSkillManager().isSourceEnabled(sourceType)) {
            return true;
        }
        if (!skill.isEnabled()) {
            return true;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (!user.hasSkillPermission(skill)) {
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
            return true;
        }
        return false;
    }

    public static @Nullable FabricGraniteSkills pluginOrNull() {
        return FabricGraniteSkills.getInstance();
    }
}
