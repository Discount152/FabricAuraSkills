package dev.granite.graniteskills.common.reward.type;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.hooks.PlaceholderHook;
import dev.granite.graniteskills.common.message.MessageKey;
import dev.granite.graniteskills.common.reward.SkillReward;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class MessagedReward extends SkillReward {

    @Nullable
    protected final String menuMessage;
    @Nullable
    protected final String chatMessage;

    public MessagedReward(GraniteSkillsPlugin plugin, Skill skill, @Nullable String menuMessage, @Nullable String chatMessage) {
        super(plugin, skill);
        this.menuMessage = menuMessage;
        this.chatMessage = chatMessage;
    }

    @Override
    @Nullable
    public String getMenuMessage(User user, Locale locale, Skill skill, int level) {
        return attemptAsMessageKey(menuMessage, user, locale, skill, level, true);
    }

    @Override
    @Nullable
    public String getChatMessage(User user, Locale locale, Skill skill, int level) {
        return attemptAsMessageKey(chatMessage, user, locale, skill, level, true);
    }

    /**
     * Attempts to use the input as a message key. If a matching translation for the key is found, it will return the translation.
     * Otherwise it will return the key.
     */
    private String attemptAsMessageKey(@Nullable String potentialKey, User user, Locale locale, Skill skill, int level, boolean raw) {
        if (potentialKey == null) return null;
        String message;
        if (raw) {
            message = plugin.getMessageProvider().getRaw(MessageKey.of(potentialKey), locale);
        } else {
            message = plugin.getMessageProvider().get(MessageKey.of(potentialKey), locale);
        }
        if (message == null) {
            message = potentialKey;
        }
        return replacePlaceholders(message, user, skill, level);
    }

    private String replacePlaceholders(String message, User user, Skill skill, int level) {
        message = TextUtil.replace(message, "{player}", user.getUsername(),
                "{skill}", skill.getId().getSimpleName(),
                "{skill_name}", skill.getDisplayName(plugin.getDefaultLanguage()),
                "{level}", String.valueOf(level));
        if (hooks.isRegistered(PlaceholderHook.class)) {
            message = hooks.getHook(PlaceholderHook.class).setPlaceholders(user, message);
        }
        message = TextUtil.replaceNonEscaped(message, "&", "§");
        message = TextUtil.replace(message, "\\n", "\n");
        return message;
    }

}
