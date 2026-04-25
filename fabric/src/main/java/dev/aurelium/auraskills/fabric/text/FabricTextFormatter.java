package dev.aurelium.auraskills.fabric.text;

import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port of the Bukkit / Slate {@code TextFormatter}: MiniMessage in YAML, legacy out for
 * components that are serialized for storage, matching Paper behavior.
 */
public final class FabricTextFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger("auraskills");

    private static final String[] LEGACY_CODES = {
            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9",
            "&a", "&b", "&c", "&d", "&e", "&f",
            "&k", "&l", "&m", "&n", "&o", "&r"
    };
    private static final String[] MINI_MESSAGE_CODES = {
            "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>", "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>",
            "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>", "<white>",
            "<obfuscated>", "<bold>", "<strikethrough>", "<underline>", "<italic>", "<reset>"
    };

    private static final LegacyComponentSerializer LEGACY_OUT = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private FabricTextFormatter() {
    }

    public static Component toComponent(String message) {
        if (message == null) {
            return Component.empty();
        }
        String m = TextUtil.replace(message, "§", "&");
        m = legacyCodesToMiniMessage(m);
        try {
            return removeItalic(MINI.deserialize(m));
        } catch (ParsingException e) {
            LOGGER.info("[FabricAuraSkills] Error applying MiniMessage to message; passing through raw: " + m);
            return removeItalic(Component.text(m));
        }
    }

    public static Component removeItalic(Component component) {
        return Component.empty()
                .style(Style.style(TextDecoration.ITALIC.withState(false)))
                .append(component);
    }

    public static String toString(Component component) {
        String message = LEGACY_OUT.serialize(component);
        return TextUtil.replaceNonEscaped(message, "&", "§");
    }

    public static String legacyCodesToMiniMessage(String input) {
        for (int i = 0; i < LEGACY_CODES.length; i++) {
            input = input.replace(LEGACY_CODES[i], MINI_MESSAGE_CODES[i]);
        }
        return input;
    }
}
