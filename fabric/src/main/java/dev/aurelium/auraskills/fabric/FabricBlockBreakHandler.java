package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public final class FabricBlockBreakHandler {

    private FabricBlockBreakHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return;
            }
            SkillSource<BlockXpSource> match = findBreakSource(plugin, state);
            if (match == null) {
                return;
            }
            BlockXpSource xpSource = match.source();
            Skill skill = match.skill();
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, sp, skill, SourceTypes.BLOCK)) {
                return;
            }
            User user = plugin.getUserManager().getUser(sp.getUuid());
            double blocksMult = FabricBlockLevelerHelper.getBlocksBroken(sw, pos, xpSource, plugin);
            double mult = blocksMult * FabricBlockStateMatcher.getStateMultiplier(state, xpSource);
            plugin.getLevelManager().addXp(user, skill, xpSource, xpSource.getXp() * mult);
            FabricInteractGatheringLuck.tryApply(plugin, sp, user, skill, xpSource, sw, pos, state);
        });
    }

    private static SkillSource<BlockXpSource> findBreakSource(FabricAuraSkills plugin, BlockState state) {
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(BlockXpSource.BlockTriggers.BREAK)) {
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
}
