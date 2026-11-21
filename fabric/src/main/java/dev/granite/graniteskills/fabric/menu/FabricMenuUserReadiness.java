package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricGraniteText;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class FabricMenuUserReadiness {

    private FabricMenuUserReadiness() {
    }

    public static @Nullable User loadedUserOrWarn(FabricGraniteSkills plugin, ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            player.sendSystemMessage(FabricGraniteText.key("graniteskills.granite.menu.user_not_loaded"), false);
        }
        return user;
    }
}
