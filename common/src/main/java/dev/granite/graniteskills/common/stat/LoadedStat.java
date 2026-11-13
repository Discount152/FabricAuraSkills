package dev.granite.graniteskills.common.stat;

import com.google.common.collect.ImmutableList;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.api.trait.Trait;

import java.util.Map;

public record LoadedStat(Stat stat, ImmutableList<Trait> traits, Map<Trait, StatTraitConfig> traitConfigs,
        StatOptions options) {

}
