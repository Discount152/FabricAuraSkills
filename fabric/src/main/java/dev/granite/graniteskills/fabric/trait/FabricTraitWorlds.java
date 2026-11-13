package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class FabricTraitWorlds {

    private FabricTraitWorlds() {
    }

    /**
     * Same mapping as {@link FabricUser#worldNameCompat(ServerPlayer)} for an arbitrary world.
     */
    public static String worldName(ServerLevel world) {
        if (world.dimension().equals(Level.OVERWORLD)) {
            return "world";
        }
        return world.dimension().identifier().getPath();
    }

    public static boolean isInDisabledWorld(FabricGraniteSkills plugin, ServerPlayer player) {
        return plugin.getWorldManager().isDisabledWorld(FabricUser.worldNameCompat(player));
    }

    public static boolean isDisabledWorld(FabricGraniteSkills plugin, ServerLevel world) {
        return plugin.getWorldManager().isDisabledWorld(worldName(world));
    }
}
