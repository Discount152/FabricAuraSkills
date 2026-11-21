package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.BlockXpSource;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.region.FabricPlacedBlocks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class FabricBlockBreakHandler {

    private FabricBlockBreakHandler() {
    }

    public static void register(FabricGraniteSkills plugin) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return;
            }
            boolean placed = FabricPlacedBlocks.isPlaced(sw, pos);
            dev.granite.graniteskills.fabric.region.FabricPlacedBlocks.clear(sw, pos);
            dev.granite.graniteskills.fabric.trait.FabricDoubleDrop.tryDuplicate(plugin, sp, sw, pos, state);
            SkillSource<BlockXpSource> match = findBreakSource(plugin, state);
            if (match == null) {
                return;
            }
            BlockXpSource xpSource = match.source();
            Skill skill = match.skill();
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, sp, skill, SourceTypes.BLOCK)) {
                return;
            }
            if (!FabricXpAwardGuard.claim(sp.getUUID(), xpSource.getId().toString(), pos,
                    sw.dimension().identifier().toString(), "break")) {
                return;
            }
            User user = plugin.getUserManager().getUser(sp.getUUID());
            if (user == null) {
                return;
            }
            if (xpSource.checkReplace()
                    && plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)
                    && placed) {
                return;
            }
            double blocksMult = FabricBlockLevelerHelper.getBlocksBroken(sw, pos, state, xpSource, plugin);
            double xp = FabricBlockStateMatcher.awardedBreakXp(state, xpSource) * blocksMult;
            plugin.getLevelManager().addXp(user, skill, xpSource, xp);
            FabricExternalMultiBreak.onAwardedBreak(plugin, sp, sw, pos, state);
            FabricInteractGatheringLuck.tryApply(plugin, sp, user, skill, xpSource, sw, pos, state);
            dev.granite.graniteskills.fabric.loot.FabricSkillLoot.tryRoll(plugin, sp, sw, pos, user, skill, xpSource);
        });
    }

    static SkillSource<BlockXpSource> findBreakSource(FabricGraniteSkills plugin, BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
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
