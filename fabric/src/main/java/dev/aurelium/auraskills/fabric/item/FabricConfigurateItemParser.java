package dev.aurelium.auraskills.fabric.item;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.common.util.PlatformUtil;
import dev.aurelium.auraskills.common.util.data.Validate;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.potion.Potion;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Item stack parsing from YAML: loot-style {@link #parseBaseStack} and richer {@link #parseMenuItemStack} for sources.
 */
public final class FabricConfigurateItemParser {

    private FabricConfigurateItemParser() {
    }

    /** Material + optional potion_data; count defaults to 1 (loot supplies range separately). */
    public static ItemStack parseBaseStack(ConfigurationNode config) {
        String materialString = config.node("material").getString();
        Validate.notNull(materialString, "Item must specify a material");
        Identifier id = materialString.contains(":") ? Identifier.of(materialString) : Identifier.ofVanilla(materialString);
        Item item = Registries.ITEM.get(id);
        Validate.notNull(item, "Unknown item: " + materialString);
        ItemStack stack = new ItemStack(item);
        if (config.hasChild("potion_data") && (stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.POTION)
                || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION))) {
            applyPotionData(stack, config.node("potion_data"));
        }
        return stack;
    }

    public static ItemStack parseMenuItemStack(FabricAuraSkills plugin, FabricItemRegistry registry, ConfigurationNode config)
            throws SerializationException {
        ItemStack base = parseBaseItem(plugin, registry, config);
        applyDisplayNameAndLore(plugin, base, config);
        return base;
    }

    private static ItemStack parseBaseItem(FabricAuraSkills plugin, FabricItemRegistry registry, ConfigurationNode config) {
        String key = config.node("key").getString();
        if (key != null) {
            ItemStack fromKey = registry.getItem(NamespacedId.fromDefaultWithColon(key));
            if (fromKey != null && !fromKey.isEmpty()) {
                return fromKey.copy();
            }
        }
        ItemStack stack = parseBaseStack(config);
        parseAmount(stack, config);
        return stack;
    }

    private static void parseAmount(ItemStack stack, ConfigurationNode config) {
        if (!config.node("amount").virtual()) {
            int amount = config.node("amount").getInt(1);
            stack.setCount(Math.max(1, amount));
        }
    }

    private static void applyDisplayNameAndLore(FabricAuraSkills plugin, ItemStack stack, ConfigurationNode config) {
        PlatformUtil util = plugin.getPlatformUtil();
        if (!config.node("display_name").virtual()) {
            String raw = config.node("display_name").getString();
            if (raw != null) {
                String withLegacy = util.toString(util.toComponent(raw));
                stack.set(DataComponentTypes.ITEM_NAME, Text.literal(withLegacy));
            }
        }
        if (!config.node("lore").virtual()) {
            List<String> loreLines;
            try {
                loreLines = config.node("lore").getList(String.class, new ArrayList<>());
            } catch (SerializationException e) {
                loreLines = new ArrayList<>();
            }
            if (!loreLines.isEmpty()) {
                List<Text> texts = new ArrayList<>();
                for (String line : loreLines) {
                    String withLegacy = util.toString(util.toComponent(line));
                    texts.add(Text.literal(withLegacy));
                }
                stack.set(DataComponentTypes.LORE, new LoreComponent(texts));
            }
        }
    }

    private static void applyPotionData(ItemStack stack, ConfigurationNode node) {
        String typeName = node.node("type").getString("water").toLowerCase(Locale.ROOT);
        Identifier potionId = mapYamlPotionType(typeName);
        RegistryEntry<Potion> entry = Registries.POTION.getEntry(potionId).orElse(null);
        if (entry == null) {
            return;
        }
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(entry));
    }

    private static Identifier mapYamlPotionType(String yaml) {
        return switch (yaml) {
            case "instant_damage", "harming" -> Identifier.ofVanilla("harming");
            case "instant_heal", "healing" -> Identifier.ofVanilla("healing");
            case "regen", "regeneration" -> Identifier.ofVanilla("regeneration");
            case "jump", "leaping" -> Identifier.ofVanilla("leaping");
            case "speed", "swiftness" -> Identifier.ofVanilla("swiftness");
            case "turtle_master" -> Identifier.ofVanilla("turtle_master");
            default -> Identifier.ofVanilla(yaml);
        };
    }

    /**
     * Best-effort load for Bukkit-serialized {@code items.yml} entries that only expose {@code type} + {@code amount}.
     */
    public static ItemStack parseLegacyBukkitItemMap(ConfigurationNode node) {
        String type = node.node("type").getString();
        if (type == null || type.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Identifier id = Identifier.ofVanilla(type.toLowerCase(Locale.ROOT));
        Item item = Registries.ITEM.get(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        int amount = Math.max(1, node.node("amount").getInt(1));
        return new ItemStack(item, amount);
    }
}
