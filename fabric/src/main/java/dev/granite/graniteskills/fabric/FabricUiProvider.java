package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.ui.ActionBarManager;
import dev.granite.graniteskills.common.ui.UiProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.text.FabricTextAdapter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

import java.text.NumberFormat;

public final class FabricUiProvider implements UiProvider {

    private final FabricGraniteSkills plugin;
    private final ActionBarManager actionBarManager;
    private final FabricBossBarManager bossBarManager;

    public FabricUiProvider(FabricGraniteSkills plugin) {
        this.plugin = plugin;
        this.actionBarManager = new FabricActionBarManager(plugin, this);
        this.bossBarManager = new FabricBossBarManager(plugin);
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
        net.kyori.adventure.text.Component adventure = plugin.getMessageProvider().stringToComponent(message);
        player.sendOverlayMessage(FabricTextAdapter.toNativeText(adventure, player.level().getServer()));
    }

    @Override
    public void sendXpBossBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
        bossBarManager.send(user, skill, currentXp, levelXp, xpGained, level, maxed, income);
    }

    @Override
    public void sendTitle(User user, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        Component nativeTitle = FabricTextAdapter.toNativeText(
                plugin.getMessageProvider().stringToComponent(title == null ? "" : title),
                player.level().getServer());
        Component nativeSubtitle = FabricTextAdapter.toNativeText(
                plugin.getMessageProvider().stringToComponent(subtitle == null ? "" : subtitle),
                player.level().getServer());
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send(new ClientboundSetSubtitleTextPacket(nativeSubtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(nativeTitle));
    }
}
