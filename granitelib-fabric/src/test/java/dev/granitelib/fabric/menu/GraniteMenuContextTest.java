package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraniteMenuContextTest {

    @Test
    void openByIdReceivesTheTypedContextAndPage() {
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
        RecordingListener listener = new RecordingListener();
        registry.load(GraniteMenuBuilder.menu("sources").title("Sources").rows(6).build(), listener);

        registry.open("sources", new GraniteMenuContext<>("farming", 2));

        assertEquals("sources", listener.menuId);
        assertEquals("farming", listener.value);
        assertEquals(2, listener.page);
    }

    private static final class RecordingListener implements GraniteMenuListener {
        private String menuId = "";
        private String value = "";
        private int page = -1;

        @Override
        public void onOpen(GraniteMenuTemplate template) {
            menuId = template.id();
        }

        @Override
        public void onOpen(GraniteMenuTemplate template, GraniteMenuContext<?> context) {
            menuId = template.id();
            value = String.valueOf(context.value());
            page = context.page();
        }

        @Override
        public void onReplaced() {
        }
    }
}
