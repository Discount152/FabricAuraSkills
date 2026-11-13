package dev.granite.graniteskills.common.api;

import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.api.config.MainConfig;
import dev.granite.graniteskills.api.loot.LootManager;
import dev.granite.graniteskills.api.message.MessageManager;
import dev.granite.graniteskills.api.registry.GlobalRegistry;
import dev.granite.graniteskills.api.registry.Handlers;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.registry.NamespacedRegistry;
import dev.granite.graniteskills.api.skill.XpRequirements;
import dev.granite.graniteskills.api.source.SourceManager;
import dev.granite.graniteskills.api.user.SkillsUser;
import dev.granite.graniteskills.api.user.UserManager;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.api.implementation.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApiGraniteSkills implements GraniteSkillsApi {

    private final GraniteSkillsPlugin plugin;
    private final UserManager userManager;
    private final MessageManager messageManager;
    private final XpRequirements xpRequirements;
    private final Map<String, NamespacedRegistry> namespacedRegistryMap;
    private final Handlers handlers;
    private final MainConfig mainConfig;
    private final GlobalRegistry globalRegistry;
    private final SourceManager sourceManager;

    public ApiGraniteSkills(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
        this.userManager = new ApiUserManager(plugin);
        this.messageManager = new ApiMessageManager(plugin);
        this.xpRequirements = new ApiXpRequirements(plugin);
        this.namespacedRegistryMap = new ConcurrentHashMap<>();
        this.handlers = new ApiHandlers(plugin);
        this.mainConfig = new ApiMainConfig(plugin);
        this.globalRegistry = new ApiGlobalRegistry(plugin);
        this.sourceManager = new ApiSourceManager(plugin);
    }

    public GraniteSkillsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public SkillsUser getUser(UUID playerId) {
        return userManager.getUser(playerId);
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public MessageManager getMessageManager() {
        return messageManager;
    }

    @Override
    public XpRequirements getXpRequirements() {
        return xpRequirements;
    }

    @Override
    public GlobalRegistry getGlobalRegistry() {
        return globalRegistry;
    }

    @Override
    public NamespacedRegistry useRegistry(String namespace, File contentDirectory) {
        namespace = namespace.toLowerCase(Locale.ROOT);
        if (namespace.equals(NamespacedId.GRANITESKILLS)) {
            throw new IllegalArgumentException("Cannot get a namespaced registry for graniteskills, use the name of your plugin!");
        }
        final String finalNamespace = namespace;
        return namespacedRegistryMap.computeIfAbsent(namespace, s -> {
            plugin.getSkillManager().addContentDirectory(contentDirectory);
            return new ApiNamespacedRegistry(plugin, finalNamespace, contentDirectory);
        });
    }

    @Nullable
    public NamespacedRegistry getNamespacedRegistry(String namespace) {
        return namespacedRegistryMap.get(namespace);
    }

    @Override
    public Handlers getHandlers() {
        return handlers;
    }

    @Override
    public MainConfig getMainConfig() {
        return mainConfig;
    }

    @Override
    public SourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public LootManager getLootManager() {
        return plugin.getApiProvider().getLootManager();
    }

    public Map<String, NamespacedRegistry> getNamespacedRegistryMap() {
        return namespacedRegistryMap;
    }

}
