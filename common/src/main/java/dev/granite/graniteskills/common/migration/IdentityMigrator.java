package dev.granite.graniteskills.common.migration;

import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.util.file.FileUtil;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves worlds and player files saved under the AuraSkills and AureliumSkills folder names
 * into the Granite Skills data folder, and rewrites stored ids.
 */
public final class IdentityMigrator {

    private IdentityMigrator() {
    }

    public static void migrate(GraniteSkillsPlugin plugin) {
        try {
            for (String line : migrateFolder(plugin.getPluginFolder().toPath())) {
                plugin.logger().info(line);
            }
        } catch (Exception e) {
            plugin.logger().severe("Could not migrate AuraSkills data into the Granite Skills folder", e);
        }
    }

    static List<String> migrateFolder(Path pluginFolder) throws IOException {
        List<String> messages = new ArrayList<>();
        Files.createDirectories(pluginFolder);
        Path parent = pluginFolder.getParent();
        if (parent == null) {
            return messages;
        }
        Path marker = pluginFolder.resolve(LegacyIdentity.MARKER_FILE);
        List<Path> oldFolders = oldFolders(parent, pluginFolder);
        if (Files.exists(marker)) {
            if (!oldFolders.isEmpty()) {
                messages.add("Granite Skills data is already migrated. Old folders are still present and were left in place: " + names(oldFolders));
            }
            return messages;
        }
        boolean fresh = !hasUserdata(pluginFolder);
        for (Path oldFolder : oldFolders) {
            int copied = copyTree(oldFolder, pluginFolder, fresh);
            messages.add("Migrated " + copied + " files from " + oldFolder.getFileName() + " into " + pluginFolder.getFileName() + ". The old folder was left in place.");
        }
        int rewritten = rewriteYamlTree(pluginFolder);
        if (rewritten > 0) {
            messages.add("Rewrote AuraSkills ids in " + rewritten + " data files.");
        }
        if (oldFolders.isEmpty()) {
            messages.add("No AuraSkills or AureliumSkills data folder was found next to " + pluginFolder.getFileName() + ".");
        }
        Files.writeString(marker, "migrated\n");
        return messages;
    }

    private static List<Path> oldFolders(Path parent, Path pluginFolder) throws IOException {
        List<Path> found = new ArrayList<>();
        Path target = pluginFolder.toAbsolutePath().normalize();
        for (String name : LegacyIdentity.OLD_FOLDER_NAMES) {
            Path oldFolder = parent.resolve(name);
            if (!Files.isDirectory(oldFolder)) {
                continue;
            }
            Path source = oldFolder.toAbsolutePath().normalize();
            if (source.equals(target) || target.startsWith(source) || source.startsWith(target)) {
                continue;
            }
            found.add(oldFolder);
        }
        return found;
    }

    private static boolean hasUserdata(Path pluginFolder) throws IOException {
        Path userdata = pluginFolder.resolve("userdata");
        if (!Files.isDirectory(userdata)) {
            return false;
        }
        try (var stream = Files.list(userdata)) {
            return stream.anyMatch(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".yml"));
        }
    }

    private static int copyTree(Path source, Path target, boolean overwrite) throws IOException {
        int copied = 0;
        try (var walk = Files.walk(source)) {
            List<Path> paths = walk.toList();
            for (Path path : paths) {
                Path dest = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                    continue;
                }
                if (LegacyIdentity.MARKER_FILE.equals(path.getFileName().toString())) {
                    continue;
                }
                if (!overwrite && Files.exists(dest)) {
                    continue;
                }
                Files.createDirectories(dest.getParent());
                if (overwrite) {
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(path, dest);
                }
                copied++;
            }
        }
        return copied;
    }

    private static int rewriteYamlTree(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return 0;
        }
        int rewritten = 0;
        try (var walk = Files.walk(root)) {
            List<Path> files = walk.filter(IdentityMigrator::isYaml).toList();
            for (Path file : files) {
                if (rewriteYamlFile(file)) {
                    rewritten++;
                }
            }
        }
        return rewritten;
    }

    private static boolean isYaml(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String name = path.getFileName().toString();
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    private static boolean rewriteYamlFile(Path file) throws IOException {
        ConfigurationNode root = FileUtil.loadYamlFile(file.toFile());
        boolean changed;
        try {
            changed = rewriteNode(root);
        } catch (SerializationException e) {
            throw new IOException("Could not rewrite " + file, e);
        }
        if (changed) {
            FileUtil.saveYamlFile(file.toFile(), root);
        }
        return changed;
    }

    private static boolean rewriteNode(ConfigurationNode node) throws SerializationException {
        if (node.isList()) {
            boolean changed = false;
            for (ConfigurationNode child : node.childrenList()) {
                changed |= rewriteNode(child);
            }
            return changed;
        }
        if (!node.isMap()) {
            Object raw = node.raw();
            if (raw instanceof String value) {
                String rewritten = LegacyIdentity.rewriteStoredString(value);
                if (!rewritten.equals(value)) {
                    node.set(rewritten);
                    return true;
                }
            }
            return false;
        }
        boolean changed = false;
        List<Object> keys = new ArrayList<>(node.childrenMap().keySet());
        for (Object key : keys) {
            ConfigurationNode child = node.node(key);
            changed |= rewriteNode(child);
            if (!(key instanceof String name)) {
                continue;
            }
            String rewritten = LegacyIdentity.rewriteStoredString(name);
            if (rewritten.equals(name)) {
                continue;
            }
            ConfigurationNode destination = node.node(rewritten);
            if (destination.virtual()) {
                destination.from(child);
            }
            node.node(name).set(null);
            changed = true;
        }
        return changed;
    }

    private static String names(List<Path> folders) {
        List<String> names = new ArrayList<>();
        for (Path folder : folders) {
            names.add(folder.getFileName().toString());
        }
        return String.join(", ", names);
    }

}
