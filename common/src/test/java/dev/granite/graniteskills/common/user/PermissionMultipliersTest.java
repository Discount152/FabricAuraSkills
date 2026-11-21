package dev.granite.graniteskills.common.user;

import dev.granite.graniteskills.api.skill.Skills;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PermissionMultipliersTest {

    @Test
    void unlistedPercentIsApplied() {
        assertEquals(0.07, PermissionMultipliers.contribution("graniteskills.multiplier.7", null));
    }

    @Test
    void decimalNegativeAndSkillSpecificValuesStack() {
        double sum = PermissionMultipliers.sum(List.of(
                "graniteskills.multiplier.7",
                "graniteskills.multiplier.-10",
                "graniteskills.multiplier.12.5",
                "graniteskills.multiplier.farming.15",
                "graniteskills.multiplier.mining.40",
                "graniteskills.not-a-multiplier.7"), Skills.FARMING);
        assertEquals(0.07 - 0.10 + 0.125 + 0.15, sum, 0.0000001);
    }

    @Test
    void malformedNumbersAreIgnored() {
        assertEquals(0.0, PermissionMultipliers.contribution("graniteskills.multiplier.7.", null));
        assertEquals(0.0, PermissionMultipliers.contribution("graniteskills.multiplier.", null));
        assertEquals(0.0, PermissionMultipliers.contribution("graniteskills.multiplier.-", null));
    }

    @Test
    void deniedNodeAbsentFromGrantListDoesNotContribute() {
        // Explicit denial is modeled as omission from the resolved grant list (Bukkit skips false).
        double sum = PermissionMultipliers.sum(List.of(
                "graniteskills.multiplier.7",
                "graniteskills.multiplier.farming.15"), Skills.FARMING);
        assertEquals(0.07 + 0.15, sum, 0.0000001);
        assertEquals(0.07, PermissionMultipliers.sum(List.of(
                "graniteskills.multiplier.7",
                "graniteskills.multiplier.farming.15"), null), 0.0000001);
    }

    @Test
    void namespacedSkillFormMatchesPlainSkillForm() {
        assertEquals(0.4, PermissionMultipliers.contribution(
                "graniteskills.multiplier.graniteskills/mining.40", Skills.MINING), 0.0000001);
        assertEquals(0.4, PermissionMultipliers.contribution(
                "graniteskills.multiplier.mining.40", Skills.MINING), 0.0000001);
        assertEquals(0.0, PermissionMultipliers.contribution(
                "graniteskills.multiplier.mining.40", Skills.FARMING), 0.0000001);
    }
}
