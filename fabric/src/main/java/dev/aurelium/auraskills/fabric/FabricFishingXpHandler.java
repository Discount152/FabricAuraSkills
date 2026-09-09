package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.item.LootItemFilter;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Bukkit: {@code FishingLootHandler} is {@link org.bukkit.event.EventPriority#HIGH}, {@code FishingLeveler} is
 * {@link org.bukkit.event.EventPriority#MONITOR} &mdash; bonus loot runs first, then base XP is resolved from the
 * (possibly updated) catch stack.
 *
 * <p>Sprint 136: Fishing XP is awarded from {@link FishingBobberEntity#use} RETURN via {@link #onBobberUseReturn} —
 * not from {@code PlayerEntity#increaseStat} (avoids fragile stat/mapping mixins on 1.21.11+).
 */
public final class FabricFishingXpHandler {

    private FabricFishingXpHandler() {
    }

    /**
     * Earliest server-side bobber {@code use} entry (Sprint 133 / 136 diagnostics). Uses only
     * {@link FabricFishingCatchHolder#peekCaughtStack()} so the catch is not consumed.
     */
    public static void onBobberUseHead(FishingBobberEntity bobber) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !FabricXpDiagnostics.isEnabled()) {
            return;
        }
        if (!(bobber.getPlayerOwner() instanceof ServerPlayerEntity sp)) {
            return;
        }
        ItemStack peek = FabricFishingCatchHolder.peekCaughtStack();
        String caughtStack = stackIdOrNull(peek);
        Entity hooked = bobber.getHookedEntity();
        String caughtEntity = hooked == null ? null : Registries.ENTITY_TYPE.getId(hooked.getType()).toString();
        FabricXpDiagnostics.logFishingEntryHead(
                plugin, sp, "FishingBobberEntity.use:HEAD", caughtStack, caughtEntity);
    }

    /**
     * Called from {@link dev.aurelium.auraskills.fabric.mixin.FishingBobberEntityMixin} at the end of
     * {@link FishingBobberEntity#use} (RETURN), after vanilla catch/loot handling. Resolves the catch (including
     * {@link FabricFishingCatchHolder#take()}), runs bonus loot, then awards Fishing XP when a matching source exists.
     */
    public static void onBobberUseReturn(FishingBobberEntity bobber) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        if (bobber.getEntityWorld().isClient()) {
            return;
        }
        if (!(bobber.getPlayerOwner() instanceof ServerPlayerEntity player)) {
            return;
        }
        Entity hooked = bobber.getHookedEntity();
        String caughtEntityStr =
                hooked == null ? null : Registries.ENTITY_TYPE.getId(hooked.getType()).toString();
        String hook = "FishingBobberEntity.use:RETURN";

        ItemStack preBonus = resolveCaughtStack(bobber);
        if (preBonus == null || preBonus.isEmpty()) {
            logFishingResult(
                    plugin,
                    player,
                    hook,
                    null,
                    caughtEntityStr,
                    "fishing:item:-",
                    false,
                    null,
                    false,
                    false,
                    "no_caught_stack",
                    false);
            return;
        }
        FabricFishingLootHandler.tryRollBonusLoot(plugin, player, bobber, preBonus, findFishingSource(plugin, preBonus));

        ItemStack postBonus = resolveCaughtStack(bobber);
        if (postBonus == null || postBonus.isEmpty()) {
            // If vanilla consumed/moved the item before RETURN snapshot, continue with pre-bonus catch.
            postBonus = preBonus.copy();
        }
        boolean lootModified = lootStacksVisiblyDiffer(preBonus, postBonus);
        String caughtStackId = stackId(postBonus);
        FishingResolve fr = resolveFishingSourceFull(plugin, postBonus);
        if (fr.source == null) {
            if (FabricXpDiagnostics.isEnabled()) {
                FabricXpDiagnostics.logSourceResolution(
                        plugin,
                        "fishing_resolution",
                        player,
                        null,
                        SourceTypes.FISHING,
                        false,
                        null,
                        0.0,
                        false,
                        "not_evaluated",
                        "caught_stack=" + caughtStackId + ";caught_entity="
                                + (caughtEntityStr == null ? "-" : caughtEntityStr),
                        "fishing:item:" + caughtStackId,
                        fishingCandidateCount(plugin),
                        fishingCandidateIdsCsv(plugin),
                        "no_matching_source");
            }
            logFishingResult(
                    plugin,
                    player,
                    hook,
                    caughtStackId.equals("-") ? null : caughtStackId,
                    caughtEntityStr,
                    "fishing:item:" + caughtStackId,
                    false,
                    null,
                    false,
                    false,
                    "no_matching_source",
                    lootModified);
            return;
        }
        SkillSource<FishingXpSource> forXp = fr.source;
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.FISHING)) {
            logFishingResult(
                    plugin,
                    player,
                    hook,
                    caughtStackId.equals("-") ? null : caughtStackId,
                    caughtEntityStr,
                    "fishing:item:" + caughtStackId,
                    true,
                    forXp.source().getId().toString(),
                    fr.tagFallbackUsed,
                    false,
                    "source_type_disabled",
                    lootModified);
            return;
        }
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, forXp.skill(), SourceTypes.FISHING)) {
            logFishingResult(
                    plugin,
                    player,
                    hook,
                    caughtStackId.equals("-") ? null : caughtStackId,
                    caughtEntityStr,
                    "fishing:item:" + caughtStackId,
                    true,
                    forXp.source().getId().toString(),
                    fr.tagFallbackUsed,
                    false,
                    "shared_xp_gate",
                    lootModified);
            return;
        }
        FishingXpSource source = forXp.source();
        User user = FabricXpUserResolution.resolve(plugin, player);
        if (user == null) {
            logFishingResult(
                    plugin,
                    player,
                    hook,
                    caughtStackId.equals("-") ? null : caughtStackId,
                    caughtEntityStr,
                    "fishing:item:" + caughtStackId,
                    true,
                    source.getId().toString(),
                    fr.tagFallbackUsed,
                    false,
                    "user_not_loaded",
                    lootModified);
            return;
        }
        plugin.getLevelManager().addXp(user, forXp.skill(), source, source.getXp());
        logFishingResult(
                plugin,
                player,
                hook,
                caughtStackId.equals("-") ? null : caughtStackId,
                caughtEntityStr,
                "fishing:item:" + caughtStackId,
                true,
                source.getId().toString(),
                fr.tagFallbackUsed,
                true,
                "awarded",
                lootModified);
    }

    private static void logFishingResult(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String hook,
            @Nullable String caughtStack,
            @Nullable String caughtEntity,
            @Nullable String sourceLookupKey,
            boolean sourceMatch,
            @Nullable String matchedSource,
            boolean fallbackUsed,
            boolean awardCalled,
            @Nullable String reason,
            boolean lootModified) {
        if (!FabricXpDiagnostics.isEnabled()) {
            return;
        }
        FabricXpDiagnostics.logFishingEntryResult(
                plugin,
                player,
                hook,
                caughtStack,
                caughtEntity,
                sourceLookupKey,
                sourceMatch,
                matchedSource,
                fallbackUsed,
                awardCalled,
                reason,
                lootModified);
    }

    public static @Nullable SkillSource<FishingXpSource> findFishingSource(FabricAuraSkills plugin, ItemStack stack) {
        return resolveFishingSourceFull(plugin, stack).source;
    }

    private static final class FishingResolve {
        final @Nullable SkillSource<FishingXpSource> source;
        final boolean tagFallbackUsed;

        FishingResolve(@Nullable SkillSource<FishingXpSource> source, boolean tagFallbackUsed) {
            this.source = source;
            this.tagFallbackUsed = tagFallbackUsed;
        }
    }

    private static FishingResolve resolveFishingSourceFull(FabricAuraSkills plugin, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new FishingResolve(null, false);
        }
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (FabricLootItemMatcher.passesLootFilter(plugin, stack, entry.source().getItem(), entry.skill())) {
                return new FishingResolve(entry, false);
            }
        }
        if (!plugin.configBoolean(Option.MOD_COMPAT_FISHING_TAG_FALLBACK_ENABLED)) {
            return new FishingResolve(null, false);
        }
        if (!stack.isIn(ItemTags.FISHES)) {
            return new FishingResolve(null, false);
        }
        String guess = guessVanillaFishMaterialPath(stack);
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            LootItemFilter filter = entry.source().getItem();
            if (filter == null) {
                continue;
            }
            String[] mats = filter.materials();
            if (mats == null) {
                continue;
            }
            for (String m : mats) {
                if (m != null && m.equalsIgnoreCase(guess)) {
                    return new FishingResolve(entry, true);
                }
            }
        }
        return new FishingResolve(null, false);
    }

    private static String stackId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "-";
        }
        return Registries.ITEM.getId(stack.getItem()).toString();
    }

    private static @Nullable String stackIdOrNull(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return Registries.ITEM.getId(stack.getItem()).toString();
    }

    static String guessVanillaFishMaterialPath(ItemStack stack) {
        String p = Registries.ITEM.getId(stack.getItem()).getPath();
        if (p.contains("salmon")) {
            return "salmon";
        }
        if (p.contains("tropical")) {
            return "tropical_fish";
        }
        if (p.contains("puffer")) {
            return "pufferfish";
        }
        return "cod";
    }

    private static boolean lootStacksVisiblyDiffer(ItemStack a, ItemStack b) {
        return !Registries.ITEM.getId(a.getItem()).equals(Registries.ITEM.getId(b.getItem()));
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

    private static String fishItemLookupKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "fishing:item:-";
        }
        return "fishing:item:" + Registries.ITEM.getId(stack.getItem());
    }

    private static int fishingCandidateCount(FabricAuraSkills plugin) {
        int n = 0;
        for (SkillSource<FishingXpSource> ignored : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            n++;
        }
        return n;
    }

    private static String fishingCandidateIdsCsv(FabricAuraSkills plugin) {
        Set<String> ids = new LinkedHashSet<>();
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            ids.add("[" + entry.skill().getId() + "]" + entry.source().getId());
        }
        return FabricXpLookupNormalize.joinTruncate(ids);
    }
}
