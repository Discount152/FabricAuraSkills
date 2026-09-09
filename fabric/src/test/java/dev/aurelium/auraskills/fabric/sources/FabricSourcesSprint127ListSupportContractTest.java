package dev.aurelium.auraskills.fabric.sources;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricSourcesSprint127ListSupportContractTest {

    @Test
    void fabricSourcesListSupport_doesNotRemoveBangRemoveSources() throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path f =
                root.resolve(
                        "src/main/java/dev/aurelium/auraskills/fabric/command/FabricSourcesListSupport.java");
        assertTrue(Files.isRegularFile(f), "expected " + f.toAbsolutePath());
        String src = Files.readString(f);
        assertFalse(
                src.contains("!!REMOVE!!"),
                "FabricSourcesListSupport must not filter rows by legacy !!REMOVE!! sentinel");
    }
}