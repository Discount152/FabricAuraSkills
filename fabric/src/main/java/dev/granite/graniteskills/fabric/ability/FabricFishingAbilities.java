package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricInteractGatheringLuck;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class FabricFishingAbilities extends FabricAbilityImpl {

    public FabricFishingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LUCKY_CATCH, Abilities.FISHER, Abilities.TREASURE_HUNTER, Abilities.GRAPPLER, Abilities.EPIC_CATCH);
    }

    /**
     * Bukkit {@code FishingAbilities#luckyCatch}: increase the caught item stack by fishing-luck
     * extra drops (capped at max stack size).
     *
     * @return number of items added to the stack
     */
    public static int applyLuckyCatch(FabricGraniteSkills plugin, ServerPlayer player, ItemStack caught) {
        if (plugin == null || player == null || caught == null || caught.isEmpty()) {
            return 0;
        }
        if (player.getAbilities().instabuild) {
            return 0;
        }
        FabricFishingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricFishingAbilities.class);
        var ability = Abilities.LUCKY_CATCH;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return 0;
        }
        if (!Traits.FISHING_LUCK.isEnabled()) {
            return 0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        int extraDrops = FabricInteractGatheringLuck.rollExtraDrops(user, Traits.FISHING_LUCK);
        if (extraDrops <= 0) {
            return 0;
        }
        int before = caught.getCount();
        int after = Math.min(before + extraDrops, caught.getMaxStackSize());
        caught.setCount(after);
        return after - before;
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
