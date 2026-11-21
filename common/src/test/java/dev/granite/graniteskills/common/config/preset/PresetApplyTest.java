package dev.granite.graniteskills.common.config.preset;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.util.file.FileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresetApplyTest {

    @TempDir
    Path temp;

    @Test
    void appendPresetKeepsExistingValuesAndAddsMissingOnes() throws Exception {
        File root = temp.resolve("graniteskills").toFile();
        assertTrue(root.mkdirs());
        File config = new File(root, "config.yml");
        Files.writeString(config.toPath(), "kept: live\nshared: live\n", StandardCharsets.UTF_8);

        File presets = new File(root, "presets");
        assertTrue(presets.mkdirs());
        File zip = new File(presets, "pack.zip");
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
            put(out, "preset.yml", """
                    file_version: 1
                    files:
                      - path: config.yml
                        action: append
                    """);
            put(out, "files/config.yml", """
                    kept: preset
                    shared: preset
                    added: from-preset
                    """);
        }

        GraniteSkillsPlugin plugin = pluginWithFolder(root);
        PresetManager manager = new PresetManager(plugin);
        ConfigPreset preset = manager.preparePreset("pack.zip");
        PresetLoadResult result = manager.loadPreset(preset);

        ConfigurationNode restored = FileUtil.loadYamlFile(config);
        assertEquals("live", restored.node("kept").getString());
        assertEquals("live", restored.node("shared").getString());
        assertEquals("from-preset", restored.node("added").getString());
        assertTrue(result.modified().contains("config.yml"));
        assertTrue(result.replaced().isEmpty());
    }

    private static void put(ZipOutputStream out, String name, String text) throws Exception {
        out.putNextEntry(new ZipEntry(name));
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }

    private static GraniteSkillsPlugin pluginWithFolder(File folder) {
        return (GraniteSkillsPlugin) Proxy.newProxyInstance(
                GraniteSkillsPlugin.class.getClassLoader(),
                new Class<?>[]{GraniteSkillsPlugin.class},
                (proxy, method, args) -> {
                    if ("getPluginFolder".equals(method.getName())) {
                        return folder;
                    }
                    if (method.isDefault()) {
                        return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
                    }
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "preset-fixture";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
