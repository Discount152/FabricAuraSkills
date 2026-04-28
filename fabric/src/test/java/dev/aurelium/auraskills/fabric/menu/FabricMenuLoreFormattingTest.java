package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricMenuLoreFormattingTest {

    @Test
    void wordWrapPlain_defaultWidth_noLineExceeds42() {
        String in =
                "aaaa bbbb cccc dddd eeee ffff gggg hhhh iiii "
                        + "jjjj kkkk llll mmmm nnnn oooo pppp qqqq";
        List<String> lines = FabricMenuLoreFormatting.wordWrapPlain(in, 42);
        assertTrue(lines.size() >= 2, "expected wrap into multiple lines: " + lines);
        for (String line : lines) {
            assertTrue(line.length() <= 42, "line too long: " + line.length() + " " + line);
        }
    }

    @Test
    void wordWrapPlain_longToken_splitsWithoutSpaces() {
        List<String> lines = FabricMenuLoreFormatting.wordWrapPlain("abcdefgh", 3);
        assertEquals(List.of("abc", "def", "gh"), lines);
    }
}