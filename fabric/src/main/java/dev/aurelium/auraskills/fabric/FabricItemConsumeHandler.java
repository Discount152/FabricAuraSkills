package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.ItemConsumeXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FabricItemConsumeHandler {

    private FabricItemConsumeHandler() {
    }

    /**
     * Called from {@link dev.aurelium.auraskills.fabric.mixin.LivingEntityConsumeMixin} at start of {@code LivingEntity.consumeItem()}.
     */
    public static void onConsumeStarting(ServerPlayer player, ItemStack consumedStack) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || consumedStack.isEmpty()) {
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
        User user = plugin.getUserManager().getUser(player.getUUID());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp());
    }
}
