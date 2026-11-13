package dev.granite.graniteskills.common.source.type;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.source.SourceValues;
import dev.granite.graniteskills.api.source.type.ManaAbilityUseXpSource;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.Source;
import org.jetbrains.annotations.Nullable;

public class ManaAbilityUseSource extends Source implements ManaAbilityUseXpSource {

    private final ManaAbility[] manaAbilities;

    public ManaAbilityUseSource(GraniteSkillsPlugin plugin, SourceValues values, ManaAbility[] manaAbilities) {
        super(plugin, values);
        this.manaAbilities = manaAbilities;
    }

    @Override
    public @Nullable ManaAbility[] getManaAbilities() {
        return manaAbilities;
    }

}
