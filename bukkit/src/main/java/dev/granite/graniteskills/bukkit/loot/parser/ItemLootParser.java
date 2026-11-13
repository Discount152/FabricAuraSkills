package dev.granite.graniteskills.bukkit.loot.parser;

import dev.granite.graniteskills.api.config.ConfigNode;
import dev.granite.graniteskills.api.loot.Loot;
import dev.granite.graniteskills.api.loot.LootParser;
import dev.granite.graniteskills.api.loot.LootParsingContext;
import dev.granite.graniteskills.api.loot.LootRequirements;
import dev.granite.graniteskills.bukkit.loot.BukkitLootManager;
import dev.granite.graniteskills.bukkit.loot.item.enchant.LootEnchantLevel;
import dev.granite.graniteskills.bukkit.loot.type.ItemLoot;
import dev.granite.graniteskills.bukkit.util.ConfigurateItemParser;
import dev.granite.graniteskills.common.api.implementation.ApiConfigNode;
import dev.granite.graniteskills.common.loot.CustomItemParser;
import dev.granite.graniteskills.common.loot.ItemSupplier;
import dev.granite.graniteskills.common.loot.enchant.LootEnchantEntry;
import dev.granite.graniteskills.common.loot.enchant.LootEnchantList;
import dev.granite.graniteskills.common.loot.enchant.LootEnchantments;
import dev.granite.graniteskills.common.util.data.Validate;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

import static dev.granite.graniteskills.bukkit.ref.BukkitItemRef.wrap;

public class ItemLootParser implements LootParser {

    private final BukkitLootManager manager;
    private final ConfigurateItemParser itemParser;

    public ItemLootParser(BukkitLootManager manager) {
        this.manager = manager;
        this.itemParser = new ConfigurateItemParser(manager.getPlugin());
    }

    @Override
    public Loot parse(LootParsingContext context, ConfigNode config, LootRequirements requirements) {
        ItemSupplier item = null;
        ConfigurationNode backing = ((ApiConfigNode) config).getBacking();
        for (CustomItemParser parser : manager.getCustomItemParsers()) {
            if (parser.shouldUseParser(backing)) {
                item = parser.parseCustomItem(backing);
                break;
            }
        }
        // Parse normally
        if (item == null) {
            try {
                item = parseItem(backing);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        }
        Validate.notNull(item, "Failed to parse item");

        int[] amounts = parseAmount(backing);
        double[] durability = parseDurability(backing);

        return new ItemLoot(context.parseValues(config, requirements), item, amounts[0], amounts[1], durability[0], durability[1]);
    }

    private ItemSupplier parseItem(ConfigurationNode config) throws SerializationException {
        List<String> excludedKeys = List.of("amount", "enchantments");
        ItemStack baseItem = itemParser.parseItem(config, excludedKeys);
        // Parse possible enchantments, value of the map is the weight
        Map<LootEnchantList, Integer> possibleEnchants = parsePossibleEnchants(config);

        return new ItemSupplier(wrap(baseItem), null, new LootEnchantments(possibleEnchants));
    }

    private Map<LootEnchantList, Integer> parsePossibleEnchants(ConfigurationNode config) {
        Map<LootEnchantList, Integer> possibleEnchants = new HashMap<>();
        if (config.hasChild("enchantments")) { // Single enchant list
            LootEnchantList singleList = parseSingleEnchantList(config);
            possibleEnchants.put(singleList, 1);
        }

        return Map.copyOf(possibleEnchants);
    }

    private LootEnchantList parseSingleEnchantList(ConfigurationNode config) {
        List<LootEnchantEntry> entryList = new ArrayList<>();

        for (ConfigurationNode enchantment : config.node("enchantments").childrenList()) {
            String enchantmentName;
            String levelString = "1";
            double chance = 1.0;

            if (enchantment.isMap()) {
                enchantmentName = enchantment.node("name").getString("").toLowerCase(Locale.ROOT);
                levelString = enchantment.node("level").getString("1");
                chance = Double.parseDouble(enchantment.node("chance").getString("1.0"));
            } else {
                String[] splitEntry = enchantment.getString("").split(" ");
                enchantmentName = splitEntry[0].toLowerCase(Locale.ROOT);
                if (splitEntry.length > 1) {
                    levelString = splitEntry[1];
                }
            }

            LootEnchantLevel enchantLevel = parseLevel(levelString);
            LootEnchantEntry entry = new LootEnchantEntry(enchantmentName, enchantLevel.minLevel(), enchantLevel.maxLevel(), chance);
            entryList.add(entry);
        }

        return new LootEnchantList(entryList);
    }

    protected LootEnchantLevel parseLevel(String levelString) {
        int minLevel;
        int maxLevel;

        if (levelString.contains("-")) { // Handle level range format (eg. 1-5)
            String[] splitLevel = levelString.split("-");
            minLevel = Integer.parseInt(splitLevel[0]);
            maxLevel = Integer.parseInt(splitLevel[1]);
        } else {
            int fixedLevel = Integer.parseInt(levelString);
            minLevel = fixedLevel;
            maxLevel = fixedLevel;
        }

        return new LootEnchantLevel(minLevel, maxLevel);
    }

    protected int[] parseAmount(ConfigurationNode config) {
        if (config.hasChild("amount")) {
            Object object = config.node("amount").raw();
            if (object instanceof Integer amount) {
                return new int[]{amount, amount};
            } else if (object instanceof String amountString) {
                String[] splitString = amountString.split("-");
                int minAmount = Integer.parseInt(splitString[0]);
                int maxAmount = minAmount;
                if (splitString.length > 1) {
                    maxAmount = Integer.parseInt(splitString[1]);
                }
                return new int[]{minAmount, maxAmount};
            }
        }
        return new int[]{1, 1};
    }

    protected double[] parseDurability(ConfigurationNode config) {
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
