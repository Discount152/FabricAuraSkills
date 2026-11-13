package dev.granite.graniteskills.bukkit.antiafk.checks;

import dev.granite.graniteskills.api.event.skill.XpGainEvent;
import dev.granite.graniteskills.api.source.type.EntityXpSource;
import dev.granite.graniteskills.bukkit.antiafk.BukkitAntiAfkManager;
import dev.granite.graniteskills.bukkit.antiafk.BukkitCheck;
import dev.granite.graniteskills.bukkit.antiafk.BukkitCheckType;
import dev.granite.graniteskills.bukkit.antiafk.HandlerFunctions;
import dev.granite.graniteskills.common.antiafk.FacingHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import static dev.granite.graniteskills.bukkit.ref.BukkitPlayerRef.wrap;

public class EntityB extends BukkitCheck {

    private final FacingHandler handler;

    public EntityB(BukkitCheckType type, BukkitAntiAfkManager manager) {
        super(type, manager);
        this.handler = new FacingHandler(optionInt("min_count"), HandlerFunctions::getYaw, HandlerFunctions::getPitch);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onXpGain(XpGainEvent event) {
        if (isDisabled() || !(event.getSource() instanceof EntityXpSource)) return;

        Player player = event.getPlayer();
        if (handler.failsCheck(getCheckData(player), wrap(player))) {
            event.setCancelled(true);
            logFail(player);
        }
    }

}
