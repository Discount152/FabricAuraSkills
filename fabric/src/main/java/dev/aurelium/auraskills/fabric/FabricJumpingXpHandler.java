package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.JumpingXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Counts vanilla-style jump impulses each tick, matching Paper {@code JumpingLeveler} heuristics.
 */
public final class FabricJumpingXpHandler {

    private final FabricAuraSkills plugin;
    private final Set<UUID> prevOnGround = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> jumpsAccumulated = new ConcurrentHashMap<>();

    private FabricJumpingXpHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void register(FabricAuraSkills plugin) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.JUMPING)) {
            return;
        }
        SkillSource<JumpingXpSource> skillSource = plugin.getSkillManager().getSingleSourceOfType(JumpingXpSource.class);
        if (skillSource == null) {
            return;
        }
        FabricJumpingXpHandler handler = new FabricJumpingXpHandler(plugin);
        ServerTickEvents.END_SERVER_TICK.register(handler::onEndTick);
    }

    private void onEndTick(MinecraftServer server) {
        SkillSource<JumpingXpSource> skillSource = plugin.getSkillManager().getSingleSourceOfType(JumpingXpSource.class);
        if (skillSource == null) {
            return;
        }
        JumpingXpSource source = skillSource.source();
        Skill skill = skillSource.skill();
        if (!skill.isEnabled()) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickPlayer(player, source, skill);
        }
    }

    private void tickPlayer(ServerPlayerEntity player, JumpingXpSource source, Skill skill) {
        UUID id = player.getUuid();
        double vy = player.getVelocity().y;
        if (vy <= 0) {
            updateGroundState(player, id);
            return;
        }
        double jumpVelocity = baseJumpVelocity(player);
        boolean wasOnGround = prevOnGround.contains(id);
        if (isLadderAtFeet(player) || !wasOnGround) {
            updateGroundState(player, id);
            return;
        }
        if (player.isOnGround() || Double.compare(vy, jumpVelocity) != 0) {
            updateGroundState(player, id);
            return;
        }
        if (failsPlayerSkillCheck(plugin, player, skill)) {
            updateGroundState(player, id);
            return;
        }
        int next = jumpsAccumulated.merge(id, 1, Integer::sum);
        if (next >= source.getInterval()) {
            User user = plugin.getUserManager().getUser(id);
            plugin.getLevelManager().addXp(user, skill, source, source.getXp());
            jumpsAccumulated.remove(id);
        }
        updateGroundState(player, id);
    }

    private void updateGroundState(ServerPlayerEntity player, UUID id) {
        if (player.isOnGround()) {
            prevOnGround.add(id);
        } else {
            prevOnGround.remove(id);
        }
    }

    private static double baseJumpVelocity(ServerPlayerEntity player) {
        double v = 0.42;
        StatusEffectInstance jump = player.getStatusEffect(StatusEffects.JUMP_BOOST);
        if (jump != null) {
            v += (jump.getAmplifier() + 1) * 0.1;
        }
        return v;
    }

    private static boolean isLadderAtFeet(ServerPlayerEntity player) {
        BlockPos pos = BlockPos.ofFloored(player.getX(), player.getY(), player.getZ());
        return player.getEntityWorld().getBlockState(pos).isOf(Blocks.LADDER);
    }

    private static boolean failsPlayerSkillCheck(FabricAuraSkills plugin, ServerPlayerEntity player, Skill skill) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (!user.hasSkillPermission(skill)) {
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName);
    }
}
