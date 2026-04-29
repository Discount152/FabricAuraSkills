package dev.aurelium.auraskills.fabric.text;

import dev.aurelium.auraskills.common.util.text.TextUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the MiniMessage → legacy string pipeline (same as Bukkit {@code TextFormatter}).
 * Prevents raw {@code <green>}-style tags from surviving into player-visible text when serialized.
 */
class FabricTextFormatterTest {

    @Test
    void toComponent_roundTrip_omitsRawGreenTag() {
        String in = "<green>Hello";
        var c = FabricTextFormatter.toComponent(in);
        String out = FabricTextFormatter.toString(c);
        assertFalse(out.contains("<"), "legacy output should not contain angle-bracket tags: " + out);
    }

    @Test
    void toComponent_nestedBold_omitsRawBoldTags() {
        String in = "<bold>Fighting</bold>";
        var c = FabricTextFormatter.toComponent(in);
        String out = FabricTextFormatter.toString(c);
        assertFalse(out.toLowerCase().contains("<bold>"), "output: " + out);
    }

    @Test
    void toComponent_actionBarStyle_redAndAqua_noRawTags() {
        String in = "<red>20/20<aqua>20/20 Mana";
        var c = FabricTextFormatter.toComponent(in);
        String out = FabricTextFormatter.toString(c);
        assertFalse(out.contains("<red>"), "output: " + out);
        assertFalse(out.contains("<aqua>"), "output: " + out);
    }

    @Test
    void toComponent_plainText_unchanged() {
        var c = FabricTextFormatter.toComponent("No tags here");
        String out = FabricTextFormatter.toString(c);
        assertTrue(out.contains("No tags") || out.equals("No tags here"), "output: " + out);
    }

    @Test
    void legacyAmpersandCodes_roundTrip_noBareAmpersandInLegacyString() {
        var c = FabricTextFormatter.toComponent("&eBack to Skills");
        String out = FabricTextFormatter.toString(c);
        assertFalse(out.contains("&e"), "legacy serializers should use section codes, not &: " + out);
    }

    @Test
    void legacySectionUnderline_mapsToUnderlinedNotUnderlineToken() {
        String in = "§7§nRelated traits";
        String mapped = FabricTextFormatter.legacyCodesToMiniMessage(TextUtil.replace(in, "§", "&"));
        assertFalse(mapped.contains("<underline>"), "mapped: " + mapped);
        assertTrue(mapped.contains("<underlined>"), "mapped: " + mapped);
        var c = FabricTextFormatter.toComponent(in);
        String legacy = FabricTextFormatter.toString(c);
        assertFalse(legacy.contains("<"), "legacy round-trip should not preserve angle-bracket tags: " + legacy);
    }

    @Test
    void legacyAmpersandUnderline_usesUnderlinedTag() {
        String mapped = FabricTextFormatter.legacyCodesToMiniMessage("&7&nRelated traits");
        assertFalse(mapped.contains("<underline>"), mapped);
        assertTrue(mapped.contains("<underlined>"), mapped);
    }
}
