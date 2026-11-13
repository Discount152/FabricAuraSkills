package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;

public abstract class MessagedRewardBuilder extends RewardBuilder {

    protected String menuMessage;
    protected String chatMessage;

    public MessagedRewardBuilder(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    public MessagedRewardBuilder menuMessage(String menuMessage) {
        this.menuMessage = menuMessage;
        return this;
    }

    public MessagedRewardBuilder chatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }

}
