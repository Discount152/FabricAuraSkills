package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.source.XpSource;
import net.minecraft.util.Language;

import java.util.Locale;

/** Sprint 120: map internal XP source type ids to short, player-facing labels via {@code en_us} keys. */
public final class FabricSourceTypeLabels {

    private FabricSourceTypeLabels() {}

    public static String playerFacingTypeLine(XpSource src) {
        if (src == null || src.getType() == null) {
            return translateOr("auraskills.granite.source_type.unknown", "Unknown");
        }
        String simple = src.getType().getId().getSimpleName().toLowerCase(Locale.ROOT);
        String key = "auraskills.granite.source_type." + simple;
        String v = Language.getInstance().get(key);
        if (v != null && !v.isBlank() && !v.equals(key)) {
            return v;
        }
        return translateOr("auraskills.granite.source_type.other", simple);
    }

    private static String translateOr(String key, String fallback) {
        String v = Language.getInstance().get(key);
        if (v == null || v.isBlank() || v.equals(key)) {
            return fallback;
        }
        return v;
    }
}
