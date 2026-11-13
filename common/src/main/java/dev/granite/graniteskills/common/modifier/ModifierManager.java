package dev.granite.graniteskills.common.modifier;

import dev.granite.graniteskills.common.user.User;

public interface ModifierManager {

    void applyModifiers(User user, boolean reload);

}
