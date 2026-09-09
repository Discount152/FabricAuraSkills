package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 145: agility YAML unchanged (jump + movement sources). Vanilla stat resolution is validated on a running
 * dedicated server — plain JUnit cannot bootstrap {@link net.minecraft.stat.Stats}.
 *
 * <p>The multi-break default assertion moved to {@code FabricS146ExternalMultiBreakXpTest}: Sprint 145 disabled the
 * feature to stop OreHarvester interference, Sprint 146 removed the interfering snapshot scanner outright and can
 * therefore ship it enabled again.
 */
class FabricS145EmergencyOreHarvesterAndAgilityTest {

    @Test
    void embeddedConfig_stillDefinesMultiBreakSection() throws IOException {
        String text;
        try (InputStream in = FabricS145EmergencyOreHarvesterAndAgilityTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(in, "config.yml should be on fabric classpath (common resources merged)");
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(text.indexOf("multi_break_xp:") >= 0, "config should define multi_break_xp");
    }

    @Test
    void bundledAgilityYaml_containsMovementAndJumpSources() throws IOException {
        String text;
        try (InputStream in = FabricS145EmergencyOreHarvesterAndAgilityTest.class.getResourceAsStream("/sources/agility.yml")) {
            assertNotNull(in);
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(text.contains("walk_per_meter:"), "walk_per_meter source");
        assertTrue(text.contains("jump_per_100:"), "jump_per_100 source");
        assertTrue(text.contains("walk_one_cm"), "walk statistic token");
    }

    @Test
    void agilityAliasKeysStillMapVanillaPaths() {
        assertEquals("walk_one_cm", FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("walk_per_meter"));
        assertEquals("sprint_one_cm", FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("sprint_per_meter"));
        assertEquals("swim_one_cm", FabricStatisticSourceResolver.agilityVanillaPathForSourceKey("swim_per_meter"));
    }
}