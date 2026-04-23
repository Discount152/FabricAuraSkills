package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.GrindstoneXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public final class FabricGrindstoneXpHandler {

    private FabricGrindstoneXpHandler() {
    }

    /**
     * Paper/Bukkit: {@code SourceLeveler#failsClickChecks} for {@code GrindstoneLeveler} (empty cursor, non-left/right
     * clicks with full main storage, and a whitelist of successful takeout actions). When this returns false, the
     * Fabric mixin must not call {@link #onGrindstoneTake} — mirrors non-awarding clicks on the reference leveler.
     * <p>
     * Call only after a confirmed result-slot take (output stack actually changed); this method does not look at
     * item stacks — only the click + cursor/storage snapshot at {@code onSlotClick} start.
     */
    public static boolean allowsGrindstoneClickForXp(
            SlotActionType actionType, boolean cursorEmptyAtClickStart, boolean playerStorageFull) {
        if (!cursorEmptyAtClickStart) {
            return false;
        }
        if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.PICKUP_ALL) {
            return true;
        }
        if (playerStorageFull) {
            return false;
        }
        return actionType == SlotActionType.QUICK_MOVE
                || actionType == SlotActionType.SWAP
                || actionType == SlotActionType.THROW;
    }

    /**
     * Called from {@code ScreenHandlerGrindstoneXpMixin} after a confirmed take from the grindstone result slot,
     * when {@link #allowsGrindstoneClickForXp} passed for that interaction.
     */
    public static void onGrindstoneTake(
            ServerPlayerEntity player,
            GrindstoneScreenHandler handler,
            ItemStack topBefore,
            ItemStack bottomBefore) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        if (player.isSpectator()) {
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
        double multiplier = getTotalLevelMultiplier(plugin, source, skill, topBefore, bottomBefore);
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, source, multiplier * source.getXp());
    }

    private static double getTotalLevelMultiplier(FabricAuraSkills plugin, GrindstoneXpSource source, Skill skill, ItemStack top, ItemStack bottom) {
        double multiplier = 1;
        String multiplierString = source.getMultiplier();
        if (multiplierString != null) {
            int totalLevel = countDisenchantableLevels(top, plugin) + countDisenchantableLevels(bottom, plugin);
            multiplierString = TextUtil.replace(multiplierString, "{total_level}", String.valueOf(totalLevel));
            try {
                multiplier = Double.parseDouble(multiplierString);
            } catch (NumberFormatException e) {
                plugin.logger().warn("Invalid multiplier for grindstone source " + source.getId() + " in skill " + skill.getId());
                e.printStackTrace();
            }
        }
        return multiplier;
    }

    private static int countDisenchantableLevels(ItemStack stack, FabricAuraSkills plugin) {
        if (stack.isEmpty()) {
            return 0;
        }
        int total = 0;
        ItemEnchantmentsComponent stored = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (stored != null) {
            total += sumDisenchantableLevels(stored, plugin);
        }
        ItemEnchantmentsComponent ench = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (ench != null) {
            total += sumDisenchantableLevels(ench, plugin);
        }
        return total;
    }

    private static int sumDisenchantableLevels(ItemEnchantmentsComponent component, FabricAuraSkills plugin) {
        int sum = 0;
        for (var e : component.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> entry = e.getKey();
            if (!isDisenchantable(entry, plugin)) {
                continue;
            }
            sum += e.getIntValue();
        }
        return sum;
    }

    private static boolean isDisenchantable(RegistryEntry<Enchantment> entry, FabricAuraSkills plugin) {
        if (entry.isIn(EnchantmentTags.CURSE)) {
            return false;
        }
        String key = entry.getKey().map(k -> k.getValue().getPath()).orElse("");
        List<String> blocked = plugin.configStringList(Option.SOURCE_GRINDSTONE_BLOCKED_ENCHANTS);
        for (String blockedName : blocked) {
            if (blockedName != null && blockedName.equalsIgnoreCase(key)) {
                return false;
            }
        }
        return true;
    }
}
