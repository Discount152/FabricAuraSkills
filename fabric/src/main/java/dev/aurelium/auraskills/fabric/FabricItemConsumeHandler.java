package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.ItemConsumeXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricItemConsumeHandler {

    private FabricItemConsumeHandler() {
    }

    /**
     * Bukkit: {@code ItemConsumeLeveler} on {@code PlayerItemConsumeEvent} at Bukkit
     * {@code EventPriority.HIGHEST} (runs after lower priorities, so {@code LevelerContext#failsChecks} can observe a
     * prior cancel). Match uses {@code event.getItem()} (the stack being consumed). Fabric:
     * {@link net.minecraft.entity.LivingEntity#consumeItem()}
     * is invoked when vanilla <strong>commits</strong> consumption (food/potion/milk completion), not when the client
     * starts using the item; this is not a hook for {@code UseItemCallback} “start use” or interrupted mid-tick use.
     */
    public static void onConsumeItemCommit(ServerPlayerEntity player, ItemStack consumedStack) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || consumedStack.isEmpty()) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.ITEM_CONSUME)) {
            return;
        }
        SkillSource<ItemConsumeXpSource> match = null;
        for (SkillSource<ItemConsumeXpSource> entry : plugin.getSkillManager().getSourcesOfType(ItemConsumeXpSource.class)) {
            Skill skill = entry.skill();
            if (FabricLootItemMatcher.passesApiItemFilter(plugin, consumedStack, entry.source().getItem(), skill)) {
                match = entry;
                break;
            }
        }
        if (match == null) {
            return;
        }
        Skill skill = match.skill();
        ItemConsumeXpSource source = match.source();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.ITEM_CONSUME)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp());
    }
}
