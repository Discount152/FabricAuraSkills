package dev.aurelium.auraskills.fabric;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 146 — XP for every block a multi-break mod removes.
 *
 * <p>Ore Harvester cancels {@code PlayerBlockBreakEvents.BEFORE} and removes the whole vein itself, so the runtime
 * behaviour can only be proven on a dedicated server. What is checkable here: the shipped defaults, the reason
 * classification the {@code xpdiag} lifecycle line reports, the block cap clamp, and — most importantly — that the
 * capture path is read-only, since Sprint 145 had to disable the previous scanner for interfering with the vein mine.
 */
class FabricS146ExternalMultiBreakXpTest {

    private static final Path CAPTURE_SOURCE = Path.of(
            "src/main/java/dev/aurelium/auraskills/fabric/FabricExternalBlockBreakXp.java");
    private static final Path CAPTURE_MIXIN = Path.of(
            "src/main/java/dev/aurelium/auraskills/fabric/mixin/BlockDropStacksMultiBreakMixin.java");

    @Test
    void embeddedConfig_enablesMultiBreakXpWithABlockCap() throws IOException {
        String text;
        try (InputStream in = FabricS146ExternalMultiBreakXpTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(in, "config.yml should be on fabric classpath (common resources merged)");
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        int i = text.indexOf("multi_break_xp:");
        assertTrue(i >= 0, "config should define multi_break_xp");
        String slice = text.substring(i, Math.min(i + 220, text.length()));
        assertTrue(slice.contains("enabled: true"), "multi_break_xp.enabled should default to true:\n" + slice);
        assertTrue(slice.contains("max_blocks:"), "multi_break_xp.max_blocks should be documented:\n" + slice);
    }

    @Test
    void captureMixin_isReadOnlyAndCannotInterfereWithTheHarvestingMod() throws IOException {
        String mixin = Files.readString(CAPTURE_MIXIN, StandardCharsets.UTF_8);
        assertTrue(mixin.contains("@At(\"HEAD\")"), "capture must observe at HEAD");
        assertFalse(mixin.contains("cancellable"), "capture must not be cancellable");
        assertFalse(mixin.contains("CallbackInfoReturnable"), "capture must not change the return value");
        assertFalse(mixin.contains("@Redirect"), "capture must not redirect calls");
        assertFalse(mixin.contains("@Overwrite"), "capture must not overwrite vanilla behaviour");
        assertFalse(mixin.contains("setBlockState"), "capture must not mutate the world");
    }

    @Test
    void capture_armsFromTryBreakBlockNotFromTheCancellableBeforeEvent() throws IOException {
        String capture = Files.readString(CAPTURE_SOURCE, StandardCharsets.UTF_8);
        // PlayerBlockBreakEvents.BEFORE short-circuits on the first listener returning false, so Ore Harvester
        // can hide the break from any listener registered after it.
        assertFalse(capture.contains("PlayerBlockBreakEvents.BEFORE.register"),
                "arming must not depend on the short-circuiting BEFORE event");
        String armSite = Files.readString(
                Path.of("src/main/java/dev/aurelium/auraskills/fabric/mixin/"
                        + "ServerPlayerInteractionManagerTryBreakPreCaptureMixin.java"),
                StandardCharsets.UTF_8);
        assertTrue(armSite.contains("FabricExternalBlockBreakXp.arm("),
                "tryBreakBlock HEAD should open the capture window");
    }

    @Test
    void maxBlocks_fallsBackWhenUnsetAndIsCeilinged() {
        assertEquals(128, FabricExternalBlockBreakXp.clampMaxBlocks(0), "missing key falls back to 128");
        assertEquals(128, FabricExternalBlockBreakXp.clampMaxBlocks(-5), "negative falls back to 128");
        assertEquals(1, FabricExternalBlockBreakXp.clampMaxBlocks(1));
        assertEquals(64, FabricExternalBlockBreakXp.clampMaxBlocks(64));
        assertEquals(1024, FabricExternalBlockBreakXp.clampMaxBlocks(999999), "runaway veins are capped");
    }

    @Test
    void lifecycleReason_separatesDedupeFromBlocked() {
        // A plain single-block mine: captured, matched, then skipped because the AFTER handler already paid it.
        assertEquals("already_awarded_by_break_event",
                FabricExternalBlockBreakXp.reasonFromCounts(1, 1, 0, 1));
        // A vein where the primary was paid normally and the extra ores were paid here.
        assertEquals("xp_awarded", FabricExternalBlockBreakXp.reasonFromCounts(6, 6, 5, 1));
        assertEquals("matched_but_blocked_or_placed",
                FabricExternalBlockBreakXp.reasonFromCounts(5, 3, 0, 0));
        assertEquals("removals_not_source_matched", FabricExternalBlockBreakXp.reasonFromCounts(5, 0, 0, 0));
        assertEquals("no_removals_observed_in_window", FabricExternalBlockBreakXp.reasonFromCounts(0, 0, 0, 0));
    }

    @Test
    void treecapitatorNoXpPath_isExcludedFromCapture() throws IOException {
        String tree = Files.readString(
                Path.of("src/main/java/dev/aurelium/auraskills/fabric/FabricTreecapitator.java"),
                StandardCharsets.UTF_8);
        assertTrue(tree.contains("FabricExternalBlockBreakXp.setInternalBreak(true)"),
                "AuraSkills' own no-XP break must not be re-awarded by the capture path");
    }
}
