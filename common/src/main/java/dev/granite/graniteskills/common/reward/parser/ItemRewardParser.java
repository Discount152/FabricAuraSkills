package dev.granite.graniteskills.common.reward.parser;

import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.message.type.RewardMessage;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.reward.builder.ItemRewardBuilder;
import dev.granite.graniteskills.common.util.text.TextUtil;
import org.spongepowered.configurate.ConfigurationNode;

public class ItemRewardParser extends RewardParser {

    public ItemRewardParser(GraniteSkillsPlugin plugin, Skill skill) {
        super(plugin, skill);
    }

    @Override
    public SkillReward parse(ConfigurationNode config) {
        String key = config.node("key").getString("");
        NamespacedId itemKey = NamespacedId.fromDefault(key);

        int amount;
        if (config.node("amount").empty()) {
            amount = plugin.getItemRegistry().getItemAmount(itemKey);
        } else {
            amount = config.node("amount").getInt(-1);
        }

        var builder = new ItemRewardBuilder(plugin)
                .itemKey(itemKey)
                .amount(amount);

        if (!config.node("menu_message").empty()) {
            builder.menuMessage(config.node("menu_message").getString());
        } else {
            // Use default menu message
            String effectiveName = plugin.getItemRegistry().getEffectiveItemName(itemKey);
            if (effectiveName != null) {
                RewardMessage messageKey;
                if (amount == 1) {
                    messageKey = RewardMessage.ITEM_DEFAULT_MENU_MESSAGE;
                } else {
                    messageKey = RewardMessage.ITEM_DEFAULT_MENU_MESSAGE_MULTIPLE;
                }
                builder.menuMessage(TextUtil.replace(plugin.getMsg(messageKey, plugin.getDefaultLanguage()),
                        "{display_name}", effectiveName,
                        "{amount}", String.valueOf(amount),
                        "{key}", key));
            }
        }

        if (!config.node("chat_message").empty()) {
            builder.chatMessage(config.node("chat_message").getString());
        } else {
            // Use default chat message
            String effectiveName = plugin.getItemRegistry().getEffectiveItemName(itemKey);
            if (effectiveName != null) {
                RewardMessage messageKey;
                if (amount == 1) {
                    messageKey = RewardMessage.ITEM_DEFAULT_CHAT_MESSAGE;
                } else {
                    messageKey = RewardMessage.ITEM_DEFAULT_CHAT_MESSAGE_MULTIPLE;
                }
                builder.chatMessage(TextUtil.replace(plugin.getMsg(messageKey, plugin.getDefaultLanguage()),
                        "{display_name}", effectiveName,
                        "{amount}", String.valueOf(amount),
                        "{key}", key));
            }
        }

        if (!config.node("message").empty()) {
            String message = config.node("message").getString();
            builder.chatMessage(message).menuMessage(message);
        }

        return builder.skill(skill).build();
    }

}
