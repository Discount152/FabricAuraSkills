package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.load.GraniteMenuExporter;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * One loaded menu per id. Reload replaces that menu and its listener. The previous listener is
 * told to drop its registration and is no longer invoked.
 */
public final class GraniteLoadedMenuRegistry {

    private final ConcurrentMap<String, Registration> loaded = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, GraniteMenuStatus> reported = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<GraniteMenuDiagnostic>> findings = new ConcurrentHashMap<>();

    public void load(GraniteMenuTemplate template, GraniteMenuListener listener) {
        replace(template, listener);
    }

    public void reload(String id, Path file, GraniteMenuListener listener)
            throws IOException, GraniteTemplateValidationException {
        GraniteMenuTemplate template = GraniteMenuTemplateYamlLoader.loadPath(file);
        if (!id.equals(template.id())) {
            GraniteTemplateValidationException mismatch = new GraniteTemplateValidationException(
                    java.util.List.of("Reloaded menu id '" + template.id() + "' does not match '" + id + "'"));
            rememberError(id, mismatch);
            throw mismatch;
        }
        replace(template, listener);
        reported.put(id, new GraniteMenuStatus(id, file.toString(), false, ""));
        findings.put(id, List.of());
    }

    /**
     * Loads {@code preferred} when it is valid. Otherwise loads {@code fallbackFile}, keeps the
     * preferred failure as the error, and marks the status as a fallback.
     */
    public GraniteMenuStatus loadPreferring(
            String id, Path preferred, Path fallbackFile, GraniteMenuListener listener) throws IOException {
        try {
            GraniteMenuTemplate template = readMenu(id, preferred);
            replace(template, listener);
            GraniteMenuStatus status = new GraniteMenuStatus(id, preferred.toString(), false, "");
            reported.put(id, status);
            findings.put(id, List.of());
            return status;
        } catch (RuntimeException | IOException preferredError) {
            findings.put(id, GraniteMenuDiagnostics.inspect(preferred));
            try {
                GraniteMenuTemplate template = readMenu(id, fallbackFile);
                replace(template, listener);
                GraniteMenuStatus status = new GraniteMenuStatus(
                        id, fallbackFile.toString(), true, message(preferredError));
                reported.put(id, status);
                return status;
            } catch (RuntimeException | IOException fallbackError) {
                rememberError(id, preferredError);
                throw fallbackError;
            }
        }
    }

    public GraniteMenuStatus status(String id) {
        GraniteMenuStatus status = reported.get(id);
        if (status != null) {
            return status;
        }
        return new GraniteMenuStatus(id, "", false, "");
    }

    public List<GraniteMenuDiagnostic> diagnostics(String id) {
        return findings.getOrDefault(id, List.of());
    }

    /**
     * Writes the menu currently registered for {@code id}. A missing id does not create a file.
     */
    public void export(String id, Path path) throws IOException {
        GraniteMenuTemplate current = template(id);
        if (current == null) {
            throw new IllegalStateException("No loaded menu '" + id + "'");
        }
        GraniteMenuExporter.write(current, path);
    }

    public GraniteMenuTemplate template(String id) {
        Registration registration = loaded.get(id);
        return registration == null ? null : registration.template;
    }

    public void open(String id) {
        open(id, null);
    }

    public <T> void open(String id, GraniteMenuContext<T> context) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("menu id is required");
        }
        Registration registration = loaded.get(id);
        if (registration == null) {
            throw new IllegalArgumentException("unknown menu id '" + id + "'");
        }
        if (context == null) {
            registration.listener.onOpen(registration.template);
        } else {
            registration.listener.onOpen(registration.template, context);
        }
    }

    public boolean isCurrent(String id, GraniteMenuListener listener) {
        Registration registration = loaded.get(id);
        return registration != null && registration.listener == listener;
    }

    public int size() {
        return loaded.size();
    }

    private void replace(GraniteMenuTemplate template, GraniteMenuListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }
        Registration next = new Registration(template, listener);
        Registration previous = loaded.put(template.id(), next);
        if (previous != null && previous.listener != listener) {
            previous.listener.onReplaced();
        }
    }

    private GraniteMenuTemplate readMenu(String id, Path file)
            throws IOException, GraniteTemplateValidationException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("Menu file does not exist: " + file);
        }
        GraniteMenuTemplate template = GraniteMenuTemplateYamlLoader.loadPath(file);
        if (!id.equals(template.id())) {
            throw new GraniteTemplateValidationException(
                    java.util.List.of("Menu id '" + template.id() + "' does not match '" + id + "'"));
        }
        return template;
    }

    private void rememberError(String id, Exception error) {
        GraniteMenuStatus previous = reported.get(id);
        String effective = previous == null ? "" : previous.effectiveFile();
        boolean fallback = previous != null && previous.fallback();
        reported.put(id, new GraniteMenuStatus(id, effective, fallback, message(error)));
    }

    private static String message(Exception error) {
        String text = error.getMessage();
        return text == null || text.isBlank() ? error.getClass().getSimpleName() : text;
    }

    private record Registration(GraniteMenuTemplate template, GraniteMenuListener listener) {
    }
}
