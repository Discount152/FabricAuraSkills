package dev.aurelium.auraskills.fabric.text;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FabricGraniteEnUsKeysTest {

    @Test
    void sprint83GraniteCommandAndMenuKeysPresentInEnUs() throws Exception {
        String json;
        try (InputStream in =
                FabricGraniteEnUsKeysTest.class.getResourceAsStream(
                        "/assets/auraskills/lang/en_us.json")) {
            assertTrue(in != null, "classpath /assets/auraskills/lang/en_us.json");
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (String key :
                new String[] {
                    "auraskills.granite.command.auraskills_usage",
                    "auraskills.granite.command.help",
                    "auraskills.granite.menu.skills_title",
                    "auraskills.granite.menu.stats_title",
                    "auraskills.granite.menu.skills_opened",
                    "auraskills.granite.menu.stats_opened",
                    "auraskills.granite.command.xp.view_line",
                    "auraskills.granite.command.skilllevel.view_line"
                }) {
            assertTrue(json.contains("\"" + key + "\""), "missing " + key);
        }
    }
}
