package dev.aurelium.auraskills.fabric.command;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.text.FabricGraniteText;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Shared **two-step** confirmation state for destructive admin commands (**Sprint 74**). Mirrors the
 * in-repo Bukkit {@code ConfirmManager} model: a per-subject pending string and a **30s** expiry.
 * Fabric uses an explicit {@code confirm} literal under Brigadier instead of “type the same command
 * twice”.
 *
 * <p><strong>Subject id:</strong> online {@link ServerPlayerEntity#getUuid()}; console/RCON (no
 * entity) share a single {@link #CONSOLE_SUBJECT} key so only one pending console operation exists
 * at a time (same class of operator risk as a single shell).
 */
public final class FabricCommandConfirmation {

    /** Single slot for dedicated server console + RCON (no {@link ServerCommandSource#getEntity()}). */
    public static final UUID CONSOLE_SUBJECT = new UUID(0L, 0L);

    public static final String STORAGE_PENDING_REMOVE_ALL_MODIFIERS = "storage:removeallmodifiers";

    private static final Map<UUID, String> PENDING = new ConcurrentHashMap<>();

    private FabricCommandConfirmation() {}

    public static UUID subjectKey(ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity pl) {
            return pl.getUuid();
        }
        return CONSOLE_SUBJECT;
    }

    /**
     * Stable key for {@code /storage mergeskills} preview → confirm (skill id keys, lowercased).
     */
    public static String storageMergePendingKey(Skill from, Skill to) {
        return "storage:mergeskills:"
                + from.getId().getKey().toLowerCase(Locale.ROOT)
                + ":"
                + to.getId().getKey().toLowerCase(Locale.ROOT);
    }

    /**
     * Call after a successful **preview** line (user must run the matching {@code confirm} within
     * {@link #PENDING_TIMEOUT_SEC}).
     */
    public static void registerPending(
            FabricAuraSkills plugin, ServerCommandSource source, String confirmationKey) {
        UUID id = subjectKey(source);
        PENDING.put(id, confirmationKey);
        String k = confirmationKey;
        plugin.getScheduler()
                .scheduleSync(
                        () -> {
                            if (k.equals(PENDING.get(id))) {
                                PENDING.remove(id);
                            }
                        },
                        PENDING_TIMEOUT_SEC,
                        TimeUnit.SECONDS);
    }

    private static final int PENDING_TIMEOUT_SEC = 30;

    /**
     * Verifies that {@code expectedKey} matches the pending preview for this subject. On success,
     * clears the entry (one-shot). On failure, sends an error and returns {@code false}.
     */
    public static boolean verifyPendingOrError(
            ServerCommandSource source, String expectedKey) {
        UUID id = subjectKey(source);
        String cur = PENDING.get(id);
        if (cur != null && cur.equals(expectedKey)) {
            PENDING.remove(id);
            return true;
        }
        if (cur == null) {
            source.sendError(
                    FabricGraniteText.key("auraskills.granite.command.confirmation.no_pending"));
        } else {
            source.sendError(
                    FabricGraniteText.key("auraskills.granite.command.confirmation.mismatch"));
        }
        return false;
    }
}
