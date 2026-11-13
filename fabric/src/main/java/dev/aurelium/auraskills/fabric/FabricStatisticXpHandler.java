package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.StatisticXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Polls vanilla statistics on a timer (Paper {@code StatisticLeveler}). Supports {@link Stats#CUSTOM} ids
 * (e.g. {@code walk_one_cm}) and typed stats {@code mined/&lt;block&gt;}, {@code used/&lt;item&gt;},
 * {@code broken/&lt;item&gt;}, {@code picked_up/&lt;item&gt;} using registry paths or full ids.
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
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            handlePlayer(player);
        }
    }

    private void handlePlayer(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        for (SkillSource<StatisticXpSource> entry : plugin.getSkillManager().getSourcesOfType(StatisticXpSource.class)) {
            Skill skill = entry.skill();
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.STATISTIC)) {
                continue;
            }
            StatisticXpSource source = entry.source();
            Stat<?> stat = resolveStat(source.getStatistic());
            if (stat == null) {
                continue;
            }
            int current = player.getStats().getValue(stat);
            int lastValue = tracker.computeIfAbsent(player.getUUID(), u -> new ConcurrentHashMap<>())
                    .computeIfAbsent(source, s -> current);
            int change = current - lastValue;
            if (change <= 0 || change < source.getMinimumIncrease()) {
                continue;
            }
            double xpToAdd = change * source.getMultiplier() * source.getXp();
            plugin.getLevelManager().addXp(user, entry.skill(), source, xpToAdd);
            tracker.computeIfAbsent(player.getUUID(), u -> new ConcurrentHashMap<>()).put(source, current);
        }
    }

    private static Stat<?> resolveStat(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String raw = name.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        int slash = lower.indexOf('/');
        if (slash > 0) {
            String kind = lower.substring(0, slash);
            String rest = raw.substring(slash + 1).trim();
            Identifier rid = Identifier.tryParse(rest);
            if (rid == null) {
                rid = Identifier.withDefaultNamespace(rest.toLowerCase(Locale.ROOT));
            }
            return switch (kind) {
                case "mined" -> minedStat(rid);
                case "used" -> usedStat(rid);
                case "broken" -> brokenStat(rid);
                case "picked_up", "pickedup" -> pickedUpStat(rid);
                case "custom" -> customStat(rest.toLowerCase(Locale.ROOT).replace('-', '_'));
                default -> null;
            };
        }
        return customStat(lower);
    }

    private static Stat<?> customStat(String vanillaPath) {
        Identifier id = Identifier.withDefaultNamespace(vanillaPath.toLowerCase(Locale.ROOT));
        if (!Stats.CUSTOM.contains(id)) {
            return null;
        }
        return Stats.CUSTOM.get(id);
    }

    private static Stat<?> minedStat(Identifier blockId) {
        Block block = BuiltInRegistries.BLOCK.getValue(blockId);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return Stats.BLOCK_MINED.get(block);
    }

    private static Stat<?> usedStat(Identifier itemId) {
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.ITEM_USED.get(item);
    }

    private static Stat<?> brokenStat(Identifier itemId) {
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.ITEM_BROKEN.get(item);
    }

    private static Stat<?> pickedUpStat(Identifier itemId) {
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.ITEM_PICKED_UP.get(item);
    }

}
