package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.JumpingXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
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
 * Per–end-tick jump detection aligned with Bukkit {@code JumpingLeveler#handleJump}:
 * <ul>
 *   <li>Upward velocity ({@code vy &gt; 0}) and initial jump speed {0.42} + Jump Boost (amplifier × 0.1)
 *   <li>Previous tick “on ground” tracking (Bukkit {@code prevPlayersOnGround})
 *   <li>Block at feet is ladder → no count (Bukkit {@code Material.LADDER})
 *   <li>Not on ground and {@code vy} exactly equals expected jump impulse (vanilla jump, not knockback)
 *   <li>Interval counter (Bukkit {@code skillsJumps} metadata) before {@link JumpingXpSource#getInterval} awards
 * </ul>
 * Uses {@link ServerTickEvents#END_SERVER_TICK} instead of Bukkit {@code PlayerMoveEvent}; core velocity rules match.
 * <p>
 * <strong>Fabric hardening (Sprint 13):</strong> {@link FabricXpHandlerUtil#shouldBlockXp} (same gates as other
 * Fabric levelers where applicable), skip {@linkplain ServerPlayerEntity#isSpectator() spectators}, skip
 * {@linkplain net.minecraft.entity.LivingEntity#isClimbing() climbing} (reduces false positives not covered by the
 * ladder-at-feet check). Bukkit location-based XP blocks (WorldGuard, etc.) are not on Fabric by default.
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
        if (player.isSpectator()) {
            updateGroundState(player, id);
            return;
        }
        if (player.isClimbing()) {
            updateGroundState(player, id);
            return;
        }
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
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.JUMPING)) {
            updateGroundState(player, id);
            return;
        }
        int next = jumpsAccumulated.merge(id, 1, Integer::sum);
        if (next >= source.getInterval()) {
            plugin.getLevelManager().addXp(plugin.getUserManager().getUser(id), skill, source, source.getXp());
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

}
