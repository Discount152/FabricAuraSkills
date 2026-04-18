package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.ui.ActionBarManager;
import dev.aurelium.auraskills.common.ui.UiProvider;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public final class FabricActionBarManager extends ActionBarManager {

    public FabricActionBarManager(AuraSkillsPlugin plugin, UiProvider uiProvider) {
        super(plugin, uiProvider);
    }

    @Override
    public @NotNull String getHp(User user) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return "";
        }
        float hp = player.getHealth();
        return String.valueOf(Math.round(hp));
    }

    @Override
    public @NotNull String getMaxHp(User user) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return "";
        }
        float max = player.getMaxHealth();
        return String.valueOf(Math.round(max));
    }

    @Override
    public @NotNull String getWorldName(User user) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return "";
        }
        return FabricUser.worldNameCompat(player);
    }
}
