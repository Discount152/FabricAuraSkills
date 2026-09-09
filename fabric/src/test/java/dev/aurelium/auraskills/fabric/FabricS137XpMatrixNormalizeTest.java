package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricS137XpMatrixNormalizeTest {

    @Test
    void joinKeys_joinsPipe() {
        assertEquals("a|b", FabricXpLookupNormalize.joinKeys(List.of("a", "b")));
        assertEquals("-", FabricXpLookupNormalize.joinKeys(List.of()));
    }

    @Test
    void damageCauseKey_lowercased() {
        assertEquals("damage_cause:fall", FabricXpLookupNormalize.damageCauseKey(DamageCause.FALL));
    }

    @Test
    void joinTruncate_respectsMaxLength() {
        LinkedHashSet<String> big = new LinkedHashSet<>();
        big.add("x".repeat(1200));
        big.add("y".repeat(1200));
        String s = FabricXpLookupNormalize.joinTruncate(big);
        assertTrue(s.contains("truncated"));
    }

    @Test
    void candidateCounts_zeroWhenPluginNull() {
        assertEquals(0, FabricXpLookupNormalize.blockBreakCandidateCount(null));
        assertEquals(0, FabricXpLookupNormalize.damageXpCandidateCount(null));
        assertEquals("-", FabricXpLookupNormalize.blockBreakCandidateSourceIdsCsv(null));
        assertEquals("-", FabricXpLookupNormalize.damageXpCandidateSourceIdsCsv(null));
    }
}