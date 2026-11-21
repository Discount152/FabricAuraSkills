package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Reads a menu file and names that file on every validation failure. */
public final class GraniteMenuDiagnostics {

    private GraniteMenuDiagnostics() {
    }

    public static List<GraniteMenuDiagnostic> inspect(Path file) {
        String name = file.toString();
        if (!Files.isRegularFile(file)) {
            return List.of(new GraniteMenuDiagnostic(name, "Menu file does not exist"));
        }
        try {
            GraniteMenuTemplateYamlLoader.loadPath(file);
            return List.of();
        } catch (GraniteTemplateValidationException ex) {
            List<GraniteMenuDiagnostic> found = new ArrayList<>();
            for (String reason : ex.errors()) {
                found.add(new GraniteMenuDiagnostic(name, reason));
            }
            if (found.isEmpty()) {
                found.add(new GraniteMenuDiagnostic(name, ex.getMessage()));
            }
            return List.copyOf(found);
        } catch (IOException ex) {
            String reason = ex.getMessage();
            if (reason == null || reason.isBlank()) {
                reason = ex.getClass().getSimpleName();
            }
            return List.of(new GraniteMenuDiagnostic(name, reason));
        }
    }
}
