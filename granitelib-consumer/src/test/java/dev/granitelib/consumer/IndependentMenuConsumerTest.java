package dev.granitelib.consumer;

import dev.granitelib.fabric.menu.GraniteLoadedMenuRegistry;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndependentMenuConsumerTest {

    @Test
    void opensActsAndReloadsAgainstTheReleasedJarWithoutGraniteSkills() throws Exception {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("dev.granite.graniteskills.fabric.FabricGraniteSkills"));
        String classpath = System.getProperty("java.class.path");
        assertFalse(classpath.toLowerCase().contains("graniteskills"));
        String codeSource = GraniteLoadedMenuRegistry.class.getProtectionDomain().getCodeSource().getLocation().toString();
        assertTrue(codeSource.contains("granitelib-fabric-") && codeSource.endsWith(".jar"));
        assertFalse(codeSource.toLowerCase().contains("graniteskills"));

        IndependentMenuConsumer menu = new IndependentMenuConsumer();
        menu.openMenu("fixture", "Opened");
        menu.runAction("close");
        Path file = Files.createTempDirectory("granitelib-consumer").resolve("fixture.yml");
        Files.writeString(file, """
                id: fixture
                title: Reloaded
                rows: 1
                """);
        menu.reload(file);

        assertEquals("fixture", menu.menuId());
        assertEquals("Reloaded", menu.title());
        assertEquals("close", menu.action());
        assertEquals("Consumer", menu.actionActor());

        Path result = Path.of("build", "independent-consumer-result.txt");
        Files.createDirectories(result.getParent());
        Files.writeString(result, "menu=" + menu.menuId()
                + "\nopenedTitle=Opened"
                + "\nreloadedTitle=" + menu.title()
                + "\naction=" + menu.action()
                + "\nactor=" + menu.actionActor()
                + "\ngraniteSkillsAbsent=true"
                + "\ncodeSource=" + codeSource
                + "\nPASS\n");
    }
}
