package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.commands.CommandExecutor;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.type.CommandReward;
import dev.granite.graniteskills.common.util.data.Validate;

public class CommandRewardBuilder extends MessagedRewardBuilder {

    private CommandExecutor executor;
    private String command;
    private CommandExecutor revertExecutor;
    private String revertCommand;

    public CommandRewardBuilder(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    public CommandRewardBuilder executor(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandRewardBuilder command(String command) {
        this.command = command;
        return this;
    }

    public CommandRewardBuilder revertCommand(String revertCommand) {
        this.revertCommand = revertCommand;
        return this;
    }

    public CommandRewardBuilder revertExecutor(CommandExecutor revertExecutor) {
        this.revertExecutor = revertExecutor;
        return this;
    }

    @Override
    public SkillReward build() {
        Validate.allNotNull(
                "executor", executor,
                "command", command
        );
        return new CommandReward(plugin, skill, menuMessage, chatMessage, executor, command, revertExecutor, revertCommand);
    }

}
