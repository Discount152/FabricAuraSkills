package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public final class FabricTraitWorlds {

    private FabricTraitWorlds() {
    }

    /**
     * Same mapping as {@link FabricUser#worldNameCompat(ServerPlayerEntity)} for an arbitrary world.
     */
    public static String worldName(ServerWorld world) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            return "world";
        }
        return world.getRegistryKey().getValue().getPath();
    }

    public static boolean isInDisabledWorld(FabricAuraSkills plugin, ServerPlayerEntity player) {
        return plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player));
    }

    public static boolean isDisabledWorld(FabricAuraSkills plugin, ServerWorld world) {
        return plugin.getWorldManager().isDisabledWorld(worldName(world));
    }
}
