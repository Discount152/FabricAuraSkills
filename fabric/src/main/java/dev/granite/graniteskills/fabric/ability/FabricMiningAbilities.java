package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.stat.Stats;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.TimeUnit;

public final class FabricMiningAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    private static final String STAMINA_MODIFIER = "mining-stamina";

    public FabricMiningAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LUCKY_MINER, Abilities.MINER, Abilities.PICK_MASTER, Abilities.HARDENED_ARMOR, Abilities.STAMINA);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                refreshStamina();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    /**
     * Used by block levelers when deciding direct-drop behavior (ported from Bukkit {@code MiningAbilities}).
     */
    public boolean dropsMineralDirectly(BlockState state) {
        String p = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return p.contains("diamond_ore") || p.contains("redstone_ore") || p.contains("emerald_ore")
                || p.contains("coal_ore") || p.contains("lapis_ore") || p.contains("nether_quartz_ore")
                || p.contains("nether_gold_ore") || p.contains("iron_ore") || p.contains("gold_ore")
                || p.contains("copper_ore");
    }

    /**
     * Bukkit Hardened Armor: chance to cancel durability loss on armor pieces.
     *
     * @param forcedRoll pass a fixed {@code [0,1)} roll for probes; {@link Double#NaN} uses the ability RNG
     */
    public static boolean shouldCancelArmorDamage(ServerPlayer player, ItemStack stack, double forcedRoll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null || stack == null || stack.isEmpty() || !isArmor(stack)) {
            return false;
        }
        FabricMiningAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricMiningAbilities.class);
        var ability = Abilities.HARDENED_ARMOR;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
        return roll < (self.getValue(ability, user) / 100.0);
    }

    public static boolean shouldCancelArmorDamage(ServerPlayer player, ItemStack stack) {
        return shouldCancelArmorDamage(player, stack, Double.NaN);
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    @Override
    public void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        if (meta.getDamageType() != DamageType.PICKAXE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        meta.addAttackModifier(pickMaster(attacker, user));
    }

    private DamageModifier pickMaster(ServerPlayer player, User user) {
        var ability = Abilities.PICK_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    /** Probe/runtime refresh: holding a pickaxe grants Toughness from Stamina. */
    public static void refreshStamina(ServerPlayer player) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null) {
            return;
        }
        FabricMiningAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricMiningAbilities.class);
        self.refreshStaminaFor(player);
    }

    private void refreshStamina() {
        if (plugin.getServer() == null) {
            return;
        }
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            refreshStaminaFor(player);
        }
    }

    private void refreshStaminaFor(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        var ability = Abilities.STAMINA;
        if (isDisabled(ability)
                || failsChecks(player, ability)
                || user.getAbilityLevel(ability) <= 0
                || !player.getMainHandItem().is(ItemTags.PICKAXES)) {
            user.removeStatModifier(STAMINA_MODIFIER);
            return;
        }
        user.addStatModifier(
                new StatModifier(STAMINA_MODIFIER, Stats.TOUGHNESS, (int) getValue(ability, user), Operation.ADD),
                false);
    }
}
