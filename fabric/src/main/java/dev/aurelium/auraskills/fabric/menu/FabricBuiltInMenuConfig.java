package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.util.file.FileUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.spongepowered.configurate.ConfigurationNode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the {@code granite} section of {@code menus/skills.yml} and {@code menus/stats.yml} (or embedded
 * defaults). **Sprint 32:** first YAML-backed path for built-in list menus; safe fallback on errors.
 */
public final class FabricBuiltInMenuConfig {

    public static final String GRANITE_KEY = "granite";

    private final Map<String, FabricMenuYamlSlice> byId = new HashMap<>();

    public void load(FabricAuraSkills plugin) {
        byId.put(
                FabricMenuKeys.SKILLS,
                loadOne(plugin, FabricMenuKeys.SKILLS, "menus/skills.yml", FabricMenuYamlSlice::defaultSkills));
        byId.put(
                FabricMenuKeys.STATS,
                loadOne(plugin, FabricMenuKeys.STATS, "menus/stats.yml", FabricMenuYamlSlice::defaultStats));
        plugin.logger().info("Built-in menu YAML: granite slices for " + String.join(", ", byId.keySet())
                + " (config: " + plugin.getPluginFolder() + "/menus/)");
    }

    public FabricMenuYamlSlice getSlice(String builtInMenuId, FabricMenuYamlSlice codeDefault) {
        return byId.getOrDefault(builtInMenuId, codeDefault);
    }

    public String resolveListOpenDetailKey(
            String builtInMenuId, FabricMenuYamlSlice codeDefault, String codeFallback) {
        FabricMenuYamlSlice s = getSlice(builtInMenuId, codeDefault);
        @Nullable
        String k = s.getFormatKey("list_open_detail", codeFallback);
        return k != null ? k : codeFallback;
    }

    private static FabricMenuYamlSlice loadOne(
            FabricAuraSkills plugin,
            String id,
            String path,
            java.util.function.Supplier<FabricMenuYamlSlice> builtInDefault) {
        FabricMenuYamlSlice def = builtInDefault.get();
        try {
            ConfigurationNode root = loadRoot(plugin, path);
            if (root == null) {
                plugin.logger().warn("Missing menu file " + path + " on disk and in JAR; using code defaults for '"
                        + id + "'.");
                return def;
            }
            return parse(root, def, plugin, path);
        } catch (Exception e) {
            plugin.logger().warn("Invalid menu YAML at " + path + " — using code defaults for '" + id + "': " + e.getMessage());
            e.printStackTrace();
            return def;
        }
    }

    @Nullable
    private static ConfigurationNode loadRoot(FabricAuraSkills plugin, String path) throws IOException {
        File f = new File(plugin.getPluginFolder(), path);
        if (f.exists()) {
            return FileUtil.loadYamlFile(f);
        }
        return FileUtil.loadEmbeddedYamlFileOrNull(path, plugin.getClass().getClassLoader());
    }

    static FabricMenuYamlSlice parse(
            ConfigurationNode root, FabricMenuYamlSlice def, @Nullable FabricAuraSkills plugin, String path) {
        ConfigurationNode g = root.node(GRANITE_KEY);
        if (g.virtual()) {
            return def;
        }
        boolean enabled = g.node("enabled").getBoolean(def.enabled());
        @Nullable
        String title = firstNonEmptyString(g.node("title_key"), g.node("title"));
        String titleKey = title != null && !title.isEmpty() ? title : def.titleTranslationKey();
        int rows = g.node("size", "rows").getInt(def.rows());
        int cols = g.node("size", "columns").getInt(def.columns());
        if (plugin != null
                && (rows != FabricMenuYamlSlice.EXPECTED_ROWS || cols != FabricMenuYamlSlice.EXPECTED_COLS)) {
            plugin.logger().warn("Menu " + path + " granite.size: expected "
                    + FabricMenuYamlSlice.EXPECTED_ROWS + "x" + FabricMenuYamlSlice.EXPECTED_COLS
                    + " for the current GUI; got " + rows + "x" + cols + " (ignored; 6x9 is fixed).");
        }
        Map<String, String> format = new HashMap<>(def.formatKeys());
        ConfigurationNode fnode = g.node("format");
        for (String key : List.of("list_open_detail")) {
            if (!fnode.node(key).virtual()) {
                String s = fnode.node(key).getString();
                if (s != null && !s.isEmpty()) {
                    format.put(key, s);
                }
            }
        }
        return new FabricMenuYamlSlice(enabled, titleKey, def.rows(), def.columns(), format);
    }

    @Nullable
    private static String firstNonEmptyString(ConfigurationNode a, ConfigurationNode b) {
        if (!a.virtual()) {
            String s = a.getString();
            if (s != null && !s.isEmpty()) {
                return s;
            }
        }
        if (!b.virtual()) {
            return b.getString();
        }
        return null;
    }
}
