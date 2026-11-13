package dev.granite.graniteskills.common.reward.type;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;

public class ItemReward extends MessagedReward {

    protected final NamespacedId itemKey;
    protected final int amount; // Amount of -1 means no amount was specified and should use amount of registered item

    public ItemReward(GraniteSkillsPlugin plugin, Skill skill, String menuMessage, String chatMessage, NamespacedId itemKey, int amount) {
        super(plugin, skill, menuMessage, chatMessage);
        this.itemKey = itemKey;
        this.amount = amount;
    }

    @Override
    public void giveReward(User user, Skill skill, int level) {
        plugin.getItemRegistry().giveItem(user, itemKey, amount);
    }

}
