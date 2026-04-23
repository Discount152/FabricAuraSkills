package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Matches Bukkit {@code EntityLeveler#getSource(LivingEntity, EntityDamagers, trigger, damageCause)} for
 * {@link EntityXpSource} (entity path + cause + damager gating) — used for mob table skill resolution, mirroring
 * {@code MobLootHandler}.
 */
public final class FabricEntityXpSourceLookup {

    private FabricEntityXpSourceLookup() {
    }

    /**
     * Bukkit: DEATH, then if null DAMAGE — same as {@code MobLootHandler}.
     */
    public static @Nullable SkillSource<EntityXpSource> forMobLootSkill(
            FabricAuraSkills plugin,
            LivingEntity entity,
            EntityDamagers eventDamager,
            DamageCause damageCause) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return null;
        }
        SkillSource<EntityXpSource> a = find(plugin, entity, eventDamager, EntityTriggers.DEATH, damageCause);
        if (a != null) {
            return a;
        }
        return find(plugin, entity, eventDamager, EntityTriggers.DAMAGE, damageCause);
    }

    /**
     * Bukkit {@code EntityLeveler#getSource(..., EntityTriggers.DEATH, damageCause)} — used for on-death XP only
     * (no DAMAGE trigger, no fallback to DAMAGE).
     */
    public static @Nullable SkillSource<EntityXpSource> forEntityDeathKill(
            FabricAuraSkills plugin,
            LivingEntity entity,
            EntityDamagers eventDamager,
            DamageCause damageCause) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return null;
        }
        return find(plugin, entity, eventDamager, EntityTriggers.DEATH, damageCause);
    }

    /**
     * Bukkit {@code EntityLeveler#getSource(..., EntityTriggers.DAMAGE, ...)} for {@link EntityTriggers#DAMAGE} hits only
     * (used by live combat; no DEATH trigger).
     */
    public static @Nullable SkillSource<EntityXpSource> forEntityDamageOnly(
            FabricAuraSkills plugin,
            LivingEntity entity,
            EntityDamagers eventDamager,
            DamageCause damageCause) {
        if (plugin == null || !plugin.isRuntimeReady()) {
            return null;
        }
        return find(plugin, entity, eventDamager, EntityTriggers.DAMAGE, damageCause);
    }

    private static @Nullable SkillSource<EntityXpSource> find(
            FabricAuraSkills plugin,
            LivingEntity entity,
            EntityDamagers eventDamager,
            EntityTriggers trigger,
            DamageCause damageCause) {
        List<SkillSource<EntityXpSource>> filtered = new ArrayList<>();
        for (SkillSource<EntityXpSource> entry : plugin.getSkillManager().getSourcesOfType(EntityXpSource.class)) {
            for (EntityTriggers t : entry.source().getTriggers()) {
                if (t == trigger) {
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
                    if (eventDamager == EntityDamagers.THROWN_POTION) {
                        return new SkillSource<>(source, Skills.ALCHEMY);
                    }
                    return entry;
                }
            }
        }
        return null;
    }
}
