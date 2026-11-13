package dev.granite.graniteskills.common.source.parser;

import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.source.ConfigurateSourceContext;
import dev.granite.graniteskills.common.source.type.ManaAbilityUseSource;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ManaAbilityUseSourceParser extends SourceParser<ManaAbilityUseSource> {

    public ManaAbilityUseSourceParser(GraniteSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public ManaAbilityUseSource parse(ConfigurationNode source, ConfigurateSourceContext context) throws SerializationException {
        ManaAbility[] manaAbilities = context.pluralizedArray("mana_ability", source, ManaAbility.class);

        return new ManaAbilityUseSource(plugin, context.parseValues(source), manaAbilities);
    }

}
