package dev.granite.graniteskills.fabric.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Strips {@code graniteskills:} attribute modifiers from <strong>offline</strong> player NBT, mirroring
 * the Bukkit {@code Uninstaller#removePlayer} 1.21+ path. Used by {@code /resethealth} (**Sprint
 * 57**).
 */
public final class FabricOfflinePlayerDataAuraStripping {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricOfflinePlayerDataAuraStripping.class);

    private FabricOfflinePlayerDataAuraStripping() {}

    public record Result(int offlineFilesScanned, int filesWithModifiersStripped, int readWriteErrors) {}

    /**
     * @param onlineUuids   players to skip (still loaded / online; avoid editing live playerdata
     *                      in parallel)
     */
    public static Result stripFromOfflinePlayerDataFiles(
            Path playerDataDir, Set<UUID> onlineUuids) throws IOException {
        if (!Files.isDirectory(playerDataDir)) {
            return new Result(0, 0, 0);
        }
        int successful = 0;
        int errors = 0;
        int offlineScanned = 0;
        try (var stream = Files.newDirectoryStream(playerDataDir, "*.dat")) {
            for (Path file : stream) {
                @Nullable UUID uuid = fileNameToUuid(file.getFileName().toString());
                if (uuid == null) {
                    continue;
                }
                if (onlineUuids.contains(uuid)) {
                    continue;
                }
                offlineScanned++;
                try {
                    if (stripFileIfNeeded(file)) {
                        successful++;
                    }
                } catch (Exception e) {
                    errors++;
                    LOGGER.warn("Failed to process playerdata file {}: {}", file, e.getMessage());
                }
            }
        }
        if (errors > 0) {
            LOGGER.warn(
                    "resethealth: {} offline player data file(s) had read/write errors; {} file(s) had modifiers stripped.",
                    errors,
                    successful);
        } else {
            LOGGER.info(
                    "resethealth: examined {} offline data file(s); stripped modifiers in {} file(s).",
                    offlineScanned,
                    successful);
        }
        return new Result(offlineScanned, successful, errors);
    }

    private static @Nullable UUID fileNameToUuid(String name) {
        if (!name.endsWith(".dat")) {
            return null;
        }
        String s = name.substring(0, name.length() - 4);
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean stripFileIfNeeded(Path file) throws IOException {
        CompoundTag root = readPlayerDat(file);
        if (root == null) {
            return false;
        }
        var changed = new AtomicInteger(0);
        strip21AttributesList(root, changed);
        if (changed.get() == 0) {
            return false;
        }
        writePlayerDat(file, root);
        return true;
    }

    /**
     * 1.21+ structure: list {@code "attributes"} of compounds, each with list {@code "modifiers"}.
     */
    private static void strip21AttributesList(CompoundTag root, AtomicInteger changed) {
        Tag raw = root.get("attributes");
        if (!(raw instanceof ListTag attributes) || attributes.isEmpty()) {
            return;
        }
        for (Tag el : attributes) {
            if (!(el instanceof CompoundTag attr)) {
                continue;
            }
            Tag modRaw = attr.get("modifiers");
            if (!(modRaw instanceof ListTag modList) || modList.isEmpty()) {
                continue;
            }
            ListTag next = new ListTag();
            for (Tag m : modList) {
                if (m instanceof CompoundTag c) {
                    String id = c.getStringOr("id", "");
                    if (!id.isEmpty() && id.startsWith("graniteskills:")) {
                        changed.incrementAndGet();
                        continue;
                    }
                }
                next.add(m);
            }
            if (next.isEmpty()) {
                attr.remove("modifiers");
            } else {
                attr.put("modifiers", next);
            }
        }
    }

    private static CompoundTag readPlayerDat(Path file) throws IOException {
        return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
    }

    private static void writePlayerDat(Path file, CompoundTag root) throws IOException {
        NbtIo.writeCompressed(root, file);
    }
}
