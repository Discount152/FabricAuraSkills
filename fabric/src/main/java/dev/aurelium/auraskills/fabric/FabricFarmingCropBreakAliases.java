package dev.aurelium.auraskills.fabric;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps block registry paths that represent the same crop for Farming BREAK sources when YAML uses
 * seed/item-style names (for example wheat_seeds) but the broken block is the crop block (for example wheat).
 */
public final class FabricFarmingCropBreakAliases {

    /** Broken or configured block path (normalized) to canonical group id (normalized). */
    private static final Map<String, String> PATH_TO_GROUP = new HashMap<>();

    static {
        link("wheat", "wheat_seeds");
        link("beetroots", "beetroot_seeds", "beetroot");
        link("carrots", "carrot");
        link("potatoes", "potato");
        link("melon", "melon_stem", "attached_melon_stem");
        link("pumpkin", "pumpkin_stem", "attached_pumpkin_stem");
        link("torchflower", "torchflower_crop");
        link("pitcher_crop", "pitcher_pod");
        link("cocoa", "cocoa_beans");
        link("sweet_berry_bush", "sweet_berries");
    }

    private FabricFarmingCropBreakAliases() {}

    private static void link(String groupId, String... otherPaths) {
        String g = norm(groupId);
        PATH_TO_GROUP.put(g, g);
        for (String p : otherPaths) {
            PATH_TO_GROUP.put(norm(p), g);
        }
    }

    private static String norm(String path) {
        if (path == null) {
            return "";
        }
        String p = path.toLowerCase(Locale.ROOT).trim();
        int colon = p.indexOf(':');
        if (colon >= 0 && colon + 1 < p.length()) {
            p = p.substring(colon + 1);
        }
        return p;
    }

    /**
     * true when broken block path matches configured farming block string, including seed/crop/stem aliases.
     */
    public static boolean matchesFarmingConfiguredBlock(String brokenBlockPath, String configuredBlockName) {
        if (brokenBlockPath == null || configuredBlockName == null) {
            return false;
        }
        String a = norm(brokenBlockPath);
        String b = norm(configuredBlockName);
        if (a.equals(b)) {
            return true;
        }
        String ga = PATH_TO_GROUP.getOrDefault(a, a);
        String gb = PATH_TO_GROUP.getOrDefault(b, b);
        return ga.equals(gb);
    }
}