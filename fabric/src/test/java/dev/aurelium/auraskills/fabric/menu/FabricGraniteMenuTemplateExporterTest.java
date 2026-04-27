package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricGraniteMenuTemplateExporterTest {

    @TempDir
    Path auraskillsRoot;

    private static FabricGraniteMenuTemplateExporter.BundledOpener openerAllValid() {
        Map<String, byte[]> bundled = new HashMap<>();
        for (String id : FabricGraniteBundledMenuTemplateIds.ALL) {
            String yaml =
                    """
                    id: %s
                    title: "t"
                    rows: 6
                    items: {}
                    """
                            .formatted(id);
            bundled.put(
                    FabricGraniteBundledMenuTemplateIds.classpathPathFor(id),
                    yaml.getBytes(StandardCharsets.UTF_8));
        }
        return path -> {
            byte[] b = bundled.get(path);
            if (b == null) {
                return null;
            }
            return new ByteArrayInputStream(b);
        };
    }

    @Test
    void createsTemplatesDirectoryAndCopiesAllWhenMissing() {
        FabricGraniteMenuTemplateExporter.ExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportMissing(auraskillsRoot, openerAllValid());
        Path expectedDir = auraskillsRoot.resolve("granite/templates").normalize();
        assertEquals(expectedDir.normalize(), out.templatesDirectory().normalize());
        assertEquals(8, out.created());
        assertEquals(0, out.skipped());
        assertEquals(0, out.failed());
        assertTrue(Files.isDirectory(out.templatesDirectory()));
    }

    @Test
    void secondExportSkipsExistingWithoutOverwrite() throws Exception {
        FabricGraniteMenuTemplateExporter.exportMissing(auraskillsRoot, openerAllValid());
        Path skills = auraskillsRoot.resolve("granite/templates/skills.yml");
        Files.writeString(skills, "OPERATOR_EDITS_PRESERVED\n");
        FabricGraniteMenuTemplateExporter.ExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportMissing(auraskillsRoot, openerAllValid());
        assertEquals(0, out.created());
        assertEquals(8, out.skipped());
        assertEquals(0, out.failed());
        assertEquals("OPERATOR_EDITS_PRESERVED\n", Files.readString(skills));
    }

    @Test
    void missingBundledResourceCountsFailed() {
        FabricGraniteMenuTemplateExporter.BundledOpener nullOpener = path -> null;
        FabricGraniteMenuTemplateExporter.ExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportMissing(auraskillsRoot, nullOpener);
        assertEquals(0, out.created());
        assertEquals(0, out.skipped());
        assertEquals(8, out.failed());
        assertEquals(8, out.files().stream().filter(f -> f.kind() == FabricGraniteMenuTemplateExporter.FileOutcome.Kind.FAILED).count());
    }

    @Test
    void partialMissingReportsPerFile() {
        FabricGraniteMenuTemplateExporter.BundledOpener opener = path -> {
            if (path.endsWith("skills.yml")) {
                return null;
            }
            String id = "x";
            for (String mid : FabricGraniteBundledMenuTemplateIds.ALL) {
                if (path.endsWith(mid + ".yml")) {
                    id = mid;
                    break;
                }
            }
            String yaml =
                    """
                    id: %s
                    title: "t"
                    rows: 6
                    items: {}
                    """
                            .formatted(id);
            return new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        };
        FabricGraniteMenuTemplateExporter.ExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportMissing(auraskillsRoot, opener);
        assertEquals(7, out.created());
        assertEquals(0, out.skipped());
        assertEquals(1, out.failed());
    }

    @Test
    void previewDoesNotMutateDiskOrCreateDirectories() {
        FabricGraniteMenuTemplateExporter.OverwritePreviewOutcome pre =
                FabricGraniteMenuTemplateExporter.previewOverwrite(auraskillsRoot, openerAllValid());
        assertEquals(8, pre.wouldCreate());
        assertEquals(0, pre.wouldOverwrite());
        assertEquals(0, pre.wouldFail());
        assertFalse(Files.exists(auraskillsRoot.resolve("granite/templates")));
    }

    @Test
    void previewCountsWouldOverwriteForExistingFiles() throws Exception {
        Path dir = auraskillsRoot.resolve("granite/templates");
        Files.createDirectories(dir);
        int i = 0;
        for (String id : FabricGraniteBundledMenuTemplateIds.ALL) {
            if (i++ < 3) {
                Files.writeString(dir.resolve(id + ".yml"), "preexisting\n");
            }
        }
        FabricGraniteMenuTemplateExporter.OverwritePreviewOutcome pre =
                FabricGraniteMenuTemplateExporter.previewOverwrite(auraskillsRoot, openerAllValid());
        assertEquals(5, pre.wouldCreate());
        assertEquals(3, pre.wouldOverwrite());
        assertEquals(0, pre.wouldFail());
    }

    @Test
    void previewAllFailWhenBundledMissing() {
        FabricGraniteMenuTemplateExporter.OverwritePreviewOutcome pre =
                FabricGraniteMenuTemplateExporter.previewOverwrite(auraskillsRoot, path -> null);
        assertEquals(0, pre.wouldCreate());
        assertEquals(0, pre.wouldOverwrite());
        assertEquals(8, pre.wouldFail());
    }

    @Test
    void overwriteCreatesAllWhenDirectoryEmpty() {
        FabricGraniteMenuTemplateExporter.OverwriteExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportOverwrite(auraskillsRoot, openerAllValid());
        assertEquals(8, out.created());
        assertEquals(0, out.overwritten());
        assertEquals(0, out.failed());
    }

    @Test
    void overwriteReplacesExistingFiles() throws Exception {
        FabricGraniteMenuTemplateExporter.exportMissing(auraskillsRoot, openerAllValid());
        Path skills = auraskillsRoot.resolve("granite/templates/skills.yml");
        Files.writeString(skills, "CUSTOM_OPERATOR_EDIT\n");
        FabricGraniteMenuTemplateExporter.OverwriteExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportOverwrite(auraskillsRoot, openerAllValid());
        assertEquals(0, out.created());
        assertEquals(8, out.overwritten());
        assertEquals(0, out.failed());
        String s = Files.readString(skills);
        assertTrue(s.contains("id: skills"), s);
        assertFalse(s.contains("CUSTOM_OPERATOR_EDIT"));
    }

    @Test
    void overwriteCreatesMissingAndOverwritesExisting() throws Exception {
        Path dir = auraskillsRoot.resolve("granite/templates");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("skills.yml"), "old\n");
        FabricGraniteMenuTemplateExporter.OverwriteExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportOverwrite(auraskillsRoot, openerAllValid());
        assertEquals(7, out.created());
        assertEquals(1, out.overwritten());
        assertEquals(0, out.failed());
    }

    @Test
    void overwriteReportsPerFileFailure() {
        FabricGraniteMenuTemplateExporter.BundledOpener opener = path -> {
            if (path.endsWith("skills.yml")) {
                return null;
            }
            String id = "x";
            for (String mid : FabricGraniteBundledMenuTemplateIds.ALL) {
                if (path.endsWith(mid + ".yml")) {
                    id = mid;
                    break;
                }
            }
            String yaml =
                    """
                    id: %s
                    title: "t"
                    rows: 6
                    items: {}
                    """
                            .formatted(id);
            return new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        };
        FabricGraniteMenuTemplateExporter.OverwriteExportOutcome out =
                FabricGraniteMenuTemplateExporter.exportOverwrite(auraskillsRoot, opener);
        assertEquals(7, out.created());
        assertEquals(0, out.overwritten());
        assertEquals(1, out.failed());
    }
}
