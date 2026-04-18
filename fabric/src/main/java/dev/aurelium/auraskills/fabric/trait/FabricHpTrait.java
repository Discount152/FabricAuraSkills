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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ports Bukkit {@code HpTrait} attribute logic. Client heart scaling ({@code setHealthScale}) is not used on Fabric.
 */
public final class FabricHpTrait extends FabricTraitImpl {

    private static final double THRESHOLD = 0.1;
    private static final Identifier MODIFIER_ID = Identifier.of("auraskills", "hp_trait");

    private final Map<UUID, Double> worldChangeHealth = new ConcurrentHashMap<>();

    public FabricHpTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.HP);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute == null) {
            return 0.0;
        }
        double current = attribute.getValue();
        EntityAttributeModifier ours = attribute.getModifier(MODIFIER_ID);
        if (ours != null) {
            current -= ours.value();
        }
        return current;
    }

    @Override
    protected void reload(ServerPlayerEntity player, Trait trait) {
        plugin.getScheduler().executeSync(() -> setHealth(player, plugin.getUserManager().getUser(player.getUuid())));
    }

    @Override
    protected void onWorldChanged(ServerPlayerEntity player, ServerWorld from, ServerWorld to, Trait trait) {
        if (!trait.equals(Traits.HP) || !Traits.HP.isEnabled()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)
                && !FabricTraitWorlds.isDisabledWorld(plugin, from)) {
            worldChangeHealth.put(player.getUuid(), (double) player.getHealth());
        }
        int delay = Traits.HP.optionInt("update_delay");
        if (delay > 0) {
            plugin.getScheduler().scheduleSync(() -> applyWorldChange(player, user, from, to), delay * 50L,
                    java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            applyWorldChange(player, user, from, to);
        }
    }

    private void applyWorldChange(ServerPlayerEntity player, User user, ServerWorld from, ServerWorld to) {
        setHealth(player, user);
        var wm = plugin.getWorldManager();
        String fromName = FabricTraitWorlds.worldName(from);
        if (!wm.isDisabledWorld(fromName) || FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return;
        }
        UUID id = player.getUuid();
        if (!worldChangeHealth.containsKey(id)) {
            return;
        }
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        double maxHealth = attribute == null ? 20.0 : attribute.getValue();
        double newHealth = worldChangeHealth.get(id);
        if (newHealth > maxHealth) {
            newHealth = maxHealth;
        }
        player.setHealth((float) newHealth);
        worldChangeHealth.remove(id);
    }

    private void setHealth(ServerPlayerEntity player, User user) {
        Trait trait = Traits.HP;
        double modifier = user.getBonusTraitLevel(trait);
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }
        double originalMax = attribute.getValue();
        EntityAttributeModifier existing = attribute.getModifier(MODIFIER_ID);
        boolean hasChange = true;
        if (existing != null) {
            if (Math.abs(originalMax - (originalMax - existing.value() + modifier)) <= THRESHOLD) {
                hasChange = false;
            }
            if (hasChange) {
                attribute.removeModifier(MODIFIER_ID);
            }
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player) || !trait.isEnabled()) {
            attribute.removeModifier(MODIFIER_ID);
            if (player.getHealth() >= originalMax && attribute.getValue() > 0) {
                player.setHealth(Math.min(player.getHealth(), (float) attribute.getValue()));
            }
            return;
        }
        if (trait.optionBoolean("force_base_health", false)) {
            attribute.setBaseValue(20.0);
        }
        if (!hasChange && existing != null) {
            return;
        }
        attribute.addPersistentModifier(new EntityAttributeModifier(
                MODIFIER_ID,
                modifier,
                EntityAttributeModifier.Operation.ADD_VALUE));
        if (player.getHealth() > attribute.getValue()) {
            player.setHealth((float) attribute.getValue());
        }
        if (trait.optionBoolean("keep_full_on_increase", false) && attribute.getValue() > originalMax) {
            final double fullThreshold = 0.01;
            if (player.getHealth() >= originalMax - fullThreshold) {
                player.setHealth((float) attribute.getValue());
            }
        }
    }
}
