package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.ItemConsumeXpSource;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.ability.FabricAgilityAbilities;
import dev.granite.graniteskills.fabric.ability.FabricFarmingAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FabricItemConsumeHandler {

    private FabricItemConsumeHandler() {
    }

    /**
     * Called from {@link dev.granite.graniteskills.fabric.mixin.LivingEntityConsumeMixin} at start of {@code LivingEntity.consumeItem()}.
     */
    public static void onConsumeStarting(ServerPlayer player, ItemStack consumedStack) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || consumedStack.isEmpty()) {
            return;
        }
        FabricAgilityAbilities.onConsume(plugin, player, consumedStack);
        FabricFarmingAbilities.onConsume(plugin, player, consumedStack);
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
        User user = plugin.getUserManager().getUser(player.getUUID());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp());
    }
}
