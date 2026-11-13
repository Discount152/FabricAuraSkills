package dev.granite.graniteskills.common.loot;

import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootValues;
import dev.granite.graniteskills.common.commands.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public class CommandLoot extends Loot {

    private final CommandExecutor executor;
    private final String[] commands;

    public CommandLoot(LootValues values, CommandExecutor executor, @NotNull String[] commands) {
        super(values);
        this.executor = executor;
        this.commands = commands;
    }

    public String[] getCommands() {
        return commands;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

}
