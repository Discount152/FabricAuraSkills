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

public final class FabricLuckTrait extends FabricTraitImpl {

    private static final Identifier MODIFIER_ID = Identifier.of("auraskills", "luck_trait");

    public FabricLuckTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.LUCK);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.LUCK);
        if (inst == null) {
            return 0.0;
        }
        double current = inst.getValue();
        EntityAttributeModifier ours = inst.getModifier(MODIFIER_ID);
        if (ours != null) {
            current -= ours.value();
        }
        return current;
    }

    @Override
    protected void reload(ServerPlayerEntity player, Trait trait) {
        plugin.getScheduler().executeSync(() -> setLuck(player));
    }

    @Override
    protected void onWorldChanged(ServerPlayerEntity player, ServerWorld from, ServerWorld to, Trait trait) {
        if (!trait.equals(Traits.LUCK)) {
            return;
        }
        setLuck(player);
    }

    private void setLuck(ServerPlayerEntity player) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.LUCK);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(MODIFIER_ID);
        if (!Traits.LUCK.isEnabled()) {
            return;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double luck = user.getBonusTraitLevel(Traits.LUCK);
        if (luck < 0.01) {
            return;
        }
        attribute.addPersistentModifier(new EntityAttributeModifier(
                MODIFIER_ID,
                luck,
                EntityAttributeModifier.Operation.ADD_VALUE));
    }
}
