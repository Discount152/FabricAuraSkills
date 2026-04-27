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
}
