package dev.aurelium.auraskills.fabric.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Strips {@code auraskills:} attribute modifiers from <strong>offline</strong> player NBT, mirroring
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
        NbtCompound root = readPlayerDat(file);
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
    private static void strip21AttributesList(NbtCompound root, AtomicInteger changed) {
        NbtElement raw = root.get("attributes");
        if (!(raw instanceof NbtList attributes) || attributes.isEmpty()) {
            return;
        }
        for (NbtElement el : attributes) {
            if (!(el instanceof NbtCompound attr)) {
                continue;
            }
            NbtElement modRaw = attr.get("modifiers");
            if (!(modRaw instanceof NbtList modList) || modList.isEmpty()) {
                continue;
            }
            NbtList next = new NbtList();
            for (NbtElement m : modList) {
                if (m instanceof NbtCompound c) {
                    String id = c.getString("id", "");
                    if (!id.isEmpty() && id.startsWith("auraskills:")) {
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

    private static NbtCompound readPlayerDat(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            return NbtIo.readCompound(in, NbtSizeTracker.ofUnlimitedBytes());
        }
    }

    private static void writePlayerDat(Path file, NbtCompound root) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
            NbtIo.writeCompound(root, out);
        }
    }
}
