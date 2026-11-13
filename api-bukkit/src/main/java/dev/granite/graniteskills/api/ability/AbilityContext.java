package dev.granite.graniteskills.api.ability;

import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.api.GraniteSkillsBukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class AbilityContext {

    private final GraniteSkillsApi api;

    public AbilityContext(GraniteSkillsApi api) {
        this.api = api;
    }

    /**
     * Gets whether the ability or it's parent skill is disabled
     *
     * @param ability the ability to check
     * @return whether the ability is disabled
     */
    public boolean isDisabled(Ability ability) {
        return !ability.isEnabled() || !ability.getSkill().isEnabled();
    }

    /**
     * Performs multiple checks to see whether the player should be allowed to use the ability.
     * Checks for the ability being locked, the location being disabled, inadequate permissions, among
     * other checks.
     *
     * @param player the player to check
     * @param ability the ability to check for
     * @return true if one of the checks failed, false if all checks passed
     */
    public boolean failsChecks(Player player, Ability ability) {
        if (player == null) return true;
        if (!ability.isEnabled()) {
            return true;
        }
        if (api.getUser(player.getUniqueId()).getAbilityLevel(ability) <= 0) {
            return true;
        }
        if (GraniteSkillsBukkit.get().getLocationManager().isPluginDisabled(player.getLocation(), player)) {
            return true;
        }
        if (!api.getUser(player.getUniqueId()).hasSkillPermission(ability.getSkill())) {
            return true;
        }
        if (api.getMainConfig().isDisabledInCreative()) {
            return player.getGameMode().equals(GameMode.CREATIVE);
        }
        return false;
    }

}
