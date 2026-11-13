package dev.granite.graniteskills.bukkit.damage;

import dev.granite.graniteskills.api.damage.DamageMeta;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.event.damage.DamageEvent;
import dev.granite.graniteskills.common.damage.DamageResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public class DamageHandler {

    public DamageResult handleDamage(@Nullable Entity attacker, Entity target, DamageType damageType, EntityDamageEvent.DamageCause damageCause, double damage, String source) {
        var damageMeta = new DamageMeta(attacker, target, damageType, damageCause, damage, source);

        var event = new DamageEvent(damageMeta);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return new DamageResult(damage, true);
        }

        double finalDamage = event.getModifiedDamage();

        return new DamageResult(finalDamage, false);
    }

}
