package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantingXpAwardTest {

    @Test
    void enchantingXpScalesWithLevelsAndAwardsNothingBelowThatCost() {
        assertEquals(30.0, EnchantingXpAward.xp(30.0, EnchantingXpAward.ENCHANT_LEVEL, 1, 0));
        assertEquals(120.0, EnchantingXpAward.xp(30.0, EnchantingXpAward.ENCHANT_LEVEL, 4, 0));
        assertEquals(0.0, EnchantingXpAward.xp(30.0, EnchantingXpAward.ENCHANT_LEVEL, 0, 5));

        assertEquals(90.0, EnchantingXpAward.xp(30.0, EnchantingXpAward.EXP_REQUIREMENT, 0, 3));
        assertEquals(0.0, EnchantingXpAward.xp(30.0, EnchantingXpAward.EXP_REQUIREMENT, 4, 0));
        assertEquals(60.0, EnchantingXpAward.xp(30.0, EnchantingXpAward.EXP_REQUIREMENT_TYPO, 0, 2));
    }
}
