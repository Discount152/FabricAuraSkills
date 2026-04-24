package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricListMenuPagingTest {

    @Test
    void listContentRegion_capsAt45() {
        FabricListMenuPaging.ListContentRegion r = FabricListMenuPaging.listContentRegion(0, 54);
        assertEquals(0, r.startInclusive());
        assertEquals(45, r.endExclusive());
        assertEquals(45, r.size());
    }

    @Test
    void maxPageIndex_and_clamp() {
        assertEquals(0, FabricListMenuPaging.maxPageIndex(5, 45));
        assertEquals(1, FabricListMenuPaging.maxPageIndex(46, 45));
        assertEquals(0, FabricListMenuPaging.clampPage(99, 10, 45));
        assertEquals(0, FabricListMenuPaging.clampPage(-1, 10, 45));
    }
}
