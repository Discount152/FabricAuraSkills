package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BrewingXpSource;
import dev.aurelium.auraskills.api.source.type.BrewingXpSource.BrewTriggers;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FabricBrewingXpHandler {

    private static final int INGREDIENT_SLOT = 3;
    private static final ThreadLocal<@Nullable BrewCraftPending> PENDING_TAKEOUT = new ThreadLocal<>();

    private FabricBrewingXpHandler() {
    }

    private static final class BrewCraftPending {
        final BlockPos pos;
        final ItemStack ingredientUsed;
        final ItemStack[] bottlesBefore;

        BrewCraftPending(BlockPos pos, ItemStack ingredientUsed, ItemStack[] bottlesBefore) {
            this.pos = pos;
            this.ingredientUsed = ingredientUsed;
            this.bottlesBefore = bottlesBefore;
        }
    }

    /**
     * Start of {@link net.minecraft.world.level.block.entity.BrewingStandBlockEntity#doBrew}: branch on first YAML row matching the ingredient (Paper {@code BrewingLeveler#onBrew}).
     */
    public static void onCraftHead(ServerLevel world, BlockPos pos, NonNullList<ItemStack> slots) {
        PENDING_TAKEOUT.remove();
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        ItemStack ingredient = slots.get(INGREDIENT_SLOT).copy();
        if (ingredient.isEmpty()) {
            return;
        }
        SkillSource<BrewingXpSource> first = findFirstMatchingSource(plugin, ingredient);
        if (first == null) {
            return;
        }
        BrewTriggers trigger = first.source().getTrigger();
        if (trigger == BrewTriggers.BREW) {
            awardBrewXp(plugin, world, pos, first);
        } else if (trigger == BrewTriggers.TAKEOUT) {
            ItemStack[] before = new ItemStack[] {slots.get(0).copy(), slots.get(1).copy(), slots.get(2).copy()};
            PENDING_TAKEOUT.set(new BrewCraftPending(pos.immutable(), ingredient.copy(), before));
        }
    }

    /**
     * End of {@code craft}: record bottle-slot changes for {@code TAKEOUT} sources.
     */
    public static void onCraftReturn(ServerLevel world, BlockPos pos, NonNullList<ItemStack> slots) {
        BrewCraftPending pending = PENDING_TAKEOUT.get();
        PENDING_TAKEOUT.remove();
        if (pending == null || !pending.pos.equals(pos)) {
            return;
        }
        ItemStack[] after =
                new ItemStack[] {slots.get(0).copy(), slots.get(1).copy(), slots.get(2).copy()};
        FabricBrewingTakeoutTracker.applyCraft(world, pos, pending.ingredientUsed, pending.bottlesBefore, after);
    }

    /**
     * When a player takes a potion from slots 0–2 (Paper {@code BrewingLeveler#onTakePotionOut}).
     */
    public static void onPotionSlotClickFinish(
            ServerPlayer player,
            BrewingStandMenu handler,
            int slotIndex,
            ContainerInput actionType,
            ItemStack stackBeforeClick) {
        if (slotIndex < 0 || slotIndex > 2) {
            return;
        }
        if (!isTakeoutClickAction(actionType)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel world) || world.isClientSide()) {
            return;
        }
        if (!(handler.getSlot(slotIndex).container instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity be)) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        ItemStack after = handler.getSlot(slotIndex).getItem();
        if (stackBeforeClick.isEmpty() || !itemRemovedOrReduced(stackBeforeClick, after)) {
            return;
        }
        FabricBrewingTakeoutTracker.BrewingSlot brewingSlot = FabricBrewingTakeoutTracker.getSlot(world, pos, slotIndex);
        if (brewingSlot == null || !brewingSlot.isBrewed()) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        List<ItemStack> ingredients = brewingSlot.getIngredients();
        if (ingredients.isEmpty()) {
            return;
        }
        brewingSlot.setBrewed(false);
        brewingSlot.resetIngredients();

        List<SkillSource<BrewingXpSource>> sources = new ArrayList<>();
        for (ItemStack ingredient : ingredients) {
            SkillSource<BrewingXpSource> skillSource = findFirstMatchingSource(plugin, ingredient);
            if (skillSource == null) {
                return;
            }
            sources.add(skillSource);
        }

        Map<Skill, Double> totalXpMap = new HashMap<>();
        for (SkillSource<BrewingXpSource> skillSource : sources) {
            BrewingXpSource source = skillSource.source();
            Skill skill = skillSource.skill();
            if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.BREWING)) {
                return;
            }
            totalXpMap.merge(skill, source.getXp(), Double::sum);
        }

        User user = plugin.getUserManager().getUser(player.getUUID());
        for (Skill skill : totalXpMap.keySet()) {
            plugin.getLevelManager().addXp(user, skill, sources.getFirst().source(), totalXpMap.getOrDefault(skill, 0.0));
        }
    }

    private static boolean isTakeoutClickAction(ContainerInput actionType) {
        return actionType == ContainerInput.PICKUP
                || actionType == ContainerInput.QUICK_MOVE
                || actionType == ContainerInput.SWAP
                || actionType == ContainerInput.PICKUP_ALL;
    }

    private static boolean itemRemovedOrReduced(ItemStack before, ItemStack after) {
        if (after.isEmpty()) {
            return true;
        }
        return after.getCount() < before.getCount() || !ItemStack.matches(before, after);
    }

    private static void awardBrewXp(
            FabricAuraSkills plugin, ServerLevel world, BlockPos pos, SkillSource<BrewingXpSource> entry) {
        ServerPlayer brewer = resolveBrewXpPlayer(world, pos);
        if (brewer == null) {
            return;
        }
        BrewingXpSource source = entry.source();
        Skill skill = entry.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, brewer, skill, SourceTypes.BREWING)) {
            return;
        }
        User user = plugin.getUserManager().getUser(brewer.getUUID());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp());
    }

    private static @Nullable SkillSource<BrewingXpSource> findFirstMatchingSource(
            FabricAuraSkills plugin, ItemStack ingredient) {
        for (SkillSource<BrewingXpSource> entry : plugin.getSkillManager().getSourcesOfType(BrewingXpSource.class)) {
            Skill skill = entry.skill();
            if (FabricLootItemMatcher.passesApiItemFilter(plugin, ingredient, entry.source().getIngredients(), skill)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Bukkit {@code BrewingLeveler#onBrew}: requires {@code skillsBrewingStandOwner} — only the tracked owner receives
     * XP (hopper/automatic brew when owner is offline awards nothing).
     */
    private static @Nullable ServerPlayer resolveBrewXpPlayer(ServerLevel world, BlockPos pos) {
        UUID ownerId = FabricBrewingOwnerTracker.getOwner(world, pos);
        if (ownerId == null) {
            return null;
        }
        return world.getServer().getPlayerList().getPlayer(ownerId);
    }

}
