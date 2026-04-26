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
}
