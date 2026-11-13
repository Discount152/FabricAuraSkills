package dev.granite.graniteskills.bukkit.antiafk.checks;

import dev.granite.graniteskills.api.event.skill.EntityXpGainEvent;
import dev.granite.graniteskills.api.source.type.EntityXpSource;
import dev.granite.graniteskills.bukkit.antiafk.BukkitAntiAfkManager;
import dev.granite.graniteskills.bukkit.antiafk.BukkitCheck;
import dev.granite.graniteskills.bukkit.antiafk.BukkitCheckType;
import dev.granite.graniteskills.common.antiafk.IdentityHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class EntityC extends BukkitCheck {

    private final IdentityHandler handler;

    public EntityC(BukkitCheckType type, BukkitAntiAfkManager manager) {
        super(type, manager);
        this.handler = new IdentityHandler(optionInt("min_count"), "previous_entity");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityXpGain(EntityXpGainEvent event) {
        if (isDisabled() || !(event.getSource() instanceof EntityXpSource) || !(event.getAttacked() instanceof Player))
            return;

        Player player = event.getPlayer();
        if (handler.failsCheck(getCheckData(player), event.getAttacked().getUniqueId())) {
            event.setCancelled(true);
            logFail(player);
        }
    }

}
