package dev.granite.graniteskills.common.message.recipient;

import co.aikar.commands.CommandIssuer;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CommandIssuerRecipient implements Recipient {

    private final GraniteSkillsPlugin plugin;
    private final CommandIssuer issuer;

    public CommandIssuerRecipient(GraniteSkillsPlugin plugin, CommandIssuer issuer) {
        this.plugin = plugin;
        this.issuer = issuer;
    }

    @Override
    public void sendMessage(Component component) {
        if (issuer.isPlayer()) { // Send component if player
            User user = plugin.getUserManager().getUser(issuer.getUniqueId());
            if (user != null) {
                user.sendMessage(component);
            }
        } else { // Convert component to string if console
            issuer.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

}
