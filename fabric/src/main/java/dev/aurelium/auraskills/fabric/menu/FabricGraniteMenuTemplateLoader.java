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
 * <p>Sprint 110: {@link #loadManagedForRegistry(Path, String, Class, String)} — structured outcome without debounced
 * logging for {@link FabricGraniteMenuTemplateRegistry} reload summaries. Operators should run {@code /auraskills reload}
 * after editing disk YAML so the registry cache refreshes.
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
     * @param diagnosticDetail for {@link GraniteMenuTemplateState#DISK_INVALID_BUNDLED_LOADED}, the disk validation
     *                         message (bundled succeeded). For {@link GraniteMenuTemplateState#BUNDLED_MISSING_OR_INVALID},
     *                         combined disk/bundle failure detail. Otherwise typically {@code null}.
     */
    public record ManagedTemplateLoad(
            GraniteMenuTemplateState state,
            Optional<GraniteMenuTemplate> template,
            String sourceLabel,
            @Nullable String diagnosticDetail) {}

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
        String diskInvalidReason = null;
        if (Files.isRegularFile(diskPath)) {
            try {
                GraniteMenuTemplate diskTemplate = GraniteMenuTemplateYamlLoader.loadPath(diskPath);
                if (menuId.equalsIgnoreCase(diskTemplate.id())) {
                    return new ManagedTemplateLoad(
                            GraniteMenuTemplateState.DISK_LOADED,
                            Optional.of(diskTemplate),
                            diskPath.toAbsolutePath().normalize().toString(),
                            null);
                }
                diskInvalidReason =
                        "template id must be '" + menuId + "', got '" + diskTemplate.id() + "'";
            } catch (IOException e) {
                diskInvalidReason = "read failed: " + e.getMessage();
            } catch (GraniteTemplateValidationException e) {
                diskInvalidReason = String.join("; ", e.errors());
            } catch (RuntimeException e) {
                diskInvalidReason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
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
                        diag);
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, bundledClasspathPath);
            boolean diskWasInvalid = diskInvalidReason != null;
            GraniteMenuTemplateState state =
                    diskWasInvalid
                            ? GraniteMenuTemplateState.DISK_INVALID_BUNDLED_LOADED
                            : GraniteMenuTemplateState.BUNDLED_LOADED;
            return new ManagedTemplateLoad(state, Optional.of(t), bundledClasspathPath, diskWasInvalid ? diskInvalidReason : null);
        } catch (IOException e) {
            String diag =
                    (diskInvalidReason != null ? "invalid disk (" + diskInvalidReason + "); " : "")
                            + "bundled read failed: "
                            + e.getMessage();
            return new ManagedTemplateLoad(
                    GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID, Optional.empty(), bundledClasspathPath, diag);
        } catch (GraniteTemplateValidationException e) {
            String diag =
                    (diskInvalidReason != null ? "invalid disk (" + diskInvalidReason + "); " : "")
                            + "bundled invalid: "
                            + String.join("; ", e.errors());
            return new ManagedTemplateLoad(
                    GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID, Optional.empty(), bundledClasspathPath, diag);
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
