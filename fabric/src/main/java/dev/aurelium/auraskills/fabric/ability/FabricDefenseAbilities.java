package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FabricDefenseAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    private static final AtomicBoolean IMMUNITY_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean NO_DEBUFF_FIRE_REGISTERED = new AtomicBoolean(false);

    public FabricDefenseAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.SHIELDING, Abilities.DEFENDER, Abilities.MOB_MASTER, Abilities.IMMUNITY, Abilities.NO_DEBUFF);
    }

    /**
     * Bukkit {@code DefenseAbilities#immunity} runs on {@code EntityDamageByEntityEvent}; Fabric uses
     * {@link ServerLivingEntityEvents#ALLOW_DAMAGE}. Registered before {@link dev.aurelium.auraskills.fabric.FabricAbsorption}
     * so Absorption can still process hits that were not randomly negated.
     */
    public static void registerImmunityListeners(FabricAuraSkills plugin) {
        if (!IMMUNITY_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity entity, DamageSource source, float amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }
            if (amount <= 0f) {
                return true;
            }
            if (source.getAttacker() == null) {
                return true;
            }
            if (!plugin.getUserManager().hasUser(player.getUuid())) {
                return true;
            }
            User user = plugin.getUserManager().getUser(player.getUuid());
            FabricDefenseAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricDefenseAbilities.class);
            if (impl.shouldImmune(player, user)) {
                return false;
            }
            return true;
        });
    }

    /**
     * Bukkit {@code DefenseAbilities#noDebuffFire} via {@code EntityDamageByEntityEvent} (non-cancelled hits).
     */
    public static void registerNoDebuffFireListener(FabricAuraSkills plugin) {
        if (!NO_DEBUFF_FIRE_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        ServerLivingEntityEvents.AFTER_DAMAGE.register((LivingEntity entity, DamageSource source, float baseDamageTaken,
                float damageTaken, boolean blocked) -> {
            if (!(entity instanceof ServerPlayerEntity victim)) {
                return;
            }
            if (blocked || damageTaken <= 0f) {
                return;
            }
            Entity direct = source.getSource();
            if (!(direct instanceof LivingEntity attacker)) {
                return;
            }
            if (attacker instanceof PlayerEntity && attacker.getUuid().equals(victim.getUuid())) {
                return;
            }
            if (fireAspectLevel(attacker) <= 0) {
                return;
            }
            if (!plugin.getUserManager().hasUser(victim.getUuid())) {
                return;
            }
            User user = plugin.getUserManager().getUser(victim.getUuid());
            FabricDefenseAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricDefenseAbilities.class);
            impl.tryClearFireAspectIgnition(plugin, victim, user);
        });
    }

    /**
     * Used by {@link dev.aurelium.auraskills.fabric.mixin.LivingEntityNoDebuffSplashMixin}. Returns {@code true} to skip
     * applying this effect instance (Bukkit {@code setIntensity(0)}). Covers splash potions and lingering area clouds.
     */
    public static boolean shouldBlockSplashDebuff(LivingEntity target, StatusEffectInstance effect, Entity source) {
        if (!(source instanceof PotionEntity) && !(source instanceof AreaEffectCloudEntity)) {
            return false;
        }
        if (!(target instanceof ServerPlayerEntity player)) {
            return false;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.getUserManager().hasUser(player.getUuid())) {
            return false;
        }
        if (effect.getEffectType().value().getCategory() != StatusEffectCategory.HARMFUL) {
            return false;
        }
        if (player.hasStatusEffect(effect.getEffectType())) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        FabricDefenseAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricDefenseAbilities.class);
        return impl.rollNoDebuffBlock(player, user);
    }

    private static int fireAspectLevel(LivingEntity attacker) {
        ItemStack stack = attacker.getMainHandStack();
        ItemEnchantmentsComponent c = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (c == null || c.isEmpty()) {
            return 0;
        }
        return attacker.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT)
                .getOptional(Enchantments.FIRE_ASPECT)
                .map(c::getLevel)
                .orElse(0);
    }

    private void tryClearFireAspectIgnition(FabricAuraSkills plugin, ServerPlayerEntity victim, User user) {
        var ability = Abilities.NO_DEBUFF;
        if (isDisabled(ability) || failsChecks(victim, ability)) {
            return;
        }
        double chance = getValue(ability, user) / 100.0;
        if (rand.nextDouble() >= chance) {
            return;
        }
        plugin.getScheduler().scheduleSync(victim::extinguish, 50L, TimeUnit.MILLISECONDS);
    }

    private boolean rollNoDebuffBlock(ServerPlayerEntity player, User user) {
        var ability = Abilities.NO_DEBUFF;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return false;
        }
        double chance = getValue(ability, user) / 100.0;
        return rand.nextDouble() < chance;
    }

    /**
     * @return {@code true} if this hit should be fully cancelled (Bukkit {@code event.setCancelled(true)}).
     */
    boolean shouldImmune(ServerPlayerEntity player, User user) {
        var ability = Abilities.IMMUNITY;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return false;
        }
        double chance = getValue(ability, user) / 100.0;
        return rand.nextDouble() < chance;
    }

    @Override
    public void contributeDefenseDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity victim) {
        User user = plugin.getUserManager().getUser(victim.getUuid());
        meta.addDefenseModifier(mobMaster(meta, user, victim));
        meta.addDefenseModifier(shielding(user, victim));
    }

    private DamageModifier shielding(User user, ServerPlayerEntity player) {
        var ability = Abilities.SHIELDING;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (!player.isSneaking()) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        double damageReduction = 1.0 - (getValue(ability, user) / 100.0);
        return new DamageModifier(damageReduction - 1.0, DamageModifier.Operation.MULTIPLY);
    }

    private DamageModifier mobMaster(FabricDamageMeta meta, User user, ServerPlayerEntity player) {
        var ability = Abilities.MOB_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        Entity attacker = meta.getAttacker();
        if (!(attacker instanceof LivingEntity) || attacker instanceof PlayerEntity) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        double damageReduction = 1.0 - (getValue(ability, user) / 100.0);
        return new DamageModifier(damageReduction - 1.0, DamageModifier.Operation.MULTIPLY);
    }
}
