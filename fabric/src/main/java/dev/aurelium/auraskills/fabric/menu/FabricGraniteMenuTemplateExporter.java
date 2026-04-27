package dev.aurelium.auraskills.fabric.menu;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 108: copy bundled Granite menu YAML into {@link FabricGraniteMenuTemplateLoader#RELATIVE_TEMPLATE_DIR} for
 * operator customization. {@link #exportMissing} never overwrites existing files.
 *
 * <p>Sprint 109: {@link #previewOverwrite} is read-only; {@link #exportOverwrite} replaces existing files with bundled
 * defaults (explicit confirm path only).
 */
public final class FabricGraniteMenuTemplateExporter {

    @FunctionalInterface
    public interface BundledOpener {
        /**
         * @param classpathPath e.g. {@code assets/auraskills/granite/templates/skills.yml}
         */
        @Nullable InputStream open(String classpathPath);
    }

    public record FileOutcome(String menuId, Kind kind, @Nullable String detail) {
        public enum Kind {
            CREATED,
            SKIPPED,
            FAILED
        }
    }

    public record ExportOutcome(int created, int skipped, int failed, Path templatesDirectory, List<FileOutcome> files) {}

    public record PreviewFileOutcome(String menuId, PreviewKind kind, @Nullable String detail) {
        public enum PreviewKind {
            WOULD_CREATE,
            WOULD_OVERWRITE,
            WOULD_FAIL
        }
    }

    public record OverwritePreviewOutcome(
            int wouldCreate,
            int wouldOverwrite,
            int wouldFail,
            Path templatesDirectory,
            List<PreviewFileOutcome> files) {}

    public record OverwriteFileOutcome(String menuId, OverwriteKind kind, @Nullable String detail) {
        public enum OverwriteKind {
            CREATED,
            OVERWRITTEN,
            FAILED
        }
    }

    public record OverwriteExportOutcome(
            int created,
            int overwritten,
            int failed,
            Path templatesDirectory,
            List<OverwriteFileOutcome> files) {}

    private FabricGraniteMenuTemplateExporter() {}

    /**
     * @param auraskillsDataRoot {@code config/auraskills} directory
     */
    public static ExportOutcome exportMissing(Path auraskillsDataRoot, BundledOpener opener) {
        Path templatesDirectory = auraskillsDataRoot.resolve(FabricGraniteMenuTemplateLoader.RELATIVE_TEMPLATE_DIR);
        try {
            Files.createDirectories(templatesDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create " + templatesDirectory, e);
        }

        int created = 0;
        int skipped = 0;
        int failed = 0;
        List<FileOutcome> outcomes = new ArrayList<>();

        for (String menuId : FabricGraniteBundledMenuTemplateIds.ALL) {
            Path dest = templatesDirectory.resolve(menuId + ".yml");
            if (Files.isRegularFile(dest)) {
                skipped++;
                outcomes.add(new FileOutcome(menuId, FileOutcome.Kind.SKIPPED, "file already exists"));
                continue;
            }
            String cp = FabricGraniteBundledMenuTemplateIds.classpathPathFor(menuId);
            try (InputStream in = opener.open(cp)) {
                if (in == null) {
                    failed++;
                    outcomes.add(
                            new FileOutcome(menuId, FileOutcome.Kind.FAILED, "bundled resource missing: " + cp));
                    continue;
                }
                Files.copy(in, dest);
                created++;
                outcomes.add(new FileOutcome(menuId, FileOutcome.Kind.CREATED, null));
            } catch (IOException e) {
                failed++;
                outcomes.add(new FileOutcome(menuId, FileOutcome.Kind.FAILED, e.getMessage()));
            }
        }

        return new ExportOutcome(created, skipped, failed, templatesDirectory, List.copyOf(outcomes));
    }

    /**
     * Read-only forecast for {@link #exportOverwrite}. Does not create directories or files.
     *
     * @param auraskillsDataRoot {@code config/auraskills} directory
     */
    public static OverwritePreviewOutcome previewOverwrite(Path auraskillsDataRoot, BundledOpener opener) {
        Path templatesDirectory = auraskillsDataRoot.resolve(FabricGraniteMenuTemplateLoader.RELATIVE_TEMPLATE_DIR);
        int wouldCreate = 0;
        int wouldOverwrite = 0;
        int wouldFail = 0;
        List<PreviewFileOutcome> outcomes = new ArrayList<>();

        for (String menuId : FabricGraniteBundledMenuTemplateIds.ALL) {
            Path dest = templatesDirectory.resolve(menuId + ".yml");
            String cp = FabricGraniteBundledMenuTemplateIds.classpathPathFor(menuId);
            try (InputStream in = opener.open(cp)) {
                if (in == null) {
                    wouldFail++;
                    outcomes.add(
                            new PreviewFileOutcome(
                                    menuId,
                                    PreviewFileOutcome.PreviewKind.WOULD_FAIL,
                                    "bundled resource missing: " + cp));
                    continue;
                }
            } catch (IOException e) {
                wouldFail++;
                outcomes.add(
                        new PreviewFileOutcome(menuId, PreviewFileOutcome.PreviewKind.WOULD_FAIL, e.getMessage()));
                continue;
            }
            if (Files.isRegularFile(dest)) {
                wouldOverwrite++;
                outcomes.add(new PreviewFileOutcome(menuId, PreviewFileOutcome.PreviewKind.WOULD_OVERWRITE, null));
            } else {
                wouldCreate++;
                outcomes.add(new PreviewFileOutcome(menuId, PreviewFileOutcome.PreviewKind.WOULD_CREATE, null));
            }
        }

        return new OverwritePreviewOutcome(
                wouldCreate,
                wouldOverwrite,
                wouldFail,
                templatesDirectory,
                List.copyOf(outcomes));
    }

    /**
     * Replace or create bundled templates on disk ({@code granite/templates/*.yml}).
     *
     * @param auraskillsDataRoot {@code config/auraskills} directory
     */
    public static OverwriteExportOutcome exportOverwrite(Path auraskillsDataRoot, BundledOpener opener) {
        Path templatesDirectory = auraskillsDataRoot.resolve(FabricGraniteMenuTemplateLoader.RELATIVE_TEMPLATE_DIR);
        try {
            Files.createDirectories(templatesDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create " + templatesDirectory, e);
        }

        int created = 0;
        int overwritten = 0;
        int failed = 0;
        List<OverwriteFileOutcome> outcomes = new ArrayList<>();

        for (String menuId : FabricGraniteBundledMenuTemplateIds.ALL) {
            Path dest = templatesDirectory.resolve(menuId + ".yml");
            String cp = FabricGraniteBundledMenuTemplateIds.classpathPathFor(menuId);
            try (InputStream in = opener.open(cp)) {
                if (in == null) {
                    failed++;
                    outcomes.add(
                            new OverwriteFileOutcome(
                                    menuId,
                                    OverwriteFileOutcome.OverwriteKind.FAILED,
                                    "bundled resource missing: " + cp));
                    continue;
                }
                boolean existed = Files.isRegularFile(dest);
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                if (existed) {
                    overwritten++;
                    outcomes.add(new OverwriteFileOutcome(menuId, OverwriteFileOutcome.OverwriteKind.OVERWRITTEN, null));
                } else {
                    created++;
                    outcomes.add(new OverwriteFileOutcome(menuId, OverwriteFileOutcome.OverwriteKind.CREATED, null));
                }
            } catch (IOException e) {
                failed++;
                outcomes.add(new OverwriteFileOutcome(menuId, OverwriteFileOutcome.OverwriteKind.FAILED, e.getMessage()));
            }
        }

        return new OverwriteExportOutcome(created, overwritten, failed, templatesDirectory, List.copyOf(outcomes));
    }
}
