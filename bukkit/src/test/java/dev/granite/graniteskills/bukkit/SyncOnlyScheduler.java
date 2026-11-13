package dev.granite.graniteskills.bukkit;

import dev.granite.graniteskills.bukkit.scheduler.BukkitScheduler;
import dev.granite.graniteskills.common.scheduler.Task;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;

import java.util.concurrent.TimeUnit;

public class SyncOnlyScheduler extends BukkitScheduler {

    public SyncOnlyScheduler(GraniteSkills plugin) {
        super(plugin);
    }

    @Override
    public Task executeAsync(Runnable runnable) {
        return executeSync(runnable);
    }

    @Override
    public Task scheduleAsync(Runnable runnable, long delay, TimeUnit timeUnit) {
        return scheduleSync(runnable, delay, timeUnit);
    }

    @Override
    public Task timerAsync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
        return timerSync(runnable, delay, period, timeUnit);
    }

}
