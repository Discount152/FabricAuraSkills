package dev.granite.graniteskills.api.stat;

import dev.granite.graniteskills.api.util.GraniteSkillsModifier;

public class StatModifier extends GraniteSkillsModifier<Stat> {

    public static final String ITEM_PREFIX = "GraniteSkills.Modifiers.";

    public StatModifier(String name, Stat stat, double value, Operation operation) {
        super(name, stat, value, operation);
    }

    @Deprecated
    public StatModifier(String name, Stat stat, double value) {
        this(name, stat, value, Operation.ADD);
    }

    public Stat stat() {
        return type;
    }

}
