package dev.granite.graniteskills.api.source.type;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.api.source.XpSource;
import org.jetbrains.annotations.Nullable;

public interface ManaAbilityUseXpSource extends XpSource {

    @Nullable
    ManaAbility[] getManaAbilities();

}
