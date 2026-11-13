package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Ports Bukkit {@code HpTrait} attribute logic. Client heart scaling ({@code setHealthScale}) is not used on Fabric.
 */
public final class FabricHpTrait extends FabricTraitImpl {

    private static final double THRESHOLD = 0.1;
    private static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath("auraskills", "hp_trait");

    private final Map<UUID, Double> worldChangeHealth = new ConcurrentHashMap<>();

    public FabricHpTrait(FabricAuraSkills plugin) {
        super(plugin, Traits.HP);
    }

    @Override
    public double getBaseLevel(ServerPlayer player, Trait trait) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return 0.0;
        }
        double current = attribute.getValue();
        AttributeModifier ours = attribute.getModifier(MODIFIER_ID);
        if (ours != null) {
            current -= ours.amount();
        }
        return current;
    }

    @Override
    protected void reload(ServerPlayer player, Trait trait) {
        plugin.getScheduler().executeSync(() -> setHealth(player, plugin.getUserManager().getUser(player.getUUID())));
    }

    @Override
    protected void onWorldChanged(ServerPlayer player, ServerLevel from, ServerLevel to, Trait trait) {
        if (!trait.equals(Traits.HP) || !Traits.HP.isEnabled()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)
                && !FabricTraitWorlds.isDisabledWorld(plugin, from)) {
            worldChangeHealth.put(player.getUUID(), (double) player.getHealth());
        }
        int delay = Traits.HP.optionInt("update_delay");
        if (delay > 0) {
            plugin.getScheduler().scheduleSync(() -> applyWorldChange(player, user, from, to), delay * 50L,
                    java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            applyWorldChange(player, user, from, to);
        }
    }

    private void applyWorldChange(ServerPlayer player, User user, ServerLevel from, ServerLevel to) {
        setHealth(player, user);
        var wm = plugin.getWorldManager();
        String fromName = FabricTraitWorlds.worldName(from);
        if (!wm.isDisabledWorld(fromName) || FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return;
        }
        UUID id = player.getUUID();
        if (!worldChangeHealth.containsKey(id)) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        double maxHealth = attribute == null ? 20.0 : attribute.getValue();
        double newHealth = worldChangeHealth.get(id);
        if (newHealth > maxHealth) {
            newHealth = maxHealth;
        }
        player.setHealth((float) newHealth);
        worldChangeHealth.remove(id);
    }

    private void setHealth(ServerPlayer player, User user) {
        Trait trait = Traits.HP;
        double modifier = user.getBonusTraitLevel(trait);
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }
        double originalMax = attribute.getValue();
        AttributeModifier existing = attribute.getModifier(MODIFIER_ID);
        boolean hasChange = true;
        if (existing != null) {
            if (Math.abs(originalMax - (originalMax - existing.amount() + modifier)) <= THRESHOLD) {
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
        attribute.addPermanentModifier(new AttributeModifier(
                MODIFIER_ID,
                modifier,
                AttributeModifier.Operation.ADD_VALUE));
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
