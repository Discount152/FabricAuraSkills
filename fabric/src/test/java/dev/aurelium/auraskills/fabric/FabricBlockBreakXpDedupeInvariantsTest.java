package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the "exactly once" contract between the normal block-break award and the external multi-break capture.
 *
 * <p>{@code Block.dropStacks} fires for every player-broken block in the game, so the capture path sees ordinary
 * mining, silk-touch mining, crop breaks and vein mines alike. Only the end-of-tick dedupe against
 * {@code FabricXpHarvestDedupe} stops a plain one-block mine from being paid twice. That ordering cannot be exercised
 * without a server, and a regression in it would be near-invisible in play (slightly fast levelling), so the
 * invariants are pinned here instead.
 */
class FabricBlockBreakXpDedupeInvariantsTest {

    private static final Path CAPTURE = Path.of(
            "src/main/java/dev/aurelium/auraskills/fabric/FabricExternalBlockBreakXp.java");
    private static final Path BREAK_HANDLER = Path.of(
            "src/main/java/dev/aurelium/auraskills/fabric/FabricBlockBreakHandler.java");
    private static final Path CAPTURE_MIXIN = Path.of(
            "src/main/java/dev/aurelium/auraskills/fabric/mixin/BlockDropStacksMultiBreakMixin.java");
    private static final Path TREECAPITATOR = Path.of(
            "src/main/java/dev/aurelium/auraskills/fabric/FabricTreecapitator.java");

    /** The normal pipeline is the dedupe source of truth; if it stops recording, every mined block pays twice. */
    @Test
    void normalBreakAwardIsRecordedSoTheCapturePathCanSkipIt() throws IOException {
        String handler = Files.readString(BREAK_HANDLER, StandardCharsets.UTF_8);
        int award = handler.indexOf("addXp(user, skill, xpSource, xpAmt)");
        int record = handler.indexOf("FabricXpHarvestDedupe.recordBlockBreakAward(");
        assertTrue(award >= 0, "AFTER handler should award block XP");
        assertTrue(record > award, "the award must be recorded straight after it is granted");
    }

    @Test
    void captureFlushChecksDedupeBeforePayingAndRecordsAfterwards() throws IOException {
        String capture = Files.readString(CAPTURE, StandardCharsets.UTF_8);
        int check = capture.indexOf("FabricXpHarvestDedupe.wasAwardedSince(");
        int award = capture.indexOf("getLevelManager().addXp(");
        int record = capture.indexOf("FabricXpHarvestDedupe.recordBlockBreakAward(");
        assertTrue(check >= 0, "flush must consult the dedupe ledger");
        assertTrue(check < award, "dedupe must be checked before XP is granted, not after");
        assertTrue(record > award, "the capture path must record its own award so nothing re-pays it");
    }

    /**
     * Dedupe is scoped to the capture window's arm tick rather than "was this position ever paid", so breaking the
     * same position again later still awards.
     */
    @Test
    void dedupeIsScopedToTheCaptureWindowTick() throws IOException {
        String capture = Files.readString(CAPTURE, StandardCharsets.UTF_8);
        assertTrue(capture.contains("wasAwardedSince(window.world, pos, window.tick)"),
                "dedupe floor must be the window's arm tick");
    }

    /** Flushing on END_SERVER_TICK is what guarantees the AFTER handler has already had its chance to pay. */
    @Test
    void flushRunsAtEndOfTickAfterTheNormalBreakPipeline() throws IOException {
        String capture = Files.readString(CAPTURE, StandardCharsets.UTF_8);
        assertTrue(capture.contains("ServerTickEvents.END_SERVER_TICK.register"),
                "awarding must be deferred to end of tick");
        assertFalse(capture.contains("PlayerBlockBreakEvents.AFTER.register"),
                "the capture path must not award from the break event itself");
    }

    /**
     * Only the player-attributed six-argument overload is observed. The three-argument
     * {@code dropStacks(state, world, pos)} used by explosions, pistons, fluids and neighbour pops carries no player,
     * and hooking it would hand out XP for blocks nobody mined.
     */
    @Test
    void captureTargetsOnlyThePlayerAttributedDropStacksOverload() throws IOException {
        String mixin = Files.readString(CAPTURE_MIXIN, StandardCharsets.UTF_8);
        assertTrue(mixin.contains("Lnet/minecraft/entity/Entity;"),
                "target signature must include the breaking entity");
        assertTrue(mixin.contains("Lnet/minecraft/item/ItemStack;)V"),
                "target signature must be the tool-carrying overload");
        String capture = Files.readString(CAPTURE, StandardCharsets.UTF_8);
        assertTrue(capture.contains("entity instanceof ServerPlayerEntity player"),
                "capture must attribute the break to a real player");
    }

    /**
     * A leaked {@code internalBreak = true} would silently switch off all external multi-break XP, so the flag has to
     * be cleared in a {@code finally}.
     */
    @Test
    void internalBreakFlagIsGuardedAndCannotLeak() throws IOException {
        String capture = Files.readString(CAPTURE, StandardCharsets.UTF_8);
        assertTrue(capture.contains("internalBreak"), "capture must skip AuraSkills' own block removals");

        String tree = Files.readString(TREECAPITATOR, StandardCharsets.UTF_8);
        int set = tree.indexOf("setInternalBreak(true)");
        int reset = tree.indexOf("setInternalBreak(false)");
        assertTrue(set >= 0 && reset > set, "the flag must be set and then cleared");
        int finallyAt = tree.indexOf("finally", set);
        assertTrue(finallyAt > set && finallyAt < reset, "the reset must run in a finally block");
    }

    /** The Sprint 145 snapshot scanner interfered with Ore Harvester; it must not come back alongside the capture. */
    @Test
    void retiredSnapshotScannerIsGoneAndCaptureIsWired() throws IOException {
        String handler = Files.readString(BREAK_HANDLER, StandardCharsets.UTF_8);
        assertFalse(handler.contains("class FabricModCompatMultiBreakXp"),
                "the snapshot scanner must stay retired");
        String levelManager = Files.readString(
                Path.of("src/main/java/dev/aurelium/auraskills/fabric/FabricLevelManager.java"),
                StandardCharsets.UTF_8);
        assertTrue(levelManager.contains("FabricExternalBlockBreakXp.register(plugin)"),
                "the capture path must be registered at startup");
        assertFalse(levelManager.contains("FabricModCompatMultiBreakXp"),
                "the retired scanner must not still be registered");
    }
}
