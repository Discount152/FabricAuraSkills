package dev.granite.graniteskills.fabric.requirement;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

final class FabricBiomeNode extends FabricRequirementNode {

    private final String biome;

    FabricBiomeNode(FabricGraniteSkills plugin, String biome, String message) {
        super(plugin, message);
        this.biome = biome;
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (biome == null || !(player.level() instanceof ServerLevel sw)) {
            return false;
        }
        Identifier id = sw.registryAccess().lookupOrThrow(Registries.BIOME).getKey(sw.getBiome(player.blockPosition()).value());
        if (id == null) {
            return false;
        }
        String full = id.toString();
        String path = id.getPath();
        return biome.equalsIgnoreCase(full) || biome.equalsIgnoreCase(path) || biome.equalsIgnoreCase(id.getNamespace() + ":" + path);
    }
}

/**
 * WorldGuard-backed on Bukkit; without that hook on Fabric this node always passes (Bukkit returns true when WG absent).
 */
