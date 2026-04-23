package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.world.rule.GameRules;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import java.util.List;

/**
 * Bukkit {@code DoubleDropTrait} / {@code BlockBreakEvent} in-scope core: chance to duplicate
 * the block’s drop list for a fixed Bukkit-allowlist of blocks, after vanilla break, without
 * Bukkit’s {@code LootDropEvent} or WorldGuard. Gates: trait enabled, not creative/spectator,
 * {@link GameRules#DO_TILE_DROPS} gamerule, not {@code blocked_worlds}, not a tracked
 * {@link dev.aurelium.auraskills.fabric.region.FabricRegionManager#isPlacedBlock}. Must register
 * {@link #register} in {@code FabricLevelManager} <em>after</em> other block-brain work but
 * <em>before</em> {@link dev.aurelium.auraskills.fabric.region.FabricPlacedBlockBreakHandler} so
 * placed-block data is still available for the check.
 */
public final class FabricDoubleDrop {

    private FabricDoubleDrop() {
    }

    public static void register(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return;
            }
            if (plugin == null || !plugin.isRuntimeReady()) {
                return;
            }
            tryApply(plugin, sw, sp, pos, state);
        });
    }

    @VisibleForTesting
    static void tryApply(
            @Nullable FabricAuraSkills plugin,
            ServerWorld world,
            ServerPlayerEntity player,
            BlockPos pos,
            BlockState state) {
        if (plugin == null) {
            return;
        }
        if (!Traits.DOUBLE_DROP.isEnabled()) {
            return;
        }
        if (player.getAbilities().creativeMode) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        if (!Boolean.TRUE.equals(world.getGameRules().getValue(GameRules.DO_TILE_DROPS))) {
            return;
        }
        if (!isDoubleDropBlock(state)) {
            return;
        }
        if (plugin.getWorldManager().isBlockedWorld(FabricUser.worldNameCompat(world))) {
            return;
        }
        if (plugin.getFabricRegionManager().isPlacedBlock(world, pos)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double percent = user.getEffectiveTraitLevel(Traits.DOUBLE_DROP);
        double cap = Traits.DOUBLE_DROP.optionDouble("max_percent", 100.0);
        double chance = toChancePercent(percent, cap) / 100.0;
        if (chance <= 0.0 || world.random.nextDouble() >= chance) {
            return;
        }
        ItemStack tool = player.getMainHandStack();
        BlockEntity be = world.getBlockEntity(pos);
        List<ItemStack> raw = Block.getDroppedStacks(state, world, pos, be, player, tool);
        if (raw.isEmpty()) {
            return;
        }
        for (ItemStack fromLoot : raw) {
            if (fromLoot.isEmpty()) {
                continue;
            }
            ItemStack extra = toExtraStack(state, fromLoot, tool, world);
            if (plugin.getFabricLootManager().toInventory(player.getMainHandStack())) {
                player.getInventory().offerOrDrop(extra);
            } else {
                Vec3d at = pos.toCenterPos();
                world.spawnEntity(new ItemEntity(world, at.x, at.y, at.z, extra));
            }
        }
    }

    @VisibleForTesting
    static double toChancePercent(double effectiveLevelPercent, double maxPercent) {
        double p = effectiveLevelPercent;
        if (p > maxPercent) {
            p = maxPercent;
        }
        return p;
    }

    @VisibleForTesting
    static boolean isDoubleDropBlock(BlockState s) {
        return s.isOf(Blocks.STONE)
                || s.isOf(Blocks.COBBLESTONE)
                || s.isOf(Blocks.SAND)
                || s.isOf(Blocks.GRAVEL)
                || s.isOf(Blocks.DIRT)
                || s.isOf(Blocks.GRASS_BLOCK)
                || s.isOf(Blocks.ANDESITE)
                || s.isOf(Blocks.DIORITE)
                || s.isOf(Blocks.GRANITE);
    }

    /**
     * Bukkit: silk touch rewrites for {@link Blocks#STONE} and {@link Blocks#GRASS_BLOCK}; other blocks clone drops.
     */
    private static ItemStack toExtraStack(BlockState state, ItemStack fromLoot, ItemStack tool, ServerWorld world) {
        if (silkTouchLevel(world, tool) > 0) {
            if (state.isOf(Blocks.STONE)) {
                return new ItemStack(Blocks.STONE);
            }
            if (state.isOf(Blocks.GRASS_BLOCK)) {
                return new ItemStack(Blocks.GRASS_BLOCK);
            }
        }
        return fromLoot.copy();
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
}
