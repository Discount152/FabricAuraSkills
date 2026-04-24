package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * {@code granite.paging} for list menus: prev/next top slots (Sprint 37+). {@link FabricListMenuPaging} reserves
 * the last row; slots must not overlap the list content area (warn + defaults).
 */
public final class FabricMenuPagingConfig {

    public record Resolved(int prevSlot, int nextSlot) {
        public static Resolved fromSlice(FabricMenuYamlSlice s) {
            return new Resolved(s.pagingPrevSlot(), s.pagingNextSlot());
        }
    }

    private FabricMenuPagingConfig() {
    }

    public static Resolved parse(
            ConfigurationNode g,
            FabricMenuYamlSlice def,
            int listStart,
            int listEndExclusive,
            @Nullable FabricAuraSkills plugin,
            String path) {
        ConfigurationNode p = g.node("paging");
        if (p.virtual()) {
            return Resolved.fromSlice(def);
        }
        int pr = p.node("prev_slot").getInt(def.pagingPrevSlot());
        int ne = p.node("next_slot").getInt(def.pagingNextSlot());
        return validate(pr, ne, def, listStart, listEndExclusive, plugin, path);
    }

    static Resolved validate(
            int prev,
            int next,
            FabricMenuYamlSlice def,
            int listStart,
            int listEndExclusive,
            @Nullable FabricAuraSkills plugin,
            String path) {
        FabricListMenuPaging.ListContentRegion r = FabricListMenuPaging.listContentRegion(listStart, listEndExclusive);
        int ps = clampPagingSlot(prev, def.pagingPrevSlot(), "prev_slot", path, plugin);
        int ns = clampPagingSlot(next, def.pagingNextSlot(), "next_slot", path, plugin);
        // Overlap: nav slot in [r.start, r.end)
        if (ps >= r.startInclusive() && ps < r.endExclusive()) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.paging.prev_slot "
                            + ps
                            + " overlaps list content; using "
                            + def.pagingPrevSlot()
                            + ".");
            ps = def.pagingPrevSlot();
        }
        if (ns >= r.startInclusive() && ns < r.endExclusive()) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.paging.next_slot "
                            + ns
                            + " overlaps list content; using "
                            + def.pagingNextSlot()
                            + ".");
            ns = def.pagingNextSlot();
        }
        if (ps == ns) {
            warn(
                    plugin,
                    "Menu " + path + " granite.paging: prev_slot == next_slot; using default prev/next.");
            return new Resolved(def.pagingPrevSlot(), def.pagingNextSlot());
        }
        return new Resolved(ps, ns);
    }

    private static int clampPagingSlot(
            int v, int def, String key, String path, @Nullable FabricAuraSkills plugin) {
        if (v < 0 || v >= FabricMenuSlotLayout.TOP_SLOTS) {
            warn(
                    plugin,
                    "Menu "
                            + path
                            + " granite.paging."
                            + key
                            + "="
                            + v
                            + " not in [0–"
                            + (FabricMenuSlotLayout.TOP_SLOTS - 1)
                            + "]; using "
                            + def
                            + ".");
            return def;
        }
        return v;
    }

    private static void warn(@Nullable FabricAuraSkills plugin, String msg) {
        if (plugin != null) {
            plugin.logger().warn(msg);
        }
    }
}
