package dev.granite.graniteskills.api.event.damage;

import dev.granite.graniteskills.api.damage.DamageCalculator;
import dev.granite.graniteskills.api.damage.DamageMeta;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DamageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final DamageMeta damageMeta;

    private boolean cancelled = false;

    public DamageEvent(DamageMeta damageMeta) {
        this.damageMeta = damageMeta;
    }

    public DamageMeta getDamageMeta() {
        return damageMeta;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getModifiedAttackDamage() {
        return DamageCalculator.computeModifiedAttackDamage(
                damageMeta.getBaseDamage(),
                damageMeta.getAttackModifiers());
    }

    public double getModifiedDamage() {
        return DamageCalculator.computeModifiedDamage(
                damageMeta.getBaseDamage(),
                damageMeta.getAttackModifiers(),
                damageMeta.getDefenseModifiers());
    }

}
