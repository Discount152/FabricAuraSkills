package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helper-level tests for Sprint 136 (no full MC registry bootstrap for ItemStack/Items here).
 * Peek/take + guessVanillaFishMaterialPath with real items: manual / runtime.
 */
class FabricS136FishingBobberUseHookTest {

    @Test
    void mixinJson_excludesPlayerEntityIncreaseStatFishingMixin() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("auraskills.mixins.json")) {
            assertNotNull(in, "auraskills.mixins.json must be on test classpath");
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertFalse(text.contains("PlayerEntityIncreaseStatFishingMixin"));
        }
    }

    @Test
    void peekCaughtStack_whenEmpty_returnsNullWithoutSideEffects() {
        FabricFishingCatchHolder.clear();
        assertNull(FabricFishingCatchHolder.peekCaughtStack());
        assertNull(FabricFishingCatchHolder.peekCaughtStack());
    }

    @Test
    void fishingXpHandler_exposesBobberReturn_notFishCaughtStatApi() {
        assertTrue(Arrays.stream(FabricFishingXpHandler.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("onBobberUseReturn")));
        assertFalse(Arrays.stream(FabricFishingXpHandler.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("onFishCaughtStat")));
        assertFalse(Arrays.stream(FabricFishingXpHandler.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("isFishCaughtStat")));
    }

    @Test
    void bundledFishingYaml_containsVanillaFishForSourceLookup() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sources/fishing.yml")) {
            assertNotNull(in);
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(text.contains("cod:"));
            assertTrue(text.contains("salmon:"));
        }
    }
}