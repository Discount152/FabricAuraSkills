package dev.granite.graniteskills.bukkit.requirement;

import dev.granite.graniteskills.bukkit.GraniteSkills;
import org.bukkit.entity.Player;

public class BiomeNode extends RequirementNode {

    private final String biome;

    public BiomeNode(GraniteSkills plugin, String biome, String message) {
        super(plugin, message);
        this.biome = biome;
    }

    @Override
    public boolean check(Player player) {
        if (!player.getLocation().getBlock().getBiome().toString().equalsIgnoreCase(biome)) {
            return false;
        }

        return true;
    }

}
