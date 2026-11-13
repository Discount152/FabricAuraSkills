package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.PotionSplashXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class FabricPotionSplashHandler {

    private FabricPotionSplashHandler() {
    }

    public static void onPotionCollision(AbstractThrownPotion potion) {
        if (potion.level().isClientSide()) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        Entity owner = potion.getOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = potion.getItem();
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
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
        User user = plugin.getUserManager().getUser(player.getUUID());
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
