package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.ability.FabricMiningAbilities;
import dev.aurelium.auraskills.fabric.trait.FabricGatheringLuckTraits;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ports Bukkit {@link dev.aurelium.auraskills.bukkit.source.BlockLeveler#applyBlockLuck} for
 * {@link dev.aurelium.auraskills.api.source.type.BlockXpSource.BlockTriggers#INTERACT} /
 * {@link dev.aurelium.auraskills.api.source.type.BlockXpSource.BlockTriggers#COLLECT} /
 * {@link dev.aurelium.auraskills.api.source.type.BlockXpSource.BlockTriggers#BREAK}. No {@code LootDropEvent} on Fabric;
 * items go to inventory or spawn at block center like Bukkit {@code ItemUtils.giveBlockLoot}.
 */
public final class FabricInteractGatheringLuck {

    private static final Random RANDOM = new Random();

    private FabricInteractGatheringLuck() {
    }

    public static void tryApply(FabricAuraSkills plugin, ServerPlayerEntity player, User user, Skill skill,
            XpSource xpSource, ServerWorld world, BlockPos pos, BlockState state) {
        if (player.getAbilities().creativeMode) {
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
        ItemStack tool = player.getMainHandStack();
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
            if (plugin.getFabricLootManager().toInventory(player.getMainHandStack())) {
                player.getInventory().offerOrDrop(dropped);
            } else {
                Vec3d at = pos.toCenterPos();
                world.spawnEntity(new ItemEntity(world, at.x, at.y, at.z, dropped));
            }
        }
    }

    private static int silkTouchLevel(ServerWorld world, ItemStack stack) {
        ItemEnchantmentsComponent c = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (c == null || c.isEmpty()) {
            return 0;
        }
        return world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT)
                .getOptional(Enchantments.SILK_TOUCH)
                .map(c::getLevel)
                .orElse(0);
    }

    private static List<ItemStack> uniqueDroppedStacks(ServerWorld world, ServerPlayerEntity player, BlockPos pos,
            BlockState state, ItemStack tool) {
        BlockEntity be = world.getBlockEntity(pos);
        List<ItemStack> raw = Block.getDroppedStacks(state, world, pos, be, player, tool);
        List<ItemStack> unique = new ArrayList<>();
        for (ItemStack item : raw) {
            if (item.isEmpty()) {
                continue;
            }
            boolean already = false;
            for (ItemStack u : unique) {
                if (ItemStack.areItemsAndComponentsEqual(u, item)) {
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
