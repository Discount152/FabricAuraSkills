package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteMenuOpenIdTest {

    @Test
    void openByIdOpensARegisteredMenuAndRejectsAnUnknownId() {
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
        CountingListener listener = new CountingListener();
        registry.load(GraniteMenuBuilder.menu("sources").title("Sources").rows(6).build(), listener);

        registry.open("sources");
        IllegalArgumentException rejected = assertThrows(
                IllegalArgumentException.class, () -> registry.open("missing"));

        assertEquals(1, listener.opens);
        assertEquals("sources", listener.menuId);
        assertTrue(rejected.getMessage().contains("unknown menu id 'missing'"));
    }

    private static final class CountingListener implements GraniteMenuListener {
        private int opens;
        private String menuId = "";

        @Override
        public void onOpen(GraniteMenuTemplate template) {
            opens++;
            menuId = template.id();
        }

        @Override
        public void onReplaced() {
        }
    }
}
