package dev.granite.graniteskills.bukkit.hooks;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.hooks.Hook;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DecentHologramsHook extends HologramsHook {

    private final GraniteSkills plugin;

    public DecentHologramsHook(GraniteSkills plugin, ConfigurationNode config) {
        super(plugin, config);
        this.plugin = plugin;
    }

    @Override
    public void createHologram(Location location, String text, Player player) {
        Hologram hologram = DHAPI.createHologram("GraniteSkills_" + UUID.randomUUID(), location, false, Collections.singletonList(text));
        deleteHologram(hologram);
    }

    public void deleteHologram(Hologram dh) {
        plugin.getScheduler().scheduleAtLocation(dh.getLocation(), dh::delete, 30L * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public Class<? extends Hook> getTypeClass() {
        return HologramsHook.class;
    }

}
