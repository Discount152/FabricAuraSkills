package dev.aurelium.auraskills.fabric.command;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricCommandLocaleArgsTest {

    @Test
    void parse_replacesUnderscoreWithHyphen() {
        assertEquals(
                Locale.forLanguageTag("en-US"),
                FabricCommandLocaleArgs.parseLanguageTag("en_US"));
    }
}
