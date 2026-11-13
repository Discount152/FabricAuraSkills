package dev.granite.graniteskills.common.trait;

import dev.granite.graniteskills.common.util.data.OptionProvider;

import java.util.Map;

public class TraitOptions extends OptionProvider {

    public TraitOptions(Map<String, Object> optionMap) {
        super(optionMap);
    }

    public boolean enabled() {
        return getBoolean("enabled");
    }

}
