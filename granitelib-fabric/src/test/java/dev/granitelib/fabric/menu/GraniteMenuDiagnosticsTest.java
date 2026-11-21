package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraniteMenuDiagnosticsTest {

    @TempDir
    Path temp;

    @Test
    void diagnosticsNameTheInvalidFileAndTheReason() throws Exception {
        Path user = temp.resolve("user.yml");
        Path defaults = temp.resolve("default.yml");
        Files.writeString(user, "id: diag_probe\ntitle: Broken\nrows: 0\n");
        Files.writeString(defaults, "id: diag_probe\ntitle: Default\nrows: 1\n");
        GraniteLoadedMenuRegistry registry = new GraniteLoadedMenuRegistry();
        registry.loadPreferring("diag_probe", user, defaults, unused());

        List<GraniteMenuDiagnostic> found = registry.diagnostics("diag_probe");
        assertTrue(found.stream().anyMatch(item ->
                item.file().endsWith("user.yml") && item.reason().contains("rows")));
        assertEquals("Default", registry.template("diag_probe").title());
        assertTrue(GraniteMenuDiagnostics.inspect(defaults).isEmpty());
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
