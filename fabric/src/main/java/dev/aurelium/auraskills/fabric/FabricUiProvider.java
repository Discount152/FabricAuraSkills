package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.ui.ActionBarManager;
import dev.aurelium.auraskills.common.ui.UiProvider;
import dev.aurelium.auraskills.common.user.User;
import java.text.NumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class FabricUiProvider implements UiProvider {

    private final ActionBarManager actionBarManager;
    private final FabricBossBarManager bossBarManager = new FabricBossBarManager();

    public FabricUiProvider(FabricAuraSkills plugin) {
        this.actionBarManager = new FabricActionBarManager(plugin, this);
    }

    public FabricBossBarManager getBossBarManager() {
        return bossBarManager;
    }

    @Override
    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }

    @Override
    public NumberFormat getFormat(FormatType type) {
        return switch (type) {
            case XP -> bossBarManager.getXpFormat();
            case PERCENT -> bossBarManager.getPercentFormat();
            case MONEY -> bossBarManager.getMoneyFormat();
        };
    }

    @Override
    public void sendActionBar(User user, String message) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null || message.isEmpty()) {
            return;
        }
        player.sendOverlayMessage(Component.literal(message));
    }

    @Override
    public void sendXpBossBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
        // MVP: boss bar XP not shown on Fabric.
    }

    @Override
    public void sendTitle(User user, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        player.sendSystemMessage(Component.literal(title));
    }
}
