package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

final class FabricItemNode extends FabricRequirementNode {

    private final Identifier expectedItemId;

    FabricItemNode(FabricAuraSkills plugin, String item, String message) {
        super(plugin, message);
        String upper = item != null ? item.toUpperCase(Locale.ROOT) : "";
        Identifier id = Identifier.withDefaultNamespace(upper.toLowerCase(Locale.ROOT));
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            this.expectedItemId = Identifier.tryParse(item != null ? item.toLowerCase(Locale.ROOT) : "");
        } else {
            this.expectedItemId = id;
        }
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (expectedItemId == null || !BuiltInRegistries.ITEM.containsKey(expectedItemId)) {
            return true;
        }
        ItemStack held = player.getMainHandItem();
        Identifier heldId = BuiltInRegistries.ITEM.getKey(held.getItem());
        return expectedItemId.equals(heldId);
    }
}
