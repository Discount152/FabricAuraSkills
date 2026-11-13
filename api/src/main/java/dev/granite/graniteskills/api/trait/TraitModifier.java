package dev.granite.graniteskills.api.trait;

import dev.granite.graniteskills.api.util.GraniteSkillsModifier;

public class TraitModifier extends GraniteSkillsModifier<Trait> {

    public static final String ITEM_PREFIX = "GraniteSkills.TraitModifiers.";

    public TraitModifier(String name, Trait trait, double value, Operation operation) {
        super(name, trait, value, operation);
    }

    @Deprecated
    public TraitModifier(String name, Trait trait, double value) {
        this(name, trait, value, Operation.ADD);
    }

    public Trait trait() {
        return type;
    }

}
