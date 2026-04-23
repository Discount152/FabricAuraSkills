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

    /**
     * Bukkit: {@code ThrownPotion#getEffects()}.isEmpty() — no splash XP. Missing {@link PotionContentsComponent} is
     * treated as no effect (Bukkit: empty or invalid stack).
     */
    public static boolean lacksPotionEffectForParity(PotionContentsComponent contents) {
        if (contents == null) {
            return true;
        }
        return contents.potion().isEmpty() && contents.customEffects().isEmpty();
    }

    /**
     * Bukkit: {@code PotionSplashLeveler#handle(ProjectileHitEvent)} on {@code PotionSplashEvent} and
     * {@code LingeringPotionSplashEvent} — only {@link net.minecraft.server.network.ServerPlayerEntity} thrower,
     * non-empty effect list, first matching source. Fabric: start of
     * {@link net.minecraft.entity.projectile.thrown.PotionEntity#onCollision} (splash + **lingering** throw share this
     * entity; the cloud is a separate follow-up, not a second award here). Bukkit {@code ProjectileHitEvent#isCancelled} /
     * location region: N/A; use {@link FabricXpHandlerUtil#shouldBlockXp} only.
     */
    public static void onPotionCollision(PotionEntity potion) {
        if (potion.getEntityWorld().isClient()) {
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return;
        }
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.POTION_SPLASH)) {
            return;
        }
        Entity owner = potion.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        ItemStack stack = potion.getStack();
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (lacksPotionEffectForParity(contents)) {
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
