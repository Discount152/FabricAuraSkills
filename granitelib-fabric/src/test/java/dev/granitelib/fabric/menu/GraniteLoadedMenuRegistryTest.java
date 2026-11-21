package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteLoadedMenuRegistryTest {

    @TempDir
    Path temp;

    @Test
    void reloadReplacesTheMenuAndDropsThePreviousListener() throws Exception {
        Path file = temp.resolve("menu.yml");
        Files.writeString(file, menu("First"));
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
        CountingListener first = new CountingListener();
        CountingListener second = new CountingListener();
        registry.load(GraniteMenuTemplateYamlLoader.loadPath(file), first);
        registry.open("reload_probe");

        Files.writeString(file, "rows: 0\n");
        try {
            registry.reload("reload_probe", file, second);
        } catch (RuntimeException ignored) {
            // The broken file must not replace the current listener.
        }
        assertTrue(registry.isCurrent("reload_probe", first));
        assertFalse(first.replaced);

        Files.writeString(file, menu("Second"));
        registry.reload("reload_probe", file, second);
        registry.open("reload_probe");

        assertEquals(1, first.opens.get());
        assertEquals("First", first.title);
        assertTrue(first.replaced);
        assertEquals(1, second.opens.get());
        assertEquals("Second", second.title);
        assertFalse(second.replaced);
        assertTrue(registry.isCurrent("reload_probe", second));
        assertFalse(registry.isCurrent("reload_probe", first));
        assertEquals("Second", registry.template("reload_probe").title());
        assertEquals(1, registry.size());
    }

    private static String menu(String title) {
        return """
                id: reload_probe
                title: %s
                rows: 1
                """.formatted(title);
    }

    private static final class CountingListener implements GraniteMenuListener {
        private final AtomicInteger opens = new AtomicInteger();
        private String title = "";
        private boolean replaced;

        @Override
        public void onOpen(GraniteMenuTemplate template) {
            opens.incrementAndGet();
            title = template.title();
        }

        @Override
        public void onReplaced() {
            replaced = true;
        }
    }
}
