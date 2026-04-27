package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sprint 110: MenuFileManager-shaped lifecycle slice — loads all known Granite v0 menu templates on server enable and
 * on {@link FabricAuraSkills#reloadFromDisk()}. Menus read parsed templates from here; disk edits require reload to take
 * effect.
 *
 * <p>Sprint 111: {@link RegistryEntry} carries operator-facing path labels, concise disk failure reasons, and fallback
 * hints for {@code /auraskills templates status}.
 */
public final class FabricGraniteMenuTemplateRegistry {

    /**
     * @param primarySourceDisplay chat label for effective YAML (disk path under {@code config/auraskills/…} or bundled
     *                             {@code assets/…} classpath).
     * @param diskSourceDisplay {@code config/…} path when a disk file was present at load; otherwise {@code null}.
     * @param bundledSourceDisplay bundled classpath path from {@link FabricGraniteMenuTemplateBindings}.
     * @param reason concise diagnostic for {@code DISK_INVALID_BUNDLED_LOADED} and {@code BUNDLED_MISSING_OR_INVALID}.
     * @param fallbackDisplay bundled classpath when falling back from invalid disk; {@code built-in Java menu} when no
     *                        template loaded.
     */
    public record RegistryEntry(
            String menuId,
            FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState state,
            Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded,
            String primarySourceDisplay,
            @Nullable String diskSourceDisplay,
            String bundledSourceDisplay,
            @Nullable String reason,
            @Nullable String fallbackDisplay) {}

    private final AtomicReference<Map<String, RegistryEntry>> entries = new AtomicReference<>(Map.of());

    public void reloadAll(FabricAuraSkills plugin) {
        reloadAll(plugin.getPluginFolder().toPath(), plugin.logger());
    }

    /**
     * Reload registry from a {@code config/auraskills} root (used by {@link #reloadAll(FabricAuraSkills)} and tests).
     */
    public void reloadAll(Path auraskillsDataRoot, PlatformLogger logger) {
        FabricGraniteMenuTemplateLoader.clearDiagnosticsState();
        Map<String, RegistryEntry> next = new LinkedHashMap<>();
        int disk = 0;
        int bundled = 0;
        int failed = 0;

        for (FabricGraniteMenuTemplateBindings.Binding b : FabricGraniteMenuTemplateBindings.ALL) {
            FabricGraniteMenuTemplateLoader.ManagedTemplateLoad ml =
                    FabricGraniteMenuTemplateLoader.loadManagedCore(
                            auraskillsDataRoot, b.menuId(), b.classpathAnchor(), b.bundledClasspathPath());

            switch (ml.state()) {
                case DISK_LOADED -> disk++;
                case BUNDLED_LOADED, DISK_INVALID_BUNDLED_LOADED -> bundled++;
                case BUNDLED_MISSING_OR_INVALID -> failed++;
            }

            Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                    ml.template().map(t -> new FabricGraniteMenuTemplateLoader.LoadedTemplate(t, ml.sourceLabel()));
            String bundledDisplay = ml.bundledClasspathPath();
            String primaryDisplay =
                    ml.state() == FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.DISK_LOADED
                            ? (ml.diskSourceDisplayPath() != null
                                    ? ml.diskSourceDisplayPath()
                                    : bundledDisplay)
                            : bundledDisplay;
            @Nullable String reason =
                    switch (ml.state()) {
                        case DISK_INVALID_BUNDLED_LOADED -> ml.diagnosticDetail();
                        case BUNDLED_MISSING_OR_INVALID -> "bundled resource missing or invalid";
                        default -> null;
                    };
            @Nullable String fallback =
                    switch (ml.state()) {
                        case DISK_INVALID_BUNDLED_LOADED -> bundledDisplay;
                        case BUNDLED_MISSING_OR_INVALID -> "built-in Java menu";
                        default -> null;
                    };
            next.put(
                    b.menuId(),
                    new RegistryEntry(
                            b.menuId(),
                            ml.state(),
                            loaded,
                            primaryDisplay,
                            ml.diskSourceDisplayPath(),
                            bundledDisplay,
                            reason,
                            fallback));

            if (ml.state() == FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.BUNDLED_MISSING_OR_INVALID) {
                logger.warn(
                        "Granite menu template registry: '"
                                + b.menuId()
                                + "' — "
                                + (ml.diagnosticDetail() != null ? ml.diagnosticDetail() : "unavailable"));
            } else if (ml.state()
                    == FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.DISK_INVALID_BUNDLED_LOADED) {
                Path diskPath = FabricGraniteMenuTemplateLoader.diskTemplatePath(auraskillsDataRoot, b.menuId());
                logger.warn(
                        "Granite menu template registry: invalid disk template '"
                                + b.menuId()
                                + "' at "
                                + diskPath.toAbsolutePath().normalize()
                                + " ("
                                + (ml.diagnosticDetail() != null ? ml.diagnosticDetail() : "invalid disk")
                                + "); using bundled");
            }
        }
        entries.set(Collections.unmodifiableMap(next));

        String summary =
                "Granite menu templates loaded: disk "
                        + disk
                        + ", bundled "
                        + bundled
                        + ", failed "
                        + failed
                        + ".";
        if (failed > 0) {
            summary += " See warnings above.";
        }
        logger.info(summary);
    }

    /** Snapshot of all known menu ids after last {@link #reloadAll}. */
    public Map<String, RegistryEntry> snapshot() {
        return entries.get();
    }

    /**
     * Template for use by Granite menu GUIs — empty if failed load or unknown menu id.
     */
    public Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> getLoadedTemplate(String menuId) {
        RegistryEntry e = entries.get().get(menuId);
        if (e == null) {
            return Optional.empty();
        }
        return e.loaded();
    }

    public Optional<RegistryEntry> getEntry(String menuId) {
        return Optional.ofNullable(entries.get().get(menuId));
    }
}
