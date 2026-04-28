package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class FabricMenuUserReadiness {

    private FabricMenuUserReadiness() {
    }

    public static @Nullable User loadedUserOrWarn(FabricAuraSkills plugin, ServerPlayerEntity player) {
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user == null) {
            player.sendMessage(FabricGraniteText.key("auraskills.granite.menu.user_not_loaded"), false);
        }
        return user;
    }
}
