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
 * <p>
 * Sprint 143: all paths are null-safe — never passes a null {@link Identifier} into {@code Stats.*} APIs.
 * Sprint 144: Agility movement sources keep AuraSkills ids ({@code walk_per_meter}, …) while resolving
 * {@code minecraft:walk_one_cm} etc.; {@link Identifier#of(String, String)} is used for bare custom paths so
 * {@code Identifier.tryParse("walk_one_cm")} never yields a null-producing lookup.
 * Sprint 145: distance CUSTOM stats prefer vanilla {@link Stats#WALK_ONE_CM} / {@link Stats#SPRINT_ONE_CM} /
 * {@link Stats#SWIM_ONE_CM} identifiers so startup resolution matches the game registry.
 */
public final class FabricStatisticSourceResolver {

    private FabricStatisticSourceResolver() {
    }

    /**
     * AuraSkills source keys (namespace key only) → vanilla custom stat path (minecraft namespace).
     * YAML {@code statistic:} may still list {@code walk_one_cm}; this table ensures resolution even if the
     * configured token is missing or wrong.
     */
    public static @Nullable String agilityVanillaPathForSourceKey(@Nullable String sourceIdKey) {
        if (sourceIdKey == null || sourceIdKey.isBlank()) {
            return null;
        }
        return switch (sourceIdKey.toLowerCase(Locale.ROOT)) {
            case "walk_per_meter" -> "walk_one_cm";
            case "sprint_per_meter" -> "sprint_one_cm";
            case "swim_per_meter" -> "swim_one_cm";
            default -> null;
        };
    }

    /**
     * Effective token for Agility statistic XP: applies {@link #agilityVanillaPathForSourceKey} first, else YAML.
     */
    public static String effectiveAgilityStatisticToken(@Nullable String yamlStatistic, String sourceIdKey) {
        String aliased = agilityVanillaPathForSourceKey(sourceIdKey);
        if (aliased != null) {
            return aliased;
        }
        return yamlStatistic == null ? "" : yamlStatistic;
    }

    /**
     * Resolves a statistic for Agility {@link StatisticXpSource} rows (source id + YAML statistic).
     */
    public static @Nullable Stat<?> resolveForAgility(@Nullable String yamlStatistic, String sourceIdKey) {
        String effective = effectiveAgilityStatisticToken(yamlStatistic, sourceIdKey);
        if (effective.isBlank()) {
            return null;
        }
        try {
            return resolveInner(effective.trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Same as {@link #resolveForAgility} with structured failure reasons.
     */
    public static StatResolveResult resolveDetailedForAgility(@Nullable String yamlStatistic, String sourceIdKey) {
        String effective = effectiveAgilityStatisticToken(yamlStatistic, sourceIdKey);
        if (effective == null || effective.isBlank()) {
            return new StatResolveResult(null, "-", "null_stat_identifier");
        }
        String trimmed = effective.trim();
        String normalizedDisplay = normalizeStatisticToken(stripStatisticPrefixes(trimmed));
        if (normalizedDisplay.isEmpty()) {
            return new StatResolveResult(null, "-", "blank_stat_identifier");
        }
        try {
            Stat<?> stat = resolveInner(trimmed);
            if (stat == null) {
                return new StatResolveResult(null, normalizedDisplay, "invalid_stat_identifier");
            }
            return new StatResolveResult(stat, normalizedDisplay, "ok");
        } catch (RuntimeException e) {
            return new StatResolveResult(null, normalizedDisplay.isEmpty() ? "-" : normalizedDisplay,
                    "exception:" + e.getClass().getSimpleName());
        }
    }

    /**
     * @param name from {@link dev.aurelium.auraskills.api.source.type.StatisticXpSource#getStatistic}
     * @return resolved stat, or {@code null} if unknown/invalid; never throws
     */
    public static @Nullable Stat<?> resolve(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return resolveInner(name);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Same as {@link #resolve} but records why resolution failed (for diagnostics only).
     */
    public static StatResolveResult resolveDetailed(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return new StatResolveResult(null, "-", "null_stat_identifier");
        }
        String normalizedDisplay = normalizeStatisticToken(stripStatisticPrefixes(name.trim()));
        if (normalizedDisplay.isEmpty()) {
            return new StatResolveResult(null, "-", "blank_stat_identifier");
        }
        try {
            Stat<?> stat = resolveInner(name);
            if (stat == null) {
                return new StatResolveResult(null, normalizedDisplay, "invalid_stat_identifier");
            }
            return new StatResolveResult(stat, normalizedDisplay, "ok");
        } catch (RuntimeException e) {
            return new StatResolveResult(null, normalizedDisplay.isEmpty() ? "-" : normalizedDisplay,
                    "exception:" + e.getClass().getSimpleName());
        }
    }

    public record StatResolveResult(@Nullable Stat<?> stat, String normalizedToken, String reason) {
        public boolean resolved() {
            return stat != null;
        }
    }

    private static @Nullable Stat<?> resolveInner(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String raw = stripStatisticPrefixes(trimmed);
        if (raw.isBlank()) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        int slash = lower.indexOf('/');
        if (slash > 0) {
            String kind = lower.substring(0, slash);
            String rest = raw.substring(slash + 1).trim();
            if (rest.isEmpty()) {
                return null;
            }
            Identifier rid = Identifier.tryParse(rest);
            if (rid == null) {
                try {
                    rid = Identifier.ofVanilla(rest.toLowerCase(Locale.ROOT));
                } catch (RuntimeException e) {
                    return null;
                }
            }
            if (rid == null) {
                return null;
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
        return customStat(normalizeStatisticToken(lower));
    }

    /**
     * Accepts {@code walk_one_cm}, {@code minecraft:walk_one_cm}, {@code stat.minecraft.walk_one_cm}, etc.
     */
    private static String stripStatisticPrefixes(String raw) {
        String t = raw.trim();
        String low = t.toLowerCase(Locale.ROOT);
        if (low.startsWith("stat.minecraft.")) {
            return t.substring("stat.minecraft.".length());
        }
        if (low.startsWith("minecraft:")) {
            return t.substring("minecraft:".length());
        }
        return t;
    }

    static String normalizeStatisticToken(String configured) {
        if (configured == null) {
            return "";
        }
        String raw = configured.trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) {
            return "";
        }
        Identifier parsed = Identifier.tryParse(raw);
        if (parsed != null) {
            String p = parsed.getPath();
            return p != null ? p : "";
        }
        return raw;
    }

    private static @Nullable Stat<?> customStat(@Nullable String vanillaPath) {
        if (vanillaPath == null || vanillaPath.isBlank()) {
            return null;
        }
        String path = normalizeStatisticToken(vanillaPath);
        if (path.isBlank()) {
            return null;
        }
        Stat<?> vanillaDistance = vanillaDistanceMovementStat(path);
        if (vanillaDistance != null) {
            return vanillaDistance;
        }
        Identifier id = minecraftCustomStatIdentifier(path);
        if (id == null || !isValidIdentifierPath(id)) {
            return null;
        }
        try {
            return Stats.CUSTOM.getOrCreateStat(id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Sprint 145: distance CUSTOM stats must use vanilla {@link Stats} identifier constants (e.g.
     * {@link Stats#WALK_ONE_CM}). Dynamic {@link Identifier#of} alone can fail registry alignment during early load,
     * yielding {@code invalid_stat_identifier} at startup even for valid paths.
     */
    private static @Nullable Stat<?> vanillaDistanceMovementStat(String pathNormalizedLowercase) {
        Identifier statId = switch (pathNormalizedLowercase) {
            case "walk_one_cm" -> Stats.WALK_ONE_CM;
            case "sprint_one_cm" -> Stats.SPRINT_ONE_CM;
            case "swim_one_cm" -> Stats.SWIM_ONE_CM;
            default -> null;
        };
        if (statId == null) {
            return null;
        }
        try {
            return Stats.CUSTOM.getOrCreateStat(statId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Builds a guaranteed non-null {@code minecraft} {@link Identifier} for CUSTOM stats.
     * Bare paths ({@code walk_one_cm}) use {@link Identifier#of(String, String)} — not {@code tryParse} alone,
     * which often returns null for unqualified paths and led to null ids in {@code getOrCreateStat}.
     */
    private static @Nullable Identifier minecraftCustomStatIdentifier(String pathOrNamespaced) {
        if (pathOrNamespaced == null || pathOrNamespaced.isBlank()) {
            return null;
        }
        String trimmed = pathOrNamespaced.trim().toLowerCase(Locale.ROOT);
        Identifier tryFull = Identifier.tryParse(trimmed);
        if (tryFull != null && isValidIdentifierPath(tryFull)) {
            if ("minecraft".equals(tryFull.getNamespace())) {
                return tryFull;
            }
            return tryFull;
        }
        Identifier known = knownCustomId(trimmed);
        if (known != null) {
            return known;
        }
        known = knownCustomId(normalizeStatisticToken(trimmed));
        if (known != null) {
            return known;
        }
        String barePath = normalizeStatisticToken(trimmed);
        if (barePath == null || barePath.isBlank()) {
            return null;
        }
        try {
            Identifier id = Identifier.of("minecraft", barePath);
            return isValidIdentifierPath(id) ? id : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean isValidIdentifierPath(Identifier id) {
        if (id == null) {
            return false;
        }
        try {
            String p = id.getPath();
            return p != null && !p.isBlank();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static @Nullable Identifier knownCustomId(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
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
        if (blockId == null) {
            return null;
        }
        Block block = Registries.BLOCK.get(blockId);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return Stats.MINED.getOrCreateStat(block);
    }

    private static Stat<?> usedStat(Identifier itemId) {
        if (itemId == null) {
            return null;
        }
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.USED.getOrCreateStat(item);
    }

    private static Stat<?> brokenStat(Identifier itemId) {
        if (itemId == null) {
            return null;
        }
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.BROKEN.getOrCreateStat(item);
    }

    private static Stat<?> pickedUpStat(Identifier itemId) {
        if (itemId == null) {
            return null;
        }
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.PICKED_UP.getOrCreateStat(item);
    }

    private static Stat<?> droppedStat(Identifier itemId) {
        if (itemId == null) {
            return null;
        }
        Item item = Registries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return Stats.DROPPED.getOrCreateStat(item);
    }

    private static @Nullable Stat<?> entityKilledStat(Identifier entityId, boolean killedBy) {
        if (entityId == null) {
            return null;
        }
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
