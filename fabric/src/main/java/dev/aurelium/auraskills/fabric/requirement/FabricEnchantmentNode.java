package dev.aurelium.auraskills.fabric.requirement;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

final class FabricEnchantmentNode extends FabricRequirementNode {

    private final Holder<Enchantment> enchantment;
    private final int levelMin;
    private final int levelMax;

    FabricEnchantmentNode(FabricAuraSkills plugin, Identifier enchantmentId, int levelMin, int levelMax, String message) {
        super(plugin, message);
        Holder<Enchantment> resolved = null;
        if (enchantmentId != null) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantmentId);
            resolved = plugin.getServer().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(key)
                    .orElse(null);
        }
        this.enchantment = resolved;
        this.levelMin = levelMin;
        this.levelMax = levelMax;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (enchantment == null) {
            return true;
        }
        ItemStack held = player.getMainHandItem();
        ItemEnchantments c = held.get(DataComponents.ENCHANTMENTS);
        if (c == null || c.isEmpty()) {
            return false;
        }
        int enchantmentLevel = c.getLevel(enchantment);
        if (levelMin >= 0) {
            if (levelMin == levelMax && enchantmentLevel != levelMin) {
                return false;
            }
            if (enchantmentLevel < levelMin) {
                return false;
            }
            if (levelMax >= 0 && enchantmentLevel > levelMax) {
                return false;
            }
        }
        return true;
    }
}
