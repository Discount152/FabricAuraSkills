package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.common.message.MessageBuilder;
import dev.aurelium.auraskills.common.message.type.CommandMessage;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Shared command **success** lines that go through the common {@link MessageBuilder} path (so
 * placeholders like {@code {mana_unit}} resolve like Bukkit) — used by **xp**, **mana**, and
 * **skill level** (Sprints 41–42).
 */
public final class FabricCommandMutationFeedback {

    private FabricCommandMutationFeedback() {}

    public static void sendToSource(
            FabricAuraSkills plugin, ServerCommandSource source, MessageBuilder built, boolean broadcast) {
        String out = plugin.getMessageProvider().applyFormatting(built.toString());
        source.sendFeedback(() -> Text.literal(out), broadcast);
    }

    public static void sendPrefixedMessageKey(
            FabricAuraSkills plugin,
            ServerCommandSource source,
            Locale locale,
            CommandMessage key,
            String... replacementPairs) {
        MessageBuilder b = MessageBuilder.create(plugin).locale(locale).prefix();
        b.message(key, replacementPairs);
        sendToSource(plugin, source, b, true);
    }

    /**
     * XP-style: {@code {amount}} / {@code {skill}} / {@code {player}} (see {@link CommandMessage}
     * for each key).
     */
    public static void sendXpStyleTriple(
            FabricAuraSkills plugin,
            ServerCommandSource source,
            Locale locale,
            CommandMessage key,
            String amount,
            String skillDisplay,
            String playerName) {
        sendPrefixedMessageKey(
                plugin,
                source,
                locale,
                key,
                "amount",
                amount,
                "skill",
                skillDisplay,
                "player",
                playerName);
    }
}
