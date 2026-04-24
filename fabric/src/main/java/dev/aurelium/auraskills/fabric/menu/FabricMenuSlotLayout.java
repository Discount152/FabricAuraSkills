package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * 6×9 top-inventory (54 slots) layout numbers from {@code granite.layout}. Invalid combinations fall back
 * to the slice’s code defaults. **Sprint 34+**
 */
public final class FabricMenuSlotLayout {

    /** Top (container) grid: 0–53. */
    public static final int TOP_SLOTS = 54;
    public static final int DEFAULT_LIST_START = 0;
    public static final int DEFAULT_LIST_END_EX = 54;
    public static final int DEFAULT_MAIN_SLOT = 4;
    public static final int DEFAULT_BACK_SLOT = 49;

    private FabricMenuSlotLayout() {
    }

    public record Resolved(int listStartSlot, int listEndSlotExclusive, int mainSlot, int backSlot) {}

    public static Resolved parseGranite(
            ConfigurationNode g,
            FabricMenuYamlSlice def,
            @Nullable FabricAuraSkills plugin,
            String path) {
        ConfigurationNode l = g.node("layout");
        if (l.virtual()) {
            return fromSlice(def);
        }
        int ls = l.node("list_start_slot").getInt(def.listStartSlot());
        int le = l.node("list_end_slot_exclusive").getInt(def.listEndSlotExclusive());
        int m = l.node("main_slot").getInt(def.mainSlot());
        int b = l.node("back_slot").getInt(def.backSlot());
        return validate(ls, le, m, b, def, plugin, path);
    }

    static Resolved fromSlice(FabricMenuYamlSlice s) {
        return new Resolved(
                s.listStartSlot(), s.listEndSlotExclusive(), s.mainSlot(), s.backSlot());
    }

    static Resolved validate(
            int listStart,
            int listEndEx,
            int mainSlot,
            int backSlot,
            FabricMenuYamlSlice def,
            @Nullable FabricAuraSkills plugin,
            String path) {
        int ls = clampIncl(listStart, 0, TOP_SLOTS - 1, "list_start_slot", def.listStartSlot(), path, plugin);
        int le = clampIncl(
                listEndEx, 1, TOP_SLOTS, "list_end_slot_exclusive", def.listEndSlotExclusive(), path, plugin);
        if (le <= ls) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.layout: need list_end_slot_exclusive > list_start_slot (got "
                            + ls
                            + "–"
                            + le
                            + "); using default list range.");
            ls = def.listStartSlot();
            le = def.listEndSlotExclusive();
        }
        int m = clampIncl(mainSlot, 0, TOP_SLOTS - 1, "main_slot", def.mainSlot(), path, plugin);
        int b = clampIncl(backSlot, 0, TOP_SLOTS - 1, "back_slot", def.backSlot(), path, plugin);
        if (m == b) {
            warn(
                    plugin,
                    "Menu " + path + " granite.layout: main_slot == back_slot; using default main/back.");
            m = def.mainSlot();
            b = def.backSlot();
        }
        return new Resolved(ls, le, m, b);
    }

    private static int clampIncl(
            int v,
            int min,
            int max,
            String name,
            int fallback,
            String path,
            @Nullable FabricAuraSkills plugin) {
        if (v < min || v > max) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.layout."
                            + name
                            + "="
                            + v
                            + " not in ["
                            + min
                            + "–"
                            + max
                            + "]; using "
                            + fallback);
            return fallback;
        }
        return v;
    }

    private static void warn(@Nullable FabricAuraSkills plugin, String msg) {
        if (plugin != null) {
            plugin.logger().warn(msg);
        }
    }
}
