package dev.granite.graniteskills.fabric;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BrewingIngredientXpTest {

    private static final String[] EXCLUDED = {"redstone", "glowstone_dust", "nether_wart", "gunpowder", "dragon_breath"};

    private static final List<BrewingIngredientXp.Rule> ALCHEMY = List.of(
            new BrewingIngredientXp.Rule(new String[]{"nether_wart"}, null, 10.0),
            new BrewingIngredientXp.Rule(null, EXCLUDED, 15.0),
            new BrewingIngredientXp.Rule(new String[]{"redstone"}, null, 25.0),
            new BrewingIngredientXp.Rule(new String[]{"glowstone_dust"}, null, 25.0),
            new BrewingIngredientXp.Rule(new String[]{"gunpowder"}, null, 35.0),
            new BrewingIngredientXp.Rule(new String[]{"dragon_breath"}, null, 50.0));

    @Test
    void brewingXpMatchesTheIngredientAndAnUnmatchedIngredientAwardsNothing() {
        assertEquals(10.0, BrewingIngredientXp.xpForFirstMatch("nether_wart", ALCHEMY));
        assertEquals(15.0, BrewingIngredientXp.xpForFirstMatch("spider_eye", ALCHEMY));
        assertEquals(25.0, BrewingIngredientXp.xpForFirstMatch("redstone", ALCHEMY));
        assertEquals(25.0, BrewingIngredientXp.xpForFirstMatch("glowstone_dust", ALCHEMY));
        assertEquals(35.0, BrewingIngredientXp.xpForFirstMatch("gunpowder", ALCHEMY));
        assertEquals(50.0, BrewingIngredientXp.xpForFirstMatch("dragon_breath", ALCHEMY));

        assertFalse(BrewingIngredientXp.matches("redstone", new String[]{"nether_wart"}, null));
        List<BrewingIngredientXp.Rule> awkwardOnly = List.of(ALCHEMY.getFirst());
        assertEquals(0.0, BrewingIngredientXp.xpForFirstMatch("redstone", awkwardOnly));
        assertEquals(0.0, BrewingIngredientXp.xpForFirstMatch("", ALCHEMY));
        assertEquals(0.0, BrewingIngredientXp.xpForFirstMatch(null, ALCHEMY));
    }
}
