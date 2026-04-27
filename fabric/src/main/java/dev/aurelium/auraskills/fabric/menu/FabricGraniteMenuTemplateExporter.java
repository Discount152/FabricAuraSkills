package dev.aurelium.auraskills.fabric.menu;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 108: copy bundled Granite menu YAML into {@link FabricGraniteMenuTemplateLoader#RELATIVE_TEMPLATE_DIR} for
 * operator customization. Never overwrites existing files (default policy).
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
}
