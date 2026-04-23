package dev.aurelium.auraskills.fabric;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.trait.FabricTraitWorlds;
import net.minecraft.screen.Property;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Bukkit {@code AnvilDiscountTrait#onAnvilPrepare} / {@code PrepareAnvilEvent} equivalent: after vanilla
 * {@link net.minecraft.screen.AnvilScreenHandler#updateResult}, adjust {@code levelCost} (same
 * percent-off math as the reference, including minimum cost of 1 when the rounded cost would be 0).
 */
public final class FabricAnvilDiscount {

    private static final Object FORMULA_LOCK = new Object();
    private static @Nullable Expression formula;

    private FabricAnvilDiscount() {
    }

    public static void resetFormula() {
        synchronized (FORMULA_LOCK) {
            formula = null;
        }
    }

    /**
     * Discount in [0,1] (0%–100% off) from the configured formula or the Bukkit fallback curve.
     */
    public static double getDiscountForTraitValue(double traitValue, PlatformLogger logger) {
        try {
            synchronized (FORMULA_LOCK) {
                if (formula == null) {
                    formula = new Expression(Traits.ANVIL_DISCOUNT.optionString("formula"));
                }
                formula.with("value", traitValue);
                return formula.evaluate().getNumberValue().doubleValue();
            }
        } catch (EvaluationException | ParseException | UnsupportedOperationException e) {
            logger.warn("Failed to evaluate formula for trait auraskills/anvil_discount: " + e.getMessage());
        }
        return -1.0 * Math.pow(1.025, -1.0 * traitValue) + 1.0;
    }

    /**
     * Bukkit: highest {@code WISDOM} among {@code PrepareAnvilEvent#getViewers} wins the
     * {@code User} whose trait applies. On Fabric, the anvil screen handler has a single
     * {@link net.minecraft.screen.ForgingScreenHandler#player} — we use that server player only;
     * multi-viewer anvil (if a mod ever adds it) is not the same as Bukkit’s list order.
     */
    public static void afterUpdateResult(Property levelCost, ServerPlayerEntity player) {
        if (!Traits.ANVIL_DISCOUNT.isEnabled()) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double traitValue = user.getEffectiveTraitLevel(Traits.ANVIL_DISCOUNT);
        int repairCost = levelCost.get();
        if (repairCost < 0) {
            return;
        }
        double discount = getDiscountForTraitValue(traitValue, plugin.logger());
        int newCost = (int) Math.round(repairCost * (1.0 - discount));
        if (newCost > 0) {
            levelCost.set(newCost);
        } else {
            levelCost.set(1);
        }
    }
}
