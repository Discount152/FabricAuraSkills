package dev.granite.graniteskills.bukkit.scheduler;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.granite.graniteskills.common.scheduler.Task;
import dev.granite.graniteskills.common.scheduler.TaskStatus;

public class BukkitTaskWrapper implements Task {

    private final WrappedTask bukkitTask;

    public BukkitTaskWrapper(WrappedTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public TaskStatus getStatus() {
        if (bukkitTask.isCancelled()) {
            return TaskStatus.STOPPED;
        } else {
            return TaskStatus.SCHEDULED;
        }
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

}
