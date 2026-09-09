package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Fabric /sources row titles when AuraSkills returns !!REMOVE!! for block rows (Sprint 127). */
public final class FabricXpSourceMenuDisplay {

    private FabricXpSourceMenuDisplay() {}

    public static String effectiveRowTitle(FabricAuraSkills plugin, XpSource source, Locale locale) {
        String raw = source.getDisplayName(locale);
        if (!(isBlankish(raw) || isHiddenSentinel(raw))) {
            return raw;
        }
        if (valuesDisplayName(source) != null) {
            return valuesDisplayName(source);
        }
        if (source instanceof BlockXpSource bx) {
            if (bx.getBlocks() != null && bx.getBlocks().length > 0) {
                return blockLabel(bx.getBlock());
            }
        }
        if (source instanceof DamageXpSource && source.getType() != null) {
            String path =
                    "sources."
                            + source.getType().toString().toLowerCase(Locale.ROOT)
                            + "."
                            + source.getId().getKey().toLowerCase(Locale.ROOT);
            String m = plugin.getMsg(MessageKey.of(path), locale);
            if (!m.equals(path)) {
                return m;
            }
        }
        if (source instanceof EntityXpSource e && e.getEntity() != null) {
            return "<lang:entity.minecraft." + e.getEntity() + ">";
        }
        return humanizeKey(source.getId().getKey());
    }

    private static @Nullable String valuesDisplayName(XpSource source) {
        return source.getValues() != null ? source.getValues().getDisplayName() : null;
    }

    public static boolean isHiddenSentinel(@Nullable String displayName) {
        if (displayName == null) {
            return false;
        }
        return "!!REMOVE!!".equals(displayName) || "!REMOVE!!".equals(displayName);
    }

    public static boolean usedFallbackFabricTitle(FabricAuraSkills plugin, XpSource source, Locale locale) {
        String raw = source.getDisplayName(locale);
        if (!isBlankish(raw) && !isHiddenSentinel(raw)) {
            return false;
        }
        String effective = effectiveRowTitle(plugin, source, locale);
        return raw == null || effective == null || !effective.equals(raw);
    }

    private static boolean isBlankish(@Nullable String s) {
        return s == null || s.isBlank();
    }

    public static String blockLabel(String blockIdStr) {
        try {
            Identifier id =
                    blockIdStr.contains(":")
                            ? Identifier.of(blockIdStr)
                            : Identifier.ofVanilla(blockIdStr);
            Block block = Registries.BLOCK.get(id);
            String itemKey = block.asItem().getTranslationKey();
            return Text.translatable(itemKey).getString();
        } catch (Throwable ignored) {
            return prettifyIdPath(blockIdStr);
        }
    }

    static String prettifyIdPath(String blockOrNamespacePath) {
        String path = blockNamespacePathSansColon(blockOrNamespacePath);
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.isEmpty() ? blockOrNamespacePath : sb.toString();
    }

    private static String blockNamespacePathSansColon(String blockOrNamespacePath) {
        try {
            return (blockOrNamespacePath.contains(":")
                            ? Identifier.of(blockOrNamespacePath)
                            : Identifier.ofVanilla(blockOrNamespacePath))
                    .getPath();
        } catch (Throwable t) {
            return blockOrNamespacePath;
        }
    }

    private static String humanizeKey(String snakeKey) {
        return prettifyIdPath(snakeKey.replace('.', '_'));
    }
}