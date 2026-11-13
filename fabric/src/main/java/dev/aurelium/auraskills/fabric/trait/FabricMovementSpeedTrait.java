package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Ports Bukkit movement trait using a {@link Attributes#MOVEMENT_SPEED} modifier (walk-speed API is not mirrored).
 */
public final class FabricMovementSpeedTrait extends FabricTraitImpl {

    private static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath("auraskills", "movement_speed_trait");
    private static final double RATIO = 500.0;

    public FabricMovementSpeedTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.MOVEMENT_SPEED);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        AttributeInstance inst = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) {
            return 100;
        }
        double total = inst.getValue();
        AttributeModifier ours = inst.getModifier(MODIFIER_ID);
        double removed = ours != null ? ours.amount() : 0.0;
        double attributeRatio = 1000.0;
        double baseValue = total - removed;
        return baseValue * attributeRatio;
    }

    @Override
    protected void reload(ServerPlayer player, Trait trait) {
        plugin.getScheduler().executeSync(() -> applyMovementSpeed(player, trait));
    }

    @Override
    protected void onWorldChanged(ServerPlayer player, ServerLevel from, ServerLevel to, Trait trait) {
        if (!trait.equals(Traits.MOVEMENT_SPEED)) {
            return;
        }
        applyMovementSpeed(player, trait);
    }

    private void applyMovementSpeed(ServerPlayer player, Trait trait) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
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
        User user = plugin.getUserManager().getUser(player.getUUID());
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
        attribute.addPermanentModifier(new AttributeModifier(
                MODIFIER_ID,
                add,
                AttributeModifier.Operation.ADD_VALUE));
    }
}
