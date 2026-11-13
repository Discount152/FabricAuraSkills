package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.FishingXpSource;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.loot.FabricFishingLootHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class FabricFishingXpHandler {

    private FabricFishingXpHandler() {
    }

    /**
     * Called from a mixin when vanilla increments {@link net.minecraft.stats.Stats#FISH_CAUGHT} during bobber {@code use}.
     */
    public static void onFishCaughtStat(FabricGraniteSkills plugin, FishingHook bobber, ServerPlayer player) {
        if (plugin == null) {
            return;
        }
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.FISHING)) {
            return;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return;
        }
        ItemStack caught = resolveCaughtStack(bobber);
        if (caught == null || caught.isEmpty()) {
            return;
        }
        SkillSource<FishingXpSource> match = null;
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (FabricLootItemMatcher.passesLootFilter(plugin, caught, entry.source().getItem(), entry.skill())) {
                match = entry;
                break;
            }
        }
        if (match == null) {
            return;
        }
        Skill skill = match.skill();
        if (!skill.isEnabled()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (!user.hasSkillPermission(skill)) {
            return;
        }
        String worldName = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
            return;
        }
        FishingXpSource source = match.source();
        plugin.getLevelManager().addXp(user, skill, source, source.getXp());
        FabricFishingLootHandler.tryRollBonusLoot(plugin, player, bobber, caught, match);
    }

    private static @Nullable ItemStack resolveCaughtStack(FishingHook bobber) {
        ItemStack fromHook = FabricFishingCatchHolder.take();
        if (fromHook != null && !fromHook.isEmpty()) {
            return fromHook;
        }
        Entity hooked = bobber.getHookedIn();
        if (hooked instanceof ItemEntity itemEntity) {
            return itemEntity.getItem().copy();
        }
        return null;
    }
}
