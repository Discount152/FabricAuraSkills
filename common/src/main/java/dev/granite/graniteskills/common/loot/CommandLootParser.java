package dev.granite.graniteskills.common.loot;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.api.loot.LootParsingContext;
import dev.granite.graniteskills.api.loot.LootRequirements;
import dev.granite.graniteskills.common.commands.CommandExecutor;
import dev.granite.graniteskills.common.util.data.Validate;

import java.util.ArrayList;
import java.util.Locale;

public class CommandLootParser implements LootParser {

    @Override
    public Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements) {
        String executorName = config.node("executor").getString("console");
        CommandExecutor executor = CommandExecutor.valueOf(executorName.toUpperCase(Locale.ROOT));

        String[] commands;
        if (!config.node("commands").empty()) {
            commands = config.node("commands")
                    .getList(String.class, new ArrayList<>())
                    .toArray(new String[0]);
        } else {
            String command = config.node("command").getString("");
            Validate.notNull(command, "Command loot must specify key command");
            commands = new String[]{command};
        }

        return new CommandLoot(context.parseValues(config, requirements), executor, commands);
    }

}
