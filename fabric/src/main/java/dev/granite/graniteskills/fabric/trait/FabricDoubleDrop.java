package dev.granite.graniteskills.fabric.trait;

import dev.granite.graniteskills.api.trait.Traits;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class FabricDoubleDrop {

    private FabricDoubleDrop() {
    }

    public static void tryDuplicate(FabricGraniteSkills plugin, ServerPlayer player, ServerLevel level,
            BlockPos pos, BlockState state) {
        if (!Traits.DOUBLE_DROP.isEnabled() || plugin == null) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        double chance = user.getBonusTraitLevel(Traits.DOUBLE_DROP);
        double maxPercent = Traits.DOUBLE_DROP.optionDouble("max_percent", 100);
        if (chance > maxPercent) {
            chance = maxPercent;
        }
        if (chance <= 0.0 || player.getRandom().nextDouble() * 100.0 >= chance) {
            return;
        }
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop.copy());
        }
    }
}
