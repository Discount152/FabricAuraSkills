package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.skill.Skills;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricSkillShortcutCommandsTest {

    @Test
    void roots_match_reference_skill_count() {
        assertEquals(15, Skills.values().length);
        assertEquals(15, FabricSkillShortcutCommands.ROOTS.size());
    }

    @Test
    void roots_literalsAreUnique() {
        Set<String> seen = new HashSet<>();
        for (var r : FabricSkillShortcutCommands.ROOTS) {
            assertTrue(seen.add(r.literal()), "duplicate: " + r.literal());
        }
    }
}
