package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

public final class FabricForgingAbilities extends FabricAbilityImpl {

    public FabricForgingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.DISENCHANTER, Abilities.FORGER, Abilities.REPAIRING, Abilities.ANVIL_MASTER, Abilities.SKILL_MENDER);
    }

    public static void onSkillXp(FabricGraniteSkills plugin, User user, double amount) {
        if (amount <= 0) {
            return;
        }
        ServerPlayer player = FabricUser.getPlayer(user);
        if (player == null) {
            return;
        }
        FabricForgingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricForgingAbilities.class);
        var ability = Abilities.SKILL_MENDER;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        if (self.rand.nextDouble() >= self.getValue(ability, user) / 100.0) {
            return;
        }
        Holder<Enchantment> mending = mending(player);
        if (mending == null) {
            return;
        }
        List<ItemStack> damaged = new ArrayList<>();
        collect(player.getMainHandItem(), mending, damaged);
        collect(player.getOffhandItem(), mending, damaged);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                collect(player.getItemBySlot(slot), mending, damaged);
            }
        }
        if (damaged.isEmpty()) {
            return;
        }
        ItemStack chosen = damaged.get(self.rand.nextInt(damaged.size()));
        int repair = (int) Math.round(amount / 2.0);
        chosen.setDamageValue(Math.max(0, chosen.getDamageValue() - repair));
    }

    public static boolean allowExpensiveAnvil(ServerPlayer player, int cost) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || cost <= 0) {
            return false;
        }
        FabricForgingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricForgingAbilities.class);
        var ability = Abilities.ANVIL_MASTER;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        int maxCost = (int) Math.round(self.getValue(ability, user));
        return cost <= maxCost;
    }

    private static void collect(ItemStack stack, Holder<Enchantment> mending, List<ItemStack> damaged) {
        if (stack.isEmpty() || stack.getDamageValue() <= 0) {
            return;
        }
        if (stack.getEnchantments().getLevel(mending) > 0) {
            damaged.add(stack);
        }
    }

    private static Holder<Enchantment> mending(ServerPlayer player) {
        RegistryAccess access = player.level().registryAccess();
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.MENDING).orElse(null);
    }
}
