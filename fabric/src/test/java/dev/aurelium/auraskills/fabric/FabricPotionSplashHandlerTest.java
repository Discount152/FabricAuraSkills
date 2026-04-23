package dev.aurelium.auraskills.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FabricPotionSplashHandlerTest {

    @Test
    void nullPotionContentsMatchesBukkitNoEffects() {
        assertTrue(FabricPotionSplashHandler.lacksPotionEffectForParity(null));
    }
}
