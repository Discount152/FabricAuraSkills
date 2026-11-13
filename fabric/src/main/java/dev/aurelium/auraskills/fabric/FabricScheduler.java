package dev.aurelium.auraskills.fabric;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.aurelium.auraskills.common.scheduler.ScheduledTask;
import dev.aurelium.auraskills.common.scheduler.Scheduler;
import dev.aurelium.auraskills.common.scheduler.Task;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.scheduler.TaskStatus;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class FabricScheduler extends Scheduler {

    private static final Task SYNC_NOOP = new Task() {
        @Override
        public TaskStatus getStatus() {
            return TaskStatus.STOPPED;
        }

        @Override
        public void cancel() {
        }
    };

    private final MinecraftServer server;
    private final ScheduledExecutorService delayExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "auraskills-fabric-delay");
        t.setDaemon(true);
        return t;
    });
    private final ScheduledExecutorService asyncTimerExecutor = Executors.newScheduledThreadPool(0,
            new ThreadFactoryBuilder().setNameFormat("auraskills-fabric-async-timer-%d").setDaemon(true).build());

    public FabricScheduler(FabricAuraSkills plugin, MinecraftServer server) {
        super(plugin);
        this.server = server;
    }

    @Override
    public void shutdown() {
        delayExecutor.shutdown();
        asyncTimerExecutor.shutdown();
        super.shutdown();
    }

    @Override
    public Task executeSync(Runnable runnable) {
        server.execute(runnable);
        return SYNC_NOOP;
    }

    @Override
    public Task scheduleSync(Runnable runnable, long delay, TimeUnit timeUnit) {
        long millis = Math.max(0L, timeUnit.toMillis(delay));
        ScheduledFuture<?> future = delayExecutor.schedule(() -> server.execute(runnable), millis, TimeUnit.MILLISECONDS);
        return new ScheduledTask(future);
    }

    @Override
    public Task timerSync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
        long dMs = Math.max(0L, timeUnit.toMillis(delay));
        long pMs = Math.max(1L, timeUnit.toMillis(period));
        ScheduledFuture<?> future = delayExecutor.scheduleAtFixedRate(() -> server.execute(runnable), dMs, pMs, TimeUnit.MILLISECONDS);
        Task task = new ScheduledTask(future);
        runnable.injectTask(task);
        return task;
    }

    @Override
    public Task timerAsync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
        long dMs = Math.max(0L, timeUnit.toMillis(delay));
        long pMs = Math.max(1L, timeUnit.toMillis(period));
        ScheduledFuture<?> future = asyncTimerExecutor.scheduleAtFixedRate(runnable, dMs, pMs, TimeUnit.MILLISECONDS);
        Task task = new ScheduledTask(future);
        runnable.injectTask(task);
        return task;
    }
}
