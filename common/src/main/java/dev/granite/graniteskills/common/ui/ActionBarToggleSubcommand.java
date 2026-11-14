package dev.granite.graniteskills.common.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Optional argument for {@code /toggle} and {@code /abtoggle}: cycle, show status, or set a mode.
 */
public final class ActionBarToggleSubcommand {

    public enum Kind {
        CYCLE,
        STATUS,
        SET_MODE,
        INVALID
    }

    public record Result(@NotNull Kind kind, @Nullable ActionBarIdleMode mode) {
    }

    private ActionBarToggleSubcommand() {
    }

    public static @NotNull Result parse(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return new Result(Kind.CYCLE, null);
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "status" -> new Result(Kind.STATUS, null);
            case "smart" -> new Result(Kind.SET_MODE, ActionBarIdleMode.SMART);
            case "always" -> new Result(Kind.SET_MODE, ActionBarIdleMode.ALWAYS);
            case "off" -> new Result(Kind.SET_MODE, ActionBarIdleMode.OFF);
            default -> new Result(Kind.INVALID, null);
        };
    }
}
