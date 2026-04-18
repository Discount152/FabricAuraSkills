package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.farming.Replenish}: readied right-click with hoe,
 * then break mature crops to replant; optional cancel on unripe break when configured.
 */
public final class FabricReplenish {

    private static final ManaAbility MANA = ManaAbilities.REPLENISH;

    private static final Set<UUID> PENDING_MATURE_REPLANT = ConcurrentHashMap.newKeySet();

    private FabricReplenish() {
    }

    public static void register(FabricAuraSkills plugin) {
        registerReadyListeners(plugin);
        registerBreakHandlers(plugin);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PENDING_MATURE_REPLANT.remove(handler.player.getUuid());
            stopIfActive(plugin, handler.player);
        });
    }

    private static void registerBreakHandlers(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return true;
            }
            if (!isReplenishCrop(state)) {
                return true;
            }
            User user = plugin.getUserManager().getUser(sp.getUuid());
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
                PENDING_MATURE_REPLANT.add(sp.getUuid());
                return true;
            }
            if (MANA.optionBoolean("prevent_unripe_break", true)) {
                return false;
            }
            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld)) {
                return;
            }
            ServerWorld sw = (ServerWorld) world;
            if (!PENDING_MATURE_REPLANT.remove(sp.getUuid())) {
                return;
            }
            if (!isReplenishCrop(state) || !isFullyGrown(state)) {
                return;
            }
            Block cropBlock = state.getBlock();
            int delayTicks = MANA.optionInt("replant_delay", 4);
            long delayMs = delayTicks * 50L;
            plugin.getScheduler().scheduleSync(() -> replantAt(sw, pos.toImmutable(), cropBlock), delayMs, TimeUnit.MILLISECONDS);
        });
    }

    private static void replantAt(ServerWorld world, BlockPos pos, Block cropBlock) {
        if (!world.getBlockState(pos).isAir()) {
            return;
        }
        BlockState below = world.getBlockState(pos.down());
        BlockState toPlace;
        if (cropBlock == net.minecraft.block.Blocks.NETHER_WART) {
            if (!below.isOf(net.minecraft.block.Blocks.SOUL_SAND)) {
                return;
            }
            toPlace = net.minecraft.block.Blocks.NETHER_WART.getDefaultState();
        } else {
            if (!below.isOf(net.minecraft.block.Blocks.FARMLAND)) {
                return;
            }
            if (cropBlock instanceof CropBlock crop) {
                toPlace = crop.getDefaultState();
            } else {
                toPlace = cropBlock.getDefaultState();
            }
        }
        world.setBlockState(pos, toPlace, Block.NOTIFY_ALL);
        if (MANA.optionBoolean("show_particles", true)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.2;
            double z = pos.getZ() + 0.5;
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 8, 0.25, 0.0, 0.25, 0.02);
        }
    }

    private static void registerReadyListeners(FabricAuraSkills plugin) {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReadyOnInteract(plugin, sp, sw, hitResult.getBlockPos(), sw.getBlockState(hitResult.getBlockPos()));
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReadyOnInteract(plugin, sp, null, null, null);
            return ActionResult.PASS;
        });
    }

    private static void tryReadyOnInteract(FabricAuraSkills plugin, ServerPlayerEntity player, @Nullable ServerWorld world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled(plugin)) {
            return;
        }
        if (!holdingHoe(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
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

    private static void scheduleUnready(FabricAuraSkills plugin, ServerPlayerEntity player, Locale locale, ManaAbilityData data) {
        int readyDurationTicks = 80;
        plugin.getScheduler().scheduleSync(() -> {
            if (!plugin.getUserManager().hasUser(player.getUuid())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUuid());
            ManaAbilityData d = user.getManaAbilityData(MANA);
            if (!d.isActivated() && d.isReady()) {
                d.setReady(false);
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.REPLENISH_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayerEntity player, @Nullable ServerWorld world, @Nullable BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isSneaking()) {
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
        User user = plugin.getUserManager().getUser(player.getUuid());
        return user.hasSkillPermission(MANA.getSkill());
    }

    private static boolean isBlockPlace(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!MANA.optionBoolean("check_offhand", true)) {
            return false;
        }
        ItemStack off = player.getOffHandStack();
        if (off.isEmpty()) {
            return false;
        }
        if (player.isSneaking() && MANA.optionBoolean("sneak_offhand_bypass", true)) {
            return false;
        }
        return off.getItem() instanceof BlockItem;
    }

    private static boolean checkActivation(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
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
        if (player.getEntityWorld() instanceof ServerWorld soundWorld) {
            soundWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
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

    private static void stop(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        ManaAbilityData data = user.getManaAbilityData(MANA);
        if (!data.isActivated()) {
            return;
        }
        data.setActivated(false);
        data.setReady(false);
        data.setCooldown((int) MANA.getCooldown(user.getManaAbilityLevel(MANA)) * 20);
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.REPLENISH_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        ManaAbilityData data = plugin.getUserManager().getUser(player.getUuid()).getManaAbilityData(MANA);
        if (data.isActivated()) {
            stop(plugin, player);
        }
    }

    private static double consumeMana(FabricAuraSkills plugin, ServerPlayerEntity player, User user, double manaCost) {
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

    private static boolean insufficientMana(FabricAuraSkills plugin, User user, double manaCost) {
        if (!plugin.configBoolean(Option.MANA_ENABLED) || user.getMana() >= manaCost) {
            return false;
        }
        plugin.getManaAbilityManager().sendNotEnoughManaMessage(user, manaCost);
        return true;
    }

    private static boolean failsReplenishContext(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        String w = FabricUser.worldNameCompat(player);
        return plugin.getWorldManager().isBlockedWorld(w) || plugin.getWorldManager().isDisabledWorld(w);
    }

    private static boolean isDisabled(FabricAuraSkills plugin) {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingHoe(ServerPlayerEntity player) {
        return player.getMainHandStack().isIn(ItemTags.HOES);
    }

    private static boolean isReplenishCrop(BlockState state) {
        Block b = state.getBlock();
        return b == net.minecraft.block.Blocks.WHEAT
                || b == net.minecraft.block.Blocks.CARROTS
                || b == net.minecraft.block.Blocks.POTATOES
                || b == net.minecraft.block.Blocks.BEETROOTS
                || b == net.minecraft.block.Blocks.NETHER_WART;
    }

    private static boolean isFullyGrown(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock crop) {
            return crop.isMature(state);
        }
        if (b instanceof NetherWartBlock) {
            int age = state.get(NetherWartBlock.AGE);
            return age >= NetherWartBlock.MAX_AGE;
        }
        return false;
    }

    private static boolean isExcludedDirtFarmland(BlockState state) {
        return state.isOf(net.minecraft.block.Blocks.DIRT)
                || state.isOf(net.minecraft.block.Blocks.GRASS_BLOCK)
                || state.isOf(net.minecraft.block.Blocks.COARSE_DIRT)
                || state.isOf(net.minecraft.block.Blocks.DIRT_PATH)
                || state.isOf(net.minecraft.block.Blocks.FARMLAND);
    }

    static boolean blocksReadiedActivate(BlockState state, ServerWorld world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof BedBlock
                || block instanceof AbstractSignBlock
                || block instanceof FenceGateBlock
                || block instanceof TrapdoorBlock
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
        return be instanceof LootableContainerBlockEntity;
    }
}
