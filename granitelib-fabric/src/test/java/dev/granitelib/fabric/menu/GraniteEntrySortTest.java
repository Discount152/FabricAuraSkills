package dev.granitelib.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraniteEntrySortTest {

    @Test
    void theSameEntriesSortInTwoOrders() {
        List<GraniteEntrySort.Entry> entries = List.of(
                new GraniteEntrySort.Entry("wheat", 30),
                new GraniteEntrySort.Entry("carrot", 5),
                new GraniteEntrySort.Entry("potato", 10));

        List<String> byName = GraniteEntrySort.sort(entries, GraniteEntrySort.Order.NAME)
                .stream().map(GraniteEntrySort.Entry::name).toList();
        List<String> byValue = GraniteEntrySort.sort(entries, GraniteEntrySort.Order.VALUE)
                .stream().map(GraniteEntrySort.Entry::name).toList();

        assertEquals(List.of("carrot", "potato", "wheat"), byName);
        assertEquals(List.of("wheat", "potato", "carrot"), byValue);
    }
}
