package dev.granite.graniteskills.fabric.damage;

import dev.granite.graniteskills.api.damage.DamageType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

public final class FabricDamageTypeResolver {

    private FabricDamageTypeResolver() {
    }

    public static DamageType resolve(Entity directAttacker, Player attackingPlayer) {
        if (attackingPlayer == null) {
            return DamageType.OTHER;
        }
        if (directAttacker instanceof Projectile) {
            return DamageType.BOW;
        }
        ItemStack main = attackingPlayer.getMainHandItem();
        if (main.isEmpty()) {
            return DamageType.HAND;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(main.getItem());
        String path = id.getPath();
        if (path.contains("trident")) {
            return DamageType.BOW;
        }
        if (path.contains("sword")) {
            return DamageType.SWORD;
        }
        if (path.endsWith("_axe") || path.contains("_axe")) {
            return DamageType.AXE;
        }
        if (path.contains("pickaxe")) {
            return DamageType.PICKAXE;
        }
        if (path.contains("shovel") || path.contains("spade")) {
            return DamageType.SHOVEL;
        }
        if (path.contains("hoe")) {
            return DamageType.HOE;
        }
        return DamageType.OTHER;
    }
}
