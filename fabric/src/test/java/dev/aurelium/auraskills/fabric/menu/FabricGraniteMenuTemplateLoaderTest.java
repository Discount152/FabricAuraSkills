package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricGraniteMenuTemplateLoaderTest {

    @TempDir
    Path auraskillsRoot;

    @BeforeEach
    void reset() {
        FabricGraniteMenuTemplateLoader.clearDiagnosticsStateForTests();
    }

    @Test
    void missingDiskUsesBundledAndPreservesClasspathLabel() {
        CollectingLogger logger = new CollectingLogger();
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                FabricGraniteMenuTemplateLoader.load(
                        auraskillsRoot,
                        logger,
                        "skills",
                        FabricSkillsTemplateSupport.class,
                        FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        assertTrue(loaded.isPresent());
        assertEquals("skills", loaded.get().template().id());
        assertEquals(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH, loaded.get().sourceLabel());
        assertTrue(logger.warns.isEmpty(), logger.warns::toString);
    }

    @Test
    void validDiskTemplatePreferred() throws Exception {
        String bundled = readBundledYaml(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        String custom = bundled.replaceFirst("(?m)^title:.*$", "title: \"Custom Skills Test\"");
        Path templates = auraskillsRoot.resolve(FabricGraniteMenuTemplateLoader.RELATIVE_TEMPLATE_DIR);
        Files.createDirectories(templates);
        Path disk = templates.resolve("skills.yml");
        Files.writeString(disk, custom);

        CollectingLogger logger = new CollectingLogger();
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                FabricGraniteMenuTemplateLoader.load(
                        auraskillsRoot,
                        logger,
                        "skills",
                        FabricSkillsTemplateSupport.class,
                        FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        assertTrue(loaded.isPresent());
        GraniteMenuTemplate t = loaded.get().template();
        assertEquals("skills", t.id());
        assertTrue(t.title().contains("Custom Skills Test"), t::title);
        assertEquals(disk.toAbsolutePath().normalize().toString(), loaded.get().sourceLabel());
        assertTrue(logger.infos.stream().anyMatch(s -> s.contains("Loaded Granite menu template 'skills' from")), logger.infos::toString);
        assertTrue(logger.warns.isEmpty(), logger.warns::toString);
    }

    @Test
    void invalidDiskYamlFallsBackToBundledWithoutThrowing() throws Exception {
        Path templates = auraskillsRoot.resolve(FabricGraniteMenuTemplateLoader.RELATIVE_TEMPLATE_DIR);
        Files.createDirectories(templates);
        Files.writeString(templates.resolve("skills.yml"), "- not\n- a\n- mapping");

        CollectingLogger logger = new CollectingLogger();
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                assertDoesNotThrow(
                        () ->
                                FabricGraniteMenuTemplateLoader.load(
                                        auraskillsRoot,
                                        logger,
                                        "skills",
                                        FabricSkillsTemplateSupport.class,
                                        FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH));
        assertTrue(loaded.isPresent());
        assertEquals(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH, loaded.get().sourceLabel());
        assertTrue(logger.warns.stream().anyMatch(w -> w.contains("falling back to bundled template")), logger.warns::toString);
    }

    @Test
    void wrongDiskIdFallsBackToBundled() throws Exception {
        String bundled = readBundledYaml(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        String broken = bundled.replaceFirst("(?m)^id:\\s*skills\\s*$", "id: wrong_id");
        Path templates = auraskillsRoot.resolve(FabricGraniteMenuTemplateLoader.RELATIVE_TEMPLATE_DIR);
        Files.createDirectories(templates);
        Files.writeString(templates.resolve("skills.yml"), broken);

        CollectingLogger logger = new CollectingLogger();
        Optional<FabricGraniteMenuTemplateLoader.LoadedTemplate> loaded =
                FabricGraniteMenuTemplateLoader.load(
                        auraskillsRoot,
                        logger,
                        "skills",
                        FabricSkillsTemplateSupport.class,
                        FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH);
        assertTrue(loaded.isPresent());
        assertEquals("skills", loaded.get().template().id());
        assertEquals(FabricSkillsTemplateSupport.TEMPLATE_RESOURCE_PATH, loaded.get().sourceLabel());
        assertTrue(
                logger.warns.stream()
                        .anyMatch(w -> w.contains("template id must be 'skills'") && w.contains("wrong_id")),
                logger.warns::toString);
    }

    @Test
    void diskPathResolverMatchesExpectedLayout() {
        Path p = FabricGraniteMenuTemplateLoader.diskTemplatePath(auraskillsRoot, "stat_info");
        assertEquals(
                auraskillsRoot.resolve("granite/templates/stat_info.yml").normalize(),
                p.normalize());
    }

    private static String readBundledYaml(String classpathPath) throws Exception {
        try (InputStream in =
                FabricGraniteMenuTemplateLoaderTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
            warns.add(message + " (" + throwable.getMessage() + ")");
        }

        @Override
        public void severe(String message) {}

        @Override
        public void severe(String message, Throwable throwable) {}

        @Override
        public void debug(String message) {}
    }
}
