package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class FabricLuckTrait extends FabricTraitImpl {

    private static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath("graniteskills", "luck_trait");

    public FabricLuckTrait(FabricGraniteSkills plugin) {
        super(plugin, Traits.LUCK);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        AttributeInstance inst = player.getAttribute(Attributes.LUCK);
        if (inst == null) {
            return 0.0;
        }
        double current = inst.getValue();
        AttributeModifier ours = inst.getModifier(MODIFIER_ID);
        if (ours != null) {
            current -= ours.amount();
        }
        return current;
    }

    @Override
    protected void reload(ServerPlayer player, Trait trait) {
        plugin.getScheduler().executeSync(() -> setLuck(player));
    }

    @Override
    protected void onWorldChanged(ServerPlayer player, ServerLevel from, ServerLevel to, Trait trait) {
        if (!trait.equals(Traits.LUCK)) {
            return;
        }
        setLuck(player);
    }

    private void setLuck(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.LUCK);
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
        User user = plugin.getUserManager().getUser(player.getUUID());
        double luck = user.getBonusTraitLevel(Traits.LUCK);
        if (luck < 0.01) {
            return;
        }
        attribute.addPermanentModifier(new AttributeModifier(
                MODIFIER_ID,
                luck,
                AttributeModifier.Operation.ADD_VALUE));
    }
}
