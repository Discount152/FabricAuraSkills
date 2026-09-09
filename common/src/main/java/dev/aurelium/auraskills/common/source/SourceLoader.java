package dev.aurelium.auraskills.common.source;

import dev.aurelium.auraskills.api.item.ItemFilter;
import dev.aurelium.auraskills.api.item.ItemFilterMeta;
import dev.aurelium.auraskills.api.item.LootItemFilter;
import dev.aurelium.auraskills.api.item.PotionData;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.*;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.api.implementation.ApiConfigNode;
import dev.aurelium.auraskills.common.config.ConfigurateLoader;
import dev.aurelium.auraskills.common.source.parser.BlockSourceParser;
import dev.aurelium.auraskills.common.source.parser.ParsingExtension;
import dev.aurelium.auraskills.common.source.parser.util.*;
import dev.aurelium.auraskills.common.util.file.ConfigUpdate;
import dev.aurelium.auraskills.common.util.file.FileUtil;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SourceLoader {

    private final AuraSkillsPlugin plugin;
    private final ConfigurateLoader configurateLoader;
    private final SourceFileUpdates fileUpdates = new SourceFileUpdates();

    public SourceLoader(AuraSkillsPlugin plugin) {
        this.plugin = plugin;
        // Register utility serializers
        BaseContext context = new BaseContext(plugin.getApi());
        TypeSerializerCollection sourceSerializers = TypeSerializerCollection.builder()
                .register(ItemFilterMeta.class, new UtilitySerializer<>(new ItemFilterMetaParser(), context))
                .register(ItemFilter.class, new UtilitySerializer<>(new ItemFilterParser(), context))
                .register(LootItemFilter.class, new UtilitySerializer<>(new LootItemFilterParser(), context))
                .register(PotionData.class, new UtilitySerializer<>(new PotionDataParser(), context))
                .register(BlockXpSource.BlockXpSourceState.class, new UtilitySerializer<>(new BlockSourceParser.BlockSourceStateParser(), context))
                .build();
        this.configurateLoader = new ConfigurateLoader(plugin, sourceSerializers);
    }

    public List<XpSource> loadSources(Skill skill, File contentDirectory, boolean loadEmbedded) {
        return loadSourcesDetailed(skill, contentDirectory, loadEmbedded).sources();
    }

    public SourceLoadResult loadSourcesDetailed(Skill skill, File contentDirectory, boolean loadEmbedded) {
        File sourceFile = new File(contentDirectory, "sources/" + skill.name().toLowerCase(Locale.ROOT) + ".yml");
        String fileName = "sources/" + skill.name().toLowerCase(Locale.ROOT) + ".yml";
        SourceLoadDiagnostics diagnostics = new SourceLoadDiagnostics(skill.getId().toString(), sourceFile.getPath());
        try {
            Map<String, ConfigurationNode> embeddedSources = new ConcurrentHashMap<>();
            ConfigurationNode fileDefault = null;
            ConfigurationNode embedded = configurateLoader.loadEmbeddedFileOrNull(fileName);
            diagnostics.setEmbeddedFileExists(embedded != null);
            if (loadEmbedded && embedded != null) {
                fileDefault = embedded.node("default");
                addToMap(embedded, embeddedSources);
            }
            diagnostics.setEmbeddedSourceKeys(embeddedSources.keySet());

            diagnostics.setDiskFileExists(sourceFile.exists());
            ConfigurationNode user = configurateLoader.loadUserFile(sourceFile);
            ConfigurationNode userDefault = user.node("default");

            Map<String, ConfigurationNode> userSources = new HashMap<>();
            addToMap(user, userSources);
            diagnostics.setDiskSourceKeys(userSources.keySet());

            if (updateUserFile(embeddedSources, userSources, sourceFile, embedded, user, skill)) {
                user = configurateLoader.loadUserFile(sourceFile);
                userDefault = user.node("default");
                userSources = new HashMap<>();
                addToMap(user, userSources);
                diagnostics.setDiskSourceKeys(userSources.keySet());
            }

            Map<String, ConfigurationNode> sources = new HashMap<>();
            LinkedHashSet<String> sourceNames = new LinkedHashSet<>();
            if (loadEmbedded) {
                sourceNames.addAll(embeddedSources.keySet());
            }
            sourceNames.addAll(userSources.keySet());
            diagnostics.setMergedSourceKeys(sourceNames);

            for (String sourceName : sourceNames) {
                ConfigurationNode userNode = userSources.get(sourceName);
                ConfigurationNode merged;
                if (loadEmbedded && fileDefault != null) {
                    ConfigurationNode embeddedNode = embeddedSources.get(sourceName);
                    merged = configurateLoader.mergeNodes(fileDefault, embeddedNode, userDefault, userNode);
                } else {
                    merged = configurateLoader.mergeNodes(userDefault, userNode);
                }
                sources.put(sourceName, merged);
            }

            List<XpSource> deserializedSources = new ArrayList<>();
            for (Map.Entry<String, ConfigurationNode> entry : sources.entrySet()) {
                String sourceName = entry.getKey();
                ConfigurationNode sourceNode = entry.getValue();
                try {
                    NamespacedId id = NamespacedId.of(NamespacedId.AURASKILLS, sourceName);
                    String type = sourceNode.node("type").getString();
                    if (type == null) {
                        diagnostics.addRejected(id.toString(), "missing type");
                        continue;
                    }
                    applyNodeReplacements(sourceNode, sourceNode, sourceName);
                    XpSource source = parseSourceFromType(type, sourceNode, sourceName, diagnostics);
                    if (source == null) {
                        diagnostics.addRejected(id.toString(), "version-invalid");
                        continue;
                    }
                    if (source.getXp() <= 0.0) {
                        diagnostics.addRejected(id.toString(), "xp<=0");
                        continue;
                    }
                    deserializedSources.add(source);
                    registerMenuItem(source, sourceNode);
                } catch (Exception sourceError) {
                    diagnostics.addRejected(sourceName, "parse-error: " + sourceError.getMessage());
                    plugin.logger().warn("Skipping source " + sourceName + " in " + fileName + ": " + sourceError.getMessage());
                }
            }

            // Sprint 125: if user overrides/legacy source files yield zero valid rows, safely fall back to
            // embedded defaults at runtime without overwriting disk. This keeps custom valid configs intact
            // while healing broken source pipelines for supported bundled skills.
            if (deserializedSources.isEmpty() && loadEmbedded && !embeddedSources.isEmpty()) {
                List<XpSource> embeddedOnly = new ArrayList<>();
                for (Map.Entry<String, ConfigurationNode> entry : embeddedSources.entrySet()) {
                    String sourceName = entry.getKey();
                    ConfigurationNode sourceNode;
                    if (fileDefault != null) {
                        sourceNode = configurateLoader.mergeNodes(fileDefault, entry.getValue());
                    } else {
                        sourceNode = entry.getValue().copy();
                    }
                    try {
                        String type = sourceNode.node("type").getString();
                        if (type == null) {
                            continue;
                        }
                        applyNodeReplacements(sourceNode, sourceNode, sourceName);
                        XpSource source = parseSourceFromType(type, sourceNode, sourceName, diagnostics);
                        if (source != null && source.getXp() > 0.0) {
                            embeddedOnly.add(source);
                            registerMenuItem(source, sourceNode);
                        }
                    } catch (Exception ignored) {
                        // Keep diagnostics from the primary merged pass; fallback is best-effort.
                    }
                }
                if (!embeddedOnly.isEmpty()) {
                    diagnostics.setEmbeddedFallbackUsed(true);
                    deserializedSources = embeddedOnly;
                }
            }
            diagnostics.setVersionValidSources(deserializedSources.size());
            return new SourceLoadResult(deserializedSources, diagnostics);
        } catch (Exception e) {
            plugin.logger().warn("Error loading source file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            diagnostics.setFatalError(e.getMessage());
            return new SourceLoadResult(new ArrayList<>(), diagnostics);
        }
    }

    public boolean updateUserFile(Map<String, ConfigurationNode> embeddedSources, Map<String, ConfigurationNode> userSources,
            File userFile, @Nullable ConfigurationNode embedded, ConfigurationNode user, Skill skill) throws IOException {
        String skillName = userFile.getName();

        if (embedded == null) {
            return false;
        }

        boolean updatedFileVersion = false;

        int embeddedVersion = embedded.node("file_version").getInt(0);
        int userVersion = user.node("file_version").getInt(0);

        if (userVersion < embeddedVersion) {
            Map<Integer, ConfigUpdate> updates = fileUpdates.getFileUpdates().getOrDefault(skill, new ConcurrentHashMap<>());
            // Apply all the numbered updates from versions user + 1 to embedded
            for (int updateTo = userVersion + 1; updateTo <= embeddedVersion; updateTo++) {
                ConfigUpdate configUpdate = updates.get(updateTo);
                if (configUpdate != null) {
                    configUpdate.apply(embedded, user);
                }
            }

            user.node("file_version").set(embeddedVersion);
            updatedFileVersion = true;
        }

        // We won't update the file if the file_version in the user file is not behind
        if (!updatedFileVersion) {
            return false;
        }

        int addedSources = 0;
        // Don't update these skills with new sources because users on the legacy preset will have sources duplicated between multiple skills
        if (!skillName.contains("alchemy") && !skillName.contains("agility") && !skillName.contains("enchanting")) {
            for (Entry<String, ConfigurationNode> embeddedEntry : embeddedSources.entrySet()) {
                String sourceName = embeddedEntry.getKey();
                // Skip if already in user source
                if (userSources.containsKey(sourceName)) {
                    continue;
                }
                ConfigurationNode embeddedSource = embeddedEntry.getValue();
                // Set embedded source to user file
                user.node("sources").node(sourceName).set(embeddedSource);
                addedSources++;
            }
        }

        int addedTags = 0;
        // Copy missing tags
        for (Object tagKey : embedded.node("tags").childrenMap().keySet()) {
            if (user.node("tags", tagKey).empty()) {
                user.node("tags", tagKey).set(embedded.node("tags", tagKey));
                addedTags++;
            }
        }

        FileUtil.saveYamlFile(userFile, user);
        String path = plugin.getPluginFolder().toPath().relativize(userFile.toPath()).toString();
        plugin.logger().info("Updated " + path + " with " + addedSources + " new source(s) and " + addedTags + " new tag(s). " +
                "If you had removed default sources they will be added back. Set the xp of the source to 0 to disable them instead.");
        return true;
    }

    public Map<SourceTag, List<XpSource>> loadTags(Skill skill, File contentDirectory) {
        File sourceFile = new File(contentDirectory, "sources/" + skill.name().toLowerCase(Locale.ROOT) + ".yml");
        try {
            Map<SourceTag, List<XpSource>> tagMap = new ConcurrentHashMap<>();

            ConfigurationNode user = configurateLoader.loadUserFile(sourceFile);

            for (SourceTag tag : SourceTag.ofSkill(skill)) {
                ConfigurationNode tagNode = user.node("tags").node(tag.name().toLowerCase(Locale.ROOT));

                // Parse sources from string list
                List<XpSource> sourceList = new ArrayList<>();
                for (String sourceString : tagNode.getList(String.class, new ArrayList<>())) {
                    if (sourceString.equals("*")) { // Add all sources in skill
                        sourceList.addAll(skill.getSources());
                    } else if (sourceString.startsWith("*") || sourceString.endsWith("*")) { // Add all wildcard sources in skill
                        sourceList.addAll(getFilteredSource(skill.getSources(), sourceString));
                    } else if (sourceString.startsWith("!")) { // Remove source if starts with !
                        if (sourceString.startsWith("!*") || sourceString.endsWith("*")) { // Add all wildcard sources in skill
                            sourceList.removeAll(getFilteredSource(sourceList, sourceString));
                        } else {
                            NamespacedId id = NamespacedId.fromDefault(sourceString.substring(1));
                            XpSource source = plugin.getSkillManager().getSourceById(id);
                            if (source != null) {
                                sourceList.remove(source);
                            }
                        }
                    } else { // Add raw source name
                        XpSource source = plugin.getSkillManager().getSourceById(NamespacedId.fromDefault(sourceString));
                        if (source != null) {
                            sourceList.add(source);
                        }
                    }
                }

                tagMap.put(tag, sourceList);
            }

            return tagMap;
        } catch (Exception e) {
            plugin.logger().warn("Error loading tags in sources file " + sourceFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
            // Return map with empty lists for each tag
            Map<SourceTag, List<XpSource>> fallback = new ConcurrentHashMap<>();
            for (SourceTag tag : SourceTag.ofSkill(skill)) {
                fallback.put(tag, new ArrayList<>());
            }
            return fallback;
        }
    }

    private Collection<XpSource> getFilteredSource(List<XpSource> sources, String rawFilter) {
        String filter = rawFilter.replace("!", "").replace("*", "").toLowerCase(Locale.ROOT);
        boolean isEndingWildcard = rawFilter.endsWith("*");
        boolean isBeginningWildcard = rawFilter.startsWith("*") || rawFilter.startsWith("!");

        if (isBeginningWildcard && !isEndingWildcard) {
            return sources.stream().filter(source -> source.name().toLowerCase().endsWith(filter)).collect(Collectors.toList());
        } else if (isEndingWildcard && !isBeginningWildcard) {
            return sources.stream().filter(source -> source.name().toLowerCase().startsWith(filter)).collect(Collectors.toList());
        } else {
            return sources.stream().filter(source -> source.name().toLowerCase().contains(filter)).collect(Collectors.toList());
        }
    }

    private void addToMap(ConfigurationNode root, Map<String, ConfigurationNode> sourcesMap) {
        root.node("sources").childrenMap().forEach((key, sourceNode) -> {
            String sourceName = key.toString();
            if (!sourceNode.isMap()) { // Replace leaf node to a child node with key _value
                try {
                    sourceNode.node("_value").set(sourceNode.raw());
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }
            }
            sourcesMap.put(sourceName, sourceNode);
        });
    }

    @Nullable
    private XpSource parseSourceFromType(String type, ConfigurationNode sourceNode, String sourceName, SourceLoadDiagnostics diagnostics) {
        NamespacedId sourceTypeId = NamespacedId.fromDefault(type.toLowerCase(Locale.ROOT));
        SourceType sourceType = plugin.getSourceTypeRegistry().get(sourceTypeId);
        if (sourceType == null) {
            diagnostics.addRejected(sourceName, "unknown-source-type: " + sourceTypeId);
            return null;
        }

        XpSourceParser<?> parser = sourceType.getParser();

        SourceContext context = new SourceContext(plugin.getApi(), sourceType, sourceName);
        try {
            XpSource source = (XpSource) parser.parse(ApiConfigNode.toApi(sourceNode), context);
            // Apply parsing extensions, if any
            for (ParsingExtension extension : plugin.getSourceTypeRegistry().getParsingExtensions(sourceType)) {
                source = extension.parse(source);
            }
            if (source.isVersionValid()) {
                return source;
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error deserializing source of type " + type + ": " + e.getMessage());
        }
    }

    private void registerMenuItem(XpSource source, ConfigurationNode sourceNode) {
        // Parse menu item if present
        ConfigurationNode menuNode = sourceNode.node("menu_item");
        if (!menuNode.virtual()) {
            plugin.getItemRegistry().getSourceMenuItems().parseAndRegisterMenuItem(source, menuNode);
        }
        // Parse unit name if present
        ConfigurationNode unitNode = sourceNode.node("unit");
        if (!unitNode.virtual()) {
            plugin.getItemRegistry().getSourceMenuItems().registerSourceUnit(source, unitNode.getString());
        }
    }

    /**
     * Searches baseNode for any String values with placeholders marked between curly braces
     * and replaces them with the corresponding value from parentNode where the key of the parentNode is the placeholder value
     *
     * @param baseNode The baseNode to search for placeholders and modify
     * @param parentNode The parentNode to get the replacement values from
     */
    private void applyNodeReplacements(ConfigurationNode baseNode, ConfigurationNode parentNode, String sourceName) throws SerializationException {
        for (ConfigurationNode child : baseNode.childrenMap().values()) {
            String text = child.getString();
            if (text != null) {
                // Get all placeholders between curly braces in text
                List<String> placeholders = TextUtil.getPlaceholders(text);
                for (String placeholder : placeholders) {
                    if (placeholder.equals("key")) { // Replace key with source name
                        child.set(text.replace("{key}", sourceName));
                        continue;
                    }
                    if (placeholder.equals("value")) {
                        child.set(text.replace("{value}", String.valueOf(parentNode.node("_value").raw())));
                    }
                    // Get replacement value from parentNode
                    String[] path = placeholder.split("\\."); // Split placeholder into path by periods
                    String replacement = parentNode.node((Object[]) path).getString();
                    if (replacement != null) {
                        // Replace placeholder with replacement value
                        child.set(text.replace("{" + placeholder + "}", replacement));
                    }
                }
            } else {
                applyNodeReplacements(child, parentNode, sourceName); // Recursively search for placeholders
            }
        }
    }

    public static final class SourceLoadResult {
        private final List<XpSource> sources;
        private final SourceLoadDiagnostics diagnostics;

        public SourceLoadResult(List<XpSource> sources, SourceLoadDiagnostics diagnostics) {
            this.sources = List.copyOf(sources);
            this.diagnostics = diagnostics;
        }

        public List<XpSource> sources() {
            return sources;
        }

        public SourceLoadDiagnostics diagnostics() {
            return diagnostics;
        }
    }

    public static final class SourceLoadDiagnostics {
        private final String skillId;
        private final String diskPath;
        private boolean diskFileExists;
        private boolean embeddedFileExists;
        private final Set<String> diskSourceKeys = new LinkedHashSet<>();
        private final Set<String> embeddedSourceKeys = new LinkedHashSet<>();
        private final Set<String> mergedSourceKeys = new LinkedHashSet<>();
        private int versionValidSources;
        private final Map<String, String> rejectedSources = new LinkedHashMap<>();
        private String fatalError = "";
        private boolean embeddedFallbackUsed;

        public SourceLoadDiagnostics(String skillId, String diskPath) {
            this.skillId = skillId;
            this.diskPath = diskPath;
        }

        public String skillId() { return skillId; }
        public String diskPath() { return diskPath; }
        public boolean diskFileExists() { return diskFileExists; }
        public boolean embeddedFileExists() { return embeddedFileExists; }
        public int diskSourceKeyCount() { return diskSourceKeys.size(); }
        public int embeddedSourceKeyCount() { return embeddedSourceKeys.size(); }
        public int mergedSourceKeyCount() { return mergedSourceKeys.size(); }
        public int versionValidSources() { return versionValidSources; }
        public Map<String, String> rejectedSources() { return Collections.unmodifiableMap(rejectedSources); }
        public String fatalError() { return fatalError; }
        public boolean embeddedFallbackUsed() { return embeddedFallbackUsed; }

        private void setDiskFileExists(boolean exists) { this.diskFileExists = exists; }
        private void setEmbeddedFileExists(boolean exists) { this.embeddedFileExists = exists; }
        private void setDiskSourceKeys(Collection<String> keys) { this.diskSourceKeys.clear(); this.diskSourceKeys.addAll(keys); }
        private void setEmbeddedSourceKeys(Collection<String> keys) { this.embeddedSourceKeys.clear(); this.embeddedSourceKeys.addAll(keys); }
        private void setMergedSourceKeys(Collection<String> keys) { this.mergedSourceKeys.clear(); this.mergedSourceKeys.addAll(keys); }
        private void setVersionValidSources(int count) { this.versionValidSources = count; }
        private void addRejected(String id, String reason) { this.rejectedSources.put(id, reason); }
        private void setFatalError(String error) { this.fatalError = error == null ? "" : error; }
        private void setEmbeddedFallbackUsed(boolean used) { this.embeddedFallbackUsed = used; }
    }

}
