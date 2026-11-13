package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric counterpart to Bukkit {@code AbilityContext} (no Slate / region manager hooks).
 */
public final class FabricAbilityContext {

    private final FabricGraniteSkills plugin;

    public FabricAbilityContext(FabricGraniteSkills plugin) {
        this.plugin = plugin;
    }

    public boolean isDisabled(Ability ability) {
        return !ability.isEnabled() || !ability.getSkill().isEnabled();
    }

    /**
     * @return true if the player must not benefit from the ability
     */
    public boolean failsChecks(ServerPlayer player, Ability ability) {
        if (player == null) {
            return true;
        }
        if (!ability.isEnabled()) {
            return true;
        }
        if (plugin.getUserManager().getUser(player.getUUID()).getAbilityLevel(ability) <= 0) {
            return true;
        }
        if (!plugin.getUserManager().getUser(player.getUUID()).hasSkillPermission(ability.getSkill())) {
            return true;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return true;
        }
        String world = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(world) || plugin.getWorldManager().isDisabledWorld(world)) {
            return true;
        }
        return false;
    }
}
