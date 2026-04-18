package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.FishingXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.loot.FabricFishingLootHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class FabricFishingXpHandler {

    private FabricFishingXpHandler() {
    }

    /**
     * Called from a mixin when vanilla increments {@link net.minecraft.stat.Stats#FISH_CAUGHT} during bobber {@code use}.
     */
    public static void onFishCaughtStat(FabricAuraSkills plugin, FishingBobberEntity bobber, ServerPlayerEntity player) {
        if (plugin == null) {
            return;
        }
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.FISHING)) {
            return;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
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
        User user = plugin.getUserManager().getUser(player.getUuid());
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

    private static @Nullable ItemStack resolveCaughtStack(FishingBobberEntity bobber) {
        ItemStack fromHook = FabricFishingCatchHolder.take();
        if (fromHook != null && !fromHook.isEmpty()) {
            return fromHook;
        }
        Entity hooked = bobber.getHookedEntity();
        if (hooked instanceof ItemEntity itemEntity) {
            return itemEntity.getStack().copy();
        }
        return null;
    }
}
