package dev.granite.graniteskills.common.user;

import dev.granite.graniteskills.api.skill.Skill;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Turns granted {@code graniteskills.multiplier} nodes into an XP multiplier.
 * The numeric tail may be any integer, decimal, or negative value the reference accepts.
 */
public final class PermissionMultipliers {

    private static final String PREFIX = "graniteskills.multiplier.";

    private PermissionMultipliers() {
    }

    public static double sum(@Nullable Iterable<String> permissions, @Nullable Skill skill) {
        if (permissions == null) {
            return 0.0;
        }
        double multiplier = 0.0;
        for (String permission : permissions) {
            multiplier += contribution(permission, skill);
        }
        return multiplier;
    }

    public static double contribution(@Nullable String permission, @Nullable Skill skill) {
        if (permission == null || !permission.startsWith(PREFIX)) {
            return 0.0;
        }
        String rest = permission.substring(PREFIX.length());
        if (isNumeric(rest)) {
            return Double.parseDouble(rest) / 100.0;
        }
        if (skill == null) {
            return 0.0;
        }
        String namespacedName = skill.toString().toLowerCase(Locale.ROOT) + ".";
        String plainName = skill.name().toLowerCase(Locale.ROOT) + ".";
        if (rest.startsWith(namespacedName)) {
            rest = rest.substring(namespacedName.length());
        } else if (rest.startsWith(plainName)) {
            rest = rest.substring(plainName.length());
        } else {
            return 0.0;
        }
        if (isNumeric(rest)) {
            return Double.parseDouble(rest) / 100.0;
        }
        return 0.0;
    }

    static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        boolean decimalSeen = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i == 0 && c == '-') {
                if (str.length() == 1) {
                    return false;
                }
                continue;
            }
            if (c == '.') {
                if (decimalSeen || i == 0 || i == str.length() - 1) {
                    return false;
                }
                decimalSeen = true;
            } else if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
