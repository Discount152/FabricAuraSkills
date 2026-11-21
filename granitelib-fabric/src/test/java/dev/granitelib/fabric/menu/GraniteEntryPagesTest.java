package dev.granitelib.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GraniteEntryPagesTest {

    @Test
    void nextPageDoesNotRepeatThePreviousSlice() {
        List<String> entries = List.of("wheat", "carrot", "potato", "beet");

        List<String> first = GraniteEntryPages.page(entries, 0, 2);
        List<String> second = GraniteEntryPages.page(entries, 1, 2);

        assertEquals(List.of("wheat", "carrot"), first);
        assertEquals(List.of("potato", "beet"), second);
        assertFalse(second.contains("wheat"));
        assertFalse(second.contains("carrot"));
    }
}
