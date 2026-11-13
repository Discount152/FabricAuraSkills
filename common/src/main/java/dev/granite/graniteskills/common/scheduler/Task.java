package dev.granite.graniteskills.common.scheduler;

public interface Task {

    TaskStatus getStatus();

    void cancel();

}
