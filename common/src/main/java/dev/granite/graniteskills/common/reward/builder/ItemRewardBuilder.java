package dev.granite.graniteskills.common.reward.builder;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.type.ItemReward;
import dev.granite.graniteskills.common.util.data.Validate;

public class ItemRewardBuilder extends MessagedRewardBuilder {

    private NamespacedId itemKey;
    private int amount;

    public ItemRewardBuilder(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    public ItemRewardBuilder itemKey(NamespacedId itemKey) {
        this.itemKey = itemKey;
        return this;
    }

    public ItemRewardBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public SkillReward build() {
        Validate.allNotNull("key", itemKey);
        return new ItemReward(plugin, skill, menuMessage, chatMessage, itemKey, amount);
    }

}
