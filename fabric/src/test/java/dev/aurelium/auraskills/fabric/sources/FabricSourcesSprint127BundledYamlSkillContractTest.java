package dev.aurelium.auraskills.fabric.sources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricSourcesSprint127BundledYamlSkillContractTest {

    private static final String[] REQUIRED = {
            "defense.yml", "farming.yml", "foraging.yml", "mining.yml", "excavation.yml",
    };

    @Test
    void sprint127_requiredSkills_yamlExistsWithSources() throws IOException {
        for (String name : REQUIRED) {
            String path = "sources/" + name;
            String text = readUtf8(path);
            assertTrue(text.contains("sources:"), path);
            assertTrue(text.contains("xp:"), path);
            String simple = name.replace(".yml", "");
            assertNotEquals(
                    "forging",
                    simple,
                    "forging is not part of Sprint 127 bundled /sources parity bundle");
        }
    }

    @Test
    void defenseYaml_hasTwoDamageDefinitions() throws IOException {
        String defense = readUtf8("sources/defense.yml");
        assertTrue(defense.contains("mob_damage"), "mob_damage row");
        assertTrue(defense.contains("player_damage"), "player_damage row");
        assertTrue(defense.contains("menu_item:"), "menu icons");
    }

    private static String readUtf8(String path) throws IOException {
        InputStream in = FabricSourcesSprint127BundledYamlSkillContractTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(in, "classpath " + path);
        try (InputStream closeable = in) {
            return new String(closeable.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}