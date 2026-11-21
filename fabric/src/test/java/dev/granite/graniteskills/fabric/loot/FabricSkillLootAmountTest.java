package dev.granite.graniteskills.fabric.loot;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricSkillLootAmountTest {

    @Test
    void equalBoundsStayConstant() {
        assertEquals(2, FabricSkillLoot.rollAmount(2, 2));
    }

    @Test
    void nonconstantRangeUsesValuesInsideTheBounds() {
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            int amount = FabricSkillLoot.rollAmount(1, 3);
            assertTrue(amount >= 1 && amount <= 3, "amount " + amount);
            seen.add(amount);
        }
        assertTrue(seen.contains(1));
        assertTrue(seen.contains(2));
        assertTrue(seen.contains(3));
    }
}
