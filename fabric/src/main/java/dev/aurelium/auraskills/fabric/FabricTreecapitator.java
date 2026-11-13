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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return;
            }
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && isPlayerPlacedBlock(sw, pos)) {
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
            User user = plugin.getUserManager().getUser(sp.getUUID());
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
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel sw)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReady(plugin, sp, sw, hitResult.getBlockPos(), sw.getBlockState(hitResult.getBlockPos()));
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            tryReady(plugin, sp, null, null, null);
            return InteractionResult.PASS;
        });
    }

    private static void tryReady(FabricAuraSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
            @Nullable BlockPos pos, @Nullable BlockState stateAtPos) {
        if (isDisabled()) {
            return;
        }
        if (!holdingAxe(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
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

    private static void scheduleUnready(FabricAuraSkills plugin, ServerPlayer player, Locale locale, ManaAbilityData data) {
        int readyDurationTicks = 80;
        plugin.getScheduler().scheduleSync(() -> {
            if (!plugin.getUserManager().hasUser(player.getUUID())) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            ManaAbilityData d = user.getManaAbilityData(MANA);
            if (!d.isActivated() && d.isReady()) {
                d.setReady(false);
                plugin.getAbilityManager().sendAbilityMessage(user, plugin.getMsg(ManaAbilityMessage.TREECAPITATOR_LOWER, locale));
            }
        }, readyDurationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    private static boolean isAllowReady(FabricAuraSkills plugin, ServerPlayer player, @Nullable ServerLevel world,
            @Nullable BlockPos pos) {
        if (MANA.optionBoolean("require_sneak", false) && !player.isShiftKeyDown()) {
            return false;
        }
        if (world != null && pos != null && isBlockPlace(player, world, pos)) {
            return false;
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

    private static boolean tryActivate(FabricAuraSkills plugin, ServerPlayer player, User user) {
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

    private static void stop(FabricAuraSkills plugin, ServerPlayer player) {
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
        user.sendMessage(plugin.getMsg(ManaAbilityMessage.TREECAPITATOR_END, user.getLocale()));
    }

    private static void stopIfActive(FabricAuraSkills plugin, ServerPlayer player) {
        if (!plugin.getUserManager().hasUser(player.getUUID())) {
            return;
        }
        if (plugin.getUserManager().getUser(player.getUUID()).getManaAbilityData(MANA).isActivated()) {
            stop(plugin, player);
        }
    }

    private static double consumeMana(FabricAuraSkills plugin, ServerPlayer player, User user, double manaCost) {
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

    private static void breakTree(FabricAuraSkills plugin, ServerPlayer player, BlockPos seedPos,
            ServerLevel world, SkillSource<BlockXpSource> initialSource) {
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

    private static void breakBlock(FabricAuraSkills plugin, ServerPlayer player, BlockPos fromPos,
            TreecapitatorTree tree, AtomicInteger taskCount, ServerLevel world, Runnable onComplete) {
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
            if (plugin.configBoolean(Option.CHECK_BLOCK_REPLACE_ENABLED) && isPlayerPlacedBlock(world, adj)) {
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
    private static boolean breakOne(FabricAuraSkills plugin, ServerPlayer player, ServerLevel world, BlockPos pos,
            BlockState state) {
        if (GIVE_XP) {
            return player.gameMode.destroyBlock(pos);
        }
        return breakWithoutXp(world, player, pos, state);
    }

    private static boolean breakWithoutXp(ServerLevel world, ServerPlayer player, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        BlockEntity be = world.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        Block.dropResources(state, world, pos, be, player, tool);
        boolean removed = world.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (removed && !player.getAbilities().instabuild && !tool.isEmpty() && tool.isDamageableItem()) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        return removed;
    }

    private static List<BlockPos> surrounding(BlockPos center) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        List<BlockPos> out = new java.util.ArrayList<>(26);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 1; y >= -1; y--) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    out.add(m.set(center.getX() + x, center.getY() + y, center.getZ() + z).immutable());
                }
            }
        }
        return out;
    }

    private static int getHoldingAxeDurabilityLimit(ServerPlayer player, int limit) {
        ItemStack item = player.getMainHandItem();
        if (item.isEmpty() || !item.isDamageableItem()) {
            return limit;
        }
        int remaining = item.getMaxDamage() - item.getDamageValue();
        return Math.min(limit, remaining);
    }

    private static void applyDurabilityMultiplierDamage(ServerPlayer player, int blocksBroken, double multiplier) {
        if (multiplier <= 0 || blocksBroken <= 0) {
            return;
        }
        ItemStack item = player.getMainHandItem();
        if (item.isEmpty() || !item.isDamageableItem()) {
            return;
        }
        int taken = (int) (blocksBroken * multiplier);
        if (taken <= 0) {
            return;
        }
        item.hurtAndBreak(taken, player, EquipmentSlot.MAINHAND);
    }

    private static @Nullable SkillSource<BlockXpSource> getNonStrippedBreakSkillSource(FabricAuraSkills plugin, BlockState state) {
        SkillSource<BlockXpSource> found = findBreakSkillSource(plugin, state);
        if (found != null) {
            return found;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (!path.startsWith("stripped_")) {
            return null;
        }
        String basePath = path.substring("stripped_".length());
        return findBreakSkillSourceForBlockPath(plugin, basePath);
    }

    private static @Nullable SkillSource<BlockXpSource> findBreakSkillSource(FabricAuraSkills plugin, BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
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

    private static boolean failsChecks(FabricAuraSkills plugin, ServerPlayer player, User user) {
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
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
            return true;
        }
        return false;
    }

    private static boolean isActivated(User user) {
        return user.getManaAbilityData(MANA).isActivated();
    }

    private static boolean holdingAxe(ServerPlayer player) {
        return player.getMainHandItem().is(ItemTags.AXES);
    }

    private static boolean isPlayerPlacedBlock(ServerLevel world, BlockPos pos) {
        return false;
    }

    private static boolean isDisabled() {
        return !MANA.isEnabled() || !MANA.getSkill().isEnabled();
    }

    private static final class TreecapitatorTree {
        private final BlockPos originalPos;
        private int blocksBroken;
        private int maxBlocks;

        TreecapitatorTree(BlockPos originalPos, BlockXpSource source) {
            this.originalPos = originalPos.immutable();
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
