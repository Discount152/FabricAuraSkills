package dev.aurelium.auraskills.fabric.damage;

import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric-side damage context mirroring the data carried in Bukkit {@code DamageMeta} for trait math.
 */
public final class FabricDamageMeta {

    private final DamageType damageType;
    private final List<DamageModifier> attackModifiers = new ArrayList<>();
    private final List<DamageModifier> defenseModifiers = new ArrayList<>();
    private final @Nullable Entity attacker;
    private final LivingEntity target;
    private final DamageSource mcSource;
    private final String source;
    private double damage;

    public FabricDamageMeta(@Nullable Entity attacker, LivingEntity target, DamageType damageType,
            DamageSource mcSource, float damage, String source) {
        this.attacker = attacker;
        this.target = target;
        this.damageType = damageType;
        this.mcSource = mcSource;
        this.damage = damage;
        this.source = source;
    }

    public double getBaseDamage() {
        return damage;
    }

    public void setBaseDamage(double damage) {
        this.damage = damage;
    }

    public List<DamageModifier> getAttackModifiers() {
        return attackModifiers;
    }

    public void addAttackModifier(DamageModifier modifier) {
        this.attackModifiers.add(modifier);
    }

    public List<DamageModifier> getDefenseModifiers() {
        return defenseModifiers;
    }

    public void addDefenseModifier(DamageModifier modifier) {
        this.defenseModifiers.add(modifier);
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public @Nullable Entity getAttacker() {
        return attacker;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public DamageSource getMcSource() {
        return mcSource;
    }

    public String getSource() {
        return source;
    }
}
