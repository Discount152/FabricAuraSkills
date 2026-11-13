package dev.granite.graniteskills.common.stat;

import dev.granite.graniteskills.api.stat.ReloadableIdentifier;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Interface with methods to manage player stats.
 */
public abstract class StatManager {

    protected final GraniteSkillsPlugin plugin;
    private final Map<Stat, LoadedStat> statMap;
    private final StatSupplier supplier;

    public StatManager(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
        this.statMap = new LinkedHashMap<>();
        this.supplier = new StatSupplier(this, plugin.getMessageProvider());
    }

    public StatSupplier getSupplier() {
        return supplier;
    }

    @NotNull
    public LoadedStat getStat(Stat stat) {
        LoadedStat loadedStat = statMap.get(stat);
        if (loadedStat == null) {
            throw new IllegalArgumentException("Stat " + stat + " is not loaded!");
        }
        return loadedStat;
    }

    public Collection<LoadedStat> getStats() {
        return statMap.values();
    }

    public Set<Stat> getEnabledStats() {
        Set<Stat> stats = new LinkedHashSet<>();
        for (LoadedStat loaded : statMap.values()) {
            if (!loaded.options().enabled()) continue;
            stats.add(loaded.stat());
        }
        return stats;
    }

    public void register(Stat stat, LoadedStat loadedStat) {
        statMap.put(stat, loadedStat);
    }

    public void unregisterAll() {
        statMap.clear();
    }

    public abstract <T extends ReloadableIdentifier> void reload(User user, T type);

    public abstract void reloadAllTraits(User user);

    public void recalculateStats(User user) {
        recalculateStats(user, true);
    }

    public void recalculateStats(User user, boolean reload) {
        if (user == null) return;

        for (Stat stat : plugin.getStatRegistry().getValues()) {
            user.getUserStats().recalculateStat(stat);
        }
        if (reload) {
            reloadAllTraits(user);
        }
    }

    public boolean isLoaded(Stat stat) {
        return statMap.containsKey(stat);
    }

    public void scheduleTemporaryModifierTask() {
        if (!plugin.configBoolean(Option.MODIFIER_TEMPORARY_ENABLED)) {
            return;
        }

        var task = new TaskRunnable() {
            @Override
            public void run() {
                for (User user : plugin.getUserManager().getOnlineUsers()) {
                    user.getUserStats().removeExpiredModifiers();
                }
            }
        };
        plugin.getScheduler().timerSync(task, 0L, plugin.configInt(Option.MODIFIER_TEMPORARY_CHECK_PERIOD) * 50L,
                TimeUnit.MILLISECONDS);
    }

}
