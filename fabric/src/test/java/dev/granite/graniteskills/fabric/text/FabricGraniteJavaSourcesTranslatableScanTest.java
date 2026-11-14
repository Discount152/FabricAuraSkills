package dev.granite.graniteskills.fabric.text;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 90 — fails if any Fabric module main source under {@code
 * dev.granite.graniteskills.fabric} uses {@code Component.translatable("graniteskills.granite…")}. Vanilla
 * Java clients would show raw keys; use {@link FabricGraniteText#key} instead.
 */
class FabricGraniteJavaSourcesTranslatableScanTest {

    private static final String BANNED_SNIPPET = "Component.translatable(\"graniteskills.granite";

    @Test
    void fabricMainSourcesDoNotUseTranslatableForGraniteKeys() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path fabricJava = root.resolve("src/main/java/dev/granite/graniteskills/fabric");
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
                                                    + " must not use Component.translatable for "
                                                    + "graniteskills.granite keys; use FabricGraniteText.key");
                                } catch (Exception e) {
                                    throw new AssertionError(f.toString(), e);
                                }
                            });
        }
    }
}
