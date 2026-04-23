package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.FishingXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.loot.FabricFishingLootHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Bukkit: {@code FishingLootHandler} is {@link org.bukkit.event.EventPriority#HIGH}, {@code FishingLeveler} is
 * {@link org.bukkit.event.EventPriority#MONITOR} &mdash; bonus loot runs first, then base XP is resolved from the
 * (possibly updated) catch stack.
 */
public final class FabricFishingXpHandler {

    private FabricFishingXpHandler() {
    }

    /**
     * Called from a mixin when vanilla increments {@link net.minecraft.stat.Stats#FISH_CAUGHT} during bobber
     * {@code use}.
     */
    public static void onFishCaughtStat(FabricAuraSkills plugin, FishingBobberEntity bobber, ServerPlayerEntity player) {
        if (plugin == null) {
            return;
        }
        ItemStack preBonus = resolveCaughtStack(bobber);
        if (preBonus == null || preBonus.isEmpty()) {
            return;
        }
        SkillSource<FishingXpSource> preMatch = findFishingSource(plugin, preBonus);
        FabricFishingLootHandler.tryRollBonusLoot(plugin, player, bobber, preBonus, preMatch);

        ItemStack postBonus = resolveCaughtStack(bobber);
        if (postBonus == null || postBonus.isEmpty()) {
            return;
        }
        SkillSource<FishingXpSource> forXp = findFishingSource(plugin, postBonus);
        if (forXp == null) {
            return;
        }
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.FISHING)) {
            return;
        }
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, forXp.skill(), SourceTypes.FISHING)) {
            return;
        }
        FishingXpSource source = forXp.source();
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, forXp.skill(), source, source.getXp());
    }

    public static @Nullable SkillSource<FishingXpSource> findFishingSource(FabricAuraSkills plugin, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (FabricLootItemMatcher.passesLootFilter(plugin, stack, entry.source().getItem(), entry.skill())) {
                return entry;
            }
        }
        return null;
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
