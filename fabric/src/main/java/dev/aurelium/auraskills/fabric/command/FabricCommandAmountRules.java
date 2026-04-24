package dev.aurelium.auraskills.fabric.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Shared validation and error feedback for numeric command arguments used by
 * **xp** and **mana** (Sprint 41) and integer **level deltas** (Sprint 42).
 */
public final class FabricCommandAmountRules {

    private FabricCommandAmountRules() {}

    /**
     * For **add**-style operations where amount must be &gt; 0. Sends error and returns
     * {@code false} if invalid.
     */
    public static boolean requirePositiveForAdd(double amount, ServerCommandSource source) {
        if (amount <= 0.0) {
            source.sendError(Text.translatable("auraskills.granite.command.xp.amount_positive"));
            return false;
        }
        return true;
    }

    /**
     * For **remove**-style and **set**-style (reference allows 0) where amount must
     * be &gt;= 0.
     */
    public static boolean requireNonNegative(double amount, ServerCommandSource source) {
        if (amount < 0.0) {
            source.sendError(Text.translatable("auraskills.granite.command.xp.amount_non_negative"));
            return false;
        }
        return true;
    }

    /**
     * For add/remove level **count**; must be &gt; 0 (Bukkit addlevel uses positive deltas).
     */
    public static boolean requirePositiveIntForAdd(int amount, ServerCommandSource source) {
        if (amount <= 0) {
            source.sendError(Text.translatable("auraskills.granite.command.level.count_positive"));
            return false;
        }
        return true;
    }
}
