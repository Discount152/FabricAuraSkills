package dev.granitelib.fabric.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GranitePlaceholderResultTest {

    @Test
    void knownPlaceholderIsReplacedAndUnknownIsReported() {
        GranitePlaceholderResult result = GraniteTemplatePlaceholders.replace(
                "Skill {skill} {missing}", Map.of("skill", "Farming"));

        assertEquals("Skill Farming {missing}", result.text());
        assertEquals(java.util.List.of("missing"), result.unknown());
        assertTrue(result.unknown().stream().noneMatch("skill"::equals));
    }
}
