package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class FabricBlockBreakHandler {

    private FabricBlockBreakHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return;
            }
            SkillSource<BlockXpSource> match = FabricBlockXpSourceLookup.findForBreak(plugin, state);
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
}
