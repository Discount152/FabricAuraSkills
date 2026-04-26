package dev.granitelib.fabric.template;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteTemplatePlaceholdersTest {

    @Test
    void expandKeysUsesOrderAndEmptyDefault() {
        List<String> order = List.of("stat_description", "stat_display", "stat_value", "stat");
        Map<String, String> m = Map.of("stat", "s");
        String out = GraniteTemplatePlaceholders.expandKeys("{stat_description}", m, order);
        assertEquals("", out);
        assertFalse(GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(out));
    }

    @Test
    void expandKeysLongestBeforePrefix() {
        List<String> order = List.of("skill_display", "skill");
        Map<String, String> m = Map.of("skill", "s", "skill_display", "Full");
        String out = GraniteTemplatePlaceholders.expandKeys("{skill_display} ({skill})", m, order);
        assertEquals("Full (s)", out);
    }

    @Test
    void expandAllKeysLongestFirst() {
        Map<String, String> m = Map.of("a", "1", "ab", "2");
        String out = GraniteTemplatePlaceholders.expandAllKeysLongestFirst("{ab}-{a}", m);
        assertEquals("2-1", out);
    }

    @Test
    void detectsUnresolved() {
        String out = GraniteTemplatePlaceholders.expandKeys("x {z}", Map.of("a", "b"), List.of("a"));
        assertEquals("x {z}", out);
        assertTrue(GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(out));
    }
}
