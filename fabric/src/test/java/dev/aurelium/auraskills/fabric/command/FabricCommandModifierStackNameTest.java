package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricCommandModifierStackNameTest {

    @Test
    void getStackedName_startsAtTwoWhenBaseExists() {
        assertEquals(
                "myMod(2)",
                FabricCommandModifierStackName.getStackedName(Set.of("myMod"), "myMod"));
    }

    @Test
    void getStackedName_picksNextAfterHighestNumberedSuffix() {
        assertEquals(
                "myMod(4)",
                FabricCommandModifierStackName.getStackedName(
                        Set.of("myMod", "myMod(1)", "myMod(2)", "myMod(3)"), "myMod"));
    }
}
