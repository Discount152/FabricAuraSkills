package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.ui.ActionBarManager;
import dev.granite.graniteskills.common.ui.UiProvider;
import dev.granite.graniteskills.common.user.User;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class FabricActionBarManager extends ActionBarManager {

    public FabricActionBarManager(GraniteSkillsPlugin plugin, UiProvider uiProvider) {
        super(plugin, uiProvider);
    }

    @Override
    public @NotNull String getHp(User user) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return "";
        }
        float hp = player.getHealth();
        return String.valueOf(Math.round(hp));
    }

    @Override
    public @NotNull String getMaxHp(User user) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return "";
        }
        float max = player.getMaxHealth();
        return String.valueOf(Math.round(max));
    }

    @Override
    public @NotNull String getWorldName(User user) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return "";
        }
        return FabricUser.worldNameCompat(player);
    }
}
