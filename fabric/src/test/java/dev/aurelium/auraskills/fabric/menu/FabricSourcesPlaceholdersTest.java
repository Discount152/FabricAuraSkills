package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricSourcesPlaceholdersTest {

    @Test
    void expandsKnownKeys() {
        Map<String, String> m =
                Map.of(
                        "skill", "farming",
                        "skill_display", "Farming",
                        "source_name", "Wheat",
                        "source_xp", "2.5",
                        "source_type", "block",
                        "sort", "Ascending",
                        "page", "1",
                        "total_pages", "3");
        String out =
                FabricSourcesPlaceholders.expand(
                        "{skill_display} / {skill} — {source_name} ({source_xp}) {source_type} [{sort}] p{page}/{total_pages}",
                        m);
        assertEquals(
                "Farming / farming — Wheat (2.5) block [Ascending] p1/3",
                out);
        assertFalse(FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(out));
    }

    @Test
    void detectsUnresolvedPlaceholder() {
        String out = FabricSourcesPlaceholders.expand("Hello {unknown}", Map.of("skill", "x"));
        assertTrue(FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(out));
    }

    @Test
    void skillDisplayNotBrokenBySkillPrefix() {
        Map<String, String> m = Map.of("skill", "s", "skill_display", "Full Name");
        String out = FabricSourcesPlaceholders.expand("{skill_display} ({skill})", m);
        assertEquals("Full Name (s)", out);
        assertFalse(FabricSourcesPlaceholders.hasUnresolvedBraceSyntax(out));
    }
}
