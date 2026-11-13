package dev.granite.graniteskills.bukkit.source;

import dev.granite.graniteskills.api.event.mana.ManaAbilityActivateEvent;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.ManaAbilityUseXpSource;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.source.SourceTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.Nullable;

public class ManaAbilityUseLeveler extends SourceLeveler {

    public ManaAbilityUseLeveler(GraniteSkills plugin) {
        super(plugin, SourceTypes.MANA_ABILITY_USE);
    }

    @EventHandler
    public void onManaAbilityActivate(ManaAbilityActivateEvent event) {
        Player player = event.getPlayer();

        var skillSource = getSource(event.getManaAbility());
        if (skillSource == null) return;

        ManaAbilityUseXpSource source = skillSource.source();
        Skill skill = skillSource.skill();

        if (failsChecks(event, player, player.getLocation(), skill)) return;

        plugin.getLevelManager().addXp(plugin.getUser(player), skill, source, source.getXp() * event.getManaUsed());
    }

    @Nullable
    private SkillSource<ManaAbilityUseXpSource> getSource(ManaAbility manaAbility) {
        for (SkillSource<ManaAbilityUseXpSource> entry : plugin.getSkillManager().getSourcesOfType(ManaAbilityUseXpSource.class)) {
            ManaAbility[] manaAbilities = entry.source().getManaAbilities();
            if (manaAbilities == null) { // Source with no mana abilities specified always matches
                return entry;
            }

            for (ManaAbility search : manaAbilities) {
                if (manaAbility.equals(search)) {
                    return entry;
                }
            }
        }
        return null;
    }

}
