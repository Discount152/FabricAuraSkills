package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.BrewingXpSource;
import dev.aurelium.auraskills.api.source.type.BrewingXpSource.BrewTriggers;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

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
     * Start of {@link net.minecraft.block.entity.BrewingStandBlockEntity#craft}: branch on first YAML row matching the ingredient (Paper {@code BrewingLeveler#onBrew}).
     */
    public static void onCraftHead(ServerWorld world, BlockPos pos, DefaultedList<ItemStack> slots) {
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
            PENDING_TAKEOUT.set(new BrewCraftPending(pos.toImmutable(), ingredient.copy(), before));
        }
    }

    /**
     * End of {@code craft}: record bottle-slot changes for {@code TAKEOUT} sources.
     */
    public static void onCraftReturn(ServerWorld world, BlockPos pos, DefaultedList<ItemStack> slots) {
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
            ServerPlayerEntity player,
            BrewingStandScreenHandler handler,
            int slotIndex,
            SlotActionType actionType,
            ItemStack stackBeforeClick) {
        if (slotIndex < 0 || slotIndex > 2) {
            return;
        }
        if (!isTakeoutClickAction(actionType)) {
            return;
        }
        if (!(player.getEntityWorld() instanceof ServerWorld world) || world.isClient()) {
            return;
        }
        if (!(handler.getSlot(slotIndex).inventory instanceof net.minecraft.block.entity.BrewingStandBlockEntity be)) {
            return;
        }
        BlockPos pos = be.getPos();
        ItemStack after = handler.getSlot(slotIndex).getStack();
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
        brewingSlot.clearTakeoutTracking();

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

        User user = plugin.getUserManager().getUser(player.getUuid());
        for (Skill skill : totalXpMap.keySet()) {
            plugin.getLevelManager().addXp(user, skill, sources.getFirst().source(), totalXpMap.getOrDefault(skill, 0.0));
        }
    }

    private static boolean isTakeoutClickAction(SlotActionType actionType) {
        return actionType == SlotActionType.PICKUP
                || actionType == SlotActionType.QUICK_MOVE
                || actionType == SlotActionType.SWAP
                || actionType == SlotActionType.PICKUP_ALL;
    }

    private static boolean itemRemovedOrReduced(ItemStack before, ItemStack after) {
        if (after.isEmpty()) {
            return true;
        }
        return after.getCount() < before.getCount() || !ItemStack.areEqual(before, after);
    }

    private static void awardBrewXp(
            FabricAuraSkills plugin, ServerWorld world, BlockPos pos, SkillSource<BrewingXpSource> entry) {
        ServerPlayerEntity brewer = resolveBrewXpPlayer(world, pos);
        if (brewer == null) {
            return;
        }
        BrewingXpSource source = entry.source();
        Skill skill = entry.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, brewer, skill, SourceTypes.BREWING)) {
            return;
        }
        User user = plugin.getUserManager().getUser(brewer.getUuid());
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
    private static @Nullable ServerPlayerEntity resolveBrewXpPlayer(ServerWorld world, BlockPos pos) {
        UUID ownerId = FabricBrewingOwnerTracker.getOwner(world, pos);
        if (ownerId == null) {
            return null;
        }
        return world.getServer().getPlayerManager().getPlayer(ownerId);
    }

}
