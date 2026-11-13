package dev.granite.graniteskills.bukkit.commands;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.hooks.HologramsHook;
import dev.granite.graniteskills.bukkit.source.BlockLeveler;
import dev.granite.graniteskills.bukkit.trait.AnvilDiscountTrait;
import dev.granite.graniteskills.bukkit.trait.DamageReductionTrait;
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class ReloadExecutor {

    private final GraniteSkills plugin;

    public ReloadExecutor(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public void reload(CommandSender sender) {
        Locale locale = plugin.getLocale(sender);
        // Load messages
        plugin.getMessageProvider().loadMessages();
        plugin.getMessageProvider().setACFMessages(plugin.getCommandManager());
        // Load config.yml file
        plugin.config().loadOptions();
        plugin.getMessageProvider().loadDefaultLanguageOption();
        plugin.getRequirementManager().load();
        plugin.getRequirementManager().loadBlocks();
        // Load blocked/disabled worlds lists
        plugin.getWorldManager().loadWorlds();
        // Load skills
        plugin.loadSkills();
        plugin.getLevelManager().loadXpRequirements();
        plugin.getUiProvider().getBossBarManager().loadOptions();
        plugin.getRewardManager().loadRewards();
        plugin.getLootManager().loadLootTables();
        plugin.getTraitManager().getTraitImpl(DamageReductionTrait.class).resetFormula();
        plugin.getTraitManager().getTraitImpl(AnvilDiscountTrait.class).resetFormula();
        plugin.getLevelManager().getLeveler(BlockLeveler.class).clearSourceCache();
        // Load menus
        plugin.getMenuFileManager().generateDefaultFiles();
        plugin.getMenuFileManager().loadMenus();
        plugin.getUiProvider().getActionBarManager().resetActionBars();
        plugin.getAntiAfkManager().reload();
        if (plugin.getHookManager().isRegistered(HologramsHook.class)) {
            plugin.getHookManager().getHook(HologramsHook.class).loadConfig();
        }
        reloadPlayers();
        sender.sendMessage(plugin.getPrefix(locale) + plugin.getMsg(CommandMessage.RELOAD_RELOADED, locale));
    }

    private void reloadPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = plugin.getUser(player);
            plugin.getStatManager().recalculateStats(user);
        }
    }

}
