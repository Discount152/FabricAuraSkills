package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandPlayerTargetTest {

    @Test
    void filterPrefix_emptyPrefix_returnsAll() {
        assertEquals(
                List.of("a", "b"),
                FabricCommandPlayerTarget.filterPrefix(List.of("a", "b"), ""));
    }

    @Test
    void filterPrefix_matchesCaseInsensitive() {
        List<String> r = FabricCommandPlayerTarget.filterPrefix(List.of("Steve", "alex"), "st");
        assertTrue(r.contains("Steve"));
        assertEquals(1, r.size());
    }
}
