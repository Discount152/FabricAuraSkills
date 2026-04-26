package dev.aurelium.auraskills.fabric.text;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Sprint 90 — fails if any Fabric module main source under {@code
 * dev.aurelium.auraskills.fabric} uses {@code Text.translatable("auraskills.granite…")}. Vanilla
 * Java clients would show raw keys; use {@link FabricGraniteText#key} instead.
 */
class FabricGraniteJavaSourcesTranslatableScanTest {

    private static final String BANNED_SNIPPET = "Text.translatable(\"auraskills.granite";

    @Test
    void fabricMainSourcesDoNotUseTranslatableForGraniteKeys() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path fabricJava = root.resolve("src/main/java/dev/aurelium/auraskills/fabric");
        assertTrue(Files.isDirectory(fabricJava), "expected directory: " + fabricJava.toAbsolutePath());
        try (Stream<Path> walk = Files.walk(fabricJava)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                    .forEach(
                            f -> {
                                try {
                                    String src = Files.readString(f);
                                    assertFalse(
                                            src.contains(BANNED_SNIPPET),
                                            f
                                                    + " must not use Text.translatable for "
                                                    + "auraskills.granite keys; use FabricGraniteText.key");
                                } catch (Exception e) {
                                    throw new AssertionError(f.toString(), e);
                                }
                            });
        }
    }
}
