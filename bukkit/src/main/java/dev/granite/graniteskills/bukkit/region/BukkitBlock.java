package dev.granite.graniteskills.bukkit.region;

import dev.granite.graniteskills.common.region.BlockPosition;
import org.bukkit.block.Block;

public class BukkitBlock {

    public static BlockPosition from(Block block) {
        return new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

}
