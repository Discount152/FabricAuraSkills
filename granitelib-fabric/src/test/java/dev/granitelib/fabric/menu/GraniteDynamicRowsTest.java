package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraniteDynamicRowsTest {

    @Test
    void rowRepeatsOncePerEntry() {
        GraniteMenuTemplate menu = GraniteMenuBuilder.menu("sources")
                .title("Sources")
                .rows(6)
                .item("source", "paper", 10)
                .build();

        List<GraniteDynamicRows.RepeatedSlot> slots = GraniteDynamicRows.expand(
                menu, "source", List.of("wheat", "carrot", "potato"));

        assertEquals(3, slots.size());
        assertEquals("wheat", slots.get(0).entry());
        assertEquals(10, slots.get(0).slot());
        assertEquals("carrot", slots.get(1).entry());
        assertEquals(19, slots.get(1).slot());
        assertEquals("potato", slots.get(2).entry());
        assertEquals(28, slots.get(2).slot());
    }
}
