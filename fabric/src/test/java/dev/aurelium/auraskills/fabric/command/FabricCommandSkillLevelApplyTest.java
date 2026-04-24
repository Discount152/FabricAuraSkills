package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricCommandSkillLevelApplyTest {

    @Test
    void clampRemoveTarget_floorsAtStartLevel() {
        assertEquals(5, FabricCommandSkillLevelApply.clampRemoveTarget(1, 10, 5));
        assertEquals(1, FabricCommandSkillLevelApply.clampRemoveTarget(1, 5, 100));
    }
}
