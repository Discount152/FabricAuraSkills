package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
            if (!(entity instanceof ServerPlayer player)) {
                return true;
            }
            if (amount <= 0f) {
                return true;
            }
            if (source.getEntity() == null) {
                return true;
            }
            if (!plugin.getUserManager().hasUser(player.getUUID())) {
                return true;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
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
            if (!(entity instanceof ServerPlayer victim)) {
                return;
            }
            if (blocked || damageTaken <= 0f) {
                return;
            }
            Entity direct = source.getDirectEntity();
            if (!(direct instanceof LivingEntity attacker)) {
                return;
            }
            if (attacker instanceof Player && attacker.getUUID().equals(victim.getUUID())) {
                return;
            }
            if (fireAspectLevel(attacker) <= 0) {
                return;
            }
            if (!plugin.getUserManager().hasUser(victim.getUUID())) {
                return;
            }
            User user = plugin.getUserManager().getUser(victim.getUUID());
            FabricDefenseAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricDefenseAbilities.class);
            impl.tryClearFireAspectIgnition(plugin, victim, user);
        });
    }

    /**
     * Used by {@link dev.aurelium.auraskills.fabric.mixin.LivingEntityNoDebuffSplashMixin}. Returns {@code true} to skip
     * applying this effect instance (Bukkit {@code setIntensity(0)}). Covers splash potions and lingering area clouds.
     */
    public static boolean shouldBlockSplashDebuff(LivingEntity target, MobEffectInstance effect, Entity source) {
        if (!(source instanceof AbstractThrownPotion) && !(source instanceof AreaEffectCloud)) {
            return false;
        }
        if (!(target instanceof ServerPlayer player)) {
            return false;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || !plugin.getUserManager().hasUser(player.getUUID())) {
            return false;
        }
        if (effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
            return false;
        }
        if (player.hasEffect(effect.getEffect())) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        FabricDefenseAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricDefenseAbilities.class);
        return impl.rollNoDebuffBlock(player, user);
    }

    private static int fireAspectLevel(LivingEntity attacker) {
        ItemStack stack = attacker.getMainHandItem();
        ItemEnchantments c = stack.get(DataComponents.ENCHANTMENTS);
        if (c == null || c.isEmpty()) {
            return 0;
        }
        return attacker.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.FIRE_ASPECT)
                .map(c::getLevel)
                .orElse(0);
    }

    private void tryClearFireAspectIgnition(FabricAuraSkills plugin, ServerPlayer victim, User user) {
        var ability = Abilities.NO_DEBUFF;
        if (isDisabled(ability) || failsChecks(victim, ability)) {
            return;
        }
        double chance = getValue(ability, user) / 100.0;
        if (rand.nextDouble() >= chance) {
            return;
        }
        plugin.getScheduler().scheduleSync(victim::clearFire, 50L, TimeUnit.MILLISECONDS);
    }

    private boolean rollNoDebuffBlock(ServerPlayer player, User user) {
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
    boolean shouldImmune(ServerPlayer player, User user) {
        var ability = Abilities.IMMUNITY;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return false;
        }
        double chance = getValue(ability, user) / 100.0;
        return rand.nextDouble() < chance;
    }

    @Override
    public void contributeDefenseDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayer victim) {
        User user = plugin.getUserManager().getUser(victim.getUUID());
        meta.addDefenseModifier(mobMaster(meta, user, victim));
        meta.addDefenseModifier(shielding(user, victim));
    }

    private DamageModifier shielding(User user, ServerPlayer player) {
        var ability = Abilities.SHIELDING;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (!player.isShiftKeyDown()) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        double damageReduction = 1.0 - (getValue(ability, user) / 100.0);
        return new DamageModifier(damageReduction - 1.0, DamageModifier.Operation.MULTIPLY);
    }

    private DamageModifier mobMaster(FabricDamageMeta meta, User user, ServerPlayer player) {
        var ability = Abilities.MOB_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        Entity attacker = meta.getAttacker();
        if (!(attacker instanceof LivingEntity) || attacker instanceof Player) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        double damageReduction = 1.0 - (getValue(ability, user) / 100.0);
        return new DamageModifier(damageReduction - 1.0, DamageModifier.Operation.MULTIPLY);
    }
}
