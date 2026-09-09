package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.JumpingXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Per–end-tick jump detection aligned with Bukkit {@code JumpingLeveler#handleJump}; Fabric uses epsilon matching for
 * jump velocity rather than bitwise {@link Double#compare} parity with vanilla impulses.
 */
public final class FabricJumpingXpHandler {

    private static final double JUMP_VY_EPSILON = 1.5e-4;

    private final FabricAuraSkills plugin;
    private final Set<UUID> prevOnGround = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> jumpsAccumulated = new ConcurrentHashMap<>();

    private FabricJumpingXpHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void register(FabricAuraSkills plugin) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.JUMPING)) {
            plugin.logger().info("[AuraSkills] Jumping XP handler not started — source.type jumping is disabled.");
            return;
        }
        List<SkillSource<JumpingXpSource>> jumps = plugin.getSkillManager().getSourcesOfType(JumpingXpSource.class);
        if (jumps.isEmpty()) {
            plugin.logger().info("[AuraSkills] Jumping XP handler not started — no JumpingXpSource entries loaded.");
            return;
        }
        SkillSource<JumpingXpSource> resolved = resolveAgilityJumpSource(plugin);
        if (resolved == null) {
            plugin.logger().info("[AuraSkills] Jumping XP handler not started — could not resolve any JumpingXpSource.");
            return;
        }
        String agilityTracked = jumps.stream()
                .filter(j -> FabricAgilitySkill.isAgility(j.skill()))
                .map(j -> j.source().getId().getSimpleName())
                .sorted()
                .collect(Collectors.joining(", "));
        plugin.logger().info("[AuraSkills] Agility jump handler registered");
        plugin.logger().info("[AuraSkills] Tracked agility jumping sources: "
                + (agilityTracked.isEmpty() ? "none resolved" : agilityTracked));
        plugin.logger().info("[AuraSkills] Jump XP award hook uses source=" + resolved.source().getId()
                + "; interval=" + resolved.source().getInterval()
                + " (total JumpingXpSource defs=" + jumps.size() + ").");
        FabricJumpingXpHandler handler = new FabricJumpingXpHandler(plugin);
        ServerTickEvents.END_SERVER_TICK.register(handler::onEndTick);
    }

    /** Prefer agility's {@code jump_per_100} when multiple {@link JumpingXpSource}s exist across skills (Bukkit parity). */
    static SkillSource<JumpingXpSource> resolveAgilityJumpSource(FabricAuraSkills plugin) {
        List<SkillSource<JumpingXpSource>> list = plugin.getSkillManager().getSourcesOfType(JumpingXpSource.class);
        if (list.isEmpty()) {
            return null;
        }
        SkillSource<JumpingXpSource> agilityAlternate = null;
        for (SkillSource<JumpingXpSource> s : list) {
            if (FabricAgilitySkill.isAgility(s.skill())) {
                agilityAlternate = s;
                if ("jump_per_100".equalsIgnoreCase(s.source().getId().getKey())) {
                    return s;
                }
            }
        }
        if (agilityAlternate != null) {
            return agilityAlternate;
        }
        return list.getFirst();
    }

    private void onEndTick(MinecraftServer server) {
        SkillSource<JumpingXpSource> skillSource = resolveAgilityJumpSource(plugin);
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
        if (player.isOnGround() || Math.abs(vy - jumpVelocity) > JUMP_VY_EPSILON) {
            updateGroundState(player, id);
            return;
        }
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.JUMPING)) {
            updateGroundState(player, id);
            return;
        }
        int beforeJumps = jumpsAccumulated.getOrDefault(id, 0);
        int next = beforeJumps + 1;
        int interval = Math.max(1, source.getInterval());
        boolean crossesInterval = next >= interval;
        User uForAward = crossesInterval ? FabricXpUserResolution.resolve(plugin, player) : null;
        boolean awardCalled = crossesInterval && uForAward != null;
        String reason =
                crossesInterval
                        ? (uForAward != null ? "threshold_met_jump_awarded" : "award_blocked_user_unresolved")
                        : "increment_no_award_yet";
        if (FabricXpDiagnostics.isEnabled()) {
            int remainderAfterJump = crossesInterval ? 0 : next;
            FabricXpDiagnostics.logAgilityJumpDelta(
                    plugin,
                    player,
                    source.getId().toString(),
                    beforeJumps,
                    next,
                    next - beforeJumps,
                    remainderAfterJump,
                    interval,
                    awardCalled,
                    reason);
            FabricXpDiagnostics.logAgilityJumpProgress(
                    plugin,
                    player,
                    source.getId().toString(),
                    next,
                    interval,
                    awardCalled,
                    reason);
        }
        if (awardCalled) {
            plugin.getLevelManager().addXp(uForAward, skill, source, source.getXp());
            jumpsAccumulated.remove(id);
        } else if (crossesInterval) {
            jumpsAccumulated.remove(id);
        } else {
            jumpsAccumulated.put(id, next);
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
