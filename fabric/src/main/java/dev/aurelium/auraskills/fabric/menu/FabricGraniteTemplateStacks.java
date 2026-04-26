package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Shared strict Minecraft item resolution + template {@link ItemStack} construction for bundled GraniteLib YAML menus
 * (Sprint 99). Stays in Fabric because it uses {@link Registries#ITEM}.
 */
public final class FabricGraniteTemplateStacks {

    private FabricGraniteTemplateStacks() {
    }

    /**
     * Unknown or blank ids log {@code menuLogName + " " + fieldPath + ": …"} and return empty (template path should
     * fall back).
     */
    public static Optional<Item> resolveStrict(
            FabricAuraSkills plugin, String materialId, String menuLogName, String fieldPath) {
        if (materialId == null || materialId.isBlank()) {
            plugin.logger().warn(menuLogName + " " + fieldPath + ": blank material; using built-in menu");
            return Optional.empty();
        }
        try {
            Identifier id = materialId.contains(":")
                    ? Identifier.of(materialId)
                    : Identifier.ofVanilla(materialId.toLowerCase(Locale.ROOT));
            if (!Registries.ITEM.containsId(id)) {
                plugin.logger().warn(
                        menuLogName
                                + " "
                                + fieldPath
                                + ": unknown item '"
                                + materialId
                                + "'; using built-in menu");
                return Optional.empty();
            }
            Item it = Registries.ITEM.get(id);
            if (it == null || it == Items.AIR) {
                plugin.logger().warn(
                        menuLogName
                                + " "
                                + fieldPath
                                + ": item '"
                                + materialId
                                + "' resolved to air; using built-in menu");
                return Optional.empty();
            }
            return Optional.of(it);
        } catch (Exception e) {
            plugin.logger().warn(
                    menuLogName + " " + fieldPath + ": could not parse item '" + materialId + "'; using built-in menu",
                    e);
            return Optional.empty();
        }
    }

    public static Optional<Text> toLiteralNoUnresolved(
            String expanded, FabricAuraSkills plugin, String menuLogName, String fieldLabel) {
        if (GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(expanded)) {
            plugin.logger().warn(
                    menuLogName
                            + " "
                            + fieldLabel
                            + " still contains unresolved placeholders after expansion; using built-in menu");
            return Optional.empty();
        }
        return Optional.of(Text.literal(expanded));
    }

    public static Optional<ItemStack> buildFillStack(
            Optional<GraniteTemplateFill> fill,
            Map<String, String> ph,
            FabricAuraSkills plugin,
            String menuLogName) {
        if (fill.isEmpty()) {
            return Optional.empty();
        }
        GraniteTemplateFill f = fill.get();
        Optional<Item> item = resolveStrict(plugin, f.material(), menuLogName, "fill.material");
        if (item.isEmpty()) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(item.get(), 1);
        if (f.displayName().isPresent()) {
            String name = GraniteTemplatePlaceholders.expandAllKeysLongestFirst(f.displayName().get(), ph);
            Optional<Text> line = toLiteralNoUnresolved(name, plugin, menuLogName, "fill.name");
            if (line.isEmpty()) {
                return Optional.empty();
            }
            stack.set(DataComponentTypes.ITEM_NAME, line.get());
        }
        return Optional.of(stack);
    }

    public static Optional<ItemStack> buildTemplateItemStack(
            GraniteTemplateItem item,
            Map<String, String> ph,
            FabricAuraSkills plugin,
            String menuLogName,
            String itemLogPath) {
        Optional<Item> resolved = resolveStrict(plugin, item.material(), menuLogName, itemLogPath + ".material");
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        int count = item.amount().orElse(1);
        ItemStack stack = new ItemStack(resolved.get(), Math.max(1, count));

        if (item.displayName().isPresent()) {
            String name = GraniteTemplatePlaceholders.expandAllKeysLongestFirst(item.displayName().get(), ph);
            Optional<Text> line = toLiteralNoUnresolved(name, plugin, menuLogName, itemLogPath + ".name");
            if (line.isEmpty()) {
                return Optional.empty();
            }
            stack.set(DataComponentTypes.ITEM_NAME, line.get());
        }

        List<Text> loreLines = new ArrayList<>();
        for (String loreRaw : item.lore()) {
            if (loreRaw.isEmpty()) {
                loreLines.add(Text.empty());
                continue;
            }
            String line = GraniteTemplatePlaceholders.expandAllKeysLongestFirst(loreRaw, ph);
            Optional<Text> text = toLiteralNoUnresolved(line, plugin, menuLogName, itemLogPath + ".lore");
            if (text.isEmpty()) {
                return Optional.empty();
            }
            loreLines.add(text.get());
        }
        if (!loreLines.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
        }
        FabricStatInfoItemComponents.applyCustomModelData(stack, item.customModelData());
        return Optional.of(stack);
    }
}
