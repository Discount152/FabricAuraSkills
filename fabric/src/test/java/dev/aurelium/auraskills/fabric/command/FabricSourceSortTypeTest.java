package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricSourceSortTypeTest {

    @Test
    void nextCyclicVisitsAllThenReturns() {
        FabricSourceSortType start = FabricSourceSortType.ASCENDING;
        FabricSourceSortType v = start;
        for (int i = 0; i < FabricSourceSortType.values().length; i++) {
            v = v.nextCyclic();
        }
        assertEquals(start, v);
    }

    @Test
    void fromTokenStrictEmptyIsAscending() {
        assertEquals(
                FabricSourceSortType.ASCENDING, FabricSourceSortType.fromTokenStrict("").orElse(null));
    }

    @Test
    void nextFollowsDeclOrder() {
        assertEquals(FabricSourceSortType.DESCENDING, FabricSourceSortType.ASCENDING.nextCyclic());
        assertEquals(
                FabricSourceSortType.REVERSE_ALPHABETICAL,
                FabricSourceSortType.ALPHABETICAL.nextCyclic());
    }
}
