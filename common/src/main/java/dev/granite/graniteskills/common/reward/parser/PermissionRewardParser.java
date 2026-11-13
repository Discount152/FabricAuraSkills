package dev.granite.graniteskills.common.reward.parser;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.builder.PermissionRewardBuilder;
import org.spongepowered.configurate.ConfigurationNode;

public class PermissionRewardParser extends RewardParser {

    public PermissionRewardParser(GraniteSkillsPlugin plugin, Skill skill) {
        super(plugin, skill);
    }

    @Override
    public SkillReward parse(ConfigurationNode config) {
        var builder = new PermissionRewardBuilder(plugin)
                .permission(config.node("permission").getString())
                .value(config.node("value").getBoolean(true))
                .menuMessage(config.node("menu_message").getString())
                .chatMessage(config.node("chat_message").getString());

        if (!config.node("message").empty()) {
            String message = config.node("message").getString();
            builder.chatMessage(message).menuMessage(message);
        }

        return builder.skill(skill).build();
    }

}
