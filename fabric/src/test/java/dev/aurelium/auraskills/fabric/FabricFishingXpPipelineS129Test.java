package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricFishingXpPipelineS129Test {

    @Test
    void bundledFishingSourceYaml_containsCoreEntries() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("sources/fishing.yml")) {
            assertNotNull(in, "sources/fishing.yml should be bundled");
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(text.contains("type: fishing"));
            assertTrue(text.contains("cod:"));
            assertTrue(text.contains("salmon:"));
            assertTrue(text.contains("treasure:"));
            assertTrue(text.contains("junk:"));
        }
    }
}
