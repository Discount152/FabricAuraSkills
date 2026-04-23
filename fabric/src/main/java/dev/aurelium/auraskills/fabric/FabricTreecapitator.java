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
import dev.aurelium.auraskills.common.source.type.BlockSource;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fabric port of Bukkit {@link dev.aurelium.auraskills.bukkit.skills.foraging.Treecapitator}.
 */
public final class FabricTreecapitator {

    private static final ManaAbility MANA = ManaAbilities.TREECAPITATOR;
    private static final boolean GIVE_XP = ManaAbilities.TREECAPITATOR.optionBoolean("give_xp", true);

    private FabricTreecapitator() {
    }

    public static void register(FabricAuraSkills plugin) {
        registerReadyListeners(plugin);
        registerBreakHandler(plugin);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopIfActive(plugin, handler.player));
    }

    private static void registerBreakHandler(FabricAuraSkills plugin) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
                return;
            }
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && isPlayerPlacedBlock(plugin, sw, pos)) {
                return;
            }
            SkillSource<BlockXpSource> skillSource = getNonStrippedBreakSkillSource(plugin, state);
            if (skillSource == null) {
                return;
            }
            BlockXpSource xpSource = skillSource.source();
            if (!plugin.getSkillManager().hasTag(xpSource, SourceTag.TRUNKS)) {
                return;
            }
            User user = plugin.getUserManager().getUser(sp.getUuid());
            if (failsChecks(plugin, sp, user)) {
                return;
            }
            if (isActivated(user)) {
                breakTree(plugin, sp, pos, sw, skillSource);
                return;
            }
            if (holdingAxe(sp) && tryActivate(plugin, sp, user)) {
                breakTree(plugin, sp, pos, sw, skillSource);
            }
        });
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
        if (!holdingAxe(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        if (user.getManaAbilityLevel(MANA) <= 0) {
            return;
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
            plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.TREECAPITATOR_RAISE, locale));
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
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.TREECAPITATOR_LOWER, locale));
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
        return off.getItem() instanceof BlockItem;
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
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.TREECAPITATOR_END, user.getLocale()));
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
        String msg = TextUtil.replace(plugin.getMsg(ManaAbilityMessage.TREECAPITATOR_START, user.getLocale()),
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

    private static void breakTree(FabricAuraSkills plugin, ServerPlayerEntity player, BlockPos seedPos,
            ServerWorld world, SkillSource<BlockXpSource> initialSource) {
        AtomicInteger taskCount = new AtomicInteger(1);
        TreecapitatorTree tree = new TreecapitatorTree(seedPos, initialSource.source());
        double multiplier = MANA.optionDouble("durability_multiplier", 0);
        if (MANA.optionBoolean("max_limit_durability", false)) {
            tree.setMaxBlocks(getHoldingAxeDurabilityLimit(player, tree.getMaxBlocks()));
        }

        breakBlock(plugin, player, seedPos, tree, taskCount, world, () -> {
            taskCount.decrementAndGet();
            if (taskCount.get() == 0) {
                applyDurabilityMultiplierDamage(player, tree.getBlocksBroken(), multiplier);
            }
        });
    }

    private static void breakBlock(FabricAuraSkills plugin, ServerPlayerEntity player, BlockPos fromPos,
            TreecapitatorTree tree, AtomicInteger taskCount, ServerWorld world, Runnable onComplete) {
        if (tree.getBlocksBroken() > tree.getMaxBlocks()) {
            onComplete.run();
            return;
        }
        BlockPos origin = tree.getOriginalPos();
        for (BlockPos adj : surrounding(fromPos)) {
            BlockState adjState = world.getBlockState(adj);
            if (adjState.isAir()) {
                continue;
            }
            SkillSource<BlockXpSource> adjSkill = findBreakSkillSource(plugin, adjState);
            if (adjSkill == null) {
                continue;
            }
            BlockXpSource adjSource = adjSkill.source();
            if (!plugin.getSkillManager().hasTag(adjSource, SourceTag.TREECAPITATOR_APPLICABLE)) {
                continue;
            }
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && isPlayerPlacedBlock(plugin, world, adj)) {
                continue;
            }

            boolean broken = breakOne(plugin, player, world, adj, adjState);
            if (!broken) {
                continue;
            }

            tree.incrementBlocksBroken();
            // XP: when GIVE_XP, tryBreakBlock runs Fabric PlayerBlockBreakEvents → FabricBlockBreakHandler.

            if (adj.getX() > origin.getX() + 6 || adj.getZ() > origin.getZ() + 6 || adj.getY() > origin.getY() + 31) {
                onComplete.run();
                return;
            }
            taskCount.incrementAndGet();
            plugin.getScheduler().scheduleSync(() -> breakBlock(plugin, player, adj, tree, taskCount, world, onComplete),
                    50L, TimeUnit.MILLISECONDS);
        }
        onComplete.run();
    }

    /**
     * When {@code give_xp} is true, use normal player break so drops respect tool enchants; XP is applied here to match
     * Bukkit's explicit {@code addXp} in the tree loop (Fabric block-break handler does not run for all break paths).
     * When false, break without XP.
     */
    private static boolean breakOne(FabricAuraSkills plugin, ServerPlayerEntity player, ServerWorld world, BlockPos pos,
            BlockState state) {
        if (GIVE_XP) {
            return player.interactionManager.tryBreakBlock(pos);
        }
        return breakWithoutXp(world, player, pos, state);
    }

    private static boolean breakWithoutXp(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        BlockEntity be = world.getBlockEntity(pos);
        ItemStack tool = player.getMainHandStack();
        Block.dropStacks(state, world, pos, be, player, tool);
        boolean removed = world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        if (removed && !player.getAbilities().creativeMode && !tool.isEmpty() && tool.isDamageable()) {
            tool.damage(1, player, EquipmentSlot.MAINHAND);
        }
        return removed;
    }

    private static List<BlockPos> surrounding(BlockPos center) {
        BlockPos.Mutable m = new BlockPos.Mutable();
        List<BlockPos> out = new java.util.ArrayList<>(26);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 1; y >= -1; y--) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    out.add(m.set(center.getX() + x, center.getY() + y, center.getZ() + z).toImmutable());
                }
            }
        }
        return out;
    }

    private static int getHoldingAxeDurabilityLimit(ServerPlayerEntity player, int limit) {
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

    private static @Nullable SkillSource<BlockXpSource> getNonStrippedBreakSkillSource(FabricAuraSkills plugin, BlockState state) {
        SkillSource<BlockXpSource> found = findBreakSkillSource(plugin, state);
        if (found != null) {
            return found;
        }
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        if (!path.startsWith("stripped_")) {
            return null;
        }
        String basePath = path.substring("stripped_".length());
        return findBreakSkillSourceForBlockPath(plugin, basePath);
    }

    private static @Nullable SkillSource<BlockXpSource> findBreakSkillSource(FabricAuraSkills plugin, BlockState state) {
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        return findBreakSkillSourceForPath(plugin, path, state);
    }

    private static @Nullable SkillSource<BlockXpSource> findBreakSkillSourceForBlockPath(FabricAuraSkills plugin, String blockPath) {
        return findBreakSkillSourceForPath(plugin, blockPath, null);
    }

    private static @Nullable SkillSource<BlockXpSource> findBreakSkillSourceForPath(FabricAuraSkills plugin, String blockPath,
            @Nullable BlockState stateForStates) {
        for (SkillSource<BlockXpSource> entry : plugin.getSkillManager().getSourcesOfType(BlockXpSource.class)) {
            BlockXpSource source = entry.source();
            if (!List.of(source.getTriggers()).contains(BlockXpSource.BlockTriggers.BREAK)) {
                continue;
            }
            boolean blockOk = false;
            for (String blockName : source.getBlocks()) {
                if (blockName != null && blockName.equalsIgnoreCase(blockPath)) {
                    blockOk = true;
                    break;
                }
            }
            if (!blockOk) {
                continue;
            }
            BlockXpSource.BlockXpSourceState[] states = source.getStates();
            if (states != null && states.length > 0) {
                if (stateForStates == null || !FabricBlockStateMatcher.matchesStates(stateForStates, states)) {
                    continue;
                }
            }
            return entry;
        }
        return null;
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

    private static boolean isActivated(User user) {
        return user.getManaAbilityData(MANA).isActivated();
    }

    private static boolean holdingAxe(ServerPlayerEntity player) {
        return player.getMainHandStack().isIn(ItemTags.AXES);
    }

    private static boolean isPlayerPlacedBlock(FabricAuraSkills plugin, ServerWorld world, BlockPos pos) {
        if (!plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED)) {
            return false;
        }
        return plugin.getFabricRegionManager().isPlacedBlock(world, pos);
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static final class TreecapitatorTree {
        private final BlockPos originalPos;
        private int blocksBroken;
        private int maxBlocks;

        TreecapitatorTree(BlockPos originalPos, BlockXpSource source) {
            this.originalPos = originalPos.toImmutable();
            setMaxBlocksFromSource(source);
        }

        BlockPos getOriginalPos() {
            return originalPos;
        }

        int getBlocksBroken() {
            return blocksBroken;
        }

        void incrementBlocksBroken() {
            blocksBroken++;
        }

        int getMaxBlocks() {
            return maxBlocks;
        }

        void setMaxBlocks(int maxBlocks) {
            this.maxBlocks = maxBlocks;
        }

        private void setMaxBlocksFromSource(BlockXpSource source) {
            int max = (source != null && source.getMaxBlocks() >= 1) ? source.getMaxBlocks() : BlockSource.DEFAULT_MAX_BLOCKS;
            double mult = ManaAbilities.TREECAPITATOR.optionDouble("max_blocks_multiplier", 1.0);
            this.maxBlocks = (int) (max * mult);
        }
    }
}
