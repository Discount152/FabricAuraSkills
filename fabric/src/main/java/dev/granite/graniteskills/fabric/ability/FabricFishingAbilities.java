package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class FabricFishingAbilities extends FabricAbilityImpl {

    public FabricFishingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LUCKY_CATCH, Abilities.FISHER, Abilities.TREASURE_HUNTER, Abilities.GRAPPLER, Abilities.EPIC_CATCH);
    }

    public static void onGrapple(FabricGraniteSkills plugin, ServerPlayer player, Entity hooked) {
        if (plugin == null || hooked == null) {
            return;
        }
        FabricFishingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricFishingAbilities.class);
        var ability = Abilities.GRAPPLER;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        Vec3 vector = player.position().subtract(hooked.position());
        double scale = 0.004 + (self.getValue(ability, user) / 25000.0);
        Vec3 result = vector.scale(scale);
        if (Math.abs(result.x) > 4 || Math.abs(result.y) > 4 || Math.abs(result.z) > 4) {
            return;
        }
        hooked.setDeltaMovement(result);
    }
}
