package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.message.PlatformLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricGraniteMenuTemplateRegistryTest {

    @TempDir
    Path auraskillsRoot;

    @Test
    void bindingsMatchCanonicalMenuIds() {
        assertEquals(FabricGraniteBundledMenuTemplateIds.ALL.size(), FabricGraniteMenuTemplateBindings.ALL.size());
        for (int i = 0; i < FabricGraniteBundledMenuTemplateIds.ALL.size(); i++) {
            assertEquals(
                    FabricGraniteBundledMenuTemplateIds.ALL.get(i),
                    FabricGraniteMenuTemplateBindings.ALL.get(i).menuId());
        }
    }

    @Test
    void noDiskUsesBundledForSkills() {
        FabricGraniteMenuTemplateRegistry reg = new FabricGraniteMenuTemplateRegistry();
        CollectingLogger log = new CollectingLogger();
        reg.reloadAll(auraskillsRoot, log);
        var e = reg.getEntry("skills").orElseThrow();
        assertEquals(FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.BUNDLED_LOADED, e.state());
        assertTrue(e.loaded().isPresent());
        assertTrue(log.infos.stream().anyMatch(s -> s.contains("disk 0") && s.contains("bundled 8")), log.infos::toString);
    }

    @Test
    void validDiskCountsAsDiskLoaded() throws Exception {
        Path dir = auraskillsRoot.resolve("granite/templates");
        Files.createDirectories(dir);
        String bundled = readBundledYaml(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        Files.writeString(dir.resolve("skills.yml"), bundled);

        FabricGraniteMenuTemplateRegistry reg = new FabricGraniteMenuTemplateRegistry();
        CollectingLogger log = new CollectingLogger();
        reg.reloadAll(auraskillsRoot, log);
        var e = reg.getEntry("skills").orElseThrow();
        assertEquals(FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.DISK_LOADED, e.state());
        assertTrue(log.infos.stream().anyMatch(s -> s.contains("disk 1") && s.contains("bundled 7")), log.infos::toString);
    }

    @Test
    void invalidDiskFallsBackToBundledEntry() throws Exception {
        Path dir = auraskillsRoot.resolve("granite/templates");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("skills.yml"), "id: wrong_id\nrows: 6\ntitle: x\nitems: {}");

        FabricGraniteMenuTemplateRegistry reg = new FabricGraniteMenuTemplateRegistry();
        CollectingLogger log = new CollectingLogger();
        reg.reloadAll(auraskillsRoot, log);
        var e = reg.getEntry("skills").orElseThrow();
        assertEquals(FabricGraniteMenuTemplateLoader.GraniteMenuTemplateState.DISK_INVALID_BUNDLED_LOADED, e.state());
        assertTrue(e.loaded().isPresent());
        assertTrue(log.warns.stream().anyMatch(w -> w.contains("invalid disk template")), log.warns::toString);
    }

    @Test
    void reloadPicksUpDiskEdit() throws Exception {
        Path dir = auraskillsRoot.resolve("granite/templates");
        Files.createDirectories(dir);
        String bundled = readBundledYaml(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        String v1 = bundled.replaceFirst("(?m)^title:.*$", "title: \"Registry v1\"");
        Files.writeString(dir.resolve("skills.yml"), v1);

        FabricGraniteMenuTemplateRegistry reg = new FabricGraniteMenuTemplateRegistry();
        reg.reloadAll(auraskillsRoot, new CollectingLogger());
        assertTrue(
                reg.getLoadedTemplate("skills").orElseThrow().template().title().contains("Registry v1"));

        String v2 = bundled.replaceFirst("(?m)^title:.*$", "title: \"Registry v2\"");
        Files.writeString(dir.resolve("skills.yml"), v2);
        reg.reloadAll(auraskillsRoot, new CollectingLogger());
        assertTrue(
                reg.getLoadedTemplate("skills").orElseThrow().template().title().contains("Registry v2"));
    }

    @Test
    void unknownMenuIdReturnsEmpty() {
        FabricGraniteMenuTemplateRegistry reg = new FabricGraniteMenuTemplateRegistry();
        reg.reloadAll(auraskillsRoot, new CollectingLogger());
        assertEquals(Optional.empty(), reg.getLoadedTemplate("not_a_real_menu"));
        assertEquals(Optional.empty(), reg.getEntry("not_a_real_menu"));
    }

    private static String readBundledYaml(String classpathPath) throws Exception {
        try (InputStream in =
                FabricGraniteMenuTemplateRegistryTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
            assertNotNull(in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static final class CollectingLogger implements PlatformLogger {

        final List<String> infos = new ArrayList<>();
        final List<String> warns = new ArrayList<>();

        @Override
        public void info(String message) {
            infos.add(message);
        }

        @Override
        public void warn(String message) {
            warns.add(message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            warns.add(message);
        }

        @Override
        public void severe(String message) {}

        @Override
        public void severe(String message, Throwable throwable) {}

        @Override
        public void debug(String message) {}
    }
}
