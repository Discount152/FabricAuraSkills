package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.common.skill.LoadedSkill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Shared {@code /sources} list path: version-valid filter + the same sort as chat output (**Sprint
 * 57**), reused by the Fabric sources menu (**Sprint 69**).
 */
public final class FabricSourcesListSupport {

    private FabricSourcesListSupport() {
    }

    /**
     * Copy of the skill’s sources, with invalid-version entries removed, sorted by {@code sort} for
     * {@code lo}. May be empty if there were none or all were pruned.
     */
    public static List<XpSource> buildSorted(LoadedSkill loaded, FabricSourceSortType sort, Locale lo) {
        List<XpSource> list = new ArrayList<>(loaded.sources());
        if (list.isEmpty()) {
            return list;
        }
        list.removeIf(s -> !s.isVersionValid());
        Collections.sort(list, sort.comparator(lo));
        return list;
    }
}
