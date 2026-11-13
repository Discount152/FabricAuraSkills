package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.api.trait.Trait;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.ability.FabricMiningAbilities;
import dev.granite.graniteskills.fabric.trait.FabricGatheringLuckTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ports Bukkit {@link dev.granite.graniteskills.bukkit.source.BlockLeveler#applyBlockLuck} for
 * {@link dev.granite.graniteskills.api.source.type.BlockXpSource.BlockTriggers#INTERACT} /
 * {@link dev.granite.graniteskills.api.source.type.BlockXpSource.BlockTriggers#COLLECT} /
 * {@link dev.granite.graniteskills.api.source.type.BlockXpSource.BlockTriggers#BREAK}. No {@code LootDropEvent} on Fabric;
 * items go to inventory or spawn at block center like Bukkit {@code ItemUtils.giveBlockLoot}.
 */
public final class FabricInteractGatheringLuck {

    private static final Random RANDOM = new Random();

    private FabricInteractGatheringLuck() {
    }

    public static void tryApply(FabricGraniteSkills plugin, ServerPlayer player, User user, Skill skill,
            XpSource xpSource, ServerLevel world, BlockPos pos, BlockState state) {
        if (player.getAbilities().instabuild) {
            return;
        }
        Trait trait = FabricGatheringLuckTraits.traitForSkill(skill);
        if (trait == null || !trait.isEnabled()) {
            return;
        }
        var tag = FabricGatheringLuckTraits.luckTagForSkill(skill);
        if (tag != null && !plugin.getSkillManager().hasTag(xpSource, tag)) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (silkTouchLevel(world, tool) > 0) {
            FabricMiningAbilities mining = plugin.getAbilityManager().getAbilityImpl(FabricMiningAbilities.class);
            if (mining.dropsMineralDirectly(state)) {
                return;
            }
        }
        List<ItemStack> unique = uniqueDroppedStacks(world, player, pos, state, tool);
        for (ItemStack dropTemplate : unique) {
            int numExtra = rollExtraDrops(user, trait);
            if (numExtra <= 0) {
                continue;
            }
            ItemStack dropped = dropTemplate.copyWithCount(numExtra);
            if (plugin.getFabricLootManager().toInventory(player.getMainHandItem())) {
                player.getInventory().placeItemBackInInventory(dropped, Prediction.SERVER_ONLY);
            } else {
                Vec3 at = Vec3.atCenterOf(pos);
                world.addFreshEntity(new ItemEntity(world, at.x, at.y, at.z, dropped));
            }
        }
    }

    private static int silkTouchLevel(ServerLevel world, ItemStack stack) {
        ItemEnchantments c = stack.get(DataComponents.ENCHANTMENTS);
        if (c == null || c.isEmpty()) {
            return 0;
        }
        return world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.SILK_TOUCH)
                .map(c::getLevel)
                .orElse(0);
    }

    private static List<ItemStack> uniqueDroppedStacks(ServerLevel world, ServerPlayer player, BlockPos pos,
            BlockState state, ItemStack tool) {
        BlockEntity be = world.getBlockEntity(pos);
        List<ItemStack> raw = Block.getDrops(state, world, pos, be, player, tool);
        List<ItemStack> unique = new ArrayList<>();
        for (ItemStack item : raw) {
            if (item.isEmpty()) {
                continue;
            }
            boolean already = false;
            for (ItemStack u : unique) {
                if (ItemStack.isSameItemSameComponents(u, item)) {
                    already = true;
                    break;
                }
            }
            if (!already) {
                unique.add(item.copy());
            }
        }
        return unique;
    }

    private static int rollExtraDrops(User user, Trait trait) {
        double value = user.getEffectiveTraitLevel(trait);
        if (value == 0.0) {
            return 0;
        }
        int guaranteed = (int) (value / 100);
        double chance = (value - guaranteed * 100) / 100.0;
        int extra = RANDOM.nextDouble() < chance ? 1 : 0;
        return guaranteed + extra;
    }
}
