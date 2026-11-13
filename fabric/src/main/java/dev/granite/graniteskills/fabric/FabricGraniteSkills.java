package dev.granite.graniteskills.fabric;

import com.mojang.brigadier.StringReader;
import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.ability.AbilityManager;
import dev.granite.graniteskills.common.ability.AbilityRegistry;
import dev.granite.graniteskills.common.antiafk.AntiAfkManager;
import dev.granite.graniteskills.common.api.ApiGraniteSkills;
import dev.granite.graniteskills.common.api.ApiRegistrationUtil;
import dev.granite.graniteskills.common.api.implementation.ApiProvider;
import dev.granite.graniteskills.common.config.ConfigProvider;
import dev.granite.graniteskills.common.config.ConfigurateLoader;
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
import dev.granite.graniteskills.common.message.type.CommandMessage;
import dev.granite.graniteskills.common.migration.MigrationManager;
import dev.granite.graniteskills.common.modifier.ModifierManager;
import dev.granite.graniteskills.common.ref.PlayerRef;
import dev.granite.graniteskills.common.region.WorldManager;
import dev.granite.graniteskills.common.reward.RewardManager;
import dev.granite.graniteskills.common.scheduler.Scheduler;
import dev.granite.graniteskills.common.skill.SkillLoader;
import dev.granite.graniteskills.common.skill.SkillManager;
import dev.granite.graniteskills.common.skill.SkillRegistry;
import dev.granite.graniteskills.common.source.SourceTypeRegistry;
import dev.granite.graniteskills.common.stat.StatLoader;
import dev.granite.graniteskills.common.stat.StatManager;
import dev.granite.graniteskills.common.stat.StatRegistry;
import dev.granite.graniteskills.common.storage.StorageFactory;
import dev.granite.graniteskills.common.storage.StorageProvider;
import dev.granite.graniteskills.common.storage.StorageType;
import dev.granite.graniteskills.common.storage.backup.BackupProvider;
import dev.granite.graniteskills.common.storage.sql.SqlStorageProvider;
import dev.granite.graniteskills.common.trait.TraitManager;
import dev.granite.graniteskills.common.trait.TraitRegistry;
import dev.granite.graniteskills.common.ui.UiProvider;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.user.UserManager;
import dev.granite.graniteskills.common.util.PlatformUtil;
import dev.granite.graniteskills.fabric.ability.FabricDefenseAbilities;
import dev.granite.graniteskills.fabric.item.FabricItemRegistry;
import dev.granite.graniteskills.fabric.loot.FabricApiLootBridge;
import dev.granite.graniteskills.fabric.loot.FabricLootManager;
import dev.granite.graniteskills.fabric.requirement.FabricRequirementManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class FabricGraniteSkills implements GraniteSkillsPlugin {

    private static volatile FabricGraniteSkills instance;

    private final MinecraftServer server;
    private final File dataFolder;

    private GraniteSkillsApi api;
    private ApiProvider apiProvider;
    private FabricLogger logger;
    private FabricMessageProvider messageProvider;
    private FabricConfigProvider configProvider;
    private SkillManager skillManager;
    private FabricAbilityManager abilityManager;
    private FabricManaAbilityManager manaAbilityManager;
    private FabricStatManager statManager;
    private FabricTraitManager traitManager;
    private SkillRegistry skillRegistry;
    private StatRegistry statRegistry;
    private TraitRegistry traitRegistry;
    private AbilityRegistry abilityRegistry;
    private ManaAbilityRegistry manaAbilityRegistry;
    private SourceTypeRegistry sourceTypeRegistry;
    private FabricItemRegistry itemRegistry;
    private FabricScheduler scheduler;
    private FabricEventHandler eventHandler;
    private HookManager hookManager;
    private FabricUserManager userManager;
    private XpRequirements xpRequirements;
    private FabricWorldManager worldManager;
    private LeaderboardManager leaderboardManager;
    private FabricUiProvider uiProvider;
    private RewardManager rewardManager;
    private FabricLevelManager levelManager;
    private FabricAntiAfkManager antiAfkManager;
    private StorageProvider storageProvider;
    private BackupProvider backupProvider;
    private PresetManager presetManager;
    private FabricModifierManager modifierManager;
    private FabricMenuHelper menuHelper;
    private FabricPlatformUtil platformUtil;
    private FabricLootManager lootManager;
    private FabricRequirementManager requirementManager;
    /** Set after {@link #finishEnable()} so reload/save commands do not run during async startup. */
    private volatile boolean runtimeReady;

    public FabricGraniteSkills(MinecraftServer server) {
        this.server = server;
        this.dataFolder = FabricLoader.getInstance().getConfigDir().resolve("graniteskills").toFile();
    }

    public static void setInstance(FabricGraniteSkills plugin) {
        instance = plugin;
    }

    public static @Nullable FabricGraniteSkills getInstance() {
        return instance;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public void enable() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            FabricGraniteSkillsMod.LOGGER.error("Could not create data folder: {}", dataFolder.getAbsolutePath());
        }
        logger = new FabricLogger(FabricGraniteSkillsMod.LOGGER);
        platformUtil = new FabricPlatformUtil();
        messageProvider = new FabricMessageProvider(this);
        messageProvider.loadMessages();

        skillManager = new SkillManager(this);
        abilityManager = new FabricAbilityManager(this);
        manaAbilityManager = new FabricManaAbilityManager(this);
        statManager = new FabricStatManager(this);
        traitManager = new FabricTraitManager(this);

        skillRegistry = new SkillRegistry(this);
        statRegistry = new StatRegistry(this);
        traitRegistry = new TraitRegistry(this);
        abilityRegistry = new AbilityRegistry(this);
        manaAbilityRegistry = new ManaAbilityRegistry(this);
        sourceTypeRegistry = new SourceTypeRegistry();
        sourceTypeRegistry.registerDefaults();
        itemRegistry = new FabricItemRegistry(this);
        itemRegistry.loadFromDisk();

        scheduler = new FabricScheduler(this, server);
        eventHandler = new FabricEventHandler();
        hookManager = new HookManager();
        userManager = new FabricUserManager(this);
        presetManager = new PresetManager(this);

        lootManager = new FabricLootManager(this);
        api = new ApiGraniteSkills(this);
        apiProvider = new FabricApiProvider(new FabricApiLootBridge(lootManager));
        ApiRegistrationUtil.register(api);

        generateConfigs();
        MigrationManager migrationManager = new MigrationManager(this);
        migrationManager.attemptConfigMigration();

        configProvider = new FabricConfigProvider(this);
        configProvider.loadOptions();

        initStorageProvider();
        migrationManager.attemptUserMigration();

        worldManager = new FabricWorldManager(this);
        worldManager.loadWorlds();

        backupProvider = new BackupProvider(this);
        xpRequirements = new XpRequirements(this);
        leaderboardManager = new LeaderboardManager(this, new FabricLeaderboardExclusion(this));
        uiProvider = new FabricUiProvider(this);
        modifierManager = new FabricModifierManager(this);
        menuHelper = new FabricMenuHelper();
        rewardManager = new RewardManager(this);
        levelManager = new FabricLevelManager(this);
        antiAfkManager = new FabricAntiAfkManager(this);
        antiAfkManager.registerChecks();

        FabricPlayerLifecycle.register(this);

        scheduler.executeSync(this::finishEnable);
    }

    private void finishEnable() {
        loadSkills();
        requirementManager = new FabricRequirementManager(this);
        lootManager.loadLootTables();
        levelManager.loadXpRequirements();
        levelManager.registerFabricXpHandlers();
        uiProvider.getBossBarManager().loadOptions();
        rewardManager.loadRewards();
        traitManager.registerTraitImplementations();
        abilityManager.registerAbilityImplementations();
        FabricDefenseAbilities.registerImmunityListeners(this);
        FabricDefenseAbilities.registerNoDebuffFireListener(this);
        manaAbilityManager.registerProviders();
        scheduler.executeAsync(() -> leaderboardManager.updateLeaderboards());
        leaderboardManager.startLeaderboardUpdater();
        statManager.scheduleTemporaryModifierTask();
        modifierManager.registerSchedulesIfNeeded();
        runtimeReady = true;
        logger.info("Granite Skills enabled — block/entity/damage/stat/jump/fishing XP; consume, splash, anvil, grindstone, enchanting table, brewing (BREW + TAKEOUT); mana abilities; permissions via fabric-permissions-api. Use /graniteskills reload after editing configs; see fabric/FABRIC_QUICKSTART.md.");
    }

    private void loadSkills() {
        SkillLoader skillLoader = new SkillLoader(this);
        skillLoader.loadSkills();
        StatLoader statLoader = new StatLoader(this);
        statLoader.loadStats();
    }

    private void generateConfigs() {
        ConfigurateLoader loader = new ConfigurateLoader(this, null);
        loader.generateUserFile("config.yml");
        loader.generateUserFile("skills.yml");
        loader.generateUserFile("stats.yml");
        loader.generateUserFile("abilities.yml");
        loader.generateUserFile("mana_abilities.yml");
        loader.generateUserFile("xp_requirements.yml");
        for (dev.granite.graniteskills.api.skill.Skill skill : dev.granite.graniteskills.api.skill.Skills.values()) {
            String sources = "sources/" + skill.name().toLowerCase(Locale.ROOT) + ".yml";
            loader.generateUserFile(sources);
        }
    }

    private void initStorageProvider() {
        StorageType type = configBoolean(Option.SQL_ENABLED) ? StorageType.MYSQL : StorageType.YAML;
        StorageFactory storageFactory = new FabricStorageFactory(this);
        storageProvider = storageFactory.createStorageProvider(type);
        storageProvider.startAutoSaving();
    }

    public void disable() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (userManager != null && storageProvider != null) {
            for (User user : userManager.getUserMap().values()) {
                user.cleanUp();
                try {
                    storageProvider.save(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            userManager.getUserMap().clear();
        }
        if (itemRegistry != null) {
            itemRegistry.saveToDisk();
        }
        if (leaderboardManager != null) {
            leaderboardManager.getLeaderboardExclusion().saveToFile();
        }
        if (storageProvider instanceof SqlStorageProvider sql) {
            sql.getPool().disable();
        }
    }

    @Override
    public GraniteSkillsApi getApi() {
        return api;
    }

    @Override
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Override
    public ConfigProvider config() {
        return configProvider;
    }

    @Override
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public FabricAbilityManager getFabricAbilityManager() {
        return abilityManager;
    }

    @Override
    public ManaAbilityManager getManaAbilityManager() {
        return manaAbilityManager;
    }

    @Override
    public StatManager getStatManager() {
        return statManager;
    }

    @Override
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public FabricItemRegistry getFabricItemRegistry() {
        return itemRegistry;
    }

    @Override
    public LevelManager getLevelManager() {
        return levelManager;
    }

    public FabricLootManager getFabricLootManager() {
        return lootManager;
    }

    public FabricRequirementManager getFabricRequirementManager() {
        return requirementManager;
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public XpRequirements getXpRequirements() {
        return xpRequirements;
    }

    @Override
    public PlatformLogger logger() {
        return logger;
    }

    @Override
    public SkillManager getSkillManager() {
        return skillManager;
    }

    @Override
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    @Override
    public StatRegistry getStatRegistry() {
        return statRegistry;
    }

    @Override
    public TraitRegistry getTraitRegistry() {
        return traitRegistry;
    }

    @Override
    public TraitManager getTraitManager() {
        return traitManager;
    }

    @Override
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    @Override
    public ManaAbilityRegistry getManaAbilityRegistry() {
        return manaAbilityRegistry;
    }

    @Override
    public SourceTypeRegistry getSourceTypeRegistry() {
        return sourceTypeRegistry;
    }

    @Override
    public HookManager getHookManager() {
        return hookManager;
    }

    @Override
    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    @Override
    public UiProvider getUiProvider() {
        return uiProvider;
    }

    @Override
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    @Override
    public BackupProvider getBackupProvider() {
        return backupProvider;
    }

    @Override
    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public MenuHelper getMenuHelper() {
        return menuHelper;
    }

    @Override
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public PresetManager getPresetManager() {
        return presetManager;
    }

    @Override
    public PlatformUtil getPlatformUtil() {
        return platformUtil;
    }

    @Override
    public ApiProvider getApiProvider() {
        return apiProvider;
    }

    @Override
    public ModifierManager getModifierManager() {
        return modifierManager;
    }

    @Override
    public AntiAfkManager getAntiAfkManager() {
        return antiAfkManager;
    }

    @Override
    public User getUser(PlayerRef ref) {
        if (ref instanceof FabricPlayerRef fpr) {
            return userManager.getUser(fpr.player());
        }
        if (ref.get() instanceof ServerPlayer player) {
            return userManager.getUser(player);
        }
        throw new IllegalArgumentException("Unsupported PlayerRef for Fabric: " + ref);
    }

    @Override
    public String getMsg(MessageKey key, Locale locale) {
        return messageProvider.get(key, locale);
    }

    @Override
    public String getPrefix(Locale locale) {
        return messageProvider.get(CommandMessage.PREFIX, locale);
    }

    @Override
    public void runConsoleCommand(String command) {
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        try {
            server.getCommands().getDispatcher().execute(new StringReader(cmd), server.createCommandSourceStack());
        } catch (Exception e) {
            logger.warn("Failed to run console command: " + cmd, e);
        }
    }

    @Override
    public void runPlayerCommand(User user, String command) {
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        try {
            server.getCommands().getDispatcher().execute(new StringReader(cmd), player.createCommandSourceStack());
        } catch (Exception e) {
            logger.warn("Failed to run player command: " + cmd, e);
        }
    }

    @Override
    public InputStream getResource(@NotNull String path) {
        return FabricGraniteSkills.class.getClassLoader().getResourceAsStream(path);
    }

    @Override
    public void saveResource(String path, boolean replace) {
        File out = new File(dataFolder, path);
        if (out.exists() && !replace) {
            return;
        }
        try (InputStream in = getResource(path)) {
            if (in == null) {
                return;
            }
            File parent = out.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRuntimeReady() {
        return runtimeReady;
    }

    /**
     * Reloads configs and YAML-driven data from disk. Does not re-register Fabric event callbacks (safe during
     * normal operation). Call only after startup has finished ({@link #finishEnable()}).
     */
    public void reloadFromDisk() {
        if (!runtimeReady) {
            throw new IllegalStateException("GraniteSkills runtime is not ready yet");
        }
        logger.info("GraniteSkills: reloading configuration and skill data...");
        runReloadStep("messages", () -> messageProvider.loadMessages());
        runReloadStep("config options", () -> configProvider.loadOptions());
        runReloadStep("items registry", () -> itemRegistry.loadFromDisk());
        runReloadStep("skills & stats", this::loadSkills);
        runReloadStep("requirements (global + blocks)", () -> {
            if (requirementManager != null) {
                requirementManager.load();
            }
        });
        runReloadStep("world lists", () -> worldManager.loadWorlds());
        runReloadStep("loot tables", () -> lootManager.loadLootTables());
        runReloadStep("XP requirements", () -> levelManager.loadXpRequirements());
        runReloadStep("rewards", () -> rewardManager.loadRewards());
        runReloadStep("boss bar options", () -> uiProvider.getBossBarManager().loadOptions());
        for (User u : userManager.getUserMap().values()) {
            statManager.reloadAllTraits(u);
        }
        scheduler.executeAsync(() -> leaderboardManager.updateLeaderboards());
        logger.info("GraniteSkills: reload finished.");
    }

    private void runReloadStep(String name, Runnable step) {
        try {
            step.run();
        } catch (Exception e) {
            logger.warn("Reload step '" + name + "' failed: " + e.getMessage(), e);
        }
    }

    /**
     * Writes every loaded user profile to storage (YAML or SQL).
     *
     * @return number of profiles saved without throwing
     */
    public int saveAllUserProfiles() {
        if (!runtimeReady) {
            throw new IllegalStateException("GraniteSkills runtime is not ready yet");
        }
        int ok = 0;
        for (User u : userManager.getUserMap().values()) {
            try {
                storageProvider.save(u);
                ok++;
            } catch (Exception e) {
                logger.warn("Failed to save user " + u.getUuid() + ": " + e.getMessage(), e);
            }
        }
        return ok;
    }

    @Override
    public File getPluginFolder() {
        return dataFolder;
    }

    @Override
    public boolean isAtLeastVersion(int majorVersion) {
        return majorVersion <= 1;
    }

    @Override
    public boolean isAtLeastVersion(int majorVersion, int minorVersion) {
        return true;
    }
}
