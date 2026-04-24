package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandPermissionsTest {

    @Test
    void permissionConstants_areDocumentedUniques() {
        String[] all = {
            FabricCommandPermissions.VERSION,
            FabricCommandPermissions.HELP,
            FabricCommandPermissions.SKILLS,
            FabricCommandPermissions.STATS,
            FabricCommandPermissions.RELOAD,
            FabricCommandPermissions.SAVE,
            FabricCommandPermissions.MANA,
            FabricCommandPermissions.MANA_OTHER,
            FabricCommandPermissions.MANA_ADD,
            FabricCommandPermissions.MANA_SET,
            FabricCommandPermissions.MANA_REMOVE,
            FabricCommandPermissions.XP_VIEW,
            FabricCommandPermissions.XP_ADD,
            FabricCommandPermissions.XP_SET,
            FabricCommandPermissions.XP_REMOVE,
            FabricCommandPermissions.SKILL_LEVEL_VIEW,
            FabricCommandPermissions.SKILL_SETLEVEL,
            FabricCommandPermissions.SKILL_RESET,
            FabricCommandPermissions.OPEN_MENU,
            FabricCommandPermissions.TOP,
            FabricCommandPermissions.RANK,
            FabricCommandPermissions.MULTIPLIER,
            FabricCommandPermissions.ABTOGGLE,
            FabricCommandPermissions.LANG
        };
        Set<String> seen = new HashSet<>();
        for (String s : all) {
            assertTrue(s.startsWith("auraskills.command."), s);
            assertTrue(seen.add(s), "duplicate node: " + s);
        }
        assertEquals(all.length, seen.size());
    }
}
