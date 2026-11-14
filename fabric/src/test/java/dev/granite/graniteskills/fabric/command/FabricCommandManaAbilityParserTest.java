package dev.granite.graniteskills.fabric.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class FabricCommandManaAbilityParserTest {

    @Test
    void parse_nullOrEmpty_doesNotTouchPlugin() {
        assertNull(FabricCommandManaAbilityParser.parse(null, null));
        assertNull(FabricCommandManaAbilityParser.parse(null, ""));
    }
}
