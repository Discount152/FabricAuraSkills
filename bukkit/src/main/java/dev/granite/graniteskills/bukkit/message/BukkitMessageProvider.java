package dev.granite.graniteskills.bukkit.message;

import co.aikar.commands.MessageKeys;
import co.aikar.commands.MinecraftMessageKeys;
import co.aikar.commands.PaperCommandManager;
import dev.aurelium.slate.text.TextFormatter;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.common.message.type.ACFCoreMessage;
import dev.granite.graniteskills.common.message.type.ACFMinecraftMessage;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class BukkitMessageProvider extends MessageProvider {

    private final TextFormatter textFormatter = new TextFormatter();

    public BukkitMessageProvider(GraniteSkills plugin) {
        super(plugin);
    }

    @Override
    public String applyFormatting(String message) {
        return textFormatter.toString(textFormatter.toComponent(message));
    }

    @Override
    public String componentToString(Component component) {
        return textFormatter.toString(component);
    }

    @Override
    public Component stringToComponent(String message) {
        return textFormatter.toComponent(message);
    }

    public void setACFMessages(@NotNull PaperCommandManager commandManager) {
        for (Locale locale : manager.getLoadedLanguages()) {
            for (ACFCoreMessage key : ACFCoreMessage.values()) {
                String configMsg = get(key, locale);
                commandManager.getLocales().addMessage(locale, MessageKeys.valueOf(key.name()), configMsg);
            }
            for (ACFMinecraftMessage key : ACFMinecraftMessage.values()) {
                String configMsg = get(key, locale);
                commandManager.getLocales().addMessage(locale, MinecraftMessageKeys.valueOf(key.name()), configMsg);
            }
        }
    }

}
