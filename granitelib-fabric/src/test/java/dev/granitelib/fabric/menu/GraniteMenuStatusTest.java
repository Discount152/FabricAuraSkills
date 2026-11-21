package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteMenuStatusTest {

    @TempDir
    Path temp;

    @Test
    void statusNamesTheEffectiveFileFallbackAndError() throws Exception {
        Path user = temp.resolve("user.yml");
        Path defaults = temp.resolve("default.yml");
        Files.writeString(user, menu("User"));
        Files.writeString(defaults, menu("Default"));
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();

        GraniteMenuStatus direct = registry.loadPreferring("status_probe", user, defaults, unused());
        assertEquals(user.toString(), direct.effectiveFile());
        assertFalse(direct.fallback());
        assertEquals("", direct.error());
        assertEquals("User", registry.template("status_probe").title());

        Files.writeString(user, "id: status_probe\ntitle: Broken\nrows: 0\n");
        GraniteMenuStatus fallback = registry.loadPreferring("status_probe", user, defaults, unused());
        assertEquals(defaults.toString(), fallback.effectiveFile());
        assertTrue(fallback.fallback());
        assertTrue(fallback.error().contains("rows"));
        assertEquals("Default", registry.template("status_probe").title());
        assertEquals(fallback, registry.status("status_probe"));
    }

    private static String menu(String title) {
        return "id: status_probe\ntitle: " + title + "\nrows: 1\n";
    }

    private static GraniteMenuListener unused() {
        return new GraniteMenuListener() {
            @Override
            public void onOpen(GraniteMenuTemplate template) {
            }

            @Override
            public void onReplaced() {
            }
        };
    }
}
