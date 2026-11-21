package dev.granitelib.fabric.menu;

import dev.granitelib.fabric.template.model.GraniteMenuTemplate;
import dev.granitelib.fabric.template.model.GraniteTemplateFill;
import dev.granitelib.fabric.template.model.GraniteTemplateItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Puts each menu item into the container slots named by that item. A fill material then occupies
 * only the empty slots in the menu grid.
 */
public final class GraniteMenuLayout {

    private GraniteMenuLayout() {
    }

    public static void place(GraniteMenuTemplate template, SimpleContainer container) {
        boolean[] used = new boolean[container.getContainerSize()];
        for (Map.Entry<String, GraniteTemplateItem> entry : template.items().entrySet()) {
            GraniteTemplateItem item = entry.getValue();
            ItemStack stack = stack(entry.getKey(), item.material(), item.amount(),
                    item.displayName(), item.lore(), item.customModelData());
            for (int slot : entry.getValue().slots()) {
                if (slot < 0 || slot >= container.getContainerSize()) {
                    throw new IllegalArgumentException("slot " + slot + " is outside the container");
                }
                if (used[slot]) {
                    throw new IllegalArgumentException("slot " + slot + " already has an item");
                }
                used[slot] = true;
                container.setItem(slot, stack.copy());
            }
        }
        template.fill().ifPresent(fill -> fillEmpty(template, container, fill));
    }

    /**
     * Fills empty slots in the menu grid. A slot that already holds an item is left alone.
     */
    public static void fillEmpty(GraniteMenuTemplate template, SimpleContainer container, String material) {
        fillEmpty(template, container, new GraniteTemplateFill(material, Optional.empty()));
    }

    public static void fillEmpty(GraniteMenuTemplate template, SimpleContainer container, GraniteTemplateFill fill) {
        ItemStack filler = stack("fill", fill.material(), OptionalInt.empty(), fill.displayName(), List.of(),
                OptionalInt.empty());
        int grid = template.rows() * 9;
        int limit = Math.min(grid, container.getContainerSize());
        for (int slot = 0; slot < limit; slot++) {
            if (container.getItem(slot).isEmpty()) {
                container.setItem(slot, filler.copy());
            }
        }
    }

    private static ItemStack stack(String key, String material, OptionalInt amount, Optional<String> displayName,
            List<String> lore, OptionalInt customModelData) {
        String name = material.trim().toLowerCase(java.util.Locale.ROOT);
        Identifier id = name.indexOf(':') >= 0
                ? Identifier.parse(name)
                : Identifier.withDefaultNamespace(name);
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            throw new IllegalArgumentException("unknown item '" + material + "' for " + key);
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(id));
        amount.ifPresent(stack::setCount);
        displayName.ifPresent(text -> stack.set(DataComponents.CUSTOM_NAME, GraniteMenuText.parse(text)));
        if (!lore.isEmpty()) {
            List<Component> lines = new ArrayList<>(lore.size());
            for (String line : lore) {
                lines.add(GraniteMenuText.parse(line));
            }
            stack.set(DataComponents.LORE, new ItemLore(lines));
        }
        customModelData.ifPresent(value -> stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of(), List.of(value))));
        return stack;
    }
}
