package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.fabric.menu.FabricMenuKeys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandBuiltInMenuIdTest {

    @Test
    void resolve_acceptsSkillsAndStatsCaseInsensitive() {
        assertEquals(FabricMenuKeys.SKILLS, FabricCommandBuiltInMenuId.resolveForOpenCommand("Skills").get());
        assertEquals(FabricMenuKeys.STATS, FabricCommandBuiltInMenuId.resolveForOpenCommand("STATS").get());
    }

    @Test
    void resolve_rejectsUnknown() {
        assertTrue(FabricCommandBuiltInMenuId.resolveForOpenCommand("not_a_menu").isEmpty());
    }

    @Test
    void resolve_acceptsContextMenus() {
        assertEquals(
                FabricMenuKeys.LEVEL_PROGRESSION,
                FabricCommandBuiltInMenuId.resolveForOpenCommand("level_progression").get());
        assertEquals(
                FabricMenuKeys.STAT_INFO, FabricCommandBuiltInMenuId.resolveForOpenCommand("stat_info").get());
        assertEquals(
                FabricMenuKeys.SOURCES, FabricCommandBuiltInMenuId.resolveForOpenCommand("sources").get());
        assertEquals(
                FabricMenuKeys.UNCLAIMED_ITEMS,
                FabricCommandBuiltInMenuId.resolveForOpenCommand("unclaimed_items").get());
    }

    @Test
    void requiresContext_isTrueForSkillAndStatContextMenus() {
        assertTrue(
                FabricCommandBuiltInMenuId.requiresContext(FabricMenuKeys.LEVEL_PROGRESSION));
        assertTrue(FabricCommandBuiltInMenuId.requiresContext(FabricMenuKeys.STAT_INFO));
        assertTrue(FabricCommandBuiltInMenuId.requiresContext(FabricMenuKeys.SOURCES));
        assertFalse(FabricCommandBuiltInMenuId.requiresContext(FabricMenuKeys.SKILLS));
        assertFalse(
                FabricCommandBuiltInMenuId.requiresContext(FabricMenuKeys.UNCLAIMED_ITEMS));
    }
}
