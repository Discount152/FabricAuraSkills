package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Block {@link BlockXpSource.BlockTriggers#INTERACT} and {@link BlockXpSource.BlockTriggers#COLLECT} XP,
 * aligned with Bukkit {@link dev.aurelium.auraskills.bukkit.source.BlockLeveler#onInteract}.
 * <p>
 * Awards run on the server thread shortly after {@link UseBlockCallback} so vanilla can update block state first.
 * Sources with {@code after_state} / {@code after_states} wait 50ms like Paper {@code scheduleAtLocation(..., 50, MS)}.
 */
public final class FabricBlockInteractHandler {

    private FabricBlockInteractHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return InteractionResult.PASS;
            }
            onUseBlock(plugin, sp, sw, hand, hitResult);
            return InteractionResult.PASS;
        });
    }

    private static void onUseBlock(FabricAuraSkills plugin, ServerPlayer sp, ServerLevel sw, InteractionHand hand,
            BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = sw.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        SkillSource<BlockXpSource> skillSource = findSource(plugin, state, BlockXpSource.BlockTriggers.INTERACT);
        boolean collecting = false;
        if (skillSource == null) {
            skillSource = findSource(plugin, state, BlockXpSource.BlockTriggers.COLLECT);
            collecting = true;
        }
        if (skillSource == null) {
            return;
        }

        ItemStack stack = sp.getItemInHand(hand);
        if (collecting && !passesCollectItemChecks(state, stack)) {
            return;
        }

        BlockXpSource xpSource = skillSource.source();
        Skill skill = skillSource.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, sp, skill, SourceTypes.BLOCK)) {
            return;
        }

        double interactMultiplier = FabricBlockLevelerHelper.getBlocksBroken(sw, pos, xpSource, plugin)
                * FabricBlockStateMatcher.getStateMultiplier(state, xpSource);

        UUID playerId = sp.getUUID();
        Identifier blockIdBefore = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        BlockXpSource.BlockXpSourceState[] after = xpSource.getAfterStates();
        boolean delayedAfter = after != null && after.length > 0;

        Runnable grant = () -> {
            ServerPlayer p = sw.getServer().getPlayerList().getPlayer(playerId);
            if (p == null) {
                return;
            }
            User u = plugin.getUserManager().getUser(playerId);
            BlockState now = sw.getBlockState(pos);
            if (!BuiltInRegistries.BLOCK.getKey(now.getBlock()).equals(blockIdBefore)) {
                return;
            }
            if (delayedAfter) {
                if (!FabricBlockStateMatcher.matchesStates(now, after)) {
                    return;
                }
            }
            plugin.getLevelManager().addXp(u, skill, xpSource, xpSource.getXp() * interactMultiplier);
            FabricInteractGatheringLuck.tryApply(plugin, p, u, skill, xpSource, sw, pos, now);
        };

        long delayMs = delayedAfter ? 50L : 0L;
        plugin.getScheduler().scheduleSync(grant, delayMs, TimeUnit.MILLISECONDS);
    }

    private static SkillSource<BlockXpSource> findSource(FabricAuraSkills plugin, BlockState state,
            BlockXpSource.BlockTriggers trigger) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(trigger)) {
                continue;
            }
            boolean blockOk = false;
            for (String blockName : source.getBlocks()) {
                if (blockName != null && blockName.equalsIgnoreCase(path)) {
                    blockOk = true;
                    break;
                }
            }
            if (!blockOk) {
                continue;
            }
            BlockXpSource.BlockXpSourceState[] states = source.getStates();
            if (states != null && states.length > 0) {
                if (!FabricBlockStateMatcher.matchesStates(state, states)) {
                    continue;
                }
            }
            return entry;
        }
        return null;
    }

    /**
     * Mirrors Paper {@code BlockLeveler} collect checks (shears / bottle / bucket on beehive, nest, cauldrons).
     */
    private static boolean passesCollectItemChecks(BlockState state, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();

        boolean collectWithShears = stack.is(Items.SHEARS)
                && (path.equals("beehive") || path.equals("bee_nest"));
        boolean collectWithBottles = stack.is(Items.GLASS_BOTTLE)
                && (path.equals("beehive") || path.equals("bee_nest")
                || path.equals("cauldron") || path.equals("water_cauldron"));
        boolean collectWithBuckets = stack.is(Items.BUCKET)
                && (path.equals("cauldron") || path.equals("water_cauldron")
                || path.equals("lava_cauldron") || path.equals("powder_snow_cauldron"));

        if (!collectWithShears && !collectWithBottles && !collectWithBuckets) {
            return false;
        }

        if (state.getBlock() instanceof BeehiveBlock) {
            int honey = state.getValue(BeehiveBlock.HONEY_LEVEL);
            return honey >= 5;
        }

        if (state.getBlock() instanceof LayeredCauldronBlock) {
            int level = state.getValue(LayeredCauldronBlock.LEVEL);
            int maxLevel = LayeredCauldronBlock.MAX_FILL_LEVEL;
            if (stack.is(Items.GLASS_BOTTLE)) {
                if (level == 0) {
                    return false;
                }
                return true;
            }
            if (stack.is(Items.BUCKET)) {
                return level >= maxLevel;
            }
        }

        return false;
    }
}
