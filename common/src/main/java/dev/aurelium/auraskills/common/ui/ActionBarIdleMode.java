package dev.aurelium.auraskills.common.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * How the health/mana <strong>idle</strong> action bar is shown.
 */
public enum ActionBarIdleMode {
    /**
     * Show only in short windows on relevant change (or join/world/combat), then stop sending
     * packets so other action bar text can appear.
     */
    SMART,
    /**
     * Legacy-style: keep updating while the idle line is not paused by other action bar traffic.
     */
    ALWAYS,
    /**
     * Never show Granite's idle health/mana on the action bar.
     */
    OFF;

    /** Short label for chat (e.g. status line). */
    public @NotNull String displayName() {
        return switch (this) {
            case SMART -> "Smart";
            case ALWAYS -> "Always";
            case OFF -> "Off";
        };
    }

    public static @NotNull ActionBarIdleMode fromStringOr(@Nullable String raw, @NotNull ActionBarIdleMode ifMissing) {
        if (raw == null) {
            return ifMissing;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "smart" -> SMART;
            case "always" -> ALWAYS;
            case "off" -> OFF;
            default -> ifMissing;
        };
    }

    public @NotNull ActionBarIdleMode nextInCycle() {
        return switch (this) {
            case SMART -> ALWAYS;
            case ALWAYS -> OFF;
            case OFF -> SMART;
        };
    }

    /**
     * @param hasExplicitIdleModeKey true if {@code idle_mode} was present in storage
     * @param hasLegacyIdleKey       true if the legacy boolean {@code idle} key was present
     * @param configModeDefault      global default from config when neither key is present
     */
    public static @NotNull ActionBarLoadResolution resolveOnLoad(
            boolean hasExplicitIdleModeKey,
            @Nullable String explicitIdleModeRaw,
            boolean hasLegacyIdleKey,
            boolean legacyIdleTrue,
            @NotNull String configModeDefault) {
        if (hasExplicitIdleModeKey) {
            return new ActionBarLoadResolution(
                    fromStringOr(explicitIdleModeRaw, SMART), false);
        }
        if (hasLegacyIdleKey) {
            if (legacyIdleTrue) {
                // Sprint 81A: was treated as "always" in 81; product default is now smart
                return new ActionBarLoadResolution(SMART, true);
            }
            return new ActionBarLoadResolution(OFF, true);
        }
        return new ActionBarLoadResolution(
                fromStringOr(configModeDefault, SMART), false);
    }

    /**
     * @param shouldPersistWhenIdleMissing true if a legacy {@code idle} key without
     *                                     {@code idle_mode} was converted (needs save)
     */
    public record ActionBarLoadResolution(
            @NotNull ActionBarIdleMode mode,
            boolean shouldPersistWhenIdleMissing) {}
}
