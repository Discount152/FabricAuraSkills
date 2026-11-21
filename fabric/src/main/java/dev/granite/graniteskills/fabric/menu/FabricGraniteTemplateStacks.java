package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.text.FabricTextAdapter;
import dev.granite.graniteskills.fabric.text.FabricTextFormatter;
import dev.granitelib.fabric.template.GraniteTemplatePlaceholders;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Shared strict Minecraft item resolution + template {@link ItemStack} construction for bundled GraniteLib YAML menus
 * (Sprint 99). Stays in Fabric because it uses {@link BuiltInRegistries#ITEM}.
 */
public final class FabricGraniteTemplateStacks {

    private FabricGraniteTemplateStacks() {}

    /**
     * Sprint 122: placeholder-expanded display names that are null, empty, or only whitespace should use
     * {@link FabricMenuItemPresentation#applyQuietFiller} instead of {@link Component#literal} on a lone space (avoids
     * vanilla item names / tooltips on glass fillers).
     */
    public static boolean isBlankTemplateDisplayName(@Nullable String expanded) {
        return expanded == null || expanded.isBlank();
    }

    /**
     * Unknown or blank ids log {@code menuLogName + " " + fieldPath + ": …"} and return empty (template path should
     * fall back).
     */
    public static Optional<Item> resolveStrict(
            FabricGraniteSkills plugin, String materialId, String menuLogName, String fieldPath) {
        if (materialId == null || materialId.isBlank()) {
            plugin.logger().warn(menuLogName + " " + fieldPath + ": blank material; using built-in menu");
            return Optional.empty();
        }
        try {
            Identifier id = materialId.contains(":")
                    ? Identifier.parse(materialId)
                    : Identifier.withDefaultNamespace(materialId.toLowerCase(Locale.ROOT));
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                plugin.logger().warn(
                        menuLogName
                                + " "
                                + fieldPath
                                + ": unknown item '"
                                + materialId
                                + "'; using built-in menu");
                return Optional.empty();
            }
            Item it = BuiltInRegistries.ITEM.getValue(id);
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

    public static Optional<Component> toLiteralNoUnresolved(
            String expanded, FabricGraniteSkills plugin, String menuLogName, String fieldLabel) {
        if (GraniteTemplatePlaceholders.hasUnresolvedBraceSyntax(expanded)) {
            plugin.logger().warn(
                    menuLogName
                            + " "
                            + fieldLabel
                            + " still contains unresolved placeholders after expansion; using built-in menu");
            return Optional.empty();
        }
        return Optional.of(FabricTextAdapter.toNativeText(FabricTextFormatter.toComponent(expanded), plugin.getServer()));
    }

    public static Optional<ItemStack> buildFillStack(
            Optional<GraniteTemplateFill> fill,
            Map<String, String> ph,
            FabricGraniteSkills plugin,
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
            if (isBlankTemplateDisplayName(name)) {
                FabricMenuItemPresentation.applyQuietFiller(stack);
            } else {
                Optional<Component> line = toLiteralNoUnresolved(name, plugin, menuLogName, "fill.name");
                if (line.isEmpty()) {
                    return Optional.empty();
                }
                stack.set(DataComponents.ITEM_NAME, line.get());
                FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
            }
        } else {
            FabricMenuItemPresentation.applyQuietFiller(stack);
        }
        return Optional.of(stack);
    }

    public static Optional<ItemStack> buildTemplateItemStack(
            GraniteTemplateItem item,
            Map<String, String> ph,
            FabricGraniteSkills plugin,
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
            if (isBlankTemplateDisplayName(name)) {
                FabricMenuItemPresentation.applyQuietFiller(stack);
            } else {
                Optional<Component> line = toLiteralNoUnresolved(name, plugin, menuLogName, itemLogPath + ".name");
                if (line.isEmpty()) {
                    return Optional.empty();
                }
                stack.set(DataComponents.ITEM_NAME, line.get());
            }
        }

        List<Component> loreLines = new ArrayList<>();
        for (String loreRaw : item.lore()) {
            if (loreRaw.isEmpty()) {
                loreLines.add(Component.empty());
                continue;
            }
            String line = GraniteTemplatePlaceholders.expandAllKeysLongestFirst(loreRaw, ph);
            Optional<Component> text = toLiteralNoUnresolved(line, plugin, menuLogName, itemLogPath + ".lore");
            if (text.isEmpty()) {
                return Optional.empty();
            }
            loreLines.add(text.get());
        }
        if (!loreLines.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(loreLines));
        }
        FabricStatInfoItemComponents.applyCustomModelData(stack, item.customModelData());
        FabricMenuTooltipPresentation.suppressVanillaMechanicsTooltip(stack);
        return Optional.of(stack);
    }
}
