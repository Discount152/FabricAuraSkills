package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.JumpingXpSource;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Counts jumps from the vanilla jump statistic and from leaving the ground while moving up.
 * 26.3 client jumps often never call {@code jumpFromGround}, so the statistic alone stays at 0.
 */
public final class FabricJumpingXpHandler {

    private final FabricGraniteSkills plugin;
    private final Map<UUID, Integer> lastJumpStat = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> jumpsAccumulated = new ConcurrentHashMap<>();
    private final Map<UUID, Double> lastY = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> lastOnGround = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> pendingStatCredit = new ConcurrentHashMap<>();

    private FabricJumpingXpHandler(FabricGraniteSkills plugin) {
        this.plugin = plugin;
    }

    public static void register(FabricGraniteSkills plugin) {
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
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player, source, skill);
        }
    }

    private void tickPlayer(ServerPlayer player, JumpingXpSource source, Skill skill) {
        UUID id = player.getUUID();
        int stat = player.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP));
        int statJumps = FabricJumpAward.newJumps(lastJumpStat.put(id, stat), stat);
        FabricJumpAward.JumpCount counted = FabricJumpAward.combine(
                statJumps, leftGroundUpward(player, id), pendingStatCredit.getOrDefault(id, 0));
        if (counted.pendingStatCredit() == 0) {
            pendingStatCredit.remove(id);
        } else {
            pendingStatCredit.put(id, counted.pendingStatCredit());
        }
        int jumps = counted.jumps();
        if (jumps == 0 || failsPlayerSkillCheck(plugin, player, skill)) {
            return;
        }
        for (int i = 0; i < jumps; i++) {
            int next = jumpsAccumulated.merge(id, 1, Integer::sum);
            double xp = FabricJumpAward.xpForJump(next, source.getInterval(), source.getXp());
            if (xp > 0.0) {
                User user = plugin.getUserManager().getUser(id);
                if (user != null) {
                    plugin.getLevelManager().addXp(user, skill, source, xp);
                }
                jumpsAccumulated.remove(id);
            }
        }
    }

    /**
     * 26.3 often moves a jumping client without calling {@code jumpFromGround}, so the vanilla jump
     * statistic stays at 0. Leaving the ground while moving up is the jump the statistic missed.
     */
    private boolean leftGroundUpward(ServerPlayer player, UUID id) {
        double y = player.getY();
        boolean onGround = player.onGround();
        Double previousY = lastY.put(id, y);
        Boolean wasOnGround = lastOnGround.put(id, onGround);
        if (previousY == null || wasOnGround == null) {
            return false;
        }
        if (player.isFallFlying()
                || player.isInWater()
                || player.isPassenger()
                || player.getAbilities().flying) {
            return false;
        }
        return FabricJumpAward.leftGroundUpward(wasOnGround, onGround, y - previousY);
    }

    private static boolean failsPlayerSkillCheck(FabricGraniteSkills plugin, ServerPlayer player, Skill skill) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return true;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null || !user.hasSkillPermission(skill)) {
            return true;
        }
        String worldName = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName);
    }
}
