package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.ui.ActionBarManager;
import dev.aurelium.auraskills.common.ui.UiProvider;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.text.FabricTextAdapter;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.text.NumberFormat;

public final class FabricUiProvider implements UiProvider {

    private final FabricAuraSkills plugin;
    private final ActionBarManager actionBarManager;
    private final FabricBossBarManager bossBarManager = new FabricBossBarManager();

    public FabricUiProvider(FabricAuraSkills plugin) {
        this.plugin = plugin;
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
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        Text line =
                FabricTextAdapter.toNativeText(
                        plugin.getMessageProvider().stringToComponent(message), plugin.getServer());
        player.sendMessage(line, true);
    }

    @Override
    public void sendXpBossBar(User user, Skill skill, double currentXp, double levelXp, double xpGained, int level, boolean maxed, double income) {
        // MVP: boss bar XP not shown on Fabric.
    }

    @Override
    public void sendTitle(User user, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        ServerPlayerEntity player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        int fadeInTicks = Math.max(0, fadeIn);
        int stayTicks = Math.max(0, stay);
        int fadeOutTicks = Math.max(0, fadeOut);
        Text titleText =
                FabricTextAdapter.toNativeText(
                        plugin.getMessageProvider().stringToComponent(title == null ? "" : title),
                        plugin.getServer());
        Text subtitleText =
                FabricTextAdapter.toNativeText(
                        plugin.getMessageProvider().stringToComponent(subtitle == null ? "" : subtitle),
                        plugin.getServer());
        var net = player.networkHandler;
        net.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        net.sendPacket(new TitleS2CPacket(titleText));
        net.sendPacket(new SubtitleS2CPacket(subtitleText));
    }
}
