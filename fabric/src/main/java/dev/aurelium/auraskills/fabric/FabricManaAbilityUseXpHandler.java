package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.ManaAbilityUseXpSource;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Awards XP for {@link ManaAbilityUseXpSource} rows. Paper fires {@code ManaAbilityActivateEvent} and
 * {@link dev.aurelium.auraskills.bukkit.source.ManaAbilityUseLeveler} listens; on Fabric there is no Bukkit event, so any
 * future {@link dev.aurelium.auraskills.fabric.FabricManaAbilityManager} provider should call
 * {@link #onManaAbilityActivated} after a successful activation (after mana is consumed), matching Paper timing.
 */
public final class FabricManaAbilityUseXpHandler {

    private FabricManaAbilityUseXpHandler() {
    }

    /**
     * @param manaUsed mana amount consumed for this activation (Paper uses {@code event.getManaUsed()})
     */
    public static void onManaAbilityActivated(ServerPlayerEntity player, ManaAbility manaAbility, double manaUsed) {
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null || manaUsed <= 0) {
            return;
        }
        SkillSource<ManaAbilityUseXpSource> match = findMatchingSource(plugin, manaAbility);
        if (match == null) {
            return;
        }
        Skill skill = match.skill();
        ManaAbilityUseXpSource source = match.source();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.MANA_ABILITY_USE)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp() * manaUsed);
    }

    private static @Nullable SkillSource<ManaAbilityUseXpSource> findMatchingSource(FabricAuraSkills plugin, ManaAbility manaAbility) {
        for (SkillSource<ManaAbilityUseXpSource> entry : plugin.getSkillManager().getSourcesOfType(ManaAbilityUseXpSource.class)) {
            ManaAbilityUseXpSource source = entry.source();
            ManaAbility[] filter = source.getManaAbilities();
            if (filter == null) {
                return entry;
            }
            for (ManaAbility allowed : filter) {
                if (manaAbility.equals(allowed)) {
                    return entry;
                }
            }
        }
        return null;
    }
}
