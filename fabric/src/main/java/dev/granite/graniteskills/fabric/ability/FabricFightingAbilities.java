package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.ability.AbilityData;
import dev.granite.graniteskills.common.message.type.AbilityMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import dev.granite.graniteskills.fabric.damage.FabricDamagePipeline;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class FabricFightingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    /** Same keys as Bukkit {@link dev.granite.graniteskills.bukkit.skills.fighting.FightingAbilities}. */
    public static final String PARRY_READY_KEY = "parry_ready";
    public static final String PARRY_VECTOR_KEY = "parry_vector";

    /** Mirrors Bukkit {@code GraniteSkills-FirstStrike} metadata: player cannot proc again until cooldown clears. */
    private final ConcurrentHashMap<java.util.UUID, Boolean> firstStrikeActive = new ConcurrentHashMap<>();

    public FabricFightingAbilities(FabricGraniteSkills plugin) {
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
    public static void onMainHandSwing(FabricGraniteSkills plugin, ServerPlayer player, InteractionHand hand) {
        if (plugin == null || hand != InteractionHand.MAIN_HAND) {
            return;
        }
        FabricFightingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricFightingAbilities.class);
        self.parryReadyOnSwing(player);
    }

    @Override
    public void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        if (meta.getDamageType() != DamageType.SWORD) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        meta.addAttackModifier(swordMaster(attacker, user));
        meta.addAttackModifier(firstStrike(plugin, attacker, user));
    }

    @Override
    public void contributeDefenseDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer victim) {
        if (!FabricDamagePipeline.hasAttackingPlayer(meta.getMcSource())) {
            return;
        }
        User user = plugin.getUserManager().getUser(victim.getUUID());
        meta.addDefenseModifier(handleParry(plugin, meta, victim, user));
    }

    private void parryReadyOnSwing(ServerPlayer player) {
        var ability = Abilities.PARRY;
        if (isDisabled(ability)) {
            return;
        }
        if (!player.getMainHandItem().is(ItemTags.SWORDS)) {
            return;
        }
        if (failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user.metadataBoolean(PARRY_READY_KEY)) {
            return;
        }
        Vec3 facing = player.getViewVector(1.0f);
        user.getMetadata().put(PARRY_READY_KEY, true);
        user.getMetadata().put(PARRY_VECTOR_KEY, facing);
        scheduleParryUnready(user);
    }

    private DamageModifier handleParry(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer player, User user) {
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
        if (ability.optionBoolean("enable_sound", true) && player.level() instanceof ServerLevel world) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0f, 1.4f);
        }

        Vec3 velBefore = player.getDeltaMovement();
        plugin.getScheduler().scheduleSync(() -> player.setDeltaMovement(velBefore), 50L, TimeUnit.MILLISECONDS);

        return new DamageModifier((1.0 - value / 100.0) - 1.0, DamageModifier.Operation.MULTIPLY);
    }

    private boolean isFacingCloseEnough(User user, ServerPlayer player, Entity damager) {
        Vec3 playerPos = new Vec3(player.getX(), player.getY(), player.getZ());
        Vec3 damagerPos = new Vec3(damager.getX(), damager.getY(), damager.getZ());
        Vec3 playerToDamager = damagerPos.subtract(playerPos);
        if (playerToDamager.lengthSqr() < 1.0e-8) {
            return false;
        }
        playerToDamager = playerToDamager.normalize();

        Object facingObj = user.getMetadata().get(PARRY_VECTOR_KEY);
        if (!(facingObj instanceof Vec3 facing)) {
            return false;
        }

        return playerToDamager.dot(facing) >= 0.7;
    }

    private void scheduleParryUnready(User user) {
        long delayMs = Abilities.PARRY.optionInt("time_ms", 250);
        plugin.getScheduler().scheduleSync(() -> {
            user.getMetadata().remove(PARRY_READY_KEY);
            user.getMetadata().remove(PARRY_VECTOR_KEY);
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private DamageModifier swordMaster(ServerPlayer player, User user) {
        var ability = Abilities.SWORD_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    private DamageModifier firstStrike(FabricGraniteSkills plugin, ServerPlayer player, User user) {
        var ability = Abilities.FIRST_STRIKE;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (Boolean.TRUE.equals(firstStrikeActive.get(player.getUUID()))) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        Locale locale = user.getLocale();
        if (ability.optionBoolean("enable_message", true)) {
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(AbilityMessage.FIRST_STRIKE_DEALT, locale));
        }
        firstStrikeActive.put(player.getUUID(), true);
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
                firstStrikeActive.remove(player.getUUID());
            }
        }, cooldownMs, TimeUnit.MILLISECONDS);

        double modifier = getValue(ability, user) / 100.0;
        return new DamageModifier(modifier, DamageModifier.Operation.ADD_COMBINED);
    }

    /** Used by {@link FabricBleedListener}. */
    boolean isBleedDisabledOrFails(ServerPlayer attacker) {
        var ability = Abilities.BLEED;
        return isDisabled(ability) || failsChecks(attacker, ability);
    }

    /** Used by {@link FabricBleedListener}. */
    boolean rollBleed(User user) {
        var ability = Abilities.BLEED;
        return rand.nextDouble() < getValue(ability, user) / 100.0;
    }
}
