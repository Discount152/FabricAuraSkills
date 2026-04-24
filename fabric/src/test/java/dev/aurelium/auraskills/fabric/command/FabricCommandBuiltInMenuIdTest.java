package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandBuiltInMenuIdTest {

    @Test
    void resolve_acceptsSkillsAndStatsCaseInsensitive() {
        assertEquals(FabricMenuKeys.SKILLS, FabricCommandBuiltInMenuId.resolveForOpenCommand("Skills").get());
        assertEquals(FabricMenuKeys.STATS, FabricCommandBuiltInMenuId.resolveForOpenCommand("STATS").get());
    }

    @Test
    void resolve_rejectsUnknown() {
        assertTrue(FabricCommandBuiltInMenuId.resolveForOpenCommand("level_progression").isEmpty());
    }
}
