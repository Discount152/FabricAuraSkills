package dev.aurelium.auraskills.fabric.damage;

import dev.aurelium.auraskills.api.damage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class FabricDamageTypeResolver {

    private FabricDamageTypeResolver() {
    }

    public static DamageType resolve(Entity directAttacker, PlayerEntity attackingPlayer) {
        if (attackingPlayer == null) {
            return DamageType.OTHER;
        }
        if (directAttacker instanceof ProjectileEntity) {
            return DamageType.BOW;
        }
        ItemStack main = attackingPlayer.getMainHandStack();
        if (main.isEmpty()) {
            return DamageType.HAND;
        }
        Identifier id = Registries.ITEM.getId(main.getItem());
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
