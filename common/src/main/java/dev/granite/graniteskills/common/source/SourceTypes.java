package dev.granite.graniteskills.common.source;

import dev.granite.graniteskills.api.GraniteSkillsApi;
import dev.granite.graniteskills.api.registry.NamespacedId;
import dev.granite.graniteskills.api.source.SourceType;
import dev.granite.graniteskills.common.GraniteSkillsPlugin;
import dev.granite.graniteskills.common.api.ApiGraniteSkills;
import dev.granite.graniteskills.common.source.parser.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public enum SourceTypes implements SourceType {

    ANVIL(AnvilSourceParser.class),
    BLOCK(BlockSourceParser.class),
    BREWING(BrewingSourceParser.class),
    DAMAGE(DamageSourceParser.class),
    ENCHANTING(EnchantingSourceParser.class),
    ENTITY(EntitySourceParser.class),
    FISHING(FishingSourceParser.class),
    GRINDSTONE(GrindstoneSourceParser.class),
    ITEM_CONSUME(ItemConsumeSourceParser.class),
    JUMPING(JumpingSourceParser.class),
    MANA_ABILITY_USE(ManaAbilityUseSourceParser.class),
    POTION_SPLASH(PotionSplashSourceParser.class),
    STATISTIC(StatisticSourceParser.class);

    private final NamespacedId id;
    private final Class<? extends SourceParser<?>> parserClass;

    SourceTypes(Class<? extends SourceParser<?>> parserClass) {
        this.id = NamespacedId.of(NamespacedId.GRANITESKILLS, this.name().toLowerCase(Locale.ROOT));
        this.parserClass = parserClass;
    }

    @Override
    public NamespacedId getId() {
        return id;
    }

    @Override
    public SourceParser<?> getParser() {
        GraniteSkillsPlugin plugin = ((ApiGraniteSkills) GraniteSkillsApi.get()).getPlugin();
        try {
            return (SourceParser<?>) parserClass.getConstructors()[0].newInstance(plugin);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Could not instantiate parser class " + parserClass.getName());
        }
    }

    @Override
    public boolean isEnabled() {
        GraniteSkillsPlugin plugin = ((ApiGraniteSkills) GraniteSkillsApi.get()).getPlugin();
        return plugin.getSkillManager().isSourceEnabled(this);
    }

}
