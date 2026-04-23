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
 * Awards XP for {@link ManaAbilityUseXpSource} rows. Bukkit fires {@code ManaAbilityActivateEvent} and
 * {@link dev.aurelium.auraskills.bukkit.source.ManaAbilityUseLeveler} multiplies {@link ManaAbilityUseXpSource#getXp()} by
 * {@code event.getManaUsed()}; Fabric has no Bukkit bus, so each active provider calls {@link #onManaAbilityActivated}
 * <strong>after</strong> mana is consumed, with the same {@code manaUsed} value.
 * <p>
 * <strong>Call sites (all {@link dev.aurelium.auraskills.fabric.FabricManaAbilityManager#registerProviders()}):</strong>
 * {@link FabricReplenish}, {@link FabricSpeedMine}, {@link FabricLightningBlade}, {@link FabricTreecapitator},
 * {@link FabricTerraform}, {@link FabricChargedShot}, {@link FabricAbsorption}, and {@link FabricSharpHook}
 * (via {@code ServerPlayerEntitySharpHookMixin}). Other mods must not add duplicate calls for the same activation.
 * <p>
 * <strong>Not on Fabric:</strong> Bukkit’s cancellable event + listener priority ordering; there is no third-party
 * “cancel ability activation and prevent XP” hook unless added later.
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
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.MANA_ABILITY_USE)) {
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
