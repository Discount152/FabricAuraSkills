package dev.granite.graniteskills.fabric.command;

import dev.granite.graniteskills.common.message.MessageBuilder;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Shared command **success** lines that go through the common {@link MessageBuilder} path (so
 * placeholders like {@code {mana_unit}} resolve like Bukkit) — used by **xp**, **mana**, and
 * **skill level** (Sprints 41–42).
 */
public final class FabricCommandMutationFeedback {

    private FabricCommandMutationFeedback() {}

    public static void sendToSource(
            FabricGraniteSkills plugin, CommandSourceStack source, MessageBuilder built, boolean broadcast) {
        String out = plugin.getMessageProvider().applyFormatting(built.toString());
        source.sendSuccess(() -> Component.literal(out), broadcast);
    }

    public static void sendPrefixedMessageKey(
            FabricGraniteSkills plugin,
            CommandSourceStack source,
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
            FabricGraniteSkills plugin,
            CommandSourceStack source,
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
