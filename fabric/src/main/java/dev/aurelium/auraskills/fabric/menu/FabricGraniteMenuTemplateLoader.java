package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.template.GraniteTemplateResources;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprint 106: disk-first load for Granite v0 menu YAML — valid {@code config/auraskills/granite/templates/&lt;id&gt;.yml}
 * overrides bundled classpath templates; invalid disk files fall back to bundled, then callers keep built-in Java
 * fallbacks.
 *
 * <p>Sprint 107: one-time diagnostics debounce for {@link #load(Path, PlatformLogger, String, Class, String)} (direct
 * per-open calls).
 *
 * <p>Sprint 110 / 111: {@link #loadManagedCore} — structured outcome without debounced logging for {@link
 * FabricGraniteMenuTemplateRegistry} reload summaries and concise operator-facing diagnostics. Operators should run {@code
 * /auraskills reload} after editing disk YAML so the registry cache refreshes.
 */
public final class FabricGraniteMenuTemplateLoader {

    /** Under {@link FabricAuraSkills} data folder ({@code config/auraskills}). */
    public static final String RELATIVE_TEMPLATE_DIR = "granite/templates";

    /**
     * Lifecycle state for a managed template load (Sprint 110). {@link #BUNDLED_MISSING_OR_INVALID} means no
     * {@link GraniteMenuTemplate} is available — menus use built-in Java.
     */
    public enum GraniteMenuTemplateState {
        DISK_LOADED,
        BUNDLED_LOADED,
        DISK_INVALID_BUNDLED_LOADED,
        BUNDLED_MISSING_OR_INVALID
    }

    /**
     * @param diagnosticDetail for {@link GraniteMenuTemplateState#DISK_INVALID_BUNDLED_LOADED}, a concise disk failure
     *                         (bundled succeeded). For {@link GraniteMenuTemplateState#BUNDLED_MISSING_OR_INVALID},
     *                         combined disk/bundle failure detail for logs. Otherwise typically {@code null}.
     * @param diskSourceDisplayPath {@code config/auraskills/granite/templates/&lt;id&gt;.yml} when a disk file was
     *                              present at load time; otherwise {@code null}.
     * @param bundledClasspathPath classpath resource path passed into this load (e.g. {@code assets/auraskills/...}).
     */
    public record ManagedTemplateLoad(
            GraniteMenuTemplateState state,
            Optional<GraniteMenuTemplate> template,
            String sourceLabel,
            @Nullable String diagnosticDetail,
            @Nullable String diskSourceDisplayPath,
            String bundledClasspathPath) {}

    private static final Set<String> INVALID_DISK_WARNED = ConcurrentHashMap.newKeySet();
    private static final Set<String> DISK_SUCCESS_LOGGED = ConcurrentHashMap.newKeySet();

    public record LoadedTemplate(GraniteMenuTemplate template, String sourceLabel) {}

    private FabricGraniteMenuTemplateLoader() {}

    /**
     * Clears per-process debounce for disk success / invalid-disk logs used by {@link #load}. {@link
     * FabricGraniteMenuTemplateRegistry#reloadAll} also calls this so legacy direct {@link #load} callers stay
     * consistent.
     */
    public static void clearDiagnosticsState() {
        INVALID_DISK_WARNED.clear();
        DISK_SUCCESS_LOGGED.clear();
    }

    @VisibleForTesting
    static void clearDiagnosticsStateForTests() {
        clearDiagnosticsState();
    }

    /**
     * Operator-facing path prefix for chat/status lines (data directory is {@code config/auraskills} on disk).
     */
    public static String toConfigRelativeDisplayPath(Path auraskillsDataRoot, Path file) {
        Path rel = auraskillsDataRoot.normalize().relativize(file.normalize());
        return "config/auraskills/" + rel.toString().replace('\\', '/');
    }

    /** Concise wrong-id message for disk YAML whose {@code id} does not match the menu file name. */
    public static String formatWrongIdDiskFailure(String expectedMenuId, String actualId) {
        return "wrong id: expected " + expectedMenuId + ", got " + actualId;
    }

    /**
     * First validation error, plus {@code (and N more)} when {@link GraniteTemplateValidationException} carries
     * multiple issues.
     */
    public static String summarizeValidationErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "validation failed";
        }
        String first = errors.get(0);
        if (errors.size() == 1) {
            return first;
        }
        return first + " (and " + (errors.size() - 1) + " more)";
    }

    private static String sanitizeOneLine(@Nullable String message) {
        if (message == null) {
            return "unknown error";
        }
        String one = message.replace('\r', ' ').replace('\n', ' ').trim();
        return one.isEmpty() ? "unknown error" : one;
    }

    private static String summarizeRuntimeFailure(RuntimeException e) {
        String msg = sanitizeOneLine(e.getMessage());
        return msg.equals("unknown error") ? e.getClass().getSimpleName() : msg;
    }

    public static Optional<LoadedTemplate> load(
            FabricAuraSkills plugin, String menuId, Class<?> classpathAnchor, String bundledClasspathPath) {
        return load(plugin.getPluginFolder().toPath(), plugin.logger(), menuId, classpathAnchor, bundledClasspathPath);
    }

    /**
     * @param auraskillsDataRoot {@code config/auraskills} root (same as {@link FabricAuraSkills#getPluginFolder()}).
     */
    public static Optional<LoadedTemplate> load(
            Path auraskillsDataRoot,
            PlatformLogger logger,
            String menuId,
            Class<?> classpathAnchor,
            String bundledClasspathPath) {
        ManagedTemplateLoad managed =
                loadManagedCore(auraskillsDataRoot, menuId, classpathAnchor, bundledClasspathPath);
        Path diskPath = diskTemplatePath(auraskillsDataRoot, menuId);
        switch (managed.state()) {
            case DISK_LOADED -> logDiskSuccessOnce(logger, menuId, diskPath);
            case DISK_INVALID_BUNDLED_LOADED ->
                    warnInvalidDiskOnce(
                            logger, menuId, diskPath, managed.diagnosticDetail() != null ? managed.diagnosticDetail() : "invalid disk");
            case BUNDLED_LOADED -> { /* quiet */ }
            case BUNDLED_MISSING_OR_INVALID ->
                    logger.warn(
                            "Granite menu template '"
                                    + menuId
                                    + "' unavailable ("
                                    + (managed.diagnosticDetail() != null ? managed.diagnosticDetail() : "no detail")
                                    + "); falling back to built-in menu");
        }
        return managed.template().map(t -> new LoadedTemplate(t, managed.sourceLabel()));
    }

    /**
     * Parse-only load for registry refresh: no debounced logging. Callers log a single summary.
     */
    public static ManagedTemplateLoad loadManagedCore(
            Path auraskillsDataRoot,
            String menuId,
            Class<?> classpathAnchor,
            String bundledClasspathPath) {
        Path diskPath = diskTemplatePath(auraskillsDataRoot, menuId);
        String diskDisplay =
                Files.isRegularFile(diskPath)
                        ? toConfigRelativeDisplayPath(auraskillsDataRoot, diskPath)
                        : null;
        String diskInvalidReason = null;
        if (Files.isRegularFile(diskPath)) {
            try {
                GraniteMenuTemplate diskTemplate = GraniteMenuTemplateYamlLoader.loadPath(diskPath);
                if (menuId.equalsIgnoreCase(diskTemplate.id())) {
                    return new ManagedTemplateLoad(
                            GraniteMenuTemplateState.DISK_LOADED,
                            Optional.of(diskTemplate),
                            diskPath.toAbsolutePath().normalize().toString(),
                            null,
                            diskDisplay,
                            bundledClasspathPath);
                }
                diskInvalidReason = formatWrongIdDiskFailure(menuId, diskTemplate.id());
            } catch (IOException e) {
                diskInvalidReason = "read failed: " + sanitizeOneLine(e.getMessage());
            } catch (GraniteTemplateValidationException e) {
                diskInvalidReason = summarizeValidationErrors(e.errors());
            } catch (RuntimeException e) {
                diskInvalidReason = summarizeRuntimeFailure(e);
            }
        }

        try (InputStream in = GraniteTemplateResources.openClasspath(classpathAnchor, bundledClasspathPath)) {
            if (in == null) {
                String diag =
                        (diskInvalidReason != null ? "invalid disk (" + diskInvalidReason + "); " : "")
                                + "bundled missing from classpath: "
                                + bundledClasspathPath;
                return new ManagedTemplateLoad(
                        GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID,
                        Optional.empty(),
                        bundledClasspathPath,
                        diag,
                        diskDisplay,
                        bundledClasspathPath);
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, bundledClasspathPath);
            boolean diskWasInvalid = diskInvalidReason != null;
            GraniteMenuTemplateState state =
                    diskWasInvalid
                            ? GraniteMenuTemplateState.DISK_INVALID_BUNDLED_LOADED
                            : GraniteMenuTemplateState.BUNDLED_LOADED;
            return new ManagedTemplateLoad(
                    state,
                    Optional.of(t),
                    bundledClasspathPath,
                    diskWasInvalid ? diskInvalidReason : null,
                    diskDisplay,
                    bundledClasspathPath);
        } catch (IOException e) {
            String diag =
                    (diskInvalidReason != null ? "invalid disk (" + diskInvalidReason + "); " : "")
                            + "bundled read failed: "
                            + sanitizeOneLine(e.getMessage());
            return new ManagedTemplateLoad(
                    GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID,
                    Optional.empty(),
                    bundledClasspathPath,
                    diag,
                    diskDisplay,
                    bundledClasspathPath);
        } catch (GraniteTemplateValidationException e) {
            String diag =
                    (diskInvalidReason != null ? "invalid disk (" + diskInvalidReason + "); " : "")
                            + "bundled invalid: "
                            + summarizeValidationErrors(e.errors());
            return new ManagedTemplateLoad(
                    GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID,
                    Optional.empty(),
                    bundledClasspathPath,
                    diag,
                    diskDisplay,
                    bundledClasspathPath);
        }
    }

    public static Path diskTemplatePath(Path auraskillsDataRoot, String menuId) {
        return auraskillsDataRoot.resolve(RELATIVE_TEMPLATE_DIR).resolve(menuId + ".yml").normalize();
    }

    private static void warnInvalidDiskOnce(PlatformLogger logger, String menuId, Path diskPath, String detail) {
        String key = menuId + "|" + diskPath.toAbsolutePath().normalize();
        if (INVALID_DISK_WARNED.add(key)) {
            logger.warn(
                    "Invalid Granite menu template '"
                            + menuId
                            + "' at "
                            + diskPath.toAbsolutePath().normalize()
                            + ": "
                            + detail
                            + "; falling back to bundled template");
        }
    }

    private static void logDiskSuccessOnce(PlatformLogger logger, String menuId, Path diskPath) {
        String key = menuId + "|" + diskPath.toAbsolutePath().normalize();
        if (DISK_SUCCESS_LOGGED.add(key)) {
            logger.info(
                    "Loaded Granite menu template '"
                            + menuId
                            + "' from "
                            + diskPath.toAbsolutePath().normalize());
        }
    }
}
