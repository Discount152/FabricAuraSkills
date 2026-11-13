package dev.granite.graniteskills.common.stat;

import dev.granite.graniteskills.common.util.data.OptionProvider;

import java.util.Map;

public class StatOptions extends OptionProvider {

    public StatOptions(Map<String, Object> optionMap) {
        super(optionMap);
    }

    public boolean enabled() {
        return getBoolean("enabled");
    }

}
