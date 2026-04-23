package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.damage.DamageModifier;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.damage.FabricDamageMeta;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class FabricFarmingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    public FabricFarmingAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.BOUNTIFUL_HARVEST, Abilities.FARMER, Abilities.GENETICIST, Abilities.SCYTHE_MASTER, Abilities.GROWTH_AURA);
    }

    @Override
    public void contributeAttackDamage(FabricAuraSkills plugin, FabricDamageMeta meta, ServerPlayerEntity attacker) {
        if (meta.getDamageType() != DamageType.HOE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUuid());
        meta.addAttackModifier(scytheMaster(attacker, user));
    }

    private DamageModifier scytheMaster(ServerPlayerEntity player, User user) {
        var ability = Abilities.SCYTHE_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    @Override
    public String replaceDescPlaceholders(String input, Ability ability, User user) {
        if (ability.equals(Abilities.GROWTH_AURA)) {
            return TextUtil.replace(input, "{radius}", String.valueOf(ability.optionInt("radius", 30)));
        }
        return input;
    }

    /**
     * Bukkit {@code PlayerItemConsumeEvent} — extra saturation for plant-based foods (Geneticist).
     * Call from the same point as {@link dev.aurelium.auraskills.fabric.FabricItemConsumeHandler}
     * (consumption commit, {@code LivingEntity#consumeItem}).
     */
    public static void applyGeneticist(ServerPlayerEntity player, ItemStack itemBeingConsumed) {
        FabricAuraSkills p = FabricAuraSkills.getInstance();
        if (p == null || !p.isRuntimeReady() || itemBeingConsumed.isEmpty()) {
            return;
        }
        var ability = Abilities.GENETICIST;
        if (!ability.isEnabled() || !ability.getSkill().isEnabled()) {
            return;
        }
        FabricFarmingAbilities impl;
        try {
            impl = p.getAbilityManager().getAbilityImpl(FabricFarmingAbilities.class);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (impl.isDisabled(ability) || impl.failsChecks(player, ability)) {
            return;
        }
        if (!isPlantBasedFood(itemBeingConsumed)) {
            return;
        }
        User user = p.getUserManager().getUser(player.getUuid());
        float amount = (float) impl.getValue(ability, user) / 10.0F;
        var hunger = player.getHungerManager();
        hunger.setSaturationLevel(hunger.getSaturationLevel() + amount);
    }

    static boolean isPlantBasedFood(ItemStack stack) {
        return stack.isOf(Items.BREAD)
                || stack.isOf(Items.APPLE)
                || stack.isOf(Items.GOLDEN_APPLE)
                || stack.isOf(Items.POTATO)
                || stack.isOf(Items.BAKED_POTATO)
                || stack.isOf(Items.CARROT)
                || stack.isOf(Items.GOLDEN_CARROT)
                || stack.isOf(Items.MELON_SLICE)
                || stack.isOf(Items.PUMPKIN_PIE)
                || stack.isOf(Items.BEETROOT)
                || stack.isOf(Items.BEETROOT_SOUP)
                || stack.isOf(Items.MUSHROOM_STEW)
                || stack.isOf(Items.POISONOUS_POTATO);
    }

    /**
     * Bukkit {@code BlockGrowEvent} for {@code Ageable} new state — at {@code TAIL} of {@code randomTick}
     * for blocks (see {@code FarmingAgeableRandomTickGrowthAuraMixin}).
     */
    public static void onNaturalAgingAfterRandomTick(
            ServerWorld world,
            BlockPos pos,
            BlockState start,
            BlockState end) {
        if (ageDidNotAdvance(start, end)) {
            return;
        }
        FabricAuraSkills p = FabricAuraSkills.getInstance();
        if (p == null || !p.isRuntimeReady()) {
            return;
        }
        var ability = Abilities.GROWTH_AURA;
        if (!ability.isEnabled() || !ability.getSkill().isEnabled()) {
            return;
        }
        int radius = ability.optionInt("radius", 30);
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        Box box = Box.of(new Vec3d(cx, cy, cz), radius, radius, radius);
        for (ServerPlayerEntity player : world.getNonSpectatingEntities(ServerPlayerEntity.class, box)) {
            handleGrowthAura(p, world, pos, end, player);
        }
    }

    @VisibleForTesting
    static boolean ageDidNotAdvance(BlockState start, BlockState end) {
        if (start.getBlock() != end.getBlock()) {
            return true;
        }
        IntProperty ap = intAgeProperty(start);
        if (ap == null || !start.contains(ap) || !end.contains(ap)) {
            return true;
        }
        return end.get(ap) <= start.get(ap);
    }

    @Nullable
    @VisibleForTesting
    static IntProperty intAgeProperty(BlockState state) {
        for (Property<?> p : state.getBlock().getStateManager().getProperties()) {
            if ("age".equals(p.getName()) && p instanceof IntProperty ip) {
                return ip;
            }
        }
        return null;
    }

    private static void handleGrowthAura(
            FabricAuraSkills plugin,
            ServerWorld world,
            BlockPos pos,
            BlockState newState,
            ServerPlayerEntity player) {
        var ability = Abilities.GROWTH_AURA;
        try {
            FabricFarmingAbilities impl = plugin.getAbilityManager().getAbilityImpl(FabricFarmingAbilities.class);
            if (impl.isDisabled(ability) || impl.failsChecks(player, ability)) {
                return;
            }
            User user = plugin.getUserManager().getUser(player.getUuid());
            int extraStages = rollExtraStages(impl, user);
            if (extraStages <= 0) {
                return;
            }
            plugin.getScheduler()
                    .scheduleSync(
                            () -> applyExtraGrowthAfterAura(world, pos, newState, extraStages),
                            50L,
                            TimeUnit.MILLISECONDS);
        } catch (IllegalArgumentException e) {
            // Ability impl not registered
        }
    }

    @VisibleForTesting
    static int rollExtraStages(FabricFarmingAbilities impl, User user) {
        double value = impl.getValue(Abilities.GROWTH_AURA, user);
        return rollExtraStagesFromAbilityValue(value, ThreadLocalRandom.current());
    }

    @VisibleForTesting
    static int rollExtraStagesFromAbilityValue(double value, java.util.Random random) {
        int guaranteed = (int) (value / 100.0);
        double chance = (value - guaranteed * 100.0) / 100.0;
        int extra = random.nextDouble() < chance ? 1 : 0;
        return guaranteed + extra;
    }

    private static void applyExtraGrowthAfterAura(
            ServerWorld world, BlockPos pos, BlockState endAfterVanilla, int extraStages) {
        if (!world.isChunkLoaded(pos)) {
            return;
        }
        BlockState at = world.getBlockState(pos);
        if (at.getBlock() != endAfterVanilla.getBlock()) {
            return;
        }
        IntProperty ap = intAgeProperty(at);
        if (ap == null || !at.contains(ap) || !endAfterVanilla.contains(ap)) {
            return;
        }
        if (at.get(ap) != endAfterVanilla.get(ap)) {
            return;
        }
        int current = at.get(ap);
        int cap = maxAgeForBlockState(at, ap);
        int n = Math.min(current + extraStages, cap);
        if (n > current) {
            world.setBlockState(pos, at.with(ap, n), 3);
        }
    }

    @VisibleForTesting
    static int maxAgeForBlockState(BlockState at, IntProperty ap) {
        if (at.getBlock() instanceof CropBlock c) {
            return c.getMaxAge();
        }
        if (at.getBlock() instanceof NetherWartBlock) {
            return NetherWartBlock.MAX_AGE;
        }
        int m = 0;
        for (int v : ap.getValues()) {
            m = Math.max(m, v);
        }
        return m;
    }
}
