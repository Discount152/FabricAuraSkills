package dev.aurelium.auraskills.fabric;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Boss bar XP display is not implemented in the Fabric MVP; formats are used as fallbacks.
 */
public final class FabricBossBarManager {

    private final NumberFormat xpFormat = new DecimalFormat("#,###.##");
    private final NumberFormat percentFormat = new DecimalFormat("#,###.#");
    private final NumberFormat moneyFormat = new DecimalFormat("#,###.##");

    public void loadOptions() {
    }

    public NumberFormat getXpFormat() {
        return xpFormat;
    }

    public NumberFormat getPercentFormat() {
        return percentFormat;
    }

    public NumberFormat getMoneyFormat() {
        return moneyFormat;
    }
}
