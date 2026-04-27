package dev.aurelium.auraskills.fabric.menu;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Sprint 111: chat-oriented multiline formatting for {@code /auraskills templates status} (counts + per-menu state,
 * sources, disk failure reasons, fallbacks).
 */
public final class FabricGraniteMenuTemplateStatusFormatter {

    private FabricGraniteMenuTemplateStatusFormatter() {}

    public record SummaryCounts(int diskLoaded, int bundledEffective, int failed) {}

    /**
     * Registry summary counts: {@code disk} = {@link FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState#DISK_LOADED};
     * {@code bundled} = {@link FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState#BUNDLED_LOADED} + {@link
     * FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState#DISK_INVALID_BUNDLED_LOADED}; {@code failed} = {@link
     * FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState#BUNDLED_MISSING_OR_INVALID}.
     */
    public static SummaryCounts summarize(Map<String, FabricGraniteMenuTemplateRegistry.RegistryEntry> snapshot) {
        int disk = 0;
        int bundled = 0;
        int failed = 0;
        for (FabricGraniteMenuTemplateRegistry.RegistryEntry e : snapshot.values()) {
            switch (e.state()) {
                case DISK_LOADED -> disk++;
                case BUNDLED_LOADED, DISK_INVALID_BUNDLED_LOADED -> bundled++;
                case BUNDLED_MISSING_OR_INVALID -> failed++;
            }
        }
        return new SummaryCounts(disk, bundled, failed);
    }

    public static String format(
            Map<String, FabricGraniteMenuTemplateRegistry.RegistryEntry> snapshot,
            List<FabricGraniteMenuTemplateBindings.Binding> order) {
        SummaryCounts c = summarize(snapshot);
        StringBuilder sb = new StringBuilder();
        sb.append("Granite menu templates: disk ")
                .append(c.diskLoaded())
                .append(", bundled ")
                .append(c.bundledEffective())
                .append(", failed ")
                .append(c.failed())
                .append("\n");
        for (FabricGraniteMenuTemplateBindings.Binding b : order) {
            FabricGraniteMenuTemplateRegistry.RegistryEntry e = snapshot.get(b.menuId());
            if (e == null) {
                sb.append("- ")
                        .append(b.menuId())
                        .append(": ")
                        .append(FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID)
                        .append("\n");
                sb.append("  reason: no registry entry\n");
                sb.append("  fallback: built-in Java menu\n");
                continue;
            }
            appendEntry(sb, e);
        }
        sb.append("\nRun /auraskills reload to refresh after editing disk YAML.");
        return sb.toString().trim();
    }

    private static void append(StringBuilder sb, String indent, String key, String value) {
        sb.append(indent).append(key).append(": ").append(value).append("\n");
    }

    private static void appendEntry(StringBuilder sb, FabricGraniteMenuTemplateRegistry.RegistryEntry e) {
        sb.append("- ").append(e.menuId()).append(": ").append(e.state()).append("\n");
        String ind = "  ";
        switch (e.state()) {
            case DISK_LOADED -> {
                append(sb, ind, "source", e.primarySourceDisplay());
            }
            case BUNDLED_LOADED -> {
                append(sb, ind, "source", e.primarySourceDisplay());
            }
            case DISK_INVALID_BUNDLED_LOADED -> {
                if (e.diskSourceDisplay() != null) {
                    append(sb, ind, "disk", e.diskSourceDisplay());
                }
                appendOptional(sb, ind, "reason", e.reason());
                appendOptional(sb, ind, "fallback", e.fallbackDisplay());
            }
            case BUNDLED_MISSING_OR_INVALID -> {
                append(sb, ind, "reason", e.reason() != null ? e.reason() : "bundled resource missing or invalid");
                append(sb, ind, "fallback", e.fallbackDisplay() != null ? e.fallbackDisplay() : "built-in Java menu");
                if (e.diskSourceDisplay() != null) {
                    append(sb, ind, "disk", e.diskSourceDisplay());
                }
            }
        }
    }

    private static void appendOptional(StringBuilder sb, String ind, String key, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            append(sb, ind, key, value);
        }
    }
}
