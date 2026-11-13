package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.common.message.type.AbilityMessage;
import dev.aurelium.auraskills.common.scheduler.Task;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricDamageCauseMapper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.fighting.FightingAbilities#bleedListener} and
 * {@code scheduleBleedTicks}. Proc requires {@link DamageSource#getDirectEntity()} to be the attacking player (melee sword),
 * matching {@code EntityDamageByEntityEvent} with a player damager. Tick damage uses {@link ServerLevel#damageSources()
 * #generic()} so bleed DoT does not re-trigger this listener.
 */
public final class FabricBleedListener {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final ConcurrentHashMap<UUID, Integer> BLEED_TICKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Task> BLEED_TASKS = new ConcurrentHashMap<>();
    /**
     * While set, {@link FabricCombatXpHandler} attributes generic bleed DoT to this user for {@link EntityTriggers#DAMAGE}
     * XP (Bukkit bleed damager PDC parity).
     */
    private static final ThreadLocal<UUID> BLEED_ATTACKER_USER = new ThreadLocal<>();

    private FabricBleedListener() {
    }

    public static void runWithBleedAttribution(User bleedingAttackerUser, Runnable during) {
        if (bleedingAttackerUser == null) {
            during.run();
            return;
        }
        try {
            BLEED_ATTACKER_USER.set(bleedingAttackerUser.getUuid());
            during.run();
        } finally {
            BLEED_ATTACKER_USER.remove();
        }
    }

    @Nullable
    public static UUID peekBleedAttackerUuid() {
        return BLEED_ATTACKER_USER.get();
    }

    public static void register(FabricAuraSkills plugin) {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) ->
                        onAfterDamage(plugin, entity, source, damageTaken, blocked));
    }

    private static void onAfterDamage(FabricAuraSkills plugin, LivingEntity entity, DamageSource source,
            float damageTaken, boolean blocked) {
        if (!(entity.level() instanceof ServerLevel world) || world.isClientSide()) {
            return;
        }
        if (entity instanceof ArmorStand || blocked || damageTaken <= 0f || !entity.isAlive()) {
            return;
        }
        if (!(source.getDirectEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        if (FabricDamageCauseMapper.toApiCause(source) == DamageCause.THORNS) {
            return;
        }
        if (!attacker.getMainHandItem().is(ItemTags.SWORDS)) {
            return;
        }
        FabricFightingAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricFightingAbilities.class);
        if (impl.isBleedDisabledOrFails(attacker)) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        if (!impl.rollBleed(user)) {
            return;
        }
        applyOrStackBleed(plugin, entity, user, Abilities.BLEED);
    }

    private static void applyOrStackBleed(FabricAuraSkills plugin, LivingEntity victim,
            User attackerUser, Ability ability) {
        UUID id = victim.getUUID();
        if (!BLEED_TICKS.containsKey(id)) {
            int baseTicks = ability.optionInt("base_ticks", 3);
            BLEED_TICKS.put(id, baseTicks);
            if (ability.optionBoolean("enable_enemy_message", true)) {
                Locale locale = attackerUser.getLocale();
                plugin.getAbilityManager().sendAbilityMessage(attackerUser,
                        plugin.getMsg(AbilityMessage.BLEED_ENEMY_BLEEDING, locale));
            }
            if (victim instanceof ServerPlayer victimPlayer) {
                if (ability.optionBoolean("enable_self_message", true)) {
                    User victimUser = plugin.getUserManager().getUser(victimPlayer.getUUID());
                    Locale locale = victimUser.getLocale();
                    plugin.getAbilityManager().sendAbilityMessage(victimUser,
                            plugin.getMsg(AbilityMessage.BLEED_SELF_BLEEDING, locale));
                }
            }
            startBleedTickerIfNeeded(plugin, victim, attackerUser, ability);
        } else {
            int current = BLEED_TICKS.getOrDefault(id, 0);
            int addedTicks = ability.optionInt("added_ticks", 2);
            int maxTicks = ability.optionInt("max_ticks", 11);
            int resulting = current + addedTicks;
            if (resulting <= maxTicks) {
                BLEED_TICKS.put(id, resulting);
            }
        }
    }

    private static void startBleedTickerIfNeeded(FabricAuraSkills plugin, LivingEntity entity, User attackerUser,
            Ability ability) {
        UUID id = entity.getUUID();
        if (BLEED_TASKS.containsKey(id)) {
            return;
        }
        long initialDelayMs = 40L * 50L;
        long periodMs = (long) ability.optionInt("tick_period", 40) * 50L;
        TaskRunnable runnable = new TaskRunnable() {
            @Override
            public void run() {
                if (entity.isRemoved() || !entity.isAlive()) {
                    cancelBleedFully(plugin, id, entity, ability, false);
                    return;
                }
                Integer ticksObj = BLEED_TICKS.get(id);
                if (ticksObj == null) {
                    cancelBleedFully(plugin, id, entity, ability, true);
                    return;
                }
                int bleedTicks = ticksObj;
                if (bleedTicks <= 0) {
                    BLEED_TICKS.remove(id);
                    cancelBleedFully(plugin, id, entity, ability, true);
                    return;
                }
                if (!(entity.level() instanceof ServerLevel sw)) {
                    cancelBleedFully(plugin, id, entity, ability, false);
                    return;
                }
                double damage = ability.getSecondaryValue(attackerUser.getAbilityLevel(ability));
                float healthBefore = entity.getHealth();
                DamageSource dot = sw.damageSources().generic();
                runWithBleedAttribution(attackerUser, () -> entity.hurtServer(sw, dot, (float) damage));
                entity.setInvulnerableTime(0);
                float healthAfter = entity.getHealth();
                if (healthAfter != healthBefore) {
                    spawnBleedParticles(entity, ability, sw);
                }
                if (bleedTicks != 1) {
                    BLEED_TICKS.put(id, bleedTicks - 1);
                } else {
                    BLEED_TICKS.remove(id);
                }
            }
        };
        Task task = plugin.getScheduler().timerSync(runnable, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
        BLEED_TASKS.put(id, task);
    }

    private static void cancelBleedFully(FabricAuraSkills plugin, UUID id, LivingEntity entity, Ability ability,
            boolean sendStopToVictimPlayer) {
        Task t = BLEED_TASKS.remove(id);
        if (t != null) {
            t.cancel();
        }
        BLEED_TICKS.remove(id);
        if (sendStopToVictimPlayer && entity instanceof ServerPlayer victimPlayer) {
            if (ability.optionBoolean("enable_stop_message", true)) {
                User victimUser = plugin.getUserManager().getUser(victimPlayer.getUUID());
                Locale locale = victimUser.getLocale();
                plugin.getAbilityManager().sendAbilityMessage(victimUser,
                        plugin.getMsg(AbilityMessage.BLEED_STOP, locale));
            }
        }
    }

    private static void spawnBleedParticles(LivingEntity entity, Ability ability, ServerLevel world) {
        if (!ability.optionBoolean("show_particles", true)) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.6;
        double z = entity.getZ();
        world.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 30, 0.25, 0.5, 0.25, 0.12);
    }
}
