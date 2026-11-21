package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.GrindstoneXpSource;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.ability.FabricForgingAbilities;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

public final class FabricGrindstoneXpHandler {

    private FabricGrindstoneXpHandler() {
    }

    /**
     * Called after the player successfully takes (or reduces) the grindstone result stack.
     */
    public static void onGrindstoneTake(ServerPlayer player, GrindstoneMenu handler, ItemStack topBefore, ItemStack bottomBefore) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return;
        }
        SkillSource<GrindstoneXpSource> skillSource = plugin.getSkillManager().getSingleSourceOfType(GrindstoneXpSource.class);
        if (skillSource == null) {
            return;
        }
        GrindstoneXpSource source = skillSource.source();
        Skill skill = skillSource.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.GRINDSTONE)) {
            return;
        }
        int totalLevel = countDisenchantableLevels(topBefore, plugin) + countDisenchantableLevels(bottomBefore, plugin);
        double xp = GrindstoneXpAward.xp(source.getXp(), source.getMultiplier(), totalLevel);
        if (xp <= 0.0) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        plugin.getLevelManager().addXp(user, skill, source, xp);
        FabricForgingAbilities.onGrindstoneDisenchant(player, topBefore, bottomBefore);
    }

    private static int countDisenchantableLevels(ItemStack stack, FabricGraniteSkills plugin) {
        if (stack.isEmpty()) {
            return 0;
        }
        int total = 0;
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null) {
            total += sumDisenchantableLevels(stored, plugin);
        }
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench != null) {
            total += sumDisenchantableLevels(ench, plugin);
        }
        return total;
    }

    private static int sumDisenchantableLevels(ItemEnchantments component, FabricGraniteSkills plugin) {
        int sum = 0;
        for (var e : component.entrySet()) {
            Holder<Enchantment> entry = e.getKey();
            if (!isDisenchantable(entry, plugin)) {
                continue;
            }
            sum += e.getIntValue();
        }
        return sum;
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
}
