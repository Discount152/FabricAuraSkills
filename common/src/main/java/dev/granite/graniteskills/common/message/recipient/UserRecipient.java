package dev.granite.graniteskills.common.message.recipient;

import dev.granite.graniteskills.common.user.User;
import net.kyori.adventure.text.Component;

public class UserRecipient implements Recipient {

    private final User user;

    public UserRecipient(User user) {
        this.user = user;
    }

    @Override
    public void sendMessage(Component component) {
        user.sendMessage(component);
    }

}
