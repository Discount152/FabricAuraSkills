package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricStatInfoPlaceholdersTest {

    @Test
    void expandsKnownKeysInStableOrder() {
        Map<String, String> m = Map.of(
                "stat", "strength",
                "stat_display", "Strength",
                "stat_description", "Melee damage",
                "stat_value", "12.5");
        String out =
                FabricStatInfoPlaceholders.expand(
                        "{stat_display} / {stat} / {stat_value} — {stat_description}", m);
        assertEquals("Strength / strength / 12.5 — Melee damage", out);
        assertFalse(FabricStatInfoPlaceholders.hasUnresolvedBraceSyntax(out));
    }

    @Test
    void detectsUnresolvedPlaceholder() {
        String out = FabricStatInfoPlaceholders.expand("Hello {unknown}", Map.of("stat", "x"));
        assertTrue(FabricStatInfoPlaceholders.hasUnresolvedBraceSyntax(out));
    }

    @Test
    void emptyDescriptionKeyStillValid() {
        String out = FabricStatInfoPlaceholders.expand("{stat_description}", Map.of("stat", "s"));
        assertEquals("", out);
        assertFalse(FabricStatInfoPlaceholders.hasUnresolvedBraceSyntax(out));
    }
}
