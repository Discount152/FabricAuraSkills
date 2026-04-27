package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.template.GraniteTemplateResources;
import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.load.GraniteTemplateValidationException;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
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
 * fallbacks. No hot reload. Does not copy defaults to disk (operators add files manually).
 */
public final class FabricGraniteMenuTemplateLoader {

    /** Under {@link FabricAuraSkills} data folder ({@code config/auraskills}). */
    public static final String RELATIVE_TEMPLATE_DIR = "granite/templates";

    private static final Set<String> INVALID_DISK_WARNED = ConcurrentHashMap.newKeySet();
    private static final Set<String> DISK_SUCCESS_LOGGED = ConcurrentHashMap.newKeySet();

    public record LoadedTemplate(GraniteMenuTemplate template, String sourceLabel) {}

    private FabricGraniteMenuTemplateLoader() {}

    @VisibleForTesting
    static void clearDiagnosticsStateForTests() {
        INVALID_DISK_WARNED.clear();
        DISK_SUCCESS_LOGGED.clear();
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
        Path diskPath = diskTemplatePath(auraskillsDataRoot, menuId);
        if (Files.isRegularFile(diskPath)) {
            try {
                GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadPath(diskPath);
                if (!menuId.equalsIgnoreCase(t.id())) {
                    warnInvalidDiskOnce(
                            logger, menuId, diskPath, "template id must be '" + menuId + "', got '" + t.id() + "'");
                } else {
                    logDiskSuccessOnce(logger, menuId, diskPath);
                    return Optional.of(new LoadedTemplate(t, diskPath.toAbsolutePath().normalize().toString()));
                }
            } catch (IOException e) {
                warnInvalidDiskOnce(logger, menuId, diskPath, "read failed: " + e.getMessage());
            } catch (GraniteTemplateValidationException e) {
                warnInvalidDiskOnce(logger, menuId, diskPath, String.join("; ", e.errors()));
            } catch (RuntimeException e) {
                String diskKey = menuId + "|" + diskPath.toAbsolutePath().normalize();
                if (INVALID_DISK_WARNED.add(diskKey)) {
                    logger.warn(
                            "Invalid Granite menu template '"
                                    + menuId
                                    + "' at "
                                    + diskPath.toAbsolutePath().normalize()
                                    + ": "
                                    + e.getMessage()
                                    + "; falling back to bundled template",
                            e);
                }
            }
        }

        return loadBundled(logger, menuId, classpathAnchor, bundledClasspathPath);
    }

    public static Path diskTemplatePath(Path auraskillsDataRoot, String menuId) {
        return auraskillsDataRoot.resolve(RELATIVE_TEMPLATE_DIR).resolve(menuId + ".yml").normalize();
    }

    private static Optional<LoadedTemplate> loadBundled(
            PlatformLogger logger, String menuId, Class<?> classpathAnchor, String bundledClasspathPath) {
        try (InputStream in = GraniteTemplateResources.openClasspath(classpathAnchor, bundledClasspathPath)) {
            if (in == null) {
                logger.warn(
                        "Bundled Granite menu template '"
                                + menuId
                                + "' missing from classpath ("
                                + bundledClasspathPath
                                + "); falling back to built-in menu");
                return Optional.empty();
            }
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, bundledClasspathPath);
            return Optional.of(new LoadedTemplate(t, bundledClasspathPath));
        } catch (IOException e) {
            logger.warn(
                    "Bundled Granite menu template '" + menuId + "' failed: " + e.getMessage()
                            + "; falling back to built-in menu",
                    e);
            return Optional.empty();
        } catch (GraniteTemplateValidationException e) {
            logger.warn(
                    "Bundled Granite menu template '"
                            + menuId
                            + "' invalid: "
                            + String.join("; ", e.errors())
                            + "; falling back to built-in menu");
            return Optional.empty();
        }
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
