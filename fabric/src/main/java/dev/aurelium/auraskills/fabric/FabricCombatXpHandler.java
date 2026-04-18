package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityTriggers;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.ability.FabricBleedListener;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player {@link DamageXpSource} (defense, agility fall, etc.) and optional {@link EntityXpSource}
 * with {@link EntityTriggers#DAMAGE} when configured.
 */
public final class FabricCombatXpHandler {

    private final FabricAuraSkills plugin;
    private final ConcurrentHashMap<UUID, Long> playerDamageCooldownUntil = new ConcurrentHashMap<>();

    private FabricCombatXpHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void register(FabricAuraSkills plugin) {
        FabricCombatXpHandler handler = new FabricCombatXpHandler(plugin);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(handler::onAfterDamage);
    }

    private void onAfterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        if (!(entity.getEntityWorld() instanceof ServerWorld world) || world.isClient()) {
            return;
        }
        if (entity instanceof ServerPlayerEntity player) {
            handlePlayerDamaged(player, source, baseDamageTaken, damageTaken, blocked);
        } else {
            handleNonPlayerDamaged(entity, source, damageTaken);
        }
    }

    private void handlePlayerDamaged(ServerPlayerEntity player, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.DAMAGE)) {
            return;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return;
        }
        DamageCause skillsCause = FabricDamageCauseMapper.toApiCause(source);
        SkillSource<DamageXpSource> skillSource = findDamageXpSource(source, skillsCause);
        if (skillSource == null) {
            return;
        }
        DamageXpSource dx = skillSource.source();
        Skill skill = skillSource.skill();
        if (!skill.isEnabled()) {
            return;
        }
        // Match Bukkit DamageLeveler: block defense XP when shield blocking if the skill disallows it (see isBlocking too).
        if (skill == Skills.DEFENSE && !Skills.DEFENSE.optionBoolean("allow_shield_blocking", false)
                && (blocked || player.isBlocking())) {
            return;
        }
        long last = playerDamageCooldownUntil.getOrDefault(player.getUuid(), 0L);
        int cd = dx.getCooldownMs();
        if (cd > 0 && last + cd > System.currentTimeMillis()) {
            return;
        }
        if (isSelfInflicted(source, player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (!user.hasSkillPermission(skill)) {
            return;
        }
        String worldName = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
            return;
        }
        double dmg = dx.useOriginalDamage() ? baseDamageTaken : damageTaken;
        if (dmg <= 0) {
            return;
        }
        // Bukkit DamageLeveler: skip when this hit would kill the player and the source requires survival.
        if (dx.mustSurvive() && damageTaken >= player.getHealth()) {
            return;
        }
        double xp = dmg * dx.getXp();
        if (skill == Skills.DEFENSE) {
            xp = Math.min(xp, Skills.DEFENSE.optionDouble("max", 100.0));
            if (xp < Skills.DEFENSE.optionDouble("min", 0)) {
                return;
            }
        }
        plugin.getLevelManager().addXp(user, skill, dx, xp);
        playerDamageCooldownUntil.put(player.getUuid(), System.currentTimeMillis());
    }

    private void handleNonPlayerDamaged(LivingEntity victim, DamageSource source, float damageTaken) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.ENTITY)) {
            return;
        }
        if (!hasAnyEntityDamageTrigger()) {
            return;
        }
        if (victim instanceof ArmorStandEntity || damageTaken <= 0f) {
            return;
        }
        UUID bleedUser = FabricBleedListener.peekBleedAttackerUuid();
        ServerPlayerEntity player = resolvePlayerAttacker(source);
        if (player == null && bleedUser != null) {
            player = plugin.getServer().getPlayerManager().getPlayer(bleedUser);
        }
        EntityDamagers eventDamager = classifyPlayerEventDamager(source);
        if (eventDamager == null && bleedUser != null && player != null) {
            eventDamager = EntityDamagers.PLAYER;
        }
        if (player == null || eventDamager == null) {
            return;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return;
        }
        DamageCause skillsCause = FabricDamageCauseMapper.toApiCause(source);
        SkillSource<EntityXpSource> skillSource = findEntityDamageSource(victim, eventDamager, skillsCause);
        if (skillSource == null) {
            return;
        }
        EntityXpSource ex = skillSource.source();
        Skill skill = skillSource.skill();
        if (!skill.isEnabled()) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (!user.hasSkillPermission(skill)) {
            return;
        }
        String worldName = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
            return;
        }
        double xpPortion = damageTaken;
        if (ex.scaleXpWithHealth()) {
            EntityAttributeInstance max = victim.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (max != null && max.getValue() > 0) {
                xpPortion = damageTaken / max.getValue();
            }
        }
        double xp = xpPortion * ex.getXp() * FabricSpawnerMobTracker.xpMultiplier(victim, skill);
        plugin.getLevelManager().addXp(user, skill, ex, xp);
    }

    private boolean hasAnyEntityDamageTrigger() {
        for (SkillSource<EntityXpSource> entry : plugin.getSkillManager().getSourcesOfType(EntityXpSource.class)) {
            for (EntityTriggers t : entry.source().getTriggers()) {
                if (t == EntityTriggers.DAMAGE) {
                    return true;
                }
            }
        }
        return false;
    }

    private SkillSource<DamageXpSource> findDamageXpSource(DamageSource source, DamageCause skillsCause) {
        for (SkillSource<DamageXpSource> entry : plugin.getSkillManager().getSourcesOfType(DamageXpSource.class)) {
            DamageXpSource s = entry.source();
            if (!causesMatch(s, skillsCause)) {
                continue;
            }
            if (excludedCauseMatches(s, skillsCause)) {
                continue;
            }
            if (!excludedDamagersMatch(s, source)) {
                continue;
            }
            if (!damagersMatch(s, source)) {
                continue;
            }
            return entry;
        }
        return null;
    }

    private static boolean causesMatch(DamageXpSource source, DamageCause skillsCause) {
        DamageCause[] causes = source.getCauses();
        if (causes == null) {
            return true;
        }
        for (DamageCause c : causes) {
            if (c.toString().equals(skillsCause.toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean excludedCauseMatches(DamageXpSource source, DamageCause skillsCause) {
        DamageCause[] excluded = source.getExcludedCauses();
        if (excluded == null) {
            return false;
        }
        for (DamageCause c : excluded) {
            if (c.toString().equals(skillsCause.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean excludedDamagersMatch(DamageXpSource source, DamageSource mcSource) {
        String[] excluded = source.getExcludedDamagers();
        if (excluded == null) {
            return true;
        }
        Entity damager = getDirectDamager(mcSource);
        if (damager == null) {
            return false;
        }
        for (String name : excluded) {
            if (damagerMatches(source, damager, name)) {
                return false;
            }
        }
        return true;
    }

    private boolean damagersMatch(DamageXpSource source, DamageSource mcSource) {
        String[] damagers = source.getDamagers();
        if (damagers == null) {
            return true;
        }
        Entity damager = getDirectDamager(mcSource);
        if (damager == null) {
            return false;
        }
        for (String name : damagers) {
            if (damagerMatches(source, damager, name)) {
                return true;
            }
        }
        return false;
    }

    private boolean damagerMatches(DamageXpSource source, Entity damager, String name) {
        if (source.includeProjectiles() && damager instanceof ProjectileEntity projectile) {
            return projectileSourceMatches(projectile.getOwner(), name);
        }
        return entityDamagerMatches(damager, name);
    }

    private static boolean entityDamagerMatches(Entity damager, String name) {
        if ("mob".equalsIgnoreCase(name)) {
            return damager instanceof MobEntity;
        }
        if ("player".equalsIgnoreCase(name)) {
            return damager instanceof PlayerEntity;
        }
        String path = Registries.ENTITY_TYPE.getId(damager.getType()).getPath();
        return path.equalsIgnoreCase(name);
    }

    private static boolean projectileSourceMatches(Entity projectileSource, String name) {
        if (projectileSource == null) {
            return false;
        }
        if ("mob".equalsIgnoreCase(name)) {
            return projectileSource instanceof MobEntity;
        }
        if ("player".equalsIgnoreCase(name)) {
            return projectileSource instanceof PlayerEntity;
        }
        if (projectileSource instanceof LivingEntity living) {
            return Registries.ENTITY_TYPE.getId(living.getType()).getPath().equalsIgnoreCase(name);
        }
        return false;
    }

    private static Entity getDirectDamager(DamageSource source) {
        Entity s = source.getSource();
        if (s != null) {
            return s;
        }
        return source.getAttacker();
    }

    private static boolean isSelfInflicted(DamageSource source, ServerPlayerEntity victim) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity p && p.getUuid().equals(victim.getUuid())) {
            return true;
        }
        Entity direct = source.getSource();
        if (direct instanceof ProjectileEntity proj) {
            Entity owner = proj.getOwner();
            if (owner != null && owner.getUuid().equals(victim.getUuid())) {
                return true;
            }
        }
        return false;
    }

    private static ServerPlayerEntity resolvePlayerAttacker(DamageSource source) {
        Entity direct = source.getSource();
        if (direct instanceof ServerPlayerEntity sp) {
            return sp;
        }
        if (direct instanceof ProjectileEntity proj) {
            Entity owner = proj.getOwner();
            if (owner instanceof ServerPlayerEntity sp) {
                return sp;
            }
        }
        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity sp) {
            return sp;
        }
        return null;
    }

    private static EntityDamagers classifyPlayerEventDamager(DamageSource source) {
        Entity direct = source.getSource();
        if (direct instanceof ServerPlayerEntity) {
            DamageCause c = FabricDamageCauseMapper.toApiCause(source);
            if (c == DamageCause.ENTITY_ATTACK || c == DamageCause.ENTITY_SWEEP_ATTACK) {
                return EntityDamagers.PLAYER;
            }
            return null;
        }
        if (direct instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) direct).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                return EntityDamagers.PROJECTILE;
            }
        }
        return null;
    }

    private SkillSource<EntityXpSource> findEntityDamageSource(LivingEntity entity, EntityDamagers eventDamager, DamageCause damageCause) {
        List<SkillSource<EntityXpSource>> filtered = new ArrayList<>();
        for (SkillSource<EntityXpSource> entry : plugin.getSkillManager().getSourcesOfType(EntityXpSource.class)) {
            for (EntityTriggers t : entry.source().getTriggers()) {
                if (t == EntityTriggers.DAMAGE) {
                    filtered.add(entry);
                    break;
                }
            }
        }
        String entityPath = Registries.ENTITY_TYPE.getId(entity.getType()).getPath();
        for (SkillSource<EntityXpSource> entry : filtered) {
            EntityXpSource source = entry.source();
            String configured = plugin.getPlatformUtil().convertEntityName(source.getEntity().toLowerCase(Locale.ROOT));
            if (!entityPath.equalsIgnoreCase(configured)) {
                continue;
            }
            DamageCause[] causes = source.getCauses();
            if (causes != null) {
                boolean ok = false;
                for (DamageCause c : causes) {
                    if (c.toString().equals(damageCause.toString())) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    continue;
                }
            }
            DamageCause[] excludedCauses = source.getExcludedCauses();
            if (excludedCauses != null) {
                boolean skip = false;
                for (DamageCause c : excludedCauses) {
                    if (c.toString().equals(damageCause.toString())) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
            }
            for (EntityDamagers sd : source.getDamagers()) {
                if (sd == eventDamager) {
                    return entry;
                }
            }
        }
        return null;
    }
}
