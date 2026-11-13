package dev.granite.graniteskills.bukkit.hooks;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.hooks.Hook;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spongepowered.configurate.ConfigurationNode;

public class SlimefunHook extends Hook implements Listener {

    private final GraniteSkills plugin;

    public SlimefunHook(GraniteSkills plugin, ConfigurationNode config) {
        super(plugin, config);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlacerPlaceEvent(BlockPlacerPlaceEvent event) {
        if (!event.isCancelled()) {
            this.plugin.getRegionManager().addPlacedBlock(event.getBlock());
        }
    }

    public boolean hasBlockInfo(Location location) {
        return BlockStorage.hasBlockInfo(location);
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return SlimefunHook.class;
    }

}
