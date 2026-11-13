package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.ManaAbilityUseXpSource;
import dev.granite.graniteskills.common.source.SourceTypes;
import dev.granite.graniteskills.common.user.User;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Awards XP for {@link ManaAbilityUseXpSource} rows. Paper fires {@code ManaAbilityActivateEvent} and
 * {@link dev.granite.graniteskills.bukkit.source.ManaAbilityUseLeveler} listens; on Fabric there is no Bukkit event, so any
 * future {@link dev.granite.graniteskills.fabric.FabricManaAbilityManager} provider should call
 * {@link #onManaAbilityActivated} after a successful activation (after mana is consumed), matching Paper timing.
 */
public final class FabricManaAbilityUseXpHandler {

    private FabricManaAbilityUseXpHandler() {
    }

    /**
     * @param manaUsed mana amount consumed for this activation (Paper uses {@code event.getManaUsed()})
     */
    public static void onManaAbilityActivated(ServerPlayer player, ManaAbility manaAbility, double manaUsed) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
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
        User user = plugin.getUserManager().getUser(player.getUUID());
        plugin.getLevelManager().addXp(user, skill, source, source.getXp() * manaUsed);
    }

    private static @Nullable SkillSource<ManaAbilityUseXpSource> findMatchingSource(FabricGraniteSkills plugin, ManaAbility manaAbility) {
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
