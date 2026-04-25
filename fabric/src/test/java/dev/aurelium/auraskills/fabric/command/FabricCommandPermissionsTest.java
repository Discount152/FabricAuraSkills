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
            FabricCommandPermissions.MANAABILITY_RESETCOOLDOWN,
            FabricCommandPermissions.ANTIAFK_LOGS,
            FabricCommandPermissions.XP_VIEW,
            FabricCommandPermissions.XP_ADD,
            FabricCommandPermissions.XP_SET,
            FabricCommandPermissions.XP_REMOVE,
            FabricCommandPermissions.SKILL_LEVEL_VIEW,
            FabricCommandPermissions.SKILL_SETLEVEL,
            FabricCommandPermissions.SKILL_RESET,
            FabricCommandPermissions.OPEN_MENU,
            FabricCommandPermissions.CLAIMITEMS,
            FabricCommandPermissions.SOURCES,
            FabricCommandPermissions.RESETHEALTH,
            FabricCommandPermissions.TRANSFER,
            FabricCommandPermissions.PROFILE,
            FabricCommandPermissions.BACKUP,
            FabricCommandPermissions.BACKUP_LOAD,
            FabricCommandPermissions.PRESET,
            FabricCommandPermissions.STORAGE,
            FabricCommandPermissions.TOP,
            FabricCommandPermissions.RANK,
            FabricCommandPermissions.MULTIPLIER,
            FabricCommandPermissions.ABTOGGLE,
            FabricCommandPermissions.LANG,
            FabricCommandPermissions.UPDATE_LEADERBOARDS,
            FabricCommandPermissions.MODIFIER,
            FabricCommandPermissions.ITEM_MODIFIER,
            FabricCommandPermissions.ITEM_IGNORE,
            FabricCommandPermissions.ITEM_REQUIREMENT,
            FabricCommandPermissions.ITEM_MULTIPLIER,
            FabricCommandPermissions.ITEM_REGISTER,
            FabricCommandPermissions.ITEM_GIVE,
            FabricCommandPermissions.ARMOR_MODIFIER,
            FabricCommandPermissions.ARMOR_REQUIREMENT,
            FabricCommandPermissions.ARMOR_MULTIPLIER
        };
        Set<String> seen = new HashSet<>();
        for (String s : all) {
            assertTrue(s.startsWith("auraskills.command."), s);
            assertTrue(seen.add(s), "duplicate node: " + s);
        }
        assertEquals(all.length, seen.size());
    }
}
