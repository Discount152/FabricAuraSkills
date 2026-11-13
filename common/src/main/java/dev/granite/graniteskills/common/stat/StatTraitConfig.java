package dev.granite.graniteskills.common.stat;

import dev.granite.graniteskills.common.util.data.OptionProvider;

import java.util.Map;

public class StatTraitConfig extends OptionProvider {

    public StatTraitConfig(Map<String, Object> optionMap) {
        super(optionMap);
    }

    public double modifier() {
        return getDouble("modifier");
    }

}
