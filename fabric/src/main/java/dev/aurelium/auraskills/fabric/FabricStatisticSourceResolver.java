package dev.aurelium.auraskills.fabric;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Maps YAML {@code statistic} strings (Bukkit / Granite) to vanilla {@link Stat} instances.
 * Bukkit’s {@code StatisticLeveler} only accepts {@code Statistic} enum names; Fabric supports the same
 * bare names and adds slash-typed families for registry-backed stats.
 */
public final class FabricStatisticSourceResolver {

    private FabricStatisticSourceResolver() {
    }

    /**
     * @param name from {@link dev.aurelium.auraskills.api.source.type.StatisticXpSource#getStatistic}
     */
    public static @Nullable Stat<?> resolve(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String raw = name.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        int slash = lower.indexOf('/');
        if (slash > 0) {
            String kind = lower.substring(0, slash);
            String rest = raw.substring(slash + 1).trim();
            Identifier rid = Identifier.tryParse(rest);
            if (rid == null) {
                rid = Identifier.ofVanilla(rest.toLowerCase(Locale.ROOT));
            }
            return switch (kind) {
                case "mined" -> minedStat(rid);
                case "used" -> usedStat(rid);
                case "broken" -> brokenStat(rid);
                case "dropped" -> droppedStat(rid);
                case "picked_up", "pickedup" -> pickedUpStat(rid);
                case "killed" -> entityKilledStat(rid, false);
                case "killed_by" -> entityKilledStat(rid, true);
                case "custom" -> customStat(rest.toLowerCase(Locale.ROOT).replace('-', '_'));
                default -> null;
            };
        }
        return customStat(lower);
    }

    private static Stat<?> customStat(String vanillaPath) {
        String path = vanillaPath.toLowerCase(Locale.ROOT);
        Identifier known = knownCustomId(path);
        if (known != null) {
            return Stats.CUSTOM.getOrCreateStat(known);
        }
        Identifier id = Identifier.ofVanilla(path);
        if (!Stats.CUSTOM.hasStat(id)) {
            return null;
        }
        return Stats.CUSTOM.getOrCreateStat(id);
    }

    private static @Nullable Identifier knownCustomId(String path) {
        if (path.equals("damage_dealt")) {
            return Stats.DAMAGE_DEALT;
        }
        if (path.equals("damage_taken")) {
            return Stats.DAMAGE_TAKEN;
        }
        if (path.equals("damage_dealt_absorbed")) {
            return Stats.DAMAGE_DEALT_ABSORBED;
        }
        if (path.equals("damage_dealt_resisted")) {
            return Stats.DAMAGE_DEALT_RESISTED;
        }
        if (path.equals("damage_resisted")) {
            return Stats.DAMAGE_RESISTED;
        }
        if (path.equals("damage_blocked_by_shield")) {
            return Stats.DAMAGE_BLOCKED_BY_SHIELD;
        }
        if (path.equals("mob_kills")) {
            return Stats.MOB_KILLS;
        }
        if (path.equals("player_kills")) {
            return Stats.PLAYER_KILLS;
        }
        if (path.equals("deaths")) {
            return Stats.DEATHS;
        }
        return null;
    }

    private static Stat<?> minedStat(Identifier blockId) {
        Block block = Registries.BLOCK.get(blockId);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return Stats.MINED.getOrCreateStat(block);
    }

    private static Stat<?> usedStat(Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.USED.getOrCreateStat(item);
    }

    private static Stat<?> brokenStat(Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.BROKEN.getOrCreateStat(item);
    }

    private static Stat<?> pickedUpStat(Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.PICKED_UP.getOrCreateStat(item);
    }

    private static Stat<?> droppedStat(Identifier itemId) {
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.DROPPED.getOrCreateStat(item);
    }

    private static @Nullable Stat<?> entityKilledStat(Identifier entityId, boolean killedBy) {
        RegistryEntry.Reference<EntityType<?>> entry = Registries.ENTITY_TYPE.getEntry(entityId)
                .filter(e -> e.value() != null)
                .orElse(null);
        if (entry == null) {
            return null;
        }
        EntityType<?> type = entry.value();
        if (killedBy) {
            return Stats.KILLED_BY.getOrCreateStat(type);
        }
        return Stats.KILLED.getOrCreateStat(type);
    }
}
