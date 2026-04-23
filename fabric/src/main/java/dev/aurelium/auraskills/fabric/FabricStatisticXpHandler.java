package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.StatisticXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Polls vanilla statistics on a timer (Bukkit {@code StatisticLeveler} — one {@link Stat} per
 * {@link StatisticXpSource#getStatistic} string; Fabric adds typed families Bukkit’s enum name alone cannot express).
 * Statistic string syntax: see {@link FabricStatisticSourceResolver}.
 * Overlap with other XP paths (e.g. entity-kill levelers vs {@code killed/…} stats) is a <strong>configuration</strong>
 * concern, not double-counted in one handler.
 */
public final class FabricStatisticXpHandler {

    private final FabricAuraSkills plugin;
    private final Map<UUID, Map<StatisticXpSource, Integer>> tracker = new ConcurrentHashMap<>();

    private FabricStatisticXpHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void start(FabricAuraSkills plugin) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.STATISTIC)) {
            return;
        }
        List<SkillSource<StatisticXpSource>> sources = plugin.getSkillManager().getSourcesOfType(StatisticXpSource.class);
        if (sources.isEmpty()) {
            return;
        }
        FabricStatisticXpHandler handler = new FabricStatisticXpHandler(plugin);
        int periodTicks = Math.max(1, plugin.configInt(Option.SOURCE_STATISTIC_GAIN_PERIOD_TICKS));
        TaskRunnable task = new TaskRunnable() {
            @Override
            public void run() {
                handler.tick();
            }
        };
        plugin.getScheduler().timerSync(task, 0, periodTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        for (ServerPlayerEntity player : plugin.getServer().getPlayerManager().getPlayerList()) {
            if (player.isSpectator()) {
                continue;
            }
            handlePlayer(player);
        }
    }

    private void handlePlayer(ServerPlayerEntity player) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        for (SkillSource<StatisticXpSource> entry : plugin.getSkillManager().getSourcesOfType(StatisticXpSource.class)) {
            Skill skill = entry.skill();
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.STATISTIC)) {
                continue;
            }
            StatisticXpSource source = entry.source();
            @Nullable Stat<?> stat = FabricStatisticSourceResolver.resolve(source.getStatistic());
            if (stat == null) {
                continue;
            }
            int current = player.getStatHandler().getStat(stat);
            int lastValue = tracker.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>())
                    .computeIfAbsent(source, s -> current);
            int change = current - lastValue;
            if (change <= 0 || change < source.getMinimumIncrease()) {
                continue;
            }
            double xpToAdd = change * source.getMultiplier() * source.getXp();
            plugin.getLevelManager().addXp(user, entry.skill(), source, xpToAdd);
            tracker.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>()).put(source, current);
        }
    }

}
