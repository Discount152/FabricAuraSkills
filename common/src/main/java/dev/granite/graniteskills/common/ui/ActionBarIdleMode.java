package dev.granite.graniteskills.common.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * How the health/mana idle action bar is shown.
 */
public enum ActionBarIdleMode {
    /** Show only in short windows on relevant change, then stop sending packets. */
    SMART,
    /** Keep updating while the idle line is not paused by other action bar traffic. */
    ALWAYS,
    /** Never show the idle health/mana line. */
    OFF;

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

    /** Bare {@code /toggle}: hides the idle line, or restores the default Smart mode when hidden. */
    public @NotNull ActionBarIdleMode toggled() {
        return this == OFF ? SMART : OFF;
    }

    public static @NotNull ActionBarLoadResolution resolveOnLoad(
            boolean hasExplicitIdleModeKey,
            @Nullable String explicitIdleModeRaw,
            boolean hasLegacyIdleKey,
            boolean legacyIdleTrue,
            @NotNull String configModeDefault) {
        if (hasExplicitIdleModeKey) {
            return new ActionBarLoadResolution(fromStringOr(explicitIdleModeRaw, SMART), false);
        }
        if (hasLegacyIdleKey) {
            if (legacyIdleTrue) {
                return new ActionBarLoadResolution(SMART, true);
            }
            return new ActionBarLoadResolution(OFF, true);
        }
        return new ActionBarLoadResolution(fromStringOr(configModeDefault, SMART), false);
    }

    public record ActionBarLoadResolution(@NotNull ActionBarIdleMode mode, boolean shouldPersistWhenIdleMissing) {
    }
}
