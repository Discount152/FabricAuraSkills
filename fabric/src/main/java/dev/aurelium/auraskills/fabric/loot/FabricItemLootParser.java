package dev.aurelium.auraskills.fabric.loot;

import dev.aurelium.auraskills.api.config.ConfigNode;
import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootParser;
import dev.aurelium.auraskills.api.loot.LootParsingContext;
import dev.aurelium.auraskills.api.loot.LootRequirements;
import dev.aurelium.auraskills.common.api.implementation.ApiConfigNode;
import dev.aurelium.auraskills.common.loot.ItemSupplier;
import dev.aurelium.auraskills.fabric.item.FabricConfigurateItemParser;
import net.minecraft.item.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

public final class FabricItemLootParser implements LootParser {

    private final FabricLootManager manager;

    public FabricItemLootParser(FabricLootManager manager) {
        this.manager = manager;
    }

    @Override
    public Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements) {
        ConfigurationNode backing = ((ApiConfigNode) config).getBacking();
        ItemSupplier item = null;
        for (var parser : manager.getCustomItemParsers()) {
            if (parser.shouldUseParser(backing)) {
                item = parser.parseCustomItem(backing);
                break;
            }
        }
        if (item == null) {
            ItemStack base = FabricConfigurateItemParser.parseBaseStack(backing);
            item = new ItemSupplier(FabricItemRef.wrap(base), null, null);
        }
        int[] amounts = parseAmount(backing);
        double[] durability = parseDurability(backing);
        return new FabricItemLoot(context.parseValues(config, requirements), item, amounts[0], amounts[1], durability[0], durability[1]);
    }

    private static int[] parseAmount(ConfigurationNode config) {
        if (config.hasChild("amount")) {
            Object object = config.node("amount").raw();
            if (object instanceof Integer amount) {
                return new int[]{amount, amount};
            } else if (object instanceof String amountString) {
                String[] splitString = amountString.split("-");
                int minAmount = Integer.parseInt(splitString[0].trim());
                int maxAmount = minAmount;
                if (splitString.length > 1) {
                    maxAmount = Integer.parseInt(splitString[1].trim());
                }
                return new int[]{minAmount, maxAmount};
            }
        }
        return new int[]{1, 1};
    }

    private static double[] parseDurability(ConfigurationNode config) {
        double minDamage = 0.0;
        double maxDamage = 0.0;
        if (config.hasChild("damage")) {
            if (config.node("damage").isMap()) {
                minDamage = config.node("damage", "min").getDouble();
                maxDamage = config.node("damage", "max").getDouble();
            } else {
                minDamage = config.node("damage").getDouble();
                maxDamage = minDamage;
            }
        }
        return new double[]{minDamage, maxDamage};
    }
}
