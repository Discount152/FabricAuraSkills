package dev.granite.graniteskills.bukkit.menus.shared;

import dev.granite.graniteskills.api.registry.NamespaceIdentified;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ModifierInstance(
        NamespaceIdentified parent,
        String id,
        double value,
        Operation operation,
        @Nullable ItemStack item,
        String displayName,
        String description,
        int index
) {

    public ModifierInstance withIndex(int index) {
        return new ModifierInstance(this.parent, this.id, this.value, this.operation, this.item, this.displayName, this.description, index);
    }

}
