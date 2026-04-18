package dev.aurelium.auraskills.fabric.requirement;

import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.common.config.ConfigurateLoader;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Loads {@code config.yml} global skill requirements and block requirement lists
 * (Bukkit {@code RequirementManager}).
 */
public final class FabricRequirementManager {

    private final FabricAuraSkills plugin;
    private Set<FabricGlobalRequirement> globalRequirements = Set.of();
    private List<FabricBlockRequirement> blockRequirements = List.of();
    private final Map<UUID, Integer> errorMessageTimer = new ConcurrentHashMap<>();

    public FabricRequirementManager(FabricAuraSkills plugin) {
        this.plugin = plugin;
        load();
        tickTimer();
    }

    public void load() {
        loadGlobalRequirements();
        loadBlocks();
    }

    private void loadGlobalRequirements() {
        ConfigurateLoader loader = new ConfigurateLoader(plugin, TypeSerializerCollection.builder().build());
        try {
            ConfigurationNode config = loader.loadUserFile("config.yml");
            Set<FabricGlobalRequirement> next = Sets.newConcurrentHashSet();
            int loaded = 0;
            for (ModifierType type : ModifierType.values()) {
                List<String> list = config.node("requirement", type.name().toLowerCase(Locale.ROOT), "global")
                        .getList(String.class, new ArrayList<>());
                for (String text : list) {
                    String[] splitText = text.split(" ");
                    if (splitText.length == 0) {
                        continue;
                    }
                    Identifier itemId = parseMaterialAsItemId(splitText[0]);
                    if (itemId == null || !Registries.ITEM.containsId(itemId)) {
                        plugin.logger().warn("Unknown material/item for global requirement: " + splitText[0]);
                        continue;
                    }
                    Map<Skill, Integer> requirements = new HashMap<>();
                    for (int i = 1; i < splitText.length; i++) {
                        String requirementText = splitText[i];
                        try {
                            String[] pair = requirementText.split(":");
                            if (pair.length < 2) {
                                continue;
                            }
                            Skill skill = plugin.getSkillRegistry().getOrNull(NamespacedId.fromDefault(pair[0].toLowerCase(Locale.ROOT)));
                            if (skill != null) {
                                int level = Integer.parseInt(pair[1]);
                                requirements.put(skill, level);
                            }
                        } catch (Exception e) {
                            plugin.logger().warn("Error parsing global " + type.name().toLowerCase(Locale.ROOT)
                                    + " requirement pair: " + requirementText);
                        }
                    }
                    next.add(new FabricGlobalRequirement(type, itemId, Map.copyOf(requirements)));
                    loaded++;
                }
            }
            this.globalRequirements = next;
            if (loaded > 0) {
                plugin.logger().info("Loaded " + loaded + " global requirement" + (loaded != 1 ? "s" : ""));
            }
        } catch (IOException e) {
            plugin.logger().warn("Error loading global requirements: " + e.getMessage());
        }
    }

    public void loadBlocks() {
        ConfigurateLoader loader = new ConfigurateLoader(plugin, TypeSerializerCollection.builder().build());
        try {
            ConfigurationNode config = loader.loadUserFile("config.yml");
            List<FabricBlockRequirement> next = new ArrayList<>();
            List<? extends ConfigurationNode> blockNodes = config.node("requirement", "blocks", "list").childrenList();
            for (ConfigurationNode blockNode : blockNodes) {
                String materialKey = blockNode.node("material").getString("");
                Identifier blockId = parseBlockMaterialId(materialKey);
                if (blockId == null || !Registries.BLOCK.containsId(blockId)) {
                    plugin.logger().warn("Unknown block for requirement.blocks.list material: " + materialKey);
                    continue;
                }
                boolean allowPlace = blockNode.node("allow_place").getBoolean(false);
                boolean allowBreak = blockNode.node("allow_break").getBoolean(false);
                boolean allowHarvest = blockNode.node("allow_harvest").getBoolean(false);
                List<FabricRequirementNode> nodes = parseRequirementNodes(blockNode.node("requirements").childrenList());
                next.add(new FabricBlockRequirement(blockId, allowPlace, allowBreak, allowHarvest, nodes));
            }
            this.blockRequirements = List.copyOf(next);
            if (!blockRequirements.isEmpty()) {
                plugin.logger().info("Loaded " + blockRequirements.size() + " block requirement" + (blockRequirements.size() != 1 ? "s" : ""));
            }
        } catch (IOException e) {
            plugin.logger().warn("Error loading block requirements: " + e.getMessage());
        }
    }

    private List<FabricRequirementNode> parseRequirementNodes(List<? extends ConfigurationNode> requirementNodes) {
        List<FabricRequirementNode> nodes = new ArrayList<>();
        for (ConfigurationNode requirementNode : requirementNodes) {
            String type = requirementNode.node("type").getString("");
            String message = requirementNode.node("message").getString("");
            switch (type) {
                case "skill_level" -> {
                    Skill skill = plugin.getSkillRegistry().getOrNull(
                            NamespacedId.fromDefault(requirementNode.node("skill").getString("").toLowerCase(Locale.ROOT)));
                    int level = requirementNode.node("level").getInt();
                    nodes.add(new FabricSkillNode(plugin, skill, level, message));
                }
                case "permission" -> {
                    String permission = requirementNode.node("permission").getString();
                    nodes.add(new FabricPermissionNode(plugin, permission, message));
                }
                case "excluded_world" -> {
                    List<String> worldList;
                    try {
                        worldList = requirementNode.node("worlds").getList(String.class, new ArrayList<>());
                    } catch (SerializationException e) {
                        worldList = new ArrayList<>();
                    }
                    String[] worlds = worldList.toArray(new String[0]);
                    nodes.add(new FabricExcludedWorldNode(plugin, worlds, message));
                }
                case "stat" -> {
                    Stat stat = plugin.getStatManager().getEnabledStats().stream()
                            .filter(s -> s.getId().equals(NamespacedId.fromDefault(requirementNode.node("stat").getString("").toLowerCase(Locale.ROOT))))
                            .findFirst()
                            .orElse(null);
                    int value = requirementNode.node("value").getInt();
                    nodes.add(new FabricStatNode(plugin, stat, value, message));
                }
                case "world" -> {
                    String world = requirementNode.node("world").getString();
                    nodes.add(new FabricWorldNode(plugin, world, message));
                }
                case "biome" -> {
                    String biome = requirementNode.node("biome").getString();
                    nodes.add(new FabricBiomeNode(plugin, biome, message));
                }
                case "region" -> {
                    String region = requirementNode.node("region").getString();
                    nodes.add(new FabricRegionNode(plugin, region, message));
                }
                case "item" -> {
                    String item = requirementNode.node("item").getString();
                    nodes.add(new FabricItemNode(plugin, item, message));
                }
                case "enchantment" -> {
                    String enchantment = requirementNode.node("enchantment").getString();
                    String level = requirementNode.node("level").getString();
                    String[] levelStrings = level != null ? level.split("-") : new String[0];
                    int[] levels = Arrays.stream(levelStrings)
                            .mapToInt(s -> {
                                try {
                                    return Integer.parseInt(s.trim());
                                } catch (NumberFormatException e) {
                                    return -1;
                                }
                            })
                            .toArray();
                    int levelMin = levels.length >= 1 ? levels[0] : -1;
                    int levelMax = levels.length >= 2 ? levels[1] : levelMin;
                    Identifier enchId = enchantment != null ? Identifier.tryParse(enchantment.contains(":") ? enchantment : "minecraft:" + enchantment) : null;
                    nodes.add(new FabricEnchantmentNode(plugin, enchId, levelMin, levelMax, message));
                }
                default -> plugin.logger().warn("Unknown requirement type: " + type);
            }
        }
        return nodes;
    }

    private static @org.jetbrains.annotations.Nullable Identifier parseBlockMaterialId(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String upper = raw.toUpperCase(Locale.ROOT);
        Identifier id = Identifier.ofVanilla(upper.toLowerCase(Locale.ROOT));
        if (Registries.BLOCK.containsId(id)) {
            return id;
        }
        return Identifier.tryParse(raw.toLowerCase(Locale.ROOT));
    }

    /**
     * Parses Bukkit-style {@code MATERIAL_NAME} (e.g. {@code DIAMOND_SWORD}) to an item registry id.
     */
    private static @org.jetbrains.annotations.Nullable Identifier parseMaterialAsItemId(String raw) {
        String upper = raw.toUpperCase(Locale.ROOT);
        Identifier id = Identifier.ofVanilla(upper.toLowerCase(Locale.ROOT));
        if (Registries.ITEM.containsId(id)) {
            return id;
        }
        return Identifier.tryParse(raw.toLowerCase(Locale.ROOT));
    }

    public List<FabricBlockRequirement> getBlocks() {
        return blockRequirements;
    }

    public Set<FabricGlobalRequirement> getGlobalRequirementsType(ModifierType type) {
        Set<FabricGlobalRequirement> matched = Sets.newConcurrentHashSet();
        for (FabricGlobalRequirement requirement : globalRequirements) {
            if (requirement.type() == type) {
                matched.add(requirement);
            }
        }
        return matched;
    }

    public Map<UUID, Integer> getErrorMessageTimer() {
        return errorMessageTimer;
    }

    private void tickTimer() {
        TaskRunnable task = new TaskRunnable() {
            @Override
            public void run() {
                for (UUID id : errorMessageTimer.keySet()) {
                    int timer = errorMessageTimer.get(id);
                    if (timer != 0) {
                        errorMessageTimer.put(id, timer - 1);
                    }
                }
            }
        };
        plugin.getScheduler().timerSync(task, 0, 5 * 50L, TimeUnit.MILLISECONDS);
    }
}
