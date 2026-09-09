package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sprint 143: resolver null-safety without requiring a bootstrapped Minecraft registry (plain JUnit).
 * Full custom-stat resolution (e.g. walk_one_cm) is validated at dedicated-server runtime.
 */
class FabricS143EmergencyStatisticResolverTest {

    @Test
    void resolve_nullOrBlank_returnsNullWithoutTouchingResolveInner() {
        assertNull(FabricStatisticSourceResolver.resolve(null));
        assertNull(FabricStatisticSourceResolver.resolve(""));
        assertNull(FabricStatisticSourceResolver.resolve("   "));
    }

    @Test
    void resolveDetailed_nullBlankAndWhitespace_reasonsWithoutRegistry() {
        FabricStatisticSourceResolver.StatResolveResult a = FabricStatisticSourceResolver.resolveDetailed(null);
        assertFalse(a.resolved());
        assertEquals("-", a.normalizedToken());
        assertEquals("null_stat_identifier", a.reason());

        FabricStatisticSourceResolver.StatResolveResult b = FabricStatisticSourceResolver.resolveDetailed("  ");
        assertFalse(b.resolved());
        assertEquals("-", b.normalizedToken());
        assertEquals("null_stat_identifier", b.reason());
    }

    @Test
    void resolve_pathsThatExitEarly_neverThrow() {
        for (String s : new String[] {"", " ", "\t", "a/b", "custom/", "mined/"}) {
            assertDoesNotThrow(() -> FabricStatisticSourceResolver.resolve(s));
        }
    }

    @Test
    void resolveDetailed_pathsThatExitEarly_neverThrow() {
        assertDoesNotThrow(() -> FabricStatisticSourceResolver.resolveDetailed("a/b"));
        assertDoesNotThrow(() -> FabricStatisticSourceResolver.resolveDetailed("custom/"));
    }
}