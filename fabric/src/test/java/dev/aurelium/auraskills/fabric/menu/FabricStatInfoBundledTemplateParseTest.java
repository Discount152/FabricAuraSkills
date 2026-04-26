package dev.aurelium.auraskills.fabric.menu;

import dev.granitelib.fabric.template.load.GraniteMenuTemplateYamlLoader;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures the bundled {@code stat_info.yml} resource parses (no Minecraft registries required).
 */
class FabricStatInfoBundledTemplateParseTest {

    @Test
    void bundledStatInfoYmlLoads() throws Exception {
        try (InputStream in =
                FabricStatInfoBundledTemplateParseTest.class
                        .getClassLoader()
                        .getResourceAsStream(FabricStatInfoTemplateSupport.TEMPLATE_RESOURCE_PATH)) {
            assertNotNull(in, "bundled template missing from test classpath");
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            GraniteMenuTemplate t = GraniteMenuTemplateYamlLoader.loadString(yaml, "stat_info.yml");
            assertEquals("stat_info", t.id());
            assertEquals(6, t.rows());
            assertTrue(t.fill().isPresent());
            assertTrue(t.items().containsKey("stat"));
            assertTrue(t.items().containsKey("back"));
            assertTrue(
                    t.items().get("stat").lore().stream().anyMatch(line -> line.contains("Related traits")),
                    "bundled stat_info should include Related traits header before Sprint 97 append");
        }
    }
}
