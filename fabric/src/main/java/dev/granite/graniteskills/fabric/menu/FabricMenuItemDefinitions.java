package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Locale;

/**
 * Bounded {@code granite.items} (Sprint 36+): list row material, main panel, back control — not full Slate
 * {@code ItemBuilder} / NBT. Strings are stored normalized (e.g. {@code minecraft:book}); invalid ids fall back
 * with a log.
 */
public final class FabricMenuItemDefinitions {

    public static final String DEFAULT_LIST_ITEM_ID = "minecraft:book";
    public static final String DEFAULT_MAIN_ITEM_ID = "minecraft:book";
    public static final String DEFAULT_BACK_ITEM_ID = "minecraft:iron_door";

    public record Resolved(String listItemId, String mainItemId, String backItemId) {
        public static Resolved fromSlice(FabricMenuYamlSlice s) {
            return new Resolved(s.listItemId(), s.mainItemId(), s.backItemId());
        }
    }

    private FabricMenuItemDefinitions() {
    }

    public static Resolved parseGranite(
            ConfigurationNode g, FabricMenuYamlSlice def, @Nullable FabricGraniteSkills plugin, String path) {
        ConfigurationNode n = g.node("items");
        if (n.virtual()) {
            return Resolved.fromSlice(def);
        }
        return new Resolved(
                normalizeId(
                        n.node("list").getString(),
                        def.listItemId(),
                        "list",
                        path,
                        plugin),
                normalizeId(
                        n.node("main").getString(),
                        def.mainItemId(),
                        "main",
                        path,
                        plugin),
                normalizeId(
                        n.node("back").getString(),
                        def.backItemId(),
                        "back",
                        path,
                        plugin));
    }

    static String normalizeId(
            @Nullable String raw,
            String def,
            String yamlKey,
            String path,
            @Nullable FabricGraniteSkills plugin) {
        if (raw == null) {
            return def;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return def;
        }
        Identifier id = tryParseId(t, path, yamlKey, plugin);
        if (id == null) {
            return def;
        }
        return id.toString();
    }

    @Nullable
    private static Identifier tryParseId(
            String t, String path, String yamlKey, @Nullable FabricGraniteSkills plugin) {
        try {
            if (t.contains(":")) {
                return Identifier.parse(t);
            }
            return Identifier.withDefaultNamespace(t.toLowerCase(Locale.ROOT));
        } catch (Exception e) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.items."
                            + yamlKey
                            + " invalid id '"
                            + t
                            + "'; using default.");
            return null;
        }
    }

    /**
     * Resolves a stored (normalized) id; logs once per failure when {@code plugin} is non-null.
     */
    public static Item resolveItem(
            String storedId,
            Item fallback,
            @Nullable FabricGraniteSkills plugin,
            String path,
            String yamlKey) {
        if (storedId == null || storedId.isEmpty()) {
            return fallback;
        }
        try {
            Identifier id = storedId.contains(":")
                    ? Identifier.parse(storedId)
                    : Identifier.withDefaultNamespace(storedId.toLowerCase(Locale.ROOT));
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                warn(
                        plugin,
                        "Menu "
                                + path
                                + " granite.items."
                                + yamlKey
                                + " unknown or unloaded item '"
                                + storedId
                                + "'; using "
                                + BuiltInRegistries.ITEM.getKey(fallback)
                                + ".");
                return fallback;
            }
            Item it = BuiltInRegistries.ITEM.getValue(id);
            if (it == null || it == Items.AIR) {
                return fallback;
            }
            return it;
        } catch (Exception e) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.items."
                            + yamlKey
                            + " could not resolve '"
                            + storedId
                            + "'; using "
                            + BuiltInRegistries.ITEM.getKey(fallback)
                            + ".");
            return fallback;
        }
    }

    public static ItemStack createItemStack(
            String storedId,
            Item fallback,
            @Nullable FabricGraniteSkills plugin,
            String path,
            String yamlKey) {
        return new ItemStack(resolveItem(storedId, fallback, plugin, path, yamlKey), 1);
    }

    private static void warn(@Nullable FabricGraniteSkills plugin, String msg) {
        if (plugin != null) {
            plugin.logger().warn(msg);
        }
    }
}
