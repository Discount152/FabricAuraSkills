package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.mana.ManaAbilities;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.mana.ManaAbilityData;
import dev.granite.graniteskills.common.message.type.ManaAbilityMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SmithingTableBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.granite.graniteskills.bukkit.skills.farming.Replenish}: readied right-click with hoe,
 * then break mature crops to replant; optional cancel on unripe break when configured.
 */
public final class FabricReplenish {

    private static final ManaAbility MANA = ManaAbilities.REPLENISH;

    private static final Set<UUID> PENDING_MATURE_REPLANT = ConcurrentHashMap.newKeySet();

    private FabricReplenish() {
    }

    public static void register(FabricGraniteSkills plugin) {
        registerReadyListeners(plugin);
        registerBreakHandlers(plugin);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PENDING_MATURE_REPLANT.remove(handler.player.getUUID());
            stopIfActive(plugin, handler.player);
        });
    }

    private static void registerBreakHandlers(FabricGraniteSkills plugin) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return true;
            }
            if (!isReplenishCrop(state)) {
                return true;
            }
            User user = plugin.getUserManager().getUser(sp.getUUID());
            if (failsReplenishContext(plugin, sp, user)) {
                return true;
            }
            if (!MANA.isEnabled() || !MANA.getSkill().isEnabled()) {
                return true;
            }
            if (user.getManaAbilityLevel(MANA) <= 0) {
                return true;
            }

            ManaAbilityData data = user.getManaAbilityData(MANA);
            boolean activated = data.isActivated();
            if (!activated) {
                if (!holdingHoe(sp) || !checkActivation(plugin, sp, user)) {
                    return true;
                }
            }

            if (isFullyGrown(state)) {
                PENDING_MATURE_REPLANT.add(sp.getUUID());
                return true;
            }
            if (MANA.optionBoolean("prevent_unripe_break", true)) {
                return false;
            }
            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel)) {
                return;
            }
            ServerLevel sw = (ServerLevel) world;
            if (!PENDING_MATURE_REPLANT.remove(sp.getUUID())) {
                return;
            }
            if (!isReplenishCrop(state) || !isFullyGrown(state)) {
                return;
            }
            Block cropBlock = state.getBlock();
            int delayTicks = MANA.optionInt("replant_delay", 4);
            long delayMs = delayTicks * 50L;
            plugin.getScheduler().scheduleSync(() -> replantAt(sw, pos.immutable(), cropBlock), delayMs, TimeUnit.MILLISECONDS);
        });
    }

    private static void replantAt(ServerLevel world, BlockPos pos, Block cropBlock) {
        if (!world.getBlockState(pos).isAir()) {
            return;
        }
        BlockState below = world.getBlockState(pos.below());
        BlockState toPlace;
        if (cropBlock == net.minecraft.world.level.block.Blocks.NETHER_WART) {
            if (!below.is(net.minecraft.world.level.block.Blocks.SOUL_SAND)) {
                return;
            }
            toPlace = net.minecraft.world.level.block.Blocks.NETHER_WART.defaultBlockState();
        } else {
            if (!below.is(net.minecraft.world.level.block.Blocks.FARMLAND)) {
                return;
            }
            if (cropBlock instanceof CropBlock crop) {
                toPlace = crop.defaultBlockState();
            } else {
                toPlace = cropBlock.defaultBlockState();
            }
        }
        world.setBlock(pos, toPlace, Block.UPDATE_ALL);
        if (MANA.optionBoolean("show_particles", true)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.2;
            double z = pos.getZ() + 0.5;
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 8, 0.25, 0.0, 0.25, 0.02);
        }
    }

    private static void registerReadyListeners(FabricGraniteSkills plugin) {
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReadyOnInteract(plugin, sp, sw, hitResult.getBlockPos(), sw.getBlockState(hitResult.getBlockPos()));
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReadyOnInteract(plugin, sp, null, null, null);
            return InteractionResult.PASS;
        });
    }

    private static void tryReadyOnInteract(FabricGraniteSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled(plugin)) {
            return;
        }
        if (!holdingHoe(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (failsReplenishContext(plugin, player, user)) {
            return;
        }
        if (!MANA.isEnabled() || !MANA.getSkill().isEnabled()) {
            return;
        }
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (world != null && pos != null && stateAtPos != null) {
            if (blocksReadiedActivate(stateAtPos, world, pos) || isExcludedDirtFarmland(stateAtPos)) {
                return;
            }
        }
        if (!isAllowReady(plugin, player, world, pos)) {
            return;
        }

        ManaAbilityData data = user.getManaAbilityData(MANA);
        Locale locale = user.getLocale();
        if (data.isActivated() || data.isReady()) {
            return;
        }
        if (data.getCooldown() == 0) {
            data.setReady(true);
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.REPLENISH_RAISE, locale));
            scheduleUnready(plugin, player, locale, data);
        } else {
            if (data.getErrorTimer() == 0) {
                String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.NOT_READY, locale), "{cooldown}",
                        NumberUtil.format0((double) data.getCooldown() / 20.0));
                user.sendMessage(msg);
                data.setErrorTimer(2);
            }
        }
    }

    private static void scheduleUnready(FabricGraniteSkills plugin, ServerPlayer player, Locale locale, ManaAbilityData data) {
        int readyDurationTicks = 80;
        plugin.getScheduler().scheduleSync(() -> {
            if (!plugin.getUserManager().hasUser(player.getUUID())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            ManaAbilityData d = user.getManaAbilityData(MANA);
            if (!d.isActivated() && d.isReady()) {
                d.setReady(false);
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.REPLENISH_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricGraniteSkills plugin, ServerPlayer player, @Nullable ServerLevel world, @Nullable BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isShiftKeyDown()) {
            return false;
        }
        if (world != null && pos != null) {
            if (isBlockPlace(player, world, pos)) {
                return false;
            }
        }
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return user.hasSkillPermission(MANA.getSkill());
    }

    private static boolean isBlockPlace(ServerPlayer player, ServerLevel world, BlockPos pos) {
        if (!MANA.optionBoolean("check_offhand", true)) {
            return false;
        }
        ItemStack off = player.getOffhandItem();
        if (off.isEmpty()) {
            return false;
        }
        if (player.isShiftKeyDown() && MANA.optionBoolean("sneak_offhand_bypass", true)) {
            return false;
        }
        return off.getItem() instanceof BlockItem;
    }

    private static boolean checkActivation(FabricGraniteSkills plugin, ServerPlayer player, User user) {
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isReady() || data.isActivated()) {
            return false;
        }
        int durationTicks = (int) Math.round(MANA.getValue(user.getManaAbilityLevel(MANA)) * 20.0);
        double manaCost = MANA.getManaCost(user.getManaAbilityLevel(MANA));
        if (insufficientMana(plugin, user, manaCost)) {
            return false;
        }

        data.setActivated(true);
        if (player.level() instanceof ServerLevel soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        double manaUsed = consumeMana(plugin, player, user, manaCost);
        FabricManaAbilityUseXpHandler.onManaAbilityActivated(player, MANA, manaUsed);

        if (durationTicks != 0) {
            plugin.getScheduler().scheduleSync(() -> stop(plugin, player), durationTicks * 50L, TimeUnit.MILLISECONDS);
        } else {
            stop(plugin, player);
        }
        return true;
    }

    private static void stop(FabricGraniteSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isActivated()) {
            return;
        }
        data.setActivated(false);
        data.setReady(false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.REPLENISH_END, user.getLocale()));
    }

    private static void stopIfActive(FabricGraniteSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        ManaAbilityData data = plugin.getUserManager().getUser(player.getUUID()).getManaAbilityData(MANA);
        if (data.isActivated()) {
            stop(plugin, player);
        }
    }

    private static double consumeMana(FabricGraniteSkills plugin, ServerPlayer player, User user, double manaCost) {
        double manaConsumed;
        if (plugin.configBoolean(Option.MANA_ENABLED)) {
            manaConsumed = manaCost;
            user.setMana(user.getMana() - manaConsumed);
        } else {
            manaConsumed = 0.0;
        }
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.REPLENISH_START, user.getLocale()),
                "{mana}", NumberUtil.format0(manaConsumed));
        user.sendMessage(msg);
        return manaConsumed;
    }

    private static boolean insufficientMana(FabricGraniteSkills plugin, User user, double manaCost) {
        if (!plugin.configBoolean(Option.MANA_ENABLED) || user.getMana() >= manaCost) {
            return false;
        }
        plugin.getManaAbilityManager().sendNotEnoughManaMessage(user, manaCost);
        return true;
    }

    private static boolean failsReplenishContext(FabricGraniteSkills plugin, ServerPlayer player, User user) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        String w = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(w) || plugin.getWorldManager().isDisabledWorld(w);
    }

    private static boolean isDisabled(FabricGraniteSkills plugin) {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingHoe(ServerPlayer player) {
        return player.getMainHandItem().is(ItemTags.HOES);
    }

    private static boolean isReplenishCrop(BlockState state) {
        Block b = state.getBlock();
        return b == net.minecraft.world.level.block.Blocks.WHEAT
                || b == net.minecraft.world.level.block.Blocks.CARROTS
                || b == net.minecraft.world.level.block.Blocks.POTATOES
                || b == net.minecraft.world.level.block.Blocks.BEETROOTS
                || b == net.minecraft.world.level.block.Blocks.NETHER_WART;
    }

    private static boolean isFullyGrown(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (b instanceof NetherWartBlock) {
            int age = state.getValue(NetherWartBlock.AGE);
            return age >= NetherWartBlock.MAX_AGE;
        }
        return false;
    }

    private static boolean isExcludedDirtFarmland(BlockState state) {
        return state.is(net.minecraft.world.level.block.Blocks.DIRT)
                || state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)
                || state.is(net.minecraft.world.level.block.Blocks.DIRT_PATH)
                || state.is(net.minecraft.world.level.block.Blocks.FARMLAND);
    }

    static boolean blocksReadiedActivate(BlockState state, ServerLevel world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof BedBlock
                || block instanceof SignBlock
                || block instanceof FenceGateBlock
                || block instanceof TrapDoorBlock
                || block instanceof DoorBlock
                || block instanceof NoteBlock
                || block instanceof CraftingTableBlock
                || block instanceof EnchantingTableBlock
                || block instanceof BeaconBlock
                || block instanceof AnvilBlock
                || block instanceof GrindstoneBlock
                || block instanceof CartographyTableBlock
                || block instanceof LoomBlock
                || block instanceof StonecutterBlock
                || block instanceof SmithingTableBlock
                || block instanceof ChestBlock
                || block instanceof EnderChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock) {
            return true;
        }
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof RandomizableContainerBlockEntity;
    }
}
