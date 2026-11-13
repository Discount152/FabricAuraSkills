package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric counterpart to Bukkit {@code AbilityContext} (no Slate / region manager hooks).
 */
public final class FabricAbilityContext {

    private final FabricAuraSkills plugin;

    public FabricAbilityContext(FabricAuraSkills plugin) {
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
