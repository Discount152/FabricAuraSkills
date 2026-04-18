package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.ability.AbilityData;
import dev.aurelium.auraskills.common.message.type.AbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import dev.aurelium.auraskills.fabric.damage.FabricDamagePipeline;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class FabricFightingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    /** Same keys as Bukkit {@link dev.aurelium.auraskills.bukkit.skills.fighting.FightingAbilities}. */
    public static final String PARRY_READY_KEY = "parry_ready";
    public static final String PARRY_VECTOR_KEY = "parry_vector";

    /** Mirrors Bukkit {@code AureliumSkills-FirstStrike} metadata: player cannot proc again until cooldown clears. */
    private final ConcurrentHashMap<java.util.UUID, Boolean> firstStrikeActive = new ConcurrentHashMap<>();

    public FabricFightingAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.PARRY, Abilities.FIGHTER, Abilities.SWORD_MASTER, Abilities.FIRST_STRIKE, Abilities.BLEED);
    }

    @Override
    public String replaceDescPlaceholders(String input, Ability ability, User user) {
        if (ability.equals(Abilities.BLEED)) {
            return TextUtil.replace(input,
                    "{base_ticks}", String.valueOf(ability.optionInt("base_ticks", 3)),
                    "{added_ticks}", String.valueOf(ability.optionInt("added_ticks", 2)));
        }
        if (ability.equals(Abilities.PARRY)) {
            String secDisplay = NumberUtil.format2((double) ability.optionInt("time_ms", 250) / 1000.0);
            return TextUtil.replace(input, "{time}", secDisplay);
        }
        return input;
    }

    /**
     * Called from {@code ServerPlayerEntityParrySwingMixin} when the client swings the main hand (Fabric analogue of
     * Bukkit {@code PlayerInteractEvent} left-click readiness).
     */
    public static void onMainHandSwing(FabricAuraSkills plugin, ServerPlayerEntity player, Hand hand) {
        if (plugin == null || hand != Hand.MAIN_HAND) {
            return;
        }
        FabricFightingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricFightingAbilities.class);
        self.parryReadyOnSwing(player);
    }

    @Override
    public void contributeAttackDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity attacker) {
        if (meta.getDamageType() != DamageType.SWORD) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUuid());
        meta.addAttackModifier(swordMaster(attacker, user));
        meta.addAttackModifier(firstStrike(plugin, attacker, user));
    }

    @Override
    public void contributeDefenseDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity victim) {
        if (!FabricDamagePipeline.hasAttackingPlayer(meta.getMcSource())) {
            return;
        }
        User user = plugin.getUserManager().getUser(victim.getUuid());
        meta.addDefenseModifier(handleParry(plugin, meta, victim, user));
    }

    private void parryReadyOnSwing(ServerPlayerEntity player) {
        var ability = Abilities.PARRY;
        if (isDisabled(ability)) {
            return;
        }
        if (!player.getMainHandStack().isIn(ItemTags.SWORDS)) {
            return;
        }
        if (failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user.metadataBoolean(PARRY_READY_KEY)) {
            return;
        }
        Vec3d facing = player.getRotationVec(1.0f);
        user.getMetadata().put(PARRY_READY_KEY, true);
        user.getMetadata().put(PARRY_VECTOR_KEY, facing);
        scheduleParryUnready(user);
    }

    private DamageModifier handleParry(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity player, User user) {
        var ability = Abilities.PARRY;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (!user.metadataBoolean(PARRY_READY_KEY)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        Entity attacker = meta.getAttacker();
        if (attacker != null && !isFacingCloseEnough(user, player, attacker)) {
            return DamageModifier.none();
        }

        double value = getValue(ability, user);
        Locale locale = user.getLocale();
        plugin.getUiProvider().sendActionBar(user, plugin.getMsg(AbilityMessage.PARRY_PARRIED, locale));
        plugin.getUiProvider().getActionBarManager().setPaused(user, 1500, TimeUnit.MILLISECONDS);
        if (ability.optionBoolean("enable_sound", true) && player.getEntityWorld() instanceof ServerWorld world) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_TRIDENT_RETURN, SoundCategory.PLAYERS, 1.0f, 1.4f);
        }

        Vec3d velBefore = player.getVelocity();
        plugin.getScheduler().scheduleSync(() -> player.setVelocity(velBefore), 50L, TimeUnit.MILLISECONDS);

        return new DamageModifier((1.0 - value / 100.0) - 1.0, DamageModifier.Operation.MULTIPLY);
    }

    private boolean isFacingCloseEnough(User user, ServerPlayerEntity player, Entity damager) {
        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d damagerPos = new Vec3d(damager.getX(), damager.getY(), damager.getZ());
        Vec3d playerToDamager = damagerPos.subtract(playerPos);
        if (playerToDamager.lengthSquared() < 1.0e-8) {
            return false;
        }
        playerToDamager = playerToDamager.normalize();

        Object facingObj = user.getMetadata().get(PARRY_VECTOR_KEY);
        if (!(facingObj instanceof Vec3d facing)) {
            return false;
        }

        return playerToDamager.dotProduct(facing) >= 0.7;
    }

    private void scheduleParryUnready(User user) {
        long delayMs = Abilities.PARRY.optionInt("time_ms", 250);
        plugin.getScheduler().scheduleSync(() -> {
            user.getMetadata().remove(PARRY_READY_KEY);
            user.getMetadata().remove(PARRY_VECTOR_KEY);
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private DamageModifier swordMaster(ServerPlayerEntity player, User user) {
        var ability = Abilities.SWORD_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    private DamageModifier firstStrike(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        var ability = Abilities.FIRST_STRIKE;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (Boolean.TRUE.equals(firstStrikeActive.get(player.getUuid()))) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        Locale locale = user.getLocale();
        if (ability.optionBoolean("enable_message", true)) {
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(AbilityMessage.FIRST_STRIKE_DEALT, locale));
        }
        firstStrikeActive.put(player.getUuid(), true);
        AbilityData abilityData = user.getAbilityData(ability);
        if (abilityData.containsKey("counter")) {
            abilityData.setData("counter", abilityData.getInt("counter") + 1);
        } else {
            abilityData.setData("counter", 0);
        }
        int id = abilityData.getInt("counter");
        long cooldownMs = ability.optionInt("cooldown_ticks", 6000) * 50L;
        plugin.getScheduler().scheduleSync(() -> {
            if (user.getAbilityData(ability).containsKey("counter") && user.getAbilityData(ability).getInt("counter") == id) {
                firstStrikeActive.remove(player.getUuid());
            }
        }, cooldownMs, TimeUnit.MILLISECONDS);

        double modifier = getValue(ability, user) / 100.0;
        return new DamageModifier(modifier, DamageModifier.Operation.ADD_COMBINED);
    }

    /** Used by {@link FabricBleedListener}. */
    boolean isBleedDisabledOrFails(ServerPlayerEntity attacker) {
        var ability = Abilities.BLEED;
        return isDisabled(ability) || failsChecks(attacker, ability);
    }

    /** Used by {@link FabricBleedListener}. */
    boolean rollBleed(User user) {
        var ability = Abilities.BLEED;
        return rand.nextDouble() < getValue(ability, user) / 100.0;
    }
}
