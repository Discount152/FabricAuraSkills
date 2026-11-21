package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.FabricUser;
import dev.granite.graniteskills.fabric.util.GrindstoneEnchant;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FabricForgingAbilities extends FabricAbilityImpl {

    public FabricForgingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.DISENCHANTER, Abilities.FORGER, Abilities.REPAIRING, Abilities.ANVIL_MASTER, Abilities.SKILL_MENDER);
    }

    /**
     * Bukkit Disenchanter parity: spawn bonus vanilla XP orbs when a grindstone result is taken.
     */
    public static void onGrindstoneDisenchant(ServerPlayer player, ItemStack topBefore, ItemStack bottomBefore) {
        int bonus = computeDisenchanterBonusXp(player, topBefore, bottomBefore);
        if (bonus <= 0 || !(player.level() instanceof ServerLevel world)) {
            return;
        }
        ExperienceOrb.award(world, player.position(), bonus);
    }

    /**
     * @return bonus experience points that Disenchanter would award (0 if gated or inapplicable)
     */
    public static int computeDisenchanterBonusXp(ServerPlayer player, ItemStack first, ItemStack second) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null) {
            return 0;
        }
        FabricForgingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricForgingAbilities.class);
        var ability = Abilities.DISENCHANTER;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return 0;
        }
        if (first != null && !first.isEmpty() && second != null && !second.isEmpty()
                && first.getItem() != second.getItem()) {
            return 0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        Set<EnchantEntry> enchants = new HashSet<>();
        collectDisenchantEntries(first, plugin, enchants);
        collectDisenchantEntries(second, plugin, enchants);
        if (enchants.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (EnchantEntry value : enchants) {
            try {
                GrindstoneEnchant table = GrindstoneEnchant.valueOf(value.name());
                sum += table.getLevel(value.level());
            } catch (IllegalArgumentException ignored) {
                // Enchant not in the grindstone XP table.
            }
        }
        if (sum <= 0) {
            return 0;
        }
        int average = (sum + (int) Math.ceil(((double) sum) / 2.0)) / 2;
        return (int) Math.round(average * (self.getValue(ability, user) / 100.0));
    }

    private static void collectDisenchantEntries(ItemStack stack, FabricGraniteSkills plugin, Set<EnchantEntry> out) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null) {
            addEntries(stored, plugin, out);
        }
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench != null) {
            addEntries(ench, plugin, out);
        }
    }

    private static void addEntries(ItemEnchantments component, FabricGraniteSkills plugin, Set<EnchantEntry> out) {
        for (var e : component.entrySet()) {
            Holder<Enchantment> entry = e.getKey();
            if (!isDisenchantable(entry, plugin)) {
                continue;
            }
            String name = entry.unwrapKey()
                    .map(k -> k.identifier().getPath().toUpperCase(Locale.ROOT))
                    .orElse("");
            if (name.isEmpty()) {
                continue;
            }
            out.add(new EnchantEntry(name, e.getIntValue()));
        }
    }

    private static boolean isDisenchantable(Holder<Enchantment> entry, FabricGraniteSkills plugin) {
        if (entry.is(EnchantmentTags.CURSE)) {
            return false;
        }
        String key = entry.unwrapKey().map(k -> k.identifier().getPath()).orElse("");
        List<String> blocked = plugin.configStringList(Option.SOURCE_GRINDSTONE_BLOCKED_ENCHANTS);
        for (String blockedName : blocked) {
            if (blockedName != null && blockedName.equalsIgnoreCase(key)) {
                return false;
            }
        }
        return true;
    }

    private record EnchantEntry(String name, int level) {}


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

    /**
     * Bukkit Repairing ability contribution: extra durability restored beyond vanilla's ~25% of max
     * when repairing with the item's raw material. Returns 0 when gated/inapplicable.
     *
     * <p>Vanilla baseline per material unit is {@code round(0.25 * maxDurability)}. Ability adds
     * {@code round(maxDurability * 0.25 * (value/100))} per unit, times {@code second.getCount()}.
     */
    public static int computeRepairingAddedDurability(ServerPlayer player, ItemStack first, ItemStack second) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null || first == null || second == null || first.isEmpty() || second.isEmpty()) {
            return 0;
        }
        FabricForgingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricForgingAbilities.class);
        var ability = Abilities.REPAIRING;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return 0;
        }
        ItemStack raw = rawMaterialFor(first);
        if (raw.isEmpty() || raw.getItem() != second.getItem()) {
            return 0;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        int max = first.getMaxDamage();
        if (max <= 0) {
            return 0;
        }
        long perUnit = Math.round(max * 0.25 * (self.getValue(ability, user) / 100.0));
        long total = perUnit * second.getCount();
        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.max(0, total);
    }

    private static ItemStack rawMaterialFor(ItemStack tool) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(tool.getItem()).getPath();
        if (path.startsWith("diamond_") && !path.contains("ore") && !path.contains("block") && !path.contains("horse")) {
            return new ItemStack(net.minecraft.world.item.Items.DIAMOND);
        }
        if ((path.startsWith("gold_") || path.startsWith("golden_"))
                && !path.contains("apple")
                && !path.contains("carrot")
                && !path.contains("horse")
                && !path.contains("block")
                && !path.contains("ingot")
                && !path.contains("nugget")
                && !path.contains("ore")) {
            return new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
        }
        if (path.startsWith("iron_")
                && !path.contains("bars")
                && !path.contains("door")
                && !path.contains("block")
                && !path.contains("ingot")
                && !path.contains("nugget")
                && !path.contains("ore")
                && !path.contains("trapdoor")
                && !path.contains("horse")) {
            return new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
        }
        if (path.startsWith("netherite_")
                && !path.contains("scrap")
                && !path.contains("ingot")
                && !path.contains("block")) {
            return new ItemStack(net.minecraft.world.item.Items.NETHERITE_INGOT);
        }
        if (path.startsWith("leather_") && !path.contains("horse")) {
            return new ItemStack(net.minecraft.world.item.Items.LEATHER);
        }
        if (path.startsWith("chainmail")) {
            return new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
        }
        if ("elytra".equals(path)) {
            return new ItemStack(net.minecraft.world.item.Items.PHANTOM_MEMBRANE);
        }
        if ("turtle_helmet".equals(path)) {
            return new ItemStack(net.minecraft.world.item.Items.TURTLE_SCUTE);
        }
        return ItemStack.EMPTY;
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
