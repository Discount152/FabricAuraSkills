package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.StatisticXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Polls vanilla statistics aligned with Minecraft **server ticks** (Bukkit used the same
 * {@link Option#SOURCE_STATISTIC_GAIN_PERIOD_TICKS} multiplier for wall-clock timers; Fabric now treats the value as
 * “every N {@link net.minecraft.server.MinecraftServer#getTicks}” so deltas line up with in-game statistic updates).
 */
public final class FabricStatisticXpHandler {

    private final FabricAuraSkills plugin;
    private final Map<UUID, Map<String, Integer>> tracker = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> remainderBySource = new ConcurrentHashMap<>();

    private FabricStatisticXpHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void start(FabricAuraSkills plugin) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.STATISTIC)) {
            plugin.logger().info("[AuraSkills] Statistic XP handler not started — source.type statistic is disabled.");
            return;
        }
        List<SkillSource<StatisticXpSource>> sources = plugin.getSkillManager().getSourcesOfType(StatisticXpSource.class);
        if (sources.isEmpty()) {
            plugin.logger().info("[AuraSkills] Statistic XP handler not started — no StatisticXpSource definitions loaded.");
            return;
        }
        for (SkillSource<StatisticXpSource> s : sources) {
            if (!FabricAgilitySkill.isAgility(s.skill())) {
                continue;
            }
            StatisticXpSource src = s.source();
            FabricStatisticSourceResolver.StatResolveResult d =
                    FabricStatisticSourceResolver.resolveDetailedForAgility(src.getStatistic(), src.getId().getKey());
            String raw = src.getStatistic() == null ? "-" : src.getStatistic();
            plugin.logger().info("[AuraSkills] Agility statistic source config: source=" + src.getId().getSimpleName()
                    + " raw_stat=" + raw
                    + " normalized=" + d.normalizedToken()
                    + " resolved=" + d.resolved()
                    + " reason=" + (d.resolved() ? "ok" : d.reason()));
        }
        int periodTicks = Math.max(1, plugin.configInt(Option.SOURCE_STATISTIC_GAIN_PERIOD_TICKS));
        FabricStatisticXpHandler handler = new FabricStatisticXpHandler(plugin);
        String agilityStats = sources.stream()
                .filter(s -> FabricAgilitySkill.isAgility(s.skill()))
                .filter(s -> FabricStatisticSourceResolver.resolveForAgility(
                        s.source().getStatistic(), s.source().getId().getKey()) != null)
                .map(s -> s.source().getId().getSimpleName())
                .sorted()
                .collect(Collectors.joining(", "));
        plugin.logger().info("[AuraSkills] Agility statistic polling registered");
        plugin.logger().info("[AuraSkills] Tracked agility statistic sources: "
                + (agilityStats.isEmpty() ? "none resolved" : agilityStats));
        plugin.logger().info("[AuraSkills] Fabric StatisticXpSource polling active (" + sources.size()
                + " sources; interval=" + periodTicks + " server ticks aligned to END_SERVER_TICK).");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!plugin.isRuntimeReady()) {
                return;
            }
            if (server.getTicks() % periodTicks != 0L) {
                return;
            }
            handler.tick();
        });
    }

    private void tick() {
        for (ServerPlayerEntity player : plugin.getServer().getPlayerManager().getPlayerList()) {
            if (player.isSpectator()) {
                continue;
            }
            try {
                handlePlayer(player);
            } catch (RuntimeException ex) {
                plugin.logger().warn("[AuraSkills] Statistic XP handler tick failed for player "
                        + player.getName().getString(), ex);
            }
        }
    }

    private void handlePlayer(ServerPlayerEntity player) {
        User user = FabricXpUserResolution.resolve(plugin, player);
        if (user == null) {
            return;
        }
        List<SkillSource<StatisticXpSource>> statisticSources =
                plugin.getSkillManager().getSourcesOfType(StatisticXpSource.class);
        for (SkillSource<StatisticXpSource> entry : statisticSources) {
            try {
                processStatisticSource(player, user, entry, statisticSources);
            } catch (RuntimeException ex) {
                StatisticXpSource src = entry.source();
                plugin.logger().warn("[AuraSkills] Statistic XP source failed (source=" + src.getId()
                        + " configured_stat=" + src.getStatistic() + "): " + ex.getMessage(), ex);
                if (FabricXpDiagnostics.isEnabled()) {
                    FabricXpDiagnostics.logStatisticResolution(
                            plugin,
                            player,
                            src.getId().getSimpleName(),
                            src.getStatistic(),
                            "-",
                            false,
                            "exception:" + ex.getClass().getSimpleName());
                }
            }
        }
    }

    private void processStatisticSource(
            ServerPlayerEntity player,
            User user,
            SkillSource<StatisticXpSource> entry,
            List<SkillSource<StatisticXpSource>> statisticSources) {
        Skill skill = entry.skill();
        StatisticXpSource source = entry.source();
        FabricStatisticSourceResolver.StatResolveResult detail = FabricAgilitySkill.isAgility(skill)
                ? FabricStatisticSourceResolver.resolveDetailedForAgility(source.getStatistic(), source.getId().getKey())
                : FabricStatisticSourceResolver.resolveDetailed(source.getStatistic());
        @Nullable Stat<?> stat = detail.stat();
        String norm = detail.normalizedToken();
        String statIdLog = stat != null ? "minecraft:" + norm : ("-".equals(norm) ? "-" : "minecraft:" + norm);

        String sourceKey = source.getId() + "|" + source.getStatistic();
        String sourceIdShort = source.getId().getSimpleName();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.STATISTIC)) {
            return;
        }
        int storedRemainderBase = remainderBySource.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>())
                .getOrDefault(sourceKey, 0);

        if (stat == null) {
            if (FabricXpDiagnostics.isEnabled()) {
                FabricXpDiagnostics.logStatisticResolution(
                        plugin,
                        player,
                        sourceIdShort,
                        source.getStatistic(),
                        norm,
                        false,
                        detail.reason());
                FabricXpDiagnostics.logAgilityStatDelta(
                        plugin,
                        player,
                        sourceIdShort,
                        statIdLog,
                        0,
                        0,
                        0,
                        storedRemainderBase,
                        source.getMinimumIncrease(),
                        0,
                        false,
                        false,
                        detail.reason());
            }
            return;
        }
        int current = player.getStatHandler().getStat(stat);
        int lastValue = tracker.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>())
                .computeIfAbsent(sourceKey, s -> current);
        int change = current - lastValue;
        int storedRemainder = remainderBySource.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>())
                .getOrDefault(sourceKey, 0);
        StatProgress progress = computeProgress(
                change,
                storedRemainder,
                source.getMinimumIncrease(),
                source.getMultiplier(),
                source.getXp());
        boolean sourceResolved = true;
        if (change <= 0) {
            if (FabricXpDiagnostics.isEnabled()) {
                FabricXpDiagnostics.logAgilityStatDelta(
                        plugin,
                        player,
                        sourceIdShort,
                        statIdLog,
                        lastValue,
                        current,
                        change,
                        storedRemainder,
                        source.getMinimumIncrease(),
                        diagConvertedUnits(skill, progress.totalDelta()),
                        sourceResolved,
                        false,
                        "no_positive_delta");
            }
            return;
        }

        tracker.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>()).put(sourceKey, current);
        if (!progress.thresholdMet()) {
            remainderBySource.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>())
                    .put(sourceKey, progress.totalDelta());
            if (FabricXpDiagnostics.isEnabled()) {
                FabricXpDiagnostics.logAgilityStatDelta(
                        plugin,
                        player,
                        sourceIdShort,
                        statIdLog,
                        lastValue,
                        current,
                        change,
                        storedRemainder,
                        source.getMinimumIncrease(),
                        diagConvertedUnits(skill, progress.totalDelta()),
                        sourceResolved,
                        false,
                        "threshold_not_met");
            }
            return;
        }
        if (FabricXpDiagnostics.isEnabled()) {
            FabricXpDiagnostics.logSourceResolution(
                    plugin,
                    "statistic_resolution",
                    player,
                    skill,
                    SourceTypes.STATISTIC,
                    true,
                    source.getId().toString(),
                    progress.xpToAdd(),
                    true,
                    "shared_gate_pass",
                    "configured_stat=" + source.getStatistic(),
                    "statistic:" + FabricStatisticSourceResolver.normalizeStatisticToken(source.getStatistic()),
                    statisticSources.size(),
                    statisticSources.stream()
                            .map(s -> "[" + s.skill().getId() + "]" + s.source().getId())
                            .collect(Collectors.joining("|")),
                    "awarded");
            if (FabricAgilitySkill.isAgility(skill)) {
                FabricXpDiagnostics.logXpSourceCompare(
                        plugin,
                        "agility",
                        "fall_damage",
                        source.getId().toString(),
                        "stat_mapping");
            }
        }
        plugin.getLevelManager().addXp(user, entry.skill(), source, progress.xpToAdd());
        if (FabricXpDiagnostics.isEnabled()) {
            FabricXpDiagnostics.logAgilityStatDelta(
                    plugin,
                    player,
                    sourceIdShort,
                    statIdLog,
                    lastValue,
                    current,
                    change,
                    storedRemainder,
                    source.getMinimumIncrease(),
                    diagConvertedUnits(skill, progress.totalDelta()),
                    sourceResolved,
                    progress.xpToAdd() != 0.0,
                    "threshold_met_awarded");
        }
        remainderBySource.computeIfAbsent(player.getUuid(), u -> new ConcurrentHashMap<>()).put(sourceKey, 0);
    }

    /** Agility movement stats are tracked in centimeters; diagnostics expose whole-meter equivalents. */
    private static int diagConvertedUnits(Skill skill, int totalDeltaStatUnits) {
        return FabricAgilitySkill.isAgility(skill) ? totalDeltaStatUnits / 100 : totalDeltaStatUnits;
    }

    static StatProgress computeProgress(
            int change,
            int storedRemainder,
            int threshold,
            double sourceMultiplier,
            double sourceXp) {
        int totalDelta = storedRemainder + Math.max(0, change);
        boolean thresholdMet = threshold <= 0 || totalDelta >= threshold;
        double xpToAdd = thresholdMet && totalDelta > 0 ? totalDelta * sourceMultiplier * sourceXp : 0.0;
        return new StatProgress(totalDelta, Math.max(threshold, 0), thresholdMet, xpToAdd);
    }

    record StatProgress(int totalDelta, int threshold, boolean thresholdMet, double xpToAdd) {}

}
