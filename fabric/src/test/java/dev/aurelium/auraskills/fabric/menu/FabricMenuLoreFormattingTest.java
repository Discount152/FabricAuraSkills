package dev.aurelium.auraskills.fabric.menu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    /**
     * Mirrors {@link FabricMenuLoreFormatting#appendWrappedFormattedLore} paragraph handling for plain
     * ability descriptions (no MiniMessage tokens): each wrapped chunk is at most 42 characters.
     */
    @Test
    void wordWrapPlain_multilineAbilityBody_noChunkExceeds42() {
        String body =
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa bbbb "
                        + "\n"
                        + "cccc dddd eeee ffff gggg hhhh iiii jjjj kkkk llll mmmm";
        for (String para : body.split("\n")) {
            String p = para.trim();
            if (p.isEmpty()) {
                continue;
            }
            List<String> chunks = FabricMenuLoreFormatting.wordWrapPlain(p, 42);
            for (String chunk : chunks) {
                assertTrue(chunk.length() <= 42, "chunk len " + chunk.length() + ": " + chunk);
                assertFalse(chunk.contains("<"), "plain body should not introduce tags: " + chunk);
            }
        }
    }
}
