package dev.granite.graniteskills.bukkit.api.implementation;

import dev.granite.graniteskills.api.region.Regions;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.block.Block;

public class ApiRegions implements Regions {

    private final GraniteSkills plugin;

    public ApiRegions(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isPlacedBlock(Block block) {
        return plugin.getRegionManager().isPlacedBlock(block);
    }

    @Override
    public void addPlacedBlock(Block block) {
        plugin.getRegionManager().addPlacedBlock(block);
    }

}
