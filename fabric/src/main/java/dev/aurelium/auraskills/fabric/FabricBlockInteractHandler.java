package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

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
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            FabricCropPreUseCapture.record(
                    sp,
                    sw,
                    hitResult.getBlockPos(),
                    sw.getBlockState(hitResult.getBlockPos()),
                    "before_useblock_handler",
                    hand,
                    sp.getStackInHand(hand));
            onUseBlock(plugin, sp, sw, hand, hitResult);
            return ActionResult.PASS;
        });
    }

    private static void onUseBlock(FabricAuraSkills plugin, ServerPlayerEntity sp, ServerWorld sw, Hand hand,
            BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = FabricCropPreUseCapture.resolveBlockStateForUse(sp, sw, pos);
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

        ItemStack stack = sp.getStackInHand(hand);
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

        UUID playerId = sp.getUuid();
        Identifier blockIdBefore = Registries.BLOCK.getId(state.getBlock());
        BlockXpSource.BlockXpSourceState[] after = xpSource.getAfterStates();
        boolean delayedAfter = after != null && after.length > 0;

        Runnable grant = () -> {
            ServerPlayerEntity p = sw.getServer().getPlayerManager().getPlayer(playerId);
            if (p == null) {
                return;
            }
            User u = plugin.getUserManager().getUser(playerId);
            BlockState now = sw.getBlockState(pos);
            if (!Registries.BLOCK.getId(now.getBlock()).equals(blockIdBefore)) {
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
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
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
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();

        boolean collectWithShears = stack.isOf(Items.SHEARS)
                && (path.equals("beehive") || path.equals("bee_nest"));
        boolean collectWithBottles = stack.isOf(Items.GLASS_BOTTLE)
                && (path.equals("beehive") || path.equals("bee_nest")
                || path.equals("cauldron") || path.equals("water_cauldron"));
        boolean collectWithBuckets = stack.isOf(Items.BUCKET)
                && (path.equals("cauldron") || path.equals("water_cauldron")
                || path.equals("lava_cauldron") || path.equals("powder_snow_cauldron"));

        if (!collectWithShears && !collectWithBottles && !collectWithBuckets) {
            return false;
        }

        if (state.getBlock() instanceof BeehiveBlock) {
            int honey = state.get(BeehiveBlock.HONEY_LEVEL);
            return honey >= 5;
        }

        if (state.getBlock() instanceof LeveledCauldronBlock) {
            int level = state.get(LeveledCauldronBlock.LEVEL);
            int maxLevel = LeveledCauldronBlock.MAX_LEVEL;
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                if (level == 0) {
                    return false;
                }
                return true;
            }
            if (stack.isOf(Items.BUCKET)) {
                return level >= maxLevel;
            }
        }

        return false;
    }
}
