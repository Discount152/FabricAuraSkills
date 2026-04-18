package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Ports Bukkit movement trait using a {@link EntityAttributes#MOVEMENT_SPEED} modifier (walk-speed API is not mirrored).
 */
public final class FabricMovementSpeedTrait extends FabricTraitImpl {

    private static final Identifier MODIFIER_ID = Identifier.of("auraskills", "movement_speed_trait");
    private static final double RATIO = 500.0;

    public FabricMovementSpeedTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.MOVEMENT_SPEED);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (inst == null) {
            return 100;
        }
        double total = inst.getValue();
        EntityAttributeModifier ours = inst.getModifier(MODIFIER_ID);
        double removed = ours != null ? ours.value() : 0.0;
        double attributeRatio = 1000.0;
        double baseValue = total - removed;
        return baseValue * attributeRatio;
    }

    @Override
    protected void reload(ServerPlayerEntity player, Trait trait) {
        plugin.getScheduler().executeSync(() -> applyMovementSpeed(player, trait));
    }

    @Override
    protected void onWorldChanged(ServerPlayerEntity player, ServerWorld from, ServerWorld to, Trait trait) {
        if (!trait.equals(Traits.MOVEMENT_SPEED)) {
            return;
        }
        applyMovementSpeed(player, trait);
    }

    private void applyMovementSpeed(ServerPlayerEntity player, Trait trait) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(MODIFIER_ID);
        if (!trait.isEnabled()) {
            return;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double add = user.getBonusTraitLevel(trait) / RATIO;
        double max = trait.optionDouble("max") / RATIO;
        double base = attribute.getValue();
        double target = base + add;
        if (target > max) {
            add = max - base;
        }
        if (add <= 0) {
            return;
        }
        attribute.addPersistentModifier(new EntityAttributeModifier(
                MODIFIER_ID,
                add,
                EntityAttributeModifier.Operation.ADD_VALUE));
    }
}
