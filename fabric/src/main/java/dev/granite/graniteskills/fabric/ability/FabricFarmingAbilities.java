package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.ability.Ability;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricFarmingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    public FabricFarmingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.BOUNTIFUL_HARVEST, Abilities.FARMER, Abilities.GENETICIST, Abilities.SCYTHE_MASTER, Abilities.GROWTH_AURA);
    }

    @Override
    public String replaceDescPlaceholders(String input, Ability ability, User user) {
        if (ability.equals(Abilities.GROWTH_AURA)) {
            return TextUtil.replace(input, "{radius}", String.valueOf(ability.optionInt("radius", 30)));
        }
        return input;
    }

    @Override
    public void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        if (meta.getDamageType() != DamageType.HOE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        meta.addAttackModifier(scytheMaster(attacker, user));
    }

    public static void onConsume(FabricGraniteSkills plugin, ServerPlayer player, ItemStack stack) {
        FabricFarmingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricFarmingAbilities.class);
        var ability = Abilities.GENETICIST;
        if (self.isDisabled(ability) || self.failsChecks(player, ability) || !isPlantBased(stack)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        float amount = (float) self.getValue(ability, user) / 10f;
        player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() + amount);
    }

    public static void onCropGrown(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return;
        }
        FabricFarmingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricFarmingAbilities.class);
        var ability = Abilities.GROWTH_AURA;
        if (self.isDisabled(ability)) {
            return;
        }
        int radius = ability.optionInt("radius", 30);
        double radiusSq = (double) radius * radius;
        BlockState state = server.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || crop.isMaxAge(state)) {
            return;
        }
        for (ServerPlayer player : server.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > radiusSq) {
                continue;
            }
            if (self.failsChecks(player, ability)) {
                continue;
            }
            User user = plugin.getUserManager().getUser(player.getUUID());
            int extra = self.rollExtraStages(user);
            if (extra <= 0) {
                continue;
            }
            BlockState current = server.getBlockState(pos);
            if (!(current.getBlock() instanceof CropBlock grown) || grown != crop) {
                return;
            }
            int age = grown.getAge(current);
            int next = Math.min(age + extra, grown.getMaxAge());
            if (next != age) {
                server.setBlock(pos, grown.getStateForAge(next), 2);
            }
            return;
        }
    }

    private DamageModifier scytheMaster(ServerPlayer player, User user) {
        var ability = Abilities.SCYTHE_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability) || user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    private int rollExtraStages(User user) {
        double value = getValue(Abilities.GROWTH_AURA, user);
        int guaranteed = (int) (value / 100);
        double chance = (value - guaranteed * 100) / 100.0;
        int extra = rand.nextDouble() < chance ? 1 : 0;
        return guaranteed + extra;
    }

    private static boolean isPlantBased(ItemStack stack) {
        return stack.is(Items.BREAD) || stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.POTATO) || stack.is(Items.BAKED_POTATO) || stack.is(Items.CARROT)
                || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.MELON_SLICE) || stack.is(Items.PUMPKIN_PIE)
                || stack.is(Items.BEETROOT) || stack.is(Items.BEETROOT_SOUP) || stack.is(Items.MUSHROOM_STEW)
                || stack.is(Items.POISONOUS_POTATO);
    }
}
