package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.TimeUnit;

public final class FabricArcheryAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    private static final Identifier STUN_MODIFIER_ID = Identifier.fromNamespaceAndPath("graniteskills", "stun_ability");

    public FabricArcheryAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.RETRIEVAL, Abilities.ARCHER, Abilities.BOW_MASTER, Abilities.PIERCING, Abilities.STUN);
    }

    @Override
    public String replaceDescPlaceholders(String input, Ability ability, User user) {
        if (ability.equals(Abilities.RETRIEVAL)) {
            return TextUtil.replace(input, "{time}", NumberUtil.format1(ability.optionDouble("delay_sec", 3)));
        }
        if (ability.equals(Abilities.STUN)) {
            return TextUtil.replace(
                    input,
                    "{reduction_percent}",
                    NumberUtil.format1(ability.optionDouble("speed_reduction", 0.2) * 100.0));
        }
        return input;
    }

    @Override
    public void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        if (meta.getDamageType() != DamageType.BOW) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        meta.addAttackModifier(bowMaster(attacker, user));
    }

    private DamageModifier bowMaster(ServerPlayer player, User user) {
        var ability = Abilities.BOW_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    /**
     * Bukkit piercing: chance to bump arrow pierce level by 1.
     *
     * @param forcedRoll {@link Double#NaN} uses live RNG
     */
    public static boolean applyPiercing(ServerPlayer player, AbstractArrow arrow, double forcedRoll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null || arrow == null) {
            return false;
        }
        FabricArcheryAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricArcheryAbilities.class);
        var ability = Abilities.PIERCING;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
        if (roll >= self.getValue(ability, user) / 100.0) {
            return false;
        }
        if (arrow.getPierceLevel() >= 127) {
            return false;
        }
        bumpPierceLevel(arrow);
        return true;
    }

    private static void bumpPierceLevel(AbstractArrow arrow) {
        try {
            var method = AbstractArrow.class.getDeclaredMethod("setPierceLevel", byte.class);
            method.setAccessible(true);
            method.invoke(arrow, (byte) (arrow.getPierceLevel() + 1));
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot bump arrow pierce level", ex);
        }
    }

    /**
     * Bukkit stun: chance to temporarily reduce target movement speed.
     *
     * @param forcedRoll {@link Double#NaN} uses live RNG
     */
    public static boolean applyStun(ServerPlayer player, LivingEntity target, double forcedRoll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null || target == null) {
            return false;
        }
        FabricArcheryAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricArcheryAbilities.class);
        var ability = Abilities.STUN;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
        if (roll >= self.getValue(ability, user) / 100.0) {
            return false;
        }
        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return false;
        }
        if (speed.getModifier(STUN_MODIFIER_ID) != null) {
            return false;
        }
        double reduction = ability.optionDouble("speed_reduction", 0.2);
        double attributeValue = -1.0 * speed.getValue() * reduction;
        speed.addPermanentModifier(
                new AttributeModifier(STUN_MODIFIER_ID, attributeValue, AttributeModifier.Operation.ADD_VALUE));
        plugin.getScheduler().scheduleSync(() -> {
            AttributeInstance current = target.getAttribute(Attributes.MOVEMENT_SPEED);
            if (current != null) {
                current.removeModifier(STUN_MODIFIER_ID);
            }
        }, 40L * 50L, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * Bukkit retrieval (immediate probe path): if arrow is within ability value blocks, give item and discard.
     */
    public static boolean applyRetrieval(
            ServerPlayer player, AbstractArrow arrow, ItemStack arrowItem, double forcedMaxDistance) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null || arrow == null || arrowItem == null || arrowItem.isEmpty()) {
            return false;
        }
        FabricArcheryAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricArcheryAbilities.class);
        var ability = Abilities.RETRIEVAL;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double value = Double.isNaN(forcedMaxDistance) ? self.getValue(ability, user) : forcedMaxDistance;
        double dx = arrow.getX() - player.getX();
        double dy = arrow.getY() - player.getY();
        double dz = arrow.getZ() - player.getZ();
        if (dx * dx + dy * dy + dz * dz > value * value) {
            return false;
        }
        ItemStack give = arrowItem.copy();
        give.setCount(1);
        if (!player.getInventory().add(give)) {
            return false;
        }
        arrow.discard();
        return true;
    }
}
