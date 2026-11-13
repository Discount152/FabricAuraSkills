package dev.granite.graniteskills.common.loot;

import com.google.common.collect.Sets;
import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.api.loot.LootRequirements;
import dev.granite.graniteskills.api.loot.LootTable;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LootManager {

    private final GraniteSkillsPlugin plugin;
    private final Map<NamespacedId, LootTable> lootTables = new ConcurrentHashMap<>();
    private final Map<String, ContextProvider> contextProviders;
    private final Set<String> lootOptionKeys;
    private final Set<String> poolOptionKeys;
    private final Map<String, LootParser> customLootParsers;

    public LootManager(GraniteSkillsPlugin plugin) {
        this.plugin = plugin;
        this.contextProviders = new ConcurrentHashMap<>();
        this.lootOptionKeys = Sets.newConcurrentHashSet();
        this.poolOptionKeys = Sets.newConcurrentHashSet();
        this.customLootParsers = new ConcurrentHashMap<>();

        registerContextProvider(new SourceContextProvider(plugin));
        addLootOptionKeys("xp");
        addPoolOptionKeys("chance_per_luck", "require_open_water");
    }

    public abstract LootRequirements getLootRequirements(ConfigurationNode config);

    public GraniteSkillsPlugin getPlugin() {
        return plugin;
    }

    @Nullable
    public LootTable getLootTable(Skill skill) {
        return lootTables.get(skill.getId());
    }

    @Nullable
    public LootTable getLootTable(NamespacedId id) {
        return lootTables.get(id);
    }

    public Map<NamespacedId, LootTable> getLootTables() {
        return lootTables;
    }

    public void addLootTable(NamespacedId id, LootTable lootTable) {
        lootTables.put(id, lootTable);
    }

    public Set<String> getContextKeySet() {
        return contextProviders.keySet();
    }

    @Nullable
    public ContextProvider getContextProvider(String contextKey) {
        return contextProviders.get(contextKey);
    }

    public void registerContextProvider(ContextProvider contextProvider) {
        this.contextProviders.put(contextProvider.getContextKey(), contextProvider);
    }

    public Set<String> getLootOptionKeys() {
        return lootOptionKeys;
    }

    public void addLootOptionKeys(String... keys) {
        lootOptionKeys.addAll(Arrays.asList(keys));
    }

    public Set<String> getPoolOptionKeys() {
        return poolOptionKeys;
    }

    public void addPoolOptionKeys(String... keys) {
        poolOptionKeys.addAll(Arrays.asList(keys));
    }

    public Map<String, LootParser> getCustomLootParsers() {
        return customLootParsers;
    }

    public void registerCustomLootParser(String name, LootParser parser) {
        customLootParsers.put(name, parser);
    }

}
