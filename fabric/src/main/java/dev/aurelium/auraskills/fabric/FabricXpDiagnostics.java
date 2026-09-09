package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Operator-safe runtime diagnostics for XP award decisions.
 */
public final class FabricXpDiagnostics {

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);

    private FabricXpDiagnostics() {}

    public static void setEnabled(boolean enabled) {
        ENABLED.set(enabled);
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static void logDecision(
            FabricAuraSkills plugin,
            String eventType,
            ServerPlayerEntity player,
            Skill skill,
            SourceType sourceType,
            @Nullable String sourceId,
            double xpValue,
            boolean awardCalled,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=" + eventType
                + " player=" + player.getName().getString()
                + " skill=" + skill.getId()
                + " source_type=" + sourceType.getId()
                + " source=" + (sourceId == null ? "-" : sourceId)
                + " xp=" + xpValue
                + " award_called=" + awardCalled
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    /**
     * Sprint 143: per-source statistic resolution outcome (null-safe resolver diagnostics).
     */
    public static void logStatisticResolution(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String sourceId,
            @Nullable String configuredStat,
            String normalizedStat,
            boolean resolved,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=statistic_resolution"
                + " player=" + player.getName().getString()
                + " source_id=" + sourceId
                + " configured_stat=" + (configuredStat == null || configuredStat.isBlank() ? "-" : configuredStat)
                + " normalized_stat=" + (normalizedStat == null || normalizedStat.isBlank() || "-".equals(normalizedStat)
                        ? "-"
                        : normalizedStat)
                + " resolved=" + resolved
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    /**
     * Sprint 131: structured fields for block / multi-break compatibility.
     */
    public static void logBlockCompat(
            FabricAuraSkills plugin,
            String eventType,
            ServerPlayerEntity player,
            Skill skill,
            SourceType sourceType,
            @Nullable String sourceId,
            double xpValue,
            boolean awardCalled,
            String reason,
            String eventPath,
            boolean multiBlockDetected,
            int blocksDetected,
            int xpAwardedBlocks,
            int cropAgeBefore,
            int cropAgeAfter,
            boolean mature,
            @Nullable String interactionPath) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=" + eventType
                + " player=" + player.getName().getString()
                + " skill=" + skill.getId()
                + " source_type=" + sourceType.getId()
                + " source=" + (sourceId == null ? "-" : sourceId)
                + " xp=" + xpValue
                + " award_called=" + awardCalled
                + " reason=" + reason
                + " event_path=" + eventPath
                + " multi_block_detected=" + multiBlockDetected
                + " blocks_detected=" + blocksDetected
                + " xp_awarded_blocks=" + xpAwardedBlocks
                + " crop_age_before=" + cropAgeBefore
                + " crop_age_after=" + cropAgeAfter
                + " mature=" + mature
                + " interaction_path=" + (interactionPath == null ? "-" : interactionPath);
        plugin.logger().info(line);
    }

    /**
     * Sprint 132: multi-break / falling-tree compensation window (Veinminer, OreHarvester, FallingTree-style removals).
     */
    public static void logMultiBreakCompat(
            FabricAuraSkills plugin,
            String eventType,
            ServerPlayerEntity player,
            Skill skill,
            SourceType sourceType,
            @Nullable String sourceId,
            double xpValue,
            boolean awardCalled,
            String reason,
            String eventPath,
            boolean multiBlockDetected,
            int snapshotCount,
            int removedCount,
            int matchedCount,
            int xpAwardedBlocks) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=" + eventType
                + " player=" + player.getName().getString()
                + " skill=" + skill.getId()
                + " source_type=" + sourceType.getId()
                + " source=" + (sourceId == null ? "-" : sourceId)
                + " xp=" + xpValue
                + " award_called=" + awardCalled
                + " reason=" + reason
                + " event_path=" + eventPath
                + " multi_block_detected=" + multiBlockDetected
                + " snapshot_count=" + snapshotCount
                + " removed_count=" + removedCount
                + " matched_count=" + matchedCount
                + " xp_awarded_blocks=" + xpAwardedBlocks;
        plugin.logger().info(line);
    }

    /**
     * Sprint 132: right-click harvest / farming resolution (pre-use captured {@link net.minecraft.block.BlockState}).
     */
    public static void logFarmingHarvestCompat(
            FabricAuraSkills plugin,
            String eventType,
            ServerPlayerEntity player,
            Identifier blockId,
            int cropAgeBefore,
            int cropAgeAfter,
            boolean mature,
            String sourceLookupKey,
            boolean sourceMatch,
            @Nullable String matchedSourceId,
            boolean awardCalled,
            String reason) {
        logFarmingHarvestCompat(
                plugin,
                eventType,
                player,
                blockId,
                cropAgeBefore,
                cropAgeAfter,
                mature,
                sourceLookupKey,
                sourceMatch,
                matchedSourceId,
                awardCalled,
                reason,
                "-",
                "-");
    }

    public static void logFarmingHarvestCompat(
            FabricAuraSkills plugin,
            String eventType,
            ServerPlayerEntity player,
            Identifier blockId,
            int cropAgeBefore,
            int cropAgeAfter,
            boolean mature,
            String sourceLookupKey,
            boolean sourceMatch,
            @Nullable String matchedSourceId,
            boolean awardCalled,
            String reason,
            String captureSource,
            String blockStateRaw) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=" + eventType
                + " player=" + player.getName().getString()
                + " block=" + blockId
                + " crop_age_before=" + cropAgeBefore
                + " crop_age_after=" + cropAgeAfter
                + " mature=" + mature
                + " source_lookup_key=" + sourceLookupKey
                + " source_match=" + sourceMatch
                + " matched_source=" + (matchedSourceId == null ? "-" : matchedSourceId)
                + " award_called=" + awardCalled
                + " capture_source=" + captureSource
                + " block_state_raw=" + blockStateRaw
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    /**
     * Sprint 131 / 132: fishing loot / source match diagnostics.
     */
    public static void logFishingCompat(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            Skill skill,
            @Nullable String sourceId,
            double xpValue,
            boolean awardCalled,
            String reason,
            boolean lootModified,
            boolean sourceMatch,
            @Nullable String detailReason,
            @Nullable String caughtStackId,
            @Nullable String sourceLookupKey,
            boolean fallbackUsed) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=fishing_bobber_use"
                + " player=" + player.getName().getString()
                + " skill=" + skill.getId()
                + " source_type=fishing"
                + " source=" + (sourceId == null ? "-" : sourceId)
                + " matched_source=" + (sourceId == null ? "-" : sourceId)
                + " xp=" + xpValue
                + " award_called=" + awardCalled
                + " reason=" + reason
                + " loot_modified=" + lootModified
                + " source_match=" + sourceMatch
                + " fallback_used=" + fallbackUsed
                + " caught_stack=" + (caughtStackId == null ? "-" : caughtStackId)
                + " source_lookup_key=" + (sourceLookupKey == null ? "-" : sourceLookupKey)
                + " detail=" + (detailReason == null ? "-" : detailReason);
        plugin.logger().info(line);
    }

    /** Sprint 133: block break hook entered before source resolution. */
    public static void logBlockBreakEntry(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            boolean sneaking,
            String blockBefore,
            @Nullable String blockAfter,
            String pos,
            String callback) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=block_break_entry"
                + " player=" + player.getName().getString()
                + " sneaking=" + sneaking
                + " block_before=" + blockBefore
                + " block_after=" + (blockAfter == null ? "-" : blockAfter)
                + " pos=" + pos
                + " callback=" + callback;
        plugin.logger().info(line);
    }

    /** Sprint 133: multi-break snapshot path entered (Veinminer / OreHarvester). */
    public static void logMultiBreakEntry(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            boolean sneaking,
            String initialBlock,
            String pos,
            boolean snapshotAttempted,
            int snapshotCount,
            String callback) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=multi_break_entry"
                + " player=" + player.getName().getString()
                + " sneaking=" + sneaking
                + " initial_block=" + initialBlock
                + " pos=" + pos
                + " snapshot_attempted=" + snapshotAttempted
                + " snapshot_count=" + snapshotCount
                + " callback=" + callback;
        plugin.logger().info(line);
    }

    /** Sprint 133: foraging multi-break (FallingTree-style) entry. */
    public static void logFallingTreeEntry(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            boolean sneaking,
            String initialBlock,
            String pos,
            boolean snapshotAttempted,
            int snapshotCount,
            String callback) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=falling_tree_entry"
                + " player=" + player.getName().getString()
                + " skill=foraging"
                + " sneaking=" + sneaking
                + " initial_block=" + initialBlock
                + " pos=" + pos
                + " snapshot_attempted=" + snapshotAttempted
                + " snapshot_count=" + snapshotCount
                + " callback=" + callback;
        plugin.logger().info(line);
    }

    /** Sprint 133 / 136: bobber {@code use} HEAD — peek-only stack (often empty before loot spawn). */
    public static void logFishingEntryHead(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String hook,
            @Nullable String caughtStack,
            @Nullable String caughtEntity) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=fishing_entry"
                + " player=" + player.getName().getString()
                + " hook=" + hook
                + " phase=head"
                + " caught_stack=" + (caughtStack == null ? "-" : caughtStack)
                + " caught_entity=" + (caughtEntity == null ? "-" : caughtEntity);
        plugin.logger().info(line);
    }

    /**
     * Sprint 136: bobber {@code use} RETURN — full fishing XP resolution outcome (one line per path when xpdiag is on).
     */
    public static void logFishingEntryResult(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String hook,
            @Nullable String caughtStack,
            @Nullable String caughtEntity,
            @Nullable String sourceLookupKey,
            boolean sourceMatch,
            @Nullable String matchedSource,
            boolean fallbackUsed,
            boolean awardCalled,
            @Nullable String reason,
            boolean lootModified) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=fishing_entry"
                + " player=" + player.getName().getString()
                + " hook=" + hook
                + " caught_stack=" + (caughtStack == null || caughtStack.isEmpty() ? "-" : caughtStack)
                + " caught_entity=" + (caughtEntity == null ? "-" : caughtEntity)
                + " source_lookup_key=" + (sourceLookupKey == null ? "-" : sourceLookupKey)
                + " source_match=" + sourceMatch
                + " matched_source=" + (matchedSource == null ? "-" : matchedSource)
                + " fallback_used=" + fallbackUsed
                + " award_called=" + awardCalled
                + " reason=" + (reason == null ? "-" : reason)
                + " loot_modified=" + lootModified;
        plugin.logger().info(line);
    }

    /** Sprint 133: right-click farming interaction harvest path entered. */
    public static void logFarmingEntry(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            boolean sneaking,
            String blockBefore,
            String pos,
            String callback) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=farming_entry"
                + " player=" + player.getName().getString()
                + " sneaking=" + sneaking
                + " block_before=" + blockBefore
                + " pos=" + pos
                + " callback=" + callback;
        plugin.logger().info(line);
    }

    /** Sprint 133: excavation skill block break entered. */
    public static void logExcavationEntry(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            boolean sneaking,
            String blockBefore,
            String pos,
            String callback) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=excavation_entry"
                + " player=" + player.getName().getString()
                + " sneaking=" + sneaking
                + " block_before=" + blockBefore
                + " pos=" + pos
                + " callback=" + callback;
        plugin.logger().info(line);
    }

    /**
     * Sprint 133: end of multi-break / falling-tree compensation window (one line per broken cluster).
     */
    /**
     * @deprecated Prefer {@link #logMultiBreakWindow(FabricAuraSkills, ServerPlayerEntity, Skill, String, int, int, int, int, int, int, String)} with skip counts.
     */
    @Deprecated
    public static void logMultiBreakWindow(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            Skill skill,
            String initialBlock,
            int snapshotCount,
            int removedCount,
            int matchedCount,
            int xpAwardedBlocks,
            String reason) {
        logMultiBreakWindow(
                plugin,
                player,
                skill,
                initialBlock,
                snapshotCount,
                removedCount,
                matchedCount,
                xpAwardedBlocks,
                0,
                0,
                reason);
    }

    /** Sprint 137: multi-break window with compensation skip instrumentation. */
    public static void logMultiBreakWindow(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            Skill skill,
            String initialBlock,
            int snapshotCount,
            int removedCount,
            int matchedCount,
            int xpAwardedBlocks,
            int dedupeSkippedCount,
            int placedBlockSkippedCount,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=multi_break_window"
                + " player=" + player.getName().getString()
                + " skill=" + skill.getId()
                + " initial_block=" + initialBlock
                + " snapshot_count=" + snapshotCount
                + " removed_count=" + removedCount
                + " matched_count=" + matchedCount
                + " xp_awarded_blocks=" + xpAwardedBlocks
                + " dedupe_skipped_count=" + dedupeSkippedCount
                + " placed_block_skipped_count=" + placedBlockSkippedCount
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    /**
     * Sprint 137: single-line source resolution failure (no vague {@code checks_passed}/{@code source=-}
     * substitutions for “why no XP”).
     */
    public static void logSourceResolution(
            FabricAuraSkills plugin,
            String event,
            ServerPlayerEntity player,
            @Nullable Skill skill,
            SourceType sourceType,
            boolean sourceMatch,
            @Nullable String matchedSourceId,
            double xpPreview,
            boolean awardCalled,
            String sharedGateResult,
            String runtimeContext,
            String normalizedLookupKeys,
            int candidateSourceCount,
            String candidateSourceIdsCsv,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=" + event
                + " player=" + player.getName().getString()
                + " skill=" + (skill != null ? skill.getId() : "-")
                + " source_type=" + sourceType.getId()
                + " runtime_context=" + runtimeContext
                + " normalized_lookup_keys=" + normalizedLookupKeys
                + " candidate_source_count=" + candidateSourceCount
                + " candidate_source_ids=" + candidateSourceIdsCsv
                + " source_match=" + sourceMatch
                + " matched_source=" + (matchedSourceId == null ? "-" : matchedSourceId)
                + " xp=" + xpPreview
                + " shared_gate_result=" + sharedGateResult
                + " award_called=" + awardCalled
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    /** Sprint 137: proves {@link FabricLevelManager#addXp} mutates in-memory skill XP / level when diagnostics are on. */
    public static void logAddXpMutation(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            Skill skill,
            @Nullable String sourceId,
            @Nullable SourceType sourceType,
            double amountRequested,
            double beforeSkillXp,
            double afterSkillXp,
            int beforeLevel,
            int afterLevel,
            boolean stateMutated,
            @Nullable String saveCalled,
            @Nullable String feedbackCalled) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=addxp_mutation"
                + " player=" + player.getName().getString()
                + " skill=" + skill.getId()
                + " source_type=" + (sourceType == null ? "-" : sourceType.getId())
                + " source=" + (sourceId == null ? "-" : sourceId)
                + " amount_requested=" + amountRequested
                + " before_skill_xp=" + beforeSkillXp
                + " after_skill_xp=" + afterSkillXp
                + " before_level=" + beforeLevel
                + " after_level=" + afterLevel
                + " state_mutated=" + stateMutated
                + " save_called=" + (saveCalled == null ? "-" : saveCalled)
                + " feedback_called=" + (feedbackCalled == null ? "-" : feedbackCalled);
        plugin.logger().info(line);
    }

    /** Sprint 140: compare working-vs-broken sibling sources to isolate mechanism delta. */
    public static void logXpSourceCompare(
            FabricAuraSkills plugin,
            String category,
            String workingReference,
            String brokenSource,
            String difference) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=xp_source_compare"
                + " category=" + category
                + " working_reference=" + workingReference
                + " broken_source=" + brokenSource
                + " difference=" + difference;
        plugin.logger().info(line);
    }

    public static void logCropCaptureAttempt(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String captureSource,
            String beforeOrAfterMutation,
            boolean rightClickHarvestPresent) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=crop_capture_attempt"
                + " player=" + player.getName().getString()
                + " capture_source=" + captureSource
                + " before_or_after_mutation=" + beforeOrAfterMutation
                + " rightclickharvest_present=" + rightClickHarvestPresent;
        plugin.logger().info(line);
    }

    public static void logCropOrderProbe(
            FabricAuraSkills plugin,
            String probe,
            long sequence,
            ServerPlayerEntity player,
            BlockPos pos,
            String blockStateRaw,
            int age,
            String hand,
            String item,
            boolean rightClickHarvestPresent) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=crop_order_probe"
                + " probe=" + probe
                + " sequence=" + sequence
                + " player=" + player.getName().getString()
                + " pos=" + pos.getX() + "," + pos.getY() + "," + pos.getZ()
                + " block_state_raw=" + blockStateRaw
                + " age=" + age
                + " hand=" + hand
                + " item=" + item
                + " rightclickharvest_present=" + rightClickHarvestPresent;
        plugin.logger().info(line);
    }

    public static void logAgilityStatDelta(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String sourceId,
            String statId,
            int beforeStat,
            int afterStat,
            int delta,
            int storedRemainder,
            int threshold,
            int convertedUnits,
            boolean sourceMatch,
            boolean awardCalled,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String statShort = statId;
        int colon = statShort.indexOf(':');
        if (colon >= 0 && colon < statShort.length() - 1) {
            statShort = statShort.substring(colon + 1);
        }
        String line = "[XPDIAG] event=agility_stat_delta"
                + " player=" + player.getName().getString()
                + " source_id=" + sourceId
                + " stat_id=" + statId
                + " stat=" + statShort
                + " before_stat=" + beforeStat
                + " after_stat=" + afterStat
                + " before=" + beforeStat
                + " after=" + afterStat
                + " delta=" + delta
                + " stored_remainder=" + storedRemainder
                + " remainder=" + storedRemainder
                + " threshold=" + threshold
                + " converted_units=" + convertedUnits
                + " source_match=" + sourceMatch
                + " award_called=" + awardCalled
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    public static void logAgilityJumpDelta(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String sourceId,
            int beforeJumps,
            int afterJumps,
            int delta,
            int storedRemainder,
            int threshold,
            boolean awardCalled,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=agility_jump_delta"
                + " player=" + player.getName().getString()
                + " source_id=" + sourceId
                + " before_jumps=" + beforeJumps
                + " after_jumps=" + afterJumps
                + " delta=" + delta
                + " stored_remainder=" + storedRemainder
                + " threshold=" + threshold
                + " award_called=" + awardCalled
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    public static void logAgilityJumpProgress(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            String sourceId,
            int jumpCount,
            int threshold,
            boolean awardCalled,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=agility_jump_progress"
                + " player=" + player.getName().getString()
                + " source_id=" + sourceId
                + " jump_count=" + jumpCount
                + " threshold=" + threshold
                + " award_called=" + awardCalled
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    /**
     * Emits once per server enable so operators can grep proof without toggling {@link #setEnabled}.
     */
    public static void logAgilitySourcesResolved(FabricAuraSkills plugin, String statisticSourcesCsv, String jumpSourcesCsv) {
        plugin.logger().info("[XPDIAG] agility_sources_resolved statistic_sources=[" + statisticSourcesCsv
                + "] jump_sources=[" + jumpSourcesCsv + "]");
    }

    public static void logMultiBreakLifecycle(
            FabricAuraSkills plugin,
            String phase,
            ServerPlayerEntity player,
            boolean sneaking,
            String initialBlock,
            int snapshotCount,
            int removedCount,
            int matchedCount,
            int xpAwardedBlocks,
            int dedupeSkippedCount,
            int placedBlockSkippedCount,
            int fallbackDetectedTotal,
            String reason) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=multi_break_lifecycle"
                + " phase=" + phase
                + " player=" + player.getName().getString()
                + " sneaking=" + sneaking
                + " initial_block=" + initialBlock
                + " snapshot_count=" + snapshotCount
                + " removed_count=" + removedCount
                + " matched_count=" + matchedCount
                + " xp_awarded_blocks=" + xpAwardedBlocks
                + " dedupe_skipped_count=" + dedupeSkippedCount
                + " placed_block_skipped_count=" + placedBlockSkippedCount
                + " fallback_detected=" + fallbackDetectedTotal
                + " reason=" + reason;
        plugin.logger().info(line);
    }

    public static void logMultiBreakCompare(
            FabricAuraSkills plugin,
            String difference) {
        if (!ENABLED.get()) {
            return;
        }
        String line = "[XPDIAG] event=multi_break_compare"
                + " reference=foraging_fallingtree"
                + " target=mining_veinmine"
                + " difference=" + difference;
        plugin.logger().info(line);
    }
}
