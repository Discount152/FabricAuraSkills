package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BlockXpSource;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.message.type.ManaAbilityMessage;
import dev.aurelium.auraskills.common.source.SourceTag;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.excavation.Terraform}.
 * Readied with shovel right-click; breaking a {@link SourceTag#TERRAFORM_APPLICABLE} block chains to
 * horizontally adjacent blocks of the same type (Paper {@code max_blocks}, durability options).
 */
public final class FabricTerraform {

    private static final ManaAbility MANA = ManaAbilities.TERRAFORM;

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private static final ThreadLocal<Boolean> IN_TERRAFORM_CHAIN = ThreadLocal.withInitial(() -> false);

    private FabricTerraform() {
    }

    public static void register(FabricAuraSkills plugin) {
        registerReadyListeners(plugin);
        registerBreakChain(plugin);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static void registerBreakChain(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return true;
            }
            if (IN_TERRAFORM_CHAIN.get()) {
                return true;
            }
            if (isDisabled()) {
                return true;
            }
            User user = plugin.getUserManager().getUser(sp.getUuid());
            if (failsChecks(plugin, sp, user)) {
                return true;
            }
            SkillSource<BlockXpSource> skillSource = findTerraformBreakSource(plugin, state);
            if (skillSource == null) {
                return true;
            }
            if (!plugin.getSkillManager().hasTag(skillSource.source(), SourceTag.TERRAFORM_APPLICABLE)) {
                return true;
            }

            ManaAbilityData data = user.getManaAbilityData(MANA);
            if (data.isActivated()) {
                runTerraformBreak(sp, sw, pos, state);
                return false;
            }
            if (!holdingShovel(sp)) {
                return true;
            }
            if (!tryActivate(plugin, sp, user)) {
                return true;
            }
            runTerraformBreak(sp, sw, pos, state);
            return false;
        });
    }

    private static void runTerraformBreak(ServerPlayerEntity player, ServerWorld world,
            BlockPos start, BlockState startState) {
        var targetBlock = startState.getBlock();
        var queue = new ArrayDeque<BlockPos>();
        queue.add(start.toImmutable());

        int maxCount = MANA.optionInt("max_blocks", 61);
        if (MANA.optionBoolean("max_limit_durability", false)) {
            maxCount = getHoldingToolDurabilityLimit(player, maxCount);
        }

        int count = 0;
        IN_TERRAFORM_CHAIN.set(true);
        try {
            while (!queue.isEmpty() && count < maxCount) {
                BlockPos p = queue.poll();
                BlockState st = world.getBlockState(p);
                if (!st.isOf(targetBlock)) {
                    continue;
                }
                if (!player.interactionManager.tryBreakBlock(p)) {
                    continue;
                }
                count++;
                for (Direction dir : HORIZONTAL) {
                    queue.add(p.offset(dir));
                }
            }
        } finally {
            IN_TERRAFORM_CHAIN.set(false);
        }

        applyDurabilityMultiplierDamage(player, count, MANA.optionDouble("durability_multiplier", 0));
    }

    private static int getHoldingToolDurabilityLimit(ServerPlayerEntity player, int limit) {
        ItemStack item = player.getMainHandStack();
        if (item.isEmpty() || !item.isDamageable()) {
            return limit;
        }
        int remaining = item.getMaxDamage() - item.getDamage();
        return Math.min(limit, remaining);
    }

    private static void applyDurabilityMultiplierDamage(ServerPlayerEntity player, int blocksBroken, double multiplier) {
        if (multiplier <= 0 || blocksBroken <= 0) {
            return;
        }
        ItemStack item = player.getMainHandStack();
        if (item.isEmpty() || !item.isDamageable()) {
            return;
        }
        int taken = (int) (blocksBroken * multiplier);
        if (taken <= 0) {
            return;
        }
        item.damage(taken, player, EquipmentSlot.MAINHAND);
    }

    private static @Nullable SkillSource<BlockXpSource> findTerraformBreakSource(FabricAuraSkills plugin, BlockState state) {
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(BlockXpSource.BlockTriggers.BREAK)) {
                continue;
            }
            boolean blockOk = false;
            for (String blockName : source.getBlocks()) {
                if (blockName != null && blockName.equalsIgnoreCase(path)) {
                    blockOk = true;
                    break;
                }
            }
            if (!blockOk) {
                continue;
            }
            BlockXpSource.BlockXpSourceState[] states = source.getStates();
            if (states != null && states.length > 0) {
                if (!FabricBlockStateMatcher.matchesStates(state, states)) {
                    continue;
                }
            }
            return entry;
        }
        return null;
    }

    private static void registerReadyListeners(FabricAuraSkills plugin) {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReady(plugin, sp, sw, hitResult.getBlockPos(), sw.getBlockState(hitResult.getBlockPos()));
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp)) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            tryReady(plugin, sp, null, null, null);
            return ActionResult.PASS;
        });
    }

    private static void tryReady(FabricAuraSkills plugin, ServerPlayerEntity player, @Nullable ServerWorld world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!holdingShovel(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
        }
        if (world != null && pos != null && stateAtPos != null) {
            if (FabricReplenish.blocksReadiedActivate(stateAtPos, world, pos)) {
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
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.TERRAFORM_RAISE, locale));
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
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.TERRAFORM_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayerEntity player, @Nullable ServerWorld world,
            @Nullable BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isSneaking()) {
            return false;
        }
        if (world != null && pos != null && isBlockPlace(player, world, pos)) {
            return false;
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
        return off.getItem() instanceof net.minecraft.item.BlockItem;
    }

    private static boolean tryActivate(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
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
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.TERRAFORM_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayerEntity player) {
        if (!plugin.getUserManager().hasUser(player.getUuid())) {
            return;
        }
        if (plugin.getUserManager().getUser(player.getUuid()).getManaAbilityData(MANA).isActivated()) {
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
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.TERRAFORM_START, user.getLocale()),
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

    private static boolean failsChecks(FabricAuraSkills plugin, ServerPlayerEntity player, User user) {
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return true;
        }
        String w = FabricUser.worldNameCompat(player);
        if (plugin.getWorldManager().isDisabledWorld(w)) {
            return true;
        }
        if (!user.hasSkillPermission(MANA.getSkill())) {
            return true;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return true;
        }
        return false;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static boolean holdingShovel(ServerPlayerEntity player) {
        return player.getMainHandStack().isIn(ItemTags.SHOVELS);
    }
}
