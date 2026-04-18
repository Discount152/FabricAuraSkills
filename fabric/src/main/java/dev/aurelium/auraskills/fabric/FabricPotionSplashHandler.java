package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.PotionSplashXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricPotionSplashHandler {

    private FabricPotionSplashHandler() {
    }

    public static void onPotionCollision(PotionEntity potion) {
        if (potion.getEntityWorld().isClient()) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        Entity owner = potion.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) {
            return;
        }
        ItemStack stack = potion.getStack();
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents != null && contents.potion().isEmpty() && contents.customEffects().isEmpty()) {
            return;
        }
        SkillSource<PotionSplashXpSource> skillSource = findSource(plugin, stack);
        if (skillSource == null) {
            return;
        }
        PotionSplashXpSource source = skillSource.source();
        Skill skill = skillSource.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.POTION_SPLASH)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp());
    }

    private static SkillSource<PotionSplashXpSource> findSource(FabricAuraSkills plugin, ItemStack stack) {
        for (SkillSource<PotionSplashXpSource> entry : plugin.getSkillManager().getSourcesOfType(PotionSplashXpSource.class)) {
            Skill skill = entry.skill();
            if (FabricLootItemMatcher.passesApiItemFilter(plugin, stack, entry.source().getItem(), skill)) {
                return entry;
            }
        }
        return null;
    }
}
