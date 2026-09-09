package dev.aurelium.auraskills.fabric.sources;

import dev.aurelium.auraskills.api.skill.Skills;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricEmbeddedSourceYamlClasspathTest {

    private static final Skills[] SKILLS_WITH_BUNDLED_SOURCES = {
            Skills.FARMING,
            Skills.FORAGING,
            Skills.MINING,
            Skills.FISHING,
            Skills.EXCAVATION,
            Skills.ARCHERY,
            Skills.FIGHTING,
            Skills.DEFENSE,
            Skills.AGILITY,
            Skills.ALCHEMY,
            Skills.ENCHANTING,
    };

    @Test
    void bundledSourceYamlsExistOnClasspath() throws IOException {
        for (Skills skill : SKILLS_WITH_BUNDLED_SOURCES) {
            String path = "sources/" + skill.name().toLowerCase() + ".yml";
            String text = readUtf8(path);
            assertFalse(text.isBlank(), path + " should not be blank");
            assertTrue(text.contains("sources:"), path + " should define sources");
        }
    }

    @Test
    void sprint125ScopeExcludesForgingFromRequiredBundledSourceValidation() {
        long forgingCount = Arrays.stream(SKILLS_WITH_BUNDLED_SOURCES)
                .filter(s -> s == Skills.FORGING)
                .count();
        assertEquals(0, forgingCount, "forging is out of Sprint 125 /sources validation scope");
    }

    @Test
    void affectedSkillYamlsContainRuntimeSourceEntries() throws IOException {
        String[] paths = {
                "sources/farming.yml",
                "sources/foraging.yml",
                "sources/mining.yml",
                "sources/excavation.yml",
                "sources/defense.yml"
        };
        for (String path : paths) {
            String text = readUtf8(path);
            assertTrue(text.contains("sources:"), path + ": sources section");
            assertTrue(text.contains("xp:"), path + ": xp rows");
        }
    }

    private static String readUtf8(String path) throws IOException {
        InputStream in = FabricEmbeddedSourceYamlClasspathTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(in, "missing classpath resource: " + path);
        try (InputStream closeable = in) {
            return new String(closeable.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}