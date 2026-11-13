package dev.granite.graniteskills.common;

import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.ability.AbilityRegistry;
import dev.granite.graniteskills.common.antiafk.AntiAfkManager;
import dev.granite.graniteskills.common.api.implementation.ApiProvider;
import dev.granite.graniteskills.common.config.ConfigProvider;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.config.preset.PresetManager;
import dev.granite.graniteskills.common.event.EventHandler;
import dev.granite.graniteskills.common.hooks.HookManager;
import dev.granite.graniteskills.common.item.ItemRegistry;
import dev.granite.graniteskills.common.leaderboard.LeaderboardManager;
import dev.granite.graniteskills.common.level.LevelManager;
import dev.granite.graniteskills.common.level.XpRequirements;
import dev.granite.graniteskills.common.mana.ManaAbilityManager;
import dev.granite.graniteskills.common.mana.ManaAbilityRegistry;
import dev.granite.graniteskills.common.menu.MenuHelper;
import dev.granite.graniteskills.common.message.MessageKey;
import dev.granite.graniteskills.common.message.MessageProvider;
import dev.granite.graniteskills.common.message.PlatformLogger;
import dev.granite.graniteskills.common.modifier.ModifierManager;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.region.WorldManager;
import dev.granite.graniteskills.common.reward.RewardManager;
import dev.granite.graniteskills.common.scheduler.Scheduler;
import dev.granite.graniteskills.common.skill.SkillManager;
import dev.granite.graniteskills.common.skill.SkillRegistry;
import dev.granite.graniteskills.common.source.SourceTypeRegistry;
import dev.granite.graniteskills.common.stat.StatManager;
import dev.granite.graniteskills.common.stat.StatRegistry;
import dev.granite.graniteskills.common.storage.StorageProvider;
import dev.granite.graniteskills.common.storage.backup.BackupProvider;
import dev.granite.graniteskills.common.trait.TraitManager;
import dev.granite.graniteskills.common.trait.TraitRegistry;
import dev.granite.graniteskills.common.ui.UiProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserManager;
import dev.granite.graniteskills.common.util.PlatformUtil;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public interface GraniteSkillsPlugin {

    GraniteSkillsApi getApi();

    MessageProvider getMessageProvider();

    ConfigProvider config();

    AbilityManager getAbilityManager();

    ManaAbilityManager getManaAbilityManager();

    StatManager getStatManager();

    ItemRegistry getItemRegistry();

    LevelManager getLevelManager();

    UserManager getUserManager();

    XpRequirements getXpRequirements();

    PlatformLogger logger();

    SkillManager getSkillManager();

    SkillRegistry getSkillRegistry();

    StatRegistry getStatRegistry();

    TraitRegistry getTraitRegistry();

    TraitManager getTraitManager();

    AbilityRegistry getAbilityRegistry();

    ManaAbilityRegistry getManaAbilityRegistry();

    SourceTypeRegistry getSourceTypeRegistry();

    HookManager getHookManager();

    LeaderboardManager getLeaderboardManager();

    UiProvider getUiProvider();

    RewardManager getRewardManager();

    Scheduler getScheduler();

    StorageProvider getStorageProvider();

    BackupProvider getBackupProvider();

    WorldManager getWorldManager();

    MenuHelper getMenuHelper();

    EventHandler getEventHandler();

    PresetManager getPresetManager();

    PlatformUtil getPlatformUtil();

    ApiProvider getApiProvider();

    ModifierManager getModifierManager();

    AntiAfkManager getAntiAfkManager();

    /**
     * Get a user instance from a player reference instance
     *
     * @param ref the reference
     * @return the user
     */
    User getUser(PlayerRef ref);

    // Message convenience methods

    /**
     * Gets a message from the message provider.
     *
     * @param key The message key
     * @param locale The language to get the message in
     * @return The message
     */
    String getMsg(MessageKey key, Locale locale);

    /**
     * Gets the plugin prefix for chat messages
     *
     * @param locale The language to get the prefix in
     * @return The prefix
     */
    String getPrefix(Locale locale);

    /**
     * Gets the default language of the plugin as set by the server's configuration.
     *
     * @return The default language
     */
    default Locale getDefaultLanguage() {
        return getMessageProvider().getDefaultLanguage();
    }

    // Config convenience methods

    default boolean configBoolean(Option option) {
        return config().getBoolean(option);
    }

    default int configInt(Option option) {
        return config().getInt(option);
    }

    default double configDouble(Option option) {
        return config().getDouble(option);
    }

    default String configString(Option option) {
        return config().getString(option);
    }

    default List<String> configStringList(Option option) {
        return config().getStringList(option);
    }

    // Platform-dependent Minecraft methods

    /**
     * Executes a command as the console
     *
     * @param command The command to execute
     */
    void runConsoleCommand(String command);

    /**
     * Executes a command as a player
     *
     * @param user The player to execute the command as
     * @param command The command to execute
     */
    void runPlayerCommand(User user, String command);

    InputStream getResource(String path);

    void saveResource(String path, boolean replace);

    File getPluginFolder();

    boolean isAtLeastVersion(int majorVersion);

    boolean isAtLeastVersion(int majorVersion, int minorVersion);

}
